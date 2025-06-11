
package database;

import model.SocialMedia;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SocialMediaPlatformDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnector.getConnection();
    }

    public SocialMedia getPlatformById(int id) throws SQLException {
        String sql = "SELECT * FROM social_media_platforms WHERE id = ?";
        SocialMedia platform = null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                platform = new SocialMedia(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("icon"),
                        rs.getString("color")
                );
            }
            rs.close();
        }

        return platform;
    }

    public SocialMedia getPlatformByName(String name) throws SQLException {
        String sql = "SELECT * FROM social_media_platforms WHERE name = ?";
        SocialMedia platform = null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                platform = new SocialMedia(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("icon"),
                        rs.getString("color")
                );
            }
            rs.close();
        }

        return platform;
    }

    public List<SocialMedia> getAllPlatforms() throws SQLException {
        String sql = "SELECT * FROM social_media_platforms ORDER BY name";
        List<SocialMedia> platforms = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                SocialMedia platform = new SocialMedia(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("icon"),
                        rs.getString("color")
                );
                platforms.add(platform);
            }
        }

        return platforms;
    }

    public void createPlatform(SocialMedia platform) throws SQLException {
        String sql = "INSERT INTO social_media_platforms (name, icon, color) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, platform.getName());
            pstmt.setString(2, platform.getIcon());
            pstmt.setString(3, platform.getColor());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating social media platform failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    platform.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating social media platform failed, no ID obtained.");
                }
            }
        }
    }

    public void updatePlatform(SocialMedia platform) throws SQLException {
        String sql = "UPDATE social_media_platforms SET name = ?, icon = ?, color = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, platform.getName());
            pstmt.setString(2, platform.getIcon());
            pstmt.setString(3, platform.getColor());
            pstmt.setInt(4, platform.getId());

            pstmt.executeUpdate();
        }
    }

    public void deletePlatform(int id) throws SQLException {
        String sql = "DELETE FROM social_media_platforms WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}
