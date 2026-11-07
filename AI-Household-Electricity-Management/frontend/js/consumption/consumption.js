document.addEventListener("DOMContentLoaded", async () => {
  renderReadingsTable();
  setupAddReadingForm();
  renderComparisonChart();
});

async function renderReadingsTable() {
  const tableBody = document.getElementById("readingsTableBody");
  if (!tableBody) return;

  const readings = await ApiClient.getReadings();
  if (readings.length === 0) {
    tableBody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">No meter readings recorded yet.</td></tr>`;
    return;
  }

  tableBody.innerHTML = readings.map(r => `
    <tr>
      <td><strong>${r.date}</strong></td>
      <td><span class="badge ${r.isPartial ? 'bg-primary-subtle text-primary border border-primary' : 'bg-success-subtle text-success border border-success'}">${r.isPartial ? 'Mid-Month Partial' : 'Completed Cycle'}</span></td>
      <td>${r.billingMonth}</td>
      <td>${r.daysElapsed} days</td>
      <td><strong class="text-dark">${r.consumptionKwh.toFixed(1)} kWh</strong></td>
      <td><span class="text-muted small">${r.type}</span></td>
    </tr>
  `).join("");
}

function setupAddReadingForm() {
  const form = document.getElementById("addReadingForm");
  if (!form) return;

  const dateInput = document.getElementById("readingDate");
  if (dateInput) {
    dateInput.value = new Date().toISOString().split("T")[0];
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const date = document.getElementById("readingDate").value;
    const kwh = parseFloat(document.getElementById("readingKwh").value);
    const days = parseInt(document.getElementById("readingDays").value, 10);
    const isPartial = document.getElementById("readingIsPartial").checked;

    if (isNaN(kwh) || kwh <= 0) {
      alert("Please enter a valid positive kWh consumption value.");
      return;
    }

    const monthStr = date.substring(0, 7);

    await ApiClient.addReading({
      date: date,
      billingMonth: monthStr,
      daysElapsed: days || 15,
      consumptionKwh: kwh,
      isPartial: isPartial
    });

    alert("Meter reading successfully logged!");
    form.reset();
    if (dateInput) dateInput.value = new Date().toISOString().split("T")[0];

    const modalEl = document.getElementById("addReadingModal");
    if (modalEl) {
      const modal = bootstrap.Modal.getInstance(modalEl);
      if (modal) modal.hide();
    }

    renderReadingsTable();
    renderComparisonChart();
  });
}

async function renderComparisonChart() {
  const ctx = document.getElementById("monthlyComparisonChart");
  if (!ctx) return;

  const readings = await ApiClient.getReadings();

  const monthlyData = [
    { month: "Jan 2024", kwh: 155.0, status: "Actual" },
    { month: "Feb 2024", kwh: 148.0, status: "Actual" },
    { month: "Mar 2024", kwh: 162.0, status: "Actual" },
    { month: "Apr 2024", kwh: 170.0, status: "Actual" },
    { month: "May 2024", kwh: 192.8, status: "Projected" }
  ];

  new Chart(ctx, {
    type: "bar",
    data: {
      labels: monthlyData.map(d => d.month),
      datasets: [
        {
          label: "Electricity Consumption (kWh)",
          data: monthlyData.map(d => d.kwh),
          backgroundColor: monthlyData.map(d => d.status === "Projected" ? "rgba(37, 99, 235, 0.75)" : "rgba(15, 43, 72, 0.85)"),
          borderColor: monthlyData.map(d => d.status === "Projected" ? "#2563eb" : "#0f2b48"),
          borderWidth: 1.5,
          borderRadius: 6
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: (ctx) => `${ctx.raw} kWh (${monthlyData[ctx.dataIndex].status})`
          }
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          max: 220,
          grid: { color: "#f1f5f9" },
          title: { display: true, text: "Total Monthly Units (kWh)" }
        },
        x: { grid: { display: false } }
      }
    }
  });
}
