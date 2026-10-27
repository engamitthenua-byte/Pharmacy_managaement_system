<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Sales Invoices & Billing" />
<c:set var="pageNav" value="sales" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar">
    <form action="${pageContext.request.contextPath}/sales" method="get" class="search-form">
        <input type="hidden" name="action" value="search">
        <input type="text" name="q" class="form-control" placeholder="Search by invoice # or customer..." value="<c:out value='${keyword}'/>">
        <button type="submit" class="btn btn-outline">🔍</button>
        <c:if test="${not empty keyword}">
            <a href="${pageContext.request.contextPath}/sales" class="btn btn-outline" title="Clear search">✕</a>
        </c:if>
    </form>

    <a href="${pageContext.request.contextPath}/sales?action=pos" class="btn btn-primary">
        ⚡ New POS Bill
    </a>
</div>

<div class="stats-grid" style="margin-bottom: 1.5rem;">
    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Today's Sales Revenue</div>
            <div class="stat-value" style="color: var(--success);">
                ₹<fmt:formatNumber value="${todayRevenue != null ? todayRevenue : 0}" pattern="#,##0.00"/>
            </div>
        </div>
        <div class="stat-icon success">💰</div>
    </div>
    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Total Lifetime Revenue</div>
            <div class="stat-value" style="color: var(--primary);">
                ₹<fmt:formatNumber value="${totalRevenue != null ? totalRevenue : 0}" pattern="#,##0.00"/>
            </div>
        </div>
        <div class="stat-icon primary">📈</div>
    </div>
    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Total Invoices Generated</div>
            <div class="stat-value">${totalCount != null ? totalCount : sales.size()}</div>
        </div>
        <div class="stat-icon info">🧾</div>
    </div>
</div>

<div class="card">
    <div class="card-header">
        <h2 class="card-title">All Sales Invoices</h2>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Invoice #</th>
                    <th>Date</th>
                    <th>Customer Name</th>
                    <th style="text-align: right;">Subtotal</th>
                    <th style="text-align: right;">Discount</th>
                    <th style="text-align: right;">Tax</th>
                    <th style="text-align: right;">Net Amount</th>
                    <th style="text-align: right;">Action</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty sales}">
                        <c:forEach var="s" items="${sales}">
                            <tr>
                                <td><strong><a href="${pageContext.request.contextPath}/sales?action=invoice&id=${s.id}"><c:out value="${s.invoiceNumber}"/></a></strong></td>
                                <td>${s.saleDate}</td>
                                <td>
                                    <c:out value="${s.displayCustomerName}"/>
                                    <c:if test="${empty s.customerId}">
                                        <span class="badge badge-neutral" style="font-size: 0.7rem; margin-left: 0.35rem;">Walk-in</span>
                                    </c:if>
                                </td>
                                <td style="text-align: right;">₹<fmt:formatNumber value="${s.subtotal}" pattern="#,##0.00"/></td>
                                <td style="text-align: right; color: #b45309;">-₹<fmt:formatNumber value="${s.discount}" pattern="#,##0.00"/></td>
                                <td style="text-align: right;">+₹<fmt:formatNumber value="${s.taxAmount}" pattern="#,##0.00"/></td>
                                <td style="text-align: right;">
                                    <strong style="color: var(--success); font-size: 0.95rem;">
                                        ₹<fmt:formatNumber value="${s.totalAmount}" pattern="#,##0.00"/>
                                    </strong>
                                </td>
                                <td style="text-align: right;">
                                    <a href="${pageContext.request.contextPath}/sales?action=invoice&id=${s.id}" class="btn btn-outline btn-sm">
                                        🧾 View Bill
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="8" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                No sales invoices recorded yet. Click "⚡ New POS Bill" to start billing.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>

    <%-- Pagination --%>
    <c:if test="${totalPages > 1}">
        <div class="pagination">
            <div>${pageInfo}</div>
            <div class="page-numbers">
                <c:forEach var="p" begin="1" end="${totalPages}">
                    <a href="${pageContext.request.contextPath}/sales?page=${p}" 
                       class="page-link ${currentPage == p ? 'active' : ''}">${p}</a>
                </c:forEach>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
