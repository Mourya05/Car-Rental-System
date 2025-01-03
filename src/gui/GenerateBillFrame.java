package gui;

import database.DatabaseConnection;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.Date;

public class GenerateBillFrame extends JFrame {
    private JComboBox<String> rentalComboBox;
    private JTextArea billArea;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public GenerateBillFrame() {
        setTitle("Generate Bill");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create top panel for combo box
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        topPanel.add(new JLabel("Select Rental:"));
        rentalComboBox = new JComboBox<>();
        loadRentals();
        topPanel.add(rentalComboBox);

        // Create button
        JButton generateButton = new JButton("Generate Bill");
        generateButton.addActionListener(e -> generateBill());

        // Create text area for bill
        billArea = new JTextArea();
        billArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(billArea);

        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(generateButton, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadRentals() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT r.rental_id, c.car_name, cu.customer_name " +
                           "FROM rentals r " +
                           "JOIN cars c ON r.car_id = c.car_id " +
                           "JOIN customers cu ON r.customer_id = cu.customer_id";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    String item = String.format("%d - %s (%s)", 
                        rs.getInt("rental_id"),
                        rs.getString("car_name"),
                        rs.getString("customer_name"));
                    rentalComboBox.addItem(item);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading rentals: " + e.getMessage());
        }
    }

    private void generateBill() {
        if (rentalComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a rental");
            return;
        }

        try {
            int rentalId = Integer.parseInt(rentalComboBox.getSelectedItem().toString().split(" - ")[0]);
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT r.rental_date, r.return_date, c.car_name, c.rent_per_day, " +
                           "cu.customer_name, cu.phone_number " +
                           "FROM rentals r " +
                           "JOIN cars c ON r.car_id = c.car_id " +
                           "JOIN customers cu ON r.customer_id = cu.customer_id " +
                           "WHERE r.rental_id = ?";
                
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, rentalId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    Date rentalDate = rs.getDate("rental_date");
                    Date returnDate = rs.getDate("return_date");
                    String carName = rs.getString("car_name");
                    double rentPerDay = rs.getDouble("rent_per_day");
                    String customerName = rs.getString("customer_name");
                    String phoneNumber = rs.getString("phone_number");

                    long diffInMillies = returnDate.getTime() - rentalDate.getTime();
                    long days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                    double totalAmount = days * rentPerDay;

                    // Generate bill
                    StringBuilder bill = new StringBuilder();
                    bill.append("=== CAR RENTAL BILL ===\n\n");
                    bill.append("Rental ID: ").append(rentalId).append("\n");
                    bill.append("Customer Name: ").append(customerName).append("\n");
                    bill.append("Phone Number: ").append(phoneNumber).append("\n");
                    bill.append("Car: ").append(carName).append("\n");
                    bill.append("Rental Date: ").append(dateFormat.format(rentalDate)).append("\n");
                    bill.append("Return Date: ").append(dateFormat.format(returnDate)).append("\n");
                    bill.append("Number of Days: ").append(days).append("\n");
                    bill.append("Rent Per Day: $").append(String.format("%.2f", rentPerDay)).append("\n");
                    bill.append("\nTotal Amount: $").append(String.format("%.2f", totalAmount)).append("\n");
                    bill.append("\nThank you for choosing our service!");

                    billArea.setText(bill.toString());
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error generating bill: " + e.getMessage());
        }
    }
}
