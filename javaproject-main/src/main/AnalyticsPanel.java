package main;

import database.PostDAO;
import database.CommentDAO;
import database.SocialMediaPlatformDAO;
import model.Post;
import model.SocialMedia;
import model.User;
import util.UIUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel for displaying user analytics with charts and summary statistics
 */
public class AnalyticsPanel extends JPanel implements AnalyticsUpdateListener {
    private static final Logger LOGGER = Logger.getLogger(AnalyticsPanel.class.getName());
    private final User currentUser;
    private final PostDAO postDAO;
    private final CommentDAO commentDAO;
    private final SocialMediaPlatformDAO platformDAO;
    private JPanel mainPanel;

    public AnalyticsPanel(User user) {
        this.currentUser = user;
        this.postDAO = new PostDAO();
        this.commentDAO = new CommentDAO();
        this.platformDAO = new SocialMediaPlatformDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Register as listener for analytics updates
        AnalyticsEventDispatcher.getInstance().addListener(this);

        initializeUI();
    }

    private void initializeUI() {
        mainPanel = UIUtil.createCardPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Title
        mainPanel.add(UIUtil.createTitleLabel("Your Social Media Analytics"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        try {
            // Summary Statistics
            mainPanel.add(createSummaryPanel());
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

            // Charts
            mainPanel.add(createPostsByPlatformChart());
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            mainPanel.add(createEngagementChart());
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            mainPanel.add(createPostActivityChart());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading analytics data for user ID: " + currentUser.getId(), e);
            JOptionPane.showMessageDialog(this,
                    "Error loading analytics: " + e.getMessage(),
                    "Analytics Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void onAnalyticsUpdate(AnalyticsUpdateEvent event) {
        // Only update if the event is for the current user
        if (event.getUserId() == currentUser.getId()) {
            SwingUtilities.invokeLater(this::refreshUI);
        }
    }

    private void refreshUI() {
        // Remove old content
        mainPanel.removeAll();

        // Rebuild UI
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(UIUtil.createTitleLabel("Your Social Media Analytics"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        try {
            // Summary Statistics
            mainPanel.add(createSummaryPanel());
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

            // Charts
            mainPanel.add(createPostsByPlatformChart());
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            mainPanel.add(createEngagementChart());
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            mainPanel.add(createPostActivityChart());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error refreshing analytics for user ID: " + currentUser.getId(), e);
            JOptionPane.showMessageDialog(this,
                    "Error refreshing analytics: " + e.getMessage(),
                    "Analytics Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Repaint the panel
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private JPanel createSummaryPanel() throws SQLException {
        JPanel panel = UIUtil.createCardPanel();
        panel.setLayout(new GridLayout(2, 2, 10, 10));

        List<Post> userPosts = postDAO.getPostsByUser(currentUser.getId());
        int totalPosts = userPosts.size();
        int totalLikes = userPosts.stream().mapToInt(Post::getLikes).sum();
        int totalComments = userPosts.stream()
                .mapToInt(post -> {
                    try {
                        return commentDAO.getCommentCount(post.getId());
                    } catch (SQLException e) {
                        LOGGER.log(Level.WARNING, "Error counting comments for post ID: " + post.getId(), e);
                        return 0;
                    }
                })
                .sum();
        double avgLikesPerPost = totalPosts > 0 ? (double) totalLikes / totalPosts : 0;

        panel.add(UIUtil.createStyledLabel("Total Posts: " + totalPosts, UIUtil.BODY_FONT, UIUtil.TEXT_PRIMARY));
        panel.add(UIUtil.createStyledLabel("Total Likes: " + totalLikes, UIUtil.BODY_FONT, UIUtil.TEXT_PRIMARY));
        panel.add(UIUtil.createStyledLabel("Total Comments: " + totalComments, UIUtil.BODY_FONT, UIUtil.TEXT_PRIMARY));
        panel.add(UIUtil.createStyledLabel(String.format("Avg Likes per Post: %.2f", avgLikesPerPost), UIUtil.BODY_FONT, UIUtil.TEXT_PRIMARY));

        return panel;
    }

    private JPanel createPostsByPlatformChart() throws SQLException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<SocialMedia> platforms = platformDAO.getAllPlatforms();
        List<Post> userPosts = postDAO.getPostsByUser(currentUser.getId());

        for (SocialMedia platform : platforms) {
            long postCount = userPosts.stream()
                    .filter(post -> post.getPlatformId() == platform.getId())
                    .count();
            dataset.addValue(postCount, "Posts", platform.getName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Posts by Platform",
                "Platform",
                "Number of Posts",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        // Enhance rendering quality
        chart.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON));
        chart.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
        chart.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));

        // Customize chart appearance
        chart.getCategoryPlot().getRenderer().setSeriesPaint(0, UIUtil.PRIMARY_COLOR);
        chart.setBackgroundPaint(UIUtil.BACKGROUND_COLOR);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18));
        chart.getCategoryPlot().getDomainAxis().setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        chart.getCategoryPlot().getRangeAxis().setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        chart.getCategoryPlot().getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        chart.getCategoryPlot().getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        ((BarRenderer) chart.getCategoryPlot().getRenderer()).setItemMargin(0.02);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400)); // Increased size for HD
        chartPanel.setBackground(UIUtil.BACKGROUND_COLOR);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);

        JPanel panel = UIUtil.createCardPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEngagementChart() throws SQLException {
        DefaultPieDataset dataset = new DefaultPieDataset();
        List<Post> userPosts = postDAO.getPostsByUser(currentUser.getId());
        int totalLikes = userPosts.stream().mapToInt(Post::getLikes).sum();
        int totalComments = userPosts.stream()
                .mapToInt(post -> {
                    try {
                        return commentDAO.getCommentCount(post.getId());
                    } catch (SQLException e) {
                        LOGGER.log(Level.WARNING, "Error counting comments for post ID: " + post.getId(), e);
                        return 0;
                    }
                })
                .sum();

        dataset.setValue("Likes", totalLikes);
        dataset.setValue("Comments", totalComments);

        JFreeChart chart = ChartFactory.createPieChart(
                "Engagement Distribution",
                dataset,
                true,
                true,
                false
        );

        // Enhance rendering quality
        chart.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON));
        chart.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
        chart.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));

        // Customize chart appearance
        chart.getPlot().setBackgroundPaint(UIUtil.BACKGROUND_COLOR);
        chart.setBackgroundPaint(UIUtil.BACKGROUND_COLOR);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18));
        chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 12));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400)); // Increased size for HD
        chartPanel.setBackground(UIUtil.BACKGROUND_COLOR);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);

        JPanel panel = UIUtil.createCardPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPostActivityChart() throws SQLException {
        TimeSeries series = new TimeSeries("Posts");
        List<Post> userPosts = postDAO.getPostsByUser(currentUser.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);

        Map<String, Integer> postCounts = new HashMap<>();
        for (Post post : userPosts) {
            String dateStr = sdf.format(post.getPostDate());
            postCounts.put(dateStr, postCounts.getOrDefault(dateStr, 0) + 1);
        }

        for (int i = 0; i <= 30; i++) {
            String dateStr = sdf.format(cal.getTime());
            int count = postCounts.getOrDefault(dateStr, 0);
            series.add(new Day(cal.getTime()), count);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection(series);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Post Activity (Last 30 Days)",
                "Date",
                "Number of Posts",
                dataset,
                false,
                true,
                false
        );

        // Enhance rendering quality
        chart.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON));
        chart.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
        chart.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));

        // Customize chart appearance
        chart.getXYPlot().getRenderer().setSeriesPaint(0, UIUtil.PRIMARY_COLOR);
        chart.getXYPlot().setBackgroundPaint(UIUtil.BACKGROUND_COLOR);
        chart.setBackgroundPaint(UIUtil.BACKGROUND_COLOR);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18));
        chart.getXYPlot().getDomainAxis().setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        chart.getXYPlot().getRangeAxis().setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        chart.getXYPlot().getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        chart.getXYPlot().getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        ((XYLineAndShapeRenderer) chart.getXYPlot().getRenderer()).setBaseShapesVisible(true);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400)); // Increased size for HD
        chartPanel.setBackground(UIUtil.BACKGROUND_COLOR);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);

        JPanel panel = UIUtil.createCardPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        return panel;
    }
}