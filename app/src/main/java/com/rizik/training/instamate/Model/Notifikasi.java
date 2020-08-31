package com.rizik.training.instamate.Model;

public class Notifikasi {
    private String userId;
    private String text;
    private String idUpload;
    private boolean isPost;

    public Notifikasi(String userId, String text, String idUpload, boolean isPost) {
        this.userId = userId;
        this.text = text;
        this.idUpload = idUpload;
        this.isPost = isPost;
    }

    public Notifikasi() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIdUpload() {
        return idUpload;
    }

    public void setIdUpload(String idUpload) {
        this.idUpload = idUpload;
    }

    public boolean isPost() {
        return isPost;
    }

    public void setPost(boolean post) {
        isPost = post;
    }
}
