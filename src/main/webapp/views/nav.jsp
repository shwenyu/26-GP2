<%--
  Created by IntelliJ IDEA.
  User: hello
  Date: 2019-12-3
  Time: 17:04
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<nav class="col-12 app-top-menu">
    <div class="app-top-nav d-lg-flex justify-content-between align-items-center px-3 py-2">

        <div class="d-flex align-items-center">
            <a class="navbar-brand mr-4 site-title-art" href="<%=request.getContextPath()%>/">Precision Medicine</a>

            <ul class="nav app-nav-primary">
                <li class="nav-item">
                    <a class='nav-link ${param.active == "dashboard" ? "active" : ""}' href="<%=request.getContextPath()%>/">
                        <span data-feather="home"></span> Dashboard
                    </a>
                </li>
                <li class="nav-item">
                    <a class='nav-link ${param.active == "matching_index" ? "active" : ""}' href="<%=request.getContextPath()%>/matchingIndex">
                        <span data-feather="activity"></span> Matching
                    </a>
                </li>
                <li class="nav-item">
                    <a class='nav-link ${param.active == "samples" ? "active" : ""}' href="<%=request.getContextPath()%>/samples">
                        <span data-feather="database"></span> Samples
                    </a>
                </li>
                <li class="nav-item dropdown">
                    <a class='nav-link dropdown-toggle ${(param.active == "drugs" or param.active == "drug_labels" or param.active == "dosing_guideline") ? "active" : ""}'
                       href="#" id="kbMenu" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        <span data-feather="book-open"></span> Knowledge Base
                    </a>
                    <div class="dropdown-menu" aria-labelledby="kbMenu">
                        <a class="dropdown-item" href="<%=request.getContextPath()%>/kb/drugs">Drugs</a>
                        <a class="dropdown-item" href="<%=request.getContextPath()%>/kb/guidelines">Guidelines</a>
                        <a class="dropdown-item" href="<%=request.getContextPath()%>/drugLabels">Drug Labels (Legacy)</a>
                    </div>
                </li>
            </ul>
        </div>

        <ul class="nav app-nav-account align-items-center">
            <c:choose>
                <c:when test="${not empty sessionScope.currentUser}">
                    <li class="nav-item px-2 mr-2">
                        <div class="app-user-pill">
                            <span data-feather="user" style="width: 14px; height: 14px;"></span>
                                ${sessionScope.currentUser.username}
                        </div>
                    </li>
                    <li class="nav-item">
                        <a class='nav-link ${param.active == "profile" ? "active" : ""}' href="<%=request.getContextPath()%>/profile">
                            Profile
                        </a>
                    </li>
                    <c:if test="${sessionScope.currentUser.username eq 'root'}">
                        <li class="nav-item">
                            <a class='nav-link ${param.active == "admin_debug" ? "active" : ""}' href="<%=request.getContextPath()%>/admin/debug">
                                后台数据检查
                            </a>
                        </li>
                    </c:if>
                    <li class="nav-item">
                        <a class='nav-link text-muted' href="<%=request.getContextPath()%>/logout">
                            <span data-feather="log-out"></span> Logout
                        </a>
                    </li>
                </c:when>
                <c:otherwise>
                    <li class="nav-item">
                        <a class='nav-link ${param.active == "login" ? "active" : ""}' href="<%=request.getContextPath()%>/login">
                            <span data-feather="log-in"></span> Login
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class='btn btn-primary btn-sm ml-2' href="<%=request.getContextPath()%>/register">
                            Register
                        </a>
                    </li>
                </c:otherwise>
            </c:choose>
        </ul>
    </div>
</nav>