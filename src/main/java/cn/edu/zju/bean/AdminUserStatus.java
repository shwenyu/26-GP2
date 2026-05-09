package cn.edu.zju.bean;

import java.util.Date;

public class AdminUserStatus {

    private long userId;
    private String username;
    private Date createdAt;
    private long sampleCount;
    private long recommendationCount;
    private Date lastSampleAt;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(long sampleCount) {
        this.sampleCount = sampleCount;
    }

    public long getRecommendationCount() {
        return recommendationCount;
    }

    public void setRecommendationCount(long recommendationCount) {
        this.recommendationCount = recommendationCount;
    }

    public Date getLastSampleAt() {
        return lastSampleAt;
    }

    public void setLastSampleAt(Date lastSampleAt) {
        this.lastSampleAt = lastSampleAt;
    }
}

