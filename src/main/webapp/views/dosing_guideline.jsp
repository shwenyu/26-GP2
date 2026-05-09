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
    <title>Knowledge Base - Guidelines</title>

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
            <jsp:param name="active" value="dosing_guideline" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4 app-main">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-2 border-bottom">
                <h2 class="mb-1">Knowledge Base - Guidelines</h2>
            </div>

            <div class="page-intro mb-3">
                Ranked guidance records for pharmacogenomic interpretation. Use source and evidence filters to compare PharmGKB and CPIC records.
            </div>

            <c:set var="sourceFilter" value="${empty param.source ? 'all' : param.source}" />
            <c:set var="evidenceFilter" value="${empty param.evidenceLevel ? 'all' : param.evidenceLevel}" />
            <c:set var="recommendationFilter" value="${empty param.recommendation ? 'all' : param.recommendation}" />
            <c:set var="keyword" value="${empty param.keyword ? '' : fn:toLowerCase(param.keyword)}" />

            <div class="app-panel filter-bar">
                <form class="form-row align-items-end" method="get" action="<%=request.getContextPath()%>/kb/guidelines">
                    <div class="form-group col-md-3">
                        <label for="source">Data Source</label>
                        <select id="source" name="source" class="form-control">
                            <option value="all" ${sourceFilter == 'all' ? 'selected' : ''}>All</option>
                            <option value="pharmgkb" ${sourceFilter == 'pharmgkb' ? 'selected' : ''}>PharmGKB (Legacy)</option>
                            <option value="cpic" ${sourceFilter == 'cpic' ? 'selected' : ''}>CPIC</option>
                            <option value="cpnds" ${sourceFilter == 'cpnds' ? 'selected' : ''}>CPNDS</option>
                            <option value="dpwg" ${sourceFilter == 'dpwg' ? 'selected' : ''}>DPWG</option>
                            <option value="fda" ${sourceFilter == 'fda' ? 'selected' : ''}>FDA Label</option>
                            <option value="pro" ${sourceFilter == 'pro' ? 'selected' : ''}>Professional Society</option>
                            <option value="unknown" ${sourceFilter == 'unknown' ? 'selected' : ''}>Unknown</option>
                        </select>
                    </div>
                    <div class="form-group col-md-3">
                        <label for="evidenceLevel">Evidence</label>
                        <select id="evidenceLevel" name="evidenceLevel" class="form-control">
                            <option value="all" ${evidenceFilter == 'all' ? 'selected' : ''}>All</option>
                            <option value="1A" ${evidenceFilter == '1A' ? 'selected' : ''}>1A</option>
                            <option value="1B" ${evidenceFilter == '1B' ? 'selected' : ''}>1B</option>
                            <option value="2A" ${evidenceFilter == '2A' ? 'selected' : ''}>2A</option>
                            <option value="2B" ${evidenceFilter == '2B' ? 'selected' : ''}>2B</option>
                            <option value="3" ${evidenceFilter == '3' ? 'selected' : ''}>3</option>
                            <option value="4" ${evidenceFilter == '4' ? 'selected' : ''}>4</option>
                        </select>
                    </div>
                    <div class="form-group col-md-3">
                        <label for="recommendation">Recommendation</label>
                        <select id="recommendation" name="recommendation" class="form-control">
                            <option value="all" ${recommendationFilter == 'all' ? 'selected' : ''}>All</option>
                            <option value="recommended" ${recommendationFilter == 'recommended' ? 'selected' : ''}>Recommended</option>
                            <option value="review" ${recommendationFilter == 'review' ? 'selected' : ''}>Needs Review</option>
                        </select>
                    </div>
                    <div class="form-group col-md-2">
                        <label for="keyword">Keyword</label>
                        <input id="keyword" name="keyword" class="form-control" value="${param.keyword}" placeholder="Guideline title keyword">
                    </div>
                    <div class="form-group col-md-1 filter-actions">
                        <button class="btn btn-primary btn-block" type="submit">Apply</button>
                    </div>
                </form>
                <a class="btn btn-link btn-sm px-0" href="<%=request.getContextPath()%>/kb/guidelines">Reset filters</a>
            </div>

            <div class="app-panel">
                <c:set var="rowCount" value="0" />
                <div class="table-responsive">
                    <table class="table table-striped table-sm">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Guideline</th>
                        <th>Source</th>
                        <th>Recommendation</th>
                        <th>Evidence</th>
                        <th>Summary</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${dosingGuidelines}" var="item">
                        <c:set var="nameText" value="${fn:toLowerCase(item.name)}" />
                        <c:set var="sourceText" value="${item.sourceCode}" />
                        <c:set var="sourceDisplay" value="${item.sourceDisplay}" />
                        <c:set var="evidenceText" value="${empty item.evidenceLevel ? 'N/A' : item.evidenceLevel}" />
                        <c:set var="matchKeyword" value="${empty keyword or fn:contains(nameText, keyword)}" />
                        <c:set var="matchSource" value="${sourceFilter == 'all' or sourceFilter == sourceText}" />
                        <c:set var="matchEvidence" value="${evidenceFilter == 'all' or evidenceFilter == evidenceText}" />
                        <c:set var="matchRecommendation" value="${recommendationFilter == 'all' or (recommendationFilter == 'recommended' and item.recommendation) or (recommendationFilter == 'review' and not item.recommendation)}" />
                        <c:if test="${matchKeyword and matchSource and matchEvidence and matchRecommendation}">
                            <c:set var="rowCount" value="${rowCount + 1}" />
                            <tr>
                                <td>${item.id}</td>
                                <td>${item.name}</td>
                                <td>
                                    <span class="source-tag ${sourceText == 'cpic' ? 'source-tag-cpic' : 'source-tag-pharmgkb'}">
                                        ${sourceDisplay}
                                    </span>
                                </td>
                                <td>
                                    <span class="status-tag ${item.recommendation ? 'status-yes' : 'status-no'}">
                                        ${item.recommendation ? 'Recommended' : 'Needs Review'}
                                    </span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${evidenceText == '1A' or evidenceText == '1B'}">
                                            <span class="evidence-badge evidence-badge-high">${evidenceText}</span>
                                        </c:when>
                                        <c:when test="${evidenceText == '2A' or evidenceText == '2B'}">
                                            <span class="evidence-badge evidence-badge-mid">${evidenceText}</span>
                                        </c:when>
                                        <c:when test="${evidenceText == '3' or evidenceText == '4'}">
                                            <span class="evidence-badge evidence-badge-low">${evidenceText}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="evidence-badge evidence-badge-unknown">N/A</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="table-cell-clamp">${item.summaryMarkdown}</td>
                            </tr>
                        </c:if>
                    </c:forEach>

                    </tbody>
                    </table>
                </div>
                <c:if test="${rowCount == 0}">
                    <div class="app-empty-state">
                        <h5>No guidelines found</h5>
                        <p>Try changing source, recommendation, or keyword filters.</p>
                    </div>
                </c:if>
            </div>
        </main>
    </div>
</div>
</body>
</html>
