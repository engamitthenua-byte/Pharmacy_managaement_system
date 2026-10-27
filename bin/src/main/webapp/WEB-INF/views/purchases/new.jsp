<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="New Purchase Order" />
<c:set var="pageNav" value="purchases" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<%-- Hidden template containing medicine options for JavaScript row generation --%>
<div id="medicine-options-template" style="display: none;">
    <c:forEach var="m" items="${medicines}">
        <option value="${m.id}"><c:out value="${m.name}"/> (${m.unit})</option>
    </c:forEach>
</div>

<form action="${pageContext.request.contextPath}/purchases" method="post" id="purchase-form">
    <input type="hidden" name="action" value="save">

    <div class="card">
        <div class="card-header">
            <h2 class="card-title">Record Stock Purchase Invoice</h2>
            <a href="${pageContext.request.contextPath}/purchases" class="btn btn-outline btn-sm">&larr; Back to Invoices</a>
        </div>
        <div class="card-body">
            <div class="form-grid">
                <div class="form-group">
                    <label class="form-label" for="invoiceNumber">Invoice / PO Number <span class="required">*</span></label>
                    <input type="text" id="invoiceNumber" name="invoiceNumber" class="form-control" 
                           value="${invoiceNumber}" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="supplierId">Supplier / Distributor <span class="required">*</span></label>
                    <select id="supplierId" name="supplierId" class="form-control" required>
                        <option value="">-- Select Supplier --</option>
                        <c:forEach var="s" items="${suppliers}">
                            <option value="${s.id}"><c:out value="${s.name}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group">
                    <label class="form-label" for="purchaseDate">Purchase Date <span class="required">*</span></label>
                    <input type="date" id="purchaseDate" name="purchaseDate" class="form-control" 
                           value="${today}" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="notes">Reference Notes</label>
                    <input type="text" id="notes" name="notes" class="form-control" 
                           placeholder="e.g. Received via delivery truck #4">
                </div>
            </div>
        </div>
    </div>

    <%-- Line Items Section --%>
    <div class="card">
        <div class="card-header">
            <h2 class="card-title">Purchase Line Items & Batches</h2>
            <button type="button" class="btn btn-outline btn-sm" onclick="addPurchaseRow()">
                + Add Another Medicine Row
            </button>
        </div>
        <div class="table-responsive">
            <table class="data-table" id="purchase-items-table">
                <thead>
                    <tr>
                        <th style="min-width: 220px;">Medicine <span class="required">*</span></th>
                        <th style="min-width: 130px;">Batch # <span class="required">*</span></th>
                        <th style="min-width: 140px;">Mfg Date</th>
                        <th style="min-width: 140px;">Expiry Date <span class="required">*</span></th>
                        <th style="width: 110px;">Cost (₹) <span class="required">*</span></th>
                        <th style="width: 110px;">MRP (₹) <span class="required">*</span></th>
                        <th style="width: 90px;">Quantity <span class="required">*</span></th>
                        <th style="width: 130px;">Line Total</th>
                        <th style="width: 50px; text-align: center;">✕</th>
                    </tr>
                </thead>
                <tbody id="purchase-items-body">
                    <tr class="purchase-item-row">
                        <td>
                            <select name="medicineIds" class="form-control" required>
                                <option value="">-- Select Medicine --</option>
                                <c:forEach var="m" items="${medicines}">
                                    <option value="${m.id}"><c:out value="${m.name}"/> (${m.unit})</option>
                                </c:forEach>
                            </select>
                        </td>
                        <td>
                            <input type="text" name="batchNumbers" class="form-control" placeholder="e.g. BATCH-01" required>
                        </td>
                        <td>
                            <input type="date" name="mfgDates" class="form-control">
                        </td>
                        <td>
                            <input type="date" name="expiryDates" class="form-control" required>
                        </td>
                        <td>
                            <input type="number" name="purchasePrices" class="form-control purchase-cost-input" step="0.01" min="0.01" value="10.00" required oninput="onPurchasePriceChange(this)">
                        </td>
                        <td>
                            <input type="number" name="sellingPrices" class="form-control" step="0.01" min="0.01" value="15.00" required>
                        </td>
                        <td>
                            <input type="number" name="quantities" class="form-control purchase-qty-input" min="1" value="10" required oninput="onPurchasePriceChange(this)">
                        </td>
                        <td>
                            <input type="text" class="form-control purchase-line-total" readonly value="100.00" style="background-color: #f8fafc; font-weight: 600;">
                        </td>
                        <td style="text-align: center;">
                            <button type="button" class="btn btn-danger btn-sm" onclick="removePurchaseRow(this)" title="Remove item">✕</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="card-body" style="background: #f8fafc; border-top: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center;">
            <button type="button" class="btn btn-outline" onclick="addPurchaseRow()">
                + Add Another Medicine Row
            </button>
            <div style="display: flex; align-items: center; gap: 2rem;">
                <div style="font-size: 1.15rem; font-weight: 700;">
                    Total Purchase Cost: <span id="purchase-total-display" style="color: var(--primary);">₹100.00</span>
                </div>
                <button type="submit" class="btn btn-primary btn-lg">
                    Confirm & Receive Stock
                </button>
            </div>
        </div>
    </div>
</form>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
