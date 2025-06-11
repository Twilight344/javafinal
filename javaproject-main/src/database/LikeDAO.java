package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LikeDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnector.getConnection();
    }

    /**
     * Check if a user has already liked a post
     * @param userId The user ID
     * @param postId The post ID
     * @return True if the user has already liked the post, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean hasUserLikedPost(int userId, int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM likes WHERE user_id = ? AND post_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, postId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            rs.close();
        }
        return false;
    }

    /**
     * Toggle like status for a post
     * @param userId The user ID
     * @param postId The post ID
     * @return True if liked, false if unliked
     * @throws SQLException If a database error occurs
     */
    public boolean toggleLike(int userId, int postId) throws SQLException {
        if (hasUserLikedPost(userId, postId)) {
            removeLike(userId, postId);
            return false;
        } else {
            addLike(userId, postId);
            return true;
        }
    }

    /**
     * Add a like to a post
     * @param userId The user ID
     * @param postId The post ID
     * @throws SQLException If a database error occurs
     */
    public void addLike(int userId, int postId) throws SQLException {
        String sql = "INSERT INTO likes (user_id, post_id) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, postId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Remove a like from a post
     * @param userId The user ID
     * @param postId The post ID
     * @throws SQLException If a database error occurs
     */
    public void removeLike(int userId, int postId) throws SQLException {
        String sql = "DELETE FROM likes WHERE user_id = ? AND post_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, postId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Get the total number of likes for a post
     * @param postId The post ID
     * @return The number of likes
     * @throws SQLException If a database error occurs
     */
    public int getLikeCount(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM likes WHERE post_id = ?";
        int count = 0;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
        }
        return count;
    }

    /**
     * Get all users who have liked a post
     * @param postId The post ID
     * @return A list of user IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> getUsersWhoLikedPost(int postId) throws SQLException {
        String sql = "SELECT user_id FROM likes WHERE post_id = ?";
        List<Integer> userIds = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                userIds.add(rs.getInt("user_id"));
            }
            rs.close();
        }
        return userIds;
    }
}