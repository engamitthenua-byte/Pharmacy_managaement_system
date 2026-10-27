<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Page Not Found — PharmaCare</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body style="background: var(--bg-body); display: flex; align-items: center; justify-content: center; min-height: 100vh; text-align: center; padding: 2rem;">
    <div class="card" style="max-width: 500px; padding: 3rem 2rem;">
        <div style="font-size: 4rem; line-height: 1; margin-bottom: 1rem;">🔍</div>
        <h1 style="font-size: 2rem; font-weight: 800; color: var(--text-main); margin-bottom: 0.5rem;">404 — Page Not Found</h1>
        <p style="color: var(--text-muted); margin-bottom: 2rem;">The page or resource you requested could not be located on the server.</p>
        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary btn-lg">Return to Dashboard</a>
    </div>
</body>
</html>
