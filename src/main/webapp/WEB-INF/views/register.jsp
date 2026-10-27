<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Account — Pharmacy Management System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="icon" href="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><text y='.9em' font-size='90'>💊</text></svg>">
</head>
<body class="login-body">

<div class="login-card" style="max-width: 480px;">
    <div class="login-brand-header">
        <div class="logo-badge">💊</div>
        <h1>Create Admin Account</h1>
        <p>Register as a new system administrator</p>
    </div>

    <%-- Error message --%>
    <c:if test="${not empty errorMsg}">
        <div class="alert alert-danger">
            <span>⚠️</span>
            <div><c:out value="${errorMsg}"/></div>
        </div>
    </c:if>

    <%-- Validation errors list --%>
    <c:if test="${not empty errors}">
        <div class="alert alert-danger">
            <span>⚠️</span>
            <div>
                <strong>Please correct the following:</strong>
                <ul style="margin-top: 0.35rem; margin-left: 1.25rem;">
                    <c:forEach var="err" items="${errors}">
                        <li><c:out value="${err}"/></li>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/register" method="post" autocomplete="off">
        <div class="form-group">
            <label class="form-label" for="fullName">Full Name <span class="required">*</span></label>
            <input type="text" id="fullName" name="fullName" class="form-control" 
                   placeholder="e.g. John Pharmacist" value="<c:out value='${fullName}'/>" required autofocus>
        </div>

        <div class="form-group">
            <label class="form-label" for="username">Username <span class="required">*</span></label>
            <input type="text" id="username" name="username" class="form-control" 
                   placeholder="e.g. pharmacist_john" value="<c:out value='${username}'/>" required>
            <small style="color: var(--text-muted); font-size: 0.75rem;">3-30 characters (letters, numbers, underscores only).</small>
        </div>

        <div class="form-grid" style="grid-template-columns: 1fr 1fr; gap: 0.75rem;">
            <div class="form-group">
                <label class="form-label" for="email">Email</label>
                <input type="email" id="email" name="email" class="form-control" 
                       placeholder="you@email.com" value="<c:out value='${email}'/>">
            </div>

            <div class="form-group">
                <label class="form-label" for="phone">Phone</label>
                <input type="text" id="phone" name="phone" class="form-control" 
                       placeholder="e.g. 9876543210" value="<c:out value='${phone}'/>">
            </div>
        </div>

        <div class="form-group">
            <label class="form-label" for="password">Password <span class="required">*</span></label>
            <input type="password" id="password" name="password" class="form-control" 
                   placeholder="Minimum 6 characters" required>
        </div>

        <div class="form-group" style="margin-bottom: 1.75rem;">
            <label class="form-label" for="confirmPassword">Confirm Password <span class="required">*</span></label>
            <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" 
                   placeholder="Re-enter password" required>
        </div>

        <button type="submit" class="btn btn-primary btn-lg" style="width: 100%;">
            Create Account & Register
        </button>
    </form>

    <div style="margin-top: 1.5rem; text-align: center; font-size: 0.9rem;">
        <span style="color: var(--text-muted);">Already have an account?</span>
        <a href="${pageContext.request.contextPath}/login" style="font-weight: 600; margin-left: 0.35rem;">
            Sign In here &rarr;
        </a>
    </div>
</div>

</body>
</html>
