package com.rizik.training.instamate.Model;

public class Comment {

    private String text;
    private String mrComment;

    public Comment(String text, String mrComment) {
        this.text = text;
        this.mrComment = mrComment;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMrComment() {
        return mrComment;
    }

    public void setMrComment(String mrComment) {
        this.mrComment = mrComment;
    }
}
