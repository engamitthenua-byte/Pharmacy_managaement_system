<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Near-Expiry Batches" />
<c:set var="pageNav" value="inventory" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="card">
    <div class="card-header">
        <h2 class="card-title">⏳ Batches Expiring in Next 30 Days</h2>
        <a href="${pageContext.request.contextPath}/inventory" class="btn btn-outline btn-sm">&larr; Back to Inventory</a>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Medicine Name</th>
                    <th>Batch #</th>
                    <th>Expiry Date</th>
                    <th>Days Remaining</th>
                    <th>Available Qty</th>
                    <th>Selling Price</th>
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
                                <td><strong>${b.expiryDate}</strong></td>
                                <td><strong style="color: #d97706;">${b.daysUntilExpiry} days</strong></td>
                                <td>${b.quantity}</td>
                                <td>₹<fmt:formatNumber value="${b.sellingPrice}" pattern="#,##0.00"/></td>
                                <td><span class="badge badge-warning">Near Expiry</span></td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="8" style="text-align: center; padding: 2.5rem; color: var(--success);">
                                ✓ No batches expiring within the next 30 days!
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
