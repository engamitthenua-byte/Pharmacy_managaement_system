<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Expired Batches" />
<c:set var="pageNav" value="inventory" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="card">
    <div class="card-header">
        <h2 class="card-title">⛔ Expired Batches in Inventory</h2>
        <div style="display: flex; gap: 0.5rem;">
            <a href="${pageContext.request.contextPath}/inventory?action=adjust" class="btn btn-danger btn-sm">Write-off Stock</a>
            <a href="${pageContext.request.contextPath}/inventory" class="btn btn-outline btn-sm">&larr; Back to Inventory</a>
        </div>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Medicine Name</th>
                    <th>Batch #</th>
                    <th>Expiry Date</th>
                    <th>Remaining Qty</th>
                    <th>Cost Price</th>
                    <th>Total Loss</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty batches}">
                        <c:forEach var="b" items="${batches}" varStatus="status">
                            <tr>
                                <td>${status.index + 1}</td>
                                <td><strong><c:out value="${b.medicineName}"/></strong></td>
                                <td><c:out value="${b.batchNumber}"/></td>
                                <td><strong style="color: var(--danger);">${b.expiryDate}</strong></td>
                                <td>${b.quantity}</td>
                                <td>₹<fmt:formatNumber value="${b.purchasePrice}" pattern="#,##0.00"/></td>
                                <td><strong style="color: var(--danger);">₹<fmt:formatNumber value="${b.purchasePrice * b.quantity}" pattern="#,##0.00"/></strong></td>
                                <td><span class="badge badge-danger">Expired</span></td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="8" style="text-align: center; padding: 2.5rem; color: var(--success);">
                                ✓ No expired batches found in inventory.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
