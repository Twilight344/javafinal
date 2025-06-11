package main;

import database.UserDAO;
import model.User;
import util.PasswordHasher;
import util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private static final Logger LOGGER = Logger.getLogger(LoginFrame.class.getName());

    public LoginFrame() {
        setTitle("Social Media Dashboard - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(2, 2, 10, 10));

        // Username
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formPanel.add(userLabel);

        usernameField = UIUtil.createStyledTextField();
        usernameField.setPreferredSize(new Dimension(200, 30));
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formPanel.add(usernameField);

        // Password
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formPanel.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 30));
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formPanel.add(passwordField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton loginButton = UIUtil.createPrimaryButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        buttonPanel.add(loginButton);

        JButton registerButton = UIUtil.createPrimaryButton("Sign Up");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSignupFrame();
            }
        });
        buttonPanel.add(registerButton);

        JLabel titleLabel = new JLabel("Welcome to Social Media Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Apply the default theme (light, since user isn't logged in yet)
        UIUtil.setTheme("light");
        UIUtil.applyTheme(this);

        setVisible(true);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username and password cannot be empty",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.getUserByUsername(username);

            if (user != null && PasswordHasher.verifyPassword(password, user.getPasswordHash())) {
                openDashboard(user);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error during login for username: " + username, e);
            JOptionPane.showMessageDialog(this,
                    "Database error: " + e.getMessage(),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSignupFrame() {
        new SignupFrame();
        dispose();
    }

    private void openDashboard(User user) {
        new DashboardFrame(user);
        dispose();
    }
}
