/**
 * Pharmacy Management System — Frontend JavaScript
 * Handles dynamic line items, real-time total calculations, AJAX batch fetching, and UI helpers.
 */

document.addEventListener("DOMContentLoaded", function () {
    initAlerts();
    initPosCalculations();
    initPurchaseCalculations();
});

// ─────────────────────────────────────────────────────────────
// Flash Alerts Auto-Dismiss
// ─────────────────────────────────────────────────────────────
function initAlerts() {
    const alerts = document.querySelectorAll(".alert-dismissible");
    alerts.forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = "opacity 0.5s ease";
            alert.style.opacity = "0";
            setTimeout(function () { alert.remove(); }, 500);
        }, 5000);
    });
}

function confirmDelete(message) {
    return confirm(message || "Are you sure you want to delete this record? This action cannot be undone.");
}

// ─────────────────────────────────────────────────────────────
// POS / Billing System Real-time Interactivity
// ─────────────────────────────────────────────────────────────
function initPosCalculations() {
    const posTable = document.getElementById("pos-items-table");
    if (!posTable) return;

    // Attach listeners to discount and tax inputs
    const discountInput = document.getElementById("pos-discount");
    const taxInput = document.getElementById("pos-tax");

    if (discountInput) discountInput.addEventListener("input", recalculatePosTotals);
    if (taxInput) taxInput.addEventListener("input", recalculatePosTotals);

    // Initial calculation
    recalculatePosTotals();
}

function addPosRow() {
    const tbody = document.getElementById("pos-items-body");
    if (!tbody) return;

    const rowCount = tbody.querySelectorAll("tr").length;
    const template = document.getElementById("medicine-options-template");
    const optionsHtml = template ? template.innerHTML : "";

    const tr = document.createElement("tr");
    tr.className = "pos-item-row";
    tr.innerHTML = `
        <td>
            <select name="medicineId[]" class="form-control pos-medicine-select" required onchange="onMedicineChange(this)">
                <option value="">-- Select Medicine --</option>
                ${optionsHtml}
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
    `;

    tbody.appendChild(tr);
}

function removePosRow(btn) {
    const row = btn.closest("tr");
    const tbody = row.parentElement;
    if (tbody.querySelectorAll("tr").length > 1) {
        row.remove();
        recalculatePosTotals();
    } else {
        alert("A bill must contain at least one line item.");
    }
}

function onMedicineChange(selectEl) {
    const medicineId = selectEl.value;
    const row = selectEl.closest("tr");
    const batchSelect = row.querySelector(".pos-batch-select");
    const stockBadge = row.querySelector(".batch-stock-badge");
    const priceInput = row.querySelector(".pos-price-input");
    const qtyInput = row.querySelector(".pos-qty-input");
    const lineTotalInput = row.querySelector(".pos-line-total");

    batchSelect.innerHTML = '<option value="">Loading...</option>';
    stockBadge.textContent = "-";
    stockBadge.className = "batch-stock-badge badge badge-neutral";
    priceInput.value = "0.00";
    qtyInput.value = "1";
    lineTotalInput.value = "0.00";

    if (!medicineId) {
        batchSelect.innerHTML = '<option value="">-- Select Batch --</option>';
        recalculatePosTotals();
        return;
    }

    // Context path lookup
    const contextPath = document.body.getAttribute("data-context") || "";
    fetch(`${contextPath}/sales?action=batches&medicineId=${medicineId}`)
        .then(response => response.json())
        .then(batches => {
            batchSelect.innerHTML = '<option value="">-- Select Batch --</option>';
            if (batches.length === 0) {
                batchSelect.innerHTML = '<option value="">No in-stock batches</option>';
                stockBadge.textContent = "Out of Stock";
                stockBadge.className = "batch-stock-badge badge badge-danger";
                return;
            }

            batches.forEach(b => {
                const opt = document.createElement("option");
                opt.value = b.id;
                opt.textContent = `${b.batchNumber} (Exp: ${b.expiryDate}, Stock: ${b.quantity})`;
                opt.dataset.price = b.sellingPrice;
                opt.dataset.stock = b.quantity;
                batchSelect.appendChild(opt);
            });

            // Auto-select first batch if available (FEFO)
            if (batches.length > 0) {
                batchSelect.selectedIndex = 1;
                onBatchChange(batchSelect);
            }
        })
        .catch(err => {
            console.error("Error loading batches:", err);
            batchSelect.innerHTML = '<option value="">Error loading</option>';
        });
}

function onBatchChange(batchSelect) {
    const row = batchSelect.closest("tr");
    const selectedOption = batchSelect.options[batchSelect.selectedIndex];
    const stockBadge = row.querySelector(".batch-stock-badge");
    const priceInput = row.querySelector(".pos-price-input");
    const qtyInput = row.querySelector(".pos-qty-input");

    if (selectedOption && selectedOption.dataset.price) {
        const price = parseFloat(selectedOption.dataset.price) || 0;
        const stock = parseInt(selectedOption.dataset.stock, 10) || 0;

        priceInput.value = price.toFixed(2);
        qtyInput.max = stock;
        stockBadge.textContent = `${stock} in stock`;
        stockBadge.className = stock <= 10 ? "batch-stock-badge badge badge-warning" : "batch-stock-badge badge badge-success";
        onQuantityChange(qtyInput);
    } else {
        priceInput.value = "0.00";
        stockBadge.textContent = "-";
        stockBadge.className = "batch-stock-badge badge badge-neutral";
        onQuantityChange(qtyInput);
    }
}

function onQuantityChange(qtyInput) {
    const row = qtyInput.closest("tr");
    const priceInput = row.querySelector(".pos-price-input");
    const lineTotalInput = row.querySelector(".pos-line-total");
    const batchSelect = row.querySelector(".pos-batch-select");

    const price = parseFloat(priceInput.value) || 0;
    let qty = parseInt(qtyInput.value, 10) || 0;

    const selectedOption = batchSelect.options[batchSelect.selectedIndex];
    if (selectedOption && selectedOption.dataset.stock) {
        const stock = parseInt(selectedOption.dataset.stock, 10) || 0;
        if (qty > stock) {
            alert(`Requested quantity (${qty}) exceeds available stock (${stock}).`);
            qty = stock;
            qtyInput.value = stock;
        }
    }

    const total = price * qty;
    lineTotalInput.value = total.toFixed(2);
    recalculatePosTotals();
}

function recalculatePosTotals() {
    let subtotal = 0;
    document.querySelectorAll(".pos-line-total").forEach(function (el) {
        subtotal += parseFloat(el.value) || 0;
    });

    const discountInput = document.getElementById("pos-discount");
    const taxInput = document.getElementById("pos-tax");
    const subtotalDisplay = document.getElementById("pos-subtotal-display");
    const subtotalHidden = document.getElementById("pos-subtotal-hidden");
    const totalDisplay = document.getElementById("pos-total-display");

    const discount = discountInput ? (parseFloat(discountInput.value) || 0) : 0;
    const tax = taxInput ? (parseFloat(taxInput.value) || 0) : 0;

    const grandTotal = Math.max(0, subtotal - discount + tax);

    if (subtotalDisplay) subtotalDisplay.textContent = "₹" + subtotal.toFixed(2);
    if (subtotalHidden) subtotalHidden.value = subtotal.toFixed(2);
    if (totalDisplay) totalDisplay.textContent = "₹" + grandTotal.toFixed(2);
}

// ─────────────────────────────────────────────────────────────
// Purchase Multi-item Row Addition
// ─────────────────────────────────────────────────────────────
function initPurchaseCalculations() {
    const purchaseTable = document.getElementById("purchase-items-table");
    if (!purchaseTable) return;
    recalculatePurchaseTotals();
}

function addPurchaseRow() {
    const tbody = document.getElementById("purchase-items-body");
    if (!tbody) return;

    const template = document.getElementById("medicine-options-template");
    const optionsHtml = template ? template.innerHTML : "";

    const tr = document.createElement("tr");
    tr.className = "purchase-item-row";
    tr.innerHTML = `
        <td>
            <select name="medicineIds" class="form-control" required>
                <option value="">-- Select Medicine --</option>
                ${optionsHtml}
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
            <input type="number" name="purchasePrices" class="form-control purchase-cost-input" step="0.01" min="0.01" required oninput="onPurchasePriceChange(this)">
        </td>
        <td>
            <input type="number" name="sellingPrices" class="form-control" step="0.01" min="0.01" required>
        </td>
        <td>
            <input type="number" name="quantities" class="form-control purchase-qty-input" min="1" value="10" required oninput="onPurchasePriceChange(this)">
        </td>
        <td>
            <input type="text" class="form-control purchase-line-total" readonly value="0.00" style="background-color: #f8fafc; font-weight: 600;">
        </td>
        <td style="text-align: center;">
            <button type="button" class="btn btn-danger btn-sm" onclick="removePurchaseRow(this)" title="Remove item">✕</button>
        </td>
    `;
    tbody.appendChild(tr);
}

function removePurchaseRow(btn) {
    const row = btn.closest("tr");
    const tbody = row.parentElement;
    if (tbody.querySelectorAll("tr").length > 1) {
        row.remove();
        recalculatePurchaseTotals();
    } else {
        alert("A purchase order must contain at least one line item.");
    }
}

function onPurchasePriceChange(input) {
    const row = input.closest("tr");
    const costInput = row.querySelector(".purchase-cost-input");
    const qtyInput = row.querySelector(".purchase-qty-input");
    const lineTotalInput = row.querySelector(".purchase-line-total");

    const cost = parseFloat(costInput.value) || 0;
    const qty = parseInt(qtyInput.value, 10) || 0;
    lineTotalInput.value = (cost * qty).toFixed(2);
    recalculatePurchaseTotals();
}

function recalculatePurchaseTotals() {
    let total = 0;
    document.querySelectorAll(".purchase-line-total").forEach(function (el) {
        total += parseFloat(el.value) || 0;
    });

    const totalDisplay = document.getElementById("purchase-total-display");
    if (totalDisplay) totalDisplay.textContent = "₹" + total.toFixed(2);
}
