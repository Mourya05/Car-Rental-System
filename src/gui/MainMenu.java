package gui;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;

public class MainMenu extends JFrame {
    private JButton addCarButton;
    private JButton removeCarButton;
    private JButton bookCarButton;
    private JButton viewCarsButton;
    private JButton generateBillButton;

    public MainMenu() {
        setTitle("Car Rental System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        
        // Initialize components
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(5, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        addCarButton = new JButton("Add New Car");
        removeCarButton = new JButton("Remove Car");
        bookCarButton = new JButton("Book a Car");
        viewCarsButton = new JButton("View Available Cars");
        generateBillButton = new JButton("Generate Bill");

        // Add action listeners
        addCarButton.addActionListener(e -> new AddCarFrame().setVisible(true));
        removeCarButton.addActionListener(e -> new RemoveCarFrame().setVisible(true));
        bookCarButton.addActionListener(e -> new BookCarFrame().setVisible(true));
        viewCarsButton.addActionListener(e -> new ViewCarsFrame().setVisible(true));
        generateBillButton.addActionListener(e -> new GenerateBillFrame().setVisible(true));

        // Add components to panel
        mainPanel.add(addCarButton);
        mainPanel.add(removeCarButton);
        mainPanel.add(bookCarButton);
        mainPanel.add(viewCarsButton);
        mainPanel.add(generateBillButton);

        // Add panel to frame
        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainMenu().setVisible(true);
        });
    }
}
