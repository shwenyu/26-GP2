package cn.edu.zju.bean;

public class DashboardStats {

    private long matchedSamples;
    private long explainedVariants;
    private long coveredDrugs;
    private long totalUsers;
    private String lastUpdated;

    public long getMatchedSamples() {
        return matchedSamples;
    }

    public void setMatchedSamples(long matchedSamples) {
        this.matchedSamples = matchedSamples;
    }

    public long getExplainedVariants() {
        return explainedVariants;
    }

    public void setExplainedVariants(long explainedVariants) {
        this.explainedVariants = explainedVariants;
    }

    public long getCoveredDrugs() {
        return coveredDrugs;
    }

    public void setCoveredDrugs(long coveredDrugs) {
        this.coveredDrugs = coveredDrugs;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}

