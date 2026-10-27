<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Edit Category" />
<c:set var="pageNav" value="categories" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="card" style="max-width: 600px; margin: 0 auto;">
    <div class="card-header">
        <h2 class="card-title">Edit Category: <c:out value="${category.name}"/></h2>
        <a href="${pageContext.request.contextPath}/categories" class="btn btn-outline btn-sm">&larr; Back to List</a>
    </div>
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/categories" method="post">
            <input type="hidden" name="action" value="edit">
            <input type="hidden" name="id" value="${category.id}">

            <div class="form-group">
                <label class="form-label" for="name">Category Name <span class="required">*</span></label>
                <input type="text" id="name" name="name" class="form-control" 
                       value="<c:out value='${category.name}'/>" required autofocus>
            </div>

            <div class="form-group">
                <label class="form-label" for="description">Description</label>
                <textarea id="description" name="description" class="form-control" rows="3"><c:out value='${category.description}'/></textarea>
            </div>

            <div style="display: flex; gap: 1rem; justify-content: flex-end; margin-top: 1.5rem;">
                <a href="${pageContext.request.contextPath}/categories" class="btn btn-outline">Cancel</a>
                <button type="submit" class="btn btn-primary">Update Category</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
