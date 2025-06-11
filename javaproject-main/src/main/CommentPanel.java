package main;

import database.CommentDAO;
import model.Comment;
import model.Post;
import model.User;
import util.DateUtil;
import util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Panel for displaying and managing comments on a post
 */
public class CommentPanel extends JPanel {
    private Post post;
    private User currentUser;
    private JPanel commentsListPanel;
    private JTextArea newCommentArea;
    private JButton submitCommentButton;
    private JButton aiSuggestButton;
    private JLabel commentCountLabel;

    public CommentPanel(Post post, User currentUser) {
        this.post = post;
        this.currentUser = currentUser;

        setupUI();
        loadComments();
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        commentCountLabel = UIUtil.createSubtitleLabel("Comments");
        headerPanel.add(commentCountLabel, BorderLayout.WEST);

        commentsListPanel = new JPanel();
        commentsListPanel.setLayout(new BoxLayout(commentsListPanel, BoxLayout.Y_AXIS));
        JScrollPane commentsScrollPane = new JScrollPane(commentsListPanel);
        commentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        commentsScrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel newCommentPanel = new JPanel(new BorderLayout(5, 5));
        newCommentPanel.setBorder(BorderFactory.createTitledBorder("Add a Comment"));

        newCommentArea = UIUtil.createStyledTextArea();
        newCommentArea.setRows(3);
        JScrollPane newCommentScrollPane = new JScrollPane(newCommentArea);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        aiSuggestButton = UIUtil.createSecondaryButton("AI Suggest");
        aiSuggestButton.addActionListener(e -> suggestComment());

        submitCommentButton = UIUtil.createPrimaryButton("Submit");
        submitCommentButton.addActionListener(e -> submitComment());

        buttonPanel.add(aiSuggestButton);
        buttonPanel.add(submitCommentButton);

        newCommentPanel.add(newCommentScrollPane, BorderLayout.CENTER);
        newCommentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(commentsScrollPane, BorderLayout.CENTER);
        add(newCommentPanel, BorderLayout.SOUTH);
    }

    public void loadComments() {
        commentsListPanel.removeAll();

        try {
            CommentDAO commentDAO = new CommentDAO();
            List<Comment> comments = commentDAO.getCommentsByPost(post.getId());

            commentCountLabel.setText("Comments (" + comments.size() + ")");

            if (comments.isEmpty()) {
                JLabel noCommentsLabel = UIUtil.createStyledLabel("No comments yet. Be the first to comment!", UIUtil.BODY_FONT, UIUtil.TEXT_SECONDARY);
                noCommentsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                commentsListPanel.add(noCommentsLabel);
            } else {
                for (Comment comment : comments) {
                    JPanel commentItemPanel = createCommentPanel(comment);
                    commentItemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, commentItemPanel.getPreferredSize().height));
                    commentItemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    commentsListPanel.add(commentItemPanel);
                    commentsListPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                }
            }

            revalidate();
            repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading comments: " + e.getMessage(),
                    "Comment Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JPanel createCommentPanel(Comment comment) {
        JPanel panel = UIUtil.createCardPanel();
        panel.setLayout(new BorderLayout(5, 3));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtil.CARD_BACKGROUND);

        JLabel usernameLabel = UIUtil.createStyledLabel("@" + comment.getUsername(), UIUtil.BODY_FONT, UIUtil.PRIMARY_COLOR);
        JLabel dateLabel = UIUtil.createStyledLabel(DateUtil.getTimeAgo(comment.getCommentDate()), UIUtil.BODY_FONT, UIUtil.TEXT_SECONDARY);

        headerPanel.add(usernameLabel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        JTextArea contentArea = UIUtil.createStyledTextArea();
        contentArea.setText(comment.getContent());
        contentArea.setEditable(false);
        contentArea.setBackground(UIUtil.CARD_BACKGROUND);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setBackground(UIUtil.CARD_BACKGROUND);

        if (comment.getUserId() == currentUser.getId()) {
            JButton editButton = UIUtil.createSecondaryButton("Edit");
            editButton.addActionListener(e -> editComment(comment));
            JButton deleteButton = UIUtil.createSecondaryButton("Delete");
            deleteButton.addActionListener(e -> deleteComment(comment));
            actionPanel.add(editButton);
            actionPanel.add(deleteButton);
        }

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentArea, BorderLayout.CENTER);
        if (comment.getUserId() == currentUser.getId()) {
            panel.add(actionPanel, BorderLayout.SOUTH);
        }

        return panel;
    }

    private void submitComment() {
        String content = newCommentArea.getText().trim();

        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Comment cannot be empty",
                    "Comment Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (content.length() > 250) {
            JOptionPane.showMessageDialog(this,
                    "Comment cannot exceed 250 characters",
                    "Comment Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            CommentDAO commentDAO = new CommentDAO();
            Comment newComment = new Comment(
                    0,
                    post.getId(),
                    currentUser.getId(),
                    currentUser.getUsername(),
                    content,
                    new Date()
            );

            commentDAO.createComment(newComment);
            newCommentArea.setText("");
            loadComments();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error posting comment: " + e.getMessage(),
                    "Comment Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void editComment(Comment comment) {
        String newContent = JOptionPane.showInputDialog(this,
                "Edit your comment:",
                comment.getContent());

        if (newContent != null && !newContent.trim().isEmpty() && newContent.trim().length() <= 250) {
            try {
                CommentDAO commentDAO = new CommentDAO();
                comment.setContent(newContent.trim());
                commentDAO.updateComment(comment);
                loadComments();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error updating comment: " + e.getMessage(),
                        "Comment Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else if (newContent != null && newContent.trim().length() > 250) {
            JOptionPane.showMessageDialog(this,
                    "Comment cannot exceed 250 characters",
                    "Comment Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteComment(Comment comment) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this comment?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                CommentDAO commentDAO = new CommentDAO();
                commentDAO.deleteComment(comment.getId(), currentUser.getId());
                loadComments();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting comment: " + e.getMessage(),
                        "Comment Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void suggestComment() {
        try {
            String suggestion = ai.AIAssistant.getContextAwareCommentSuggestion(post.getContent());
            newCommentArea.setText(suggestion);
        } catch (Exception e) {
            newCommentArea.setText("I like what you shared! Thanks for posting.");
        }
    }
}