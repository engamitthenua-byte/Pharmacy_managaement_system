<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Pharmacy Management Dashboard" />
<c:set var="pageNav" value="dashboard" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<%-- Critical Stock Alerts Banner --%>
<c:if test="${summary.expiredCount > 0}">
    <div class="alert alert-danger">
        <span style="font-size: 1.25rem;">⚠️</span>
        <div>
            <strong>Attention:</strong> There are <strong>${summary.expiredCount}</strong> expired medicine batches in stock. 
            <a href="${pageContext.request.contextPath}/medicines?action=expired" style="color: #991b1b; text-decoration: underline; font-weight: 600; margin-left: 0.5rem;">
                Review and write off now &rarr;
            </a>
        </div>
    </div>
</c:if>

<c:if test="${summary.lowStockCount > 0}">
    <div class="alert alert-warning">
        <span style="font-size: 1.25rem;">📦</span>
        <div>
            <strong>Notice:</strong> <strong>${summary.lowStockCount}</strong> medicines have reached or fallen below minimum stock thresholds.
            <a href="${pageContext.request.contextPath}/medicines?action=lowstock" style="color: #92400e; text-decoration: underline; font-weight: 600; margin-left: 0.5rem;">
                View low-stock items &rarr;
            </a>
        </div>
    </div>
</c:if>

<%-- Revenue & Operations Overview Cards --%>
<div class="stats-grid">
    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Today's Revenue</div>
            <div class="stat-value">₹<fmt:formatNumber value="${summary.todayRevenue != null ? summary.todayRevenue : 0}" pattern="#,##0.00"/></div>
        </div>
        <div class="stat-icon success">💰</div>
    </div>

    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">This Month's Revenue</div>
            <div class="stat-value">₹<fmt:formatNumber value="${summary.monthRevenue != null ? summary.monthRevenue : 0}" pattern="#,##0.00"/></div>
        </div>
        <div class="stat-icon primary">📈</div>
    </div>

    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Total Invoices</div>
            <div class="stat-value">${summary.totalSales != null ? summary.totalSales : 0}</div>
        </div>
        <div class="stat-icon info">🧾</div>
    </div>

    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Total Medicines</div>
            <div class="stat-value">${summary.totalMedicines != null ? summary.totalMedicines : 0}</div>
        </div>
        <div class="stat-icon primary">💊</div>
    </div>
</div>

<%-- Inventory Risk Metrics --%>
<div class="stats-grid">
    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Low Stock Items</div>
            <div class="stat-value" style="color: #d97706;">${summary.lowStockCount != null ? summary.lowStockCount : 0}</div>
        </div>
        <div class="stat-icon warning">⚠️</div>
    </div>

    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Expired Batches</div>
            <div class="stat-value" style="color: #ef4444;">${summary.expiredCount != null ? summary.expiredCount : 0}</div>
        </div>
        <div class="stat-icon danger">⛔</div>
    </div>

    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Near Expiry (30d)</div>
            <div class="stat-value" style="color: #f59e0b;">${summary.nearExpiryCount != null ? summary.nearExpiryCount : 0}</div>
        </div>
        <div class="stat-icon warning">⏳</div>
    </div>

    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Active Customers</div>
            <div class="stat-value">${summary.totalCustomers != null ? summary.totalCustomers : 0}</div>
        </div>
        <div class="stat-icon info">👥</div>
    </div>
</div>

<%-- Quick Operational Shortcuts --%>
<div class="card">
    <div class="card-header">
        <h2 class="card-title">Quick Actions & Shortcuts</h2>
    </div>
    <div class="card-body">
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem;">
            <a href="${pageContext.request.contextPath}/sales?action=pos" class="btn btn-primary btn-lg" style="justify-content: flex-start; gap: 0.75rem;">
                <span style="font-size: 1.25rem;">⚡</span>
                <span>New POS Bill</span>
            </a>
            <a href="${pageContext.request.contextPath}/purchases?action=new" class="btn btn-outline btn-lg" style="justify-content: flex-start; gap: 0.75rem;">
                <span style="font-size: 1.25rem;">📥</span>
                <span>Add Purchase</span>
            </a>
            <a href="${pageContext.request.contextPath}/medicines?action=add" class="btn btn-outline btn-lg" style="justify-content: flex-start; gap: 0.75rem;">
                <span style="font-size: 1.25rem;">💊</span>
                <span>Add Medicine</span>
            </a>
            <a href="${pageContext.request.contextPath}/inventory?action=adjust" class="btn btn-outline btn-lg" style="justify-content: flex-start; gap: 0.75rem;">
                <span style="font-size: 1.25rem;">⚖️</span>
                <span>Stock Adjust</span>
            </a>
            <a href="${pageContext.request.contextPath}/reports?action=sales" class="btn btn-outline btn-lg" style="justify-content: flex-start; gap: 0.75rem;">
                <span style="font-size: 1.25rem;">📊</span>
                <span>Sales Report</span>
            </a>
            <a href="${pageContext.request.contextPath}/customers?action=add" class="btn btn-outline btn-lg" style="justify-content: flex-start; gap: 0.75rem;">
                <span style="font-size: 1.25rem;">👤</span>
                <span>Add Customer</span>
            </a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
