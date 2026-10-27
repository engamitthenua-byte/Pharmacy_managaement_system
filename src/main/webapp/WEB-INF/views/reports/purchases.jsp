<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Purchases Report" />
<c:set var="pageNav" value="reports" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar no-print">
    <form action="${pageContext.request.contextPath}/reports" method="get" style="display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap;">
        <input type="hidden" name="action" value="purchases">
        <label style="font-size: 0.88rem; font-weight: 600;">From:</label>
        <input type="date" name="from" class="form-control" value="${fromDate}" style="width: auto;">
        <label style="font-size: 0.88rem; font-weight: 600;">To:</label>
        <input type="date" name="to" class="form-control" value="${toDate}" style="width: auto;">
        <button type="submit" class="btn btn-primary btn-sm">Filter</button>
    </form>

    <div style="display: flex; gap: 0.5rem;">
        <button type="button" class="btn btn-outline btn-sm" onclick="window.print()">🖨️ Print Report</button>
        <a href="${pageContext.request.contextPath}/reports" class="btn btn-outline btn-sm">&larr; Reports Hub</a>
    </div>
</div>

<div class="card">
    <div class="card-header">
        <h2 class="card-title">Procurement & Purchases Report</h2>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Date</th>
                    <th>Invoice #</th>
                    <th>Supplier</th>
                    <th style="text-align: right;">Items Count</th>
                    <th style="text-align: right;">Purchase Cost</th>
                </tr>
            </thead>
            <tbody>
                <c:set var="totalSpend" value="0" />
                <c:choose>
                    <c:when test="${not empty reportData}">
                        <c:forEach var="row" items="${reportData}">
                            <c:set var="totalSpend" value="${totalSpend + row.total_amount}" />
                            <tr>
                                <td>${row.purchase_date}</td>
                                <td><strong><c:out value="${row.invoice_number}"/></strong></td>
                                <td><c:out value="${row.supplier_name}"/></td>
                                <td style="text-align: right;">${row.item_count}</td>
                                <td style="text-align: right;">
                                    <strong style="color: var(--primary);">₹<fmt:formatNumber value="${row.total_amount}" pattern="#,##0.00"/></strong>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="5" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                No purchases found for the selected period.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
            <c:if test="${not empty reportData}">
                <tfoot style="background: #f8fafc; font-weight: 700; border-top: 2px solid var(--border);">
                    <tr>
                        <td colspan="4">TOTAL PROCUREMENT SPEND</td>
                        <td style="text-align: right; color: var(--primary); font-size: 1.05rem;">
                            ₹<fmt:formatNumber value="${totalSpend}" pattern="#,##0.00"/>
                        </td>
                    </tr>
                </tfoot>
            </c:if>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
