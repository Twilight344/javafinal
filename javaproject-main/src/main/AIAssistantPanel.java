package main;

import ai.AIAssistant;
import util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel for the AI Assistant feature that provides content suggestions,
 * writing help, and other assistance to the user.
 */
public class AIAssistantPanel extends JPanel {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton suggestPostButton;
    private JButton suggestCommentButton;

    /**
     * Constructor for the AI Assistant panel
     */
    public AIAssistantPanel() {
        setupUI();
        addInitialMessage();
    }

    /**
     * Set up the UI components for the AI Assistant panel
     */
    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Chat area
        chatArea = UIUtil.createStyledTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBorder(BorderFactory.createTitledBorder("AI Assistant"));
        chatScrollPane.setPreferredSize(new Dimension(400, 300));

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton sendButton = UIUtil.createPrimaryButton("Send");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Quick actions panel
        JPanel quickActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        suggestPostButton = UIUtil.createSecondaryButton("Suggest Post");
        suggestCommentButton = UIUtil.createSecondaryButton("Suggest Comment");

        suggestPostButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getPostSuggestion();
            }
        });

        suggestCommentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getCommentSuggestion();
            }
        });

        quickActionsPanel.add(suggestPostButton);
        quickActionsPanel.add(suggestCommentButton);

        // Combine input and quick actions in a bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 5));
        bottomPanel.add(inputPanel, BorderLayout.NORTH);
        bottomPanel.add(quickActionsPanel, BorderLayout.CENTER);

        // Add components to main panel
        add(chatScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Add the initial welcome message from the AI
     */
    private void addInitialMessage() {
        appendToChatArea("AI Assistant", "👋 Hi there! I'm your AI Assistant. I can help with content suggestions, " +
                "post ideas, or answering questions about social media. How can I help you today?");
    }

    /**
     * Send a user message to the AI
     */
    private void sendMessage() {
        String userMessage = inputField.getText().trim();
        if (userMessage.isEmpty()) return;

        // Add user message to chat
        appendToChatArea("You", userMessage);

        // Process the message and get AI response
        String aiResponse = AIAssistant.processQuery(userMessage);

        // Add AI response to chat
        appendToChatArea("AI Assistant", aiResponse);

        // Clear input field
        inputField.setText("");
    }

    /**
     * Get a post suggestion from the AI
     */
    private void getPostSuggestion() {
        String suggestion = AIAssistant.getPostSuggestion();
        appendToChatArea("AI Assistant", "Here's a post suggestion for you:\n\n\"" + suggestion + "\"");
    }

    /**
     * Get a comment suggestion from the AI
     */
    private void getCommentSuggestion() {
        String suggestion = AIAssistant.getCommentSuggestion();
        appendToChatArea("AI Assistant", "Here's a comment suggestion for you:\n\n\"" + suggestion + "\"");
    }

    /**
     * Append a message to the chat area
     * @param sender The sender name
     * @param message The message content
     */
    private void appendToChatArea(String sender, String message) {
        String formattedMessage = String.format("%s: %s\n\n", sender, message);
        chatArea.append(formattedMessage);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    /**
     * Get a post suggestion that can be used externally
     * @return A post suggestion
     */
    public String getExternalPostSuggestion() {
        return AIAssistant.getPostSuggestion();
    }

    /**
     * Get a comment suggestion that can be used externally
     * @return A comment suggestion
     */
    public String getExternalCommentSuggestion() {
        return AIAssistant.getCommentSuggestion();
    }
}