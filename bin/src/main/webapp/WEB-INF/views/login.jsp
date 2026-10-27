<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login — Pharmacy Management System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="icon" href="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><text y='.9em' font-size='90'>💊</text></svg>">
</head>
<body class="login-body">

<div class="login-card">
    <div class="login-brand-header">
        <div class="logo-badge">💊</div>
        <h1>PharmaCare</h1>
        <p>Pharmacy Management System</p>
    </div>

    <%-- Flash Alerts --%>
    <c:if test="${not empty error}">
        <div class="alert alert-danger">
            <span>⚠️</span>
            <div><c:out value="${error}"/></div>
        </div>
    </c:if>

    <c:if test="${not empty param.logout}">
        <div class="alert alert-success">
            <span>✓</span>
            <div>You have been safely logged out.</div>
        </div>
    </c:if>

    <c:if test="${not empty param.expired}">
        <div class="alert alert-warning">
            <span>⏰</span>
            <div>Your session has expired. Please sign in again.</div>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/login" method="post" autocomplete="off">
        <div class="form-group">
            <label class="form-label" for="username">Username</label>
            <input type="text" id="username" name="username" class="form-control" 
                   placeholder="Enter your username" value="${username}" required autofocus>
        </div>

        <div class="form-group" style="margin-bottom: 1.75rem;">
            <label class="form-label" for="password">Password</label>
            <input type="password" id="password" name="password" class="form-control" 
                   placeholder="Enter your password" required>
        </div>

        <button type="submit" class="btn btn-primary btn-lg" style="width: 100%;">
            Sign In to Dashboard
        </button>
    </form>

    <div style="margin-top: 1.75rem; padding-top: 1.25rem; border-top: 1px solid var(--border); text-align: center;">
        <span class="badge badge-info" style="font-size: 0.8rem; padding: 0.4rem 0.75rem;">
            Demo Credentials: <strong>admin</strong> / <strong>Admin@1234</strong>
        </span>
    </div>
</div>

</body>
</html>
