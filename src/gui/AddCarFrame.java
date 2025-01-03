package gui;

import database.DatabaseConnection;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddCarFrame extends JFrame {
    private JTextField carNameField;
    private JTextField carTypeField;
    private JTextField rentPerDayField;
    private JButton submitButton;

    public AddCarFrame() {
        setTitle("Add New Car");
        setSize(300, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create panel
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Initialize components
        panel.add(new JLabel("Car Name:"));
        carNameField = new JTextField();
        panel.add(carNameField);

        panel.add(new JLabel("Car Type:"));
        carTypeField = new JTextField();
        panel.add(carTypeField);

        panel.add(new JLabel("Rent Per Day:"));
        rentPerDayField = new JTextField();
        panel.add(rentPerDayField);

        submitButton = new JButton("Add Car");
        submitButton.addActionListener(e -> addCar());
        panel.add(submitButton);

        add(panel);
    }

    private void addCar() {
        String carName = carNameField.getText();
        String carType = carTypeField.getText();
        String rentPerDayText = rentPerDayField.getText();

        if (carName.isEmpty() || carType.isEmpty() || rentPerDayText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double rentPerDay = Double.parseDouble(rentPerDayText);
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String sql = "INSERT INTO cars (car_name, car_type, rent_per_day, is_available) VALUES (?, ?, ?, true)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, carName);
                    pstmt.setString(2, carType);
                    pstmt.setDouble(3, rentPerDay);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Car added successfully!");
                    clearFields();
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for rent", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding car: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        carNameField.setText("");
        carTypeField.setText("");
        rentPerDayField.setText("");
    }
}
