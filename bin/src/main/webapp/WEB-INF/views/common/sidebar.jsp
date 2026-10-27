<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<aside class="app-sidebar">
    <div class="sidebar-brand">
        <div class="brand-icon">💊</div>
        <span>PharmaCare</span>
    </div>

    <ul class="sidebar-nav">
        <li class="nav-section-title">Main</li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-link ${pageNav == 'dashboard' ? 'active' : ''}">
                <span class="nav-icon">📊</span>
                <span>Dashboard</span>
            </a>
        </li>

        <li class="nav-section-title">Operations</li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/sales?action=pos" class="nav-link ${pageNav == 'pos' ? 'active' : ''}">
                <span class="nav-icon">⚡</span>
                <span>Billing (POS)</span>
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/sales" class="nav-link ${pageNav == 'sales' ? 'active' : ''}">
                <span class="nav-icon">🧾</span>
                <span>Sales Invoices</span>
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/purchases" class="nav-link ${pageNav == 'purchases' ? 'active' : ''}">
                <span class="nav-icon">📥</span>
                <span>Purchases</span>
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/inventory" class="nav-link ${pageNav == 'inventory' ? 'active' : ''}">
                <span class="nav-icon">📦</span>
                <span>Inventory & Stock</span>
            </a>
        </li>

        <li class="nav-section-title">Catalogue & Partners</li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/medicines" class="nav-link ${pageNav == 'medicines' ? 'active' : ''}">
                <span class="nav-icon">💊</span>
                <span>Medicines</span>
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/categories" class="nav-link ${pageNav == 'categories' ? 'active' : ''}">
                <span class="nav-icon">🏷️</span>
                <span>Categories</span>
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/suppliers" class="nav-link ${pageNav == 'suppliers' ? 'active' : ''}">
                <span class="nav-icon">🏢</span>
                <span>Suppliers</span>
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/customers" class="nav-link ${pageNav == 'customers' ? 'active' : ''}">
                <span class="nav-icon">👥</span>
                <span>Customers</span>
            </a>
        </li>

        <li class="nav-section-title">Analytics & Account</li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/reports" class="nav-link ${pageNav == 'reports' ? 'active' : ''}">
                <span class="nav-icon">📈</span>
                <span>Reports</span>
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/profile" class="nav-link ${pageNav == 'profile' ? 'active' : ''}">
                <span class="nav-icon">⚙️</span>
                <span>Settings</span>
            </a>
        </li>
    </ul>

    <div class="sidebar-footer">
        <div class="user-badge">
            <div class="user-avatar">
                <c:choose>
                    <c:when test="${not empty sessionScope.admin.fullName}">
                        ${sessionScope.admin.fullName.substring(0, 1).toUpperCase()}
                    </c:when>
                    <c:otherwise>A</c:otherwise>
                </c:choose>
            </div>
            <div class="user-meta">
                <div class="user-name">
                    <c:out value="${not empty sessionScope.admin.fullName ? sessionScope.admin.fullName : 'Admin'}" />
                </div>
                <div class="user-role">Administrator</div>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm" title="Log Out" style="color: #cbd5e1; border-color: #334155;">
            🚪
        </a>
    </div>
</aside>
<main class="app-main">
