package Market;

import java.sql.*;
import java.util.Scanner;

public class SuperMarketManagement {

    static final String URL =
        "jdbc:mysql://localhost:3306/supermarket_db";

    static final String USER = "root";
    static final String PASSWORD = "Password";

    static Scanner sc = new Scanner(System.in);

    // Database Connection
    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // 1. Add Product
    static void addProduct() {

        String sql = "INSERT INTO products VALUES(?,?,?,?,?,?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Product ID: ");
            ps.setInt(1, sc.nextInt());
            sc.nextLine();

            System.out.print("Product Name: ");
            ps.setString(2, sc.nextLine());

            System.out.print("Category: ");
            ps.setString(3, sc.nextLine());

            System.out.print("Price: ");
            ps.setDouble(4, sc.nextDouble());

            System.out.print("Quantity: ");
            ps.setInt(5, sc.nextInt());
            sc.nextLine();

            System.out.print("Expiry Date (YYYY-MM-DD): ");
            ps.setDate(6, Date.valueOf(sc.nextLine()));

            ps.executeUpdate();

            System.out.println("Product Added Successfully!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // 2. Display Products
    static void displayProducts() {

        String sql = "SELECT * FROM products";

        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n--- PRODUCT DETAILS ---");

            while (rs.next()) {

                System.out.println(
                    rs.getInt("product_id") + " | " +
                    rs.getString("name") + " | " +
                    rs.getString("category") + " | Rs." +
                    rs.getBigDecimal("price") + " | " +
                    rs.getInt("quantity") + " | " +
                    rs.getDate("expiry_date")
                );
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // 3. Search Product
    static void searchProduct() {

        System.out.println("1. Search by Product ID");
        System.out.println("2. Search by Category");

        int choice = sc.nextInt();
        sc.nextLine();

        String sql;

        if (choice == 1) {
            sql = "SELECT * FROM products WHERE product_id=?";
        } else if (choice == 2) {
            sql = "SELECT * FROM products WHERE category=?";
        } else {
            System.out.println("Invalid Choice!");
            return;
        }

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (choice == 1) {
                System.out.print("Enter Product ID: ");
                ps.setInt(1, sc.nextInt());
            } else {
                System.out.print("Enter Category: ");
                ps.setString(1, sc.nextLine());
            }

            ResultSet rs = ps.executeQuery();

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.println(
                    rs.getInt("product_id") + " | " +
                    rs.getString("name") + " | " +
                    rs.getString("category") + " | Rs." +
                    rs.getBigDecimal("price") + " | " +
                    rs.getInt("quantity")
                );
            }

            if (!found) {
                System.out.println("Product Not Found!");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // 4. Update Product Price and Quantity
    static void updateProduct() {

        String sql =
            "UPDATE products SET price=?, quantity=? WHERE product_id=?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter Product ID: ");
            int id = sc.nextInt();

            System.out.print("Enter New Price: ");
            double price = sc.nextDouble();

            System.out.print("Enter New Quantity: ");
            int quantity = sc.nextInt();

            ps.setDouble(1, price);
            ps.setInt(2, quantity);
            ps.setInt(3, id);

            int rows = ps.executeUpdate();

            if (rows > 0)
                System.out.println("Product Updated Successfully!");
            else
                System.out.println("Product Not Found!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // 5. Delete Product
    static void deleteProduct() {

        String sql = "DELETE FROM products WHERE product_id=?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter Product ID to Delete: ");
            int id = sc.nextInt();

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0)
                System.out.println("Product Deleted Successfully!");
            else
                System.out.println("Product Not Found!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // 6. Generate Customer Bill
    static void generateBill() {

        String selectSQL =
            "SELECT name, price, quantity FROM products WHERE product_id=?";

        String updateSQL =
            "UPDATE products SET quantity=quantity-? " +
            "WHERE product_id=? AND quantity>=?";

        try (Connection con = getConnection()) {

            con.setAutoCommit(false);

            double total = 0;

            try (PreparedStatement select =
                     con.prepareStatement(selectSQL);
                 PreparedStatement update =
                     con.prepareStatement(updateSQL)) {

                System.out.println("\n--- CUSTOMER BILL ---");

                while (true) {

                    System.out.print(
                        "Enter Product ID (0 to finish): ");

                    int id = sc.nextInt();

                    if (id == 0)
                        break;

                    System.out.print("Enter Quantity: ");
                    int qty = sc.nextInt();

                    if (qty <= 0) {
                        System.out.println("Invalid Quantity!");
                        continue;
                    }

                    select.setInt(1, id);

                    try (ResultSet rs = select.executeQuery()) {

                        if (!rs.next()) {
                            System.out.println("Product Not Found!");
                            continue;
                        }

                        String name = rs.getString("name");
                        double price = rs.getDouble("price");
                        int stock = rs.getInt("quantity");

                        if (qty > stock) {
                            System.out.println("Insufficient Stock!");
                            continue;
                        }

                        update.setInt(1, qty);
                        update.setInt(2, id);
                        update.setInt(3, qty);

                        if (update.executeUpdate() == 0) {
                            System.out.println("Stock Update Failed!");
                            continue;
                        }

                        double amount = price * qty;
                        total += amount;

                        System.out.println(
                            name + " | " + qty +
                            " x Rs." + price +
                            " = Rs." + amount
                        );
                    }
                }

                // Discount Calculation
                double discount = 0;

                if (total >= 5000)
                    discount = total * 0.10;
                else if (total >= 2000)
                    discount = total * 0.05;

                double finalAmount = total - discount;

                System.out.println("\n--- BILL SUMMARY ---");
                System.out.println("Total Amount: Rs." + total);
                System.out.println("Discount: Rs." + discount);
                System.out.println("Final Amount: Rs." + finalAmount);

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw e;
            }

            con.setAutoCommit(true);

        } catch (Exception e) {
            System.out.println("Billing Error: " + e.getMessage());
        }
    }

    // Main Menu
    public static void main(String[] args) {

        while (true) {

            System.out.println("\n===== SUPERMARKET MANAGEMENT =====");
            System.out.println("1. Add Product");
            System.out.println("2. Display Products");
            System.out.println("3. Search Product");
            System.out.println("4. Update Product");
            System.out.println("5. Delete Product");
            System.out.println("6. Generate Customer Bill");
            System.out.println("7. Exit");

            System.out.print("Enter Your Choice: ");

            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    addProduct();
                    break;

                case 2:
                    displayProducts();
                    break;

                case 3:
                    searchProduct();
                    break;

                case 4:
                    updateProduct();
                    break;

                case 5:
                    deleteProduct();
                    break;

                case 6:
                    generateBill();
                    break;

                case 7:
                    System.out.println("Thank You!");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid Choice!");
            }
        }
    }
}
