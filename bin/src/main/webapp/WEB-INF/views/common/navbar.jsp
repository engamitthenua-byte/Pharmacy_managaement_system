<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<header class="app-topbar">
    <div class="topbar-title">
        <c:out value="${not empty pageTitle ? pageTitle : 'Pharmacy Dashboard'}" />
    </div>

    <div class="topbar-actions">
        <a href="${pageContext.request.contextPath}/sales?action=pos" class="btn btn-primary btn-sm">
            ⚡ Quick POS Sale
        </a>
        <a href="${pageContext.request.contextPath}/purchases?action=new" class="btn btn-outline btn-sm">
            📥 Add Purchase
        </a>
    </div>
</header>
<div class="page-content">
