document.addEventListener("DOMContentLoaded", async () => {
  renderSubsidyAssessment();
});

async function renderSubsidyAssessment() {
  const summary = await ApiClient.getDashboardSummary();
  const hh = summary.household || {};
  const pred = summary.currentPrediction || {};

  const entitledUnits = hh.entitledUnitsKwh || 175.0;
  const historicalAvg = hh.historical12MonthAvgKwh || 159.0;
  const projectedKwh = pred.predictedKwh || 192.8;

  const projectedKwhEl = document.getElementById("subsidyProjectedKwh");
  if (projectedKwhEl) projectedKwhEl.textContent = `${projectedKwh.toFixed(1)} kWh`;

  const entitledUnitsEl = document.getElementById("subsidyEntitledUnits");
  if (entitledUnitsEl) entitledUnitsEl.textContent = `${entitledUnits.toFixed(1)} kWh`;

  const historicalAvgEl = document.getElementById("subsidyHistoricalAvg");
  if (historicalAvgEl) historicalAvgEl.textContent = `${historicalAvg.toFixed(1)} kWh/mo`;

  const statusBadgeEl = document.getElementById("subsidyStatusBadge");
  const bannerEl = document.getElementById("subsidyStatusBanner");
  const detailEl = document.getElementById("subsidyDetailedAdvice");
  const meterFillEl = document.getElementById("subsidyMeterFill");

  const pctOfCeiling = Math.min(100, (projectedKwh / 200.0) * 100);
  if (meterFillEl) {
    meterFillEl.style.width = `${pctOfCeiling}%`;
  }

  if (projectedKwh <= entitledUnits) {

    if (statusBadgeEl) {
      statusBadgeEl.className = "badge-status badge-eligible fs-6 py-2 px-3";
      statusBadgeEl.innerHTML = `<i class="bi bi-shield-check"></i> Likely to Retain 100% Free Electricity`;
    }
    if (bannerEl) {
      bannerEl.className = "alert alert-success d-flex align-items-center gap-3";
      bannerEl.innerHTML = `
        <i class="bi bi-check-circle-fill fs-3 text-success"></i>
        <div>
          <h6 class="alert-heading mb-1 fw-bold">Zero Bill Entitlement Secured</h6>
          <p class="mb-0 small">Your projected consumption of <strong>${projectedKwh.toFixed(1)} kWh</strong> is well within your entitled limit of <strong>${entitledUnits} kWh</strong>.</p>
        </div>
      `;
    }
    if (detailEl) {
      detailEl.innerHTML = `
        <p>Under Karnataka's Gruha Jyothi Scheme rules, households consuming up to their personalized monthly entitlement (calculated as past 12 months average + 10%) receive a <strong>100% subsidy</strong> on energy charges and fixed charges.</p>
        <p class="mb-0 text-success fw-bold">Estimated Net Payable: ₹0.00 (Zero Electricity Bill)</p>
      `;
    }
    if (meterFillEl) meterFillEl.style.backgroundColor = "#10b981";

  } else if (projectedKwh > entitledUnits && projectedKwh <= 200) {

    const excess = (projectedKwh - entitledUnits).toFixed(1);
    if (statusBadgeEl) {
      statusBadgeEl.className = "badge-status badge-warning fs-6 py-2 px-3";
      statusBadgeEl.innerHTML = `<i class="bi bi-exclamation-triangle-fill"></i> Approaching Entitlement Limit (Excess Billed)`;
    }
    if (bannerEl) {
      bannerEl.className = "alert alert-warning d-flex align-items-center gap-3";
      bannerEl.innerHTML = `
        <i class="bi bi-exclamation-triangle-fill fs-3 text-warning"></i>
        <div>
          <h6 class="alert-heading mb-1 fw-bold">Consumption Pace Exceeds Historical Entitlement</h6>
          <p class="mb-0 small">Projected usage (<strong>${projectedKwh.toFixed(1)} kWh</strong>) exceeds your entitlement limit of <strong>${entitledUnits} kWh</strong> by approximately <strong>${excess} units</strong>.</p>
        </div>
      `;
    }
    if (detailEl) {
      detailEl.innerHTML = `
        <p>Because your total consumption remains under the <strong>200 units state ceiling</strong>, you will not lose scheme enrollment. However, you will be billed for the <strong>excess ${excess} units</strong> at standard domestic slab rates (approx ₹7.00/unit + tax).</p>
        <p class="mb-0 text-primary fw-bold">Estimated Net Payable: approx ₹${pred.netPayableInr || 135} (Government covers ₹${pred.estimatedSubsidyBenefitInr || 1260})</p>
      `;
    }
    if (meterFillEl) meterFillEl.style.backgroundColor = "#f59e0b";

  } else {

    if (statusBadgeEl) {
      statusBadgeEl.className = "badge-status badge-danger fs-6 py-2 px-3";
      statusBadgeEl.innerHTML = `<i class="bi bi-shield-x"></i> Benefit Risk: Exceeds 200 kWh State Ceiling`;
    }
    if (bannerEl) {
      bannerEl.className = "alert alert-danger d-flex align-items-center gap-3";
      bannerEl.innerHTML = `
        <i class="bi bi-x-octagon-fill fs-3 text-danger"></i>
        <div>
          <h6 class="alert-heading mb-1 fw-bold">Exceeding 200 Units Ceiling</h6>
          <p class="mb-0 small">Projected usage of <strong>${projectedKwh.toFixed(1)} kWh</strong> surpasses the Karnataka Gruha Jyothi 200-unit ceiling.</p>
        </div>
      `;
    }
    if (detailEl) {
      detailEl.innerHTML = `
        <p>When a household consumes more than 200 kWh in any billing cycle, <strong>no subsidy deduction is applied</strong>. The consumer is billed for the entire consumption at full residential rates.</p>
        <p class="mb-0 text-danger fw-bold">Estimated Net Payable: approx ₹${pred.estimatedBillInr || 1550} (Full Bill Payable)</p>
      `;
    }
    if (meterFillEl) meterFillEl.style.backgroundColor = "#ef4444";
  }
}
