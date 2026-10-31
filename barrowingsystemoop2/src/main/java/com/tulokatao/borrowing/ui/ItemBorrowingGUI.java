package com.tulokatao.borrowing.ui;

import com.tulokatao.borrowing.model.*;
import com.tulokatao.borrowing.service.BorrowingSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Swing GUI for the CCS Item Borrowing System. Run this class instead of Main. */
public class ItemBorrowingGUI extends JFrame {
    private final BorrowingSystem system = new BorrowingSystem();
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private JPanel mainPanel;

    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new ItemBorrowingGUI().setVisible(true));
    }

    public ItemBorrowingGUI() {
        super("CCS Item Borrowing System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(920, 580);
        setLocationRelativeTo(null);
        root.add(buildLoginPanel(), "login");
        add(root);
        cards.show(root, "login");
    }

    // ================= LOGIN =================
    private JPanel buildLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        JPanel box = new JPanel(new GridBagLayout());
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(24, 32, 24, 32)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Item Borrowing System", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel sub = new JLabel("CCS Department", SwingConstants.CENTER);

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; box.add(title, c);
        c.gridy = 1; box.add(sub, c);
        c.gridwidth = 1; c.gridy = 2; box.add(new JLabel("Username:"), c);
        c.gridx = 1; box.add(usernameField, c);
        c.gridx = 0; c.gridy = 3; box.add(new JLabel("Password:"), c);
        c.gridx = 1; box.add(passwordField, c);

        JButton loginBtn = new JButton("Log in");
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; box.add(loginBtn, c);

        JLabel hint = new JLabel("<html><center><small>Test: admin / admin123<br>student1 / pass123</small></center></html>",
                SwingConstants.CENTER);
        hint.setForeground(Color.GRAY);
        c.gridy = 5; box.add(hint, c);

        loginBtn.addActionListener(e -> doLogin());
        usernameField.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());

        outer.add(box);
        return outer;
    }

    private void doLogin() {
        User user = system.login(usernameField.getText().trim(), new String(passwordField.getPassword()));
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        passwordField.setText("");
        showMain(user);
    }

    private void showMain(User user) {
        if (mainPanel != null) root.remove(mainPanel);
        mainPanel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel welcome = new JLabel("Welcome, " + user);
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 15f));
        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> { usernameField.setText(""); cards.show(root, "login"); });
        top.add(welcome, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        mainPanel.add(top, BorderLayout.NORTH);
        mainPanel.add(user.isAdmin() ? buildAdminTabs() : buildBorrowerTabs(user), BorderLayout.CENTER);
        root.add(mainPanel, "main");
        cards.show(root, "main");
    }

    // ================= BORROWER =================
    private JTabbedPane buildBorrowerTabs(User user) {
        JTabbedPane tabs = new JTabbedPane();

        // Tab 1: search + request to borrow
        JTable itemsT = makeTable("ID", "Name", "Category", "Status");
        JTextField search = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JButton borrowBtn = new JButton("Request to Borrow");
        Runnable refreshItems = () -> fill(itemsT, itemRows(system.searchAvailable(search.getText())));
        searchBtn.addActionListener(e -> refreshItems.run());
        search.addActionListener(e -> refreshItems.run());
        borrowBtn.addActionListener(e -> {
            int id = selectedId(itemsT);
            if (id < 0) return;
            String date = JOptionPane.showInputDialog(this, "Return date (YYYY-MM-DD):", LocalDate.now().plusDays(7).toString());
            if (date == null) return;
            attempt(() -> {
                BorrowRequest r = system.requestBorrow(user, id, LocalDate.parse(date.trim()));
                JOptionPane.showMessageDialog(this, "Request #" + r.getId() + " sent. Status: pending.");
                refreshItems.run();
            });
        });
        tabs.addTab("Search Items", tablePanel(itemsT, row("Search:", search, searchBtn), row(borrowBtn)));

        // Tab 2: my history
        JTable histT = makeTable("Req #", "Item", "Borrower", "Requested", "Due", "Returned", "Status");
        Runnable refreshHist = () -> fill(histT, requestRows(system.getHistory(user)));
        tabs.addTab("My Borrowing History", tablePanel(histT, null, null));

        refreshItems.run();
        tabs.addChangeListener(e -> { refreshItems.run(); refreshHist.run(); });
        return tabs;
    }

    // ================= ADMIN =================
    private JTabbedPane buildAdminTabs() {
        JTabbedPane tabs = new JTabbedPane();
        List<Runnable> refreshers = new ArrayList<>();

        // Tab 1: items (search, add, update, remove)
        JTable itemsT = makeTable("ID", "Name", "Category", "Status");
        JTextField search = new JTextField(16);
        JCheckBox onlyAvail = new JCheckBox("Available only");
        JButton searchBtn = new JButton("Search");
        JButton addBtn = new JButton("Add Item");
        JButton updBtn = new JButton("Update Item");
        JButton delBtn = new JButton("Remove Item");
        Runnable refreshItems = () -> {
            String k = search.getText().trim().toLowerCase();
            List<Item> list = new ArrayList<>();
            for (Item i : system.getAllItems()) {
                boolean match = i.getName().toLowerCase().contains(k) || i.getCategory().toLowerCase().contains(k);
                if (match && (!onlyAvail.isSelected() || i.isAvailable())) list.add(i);
            }
            fill(itemsT, itemRows(list));
        };
        refreshers.add(refreshItems);
        searchBtn.addActionListener(e -> refreshItems.run());
        search.addActionListener(e -> refreshItems.run());
        onlyAvail.addActionListener(e -> refreshItems.run());
        addBtn.addActionListener(e -> {
            String[] f = itemForm("Add Item", "", "");
            if (f == null) return;
            attempt(() -> { system.addItem(f[0], f[1]); refreshItems.run(); });
        });
        updBtn.addActionListener(e -> {
            int id = selectedId(itemsT);
            if (id < 0) return;
            Item it = system.findItem(id);
            String[] f = itemForm("Update Item", it.getName(), it.getCategory());
            if (f == null) return;
            attempt(() -> { system.updateItem(id, f[0], f[1]); refreshItems.run(); });
        });
        delBtn.addActionListener(e -> {
            int id = selectedId(itemsT);
            if (id < 0) return;
            if (JOptionPane.showConfirmDialog(this, "Remove this item?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                attempt(() -> { system.removeItem(id); refreshItems.run(); });
        });
        tabs.addTab("Items", tablePanel(itemsT, row("Search:", search, onlyAvail, searchBtn), row(addBtn, updBtn, delBtn)));

        // Tab 2: pending requests (approve / reject)
        JTable reqT = makeTable("Req #", "Item", "Borrower", "Requested", "Due", "Returned", "Status");
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");
        Runnable refreshReq = () -> fill(reqT, requestRows(system.getPendingRequests()));
        refreshers.add(refreshReq);
        approveBtn.addActionListener(e -> {
            int id = selectedId(reqT);
            if (id < 0) return;
            attempt(() -> { system.approve(id); refreshReq.run(); });
        });
        rejectBtn.addActionListener(e -> {
            int id = selectedId(reqT);
            if (id < 0) return;
            attempt(() -> { system.reject(id); refreshReq.run(); });
        });
        tabs.addTab("Pending Requests", tablePanel(reqT, null, row(approveBtn, rejectBtn)));

        // Tab 3: record returns
        JTable borrowedT = makeTable("Req #", "Item", "Borrower", "Requested", "Due", "Returned", "Status");
        JButton returnBtn = new JButton("Record Return");
        Runnable refreshBorrowed = () -> fill(borrowedT, requestRows(system.getBorrowedRecords()));
        refreshers.add(refreshBorrowed);
        returnBtn.addActionListener(e -> {
            int id = selectedId(borrowedT);
            if (id < 0) return;
            attempt(() -> { system.recordReturn(id); refreshBorrowed.run(); });
        });
        tabs.addTab("Borrowed Items", tablePanel(borrowedT, null, row(returnBtn)));

        // Tab 4: full history
        JTable histT = makeTable("Req #", "Item", "Borrower", "Requested", "Due", "Returned", "Status");
        Runnable refreshHist = () -> fill(histT, requestRows(system.getHistory(
                new User(0, "", "", "", Role.ADMIN))));
        refreshers.add(refreshHist);
        tabs.addTab("Borrowing History", tablePanel(histT, null, null));

        for (Runnable r : refreshers) r.run();
        tabs.addChangeListener(e -> refreshers.get(tabs.getSelectedIndex()).run());
        return tabs;
    }

    // ================= HELPERS =================
    private void attempt(Runnable action) {
        try {
            action.run();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String[] itemForm(String title, String name, String category) {
        JTextField n = new JTextField(name, 18);
        JTextField c = new JTextField(category, 18);
        JPanel p = new JPanel(new GridLayout(2, 2, 6, 6));
        p.add(new JLabel("Name:")); p.add(n);
        p.add(new JLabel("Category:")); p.add(c);
        int r = JOptionPane.showConfirmDialog(this, p, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return r == JOptionPane.OK_OPTION ? new String[]{n.getText(), c.getText()} : null;
    }

    private static JTable makeTable(String... cols) {
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setRowHeight(24);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    private static void fill(JTable t, List<Object[]> rows) {
        DefaultTableModel m = (DefaultTableModel) t.getModel();
        m.setRowCount(0);
        for (Object[] r : rows) m.addRow(r);
    }

    private int selectedId(JTable t) {
        int r = t.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return -1;
        }
        return (Integer) t.getValueAt(r, 0);
    }

    private static List<Object[]> itemRows(List<Item> items) {
        List<Object[]> rows = new ArrayList<>();
        for (Item i : items) rows.add(new Object[]{i.getId(), i.getName(), i.getCategory(), i.getStatus()});
        return rows;
    }

    private static List<Object[]> requestRows(List<BorrowRequest> reqs) {
        List<Object[]> rows = new ArrayList<>();
        for (BorrowRequest r : reqs) {
            rows.add(new Object[]{r.getId(), r.getItem().getName(), r.getBorrower().getFullName(),
                    r.getRequestDate(), r.getDueDate(),
                    r.getReturnDate() == null ? "-" : r.getReturnDate(), r.getStatus()});
        }
        return rows;
    }

    private static JPanel row(Object... parts) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        for (Object o : parts) {
            if (o instanceof String) p.add(new JLabel((String) o));
            else p.add((Component) o);
        }
        return p;
    }

    private static JPanel tablePanel(JTable table, JPanel top, JPanel bottom) {
        JPanel p = new JPanel(new BorderLayout());
        if (top != null) p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        if (bottom != null) p.add(bottom, BorderLayout.SOUTH);
        return p;
    }
}
