<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Admin Debug Console</title>
    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <script src="https://unpkg.com/feather-icons"></script>
</head>
<body>
<jsp:include page="nav.jsp">
    <jsp:param name="active" value="admin_debug"/>
</jsp:include>

<main role="main" class="app-main mt-4 px-2 px-md-4">
    <div class="app-panel">
        <h2 class="mb-2">后台数据检查</h2>
        <p class="text-muted mb-0">仅 root 用户可访问。该页面仅做只读调试与链路可视检查。</p>
    </div>

    <c:if test="${not recommendationTableReady}">
        <div class="alert alert-warning">
            recommendation_record 表尚未初始化，部分统计会展示为 0。
        </div>
    </c:if>

    <c:if test="${not empty adminError}">
        <div class="alert alert-danger">${adminError}</div>
    </c:if>

    <div class="admin-kpi-grid mb-4">
        <div class="admin-kpi-card">
            <div class="admin-kpi-label">Total Users</div>
            <div class="admin-kpi-value">${overviewStats.totalUsers}</div>
        </div>
        <div class="admin-kpi-card">
            <div class="admin-kpi-label">Total Samples</div>
            <div class="admin-kpi-value">${overviewStats.totalSamples}</div>
        </div>
        <div class="admin-kpi-card">
            <div class="admin-kpi-label">Recommendations</div>
            <div class="admin-kpi-value">${overviewStats.totalRecommendations}</div>
        </div>
        <div class="admin-kpi-card">
            <div class="admin-kpi-label">Samples Today</div>
            <div class="admin-kpi-value">${overviewStats.samplesToday}</div>
        </div>
    </div>

    <div class="app-panel">
        <h4 class="mb-3">用户状态（最近 20 条）</h4>
        <div class="table-responsive admin-table-wrap">
            <table class="table table-striped table-sm">
                <thead>
                <tr>
                    <th>User ID</th>
                    <th>Username</th>
                    <th>Created At</th>
                    <th>Samples</th>
                    <th>Recommendations</th>
                    <th>Last Upload</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${adminUsers}" var="item">
                    <tr>
                        <td>${item.userId}</td>
                        <td>${item.username}</td>
                        <td>${item.createdAt}</td>
                        <td>${item.sampleCount}</td>
                        <td>${item.recommendationCount}</td>
                        <td>${item.lastSampleAt}</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty adminUsers}">
                    <tr>
                        <td colspan="6" class="text-muted">暂无可展示用户数据</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <div class="app-panel">
        <h4 class="mb-3">数据提交（最近 20 条样本）</h4>
        <div class="table-responsive admin-table-wrap">
            <table class="table table-striped table-sm">
                <thead>
                <tr>
                    <th>Sample ID</th>
                    <th>Uploaded By</th>
                    <th>Uploaded At</th>
                    <th>Annovar Rows</th>
                    <th>Recommendation Rows</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${recentSubmissions}" var="item">
                    <tr>
                        <td>#${item.sampleId}</td>
                        <td>${empty item.username ? '-' : item.username}</td>
                        <td>${item.uploadedAt}</td>
                        <td>${item.variantCount}</td>
                        <td>${item.recommendationCount}</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty recentSubmissions}">
                    <tr>
                        <td colspan="5" class="text-muted">暂无样本提交数据</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <div class="app-panel">
        <h4 class="mb-3">后台数据流（最近 20 条）</h4>
        <div class="table-responsive admin-table-wrap">
            <table class="table table-striped table-sm">
                <thead>
                <tr>
                    <th>Sample ID</th>
                    <th>User</th>
                    <th>Uploaded At</th>
                    <th>Variant Count</th>
                    <th>Distinct Gene Count</th>
                    <th>Recommendation Count</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${flowRecords}" var="item">
                    <tr>
                        <td>#${item.sampleId}</td>
                        <td>${empty item.username ? '-' : item.username}</td>
                        <td>${item.uploadedAt}</td>
                        <td>${item.variantCount}</td>
                        <td>${item.distinctGeneCount}</td>
                        <td>${item.recommendationCount}</td>
                        <td>
                            <span class="badge badge-pill ${item.flowStatus == 'MATCH_READY' ? 'badge-success' : (item.flowStatus == 'NO_MATCHED_DRUG' ? 'badge-warning' : 'badge-secondary')}">
                                ${item.flowStatus}
                            </span>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty flowRecords}">
                    <tr>
                        <td colspan="7" class="text-muted">暂无数据流记录</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </div>
</main>

<script>
    feather.replace();
</script>
</body>
</html>

