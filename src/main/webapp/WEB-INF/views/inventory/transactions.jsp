<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Inventory Audit Trail" />
<c:set var="pageNav" value="inventory" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar">
    <form action="${pageContext.request.contextPath}/inventory" method="get" class="search-form">
        <input type="hidden" name="action" value="transactions">
        <select name="medicineId" class="form-control" onchange="this.form.submit()">
            <option value="">-- All Medicines (All Movements) --</option>
            <c:forEach var="m" items="${medicines}">
                <option value="${m.id}" ${filterMedicineId == m.id ? 'selected' : ''}><c:out value="${m.name}"/></option>
            </c:forEach>
        </select>
        <c:if test="${not empty filterMedicineId}">
            <a href="${pageContext.request.contextPath}/inventory?action=transactions" class="btn btn-outline" title="Show All">Clear</a>
        </c:if>
    </form>

    <div style="display: flex; gap: 0.5rem;">
        <a href="${pageContext.request.contextPath}/inventory?action=adjust" class="btn btn-primary">
            + Record Stock Adjustment
        </a>
        <a href="${pageContext.request.contextPath}/inventory" class="btn btn-outline">
            &larr; Back to Stock List
        </a>
    </div>
</div>

<div class="card">
    <div class="card-header">
        <h2 class="card-title">Inventory Audit Trail & Stock Movements (${totalCount != null ? totalCount : transactions.size()})</h2>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Date & Time</th>
                    <th>Medicine</th>
                    <th>Batch #</th>
                    <th>Movement Type</th>
                    <th style="text-align: right;">Quantity Change</th>
                    <th>Reference / Invoice #</th>
                    <th>Reason / Notes</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty transactions}">
                        <c:forEach var="t" items="${transactions}">
                            <tr>
                                <td>${t.transactionDate}</td>
                                <td><strong><c:out value="${t.medicineName}"/></strong></td>
                                <td><c:out value="${t.batchNumber}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${t.transactionType == 'PURCHASE'}">
                                            <span class="badge badge-success">Purchase (Stock In)</span>
                                        </c:when>
                                        <c:when test="${t.transactionType == 'SALE'}">
                                            <span class="badge badge-info">Sale (Stock Out)</span>
                                        </c:when>
                                        <c:when test="${t.transactionType == 'ADJUSTMENT_IN'}">
                                            <span class="badge badge-success">Adjustment (+)</span>
                                        </c:when>
                                        <c:when test="${t.transactionType == 'ADJUSTMENT_OUT'}">
                                            <span class="badge badge-danger">Adjustment (-)</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-neutral">${t.transactionType}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align: right;">
                                    <strong style="color: ${t.quantityChange > 0 ? 'var(--success)' : 'var(--danger)'};">
                                        ${t.quantityChange > 0 ? '+'.concat(t.quantityChange) : t.quantityChange}
                                    </strong>
                                </td>
                                <td><c:out value="${t.referenceNumber != null ? t.referenceNumber : '-'}"/></td>
                                <td><c:out value="${t.reason != null ? t.reason : '-'}"/></td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="7" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                No stock transactions recorded yet.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>

    <%-- Pagination --%>
    <c:if test="${totalPages > 1 && empty filterMedicineId}">
        <div class="pagination">
            <div>${pageInfo}</div>
            <div class="page-numbers">
                <c:forEach var="p" begin="1" end="${totalPages}">
                    <a href="${pageContext.request.contextPath}/inventory?action=transactions&page=${p}" 
                       class="page-link ${currentPage == p ? 'active' : ''}">${p}</a>
                </c:forEach>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
