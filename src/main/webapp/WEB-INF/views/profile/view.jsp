<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Admin Profile & Account Settings" />
<c:set var="pageNav" value="profile" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(340px, 1fr)); gap: 1.5rem; max-width: 950px; margin: 0 auto;">
    
    <%-- Account Details Form --%>
    <div class="card">
        <div class="card-header">
            <h2 class="card-title">Profile Information</h2>
        </div>
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/profile" method="post">
                <input type="hidden" name="action" value="info">

                <div class="form-group">
                    <label class="form-label" for="username">Username</label>
                    <input type="text" id="username" class="form-control" 
                           value="<c:out value='${admin.username}'/>" readonly style="background-color: #f8fafc; font-weight: 600;">
                    <small style="color: var(--text-muted); font-size: 0.78rem;">Username cannot be modified.</small>
                </div>

                <div class="form-group">
                    <label class="form-label" for="fullName">Full Name <span class="required">*</span></label>
                    <input type="text" id="fullName" name="fullName" class="form-control" 
                           value="<c:out value='${admin.fullName}'/>" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="email">Email Address <span class="required">*</span></label>
                    <input type="email" id="email" name="email" class="form-control" 
                           value="<c:out value='${admin.email}'/>" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="phone">Contact Phone</label>
                    <input type="text" id="phone" name="phone" class="form-control" 
                           value="<c:out value='${admin.phone}'/>">
                </div>

                <div style="margin-top: 1.5rem; text-align: right;">
                    <button type="submit" class="btn btn-primary">Update Profile</button>
                </div>
            </form>
        </div>
    </div>

    <%-- Change Password Form --%>
    <div class="card">
        <div class="card-header">
            <h2 class="card-title">Change Password</h2>
        </div>
        <div class="card-body">
            <c:if test="${not empty passwordErrors}">
                <div class="alert alert-danger">
                    <span>⚠️</span>
                    <div>
                        <ul style="margin-left: 1.25rem;">
                            <c:forEach var="perr" items="${passwordErrors}">
                                <li><c:out value="${perr}"/></li>
                            </c:forEach>
                        </ul>
                    </div>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/profile" method="post">
                <input type="hidden" name="action" value="password">

                <div class="form-group">
                    <label class="form-label" for="currentPassword">Current Password <span class="required">*</span></label>
                    <input type="password" id="currentPassword" name="currentPassword" class="form-control" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="newPassword">New Password <span class="required">*</span></label>
                    <input type="password" id="newPassword" name="newPassword" class="form-control" 
                           placeholder="Minimum 6 characters" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="confirmPassword">Confirm New Password <span class="required">*</span></label>
                    <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" required>
                </div>

                <div style="margin-top: 1.5rem; text-align: right;">
                    <button type="submit" class="btn btn-primary">Change Password</button>
                </div>
            </form>
        </div>
    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
