package com.rizik.training.instamate.Model;

public class Post {
    private String idUpload;
    private String gambar;
    private String uploader;
    private String deskripsi;

    public Post(){

    }

    public Post(String idUpload, String gambar, String uploader, String deskripsi) {
        this.idUpload = idUpload;
        this.gambar = gambar;
        this.uploader = uploader;
        this.deskripsi = deskripsi;
    }

    public String getIdUpload() {
        return idUpload;
    }

    public void setIdUpload(String idUpload) {
        this.idUpload = idUpload;
    }

    public String getGambar() {
        return gambar;
    }

    public void setGambar(String gambar) {
        this.gambar = gambar;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }
}
