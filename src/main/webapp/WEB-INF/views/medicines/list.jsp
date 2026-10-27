<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Medicines Catalogue" />
<c:set var="pageNav" value="medicines" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar">
    <form action="${pageContext.request.contextPath}/medicines" method="get" class="search-form">
        <input type="hidden" name="action" value="search">
        <input type="text" name="q" class="form-control" placeholder="Search medicine by name..." value="<c:out value='${keyword}'/>">
        <button type="submit" class="btn btn-outline">🔍</button>
        <c:if test="${not empty keyword}">
            <a href="${pageContext.request.contextPath}/medicines" class="btn btn-outline" title="Clear search">✕</a>
        </c:if>
    </form>

    <div style="display: flex; gap: 0.5rem; align-items: center;">
        <a href="${pageContext.request.contextPath}/medicines?action=lowstock" class="btn btn-outline btn-sm">
            ⚠️ Low Stock
        </a>
        <a href="${pageContext.request.contextPath}/medicines?action=expired" class="btn btn-outline btn-sm">
            ⛔ Expired
        </a>
        <a href="${pageContext.request.contextPath}/medicines?action=add" class="btn btn-primary">
            + Add Medicine
        </a>
    </div>
</div>

<div class="card">
    <div class="card-header">
        <h2 class="card-title">All Medicines (${totalCount != null ? totalCount : medicines.size()})</h2>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Medicine Name</th>
                    <th>Category</th>
                    <th>Supplier</th>
                    <th>Manufacturer</th>
                    <th>Unit</th>
                    <th>Min Level</th>
                    <th>In Stock</th>
                    <th>Status</th>
                    <th style="text-align: right;">Actions</th>
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
                                    <c:if test="${not empty m.description}">
                                        <div style="font-size: 0.78rem; color: var(--text-muted);"><c:out value="${m.description}"/></div>
                                    </c:if>
                                </td>
                                <td><c:out value="${m.categoryName != null ? m.categoryName : '-'}"/></td>
                                <td><c:out value="${m.supplierName != null ? m.supplierName : '-'}"/></td>
                                <td><c:out value="${m.manufacturer != null ? m.manufacturer : '-'}"/></td>
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
                                            <span class="badge badge-success">In Stock</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="table-actions" style="justify-content: flex-end;">
                                        <a href="${pageContext.request.contextPath}/medicines?action=batches&id=${m.id}" class="btn btn-outline btn-sm" title="View Batches">
                                            📦
                                        </a>
                                        <a href="${pageContext.request.contextPath}/medicines?action=edit&id=${m.id}" class="btn btn-outline btn-sm" title="Edit">
                                            ✏️
                                        </a>
                                        <form action="${pageContext.request.contextPath}/medicines" method="post" style="display:inline;" onsubmit="return confirmDelete('Are you sure you want to delete medicine: ${m.name}?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${m.id}">
                                            <button type="submit" class="btn btn-danger btn-sm" title="Delete">🗑️</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="10" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                No medicines found in the catalogue.
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
                <c:forEach var="p" begin="1" end="${totalPages}">
                    <a href="${pageContext.request.contextPath}/medicines?page=${p}" 
                       class="page-link ${currentPage == p ? 'active' : ''}">${p}</a>
                </c:forEach>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
