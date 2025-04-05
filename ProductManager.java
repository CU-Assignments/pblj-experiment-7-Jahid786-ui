// question 1
import java.sql.*;
import java.util.Scanner;

public class ProductManager {
    private static final String DB_URL = "jdbc:sqlite:products.db";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            createTable(conn);
            runMenu(conn);
        } catch (SQLException e) {
            System.out.println("❌ Database connection error: " + e.getMessage());
        }
    }

    private static void createTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS Product (
                ProductID INTEGER PRIMARY KEY AUTOINCREMENT,
                ProductName TEXT NOT NULL,
                Price REAL NOT NULL,
                Quantity INTEGER NOT NULL
            );
            """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static void runMenu(Connection conn) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== PRODUCT MANAGEMENT MENU ===");
            System.out.println("1. Add Product");
            System.out.println("2. View Products");
            System.out.println("3. Update Product");
            System.out.println("4. Delete Product");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1" -> createProduct(conn, scanner);
                    case "2" -> readProducts(conn);
                    case "3" -> updateProduct(conn, scanner);
                    case "4" -> deleteProduct(conn, scanner);
                    case "5" -> {
                        System.out.println("👋 Exiting...");
                        return;
                    }
                    default -> System.out.println("⚠️ Invalid option.");
                }
            } catch (SQLException e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private static void createProduct(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();
        System.out.print("Enter price: ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Enter quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine());

        String sql = "INSERT INTO Product (ProductName, Price, Quantity) VALUES (?, ?, ?)";
        conn.setAutoCommit(false);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, quantity);
            pstmt.executeUpdate();
            conn.commit();
            System.out.println("✅ Product added.");
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static void readProducts(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Product";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("\n%-10s %-20s %-10s %-10s%n", "ID", "Name", "Price", "Qty");
            while (rs.next()) {
                System.out.printf("%-10d %-20s %-10.2f %-10d%n",
                        rs.getInt("ProductID"),
                        rs.getString("ProductName"),
                        rs.getDouble("Price"),
                        rs.getInt("Quantity"));
            }
        }
    }

    private static void updateProduct(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter product ID to update: ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter new product name: ");
        String name = scanner.nextLine();
        System.out.print("Enter new price: ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Enter new quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine());

        String sql = "UPDATE Product SET ProductName = ?, Price = ?, Quantity = ? WHERE ProductID = ?";
        conn.setAutoCommit(false);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, quantity);
            pstmt.setInt(4, id);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                conn.commit();
                System.out.println("✅ Product updated.");
            } else {
                conn.rollback();
                System.out.println("⚠️ Product not found.");
            }
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static void deleteProduct(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter product ID to delete: ");
        int id = Integer.parseInt(scanner.nextLine());

        String sql = "DELETE FROM Product WHERE ProductID = ?";
        conn.setAutoCommit(false);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                conn.commit();
                System.out.println("✅ Product deleted.");
            } else {
                conn.rollback();
                System.out.println("⚠️ Product not found.");
            }
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}

// Question 2

public class Student {
    private int studentID;
    private String name;
    private String department;
    private double marks;

    // Constructor with ID (for update/read)
    public Student(int studentID, String name, String department, double marks) {
        this.studentID = studentID;
        this.name = name;
        this.department = department;
        this.marks = marks;
    }

    // Constructor without ID (for insert)
    public Student(String name, String department, double marks) {
        this.name = name;
        this.department = department;
        this.marks = marks;
    }

    // Getters and Setters
    public int getStudentID() { return studentID; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public double getMarks() { return marks; }

    public void setStudentID(int studentID) { this.studentID = studentID; }
    public void setName(String name) { this.name = name; }
    public void setDepartment(String department) { this.department = department; }
    public void setMarks(double marks) { this.marks = marks; }
}
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentController {
    private final Connection conn;

    public StudentController() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:students.db");
        createTable();
    }

    // Create table if not exists
    private void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS Student (
                StudentID INTEGER PRIMARY KEY AUTOINCREMENT,
                Name TEXT NOT NULL,
                Department TEXT NOT NULL,
                Marks REAL NOT NULL
            );
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    // Add a student
    public void addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO Student (Name, Department, Marks) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getName());
            stmt.setString(2, student.getDepartment());
            stmt.setDouble(3, student.getMarks());
            stmt.executeUpdate();
        }
    }

    // Retrieve all students
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM Student";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(new Student(
                        rs.getInt("StudentID"),
                        rs.getString("Name"),
                        rs.getString("Department"),
                        rs.getDouble("Marks")));
            }
        }
        return students;
    }

    // Update student
    public void updateStudent(Student student) throws SQLException {
        String sql = "UPDATE Student SET Name = ?, Department = ?, Marks = ? WHERE StudentID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getName());
            stmt.setString(2, student.getDepartment());
            stmt.setDouble(3, student.getMarks());
            stmt.setInt(4, student.getStudentID());
            stmt.executeUpdate();
        }
    }

    // Delete student
    public void deleteStudent(int studentID) throws SQLException {
        String sql = "DELETE FROM Student WHERE StudentID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentID);
            stmt.executeUpdate();
        }
    }

    // Close connection
    public void close() throws SQLException {
        conn.close();
    }
}
