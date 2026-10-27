<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Add New Medicine" />
<c:set var="pageNav" value="medicines" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="card" style="max-width: 800px; margin: 0 auto;">
    <div class="card-header">
        <h2 class="card-title">Add New Medicine to Catalogue</h2>
        <a href="${pageContext.request.contextPath}/medicines" class="btn btn-outline btn-sm">&larr; Back to List</a>
    </div>
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/medicines" method="post">
            <input type="hidden" name="action" value="add">

            <div class="form-grid">
                <div class="form-group">
                    <label class="form-label" for="name">Medicine Name <span class="required">*</span></label>
                    <input type="text" id="name" name="name" class="form-control" 
                           placeholder="e.g. Paracetamol 500mg" value="<c:out value='${medicine.name}'/>" required autofocus>
                </div>

                <div class="form-group">
                    <label class="form-label" for="categoryId">Category <span class="required">*</span></label>
                    <select id="categoryId" name="categoryId" class="form-control" required>
                        <option value="">-- Select Category --</option>
                        <c:forEach var="c" items="${categories}">
                            <option value="${c.id}" ${medicine.categoryId == c.id ? 'selected' : ''}><c:out value="${c.name}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group">
                    <label class="form-label" for="supplierId">Primary Supplier <span class="required">*</span></label>
                    <select id="supplierId" name="supplierId" class="form-control" required>
                        <option value="">-- Select Supplier --</option>
                        <c:forEach var="s" items="${suppliers}">
                            <option value="${s.id}" ${medicine.supplierId == s.id ? 'selected' : ''}><c:out value="${s.name}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group">
                    <label class="form-label" for="manufacturer">Manufacturer</label>
                    <input type="text" id="manufacturer" name="manufacturer" class="form-control" 
                           placeholder="e.g. Pfizer, Sun Pharma, Cipla" value="<c:out value='${medicine.manufacturer}'/>">
                </div>

                <div class="form-group">
                    <label class="form-label" for="unit">Unit of Measure</label>
                    <select id="unit" name="unit" class="form-control">
                        <option value="Tablet" ${medicine.unit == 'Tablet' ? 'selected' : ''}>Tablet</option>
                        <option value="Capsule" ${medicine.unit == 'Capsule' ? 'selected' : ''}>Capsule</option>
                        <option value="Syrup" ${medicine.unit == 'Syrup' ? 'selected' : ''}>Syrup</option>
                        <option value="Injection" ${medicine.unit == 'Injection' ? 'selected' : ''}>Injection</option>
                        <option value="Ointment" ${medicine.unit == 'Ointment' ? 'selected' : ''}>Ointment / Cream</option>
                        <option value="Drops" ${medicine.unit == 'Drops' ? 'selected' : ''}>Drops</option>
                        <option value="Bottle" ${medicine.unit == 'Bottle' ? 'selected' : ''}>Bottle</option>
                        <option value="Box" ${medicine.unit == 'Box' ? 'selected' : ''}>Box</option>
                    </select>
                </div>

                <div class="form-group">
                    <label class="form-label" for="minStockLevel">Minimum Stock Alert Threshold</label>
                    <input type="number" id="minStockLevel" name="minStockLevel" class="form-control" 
                           min="0" value="${medicine.minStockLevel != null ? medicine.minStockLevel : 10}">
                    <small style="color: var(--text-muted); font-size: 0.78rem;">System alerts when available stock reaches or drops below this quantity.</small>
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="description">Description / Composition / Instructions</label>
                <textarea id="description" name="description" class="form-control" rows="3" placeholder="Optional notes on drug dosage or indications..."><c:out value='${medicine.description}'/></textarea>
            </div>

            <div style="display: flex; gap: 1rem; justify-content: flex-end; margin-top: 1.5rem;">
                <a href="${pageContext.request.contextPath}/medicines" class="btn btn-outline">Cancel</a>
                <button type="submit" class="btn btn-primary">Save Medicine</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
