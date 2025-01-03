package gui;

import database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BookCarFrame extends JFrame implements CarUpdateListener {
    private JComboBox<String> carComboBox;
    private JTextField customerNameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JSpinner daysSpinner;
    private JButton bookButton;

    public BookCarFrame() {
        setTitle("Book a Car");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Car selection
        panel.add(new JLabel("Select Car:"));
        carComboBox = new JComboBox<>();
        panel.add(carComboBox);

        // Customer name
        panel.add(new JLabel("Customer Name:"));
        customerNameField = new JTextField();
        panel.add(customerNameField);

        // Phone number
        panel.add(new JLabel("Phone Number:"));
        phoneField = new JTextField();
        panel.add(phoneField);

        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);

        // Rental days
        panel.add(new JLabel("Number of Days:"));
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 30, 1);
        daysSpinner = new JSpinner(spinnerModel);
        panel.add(daysSpinner);

        // Book button
        bookButton = new JButton("Book Car");
        bookButton.addActionListener(e -> bookCar());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(bookButton);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Register for car updates
        CarUpdateManager.getInstance().addListener(this);

        // Load available cars
        loadAvailableCars();

        // Remove listener when frame is closed
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                CarUpdateManager.getInstance().removeListener(BookCarFrame.this);
            }
        });
    }

    @Override
    public void onCarUpdate() {
        refreshData();
    }

    public void refreshData() {
        carComboBox.removeAllItems();
        loadAvailableCars();
    }

    private void loadAvailableCars() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT c.car_id, c.car_name, c.car_type, c.rent_per_day " +
                           "FROM cars c " +
                           "WHERE c.is_available = true " +
                           "AND NOT EXISTS (SELECT 1 FROM rentals r " +
                           "WHERE r.car_id = c.car_id AND r.return_date >= CURRENT_DATE)";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    while (rs.next()) {
                        String carInfo = String.format("%d - %s (%s) - $%.2f/day", 
                            rs.getInt("car_id"),
                            rs.getString("car_name"),
                            rs.getString("car_type"),
                            rs.getDouble("rent_per_day"));
                        carComboBox.addItem(carInfo);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading cars: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bookCar() {
        if (carComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a car");
            return;
        }

        String customerName = customerNameField.getText().trim();
        String phoneNumber = phoneField.getText().trim();
        String email = emailField.getText().trim();
        int days = (Integer) daysSpinner.getValue();

        if (customerName.isEmpty() || phoneNumber.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                conn.setAutoCommit(false);
                try {
                    // Extract car_id from the selected item
                    String selectedCar = carComboBox.getSelectedItem().toString();
                    int carId = Integer.parseInt(selectedCar.split(" - ")[0]);

                    // Insert customer
                    String insertCustomerSql = "INSERT INTO customers (customer_name, phone_number, email) VALUES (?, ?, ?)";
                    int customerId;
                    try (PreparedStatement pstmt = conn.prepareStatement(insertCustomerSql, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setString(1, customerName);
                        pstmt.setString(2, phoneNumber);
                        pstmt.setString(3, email);
                        pstmt.executeUpdate();

                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                customerId = generatedKeys.getInt(1);
                            } else {
                                throw new SQLException("Failed to get customer ID");
                            }
                        }
                    }

                    // Calculate return date
                    java.sql.Date returnDate = new java.sql.Date(new Date().getTime() + (days * 86400000L));

                    // Insert rental
                    String insertRentalSql = "INSERT INTO rentals (car_id, customer_id, rental_date, return_date) VALUES (?, ?, CURRENT_DATE, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertRentalSql)) {
                        pstmt.setInt(1, carId);
                        pstmt.setInt(2, customerId);
                        pstmt.setDate(3, returnDate);
                        pstmt.executeUpdate();
                    }

                    // Update car availability
                    String updateCarSql = "UPDATE cars SET is_available = false WHERE car_id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateCarSql)) {
                        pstmt.setInt(1, carId);
                        pstmt.executeUpdate();
                    }

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Car booked successfully!");
                    
                    // Notify all listeners about the car update
                    CarUpdateManager.getInstance().notifyCarUpdate();
                    
                    // Clear fields
                    customerNameField.setText("");
                    phoneField.setText("");
                    emailField.setText("");
                    daysSpinner.setValue(1);
                    refreshData();
                    
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error booking car: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
