package main;

import database.PostDAO;
import database.SocialMediaPlatformDAO;
import database.UserDAO;
import model.Post;
import model.SocialMedia;
import model.User;
import util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardFrame extends JFrame {
    private User currentUser;
    private JTabbedPane socialMediaTabs;
    private static final Logger LOGGER = Logger.getLogger(DashboardFrame.class.getName());

    public DashboardFrame(User user) {
        this.currentUser = user;

        // Set the theme based on user's preference
        try {
            UserDAO userDAO = new UserDAO();
            User updatedUser = userDAO.getUserById(user.getId());
            if (updatedUser != null) {
                this.currentUser = updatedUser;
                UIUtil.setTheme(currentUser.getThemePreference());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading user theme preference for ID: " + user.getId(), e);
            JOptionPane.showMessageDialog(this,
                    "Error loading theme preference: " + e.getMessage(),
                    "Dashboard Error",
                    JOptionPane.ERROR_MESSAGE);
            UIUtil.setTheme("light"); // Fallback to light theme
        }

        setTitle("Social Media Dashboard - Welcome " + user.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        JLabel welcomeLabel = UIUtil.createTitleLabel("Welcome to your Social Media Dashboard, " + user.getFullName());
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = UIUtil.createPrimaryButton("Logout");
        logoutButton.addActionListener(e -> logout());
        headerPanel.add(logoutButton, BorderLayout.EAST);

        socialMediaTabs = new JTabbedPane();

        try {
            SocialMediaPlatformDAO platformDAO = new SocialMediaPlatformDAO();
            List<SocialMedia> platforms = platformDAO.getAllPlatforms();

            for (SocialMedia platform : platforms) {
                socialMediaTabs.addTab(platform.getName(), createSocialMediaPanel(platform));
            }

            socialMediaTabs.addTab("Profile Settings", createProfileSettingsPanel());
            socialMediaTabs.addTab("AI Assistant", new AIAssistantPanel());
            socialMediaTabs.addTab("Analytics", new AnalyticsPanel(currentUser));

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading social media platforms", e);
            JOptionPane.showMessageDialog(this,
                    "Error loading social media platforms: " + e.getMessage(),
                    "Dashboard Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(socialMediaTabs, BorderLayout.CENTER);

        add(mainPanel);

        // Apply the theme to the entire frame
        UIUtil.applyTheme(this);

        setVisible(true);
    }

    private JPanel createSocialMediaPanel(SocialMedia platform) {
        return new SocialMediaPanel(platform, currentUser);
    }

    private JPanel createProfileSettingsPanel() {
        JPanel panel = UIUtil.createCardPanel();
        panel.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField fullNameField = UIUtil.createStyledTextField();
        fullNameField.setText(currentUser.getFullName());
        JTextField emailField = UIUtil.createStyledTextField();
        emailField.setText(currentUser.getEmail());
        JComboBox<String> themeComboBox = new JComboBox<>(new String[]{"light", "dark"});
        themeComboBox.setSelectedItem(currentUser.getThemePreference());

        formPanel.add(UIUtil.createStyledLabel("Full Name:", UIUtil.BODY_FONT, UIUtil.TEXT_PRIMARY));
        formPanel.add(fullNameField);
        formPanel.add(UIUtil.createStyledLabel("Email:", UIUtil.BODY_FONT, UIUtil.TEXT_PRIMARY));
        formPanel.add(emailField);
        formPanel.add(UIUtil.createStyledLabel("Theme Preference:", UIUtil.BODY_FONT, UIUtil.TEXT_PRIMARY));
        formPanel.add(themeComboBox);
        formPanel.add(new JLabel()); // Empty cell for alignment

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = UIUtil.createPrimaryButton("Save Changes");
        saveButton.addActionListener(e -> saveProfileChanges(fullNameField.getText(), emailField.getText(), (String) themeComboBox.getSelectedItem()));

        buttonPanel.add(saveButton);

        panel.add(UIUtil.createTitleLabel("Profile Settings"), BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void saveProfileChanges(String fullName, String email, String theme) {
        if (fullName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Full name and email cannot be empty",
                    "Profile Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address",
                    "Profile Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            currentUser.setFullName(fullName);
            currentUser.setEmail(email);
            currentUser.setThemePreference(theme);
            userDAO.updateUser(currentUser);

            // Update the theme immediately
            UIUtil.setTheme(theme);
            UIUtil.applyTheme(this);

            JOptionPane.showMessageDialog(this,
                    "Profile updated successfully!",
                    "Profile Success LOCKED",
                    JOptionPane.INFORMATION_MESSAGE);
            setTitle("Social Media Dashboard - Welcome " + fullName);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user profile for ID: " + currentUser.getId(), e);
            JOptionPane.showMessageDialog(this,
                    "Error updating profile: " + e.getMessage(),
                    "Profile Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        new LoginFrame();
        dispose();
    }
}