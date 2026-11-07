document.addEventListener("DOMContentLoaded", async () => {
  renderPaymentsTable();
  setupPaymentModal();
});

async function renderPaymentsTable() {
  const tableBody = document.getElementById("paymentsTableBody");
  if (!tableBody) return;

  const payments = await ApiClient.getPayments();
  if (payments.length === 0) {
    tableBody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">No transactions found.</td></tr>`;
    return;
  }

  tableBody.innerHTML = payments.map(p => `
    <tr>
      <td><span class="font-monospace small fw-bold">${p.ref}</span></td>
      <td>${p.month}</td>
      <td>${p.date}</td>
      <td><strong class="text-dark">₹${p.amount.toFixed(2)}</strong></td>
      <td><span class="badge bg-light text-dark border"><i class="bi bi-wallet2 me-1"></i>${p.method}</span></td>
      <td>
        <span class="badge ${p.status === 'SUCCESS' ? 'bg-success' : 'bg-danger'}">
          ${p.status}
        </span>
      </td>
    </tr>
  `).join("");
}

function setupPaymentModal() {
  const payForm = document.getElementById("mockPaymentForm");
  if (!payForm) return;

  payForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const method = document.querySelector('input[name="paymentMethod"]:checked').value;
    const amount = parseFloat(document.getElementById("payAmountInput").value);

    const btn = document.getElementById("btnConfirmPayment");
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Connecting to Gateway Sandbox...`;

    setTimeout(async () => {
      await ApiClient.recordPayment("bill-05-2024", amount, method);
      btn.disabled = false;
      btn.innerHTML = `<i class="bi bi-check-circle"></i> Pay Bill`;

      alert(`Payment of ₹${amount} via ${method} was successful!\nTransaction recorded.`);

      const modalEl = document.getElementById("paymentModal");
      if (modalEl) {
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();
      }

      renderPaymentsTable();
    }, 1200);
  });
}
