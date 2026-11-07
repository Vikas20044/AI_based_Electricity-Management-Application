document.addEventListener("DOMContentLoaded", async () => {
  const summary = await ApiClient.getDashboardSummary();
  renderKPIs(summary);
  renderCharts(summary);
  renderAlertsAndRecommendations(summary);
});

function renderKPIs(summary) {
  const pred = summary.currentPrediction || {};
  const hh = summary.household || {};

  const currentKwhEl = document.getElementById("kpiCurrentKwh");
  if (currentKwhEl) currentKwhEl.textContent = `${pred.currentKwh || 112.5} kWh`;

  const elapsedDaysEl = document.getElementById("kpiElapsedDays");
  if (elapsedDaysEl) elapsedDaysEl.textContent = `Day ${pred.elapsedDays || 18} of ${pred.totalDays || 31} (${pred.dailyRunRate || 6.25} kWh/day)`;

  const predictedKwhEl = document.getElementById("kpiPredictedKwh");
  if (predictedKwhEl) predictedKwhEl.textContent = `${pred.predictedKwh || 192.8} kWh`;

  const modelUsedEl = document.getElementById("kpiModelName");
  if (modelUsedEl) modelUsedEl.textContent = pred.modelName || "Linear Regression (R² = 0.6863)";

  const estimatedBillEl = document.getElementById("kpiEstimatedBill");
  if (estimatedBillEl) estimatedBillEl.textContent = `₹${pred.netPayableInr !== undefined ? pred.netPayableInr.toFixed(0) : "135"}`;

  const billSubtextEl = document.getElementById("kpiBillSubtext");
  if (billSubtextEl) {
    if (pred.netPayableInr === 0) {
      billSubtextEl.innerHTML = `<span class="text-success fw-bold"><i class="bi bi-check-circle"></i> 100% Gruha Jyothi Zero Bill</span>`;
    } else {
      billSubtextEl.innerHTML = `Gross: ₹${pred.estimatedBillInr || 1395} (Saved: ₹${pred.estimatedSubsidyBenefitInr || 1260})`;
    }
  }

  const schemeBadgeEl = document.getElementById("kpiSchemeBadge");
  const schemeDescEl = document.getElementById("kpiSchemeDesc");
  if (schemeBadgeEl && schemeDescEl) {
    const entitled = hh.entitledUnitsKwh || 175.0;
    const projected = pred.predictedKwh || 192.8;

    if (projected <= entitled && projected <= 200) {
      schemeBadgeEl.className = "badge-status badge-eligible";
      schemeBadgeEl.innerHTML = `<i class="bi bi-shield-check"></i> Likely to Retain Full Benefit`;
      schemeDescEl.textContent = `Within your entitlement of ${entitled} units/month.`;
    } else if (projected > entitled && projected <= 200) {
      schemeBadgeEl.className = "badge-status badge-warning";
      schemeBadgeEl.innerHTML = `<i class="bi bi-exclamation-triangle"></i> Approaching Entitlement`;
      schemeDescEl.textContent = `Pace exceeds ${entitled} units. Excess (~${(projected - entitled).toFixed(0)} units) will be billed.`;
    } else {
      schemeBadgeEl.className = "badge-status badge-danger";
      schemeBadgeEl.innerHTML = `<i class="bi bi-shield-x"></i> 200 kWh Ceiling At Risk`;
      schemeDescEl.textContent = `Projected usage exceeds 200 units ceiling. Subsidy risks cancellation.`;
    }
  }
}

function renderCharts(summary) {

  const trendCtx = document.getElementById("consumptionTrendChart");
  if (trendCtx) {
    const readings = (summary.readings || []).slice(0, 5).reverse();
    const labels = ["Jan", "Feb", "Mar", "Apr", "May (Actual)", "May (AI Forecast)"];
    const actualData = [155, 148, 162, 170, 112.5, null];
    const predictedData = [null, null, null, null, 112.5, 192.8];
    const thresholdLine = [175, 175, 175, 175, 175, 175];

    new Chart(trendCtx, {
      type: "line",
      data: {
        labels: labels,
        datasets: [
          {
            label: "Actual Consumption (kWh)",
            data: actualData,
            borderColor: "#0f2b48",
            backgroundColor: "rgba(15, 43, 72, 0.15)",
            borderWidth: 2.5,
            pointBackgroundColor: "#0f2b48",
            pointRadius: 4,
            tension: 0.25,
            fill: true
          },
          {
            label: "AI Projected Consumption (kWh)",
            data: predictedData,
            borderColor: "#2563eb",
            borderDash: [6, 4],
            backgroundColor: "rgba(37, 99, 235, 0.1)",
            borderWidth: 2.5,
            pointBackgroundColor: "#2563eb",
            pointRadius: 4,
            tension: 0.25,
            fill: false
          },
          {
            label: "Gruha Jyothi Entitlement (175 kWh)",
            data: thresholdLine,
            borderColor: "#10b981",
            borderWidth: 2,
            borderDash: [3, 3],
            pointRadius: 0,
            fill: false
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: "top", labels: { boxWidth: 14, font: { size: 12 } } },
          tooltip: {
            callbacks: {
              label: (context) => `${context.dataset.label}: ${context.raw !== null ? context.raw + " kWh" : "N/A"}`
            }
          }
        },
        scales: {
          y: {
            beginAtZero: false,
            min: 100,
            max: 220,
            grid: { color: "#f1f5f9" },
            title: { display: true, text: "Electricity Usage (kWh)" }
          },
          x: { grid: { display: false } }
        }
      }
    });
  }

  const applianceCtx = document.getElementById("applianceShareChart");
  if (applianceCtx) {
    const apps = summary.appliances || [];
    const labels = apps.map(a => a.name);
    const data = apps.map(a => a.monthlyKwh);

    new Chart(applianceCtx, {
      type: "doughnut",
      data: {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: ["#0f2b48", "#10b981", "#2563eb", "#f59e0b", "#ef4444", "#8b5cf6"],
          borderWidth: 2,
          borderColor: "#ffffff"
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: "bottom", labels: { boxWidth: 12, font: { size: 11 } } },
          tooltip: {
            callbacks: {
              label: (context) => `${context.label}: ${context.raw} kWh/mo`
            }
          }
        },
        cutout: "68%"
      }
    });
  }
}

function renderAlertsAndRecommendations(summary) {

  const alertContainer = document.getElementById("dashboardAlertsList");
  if (alertContainer) {
    const warnings = summary.warnings || [];
    if (warnings.length === 0) {
      alertContainer.innerHTML = `<div class="text-muted small p-2">No active warnings. Everything looks healthy!</div>`;
    } else {
      alertContainer.innerHTML = warnings.map(w => `
        <div class="p-3 mb-2 rounded border ${w.severity === 'WARNING' ? 'bg-warning-subtle border-warning' : 'bg-info-subtle border-info'}">
          <div class="d-flex align-items-center gap-2 mb-1">
            <i class="bi ${w.severity === 'WARNING' ? 'bi-exclamation-triangle-fill text-warning' : 'bi-info-circle-fill text-info'}"></i>
            <strong class="small">${w.title}</strong>
          </div>
          <div class="small text-secondary">${w.message}</div>
        </div>
      `).join("");
    }
  }

  const recContainer = document.getElementById("dashboardRecsList");
  if (recContainer) {
    const recs = summary.recommendations || [];
    recContainer.innerHTML = recs.map(r => `
      <div class="d-flex gap-3 p-3 border-bottom">
        <div class="stat-icon icon-success flex-shrink-0" style="width: 36px; height: 36px; font-size: 1rem;">
          <i class="bi bi-lightbulb"></i>
        </div>
        <div>
          <h6 class="mb-1 small fw-bold text-dark">${r.title}</h6>
          <p class="mb-0 text-muted small">${r.text}</p>
        </div>
      </div>
    `).join("");
  }
}
