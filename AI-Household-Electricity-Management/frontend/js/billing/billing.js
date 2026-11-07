document.addEventListener("DOMContentLoaded", async () => {
  renderCurrentBillCard();
  renderBillsTable();
});

async function renderCurrentBillCard() {
  const summary = await ApiClient.getDashboardSummary();
  const pred = summary.currentPrediction || {};

  const grossEl = document.getElementById("billGrossAmount");
  const fixedEl = document.getElementById("billFixedCharges");
  const energyEl = document.getElementById("billEnergyCharges");
  const taxEl = document.getElementById("billTaxCharges");
  const subsidyEl = document.getElementById("billSubsidyDeduction");
  const netEl = document.getElementById("billNetAmount");

  const gross = pred.estimatedBillInr || 1395.0;
  const subsidy = pred.estimatedSubsidyBenefitInr || 1260.0;
  const net = pred.netPayableInr !== undefined ? pred.netPayableInr : 135.0;

  if (grossEl) grossEl.textContent = `₹${gross.toFixed(2)}`;
  if (fixedEl) fixedEl.textContent = `₹140.00`;
  if (energyEl) energyEl.textContent = `₹${(gross - 140 - (gross * 0.082)).toFixed(2)}`;
  if (taxEl) taxEl.textContent = `₹${(gross * 0.082).toFixed(2)}`;
  if (subsidyEl) subsidyEl.textContent = `-₹${subsidy.toFixed(2)}`;
  if (netEl) netEl.textContent = `₹${net.toFixed(2)}`;
}

async function renderBillsTable() {
  const tableBody = document.getElementById("billsTableBody");
  if (!tableBody) return;

  const bills = await ApiClient.getBills();
  if (bills.length === 0) {
    tableBody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">No billing records available.</td></tr>`;
    return;
  }

  tableBody.innerHTML = bills.map(b => `
    <tr>
      <td><strong>${b.month}</strong></td>
      <td>${b.unitsKwh.toFixed(1)} kWh</td>
      <td><span class="badge ${b.isEstimated ? 'bg-primary-subtle text-primary border border-primary' : 'bg-secondary-subtle text-dark border'}">${b.isEstimated ? 'AI Projection' : 'Official Actual Bill'}</span></td>
      <td>₹${(b.fixedCharges + b.energyCharges + b.tax).toFixed(0)}</td>
      <td class="text-success fw-semibold">-₹${b.subsidyAmount.toFixed(0)}</td>
      <td><strong class="text-dark">₹${b.netPayable.toFixed(0)}</strong></td>
      <td>
        <span class="badge ${b.status === 'PAID' ? 'bg-success' : 'bg-warning text-dark'}">
          ${b.status}
        </span>
      </td>
    </tr>
  `).join("");
}
