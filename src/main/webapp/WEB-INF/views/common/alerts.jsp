<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- Flash Success Message --%>
<c:if test="${not empty sessionScope.successMsg}">
    <div class="alert alert-success alert-dismissible">
        <span>✓</span>
        <div><strong>Success:</strong> <c:out value="${sessionScope.successMsg}"/></div>
    </div>
    <c:remove var="successMsg" scope="session"/>
</c:if>

<c:if test="${not empty requestScope.successMsg}">
    <div class="alert alert-success alert-dismissible">
        <span>✓</span>
        <div><strong>Success:</strong> <c:out value="${requestScope.successMsg}"/></div>
    </div>
</c:if>

<%-- Flash Error Message --%>
<c:if test="${not empty sessionScope.errorMsg}">
    <div class="alert alert-danger alert-dismissible">
        <span>⚠️</span>
        <div><strong>Error:</strong> <c:out value="${sessionScope.errorMsg}"/></div>
    </div>
    <c:remove var="errorMsg" scope="session"/>
</c:if>

<c:if test="${not empty requestScope.errorMsg}">
    <div class="alert alert-danger alert-dismissible">
        <span>⚠️</span>
        <div><strong>Error:</strong> <c:out value="${requestScope.errorMsg}"/></div>
    </div>
</c:if>

<%-- Validation Errors List --%>
<c:if test="${not empty requestScope.errors}">
    <div class="alert alert-danger">
        <span>⚠️</span>
        <div>
            <strong>Please correct the following:</strong>
            <ul style="margin-top: 0.35rem; margin-left: 1.25rem;">
                <c:forEach var="err" items="${requestScope.errors}">
                    <li><c:out value="${err}"/></li>
                </c:forEach>
            </ul>
        </div>
    </div>
</c:if>
