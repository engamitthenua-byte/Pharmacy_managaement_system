<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Medicine Details: ${medicine.name}" />
<c:set var="pageNav" value="medicines" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="card">
    <div class="card-header">
        <div>
            <h2 class="card-title"><c:out value="${medicine.name}"/></h2>
            <span style="font-size: 0.85rem; color: var(--text-muted);">
                Category: <strong><c:out value="${medicine.categoryName}"/></strong> | 
                Supplier: <strong><c:out value="${medicine.supplierName}"/></strong>
            </span>
        </div>
        <div style="display: flex; gap: 0.5rem;">
            <a href="${pageContext.request.contextPath}/medicines?action=edit&id=${medicine.id}" class="btn btn-outline btn-sm">✏️ Edit</a>
            <a href="${pageContext.request.contextPath}/purchases?action=new" class="btn btn-primary btn-sm">+ Buy More Stock</a>
            <a href="${pageContext.request.contextPath}/medicines" class="btn btn-outline btn-sm">&larr; Back to List</a>
        </div>
    </div>
    <div class="card-body">
        <div class="stats-grid" style="margin-bottom: 1.5rem;">
            <div class="stat-card">
                <div class="stat-info">
                    <div class="stat-label">Available Stock</div>
                    <div class="stat-value">${medicine.totalQuantity} <span style="font-size: 1rem; font-weight: normal;">${medicine.unit}s</span></div>
                </div>
                <div class="stat-icon ${medicine.totalQuantity <= medicine.minStockLevel ? 'warning' : 'success'}">📦</div>
            </div>
            <div class="stat-card">
                <div class="stat-info">
                    <div class="stat-label">Min Stock Threshold</div>
                    <div class="stat-value">${medicine.minStockLevel}</div>
                </div>
                <div class="stat-icon primary">🎯</div>
            </div>
            <div class="stat-card">
                <div class="stat-info">
                    <div class="stat-label">Manufacturer</div>
                    <div class="stat-value" style="font-size: 1.25rem;"><c:out value="${medicine.manufacturer != null ? medicine.manufacturer : 'N/A'}"/></div>
                </div>
                <div class="stat-icon info">🏭</div>
            </div>
            <div class="stat-card">
                <div class="stat-info">
                    <div class="stat-label">Active Batches</div>
                    <div class="stat-value">${batches != null ? batches.size() : 0}</div>
                </div>
                <div class="stat-icon primary">📑</div>
            </div>
        </div>

        <c:if test="${not empty medicine.description}">
            <div style="background-color: var(--bg-body); padding: 1rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem;">
                <strong>Description / Composition:</strong>
                <p style="margin-top: 0.25rem; font-size: 0.92rem; color: var(--text-muted);"><c:out value="${medicine.description}"/></p>
            </div>
        </c:if>

        <h3 style="font-size: 1.1rem; font-weight: 600; margin-bottom: 0.85rem;">Medicine Batches in Inventory</h3>
        <div class="table-responsive">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Batch Number</th>
                        <th>Mfg Date</th>
                        <th>Expiry Date</th>
                        <th>Purchase Cost</th>
                        <th>Selling Price</th>
                        <th>Remaining Qty</th>
                        <th>Batch Status</th>
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
                                        <c:if test="${b.nearExpiry && !b.expired}">
                                            <span style="color: #d97706; font-size: 0.75rem; display: block;">Expires in ${b.daysUntilExpiry} days</span>
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
                                                <span class="badge badge-neutral">Depleted</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-success">Active Stock</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="7" style="text-align: center; padding: 2rem; color: var(--text-muted);">
                                    No batches recorded for this medicine yet. Add stock via Purchase Invoice.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
