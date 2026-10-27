<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Suppliers Directory" />
<c:set var="pageNav" value="suppliers" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar">
    <form action="${pageContext.request.contextPath}/suppliers" method="get" class="search-form">
        <input type="hidden" name="action" value="search">
        <input type="text" name="q" class="form-control" placeholder="Search suppliers by name, email, or contact..." value="<c:out value='${keyword}'/>">
        <button type="submit" class="btn btn-outline">🔍</button>
        <c:if test="${not empty keyword}">
            <a href="${pageContext.request.contextPath}/suppliers" class="btn btn-outline" title="Clear search">✕</a>
        </c:if>
    </form>

    <a href="${pageContext.request.contextPath}/suppliers?action=add" class="btn btn-primary">
        + Add Supplier
    </a>
</div>

<div class="card">
    <div class="card-header">
        <h2 class="card-title">All Suppliers (${suppliers != null ? suppliers.size() : 0})</h2>
    </div>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Supplier / Distributor Name</th>
                    <th>Contact Person</th>
                    <th>Phone</th>
                    <th>Email</th>
                    <th>Address</th>
                    <th style="text-align: right;">Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty suppliers}">
                        <c:forEach var="s" items="${suppliers}" varStatus="status">
                            <tr>
                                <td>${status.index + 1}</td>
                                <td><strong><c:out value="${s.name}"/></strong></td>
                                <td><c:out value="${s.contactPerson != null ? s.contactPerson : '-'}"/></td>
                                <td><c:out value="${s.phone != null ? s.phone : '-'}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty s.email}">
                                            <a href="mailto:${s.email}"><c:out value="${s.email}"/></a>
                                        </c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </td>
                                <td><c:out value="${s.address != null ? s.address : '-'}"/></td>
                                <td>
                                    <div class="table-actions" style="justify-content: flex-end;">
                                        <a href="${pageContext.request.contextPath}/purchases?action=new" class="btn btn-outline btn-sm" title="New Purchase from this Supplier">
                                            📥
                                        </a>
                                        <a href="${pageContext.request.contextPath}/suppliers?action=edit&id=${s.id}" class="btn btn-outline btn-sm" title="Edit">
                                            ✏️
                                        </a>
                                        <form action="${pageContext.request.contextPath}/suppliers" method="post" style="display:inline;" onsubmit="return confirmDelete('Are you sure you want to delete supplier: ${s.name}?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${s.id}">
                                            <button type="submit" class="btn btn-danger btn-sm" title="Delete">🗑️</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="7" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                No suppliers registered yet.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
