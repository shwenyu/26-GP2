<%--
  Created by IntelliJ IDEA.
  User: hello
  Date: 2019-12-3
  Time: 15:37
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page isELIgnored="false" %>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="">
    <meta name="author" content="">
    <meta name="generator" content="">
    <title>Matching Results</title>

    <!-- Bootstrap core CSS -->
    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <!-- Custom styles for this template -->
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-dark fixed-top bg-dark flex-md-nowrap p-0 shadow">
    <a class="navbar-brand col-sm-3 col-md-2 mr-0 site-title-art" href="#">Precision Medicine Matching System</a>

</nav>

<div class="container-fluid">
    <div class="row">
        <jsp:include page="nav.jsp" >
            <jsp:param name="active" value="matching_index" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4 app-main">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-2 border-bottom">
                <h2 class="mb-1">Matching Results</h2>
            </div>

            <div class="page-intro mb-3">
                Review sample summary, matched records, and source traceability before clinical interpretation.
            </div>

            <c:if test="${not empty matchingWarn}">
                <div class="alert alert-warning" role="alert">${matchingWarn}</div>
            </c:if>

            <c:set var="matchedCount" value="${fn:length(matchedRecords)}" />
            <div class="stats-grid mb-3">
                <div class="stat-card">
                    <div class="stat-label">Sample ID</div>
                    <div class="stat-value">#${sample.id}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Uploaded At</div>
                    <div class="stat-value stat-value-sm">${sample.createdAt}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Uploaded By</div>
                    <div class="stat-value stat-value-sm">${sample.uploadedByUsername}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Matched Records</div>
                    <div class="stat-value">${matchedCount}</div>
                </div>
            </div>

            <div class="app-panel">
                <div class="d-flex justify-content-between align-items-center flex-wrap mb-2">
                    <h4 class="mb-2">Matched Drug Labels</h4>
                    <a class="btn btn-sm btn-outline-primary mb-2" href="<%=request.getContextPath()%>/matchingIndex">Back to Upload</a>
                </div>
                <c:if test="${not empty matchedRecords}">
                    <div class="table-responsive">
                        <table class="table table-striped table-sm">
                            <thead>
                            <tr>
                                <th>#</th>
                                <th>Name</th>
                                <th>Matched Genes</th>
                                <th>Source</th>
                                <th>Summary</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${matchedRecords}" var="item" varStatus="loop">
                                <tr>
                                    <td>${loop.index + 1}</td>
                                    <td>${item.drugName}</td>
                                    <td>${item.matchedGenes}</td>
                                    <td>
                                        <span class="source-tag ${item.source == 'CPIC' ? 'source-tag-cpic' : 'source-tag-pharmgkb'}">${empty item.source ? 'PharmGKB' : item.source}</span>
                                    </td>
                                    <td class="table-cell-clamp">${item.summaryMarkdown}</td>
                                </tr>
                            </c:forEach>

                            </tbody>
                        </table>
                    </div>
                </c:if>
                <c:if test="${empty matchedRecords}">
                    <div class="app-empty-state">
                        <h5>No matched records</h5>
                        <p>This sample currently has no recommendation hit. You can upload another file or verify upstream annotation quality.</p>
                    </div>
                </c:if>
            </div>
        </main>
    </div>
</div>
</body>
</html>
