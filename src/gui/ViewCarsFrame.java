package gui;

import database.DatabaseConnection;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.sql.*;

public class ViewCarsFrame extends JFrame implements CarUpdateListener {
    private JTable carsTable;
    private DefaultTableModel tableModel;

    public ViewCarsFrame() {
        setTitle("Available Cars");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create table model
        String[] columns = {"Car ID", "Car Name", "Car Type", "Rent Per Day", "Available"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create table
        carsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(carsTable);
        add(scrollPane);

        // Register for car updates
        CarUpdateManager.getInstance().addListener(this);

        // Load cars data
        loadCarsData();

        // Remove listener when frame is closed
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                CarUpdateManager.getInstance().removeListener(ViewCarsFrame.this);
            }
        });
    }

    @Override
    public void onCarUpdate() {
        refreshData();
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        loadCarsData();
    }

    private void loadCarsData() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT c.*, " +
                           "(SELECT COUNT(*) FROM rentals r " +
                           "WHERE r.car_id = c.car_id AND r.return_date >= CURRENT_DATE) as active_rentals " +
                           "FROM cars c";
                           
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {

                    while (rs.next()) {
                        boolean isAvailable = rs.getBoolean("is_available") && rs.getInt("active_rentals") == 0;
                        Object[] row = {
                            rs.getInt("car_id"),
                            rs.getString("car_name"),
                            rs.getString("car_type"),
                            rs.getDouble("rent_per_day"),
                            isAvailable ? "Yes" : "No"
                        };
                        tableModel.addRow(row);
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
}
