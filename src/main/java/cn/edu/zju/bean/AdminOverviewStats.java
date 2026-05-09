package cn.edu.zju.bean;

public class AdminOverviewStats {

    private long totalUsers;
    private long totalSamples;
    private long totalRecommendations;
    private long samplesToday;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalSamples() {
        return totalSamples;
    }

    public void setTotalSamples(long totalSamples) {
        this.totalSamples = totalSamples;
    }

    public long getTotalRecommendations() {
        return totalRecommendations;
    }

    public void setTotalRecommendations(long totalRecommendations) {
        this.totalRecommendations = totalRecommendations;
    }

    public long getSamplesToday() {
        return samplesToday;
    }

    public void setSamplesToday(long samplesToday) {
        this.samplesToday = samplesToday;
    }
}

