package database;

import model.Comment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnector.getConnection();
    }

    public Comment getCommentById(int id) throws SQLException {
        String sql = "SELECT * FROM comments WHERE id = ?";
        Comment comment = null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                comment = new Comment(
                        rs.getInt("id"),
                        rs.getInt("post_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("content"),
                        rs.getTimestamp("comment_date")
                );
            }
            rs.close();
        }

        return comment;
    }

    public List<Comment> getCommentsByPost(int postId) throws SQLException {
        String sql = "SELECT * FROM comments WHERE post_id = ? ORDER BY comment_date";
        List<Comment> comments = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, postId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Comment comment = new Comment(
                        rs.getInt("id"),
                        rs.getInt("post_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("content"),
                        rs.getTimestamp("comment_date")
                );
                comments.add(comment);
            }
            rs.close();
        }

        return comments;
    }

    public List<Comment> getCommentsByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM comments WHERE user_id = ? ORDER BY comment_date DESC";
        List<Comment> comments = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Comment comment = new Comment(
                        rs.getInt("id"),
                        rs.getInt("post_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("content"),
                        rs.getTimestamp("comment_date")
                );
                comments.add(comment);
            }
            rs.close();
        }

        return comments;
    }

    public void createComment(Comment comment) throws SQLException {
        String sql = "INSERT INTO comments (post_id, user_id, username, content, comment_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, comment.getPostId());
            pstmt.setInt(2, comment.getUserId());
            pstmt.setString(3, comment.getUsername());
            pstmt.setString(4, comment.getContent());
            pstmt.setTimestamp(5, new Timestamp(comment.getCommentDate().getTime()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating comment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comment.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating comment failed, no ID obtained.");
                }
            }
        }
    }

    public void updateComment(Comment comment) throws SQLException {
        String sql = "UPDATE comments SET content = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, comment.getContent());
            pstmt.setInt(2, comment.getId());
            pstmt.setInt(3, comment.getUserId());

            pstmt.executeUpdate();
        }
    }

    public void deleteComment(int id, int userId) throws SQLException {
        String sql = "DELETE FROM comments WHERE id = ? AND user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    public int getCommentCount(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comments WHERE post_id = ?";
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
}