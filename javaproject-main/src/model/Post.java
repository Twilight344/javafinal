package model;

import java.util.Date;

public class Post {
    private int id;
    private int platformId;
    private int userId;
    private String username;
    private String content;
    private Date postDate;
    private int likes;

    public Post(int id, int platformId, int userId, String username, String content, Date postDate, int likes) {
        this.id = id;
        this.platformId = platformId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.postDate = postDate;
        this.likes = likes;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlatformId() {
        return platformId;
    }

    public void setPlatformId(int platformId) {
        this.platformId = platformId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}
