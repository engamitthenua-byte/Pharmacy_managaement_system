<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="POS Billing & Counter Sale" />
<c:set var="pageNav" value="pos" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<%-- Hidden medicine options template for JavaScript dynamic rows --%>
<div id="medicine-options-template" style="display: none;">
    <c:forEach var="m" items="${medicines}">
        <c:if test="${m.totalQuantity > 0}">
            <option value="${m.id}"><c:out value="${m.name}"/> (${m.unit}) [Avail: ${m.totalQuantity}]</option>
        </c:if>
    </c:forEach>
</div>

<form action="${pageContext.request.contextPath}/sales" method="post" id="pos-billing-form">
    <input type="hidden" name="action" value="save">
    <input type="hidden" name="subtotal" id="pos-subtotal-hidden" value="0.00">

    <div class="pos-container">
        <%-- Left Column: Customer info & Items --%>
        <div>
            <div class="card">
                <div class="card-header">
                    <h2 class="card-title">Bill & Customer Details</h2>
                    <span class="badge badge-info">${invoiceNumber}</span>
                </div>
                <div class="card-body">
                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label" for="invoiceNumber">Invoice Number</label>
                            <input type="text" id="invoiceNumber" name="invoiceNumber" class="form-control" 
                                   value="${invoiceNumber}" readonly style="background-color: #f8fafc; font-weight: 600;">
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="saleDate">Bill Date</label>
                            <input type="date" id="saleDate" name="saleDate" class="form-control" 
                                   value="${today}" required>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="customerId">Select Customer</label>
                            <select id="customerId" name="customerId" class="form-control">
                                <option value="0">-- Walk-in / Cash Customer --</option>
                                <c:forEach var="c" items="${customers}">
                                    <option value="${c.id}" ${param.customerId == c.id ? 'selected' : ''}>
                                        <c:out value="${c.name}"/> ${not empty c.phone ? '('.concat(c.phone).concat(')') : ''}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="customerName">Walk-in Name (Optional)</label>
                            <input type="text" id="customerName" name="customerName" class="form-control" 
                                   placeholder="e.g. Walk-in / Patient Name">
                        </div>
                    </div>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <h2 class="card-title">Prescription / Billed Medicines</h2>
                    <button type="button" class="btn btn-outline btn-sm" onclick="addPosRow()">
                        + Add Medicine
                    </button>
                </div>
                <div class="table-responsive">
                    <table class="data-table" id="pos-items-table">
                        <thead>
                            <tr>
                                <th style="min-width: 200px;">Medicine <span class="required">*</span></th>
                                <th style="min-width: 170px;">Batch / Expiry <span class="required">*</span></th>
                                <th style="width: 110px;">Stock</th>
                                <th style="width: 100px;">Price (₹)</th>
                                <th style="width: 90px;">Qty <span class="required">*</span></th>
                                <th style="width: 110px;">Total</th>
                                <th style="width: 50px; text-align: center;">✕</th>
                            </tr>
                        </thead>
                        <tbody id="pos-items-body">
                            <tr class="pos-item-row">
                                <td>
                                    <select name="medicineId[]" class="form-control pos-medicine-select" required onchange="onMedicineChange(this)">
                                        <option value="">-- Select Medicine --</option>
                                        <c:forEach var="m" items="${medicines}">
                                            <c:if test="${m.totalQuantity > 0}">
                                                <option value="${m.id}"><c:out value="${m.name}"/> (${m.unit}) [Avail: ${m.totalQuantity}]</option>
                                            </c:if>
                                        </c:forEach>
                                    </select>
                                </td>
                                <td>
                                    <select name="batchId[]" class="form-control pos-batch-select" required onchange="onBatchChange(this)">
                                        <option value="">-- Select Batch --</option>
                                    </select>
                                </td>
                                <td>
                                    <span class="batch-stock-badge badge badge-neutral">-</span>
                                </td>
                                <td>
                                    <input type="number" name="price[]" class="form-control pos-price-input" step="0.01" min="0" required readonly style="background-color: #f8fafc;">
                                </td>
                                <td>
                                    <input type="number" name="quantity[]" class="form-control pos-qty-input" min="1" value="1" required oninput="onQuantityChange(this)">
                                </td>
                                <td>
                                    <input type="text" class="form-control pos-line-total" readonly value="0.00" style="background-color: #f8fafc; font-weight: 600;">
                                </td>
                                <td style="text-align: center;">
                                    <button type="button" class="btn btn-danger btn-sm" onclick="removePosRow(this)" title="Remove item">✕</button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="card-body" style="padding: 1rem 1.5rem; background-color: #f8fafc; border-top: 1px solid var(--border);">
                    <button type="button" class="btn btn-outline" onclick="addPosRow()">
                        + Add Another Item
                    </button>
                </div>
            </div>

            <div class="card">
                <div class="card-body">
                    <label class="form-label" for="notes">Notes / Doctor / Prescription Reference</label>
                    <input type="text" id="notes" name="notes" class="form-control" placeholder="e.g. Dr. Sharma Clinic Rx #1092">
                </div>
            </div>
        </div>

        <%-- Right Column: Payment & Summary --%>
        <div>
            <div class="pos-summary-card">
                <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 1.25rem; border-bottom: 1px solid var(--border); padding-bottom: 0.75rem;">
                    Payment Summary
                </h3>

                <div class="summary-row">
                    <span>Subtotal:</span>
                    <span id="pos-subtotal-display" style="font-weight: 600;">₹0.00</span>
                </div>

                <div class="form-group" style="margin-top: 0.75rem; margin-bottom: 0.75rem;">
                    <label class="form-label" for="pos-discount" style="font-size: 0.8rem;">Discount (₹)</label>
                    <input type="number" id="pos-discount" name="discount" class="form-control" step="0.01" min="0" value="0.00">
                </div>

                <div class="form-group" style="margin-bottom: 1rem;">
                    <label class="form-label" for="pos-tax" style="font-size: 0.8rem;">Tax / GST (₹)</label>
                    <input type="number" id="pos-tax" name="taxAmount" class="form-control" step="0.01" min="0" value="0.00">
                </div>

                <div class="summary-row total">
                    <span>Grand Total:</span>
                    <span id="pos-total-display" style="color: var(--primary);">₹0.00</span>
                </div>

                <button type="submit" class="btn btn-primary btn-lg" style="width: 100%; margin-top: 1.5rem;">
                    ⚡ Complete Sale & Print Bill
                </button>

                <div style="margin-top: 1rem; text-align: center;">
                    <a href="${pageContext.request.contextPath}/sales" class="btn btn-outline btn-sm" style="width: 100%;">
                        Cancel Bill
                    </a>
                </div>
            </div>
        </div>
    </div>
</form>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
