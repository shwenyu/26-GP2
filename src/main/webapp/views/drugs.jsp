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
    <title>Knowledge Base - Drugs</title>

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
            <jsp:param name="active" value="drugs" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4 app-main">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-2 border-bottom">
                <h2 class="mb-1">Knowledge Base - Drugs</h2>
            </div>

            <div class="page-intro mb-3">
                Browse drug entries and quickly narrow the list by source, biomarker status, and keyword.
            </div>

            <c:set var="sourceFilter" value="${empty param.source ? 'all' : param.source}" />
            <c:set var="biomarkerFilter" value="${empty param.biomarker ? 'all' : param.biomarker}" />
            <c:set var="keyword" value="${empty param.keyword ? '' : fn:toLowerCase(param.keyword)}" />

            <div class="app-panel filter-bar">
                <form class="form-row align-items-end" method="get" action="<%=request.getContextPath()%>/kb/drugs">
                    <div class="form-group col-md-3">
                        <label for="source">Data Source</label>
                        <select id="source" name="source" class="form-control">
                            <option value="all" ${sourceFilter == 'all' ? 'selected' : ''}>All</option>
                            <option value="pharmgkb" ${sourceFilter == 'pharmgkb' ? 'selected' : ''}>PharmGKB</option>
                            <option value="cpic" ${sourceFilter == 'cpic' ? 'selected' : ''}>CPIC</option>
                        </select>
                    </div>
                    <div class="form-group col-md-3">
                        <label for="biomarker">Biomarker</label>
                        <select id="biomarker" name="biomarker" class="form-control">
                            <option value="all" ${biomarkerFilter == 'all' ? 'selected' : ''}>All</option>
                            <option value="yes" ${biomarkerFilter == 'yes' ? 'selected' : ''}>Yes</option>
                            <option value="no" ${biomarkerFilter == 'no' ? 'selected' : ''}>No</option>
                        </select>
                    </div>
                    <div class="form-group col-md-4">
                        <label for="keyword">Keyword</label>
                        <input id="keyword" name="keyword" class="form-control" value="${param.keyword}" placeholder="Drug name keyword">
                    </div>
                    <div class="form-group col-md-2 filter-actions">
                        <button class="btn btn-primary btn-block" type="submit">Apply</button>
                    </div>
                </form>
                <a class="btn btn-link btn-sm px-0" href="<%=request.getContextPath()%>/kb/drugs">Reset filters</a>
            </div>

            <div class="app-panel">
                <c:set var="rowCount" value="0" />
                <div class="table-responsive">
                    <table class="table table-striped table-sm">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Name</th>
                        <th>Source</th>
                        <th>Biomarker</th>
                        <th>Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${drugs}" var="item">
                        <c:set var="nameText" value="${fn:toLowerCase(item.name)}" />
                        <c:set var="matchKeyword" value="${empty keyword or fn:contains(nameText, keyword)}" />
                        <c:set var="matchBiomarker" value="${biomarkerFilter == 'all' or (biomarkerFilter == 'yes' and item.biomarker) or (biomarkerFilter == 'no' and not item.biomarker)}" />
                        <c:set var="matchSource" value="${sourceFilter == 'all' or sourceFilter == 'pharmgkb'}" />
                        <c:if test="${matchKeyword and matchBiomarker and matchSource}">
                            <c:set var="rowCount" value="${rowCount + 1}" />
                            <tr>
                                <td>${item.id}</td>
                                <td>${item.name}</td>
                                <td><span class="source-tag source-tag-pharmgkb">PharmGKB</span></td>
                                <td>
                                    <span class="status-tag ${item.biomarker ? 'status-yes' : 'status-no'}">${item.biomarker ? 'Yes' : 'No'}</span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty item.drugUrlNormalized}">
                                            <a class="btn btn-sm btn-outline-primary" href="${item.drugUrlNormalized}" target="_blank" rel="noopener noreferrer">Open</a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">No link</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:if>
                    </c:forEach>

                    </tbody>
                    </table>
                </div>
                <c:if test="${rowCount == 0}">
                    <div class="app-empty-state">
                        <h5>No drugs found</h5>
                        <p>Try broadening your keyword or resetting filters.</p>
                    </div>
                </c:if>
            </div>
        </main>
    </div>
</div>
</body>
</html>
