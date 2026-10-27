<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Server Error — PharmaCare</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body style="background: var(--bg-body); display: flex; align-items: center; justify-content: center; min-height: 100vh; text-align: center; padding: 2rem;">
    <div class="card" style="max-width: 550px; padding: 3rem 2rem;">
        <div style="font-size: 4rem; line-height: 1; margin-bottom: 1rem;">⚠️</div>
        <h1 style="font-size: 2rem; font-weight: 800; color: var(--text-main); margin-bottom: 0.5rem;">500 — Internal Server Error</h1>
        <p style="color: var(--text-muted); margin-bottom: 1.5rem;">An unexpected server condition occurred while processing your request.</p>
        
        <% if (exception != null) { %>
            <div style="background: #fef2f2; border: 1px solid #fecaca; color: #991b1b; padding: 0.75rem 1rem; border-radius: var(--radius-sm); text-align: left; font-family: monospace; font-size: 0.82rem; margin-bottom: 1.75rem; word-break: break-all;">
                <%= exception.getMessage() != null ? exception.getMessage() : exception.toString() %>
            </div>
        <% } %>

        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary btn-lg">Return to Dashboard</a>
    </div>
</body>
</html>
