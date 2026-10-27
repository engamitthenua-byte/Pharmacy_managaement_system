<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Batches for ${medicine.name}" />
<c:set var="pageNav" value="medicines" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="card">
    <div class="card-header">
        <div>
            <h2 class="card-title">Batches for Medicine: <c:out value="${medicine.name}"/></h2>
            <p style="font-size: 0.85rem; color: var(--text-muted);">
                Total Stock: <strong>${medicine.totalQuantity}</strong> ${medicine.unit}s |
                Min Threshold: <strong>${medicine.minStockLevel}</strong>
            </p>
        </div>
        <div style="display: flex; gap: 0.5rem;">
            <a href="${pageContext.request.contextPath}/purchases?action=new" class="btn btn-primary btn-sm">+ Add Stock (Purchase)</a>
            <a href="${pageContext.request.contextPath}/medicines" class="btn btn-outline btn-sm">&larr; Back to Medicines</a>
        </div>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Batch #</th>
                    <th>Mfg Date</th>
                    <th>Expiry Date</th>
                    <th>Purchase Price</th>
                    <th>Selling Price</th>
                    <th>Available Quantity</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty batches}">
                        <c:forEach var="b" items="${batches}">
                            <tr>
                                <td><strong><c:out value="${b.batchNumber}"/></strong></td>
                                <td>${b.manufacturingDate != null ? b.manufacturingDate : '-'}</td>
                                <td>
                                    <strong>${b.expiryDate}</strong>
                                    <c:if test="${b.expired}">
                                        <span style="color: var(--danger); font-size: 0.75rem; display: block;">Expired</span>
                                    </c:if>
                                </td>
                                <td>₹<fmt:formatNumber value="${b.purchasePrice}" pattern="#,##0.00"/></td>
                                <td><strong>₹<fmt:formatNumber value="${b.sellingPrice}" pattern="#,##0.00"/></strong></td>
                                <td><strong>${b.quantity}</strong></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${b.expired}">
                                            <span class="badge badge-danger">Expired</span>
                                        </c:when>
                                        <c:when test="${b.nearExpiry}">
                                            <span class="badge badge-warning">Near Expiry</span>
                                        </c:when>
                                        <c:when test="${b.quantity == 0}">
                                            <span class="badge badge-neutral">Out of Stock</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-success">In Stock</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="7" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                No batches found for this medicine.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
