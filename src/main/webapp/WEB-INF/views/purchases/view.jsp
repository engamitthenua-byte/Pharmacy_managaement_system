<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Purchase Invoice: ${purchase.invoiceNumber}" />
<c:set var="pageNav" value="purchases" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar no-print">
    <a href="${pageContext.request.contextPath}/purchases" class="btn btn-outline">&larr; Back to Invoices</a>
    <div style="display: flex; gap: 0.5rem;">
        <button type="button" class="btn btn-outline" onclick="window.print()">🖨️ Print Voucher</button>
        <a href="${pageContext.request.contextPath}/purchases?action=new" class="btn btn-primary">+ New Purchase</a>
    </div>
</div>

<div class="invoice-sheet">
    <div class="invoice-header">
        <div>
            <div class="invoice-brand">💊 PharmaCare</div>
            <div style="font-size: 0.85rem; color: var(--text-muted); margin-top: 0.25rem;">
                Stock Receiving Voucher / Purchase Invoice
            </div>
        </div>
        <div class="invoice-meta">
            <h2 style="font-size: 1.25rem; font-weight: 700; color: var(--primary);"><c:out value="${purchase.invoiceNumber}"/></h2>
            <div style="font-size: 0.88rem; color: var(--text-muted);">
                Purchase Date: <strong>${purchase.purchaseDate}</strong>
            </div>
            <div style="font-size: 0.85rem; color: var(--text-light);">
                Recorded: ${purchase.createdAt}
            </div>
        </div>
    </div>

    <div style="display: flex; justify-content: space-between; margin-bottom: 2rem; background: var(--bg-body); padding: 1.25rem; border-radius: var(--radius-sm);">
        <div>
            <span style="font-size: 0.78rem; font-weight: 600; text-transform: uppercase; color: var(--text-muted);">Supplier Details</span>
            <h3 style="font-size: 1.15rem; font-weight: 700; margin-top: 0.25rem;"><c:out value="${purchase.supplierName}"/></h3>
        </div>
        <c:if test="${not empty purchase.notes}">
            <div style="text-align: right; max-width: 300px;">
                <span style="font-size: 0.78rem; font-weight: 600; text-transform: uppercase; color: var(--text-muted);">Reference Notes</span>
                <p style="font-size: 0.88rem; color: var(--text-muted); margin-top: 0.25rem;"><c:out value="${purchase.notes}"/></p>
            </div>
        </c:if>
    </div>

    <table class="data-table" style="margin-bottom: 1.5rem;">
        <thead>
            <tr>
                <th>#</th>
                <th>Medicine Description</th>
                <th>Batch #</th>
                <th>Expiry Date</th>
                <th style="text-align: right;">Unit Cost</th>
                <th style="text-align: right;">MRP</th>
                <th style="text-align: right;">Qty</th>
                <th style="text-align: right;">Total Amount</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="item" items="${purchase.items}" varStatus="status">
                <tr>
                    <td>${status.index + 1}</td>
                    <td><strong><c:out value="${item.medicineName}"/></strong></td>
                    <td><c:out value="${item.batchNumber}"/></td>
                    <td>${item.expiryDate}</td>
                    <td style="text-align: right;">₹<fmt:formatNumber value="${item.purchasePrice}" pattern="#,##0.00"/></td>
                    <td style="text-align: right;">₹<fmt:formatNumber value="${item.sellingPrice}" pattern="#,##0.00"/></td>
                    <td style="text-align: right;"><strong>${item.quantity}</strong></td>
                    <td style="text-align: right;"><strong>₹<fmt:formatNumber value="${item.totalPrice}" pattern="#,##0.00"/></strong></td>
                </tr>
            </c:forEach>
        </tbody>
    </table>

    <div style="display: flex; justify-content: flex-end;">
        <div style="width: 280px; background: #f8fafc; padding: 1.25rem; border-radius: var(--radius-sm); border: 1px solid var(--border);">
            <div class="summary-row total">
                <span>Grand Total:</span>
                <span style="color: var(--primary);">₹<fmt:formatNumber value="${purchase.totalAmount}" pattern="#,##0.00"/></span>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
