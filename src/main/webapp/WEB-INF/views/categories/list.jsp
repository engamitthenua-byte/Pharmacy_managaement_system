<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Medicine Categories" />
<c:set var="pageNav" value="categories" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar">
    <form action="${pageContext.request.contextPath}/categories" method="get" class="search-form">
        <input type="hidden" name="action" value="search">
        <input type="text" name="q" class="form-control" placeholder="Search categories..." value="<c:out value='${keyword}'/>">
        <button type="submit" class="btn btn-outline">🔍</button>
        <c:if test="${not empty keyword}">
            <a href="${pageContext.request.contextPath}/categories" class="btn btn-outline" title="Clear search">✕</a>
        </c:if>
    </form>

    <a href="${pageContext.request.contextPath}/categories?action=add" class="btn btn-primary">
        + Add Category
    </a>
</div>

<div class="card">
    <div class="card-header">
        <h2 class="card-title">All Categories (${categories != null ? categories.size() : 0})</h2>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Category Name</th>
                    <th>Description</th>
                    <th>Associated Medicines</th>
                    <th style="text-align: right;">Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty categories}">
                        <c:forEach var="c" items="${categories}" varStatus="status">
                            <tr>
                                <td>${status.index + 1}</td>
                                <td><strong><c:out value="${c.name}"/></strong></td>
                                <td><c:out value="${c.description != null ? c.description : '-'}"/></td>
                                <td>
                                    <span class="badge badge-info">${c.medicineCount} Medicines</span>
                                </td>
                                <td>
                                    <div class="table-actions" style="justify-content: flex-end;">
                                        <a href="${pageContext.request.contextPath}/categories?action=edit&id=${c.id}" class="btn btn-outline btn-sm" title="Edit">
                                            ✏️
                                        </a>
                                        <form action="${pageContext.request.contextPath}/categories" method="post" style="display:inline;" onsubmit="return confirmDelete('Are you sure you want to delete category: ${c.name}?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${c.id}">
                                            <button type="submit" class="btn btn-danger btn-sm" title="Delete">🗑️</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="5" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                No categories found. Click "+ Add Category" to create one.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
