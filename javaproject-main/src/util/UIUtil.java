package util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UIUtil {
    private static final Logger LOGGER = Logger.getLogger(UIUtil.class.getName());

    // Font settings (unchanged)
    public static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font SUBHEADING_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    // Theme settings
    private static String currentTheme = "light"; // Default theme

    // Light theme colors (based on your existing colors)
    private static final Color LIGHT_PRIMARY_COLOR = new Color(63, 81, 181); // Material Indigo
    private static final Color LIGHT_ACCENT_COLOR = new Color(255, 64, 129); // Material Pink
    private static final Color LIGHT_BACKGROUND_COLOR = new Color(250, 250, 250);
    private static final Color LIGHT_CARD_BACKGROUND = Color.WHITE;
    private static final Color LIGHT_TEXT_PRIMARY = new Color(33, 33, 33);
    private static final Color LIGHT_TEXT_SECONDARY = new Color(117, 117, 117);

    // Dark theme colors (new)
    private static final Color DARK_PRIMARY_COLOR = new Color(100, 181, 246); // Lighter Indigo for dark theme
    private static final Color DARK_ACCENT_COLOR = new Color(255, 128, 171); // Softer Pink for dark theme
    private static final Color DARK_BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color DARK_CARD_BACKGROUND = new Color(50, 50, 50);
    private static final Color DARK_TEXT_PRIMARY = Color.WHITE;
    private static final Color DARK_TEXT_SECONDARY = new Color(150, 150, 150);

    // Theme-aware color variables (initially set to light theme)
    public static Color PRIMARY_COLOR = LIGHT_PRIMARY_COLOR;
    public static Color ACCENT_COLOR = LIGHT_ACCENT_COLOR;
    public static Color BACKGROUND_COLOR = LIGHT_BACKGROUND_COLOR;
    public static Color CARD_BACKGROUND = LIGHT_CARD_BACKGROUND;
    public static Color TEXT_PRIMARY = LIGHT_TEXT_PRIMARY;
    public static Color TEXT_SECONDARY = LIGHT_TEXT_SECONDARY;

    /**
     * Set the current theme and update colors
     * @param theme The theme to apply ("light" or "dark")
     */
    public static void setTheme(String theme) {
        currentTheme = theme != null && theme.equals("dark") ? "dark" : "light";
        LOGGER.log(Level.INFO, "Setting theme to: " + currentTheme);

        if (currentTheme.equals("dark")) {
            PRIMARY_COLOR = DARK_PRIMARY_COLOR;
            ACCENT_COLOR = DARK_ACCENT_COLOR;
            BACKGROUND_COLOR = DARK_BACKGROUND_COLOR;
            CARD_BACKGROUND = DARK_CARD_BACKGROUND;
            TEXT_PRIMARY = DARK_TEXT_PRIMARY;
            TEXT_SECONDARY = DARK_TEXT_SECONDARY;
        } else {
            PRIMARY_COLOR = LIGHT_PRIMARY_COLOR;
            ACCENT_COLOR = LIGHT_ACCENT_COLOR;
            BACKGROUND_COLOR = LIGHT_BACKGROUND_COLOR;
            CARD_BACKGROUND = LIGHT_CARD_BACKGROUND;
            TEXT_PRIMARY = LIGHT_TEXT_PRIMARY;
            TEXT_SECONDARY = LIGHT_TEXT_SECONDARY;
        }
    }

    /**
     * Apply the current theme to a component and its children
     * @param component The component to update
     */
    public static void applyTheme(Component component) {
        if (component == null) return;

        // Apply background and foreground colors based on component type
        if (component instanceof JPanel) {
            JPanel panel = (JPanel) component;
            // Check if it's a card panel (based on border style)
            Border border = panel.getBorder();
            if (border != null && border.toString().contains("LineBorder")) {
                panel.setBackground(CARD_BACKGROUND);
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1, true),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
            } else {
                panel.setBackground(BACKGROUND_COLOR);
            }
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            if (button.getBorder() != null && button.getBorder().toString().contains("LineBorder")) {
                // Primary or accent button
                if (button.getBackground().equals(LIGHT_PRIMARY_COLOR) || button.getBackground().equals(DARK_PRIMARY_COLOR)) {
                    button.setBackground(PRIMARY_COLOR);
                    button.setForeground(Color.WHITE);
                } else if (button.getBackground().equals(LIGHT_ACCENT_COLOR) || button.getBackground().equals(DARK_ACCENT_COLOR)) {
                    button.setBackground(ACCENT_COLOR);
                    button.setForeground(Color.WHITE);
                } else {
                    // Secondary button
                    button.setBackground(CARD_BACKGROUND);
                    button.setForeground(PRIMARY_COLOR);
                    button.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                            BorderFactory.createEmptyBorder(8, 15, 8, 15)
                    ));
                }
            }
        } else if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            if (label.getFont().equals(HEADING_FONT) || label.getFont().equals(SUBHEADING_FONT)) {
                label.setForeground(TEXT_PRIMARY);
            } else {
                label.setForeground(label.getForeground() == PRIMARY_COLOR ? PRIMARY_COLOR : TEXT_SECONDARY);
            }
        } else if (component instanceof JTextField || component instanceof JPasswordField) {
            JTextField textField = (JTextField) component;
            textField.setBackground(CARD_BACKGROUND);
            textField.setForeground(TEXT_PRIMARY);
            textField.setCaretColor(TEXT_PRIMARY);
            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, PRIMARY_COLOR),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
        } else if (component instanceof JTextArea) {
            JTextArea textArea = (JTextArea) component;
            textArea.setBackground(CARD_BACKGROUND);
            textArea.setForeground(TEXT_PRIMARY);
            textArea.setCaretColor(TEXT_PRIMARY);
            textArea.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(currentTheme.equals("dark") ? new Color(150, 150, 150) : new Color(200, 200, 200), 1, true),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
        }

        // Recursively apply to child components
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyTheme(child);
            }
        }

        component.revalidate();
        component.repaint();
    }

    // Create a styled button with modern look (unchanged)
    public static JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(BODY_FONT);
        button.setOpaque(true);

        // Add rounded corners with drop shadow
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1, true),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        return button;
    }

    // Create primary action button (updated to use theme-aware color)
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR, Color.WHITE);
    }

    // Create accent action button (updated to use theme-aware color)
    public static JButton createAccentButton(String text) {
        return createStyledButton(text, ACCENT_COLOR, Color.WHITE);
    }

    // Create secondary/outline button (updated to use theme-aware colors)
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(CARD_BACKGROUND);
        button.setForeground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setFont(BODY_FONT);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        return button;
    }

    // Create a modern card panel with shadow effect (updated to use theme-aware color)
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        return panel;
    }

    // Create a styled text field (updated to use theme-aware colors)
    public static JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(BODY_FONT);
        textField.setForeground(TEXT_PRIMARY);
        textField.setCaretColor(TEXT_PRIMARY);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        return textField;
    }

    // Create a styled password field (updated to use theme-aware colors)
    public static JPasswordField createStyledPasswordField() {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(BODY_FONT);
        passwordField.setForeground(TEXT_PRIMARY);
        passwordField.setCaretColor(TEXT_PRIMARY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        return passwordField;
    }

    // Create a styled text area (updated to use theme-aware colors)
    public static JTextArea createStyledTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setFont(BODY_FONT);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setForeground(TEXT_PRIMARY);
        textArea.setCaretColor(TEXT_PRIMARY);
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(currentTheme.equals("dark") ? new Color(150, 150, 150) : new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        return textArea;
    }

    // Create a styled label (updated to use theme-aware colors)
    public static JLabel createStyledLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color == PRIMARY_COLOR ? PRIMARY_COLOR : (color == TEXT_PRIMARY ? TEXT_PRIMARY : TEXT_SECONDARY));

        return label;
    }

    // Create a title label (updated to use theme-aware color)
    public static JLabel createTitleLabel(String text) {
        return createStyledLabel(text, HEADING_FONT, TEXT_PRIMARY);
    }

    // Create a subtitle label (updated to use theme-aware color)
    public static JLabel createSubtitleLabel(String text) {
        return createStyledLabel(text, SUBHEADING_FONT, TEXT_PRIMARY);
    }
}