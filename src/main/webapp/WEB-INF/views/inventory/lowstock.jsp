<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Low Stock Alert" />
<c:set var="pageNav" value="inventory" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="card">
    <div class="card-header">
        <h2 class="card-title">⚠️ Low-Stock Medicines Alert</h2>
        <div style="display: flex; gap: 0.5rem;">
            <a href="${pageContext.request.contextPath}/purchases?action=new" class="btn btn-primary btn-sm">+ Purchase Stock</a>
            <a href="${pageContext.request.contextPath}/inventory" class="btn btn-outline btn-sm">&larr; Back to Inventory</a>
        </div>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Medicine Name</th>
                    <th>Category</th>
                    <th>Supplier</th>
                    <th>Min Level</th>
                    <th>Current Stock</th>
                    <th>Status</th>
                    <th style="text-align: right;">Action</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty medicines}">
                        <c:forEach var="m" items="${medicines}" varStatus="status">
                            <tr>
                                <td>${status.index + 1}</td>
                                <td><strong><c:out value="${m.name}"/></strong></td>
                                <td><c:out value="${m.categoryName != null ? m.categoryName : '-'}"/></td>
                                <td><c:out value="${m.supplierName != null ? m.supplierName : '-'}"/></td>
                                <td>${m.minStockLevel}</td>
                                <td><strong style="color: #d97706;">${m.totalQuantity} ${m.unit}s</strong></td>
                                <td><span class="badge badge-warning">Low Stock</span></td>
                                <td style="text-align: right;">
                                    <a href="${pageContext.request.contextPath}/purchases?action=new" class="btn btn-primary btn-sm">Order</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="8" style="text-align: center; padding: 2.5rem; color: var(--success);">
                                ✓ No low-stock items at this moment!
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
