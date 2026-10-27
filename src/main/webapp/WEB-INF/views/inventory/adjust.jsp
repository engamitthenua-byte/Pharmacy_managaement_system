<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manual Stock Adjustment" />
<c:set var="pageNav" value="inventory" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="card" style="max-width: 650px; margin: 0 auto;">
    <div class="card-header">
        <h2 class="card-title">Record Stock Adjustment / Disposal</h2>
        <a href="${pageContext.request.contextPath}/inventory" class="btn btn-outline btn-sm">&larr; Back to Inventory</a>
    </div>
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/inventory" method="post">
            <input type="hidden" name="action" value="adjust">

            <div class="form-group">
                <label class="form-label" for="adjust-med-select">Select Medicine <span class="required">*</span></label>
                <select id="adjust-med-select" name="medicineId" class="form-control" required onchange="loadAdjustBatches(this)">
                    <option value="">-- Choose Medicine --</option>
                    <c:forEach var="m" items="${medicines}">
                        <option value="${m.id}"><c:out value="${m.name}"/> (${m.unit})</option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label class="form-label" for="adjust-batch-select">Select Batch <span class="required">*</span></label>
                <select id="adjust-batch-select" name="batchId" class="form-control" required>
                    <option value="">-- Choose Medicine First --</option>
                </select>
            </div>

            <div class="form-grid">
                <div class="form-group">
                    <label class="form-label" for="type">Adjustment Type <span class="required">*</span></label>
                    <select id="type" name="type" class="form-control" required>
                        <option value="OUT">Stock Decrease (Disposal, Damage, Expired)</option>
                        <option value="IN">Stock Increase (Found Extra, Audit Correction)</option>
                    </select>
                </div>

                <div class="form-group">
                    <label class="form-label" for="quantity">Quantity to Adjust <span class="required">*</span></label>
                    <input type="number" id="quantity" name="quantity" class="form-control" min="1" value="1" required>
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="reason">Reason / Notes <span class="required">*</span></label>
                <input type="text" id="reason" name="reason" class="form-control" 
                       placeholder="e.g. Expired batch disposed, Broken bottle in transit, Inventory audit" required>
            </div>

            <div style="display: flex; gap: 1rem; justify-content: flex-end; margin-top: 1.5rem;">
                <a href="${pageContext.request.contextPath}/inventory" class="btn btn-outline">Cancel</a>
                <button type="submit" class="btn btn-primary">Submit Stock Adjustment</button>
            </div>
        </form>
    </div>
</div>

<script>
function loadAdjustBatches(selectEl) {
    const medId = selectEl.value;
    const batchSelect = document.getElementById("adjust-batch-select");
    batchSelect.innerHTML = '<option value="">Loading...</option>';
    if (!medId) {
        batchSelect.innerHTML = '<option value="">-- Choose Medicine First --</option>';
        return;
    }
    const contextPath = document.body.getAttribute("data-context") || "";
    fetch(contextPath + "/sales?action=batches&medicineId=" + medId)
        .then(r => r.json())
        .then(batches => {
            batchSelect.innerHTML = '<option value="">-- Select Batch --</option>';
            if (batches.length === 0) {
                batchSelect.innerHTML = '<option value="">No batches found</option>';
                return;
            }
            batches.forEach(b => {
                const opt = document.createElement("option");
                opt.value = b.id;
                opt.textContent = b.batchNumber + " (Exp: " + b.expiryDate + ", Stock: " + b.quantity + ")";
                batchSelect.appendChild(opt);
            });
        });
}
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
