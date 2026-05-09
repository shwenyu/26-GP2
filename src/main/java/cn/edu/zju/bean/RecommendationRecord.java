package cn.edu.zju.bean;

import java.util.Date;

public class RecommendationRecord {

    private Long id;
    private Long userId;
    private Integer sampleId;
    private String drugLabelId;
    private String drugName;
    private String source;
    private String summaryMarkdown;
    private String matchedGenes;
    private Date createdAt;

    public RecommendationRecord() {
    }

    public RecommendationRecord(Long id, Long userId, Integer sampleId, String drugLabelId, String drugName, String source,
                                String summaryMarkdown, String matchedGenes, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.sampleId = sampleId;
        this.drugLabelId = drugLabelId;
        this.drugName = drugName;
        this.source = source;
        this.summaryMarkdown = summaryMarkdown;
        this.matchedGenes = matchedGenes;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getSampleId() {
        return sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public String getDrugLabelId() {
        return drugLabelId;
    }

    public void setDrugLabelId(String drugLabelId) {
        this.drugLabelId = drugLabelId;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSummaryMarkdown() {
        return summaryMarkdown;
    }

    public void setSummaryMarkdown(String summaryMarkdown) {
        this.summaryMarkdown = summaryMarkdown;
    }

    public String getMatchedGenes() {
        return matchedGenes;
    }

    public void setMatchedGenes(String matchedGenes) {
        this.matchedGenes = matchedGenes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

