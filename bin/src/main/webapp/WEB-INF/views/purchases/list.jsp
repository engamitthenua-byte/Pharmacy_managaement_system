<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Purchases Invoices" />
<c:set var="pageNav" value="purchases" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar">
    <form action="${pageContext.request.contextPath}/purchases" method="get" class="search-form">
        <input type="hidden" name="action" value="search">
        <input type="text" name="q" class="form-control" placeholder="Search by invoice number..." value="<c:out value='${keyword}'/>">
        <button type="submit" class="btn btn-outline">🔍</button>
        <c:if test="${not empty keyword}">
            <a href="${pageContext.request.contextPath}/purchases" class="btn btn-outline" title="Clear search">✕</a>
        </c:if>
    </form>

    <a href="${pageContext.request.contextPath}/purchases?action=new" class="btn btn-primary">
        + New Purchase Order
    </a>
</div>

<div class="card">
    <div class="card-header">
        <h2 class="card-title">All Purchase Invoices (${totalCount != null ? totalCount : purchases.size()})</h2>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Invoice #</th>
                    <th>Purchase Date</th>
                    <th>Supplier Name</th>
                    <th>Items Count</th>
                    <th>Total Purchase Cost</th>
                    <th style="text-align: right;">Action</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty purchases}">
                        <c:forEach var="p" items="${purchases}">
                            <tr>
                                <td><strong><a href="${pageContext.request.contextPath}/purchases?action=view&id=${p.id}"><c:out value="${p.invoiceNumber}"/></a></strong></td>
                                <td>${p.purchaseDate}</td>
                                <td><strong><c:out value="${p.supplierName != null ? p.supplierName : '-'}"/></strong></td>
                                <td><span class="badge badge-neutral">${p.items != null ? p.items.size() : '-'} items</span></td>
                                <td><strong style="color: var(--primary);">₹<fmt:formatNumber value="${p.totalAmount}" pattern="#,##0.00"/></strong></td>
                                <td style="text-align: right;">
                                    <a href="${pageContext.request.contextPath}/purchases?action=view&id=${p.id}" class="btn btn-outline btn-sm">
                                        View Details
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="6" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                No purchases recorded yet. Click "+ New Purchase Order" to receive stock.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>

    <%-- Pagination --%>
    <c:if test="${totalPages > 1}">
        <div class="pagination">
            <div>${pageInfo}</div>
            <div class="page-numbers">
                <c:forEach var="pg" begin="1" end="${totalPages}">
                    <a href="${pageContext.request.contextPath}/purchases?page=${pg}" 
                       class="page-link ${currentPage == pg ? 'active' : ''}">${pg}</a>
                </c:forEach>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
