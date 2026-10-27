<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Tax Invoice: ${sale.invoiceNumber}" />
<c:set var="pageNav" value="sales" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<jsp:include page="/WEB-INF/views/common/alerts.jsp" />

<div class="search-toolbar no-print">
    <a href="${pageContext.request.contextPath}/sales" class="btn btn-outline">&larr; Back to Sales</a>
    <div style="display: flex; gap: 0.5rem;">
        <button type="button" class="btn btn-outline" onclick="window.print()">🖨️ Print Bill / Receipt</button>
        <a href="${pageContext.request.contextPath}/sales?action=pos" class="btn btn-primary">⚡ Next Bill</a>
    </div>
</div>

<div class="invoice-sheet">
    <div class="invoice-header">
        <div>
            <div class="invoice-brand">💊 PharmaCare Pharmacy</div>
            <div style="font-size: 0.85rem; color: var(--text-muted); margin-top: 0.25rem;">
                Retail Drug License No: <strong>DL-2026/AIP-99</strong> | GSTIN: <strong>07AAAAA0000A1Z5</strong><br>
                123 Healthcare Boulevard, Medical Center Complex<br>
                Tel: +91 98765 43210 | Email: care@pharmacare.local
            </div>
        </div>
        <div class="invoice-meta">
            <span class="badge badge-success" style="font-size: 0.82rem; margin-bottom: 0.5rem;">PAID CASH / UPI</span>
            <h2 style="font-size: 1.35rem; font-weight: 800; color: var(--primary);"><c:out value="${sale.invoiceNumber}"/></h2>
            <div style="font-size: 0.88rem; color: var(--text-muted);">
                Date: <strong>${sale.saleDate}</strong>
            </div>
            <div style="font-size: 0.8rem; color: var(--text-light);">
                Time: ${sale.createdAt}
            </div>
        </div>
    </div>

    <div style="display: flex; justify-content: space-between; margin-bottom: 1.75rem; background: var(--bg-body); padding: 1rem 1.25rem; border-radius: var(--radius-sm);">
        <div>
            <span style="font-size: 0.75rem; font-weight: 600; text-transform: uppercase; color: var(--text-muted);">Billed To:</span>
            <h3 style="font-size: 1.1rem; font-weight: 700; margin-top: 0.2rem;">
                <c:out value="${sale.displayCustomerName}"/>
            </h3>
        </div>
        <c:if test="${not empty sale.notes}">
            <div style="text-align: right; max-width: 320px;">
                <span style="font-size: 0.75rem; font-weight: 600; text-transform: uppercase; color: var(--text-muted);">Prescription / Notes:</span>
                <p style="font-size: 0.88rem; color: var(--text-muted); margin-top: 0.2rem;"><c:out value="${sale.notes}"/></p>
            </div>
        </c:if>
    </div>

    <table class="data-table" style="margin-bottom: 1.5rem;">
        <thead>
            <tr>
                <th style="width: 40px;">#</th>
                <th>Medicine Description</th>
                <th>Batch #</th>
                <th style="text-align: right; width: 110px;">Price (₹)</th>
                <th style="text-align: right; width: 80px;">Qty</th>
                <th style="text-align: right; width: 130px;">Amount (₹)</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="item" items="${sale.items}" varStatus="status">
                <tr>
                    <td>${status.index + 1}</td>
                    <td>
                        <strong><c:out value="${item.medicineName}"/></strong>
                        <c:if test="${not empty item.unit}">
                            <span style="font-size: 0.8rem; color: var(--text-muted);">(${item.unit})</span>
                        </c:if>
                    </td>
                    <td><c:out value="${item.batchNumber}"/></td>
                    <td style="text-align: right;">₹<fmt:formatNumber value="${item.sellingPrice}" pattern="#,##0.00"/></td>
                    <td style="text-align: right;"><strong>${item.quantity}</strong></td>
                    <td style="text-align: right;"><strong>₹<fmt:formatNumber value="${item.totalPrice}" pattern="#,##0.00"/></strong></td>
                </tr>
            </c:forEach>
        </tbody>
    </table>

    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-top: 1.5rem;">
        <div style="font-size: 0.82rem; color: var(--text-muted); max-width: 400px;">
            <p><strong>Terms & Conditions:</strong></p>
            <p>1. Please check medicines and expiry dates before leaving counter.</p>
            <p>2. Opened bottles, cut strips, or refrigerated items are not returnable.</p>
            <p>3. This is a computer generated retail invoice.</p>
        </div>

        <div style="width: 300px; background: #f8fafc; padding: 1.25rem; border-radius: var(--radius-sm); border: 1px solid var(--border);">
            <div class="summary-row">
                <span>Subtotal:</span>
                <span>₹<fmt:formatNumber value="${sale.subtotal}" pattern="#,##0.00"/></span>
            </div>
            <c:if test="${sale.discount > 0}">
                <div class="summary-row" style="color: #b45309;">
                    <span>Discount:</span>
                    <span>-₹<fmt:formatNumber value="${sale.discount}" pattern="#,##0.00"/></span>
                </div>
            </c:if>
            <c:if test="${sale.taxAmount > 0}">
                <div class="summary-row">
                    <span>Tax / GST:</span>
                    <span>+₹<fmt:formatNumber value="${sale.taxAmount}" pattern="#,##0.00"/></span>
                </div>
            </c:if>
            <div class="summary-row total">
                <span>Grand Total:</span>
                <span style="color: var(--primary);">₹<fmt:formatNumber value="${sale.totalAmount}" pattern="#,##0.00"/></span>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
