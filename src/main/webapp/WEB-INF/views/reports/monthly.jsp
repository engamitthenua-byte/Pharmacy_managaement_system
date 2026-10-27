<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Monthly Sales Report" />
<c:set var="pageNav" value="reports" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar no-print">
    <form action="${pageContext.request.contextPath}/reports" method="get" style="display: flex; gap: 0.75rem; align-items: center;">
        <input type="hidden" name="action" value="monthly">
        <label style="font-size: 0.88rem; font-weight: 600;">Select Year:</label>
        <input type="number" name="year" class="form-control" value="${currentYear}" min="2020" max="2035" style="width: 110px;">
        <button type="submit" class="btn btn-primary btn-sm">Load Year</button>
    </form>

    <div style="display: flex; gap: 0.5rem;">
        <button type="button" class="btn btn-outline btn-sm" onclick="window.print()">🖨️ Print Report</button>
        <a href="${pageContext.request.contextPath}/reports" class="btn btn-outline btn-sm">&larr; Reports Hub</a>
    </div>
</div>

<div class="card">
    <div class="card-header">
        <h2 class="card-title">Monthly Revenue Breakdown for ${currentYear}</h2>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Month</th>
                    <th style="text-align: right;">Total Invoices</th>
                    <th style="text-align: right;">Monthly Revenue</th>
                </tr>
            </thead>
            <tbody>
                <c:set var="sumInvoices" value="0" />
                <c:set var="sumRevenue" value="0" />

                <c:choose>
                    <c:when test="${not empty reportData}">
                        <c:forEach var="row" items="${reportData}">
                            <c:set var="sumInvoices" value="${sumInvoices + row.total_invoices}" />
                            <c:set var="sumRevenue" value="${sumRevenue + row.total_revenue}" />
                            <tr>
                                <td><strong><c:out value="${row.month_name}"/></strong></td>
                                <td style="text-align: right;">${row.total_invoices}</td>
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
                            <td colspan="3" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                No sales recorded for year ${currentYear}.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
            <c:if test="${not empty reportData}">
                <tfoot style="background: #f8fafc; font-weight: 700; border-top: 2px solid var(--border);">
                    <tr>
                        <td>ANNUAL TOTAL</td>
                        <td style="text-align: right;">${sumInvoices}</td>
                        <td style="text-align: right; color: var(--primary); font-size: 1.05rem;">
                            ₹<fmt:formatNumber value="${sumRevenue}" pattern="#,##0.00"/>
                        </td>
                    </tr>
                </tfoot>
            </c:if>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
