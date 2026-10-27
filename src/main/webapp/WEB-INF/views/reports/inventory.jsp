<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Inventory Valuation Report" />
<c:set var="pageNav" value="reports" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar no-print">
    <div style="font-size: 0.95rem; font-weight: 600; color: var(--text-muted);">
        Comprehensive Stock & Reserve Level Audit
    </div>
    <div style="display: flex; gap: 0.5rem;">
        <button type="button" class="btn btn-outline btn-sm" onclick="window.print()">🖨️ Print Report</button>
        <a href="${pageContext.request.contextPath}/reports" class="btn btn-outline btn-sm">&larr; Reports Hub</a>
    </div>
</div>

<div class="card">
    <div class="card-header">
        <h2 class="card-title">Inventory Valuation & Stock Health Report</h2>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Medicine Description</th>
                    <th>Category</th>
                    <th>Supplier</th>
                    <th>Unit</th>
                    <th>Min Level</th>
                    <th>Available (Non-Expired)</th>
                    <th>Total Batch Qty</th>
                    <th>Stock Condition</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty reportData}">
                        <c:forEach var="row" items="${reportData}" varStatus="status">
                            <tr>
                                <td>${status.index + 1}</td>
                                <td><strong><c:out value="${row.medicine_name}"/></strong></td>
                                <td><c:out value="${row.category_name != null ? row.category_name : '-'}"/></td>
                                <td><c:out value="${row.supplier_name != null ? row.supplier_name : '-'}"/></td>
                                <td><c:out value="${row.unit != null ? row.unit : 'Tablet'}"/></td>
                                <td>${row.min_stock_level}</td>
                                <td><strong>${row.available_qty}</strong></td>
                                <td>${row.total_qty}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${row.available_qty == 0}">
                                            <span class="badge badge-danger">Out of Stock</span>
                                        </c:when>
                                        <c:when test="${row.available_qty <= row.min_stock_level}">
                                            <span class="badge badge-warning">Low Stock</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-success">Optimal</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="9" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                No inventory records found.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
