package cn.edu.zju.controller;

import cn.edu.zju.bean.AdminOverviewStats;
import cn.edu.zju.bean.User;
import cn.edu.zju.dao.AdminDebugDao;
import cn.edu.zju.servlet.DispatchServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private static final int DEFAULT_DEBUG_LIST_LIMIT = 20;

    private final AdminDebugDao adminDebugDao = new AdminDebugDao();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerGetMapping("/admin/debug", this::debugDashboard);
    }

    public void debugDashboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute(AuthController.CURRENT_USER);
        if (!AuthController.isAdmin(currentUser)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }

        try {
            AdminOverviewStats overviewStats = adminDebugDao.loadOverviewStats();
            request.setAttribute("overviewStats", overviewStats);
            request.setAttribute("adminUsers", adminDebugDao.listUserStatus(DEFAULT_DEBUG_LIST_LIMIT));
            request.setAttribute("recentSubmissions", adminDebugDao.listRecentSubmissions(DEFAULT_DEBUG_LIST_LIMIT));
            request.setAttribute("flowRecords", adminDebugDao.listRecentDataFlow(DEFAULT_DEBUG_LIST_LIMIT));
            request.setAttribute("recommendationTableReady", adminDebugDao.isRecommendationTableReady());
            log.info("Admin debug dashboard viewed by user={}", currentUser.getUsername());
        } catch (IllegalStateException e) {
            log.warn("Failed to load admin debug dashboard", e);
            request.setAttribute("adminError", "后台调试数据暂时不可用，请检查数据库连接和迁移状态。");
            request.setAttribute("overviewStats", new AdminOverviewStats());
        }

        request.getRequestDispatcher("/views/admin_debug.jsp").forward(request, response);
    }
}

