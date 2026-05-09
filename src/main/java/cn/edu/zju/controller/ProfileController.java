package cn.edu.zju.controller;

import cn.edu.zju.bean.RecommendationRecord;
import cn.edu.zju.bean.User;
import cn.edu.zju.dao.RecommendationDao;
import cn.edu.zju.servlet.DispatchServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileController {

    private final RecommendationDao recommendationDao = new RecommendationDao();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerGetMapping("/profile", this::profile);
    }

    public void profile(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute(AuthController.CURRENT_USER);
        if (currentUser == null || currentUser.getId() == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        List<RecommendationRecord> recommendationRecords;
        try {
            recommendationRecords = recommendationDao.findByUserId(currentUser.getId());
        } catch (IllegalStateException e) {
            recommendationRecords = new ArrayList<>();
            request.setAttribute("profileError", "推荐记录暂时不可用，请先执行数据库升级脚本 V20260313_01_create_recommendation_record.sql");
        }
        request.setAttribute("recommendationRecords", recommendationRecords);
        request.getRequestDispatcher("/views/profile.jsp").forward(request, response);
    }
}

