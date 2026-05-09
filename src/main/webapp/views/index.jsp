<%--
  Created by IntelliJ IDEA.
  User: hello
  Date: 2019-12-3
  Time: 15:37
  To change this template use File |
  Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="">
    <meta name="author" content="">
    <meta name="generator" content="">
    <title>Precision Medicine Matching System</title>

    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">

    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">

    <script src="https://unpkg.com/feather-icons"></script>
</head>
<body class="home-page">

<jsp:include page="nav.jsp" >
    <jsp:param name="active" value="dashboard" />
</jsp:include>

<main role="main" class="app-main mt-4 px-2 px-md-4">
    <div class="app-main-content">

        <div class="hero-card">
            <div class="hero-eyebrow">Why Teams Use This System</div>
            <h1 class="hero-title">精准医疗匹配系统</h1>
            <p class="hero-subtitle mb-4 text-muted" style="max-width: 600px; margin-left: 0;">
                面向医学研究与临床辅助场景，支持样本上传、变异匹配和推荐药物回溯，帮助用户更高效地完成解释与决策。
            </p>
            <div class="hero-actions justify-content-start gap-2 flex-wrap">
                <a href="<%=request.getContextPath()%>/matchingIndex" class="btn btn-primary">开始匹配</a>
                <a href="<%=request.getContextPath()%>/kb/drugs" class="btn btn-outline-primary">浏览知识库</a>
            </div>
        </div>

        <h3 class="mb-4 font-weight-bold" style="color: #1e2a36; padding-left: 1rem;">Core Workflows</h3>

        <div class="workflow-grid mb-4">
            <div class="workflow-card">
                <span data-feather="cloud-upload" class="workflow-icon"></span>
                <h4>Upload and Match</h4>
                <p>上传 Annovar 注释 TSV，快速生成样本匹配结果与推荐药物。</p>
                <a href="<%=request.getContextPath()%>/matchingIndex" class="btn btn-sm btn-primary mt-auto">Go to Matching</a>
            </div>

            <div class="workflow-card">
                <span data-feather="database" class="workflow-icon"></span>
                <h4>Sample Management</h4>
                <p>查看样本上传时间、归属信息和可复查的匹配入口。</p>
                <a href="<%=request.getContextPath()%>/samples" class="btn btn-sm btn-outline-primary mt-auto">View Samples</a>
            </div>

            <div class="workflow-card">
                <span data-feather="user-check" class="workflow-icon"></span>
                <h4>Recommendation History</h4>
                <p>在个人主页查看推荐药物与命中基因，便于回顾和沟通。</p>
                <a href="<%=request.getContextPath()%>/profile" class="btn btn-sm btn-outline-primary mt-auto">Open Profile</a>
            </div>
        </div>

        <div class="app-panel mt-4">
            <div class="stats-grid">
                <div class="stat-item">
                    <div class="stat-label">Matched Samples</div>
                    <div class="stat-value">${dashboardStats.matchedSamples}</div>
                </div>
                <div class="stat-item border-left pl-3">
                    <div class="stat-label">Explained Variants</div>
                    <div class="stat-value">${dashboardStats.explainedVariants}</div>
                </div>
                <div class="stat-item border-left pl-3">
                    <div class="stat-label">Covered Drugs</div>
                    <div class="stat-value d-flex align-items-center gap-2">
                        ${dashboardStats.coveredDrugs} <span class="status-tag verified ml-2" style="font-size: 0.7rem;">Verified</span>
                    </div>
                </div>
                <div class="stat-item border-left pl-3">
                    <div class="stat-label">Total Users</div>
                    <div class="stat-value">${dashboardStats.totalUsers}</div>
                </div>
            </div>
            <div class="mt-3 text-muted" style="font-size: 0.8rem;">
                Last updated: ${dashboardStats.lastUpdated} | Data source: Internal knowledge base
            </div>
        </div>

    </div>
</main>

<script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
<script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
<script>
    feather.replace()
</script>
</body>
</html>