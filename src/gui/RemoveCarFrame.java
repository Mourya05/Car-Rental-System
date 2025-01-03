package gui;

import database.DatabaseConnection;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import java.awt.GridLayout;
import java.sql.*;

public class RemoveCarFrame extends JFrame {
    private JComboBox<String> carComboBox;
    private JButton removeButton;

    public RemoveCarFrame() {
        setTitle("Remove Car");
        setSize(300, 150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create panel
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Initialize components
        panel.add(new JLabel("Select Car to Remove:"));
        carComboBox = new JComboBox<>();
        loadAvailableCars();
        panel.add(carComboBox);

        removeButton = new JButton("Remove Car");
        removeButton.addActionListener(e -> removeCar());
        panel.add(removeButton);

        add(panel);
    }

    private void loadAvailableCars() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT c.car_id, c.car_name, c.car_type, " +
                           "(SELECT COUNT(*) FROM rentals r WHERE r.car_id = c.car_id AND r.return_date >= CURRENT_DATE) as active_rentals " +
                           "FROM cars c " +
                           "WHERE c.is_available = true";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    while (rs.next()) {
                        if (rs.getInt("active_rentals") == 0) {
                            String carInfo = String.format("%d - %s (%s)", 
                                rs.getInt("car_id"),
                                rs.getString("car_name"),
                                rs.getString("car_type"));
                            carComboBox.addItem(carInfo);
                        }
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

    private void removeCar() {
        if (carComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a car", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedCar = carComboBox.getSelectedItem().toString();
        int carId = Integer.parseInt(selectedCar.split(" - ")[0]);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to remove this car?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                // Start transaction
                conn.setAutoCommit(false);
                try {
                    // First check if car has any active rentals
                    String checkSql = "SELECT COUNT(*) FROM rentals WHERE car_id = ? AND return_date >= CURRENT_DATE";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                        checkStmt.setInt(1, carId);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next() && rs.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(this,
                                "Cannot remove car: It has active rentals",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // If no active rentals, mark car as unavailable
                    String updateSql = "UPDATE cars SET is_available = false WHERE car_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, carId);
                        int rowsAffected = updateStmt.executeUpdate();
                        
                        if (rowsAffected > 0) {
                            conn.commit();
                            JOptionPane.showMessageDialog(this,
                                "Car removed successfully",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                                
                            // Notify all listeners about the car update
                            CarUpdateManager.getInstance().notifyCarUpdate();
                            
                            // Refresh the car list
                            carComboBox.removeAllItems();
                            loadAvailableCars();
                        } else {
                            conn.rollback();
                            JOptionPane.showMessageDialog(this,
                                "Failed to remove car",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
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
                "Error removing car: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
