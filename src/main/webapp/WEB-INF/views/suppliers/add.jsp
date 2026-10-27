<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Add Supplier" />
<c:set var="pageNav" value="suppliers" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="card" style="max-width: 700px; margin: 0 auto;">
    <div class="card-header">
        <h2 class="card-title">Add New Supplier / Distributor</h2>
        <a href="${pageContext.request.contextPath}/suppliers" class="btn btn-outline btn-sm">&larr; Back to List</a>
    </div>
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/suppliers" method="post">
            <input type="hidden" name="action" value="add">

            <div class="form-grid">
                <div class="form-group">
                    <label class="form-label" for="name">Company / Supplier Name <span class="required">*</span></label>
                    <input type="text" id="name" name="name" class="form-control" 
                           placeholder="e.g. MedLife Pharma Ltd." value="<c:out value='${supplier.name}'/>" required autofocus>
                </div>

                <div class="form-group">
                    <label class="form-label" for="contactPerson">Contact Person</label>
                    <input type="text" id="contactPerson" name="contactPerson" class="form-control" 
                           placeholder="e.g. John Doe" value="<c:out value='${supplier.contactPerson}'/>">
                </div>

                <div class="form-group">
                    <label class="form-label" for="phone">Phone Number</label>
                    <input type="text" id="phone" name="phone" class="form-control" 
                           placeholder="e.g. +91 9876543210" value="<c:out value='${supplier.phone}'/>">
                </div>

                <div class="form-group">
                    <label class="form-label" for="email">Email Address</label>
                    <input type="email" id="email" name="email" class="form-control" 
                           placeholder="e.g. sales@medlife.com" value="<c:out value='${supplier.email}'/>">
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="address">Office / Warehouse Address</label>
                <textarea id="address" name="address" class="form-control" rows="3" placeholder="Full postal address..."><c:out value='${supplier.address}'/></textarea>
            </div>

            <div style="display: flex; gap: 1rem; justify-content: flex-end; margin-top: 1.5rem;">
                <a href="${pageContext.request.contextPath}/suppliers" class="btn btn-outline">Cancel</a>
                <button type="submit" class="btn btn-primary">Save Supplier</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
