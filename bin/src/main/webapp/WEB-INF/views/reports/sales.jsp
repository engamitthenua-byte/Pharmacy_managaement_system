<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Daily Sales Report" />
<c:set var="pageNav" value="reports" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar no-print">
    <form action="${pageContext.request.contextPath}/reports" method="get" style="display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap;">
        <input type="hidden" name="action" value="sales">
        <label style="font-size: 0.88rem; font-weight: 600;">From:</label>
        <input type="date" name="from" class="form-control" value="${fromDate}" style="width: auto;">
        <label style="font-size: 0.88rem; font-weight: 600;">To:</label>
        <input type="date" name="to" class="form-control" value="${toDate}" style="width: auto;">
        <button type="submit" class="btn btn-primary btn-sm">Filter Report</button>
    </form>

    <div style="display: flex; gap: 0.5rem;">
        <button type="button" class="btn btn-outline btn-sm" onclick="window.print()">🖨️ Print Report</button>
        <a href="${pageContext.request.contextPath}/reports" class="btn btn-outline btn-sm">&larr; Reports Hub</a>
    </div>
</div>

<div class="card">
    <div class="card-header">
        <div>
            <h2 class="card-title">Sales Revenue Report</h2>
            <p style="font-size: 0.85rem; color: var(--text-muted);">
                Showing daily aggregated performance from <strong>${fromDate}</strong> to <strong>${toDate}</strong>
            </p>
        </div>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Date</th>
                    <th style="text-align: right;">Total Invoices</th>
                    <th style="text-align: right;">Discounts Given</th>
                    <th style="text-align: right;">Gross Revenue</th>
                </tr>
            </thead>
            <tbody>
                <c:set var="sumInvoices" value="0" />
                <c:set var="sumRevenue" value="0" />
                <c:set var="sumDiscount" value="0" />

                <c:choose>
                    <c:when test="${not empty reportData}">
                        <c:forEach var="row" items="${reportData}">
                            <c:set var="sumInvoices" value="${sumInvoices + row.total_invoices}" />
                            <c:set var="sumRevenue" value="${sumRevenue + row.total_revenue}" />
                            <c:set var="sumDiscount" value="${sumDiscount + (row.total_discount != null ? row.total_discount : 0)}" />
                            <tr>
                                <td><strong>${row.sale_date}</strong></td>
                                <td style="text-align: right;">${row.total_invoices}</td>
                                <td style="text-align: right; color: #b45309;">₹<fmt:formatNumber value="${row.total_discount}" pattern="#,##0.00"/></td>
                                <td style="text-align: right;">
                                    <strong style="color: var(--primary);">
                                        ₹<fmt:formatNumber value="${row.total_revenue}" pattern="#,##0.00"/>
                                    </strong>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="4" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                No sales data found for the selected date range.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
            <c:if test="${not empty reportData}">
                <tfoot style="background: #f8fafc; font-weight: 700; border-top: 2px solid var(--border);">
                    <tr>
                        <td>TOTALS</td>
                        <td style="text-align: right;">${sumInvoices}</td>
                        <td style="text-align: right; color: #b45309;">₹<fmt:formatNumber value="${sumDiscount}" pattern="#,##0.00"/></td>
                        <td style="text-align: right; color: var(--primary); font-size: 1.05rem;">₹<fmt:formatNumber value="${sumRevenue}" pattern="#,##0.00"/></td>
                    </tr>
                </tfoot>
            </c:if>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
