package cn.edu.zju.bean;

import java.util.Date;

public class Sample {
    private int id;
    private Date createdAt;
    private Long uploadedBy;
    private String uploadedByUsername;

    public Sample() {
    }

    public Sample(int id, Date createdAt, Long uploadedBy, String uploadedByUsername) {
        this.id = id;
        this.createdAt = createdAt;
        this.uploadedBy = uploadedBy;
        this.uploadedByUsername = uploadedByUsername;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Long uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getUploadedByUsername() {
        return uploadedByUsername;
    }

    public void setUploadedByUsername(String uploadedByUsername) {
        this.uploadedByUsername = uploadedByUsername;
    }
}
