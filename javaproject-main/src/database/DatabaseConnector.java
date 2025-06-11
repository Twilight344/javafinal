package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/social_media_dashboard";
    private static final String USER = "postgres";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    public static void initialize() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            createTables();
            initializeDefaultData();
            System.out.println("Database connection established successfully");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    private static void createTables() throws SQLException {
        Statement stmt = connection.createStatement();

        // Users table
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id SERIAL PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) NOT NULL, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "theme_preference VARCHAR(10) DEFAULT 'light', " +

                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");

        // Social media platforms table
        stmt.execute("CREATE TABLE IF NOT EXISTS social_media_platforms (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(50) UNIQUE NOT NULL, " +
                "icon VARCHAR(50), " +
                "color VARCHAR(20)" +
                ")");

        // Posts table
        stmt.execute("CREATE TABLE IF NOT EXISTS posts (" +
                "id SERIAL PRIMARY KEY, " +
                "platform_id INTEGER REFERENCES social_media_platforms(id), " +
                "user_id INTEGER REFERENCES users(id), " +
                "username VARCHAR(50) NOT NULL, " +
                "content TEXT NOT NULL, " +
                "post_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "likes INTEGER DEFAULT 0" +
                ")");

        // Comments table
        stmt.execute("CREATE TABLE IF NOT EXISTS comments (" +
                "id SERIAL PRIMARY KEY, " +
                "post_id INTEGER REFERENCES posts(id) ON DELETE CASCADE, " +
                "user_id INTEGER REFERENCES users(id), " +
                "username VARCHAR(50) NOT NULL, " +
                "content TEXT NOT NULL, " +
                "comment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");

        // Likes table
        stmt.execute("CREATE TABLE IF NOT EXISTS likes (" +
                "user_id INTEGER REFERENCES users(id), " +
                "post_id INTEGER REFERENCES posts(id) ON DELETE CASCADE, " +
                "PRIMARY KEY (user_id, post_id)" +
                ")");

        stmt.close();
    }

    private static void initializeDefaultData() throws SQLException {
        Statement checkStmt = connection.createStatement();
        java.sql.ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM social_media_platforms");
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        checkStmt.close();

        if (count == 0) {
            Statement stmt = connection.createStatement();
            stmt.execute("INSERT INTO social_media_platforms (name, icon, color) VALUES " +
                    "('Instagram', 'instagram.png', '#E1306C')");
            stmt.execute("INSERT INTO social_media_platforms (name, icon, color) VALUES " +
                    "('Facebook', 'facebook.png', '#1877F2')");
            stmt.execute("INSERT INTO social_media_platforms (name, icon, color) VALUES " +
                    "('LinkedIn', 'linkedin.png', '#0A66C2')");
            stmt.execute("INSERT INTO social_media_platforms (name, icon, color) VALUES " +
                    "('X', 'twitter.png', '#000000')");
            stmt.close();
            System.out.println("Default social media platforms added");
            addSampleData();
        }
    }

    private static void addSampleData() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("INSERT INTO users (username, full_name, email, password_hash) VALUES " +
                "('demo', 'Demo User', 'demo@example.com', '" + util.PasswordHasher.hashPassword("password") + "')");

        java.sql.ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE username = 'demo'");
        rs.next();
        int userId = rs.getInt("id");
        rs.close();

        stmt.execute("INSERT INTO posts (platform_id, user_id, username, content, post_date, likes) VALUES " +
                "(1, " + userId + ", 'demo', 'This is my first Instagram post! #excited', CURRENT_TIMESTAMP, 15)");
        stmt.execute("INSERT INTO posts (platform_id, user_id, username, content, post_date, likes) VALUES " +
                "(2, " + userId + ", 'demo', 'Just joined Facebook. Connect with me!', CURRENT_TIMESTAMP, 10)");
        stmt.execute("INSERT INTO posts (platform_id, user_id, username, content, post_date, likes) VALUES " +
                "(3, " + userId + ", 'demo', 'Excited to announce I have joined a new company as a software developer. #newjob #career', CURRENT_TIMESTAMP, 24)");
        stmt.execute("INSERT INTO posts (platform_id, user_id, username, content, post_date, likes) VALUES " +
                "(4, " + userId + ", 'demo', 'Just posted my first tweet on X! #HelloWorld', CURRENT_TIMESTAMP, 7)");

        java.sql.ResultSet postRs = stmt.executeQuery("SELECT id FROM posts ORDER BY id");
        if (postRs.next()) {
            int postId = postRs.getInt("id");
            stmt.execute("INSERT INTO comments (post_id, user_id, username, content, comment_date) VALUES " +
                    "(" + postId + ", " + userId + ", 'demo', 'This is a comment on my own post!', CURRENT_TIMESTAMP)");
        }
        postRs.close();
        stmt.close();
        System.out.println("Sample data added successfully");
    }
}