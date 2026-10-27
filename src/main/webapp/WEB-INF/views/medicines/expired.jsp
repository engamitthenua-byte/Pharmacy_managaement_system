<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="${not empty viewTitle ? viewTitle : 'Expired Medicines'}" />
<c:set var="pageNav" value="medicines" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="card">
    <div class="card-header">
        <div>
            <h2 class="card-title">${viewTitle}</h2>
            <p style="font-size: 0.85rem; color: var(--text-muted);">
                Batches that are expired or approaching expiry. Expired items are automatically blocked from sales in POS.
            </p>
        </div>
        <div style="display: flex; gap: 0.5rem;">
            <a href="${pageContext.request.contextPath}/inventory?action=adjust" class="btn btn-danger btn-sm">Write-off / Dispose Stock</a>
            <a href="${pageContext.request.contextPath}/medicines" class="btn btn-outline btn-sm">&larr; Back to Catalogue</a>
        </div>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Medicine Name</th>
                    <th>Batch Number</th>
                    <th>Category</th>
                    <th>Supplier</th>
                    <th>Expiry Date</th>
                    <th>Remaining Quantity</th>
                    <th>Unit Cost</th>
                    <th>Total Loss Value</th>
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
                                <td><c:out value="${b.categoryName != null ? b.categoryName : '-'}"/></td>
                                <td><c:out value="${b.supplierName != null ? b.supplierName : '-'}"/></td>
                                <td><strong style="color: ${b.expired ? 'var(--danger)' : '#d97706'};">${b.expiryDate}</strong></td>
                                <td><strong>${b.quantity}</strong></td>
                                <td>₹<fmt:formatNumber value="${b.purchasePrice}" pattern="#,##0.00"/></td>
                                <td>
                                    <strong style="color: var(--danger);">
                                        ₹<fmt:formatNumber value="${b.purchasePrice * b.quantity}" pattern="#,##0.00"/>
                                    </strong>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${b.expired}">
                                            <span class="badge badge-danger">Expired</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-warning">Expires Soon</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="10" style="text-align: center; padding: 2.5rem; color: var(--success); font-weight: 500;">
                                ✓ No expired batches found in inventory!
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
