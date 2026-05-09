package cn.edu.zju.crawler;

import cn.edu.zju.bean.DashboardStats;
import cn.edu.zju.dao.DashboardStatsDao;

public class DashboardStatsMain {

    public static void main(String[] args) {
        DashboardStatsDao dao = new DashboardStatsDao();
        DashboardStats stats = dao.loadStats();
        System.out.println("matchedSamples=" + stats.getMatchedSamples());
        System.out.println("explainedVariants=" + stats.getExplainedVariants());
        System.out.println("coveredDrugs=" + stats.getCoveredDrugs());
        System.out.println("totalUsers=" + stats.getTotalUsers());
        System.out.println("lastUpdated=" + stats.getLastUpdated());
    }
}

