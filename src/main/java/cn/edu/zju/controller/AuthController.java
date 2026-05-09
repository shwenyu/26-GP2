package cn.edu.zju.controller;

import cn.edu.zju.bean.User;
import cn.edu.zju.dao.UserDAO;
import cn.edu.zju.servlet.DispatchServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthController {

    public static final String CURRENT_USER = "currentUser";
    public static final String ADMIN_USERNAME = "root";
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_USERNAME_LENGTH = 32;
    private static final String AUTH_SYSTEM_ERROR = "认证功能暂时不可用，请确认数据库已初始化且包含 user 表。";

    private final UserDAO userDAO = new UserDAO();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerGetMapping("/login", this::loginPage);
        dispatcher.registerGetMapping("/register", this::registerPage);
        dispatcher.registerPostMapping("/login", this::login);
        dispatcher.registerPostMapping("/register", this::registerUser);
        dispatcher.registerGetMapping("/logout", this::logout);
    }

    public void loginPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/views/login.jsp").forward(request, response);
    }

    public void registerPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/views/register.jsp").forward(request, response);
    }

    public void registerUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = normalizeUsername(request.getParameter("username"));
        String password = request.getParameter("password");
        request.setAttribute("username", username);

        String validationError = validateCredential(username, password);
        if (validationError != null) {
            request.setAttribute("error", validationError);
            request.getRequestDispatcher("/views/register.jsp").forward(request, response);
            return;
        }

        try {
            if (userDAO.findByUsername(username) != null) {
                request.setAttribute("error", "用户名已存在，请更换后重试");
                request.getRequestDispatcher("/views/register.jsp").forward(request, response);
                return;
            }

            String passwordHash = PasswordUtils.hashPassword(password);
            Long userId = userDAO.insertUser(username, passwordHash);
            if (userId == null) {
                request.setAttribute("error", "注册失败，请稍后重试");
                request.getRequestDispatcher("/views/register.jsp").forward(request, response);
                return;
            }
        } catch (IllegalStateException e) {
            log.info("Register failed due to system error", e);
            request.setAttribute("error", AUTH_SYSTEM_ERROR);
            request.getRequestDispatcher("/views/register.jsp").forward(request, response);
            return;
        }

        log.info("Registered user {}", username);
        response.sendRedirect(request.getContextPath() + "/login?registered=1");
    }

    public void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = normalizeUsername(request.getParameter("username"));
        String password = request.getParameter("password");
        request.setAttribute("username", username);

        String validationError = validateCredential(username, password);
        if (validationError != null) {
            request.setAttribute("error", validationError);
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        User user;
        try {
            user = userDAO.findByUsername(username);
        } catch (IllegalStateException e) {
            log.info("Login failed due to system error", e);
            request.setAttribute("error", AUTH_SYSTEM_ERROR);
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        if (user == null || user.getPasswordHash() == null || !PasswordUtils.matches(password, user.getPasswordHash())) {
            request.setAttribute("error", "用户名不存在或密码错误");
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(CURRENT_USER, user);
        response.sendRedirect(request.getContextPath() + "/matchingIndex");
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login?logout=1");
    }

    private String validateCredential(String username, String password) {
        if (username == null || username.isBlank()) {
            return "用户名不能为空";
        }
        if (!username.matches("^[A-Za-z0-9_]{3,32}$")) {
            return "用户名需为 3-32 位字母、数字或下划线";
        }
        if (password == null || password.isBlank()) {
            return "密码不能为空";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "密码长度不能少于 6 位";
        }
        if (username.length() > MAX_USERNAME_LENGTH) {
            return "用户名长度不能超过 32 位";
        }
        return null;
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim();
    }

    public static boolean isAdmin(User user) {
        return user != null && ADMIN_USERNAME.equals(user.getUsername());
    }
}
