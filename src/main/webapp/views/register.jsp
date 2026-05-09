<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Register</title>
    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-dark fixed-top bg-dark flex-md-nowrap p-0 shadow">
    <a class="navbar-brand col-sm-3 col-md-2 mr-0" href="<%=request.getContextPath()%>/">Precision Medicine Matching System</a>
</nav>
<div class="container-fluid">
    <div class="row">
        <jsp:include page="nav.jsp">
            <jsp:param name="active" value="register"/>
        </jsp:include>
        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4 app-main">
            <div class="pt-3 pb-2 mb-3 border-bottom">
                <h2>Register</h2>
            </div>
            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>
            <div class="app-panel app-form-panel">
                <form method="post" action="<%=request.getContextPath()%>/register">
                    <div class="form-group">
                        <label for="username">Username</label>
                        <input id="username" name="username" class="form-control" value="${username}" maxlength="32" required>
                        <small class="form-text text-muted">3-32 位，只允许字母、数字和下划线。</small>
                    </div>
                    <div class="form-group">
                        <label for="password">Password</label>
                        <input id="password" type="password" name="password" class="form-control" minlength="6" required>
                        <small class="form-text text-muted">至少 6 位。</small>
                    </div>
                    <button type="submit" class="btn btn-primary">Register</button>
                    <a class="btn btn-link" href="<%=request.getContextPath()%>/login">已有账号？去登录</a>
                </form>
            </div>
        </main>
    </div>
</div>
</body>
</html>
