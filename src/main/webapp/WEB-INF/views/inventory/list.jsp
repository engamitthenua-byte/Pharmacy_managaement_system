<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Inventory & Stock Control" />
<c:set var="pageNav" value="inventory" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar">
    <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
        <a href="${pageContext.request.contextPath}/inventory?action=transactions" class="btn btn-outline">
            📜 Stock Audit Log
        </a>
        <a href="${pageContext.request.contextPath}/inventory?action=lowstock" class="btn btn-outline">
            ⚠️ Low Stock (${lowStockList != null ? lowStockList.size() : 0})
        </a>
        <a href="${pageContext.request.contextPath}/inventory?action=expired" class="btn btn-outline">
            ⛔ Expired (${expiredBatches != null ? expiredBatches.size() : 0})
        </a>
        <a href="${pageContext.request.contextPath}/inventory?action=nearexpiry" class="btn btn-outline">
            ⏳ Near Expiry (${nearExpiryBatches != null ? nearExpiryBatches.size() : 0})
        </a>
    </div>

    <div style="display: flex; gap: 0.5rem;">
        <a href="${pageContext.request.contextPath}/inventory?action=adjust" class="btn btn-primary">
            ⚖️ Adjust Stock
        </a>
    </div>
</div>

<div class="card">
    <div class="card-header">
        <h2 class="card-title">Inventory Stock Status by Medicine</h2>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Medicine Name</th>
                    <th>Category</th>
                    <th>Supplier</th>
                    <th>Unit</th>
                    <th>Min Level</th>
                    <th>Available Stock</th>
                    <th>Stock Health</th>
                    <th style="text-align: right;">Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty medicines}">
                        <c:forEach var="m" items="${medicines}" varStatus="status">
                            <tr>
                                <td>${status.index + 1}</td>
                                <td><strong><a href="${pageContext.request.contextPath}/medicines?action=batches&id=${m.id}"><c:out value="${m.name}"/></a></strong></td>
                                <td><c:out value="${m.categoryName != null ? m.categoryName : '-'}"/></td>
                                <td><c:out value="${m.supplierName != null ? m.supplierName : '-'}"/></td>
                                <td><c:out value="${m.unit != null ? m.unit : 'Tablet'}"/></td>
                                <td>${m.minStockLevel}</td>
                                <td><strong>${m.totalQuantity}</strong></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${m.totalQuantity == 0}">
                                            <span class="badge badge-danger">Out of Stock</span>
                                        </c:when>
                                        <c:when test="${m.totalQuantity <= m.minStockLevel}">
                                            <span class="badge badge-warning">Low Stock</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-success">Good Stock</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="table-actions" style="justify-content: flex-end;">
                                        <a href="${pageContext.request.contextPath}/medicines?action=batches&id=${m.id}" class="btn btn-outline btn-sm" title="View Batches">
                                            Batches
                                        </a>
                                        <a href="${pageContext.request.contextPath}/inventory?action=transactions&medicineId=${m.id}" class="btn btn-outline btn-sm" title="Stock History">
                                            History
                                        </a>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="9" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                No medicines in inventory.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
