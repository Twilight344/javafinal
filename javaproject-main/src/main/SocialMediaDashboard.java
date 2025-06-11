package main;

import database.DatabaseConnector;
import javax.swing.*;
import java.awt.*;

public class SocialMediaDashboard {
    public static void main(String[] args) {
        try {
            // Set the look and feel to the system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Initialize database
        try {
            DatabaseConnector.initialize();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to the database: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Start the application with the login screen
        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }
}