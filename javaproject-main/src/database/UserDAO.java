package database;

import model.User;
import util.PasswordHasher;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    private Connection getConnection() throws SQLException {
        try {
            return DatabaseConnector.getConnection();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get database connection", e);
            throw e;
        }
    }

    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, full_name, email, password_hash, theme_preference) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getFullName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPasswordHash());
            pstmt.setString(5, user.getThemePreference());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating user: " + user.getUsername(), e);
            throw e;
        }
    }

    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("theme_preference")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching user with ID: " + id, e);
            throw e;
        }
        return null;
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, id);
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("theme_preference")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching user with username: " + username, e);
            throw e;
        }
        return null;
    }

    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, full_name = ?, email = ?, password_hash = ?, theme_preference = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Log parameter values for debugging
            LOGGER.log(Level.INFO, "Updating user ID: " + user.getId() +
                    ", username: " + user.getUsername() +
                    ", full_name: " + user.getFullName() +
                    ", email: " + user.getEmail() +
                    ", password_hash: " + user.getPasswordHash() +
                    ", theme_preference: " + user.getThemePreference());

            // Validate parameters
            if (user.getUsername() == null || user.getFullName() == null || user.getEmail() == null ||
                    user.getPasswordHash() == null || user.getThemePreference() == null) {
                throw new SQLException("One or more user fields are null: " +
                        "username=" + user.getUsername() + ", full_name=" + user.getFullName() +
                        ", email=" + user.getEmail() + ", password_hash=" + user.getPasswordHash() +
                        ", theme_preference=" + user.getThemePreference());
            }

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getFullName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPasswordHash());
            pstmt.setString(5, user.getThemePreference());
            pstmt.setInt(6, user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user with ID: " + user.getId(), e);
            throw e;
        }
    }

    public void updateThemePreference(int userId, String themePreference) throws SQLException {
        String sql = "UPDATE users SET theme_preference = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, themePreference);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating theme preference for user ID: " + userId, e);
            throw e;
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if username exists: " + username, e);
            throw e;
        }
        return false;
    }

    public boolean validateUser(String username, String password) throws SQLException {
        User user = getUserByUsername(username);
        if (user != null) {
            return PasswordHasher.verifyPassword(password, user.getPasswordHash());
        }
        return false;
    }
}