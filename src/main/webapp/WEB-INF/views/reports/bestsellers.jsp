<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Best-Selling Medicines" />
<c:set var="pageNav" value="reports" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar no-print">
    <div style="font-size: 0.95rem; font-weight: 600; color: var(--text-muted);">
        Ranked by Total Units Sold
    </div>
    <div style="display: flex; gap: 0.5rem;">
        <button type="button" class="btn btn-outline btn-sm" onclick="window.print()">🖨️ Print Report</button>
        <a href="${pageContext.request.contextPath}/reports" class="btn btn-outline btn-sm">&larr; Reports Hub</a>
    </div>
</div>

<div class="card">
    <div class="card-header">
        <h2 class="card-title">Top 20 Best-Selling Medicines</h2>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th style="width: 50px;">Rank</th>
                    <th>Medicine Name</th>
                    <th>Therapeutic Category</th>
                    <th style="text-align: right;">Total Units Dispensed</th>
                    <th style="text-align: right;">Revenue Generated</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty reportData}">
                        <c:forEach var="row" items="${reportData}" varStatus="status">
                            <tr>
                                <td>
                                    <c:choose>
                                        <c:when test="${status.index == 0}"><span class="badge badge-warning" style="font-size: 0.85rem;">🥇 #1</span></c:when>
                                        <c:when test="${status.index == 1}"><span class="badge badge-neutral" style="font-size: 0.85rem;">🥈 #2</span></c:when>
                                        <c:when test="${status.index == 2}"><span class="badge badge-neutral" style="font-size: 0.85rem;">🥉 #3</span></c:when>
                                        <c:otherwise>#${status.index + 1}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td><strong><c:out value="${row.medicine_name}"/></strong></td>
                                <td><c:out value="${row.category_name != null ? row.category_name : '-'}"/></td>
                                <td style="text-align: right;"><strong>${row.total_sold}</strong> units</td>
                                <td style="text-align: right;">
                                    <strong style="color: var(--primary);">₹<fmt:formatNumber value="${row.total_revenue}" pattern="#,##0.00"/></strong>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="5" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                No sales recorded yet.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
