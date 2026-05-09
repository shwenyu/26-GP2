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
    <title>Upload and Match</title>

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
                <h2 class="mb-1">Upload and Match</h2>
            </div>

            <div class="page-intro mb-3">
                Upload an Annovar TSV file to run the matching pipeline. The system links the sample to your account and shows traceable recommendation results.
            </div>

            <div class="app-panel process-panel">
                <h5 class="mb-3">Matching Pipeline</h5>
                <div class="process-steps">
                    <div class="process-step"><span>1</span><strong>Quality Filter</strong><small>QUAL and genotype validation</small></div>
                    <div class="process-step"><span>2</span><strong>Phenotype Inference</strong><small>Gene-level interpretation</small></div>
                    <div class="process-step"><span>3</span><strong>Guideline Match</strong><small>Drug recommendation lookup</small></div>
                </div>
            </div>

            <c:if test="${not empty validateError}">
                <div class="alert alert-danger" role="alert">${validateError}</div>
            </c:if>

            <div class="app-panel app-form-panel">
                <form method="post" action="upload" enctype="multipart/form-data">
                    <div class="form-group">
                        <label for="annovarFile">Annovar Output File</label>
                        <input type="file" class="form-control-file" id="annovarFile" name="annovar" required>
                        <small class="form-text text-muted">Accepted format: tab-delimited Annovar result (UTF-8).</small>
                    </div>
                    <div class="form-group">
                        <label>Uploaded By</label>
                        <input type="text" class="form-control" value="${sessionScope.currentUser.username}" disabled>
                        <small class="form-text text-muted">Sample ownership is automatically bound to your account.</small>
                    </div>
                    <div class="d-flex flex-wrap align-items-center">
                        <button type="submit" class="btn btn-primary mr-2">Upload and Run</button>
                        <a class="btn btn-outline-primary" href="<%=request.getContextPath()%>/samples">View My Samples</a>
                    </div>
                </form>
            </div>
        </main>
    </div>
</div>
</body>
</html>
