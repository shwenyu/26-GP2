<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Login</title>
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
            <jsp:param name="active" value="login"/>
        </jsp:include>
        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4 app-main">
            <div class="pt-3 pb-2 mb-3 border-bottom">
                <h2>Login</h2>
            </div>
            <c:if test="${param.registered == '1'}">
                <div class="alert alert-success">注册成功，请登录。</div>
            </c:if>
            <c:if test="${param.logout == '1'}">
                <div class="alert alert-info">您已安全退出。</div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>
            <div class="app-panel app-form-panel">
                <form method="post" action="<%=request.getContextPath()%>/login">
                    <div class="form-group">
                        <label for="username">Username</label>
                        <input id="username" name="username" class="form-control" value="${username}" maxlength="32" required>
                    </div>
                    <div class="form-group">
                        <label for="password">Password</label>
                        <input id="password" type="password" name="password" class="form-control" required>
                    </div>
                    <button type="submit" class="btn btn-primary">Login</button>
                    <a class="btn btn-link" href="<%=request.getContextPath()%>/register">没有账号？去注册</a>
                </form>
            </div>
        </main>
    </div>
</div>
</body>
</html>
