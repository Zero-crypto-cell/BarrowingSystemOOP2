package com.tulokatao.borrowing.model;

public class User {
    private final int id;
    private final String username;
    private final String password;
    private final String fullName;
    private final Role role;

    public User(int id, String username, String password, String fullName, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public boolean checkPassword(String input) { return password.equals(input); }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    public boolean isAdmin() { return role == Role.ADMIN; }

    @Override
    public String toString() { return fullName + " (" + role + ")"; }
}
