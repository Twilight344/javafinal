package database;

import model.Post;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnector.getConnection();
    }

    public Post getPostById(int id) throws SQLException {
        String sql = "SELECT * FROM posts WHERE id = ?";
        Post post = null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                post = new Post(
                        rs.getInt("id"),
                        rs.getInt("platform_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("content"),
                        rs.getTimestamp("post_date"),
                        rs.getInt("likes")
                );
            }
            rs.close();
        }

        return post;
    }

    public List<Post> getPostsByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM posts WHERE user_id = ? ORDER BY post_date DESC";
        List<Post> posts = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Post post = new Post(
                        rs.getInt("id"),
                        rs.getInt("platform_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("content"),
                        rs.getTimestamp("post_date"),
                        rs.getInt("likes")
                );
                posts.add(post);
            }
            rs.close();
        }

        return posts;
    }

    public List<Post> getPostsByPlatform(int platformId) throws SQLException {
        String sql = "SELECT * FROM posts WHERE platform_id = ? ORDER BY post_date DESC";
        List<Post> posts = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, platformId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Post post = new Post(
                        rs.getInt("id"),
                        rs.getInt("platform_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("content"),
                        rs.getTimestamp("post_date"),
                        rs.getInt("likes")
                );
                posts.add(post);
            }
            rs.close();
        }

        return posts;
    }

    public List<Post> getAllPosts() throws SQLException {
        String sql = "SELECT * FROM posts ORDER BY post_date DESC";
        List<Post> posts = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Post post = new Post(
                        rs.getInt("id"),
                        rs.getInt("platform_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("content"),
                        rs.getTimestamp("post_date"),
                        rs.getInt("likes")
                );
                posts.add(post);
            }
        }

        return posts;
    }

    public void createPost(Post post) throws SQLException {
        String sql = "INSERT INTO posts (platform_id, user_id, username, content, post_date, likes) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, post.getPlatformId());
            pstmt.setInt(2, post.getUserId());
            pstmt.setString(3, post.getUsername());
            pstmt.setString(4, post.getContent());
            pstmt.setTimestamp(5, new Timestamp(post.getPostDate().getTime()));
            pstmt.setInt(6, post.getLikes());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating post failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating post failed, no ID obtained.");
                }
            }
        }
    }

    public void updatePost(Post post) throws SQLException {
        String sql = "UPDATE posts SET platform_id = ?, user_id = ?, username = ?, content = ?, post_date = ?, likes = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, post.getPlatformId());
            pstmt.setInt(2, post.getUserId());
            pstmt.setString(3, post.getUsername());
            pstmt.setString(4, post.getContent());
            pstmt.setTimestamp(5, new Timestamp(post.getPostDate().getTime()));
            pstmt.setInt(6, post.getLikes());
            pstmt.setInt(7, post.getId());

            pstmt.executeUpdate();
        }
    }

    public void deletePost(int id) throws SQLException {
        String sql = "DELETE FROM posts WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}