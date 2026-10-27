<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Reports & Business Analytics" />
<c:set var="pageNav" value="reports" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

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
            <div class="stat-value">${summary.totalSales}</div>
        </div>
        <div class="stat-icon info">🧾</div>
    </div>
    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Total Medicines</div>
            <div class="stat-value">${summary.totalMedicines}</div>
        </div>
        <div class="stat-icon primary">💊</div>
    </div>
</div>

<div class="card">
    <div class="card-header">
        <h2 class="card-title">Business Reports & Financial Statements</h2>
    </div>
    <div class="card-body">
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.5rem;">
            
            <div style="background: var(--bg-body); padding: 1.5rem; border-radius: var(--radius-md); border: 1px solid var(--border);">
                <div style="font-size: 1.75rem; margin-bottom: 0.5rem;">📊</div>
                <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.35rem;">Daily / Date-Range Sales</h3>
                <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1.25rem;">
                    Daily turnover, invoices count, and discounts applied within any selected date range.
                </p>
                <a href="${pageContext.request.contextPath}/reports?action=sales" class="btn btn-primary btn-sm">
                    View Sales Report &rarr;
                </a>
            </div>

            <div style="background: var(--bg-body); padding: 1.5rem; border-radius: var(--radius-md); border: 1px solid var(--border);">
                <div style="font-size: 1.75rem; margin-bottom: 0.5rem;">📅</div>
                <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.35rem;">Monthly Revenue Breakdown</h3>
                <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1.25rem;">
                    Month-by-month sales revenue comparison and invoice volumes across the year.
                </p>
                <a href="${pageContext.request.contextPath}/reports?action=monthly" class="btn btn-outline btn-sm">
                    View Monthly Report &rarr;
                </a>
            </div>

            <div style="background: var(--bg-body); padding: 1.5rem; border-radius: var(--radius-md); border: 1px solid var(--border);">
                <div style="font-size: 1.75rem; margin-bottom: 0.5rem;">📥</div>
                <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.35rem;">Stock Purchase Report</h3>
                <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1.25rem;">
                    Purchase expenses, distributor invoices, and item received quantities.
                </p>
                <a href="${pageContext.request.contextPath}/reports?action=purchases" class="btn btn-outline btn-sm">
                    View Purchases Report &rarr;
                </a>
            </div>

            <div style="background: var(--bg-body); padding: 1.5rem; border-radius: var(--radius-md); border: 1px solid var(--border);">
                <div style="font-size: 1.75rem; margin-bottom: 0.5rem;">📦</div>
                <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.35rem;">Inventory Valuation</h3>
                <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1.25rem;">
                    Comprehensive stock availability and medicine unit analysis across categories.
                </p>
                <a href="${pageContext.request.contextPath}/reports?action=inventory" class="btn btn-outline btn-sm">
                    View Inventory Report &rarr;
                </a>
            </div>

            <div style="background: var(--bg-body); padding: 1.5rem; border-radius: var(--radius-md); border: 1px solid var(--border);">
                <div style="font-size: 1.75rem; margin-bottom: 0.5rem;">🏆</div>
                <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.35rem;">Top Selling Medicines</h3>
                <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1.25rem;">
                    Identify top performers by quantity sold and revenue contribution.
                </p>
                <a href="${pageContext.request.contextPath}/reports?action=bestsellers" class="btn btn-outline btn-sm">
                    View Best Sellers &rarr;
                </a>
            </div>

            <div style="background: var(--bg-body); padding: 1.5rem; border-radius: var(--radius-md); border: 1px solid var(--border);">
                <div style="font-size: 1.75rem; margin-bottom: 0.5rem;">👥</div>
                <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.35rem;">Customer Spend History</h3>
                <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1.25rem;">
                    Customer order frequency, purchase histories, and revenue per patron.
                </p>
                <a href="${pageContext.request.contextPath}/reports?action=customers" class="btn btn-outline btn-sm">
                    View Customer Analytics &rarr;
                </a>
            </div>

            <div style="background: var(--bg-body); padding: 1.5rem; border-radius: var(--radius-md); border: 1px solid var(--border);">
                <div style="font-size: 1.75rem; margin-bottom: 0.5rem;">🏢</div>
                <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.35rem;">Supplier Procurement History</h3>
                <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1.25rem;">
                    Total procurement spending breakdown by distributor and supplier.
                </p>
                <a href="${pageContext.request.contextPath}/reports?action=suppliers" class="btn btn-outline btn-sm">
                    View Supplier History &rarr;
                </a>
            </div>

        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
