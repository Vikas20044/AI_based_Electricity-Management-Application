document.addEventListener("DOMContentLoaded", async () => {
  await populateHouseholdDefaults();
  setupPredictionForm();
  renderLastPrediction();
});

async function populateHouseholdDefaults() {
  const hh = await ApiClient.getHousehold();
  if (!hh) return;

  const stateSelect = document.getElementById("predState");
  const districtSelect = document.getElementById("predDistrict");
  const dwellingSelect = document.getElementById("predDwelling");
  const sizeInput = document.getElementById("predHouseholdSize");

  if (stateSelect) stateSelect.value = hh.state || "Karnataka";
  if (districtSelect) districtSelect.value = hh.district || "Tumakuru";
  if (dwellingSelect) dwellingSelect.value = hh.dwellingType || "Apartment/flat";
  if (sizeInput) sizeInput.value = hh.householdSize || 4;
}

function setupPredictionForm() {
  const form = document.getElementById("predictionForm");
  if (!form) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const partialKwh = parseFloat(document.getElementById("predPartialKwh").value);
    const daysElapsed = parseInt(document.getElementById("predDaysElapsed").value, 10);
    const season = document.getElementById("predSeason").value;
    const temperature = parseFloat(document.getElementById("predTemperature").value) || 28.5;
    const humidity = parseFloat(document.getElementById("predHumidity").value) || 58.0;
    const rainfall = parseFloat(document.getElementById("predRainfall").value) || 30.0;
    const prevKwh = parseFloat(document.getElementById("predPrevKwh").value) || 170.0;
    const prior3AvgKwh = parseFloat(document.getElementById("predPrior3Avg").value) || 160.0;

    if (isNaN(partialKwh) || partialKwh < 0) {
      alert("Please enter a valid partial consumption value in kWh.");
      return;
    }

    if (isNaN(daysElapsed) || daysElapsed < 1 || daysElapsed > 31) {
      alert("Billing days elapsed must be between 1 and 31.");
      return;
    }

    const submitBtn = document.getElementById("btnRunPrediction");
    const originalText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-2" role="status"></span>Evaluating Regression Models...`;

    try {

      const result = await ApiClient.predictConsumption({
        partialKwh: partialKwh,
        daysElapsed: daysElapsed,
        season: season,
        temperature: temperature,
        humidity: humidity,
        rainfall: rainfall,
        previousMonthKwh: prevKwh,
        prior3AvgKwh: prior3AvgKwh
      });

      renderPredictionResult(result);
    } catch (err) {
      alert("Prediction error: " + (err.message || "Failed to generate prediction."));
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalText;
    }
  });
}

function renderPredictionResult(result) {
  const resultCard = document.getElementById("predictionResultCard");
  if (!resultCard) return;

  resultCard.classList.remove("d-none");
  resultCard.scrollIntoView({ behavior: "smooth", block: "start" });

  document.getElementById("resPredictedKwh").textContent = `${result.predictedKwh.toFixed(1)} kWh`;
  document.getElementById("resCurrentKwh").textContent = `${result.currentKwh.toFixed(1)} kWh`;
  document.getElementById("resRemainingKwh").textContent = `${result.remainingKwh.toFixed(1)} kWh`;
  document.getElementById("resRunRate").textContent = `${result.dailyRunRate.toFixed(2)} kWh/day`;
  document.getElementById("resModelUsed").textContent = result.modelName;

  const entitled = 175.0; 
  const statusEl = document.getElementById("resSubsidyBadge");
  const impactEl = document.getElementById("resSubsidyImpact");

  if (result.predictedKwh <= entitled && result.predictedKwh <= 200) {
    statusEl.className = "badge-status badge-eligible";
    statusEl.innerHTML = `<i class="bi bi-shield-check"></i> Eligible for Zero Bill Subsidy`;
    impactEl.innerHTML = `Predicted usage is within your ${entitled} kWh entitlement limit. Estimated net bill is <strong>₹0 (Zero Bill)</strong>.`;
  } else if (result.predictedKwh > entitled && result.predictedKwh <= 200) {
    statusEl.className = "badge-status badge-warning";
    statusEl.innerHTML = `<i class="bi bi-exclamation-triangle"></i> Approaching Entitlement`;
    const excess = (result.predictedKwh - entitled).toFixed(1);
    impactEl.innerHTML = `Projected usage exceeds your ${entitled} kWh entitlement by <strong>${excess} units</strong>. You will pay for the excess units only (approx ₹${result.netPayableInr}).`;
  } else {
    statusEl.className = "badge-status badge-danger";
    statusEl.innerHTML = `<i class="bi bi-shield-x"></i> 200 kWh Ceiling Exceeded`;
    impactEl.innerHTML = `Projected usage exceeds the 200 units ceiling. You will be billed at regular commercial/residential tariffs for all units (approx ₹${result.estimatedBillInr}).`;
  }

  const pctUsed = Math.min(100, (result.currentKwh / result.predictedKwh) * 100);
  const progBar = document.getElementById("resProgressBar");
  if (progBar) {
    progBar.style.width = `${pctUsed}%`;
    progBar.textContent = `${pctUsed.toFixed(0)}% Consumed (${result.currentKwh} of ${result.predictedKwh} kWh)`;
  }
}

async function renderLastPrediction() {
  const summary = await ApiClient.getDashboardSummary();
  if (summary && summary.currentPrediction) {
    renderPredictionResult(summary.currentPrediction);
  }
}
