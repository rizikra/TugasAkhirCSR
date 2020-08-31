package com.rizik.training.instamate.Model;

public class UserData {
    private String userId;
    private String username;
    private String fullname;
    private String bio;
    private String imageUrl;

    public UserData(){

    }

    public UserData(String userId, String username, String fullname, String bio, String imageUrl) {
        this.userId = userId;
        this.username = username;
        this.fullname = fullname;
        this.bio = bio;
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
