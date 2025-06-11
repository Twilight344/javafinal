package main;

import database.PostDAO;
import database.LikeDAO;
import model.Post;
import model.SocialMedia;
import model.User;
import util.DateUtil;
import util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocialMediaPanel extends JPanel {
    private final SocialMedia platform;
    private final User currentUser;
    private final JPanel postsPanel;
    private final JTextArea newPostTextArea;
    private static final Logger LOGGER = Logger.getLogger(SocialMediaPanel.class.getName());

    public SocialMediaPanel(SocialMedia platform, User currentUser) {
        this.platform = platform;
        this.currentUser = currentUser;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(UIUtil.createTitleLabel(platform.getName() + " Dashboard"), BorderLayout.WEST);

        JPanel newPostPanel = new JPanel();
        newPostPanel.setLayout(new BorderLayout(5, 5));
        newPostPanel.setBorder(BorderFactory.createTitledBorder("Create New Post"));

        newPostTextArea = UIUtil.createStyledTextArea();
        newPostTextArea.setRows(3);
        JScrollPane newPostScrollPane = new JScrollPane(newPostTextArea);

        JPanel postButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton aiSuggestButton = UIUtil.createSecondaryButton("AI Suggest");
        aiSuggestButton.addActionListener(e -> suggestPost());
        JButton postButton = UIUtil.createPrimaryButton("Post");
        postButton.addActionListener(e -> createNewPost());

        postButtonPanel.add(aiSuggestButton);
        postButtonPanel.add(postButton);

        newPostPanel.add(newPostScrollPane, BorderLayout.CENTER);
        newPostPanel.add(postButtonPanel, BorderLayout.SOUTH);

        postsPanel = new JPanel();
        postsPanel.setLayout(new BoxLayout(postsPanel, BoxLayout.Y_AXIS));
        JScrollPane postsScrollPane = new JScrollPane(postsPanel);
        postsScrollPane.setBorder(BorderFactory.createTitledBorder("Recent Posts"));

        loadPosts();

        add(infoPanel, BorderLayout.NORTH);
        add(newPostPanel, BorderLayout.SOUTH);
        add(postsScrollPane, BorderLayout.CENTER);
    }

    private void suggestPost() {
        String suggestion = ai.AIAssistant.getPostSuggestion();
        newPostTextArea.setText(suggestion);
    }

    private void loadPosts() {
        postsPanel.removeAll();
        try {
            PostDAO postDAO = new PostDAO();
            List<Post> posts = postDAO.getPostsByPlatform(platform.getId());

            if (posts.isEmpty()) {
                postsPanel.add(UIUtil.createStyledLabel("No posts yet. Create your first post!", UIUtil.BODY_FONT, UIUtil.TEXT_SECONDARY));
            } else {
                for (Post post : posts) {
                    JPanel postPanel = createPostPanel(post);
                    postsPanel.add(postPanel);
                    postsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
            revalidate();
            repaint();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading posts for platform " + platform.getName(), e);
            JOptionPane.showMessageDialog(this,
                    "Error loading posts: " + e.getMessage(),
                    "Post Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JPanel createPostPanel(Post post) {
        JPanel panel = UIUtil.createCardPanel();
        panel.setLayout(new BorderLayout(5, 5));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtil.CARD_BACKGROUND);
        headerPanel.add(UIUtil.createStyledLabel("@" + post.getUsername(), UIUtil.BODY_FONT, UIUtil.PRIMARY_COLOR), BorderLayout.WEST);
        headerPanel.add(UIUtil.createStyledLabel(DateUtil.getTimeAgo(post.getPostDate()), UIUtil.BODY_FONT, UIUtil.TEXT_SECONDARY), BorderLayout.EAST);

        JTextArea contentArea = UIUtil.createStyledTextArea();
        contentArea.setText(post.getContent());
        contentArea.setEditable(false);
        contentArea.setBackground(UIUtil.CARD_BACKGROUND);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setBackground(UIUtil.CARD_BACKGROUND);

        LikeDAO likeDAO = new LikeDAO();
        boolean hasLiked = false;
        int likeCount = 0;
        try {
            hasLiked = likeDAO.hasUserLikedPost(currentUser.getId(), post.getId());
            likeCount = likeDAO.getLikeCount(post.getId());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching like status for post " + post.getId(), e);
            JOptionPane.showMessageDialog(this,
                    "Error loading like status: " + e.getMessage(),
                    "Like Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        JButton likeButton = UIUtil.createSecondaryButton(hasLiked ? "Unlike (" + likeCount + ")" : "Like (" + likeCount + ")");
        likeButton.addActionListener(e -> toggleLike(post, likeButton));

        JButton commentButton = UIUtil.createSecondaryButton("Comment");
        commentButton.addActionListener(e -> showCommentPanel(post));

        JButton shareButton = UIUtil.createSecondaryButton("Share");
        shareButton.addActionListener(e -> sharePost(post));

        actionPanel.add(likeButton);
        actionPanel.add(commentButton);
        actionPanel.add(shareButton);

        if (post.getUserId() == currentUser.getId()) {
            JButton editButton = UIUtil.createSecondaryButton("Edit");
            editButton.addActionListener(e -> editPost(post));
            JButton deleteButton = UIUtil.createSecondaryButton("Delete");
            deleteButton.addActionListener(e -> deletePost(post));
            actionPanel.add(editButton);
            actionPanel.add(deleteButton);
        }

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentArea, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void toggleLike(Post post, JButton likeButton) {
        try {
            LikeDAO likeDAO = new LikeDAO();
            boolean liked = likeDAO.toggleLike(currentUser.getId(), post.getId());
            int likeCount = likeDAO.getLikeCount(post.getId());
            likeButton.setText(liked ? "Unlike (" + likeCount + ")" : "Like (" + likeCount + ")");
            PostDAO postDAO = new PostDAO();
            post.setLikes(likeCount);
            postDAO.updatePost(post);

            // Fire analytics update event
            AnalyticsEventDispatcher.getInstance().fireEvent(new AnalyticsUpdateEvent(currentUser.getId()));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error toggling like for post " + post.getId(), e);
            JOptionPane.showMessageDialog(this,
                    "Error toggling like: " + e.getMessage(),
                    "Like Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showCommentPanel(Post post) {
        JDialog commentDialog = new JDialog((Frame) SwingUtilities.getAncestorOfClass(Frame.class, this), "Comments on Post", true);
        commentDialog.setSize(600, 400);
        commentDialog.setLocationRelativeTo(this);
        commentDialog.add(new CommentPanel(post, currentUser));
        commentDialog.setVisible(true);
    }

    private void sharePost(Post post) {
        String shareText = post.getUsername() + ": " + post.getContent() + " (Posted on " + platform.getName() + ")";
        StringSelection stringSelection = new StringSelection(shareText);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        JOptionPane.showMessageDialog(this,
                "Post copied to clipboard!",
                "Share Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void editPost(Post post) {
        String newContent = JOptionPane.showInputDialog(this,
                "Edit your post:",
                post.getContent());
        if (newContent != null && !newContent.trim().isEmpty()) {
            try {
                PostDAO postDAO = new PostDAO();
                post.setContent(newContent.trim());
                postDAO.updatePost(post);
                loadPosts();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error updating post " + post.getId(), e);
                JOptionPane.showMessageDialog(this,
                        "Error updating post: " + e.getMessage(),
                        "Post Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void deletePost(Post post) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this post?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PostDAO postDAO = new PostDAO();
                postDAO.deletePost(post.getId());
                loadPosts();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error deleting post " + post.getId(), e);
                JOptionPane.showMessageDialog(this,
                        "Error deleting post: " + e.getMessage(),
                        "Post Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void createNewPost() {
        String content = newPostTextArea.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Post content cannot be empty",
                    "Post Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (content.length() > 500) {
            JOptionPane.showMessageDialog(this,
                    "Post content cannot exceed 500 characters",
                    "Post Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            PostDAO postDAO = new PostDAO();
            Post newPost = new Post(
                    0,
                    platform.getId(),
                    currentUser.getId(),
                    currentUser.getUsername(),
                    content,
                    new Date(),
                    0
            );
            postDAO.createPost(newPost);
            newPostTextArea.setText("");
            loadPosts();
            JOptionPane.showMessageDialog(this,
                    "Post created successfully!",
                    "Post Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Fire analytics update event
            AnalyticsEventDispatcher.getInstance().fireEvent(new AnalyticsUpdateEvent(currentUser.getId()));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating new post", e);
            JOptionPane.showMessageDialog(this,
                    "Error creating post: " + e.getMessage(),
                    "Post Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}