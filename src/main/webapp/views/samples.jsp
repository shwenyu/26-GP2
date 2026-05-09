<%--
  Created by IntelliJ IDEA.
  User: hello
  Date: 2019-12-3
  Time: 15:37
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="">
    <meta name="author" content="">
    <meta name="generator" content="">
    <title>My Samples</title>

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
            <jsp:param name="active" value="samples" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4 app-main">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-2 border-bottom">
                <h2 class="mb-1">My Samples</h2>
            </div>

            <div class="page-intro mb-3">
                Review your uploaded samples and open matching results from the same workspace.
            </div>

            <div class="app-panel">
                <div class="table-responsive">
                    <table class="table table-striped table-sm">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Uploaded By</th>
                        <th>Uploaded At</th>
                        <th>Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:if test="${empty samples}">
                        <tr>
                            <td colspan="4">
                                <div class="app-empty-state mb-0">
                                    <h5>No samples yet</h5>
                                    <p>Upload your first annotation file to start matching.</p>
                                </div>
                            </td>
                        </tr>
                    </c:if>
                    <c:forEach items="${samples}" var="item" varStatus="loop">
                        <tr>
                            <td>${item.id}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty item.uploadedByUsername}">${item.uploadedByUsername}</c:when>
                                    <c:otherwise>User #${item.uploadedBy}</c:otherwise>
                                </c:choose>
                            </td>
                            <td>${item.createdAt}</td>
                            <td><a class="btn btn-sm btn-outline-primary" href="matching?sampleId=${item.id}">View Results</a></td>
                        </tr>
                    </c:forEach>

                    </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
</div>
</body>
</html>
