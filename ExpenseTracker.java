
import java.sql.*;
import java.util.Scanner;
import java.math.BigDecimal;

public class ExpenseTracker {

    static final String URL =
        "jdbc:mysql://localhost:3306/expense_tracker";
    static final String USER = "root";
    static final String PASSWORD = "pradnya@7555";

    static Scanner sc = new Scanner(System.in);

    public static Connection getConnection()
            throws SQLException {
        return DriverManager.getConnection(
            URL, USER, PASSWORD);
    }

    public static void addExpense() {
        try (Connection con = getConnection()) {

            System.out.print("Enter expense title: ");
            String title = sc.nextLine();

            System.out.print("Enter amount: ");
            BigDecimal amount =
                new BigDecimal(sc.nextLine());

            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                System.out.println("Amount cannot be negative.");
                return;
            }

            System.out.print("Enter category: ");
            String category = sc.nextLine();

            System.out.print("Enter date (YYYY-MM-DD): ");
            Date date = Date.valueOf(sc.nextLine());

            String sql = "INSERT INTO expenses " +
                "(title, amount, category, expense_date) " +
                "VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setString(1, title);
                ps.setBigDecimal(2, amount);
                ps.setString(3, category);
                ps.setDate(4, date);

                ps.executeUpdate();

                System.out.println(
                    "Expense added successfully!");
            }

        } catch (SQLException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    
public static void viewExpenses() {
    String sql = "SELECT * FROM expenses ORDER BY id";

    try (Connection con = getConnection();
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(sql)) {

        System.out.println("\n------ ALL EXPENSES ------");

        boolean found = false;

        while (rs.next()) {
            found = true;

            System.out.println("ID: " + rs.getInt("id"));
            System.out.println("Title: " + rs.getString("title"));
            System.out.println("Amount: Rs. " + rs.getBigDecimal("amount"));
            System.out.println("Category: " + rs.getString("category"));
            System.out.println("Date: " + rs.getDate("expense_date"));
            System.out.println("--------------------------");
        }

        if (!found) {
            System.out.println("No expenses found.");
        }

    } catch (SQLException e) {
        System.out.println("Database Error: " + e.getMessage());
    }
}


    public static void searchExpense() {
        try (Connection con = getConnection()) {

            System.out.print("Enter category to search: ");
            String category = sc.nextLine();

            String sql = "SELECT * FROM expenses " +
                         "WHERE LOWER(category) = LOWER(?)";

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setString(1, category);

                try (ResultSet rs = ps.executeQuery()) {
                    boolean found = false;

                    while (rs.next()) {
                        found = true;

                        System.out.println(
                            "\nID: " + rs.getInt("id"));
                        System.out.println(
                            "Title: " +
                            rs.getString("title"));
                        System.out.println(
                            "Amount: Rs. " +
                            rs.getBigDecimal("amount"));
                        System.out.println(
                            "Date: " +
                            rs.getDate("expense_date"));
                    }

                    if (!found) {
                        System.out.println(
                            "No expenses in this category.");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void updateExpense() {
        try (Connection con = getConnection()) {

            System.out.print("Enter expense ID: ");
            int id = Integer.parseInt(sc.nextLine());

            System.out.print("Enter new title: ");
            String title = sc.nextLine();

            System.out.print("Enter new amount: ");
            BigDecimal amount =
                new BigDecimal(sc.nextLine());

            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                System.out.println("Amount cannot be negative.");
                return;
            }

            System.out.print("Enter new category: ");
            String category = sc.nextLine();

            System.out.print("Enter new date (YYYY-MM-DD): ");
            Date date = Date.valueOf(sc.nextLine());

            String sql = "UPDATE expenses SET " +
                "title=?, amount=?, category=?, " +
                "expense_date=? WHERE id=?";

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setString(1, title);
                ps.setBigDecimal(2, amount);
                ps.setString(3, category);
                ps.setDate(4, date);
                ps.setInt(5, id);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    System.out.println(
                        "Expense updated successfully!");
                } else {
                    System.out.println("Expense ID not found.");
                }
            }

        } catch (SQLException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void deleteExpense() {
        try (Connection con = getConnection()) {

            System.out.print("Enter expense ID to delete: ");
            int id = Integer.parseInt(sc.nextLine());

            String sql = "DELETE FROM expenses WHERE id=?";

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setInt(1, id);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    System.out.println(
                        "Expense deleted successfully!");
                } else {
                    System.out.println("Expense ID not found.");
                }
            }

        } catch (SQLException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void totalExpense() {
        String sql = "SELECT COALESCE(SUM(amount), 0) " +
                     "AS total FROM expenses";

        try (Connection con = getConnection();
             PreparedStatement ps =
                 con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.println(
                    "Total Expenses: Rs. " +
                    rs.getBigDecimal("total"));
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void monthlyExpense() {
        try (Connection con = getConnection()) {

            System.out.print("Enter year (YYYY): ");
            int year = Integer.parseInt(sc.nextLine());

            System.out.print("Enter month (1-12): ");
            int month = Integer.parseInt(sc.nextLine());

            java.time.YearMonth ym =
                java.time.YearMonth.of(year, month);

            Date start = Date.valueOf(ym.atDay(1));
            Date end = Date.valueOf(
                ym.plusMonths(1).atDay(1));

            String sql = "SELECT COALESCE(SUM(amount), 0) " +
                "AS total FROM expenses " +
                "WHERE expense_date >= ? AND expense_date < ?";

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setDate(1, start);
                ps.setDate(2, end);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println(
                            "Monthly Expenses: Rs. " +
                            rs.getBigDecimal("total"));
                    }
                }
            }

        } catch (SQLException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        while (true) {
            System.out.println(
                "\n================================");
            System.out.println(
                "   EXPENSE TRACKER SYSTEM");
            System.out.println(
                "================================");

            System.out.println("1. Add Expense");
            System.out.println("2. View All Expenses");
            System.out.println("3. Search Expense");
            System.out.println("4. Update Expense");
            System.out.println("5. Delete Expense");
            System.out.println("6. Total Expenses");
            System.out.println("7. Monthly Expenses");
            System.out.println("8. Exit");

            System.out.print("Enter your choice: ");

            try {
                int choice =
                    Integer.parseInt(sc.nextLine());

                switch (choice) {
                    case 1:
                        addExpense();
                        break;

                    case 2:
                        viewExpenses();
                        break;

                    case 3:
                        searchExpense();
                        break;

                    case 4:
                        updateExpense();
                        break;

                    case 5:
                        deleteExpense();
                        break;

                    case 6:
                        totalExpense();
                        break;

                    case 7:
                        monthlyExpense();
                        break;

                    case 8:
                        System.out.println(
                            "Thank you for using Expense Tracker!");
                        sc.close();
                        return;

                    default:
                        System.out.println(
                            "Invalid choice! Try again.");
                }

            } catch (NumberFormatException e) {
                System.out.println(
                    "Please enter a valid number.");
            }
        }
    }
}
