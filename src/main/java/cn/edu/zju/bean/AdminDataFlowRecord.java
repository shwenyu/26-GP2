package cn.edu.zju.bean;

import java.util.Date;

public class AdminDataFlowRecord {

    private int sampleId;
    private String username;
    private Date uploadedAt;
    private long variantCount;
    private long distinctGeneCount;
    private long recommendationCount;
    private String flowStatus;

    public int getSampleId() {
        return sampleId;
    }

    public void setSampleId(int sampleId) {
        this.sampleId = sampleId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Date uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public long getVariantCount() {
        return variantCount;
    }

    public void setVariantCount(long variantCount) {
        this.variantCount = variantCount;
    }

    public long getDistinctGeneCount() {
        return distinctGeneCount;
    }

    public void setDistinctGeneCount(long distinctGeneCount) {
        this.distinctGeneCount = distinctGeneCount;
    }

    public long getRecommendationCount() {
        return recommendationCount;
    }

    public void setRecommendationCount(long recommendationCount) {
        this.recommendationCount = recommendationCount;
    }

    public String getFlowStatus() {
        return flowStatus;
    }

    public void setFlowStatus(String flowStatus) {
        this.flowStatus = flowStatus;
    }
}

