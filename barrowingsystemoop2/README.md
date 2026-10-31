# CCS Item Borrowing System

CSIT228 project of group **Tulo ka TAO** (Section G02). Maven project, **Java 17 or newer**.

## Project structure

```
ItemBorrowingSystem/
├── pom.xml
├── database/
│   └── schema.sql                          MySQL tables and sample data
├── src/
│   ├── main/java/com/tulokatao/borrowing/
│   │   ├── model/                          entity classes and enums
│   │   │   ├── Role.java
│   │   │   ├── ItemStatus.java
│   │   │   ├── RequestStatus.java
│   │   │   ├── User.java
│   │   │   ├── Item.java
│   │   │   └── BorrowRequest.java
│   │   ├── service/                        business rules (all 8 features)
│   │   │   └── BorrowingSystem.java
│   │   └── ui/                             user interfaces
│   │       ├── ItemBorrowingGUI.java       Swing version (recommended)
│   │       └── Main.java                   console version
│   └── test/java/com/tulokatao/borrowing/  unit tests (for Part 8)
└── README.md
```

## Build and run

Run these in the `ItemBorrowingSystem` folder (needs Maven and a JDK installed).

```
mvn compile                  # compile
mvn exec:java                # run the Swing GUI
mvn exec:java -Dexec.mainClass=com.tulokatao.borrowing.ui.Main     # run the console version
mvn package                  # build target/item-borrowing-system-1.0.0.jar
java -jar target/item-borrowing-system-1.0.0.jar                   # run the jar
```

### In an IDE

- **IntelliJ IDEA:** File > Open, choose the folder (or `pom.xml`), then run `ItemBorrowingGUI`.
- **NetBeans:** File > Open Project, choose the folder, then run `ItemBorrowingGUI`.
- **Eclipse:** File > Import > Existing Maven Projects.

## Test accounts

| Role     | Username   | Password   |
|----------|------------|------------|
| Admin    | `admin`    | `admin123` |
| Borrower | `student1` | `pass123`  |
| Borrower | `student2` | `pass123`  |

Data is kept in memory, so it resets when the program closes. `database/schema.sql` is the design for the MySQL version (Part 5): uncomment the `mysql-connector-j` dependency in `pom.xml` when you add the connection.
