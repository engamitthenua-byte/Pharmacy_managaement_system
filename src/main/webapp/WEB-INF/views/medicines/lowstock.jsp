<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Low Stock Medicines Alert" />
<c:set var="pageNav" value="medicines" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="card">
    <div class="card-header">
        <div>
            <h2 class="card-title">⚠️ Low-Stock Medicines Alert</h2>
            <p style="font-size: 0.85rem; color: var(--text-muted);">Medicines that have reached or fallen below minimum reserve levels.</p>
        </div>
        <div style="display: flex; gap: 0.5rem;">
            <a href="${pageContext.request.contextPath}/purchases?action=new" class="btn btn-primary btn-sm">+ Create Purchase Order</a>
            <a href="${pageContext.request.contextPath}/medicines" class="btn btn-outline btn-sm">&larr; All Medicines</a>
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
                    <th>Current Available Stock</th>
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
                                <td>
                                    <strong><a href="${pageContext.request.contextPath}/medicines?action=view&id=${m.id}"><c:out value="${m.name}"/></a></strong>
                                </td>
                                <td><c:out value="${m.categoryName != null ? m.categoryName : '-'}"/></td>
                                <td><c:out value="${m.supplierName != null ? m.supplierName : '-'}"/></td>
                                <td>${m.minStockLevel}</td>
                                <td><strong style="color: ${m.totalQuantity == 0 ? 'var(--danger)' : '#d97706'};">${m.totalQuantity} ${m.unit}s</strong></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${m.totalQuantity == 0}">
                                            <span class="badge badge-danger">Out of Stock</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-warning">Low Stock</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align: right;">
                                    <a href="${pageContext.request.contextPath}/purchases?action=new" class="btn btn-primary btn-sm">
                                        Reorder Stock
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="8" style="text-align: center; padding: 2.5rem; color: var(--success); font-weight: 500;">
                                ✓ All medicines are sufficiently stocked above minimum levels!
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
