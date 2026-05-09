package cn.edu.zju.servlet;

import cn.edu.zju.controller.AuthController;
import cn.edu.zju.controller.AdminController;
import cn.edu.zju.controller.IndexController;
import cn.edu.zju.controller.KnowledgeBaseController;
import cn.edu.zju.controller.MatchingController;
import cn.edu.zju.controller.ProfileController;
import cn.edu.zju.bean.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DispatchServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(DispatchServlet.class);
    private static final Set<String> WHITELIST_PATHS = new HashSet<>(Arrays.asList(
            "/",
            "/login",
            "/register",
            "/logout",
            "/kb/drugs",
            "/kb/guidelines",
            "/drugs",
            "/drugLabels",
            "/dosingGuideline"
    ));

    private ConcurrentHashMap<String, HttpConsumer<HttpServletRequest, HttpServletResponse>> getRequestMapping;
    private ConcurrentHashMap<String, HttpConsumer<HttpServletRequest, HttpServletResponse>> postRequestMapping;

    private HttpConsumer<HttpServletRequest, HttpServletResponse> notFound = (request, response) -> {
        try {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Not Found");
        } catch (IOException e) {
            log.info("", e);
        }
    };

    public class Dispatcher {
        public void registerGetMapping(String path, HttpConsumer<HttpServletRequest, HttpServletResponse> consumer) {
            getRequestMapping.put(path, consumer);
        }
        public void registerPostMapping(String path, HttpConsumer<HttpServletRequest, HttpServletResponse> consumer) {
            postRequestMapping.put(path, consumer);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.getRequestMapping = new ConcurrentHashMap<>();
        this.postRequestMapping = new ConcurrentHashMap<>();

        Dispatcher dispatcher = new Dispatcher();
        IndexController indexController = new IndexController();
        indexController.register(dispatcher);

        KnowledgeBaseController knowledgeBaseController = new KnowledgeBaseController();
        knowledgeBaseController.register(dispatcher);

        MatchingController matchingController = new MatchingController();
        matchingController.register(dispatcher);

        ProfileController profileController = new ProfileController();
        profileController.register(dispatcher);

        AuthController authController = new AuthController();
        authController.register(dispatcher);

        AdminController adminController = new AdminController();
        adminController.register(dispatcher);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = getPathInfo(req);
        log.info("{}: {}", req.getMethod(), pathInfo);
        if (requiresAuth(pathInfo) && !isLoggedIn(req)) {
            handleUnauthenticated(req, resp);
            return;
        }
        if (requiresAdmin(pathInfo) && !isAdmin(req)) {
            handleForbidden(req, resp, pathInfo);
            return;
        }
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = getPathInfo(req);
        HttpConsumer<HttpServletRequest, HttpServletResponse> consumer = getRequestMapping.getOrDefault(pathInfo, notFound);
        consumer.accept(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = getPathInfo(req);
        HttpConsumer<HttpServletRequest, HttpServletResponse> consumer = postRequestMapping.getOrDefault(pathInfo, notFound);
        consumer.accept(req, resp);
    }

    private boolean requiresAuth(String pathInfo) {
        return !WHITELIST_PATHS.contains(pathInfo);
    }

    private boolean requiresAdmin(String pathInfo) {
        return pathInfo != null && pathInfo.startsWith("/admin");
    }

    private boolean isLoggedIn(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute(AuthController.CURRENT_USER) != null;
    }

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return false;
        }
        User user = (User) session.getAttribute(AuthController.CURRENT_USER);
        return AuthController.isAdmin(user);
    }

    private void handleUnauthenticated(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestedWith = req.getHeader("X-Requested-With");
        boolean ajaxRequest = "XMLHttpRequest".equalsIgnoreCase(requestedWith);
        if (ajaxRequest) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"message\":\"Unauthorized\"}");
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    private void handleForbidden(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(AuthController.CURRENT_USER);
        String username = user == null ? "anonymous" : user.getUsername();
        log.warn("Forbidden admin access user={} path={}", username, pathInfo);

        String requestedWith = req.getHeader("X-Requested-With");
        boolean ajaxRequest = "XMLHttpRequest".equalsIgnoreCase(requestedWith);
        if (ajaxRequest) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"message\":\"Forbidden\"}");
            return;
        }
        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
    }

    private String getPathInfo(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }
        return pathInfo;
    }
}
