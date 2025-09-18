<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>Error - User Management System</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/styles.css">
    </head>
    <body>
        <div class="app-container">
            <header class="app-header">
                <div class="container">
                    <div class="navbar">
                        <div class="app-title">User Management System</div>
                    </div>
                </div>
            </header>

            <main class="container main-content">
                <div class="card error-card">
                    <div class="error-icon">⚠️</div>
                    <h2>Error</h2>
                    <div class="error-message">
                        <c:choose>
                            <c:when test="${not empty error}">
                                <p>${error}</p>
                            </c:when>
                            <c:when test="${not empty pageContext.exception}">
                                <p>An error occurred: ${pageContext.exception.message}</p>
                            </c:when>
                            <c:otherwise>
                                <p>An unknown error occurred.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <a href="${pageContext.request.contextPath}/login" class="btn btn-primary">Go to Login</a>
                </div>
            </main>

            <footer class="app-footer">
                <div class="container">
                    <p>&copy; 2025 User Management System</p>
                </div>
            </footer>
        </div>
    </body>
</html>
