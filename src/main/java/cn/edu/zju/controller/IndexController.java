package cn.edu.zju.controller;

import cn.edu.zju.bean.DashboardStats;
import cn.edu.zju.dao.DashboardStatsDao;
import cn.edu.zju.servlet.DispatchServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class IndexController {

    private static final Logger log = LoggerFactory.getLogger(IndexController.class);
    private final DashboardStatsDao dashboardStatsDao = new DashboardStatsDao();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerGetMapping("/", this::index);
    }

    public void index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DashboardStats stats;
        try {
            stats = dashboardStatsDao.loadStats();
        } catch (Exception e) {
            log.warn("Failed to load dashboard stats, fallback to zeros", e);
            stats = new DashboardStats();
            stats.setLastUpdated("N/A");
        }
        request.setAttribute("dashboardStats", stats);
        request.getRequestDispatcher("/views/index.jsp").forward(request, response);

    }
}
