<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>User Profile</title>
    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-dark fixed-top bg-dark flex-md-nowrap p-0 shadow">
    <a class="navbar-brand col-sm-3 col-md-2 mr-0" href="#">Precision Medicine Matching System</a>
</nav>

<div class="container-fluid">
    <div class="row">
        <jsp:include page="nav.jsp">
            <jsp:param name="active" value="profile"/>
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4 app-main">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Profile</h2>
            </div>

            <c:if test="${not empty profileError}">
                <div class="alert alert-warning" role="alert">${profileError}</div>
            </c:if>

            <div class="app-panel" role="region" aria-label="User profile info">
                <div><strong>Username:</strong> ${sessionScope.currentUser.username}</div>
                <div><strong>User ID:</strong> ${sessionScope.currentUser.id}</div>
            </div>

            <div class="app-panel">
                <h4>Recommended Drugs History</h4>
                <c:choose>
                    <c:when test="${not empty recommendationRecords}">
                        <div class="table-responsive">
                            <table class="table table-striped table-sm">
                                <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Sample</th>
                                    <th>Drug</th>
                                    <th>Matched Genes</th>
                                    <th>Source</th>
                                    <th>Created At</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${recommendationRecords}" var="item" varStatus="loop">
                                    <tr>
                                        <td>${loop.index + 1}</td>
                                        <td><a href="matching?sampleId=${item.sampleId}">#${item.sampleId}</a></td>
                                        <td>${item.drugName}</td>
                                        <td>${item.matchedGenes}</td>
                                        <td>${item.source}</td>
                                        <td>${item.createdAt}</td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="alert alert-info" role="alert">
                            暂无推荐记录。请先上传 TSV 并完成匹配。
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>
</div>
</body>
</html>

