<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Customer Spend History" />
<c:set var="pageNav" value="reports" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar no-print">
    <div style="font-size: 0.95rem; font-weight: 600; color: var(--text-muted);">
        Ranked by Total Spend Volume
    </div>
    <div style="display: flex; gap: 0.5rem;">
        <button type="button" class="btn btn-outline btn-sm" onclick="window.print()">🖨️ Print Report</button>
        <a href="${pageContext.request.contextPath}/reports" class="btn btn-outline btn-sm">&larr; Reports Hub</a>
    </div>
</div>

<div class="card">
    <div class="card-header">
        <h2 class="card-title">Customer Lifetime Purchase History</h2>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Customer Name</th>
                    <th style="text-align: right;">Total Bills / Invoices</th>
                    <th style="text-align: right;">Total Spend</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty reportData}">
                        <c:forEach var="row" items="${reportData}" varStatus="status">
                            <tr>
                                <td>${status.index + 1}</td>
                                <td><strong><c:out value="${row.customer_name}"/></strong></td>
                                <td style="text-align: right;">${row.total_invoices} bills</td>
                                <td style="text-align: right;">
                                    <strong style="color: var(--primary);">₹<fmt:formatNumber value="${row.total_spent}" pattern="#,##0.00"/></strong>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="4" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                No customer purchase history recorded yet.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
