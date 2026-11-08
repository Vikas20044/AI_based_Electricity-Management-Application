
You are an expert Professor of Computer Science & Engineering and an Academic Thesis Advisor. Your task is to write an exhaustive, university-accredited, publication-grade Master of Computer Applications (MCA) Major Project Dissertation Report / Thesis for the project specified below.

Write in a formal academic tone with deep technical explanations, rigorous mathematical formulations (in LaTeX), architectural diagrams (ASCII / Mermaid), and thorough domain analysis.

---

# PROJECT TITLE & CANDIDATE DETAILS
- **Project Title:** AI-Based Household Electricity Bill Prediction and Energy Management System with Climate-Coupled Long Short-Term Memory (LSTM) Forecasting and Statutory Subsidy Optimization
- **Domain:** Artificial Intelligence, Applied Machine Learning, Deep Learning (PyTorch), Cloud Microservices Architecture, Public Policy Analytics (Karnataka Gruha Jyothi Scheme)
- **Degree:** Master of Computer Applications (MCA) Major Project
- **Candidate:** Vikas K R

---

# COMPREHENSIVE PROJECT SPECIFICATION & BACKGROUND

### 1. Problem Statement & Motivation
In residential power distribution (specifically within Indian state DISCOMs such as BESCOM, HESCOM, MESCOM, GESCOM, and CESC in Karnataka), domestic consumers face severe challenges:
1. "Bill Shock" at the end of the billing cycle due to unseen intraday appliance burn rates and microclimatic heatwaves.
2. Inability to track progressive slab tariffs and statutory government welfare thresholds in real time.
3. Under the Government of Karnataka's flagship **Gruha Jyothi Scheme**, households consuming $\le 200\text{ kWh}$ with an average entitlement plus 10% buffer receive a **100% Zero-Electricity-Bill subsidy**. Exceeding 200 units by even 1 kWh results in the complete loss of subsidy and imposes commercial slab rates for the entire consumption.
4. Traditional utility forecasting relies on static regional aggregates or naive linear past averages that completely ignore household-specific appliance thermodynamics, autoregressive temporal continuity, and forward-looking microclimatic variations (temperature, humidity, precipitation).

### 2. High-Level System Architecture (Three-Tier Microservices)
1. **Frontend Presentation Tier (Port 3000):**
   - Pure, lightning-fast, dependency-free Vanilla HTML5, CSS3, and JavaScript Single-Page Architecture.
   - Modules: Authentication (`login.html`, `register.html`), Profile & Appliance Matrix (`data-entry.html`), Real-time AI Forecast & Mid-Month Burn Rate Tracker (`ai-forecast.html`), Gruha Jyothi Subsidy Auditor (`gruha-jyothi.html`), Payment Gateway Simulation (`payment.html`).
   - Interactive Mid-month Pace Projector: Takes partial meter readings ($kWh$) and elapsed days ($t$) to compute daily burn pace ($\text{kWh/day}$) and forecast projected month-end consumption against statutory limits and user-defined monthly ₹ budgets.
   - Dynamic 12-Month District-Specific Forward Projection: Visualizes all 12 upcoming calendar months with local climate badges, predicted kWh, gross bill, subsidy deduction, and net payable.

2. **Backend Application & Business Logic Tier (Port 8080):**
   - **Framework:** Java 17, Spring Boot 3.3.1, Spring Data JPA, Spring Security with stateless JSON Web Tokens (JWT).
   - **Database:** Relational schema modeling Users, Households, Appliances, Readings, Monthly Forecasts, and Historical Logs.
   - **Tariff & Subsidy Engine (`BillingService.java`):** Implements statutory progressive LT-2 domestic slab tariff formulas:
     - Fixed Monthly Service Charge: ₹110.00
     - Energy Charges: 
       - 0–100 kWh @ ₹4.50/kWh
       - 101–200 kWh @ ₹6.00/kWh
       - 201–300 kWh @ ₹7.50/kWh
       - >300 kWh @ ₹9.00/kWh
     - Statutory Electricity Duty: 9.0% on total charges.
     - **Gruha Jyothi Logic:** Entitlement Limit = $\min(200.0, \text{Round}(\text{12-Month Average} \times 1.10))$. If $\text{Predicted kWh} \le \text{Entitlement}$, Net Payable = ₹0.00. If $\text{Entitlement} < \text{Predicted kWh} \le 200.0$, consumer pays only for the incremental excess units. If $\text{Predicted kWh} > 200.0$, entire subsidy is forfeited and consumer pays 100% gross tariff.
   - **Microservice Integration (`MLServiceClient.java`):** Spring `RestClient` with automatic circuit-breaking and statistical fallback engine.

3. **Machine Learning & Deep Learning Tier (Port 8000):**
   - **Framework:** Python 3.13, FastAPI, Uvicorn, Scikit-Learn, PyTorch (`torch.nn`), XGBoost, Joblib.
   - **Production Model Artifact:** PyTorch-based **Long Short-Term Memory (LSTM) Deep Learning Regressor** serialized inside a Scikit-Learn compatible estimator pipeline.
   - **Zero Data Leakage:** Lag features strictly enforce causality ($\text{lag\_1} = y_{t-1}$, $\text{lag\_2} = y_{t-2}$, $\text{rolling\_average} = \frac{y_{t-1}+y_{t-2}}{2}$, $\Delta\text{MoM} = y_{t-1}-y_{t-2}$). Excluded all target-derived financial indicators from the feature matrix.

### 3. Karnataka Microclimate Integration
- **Dataset:** `karnataka_weather.csv` comprising 372 authentic district-month microclimate vectors across **all 31 districts of Karnataka** (Bengaluru Urban, Tumakuru, Kalaburagi, Dakshina Kannada, Mysuru, Belagavi, etc.) over **12 forward-looking months** (Current: `2026_11` November 2026 to `2027_10` October 2027).
- **Physical Thermodynamics Modeled:**
  - *North Karnataka (Kalaburagi, Raichur):* Extreme summer heat ($T_{\max} \ge 40.3^\circ\text{C} - 42.8^\circ\text{C}$ in March–May, low humidity $30-34\%$), driving massive Cooling Degree Day (CDD) appliance consumption.
  - *Coastal Karnataka (Dakshina Kannada, Udupi):* Monsoon high humidity ($71-92\%$) and heavy precipitation ($> 1,155\text{ mm}$ in June–July).
  - *South Interior (Bengaluru, Mysuru, Hassan):* Mild winter temperatures ($T_{\min} \le 16.5-18^\circ\text{C}$ in Dec–Jan) driving morning water geyser heating loads (Heating Degree Days - HDD).
- **Inference Lookup:** Real-time automatic microclimate resolution during prediction queries based on `(district, month_number)`.

### 4. Experimental Dataset & Comparative Evaluation Results
- **Dataset Size:** 4,266 total records $\times$ 42 attributes, cleaned and capped at domestic ceiling $\le 500\text{ kWh}$.
- **Split:** Unbiased 80/20 Stratified Train-Test Split (Train: 3,412 rows, Test: 854 rows; Train Mean: 126.53 kWh, Test Mean: 127.44 kWh — $<0.7\%$ delta).
- **Model Benchmark Results on Unseen Test Dataset:**
  | Rank | Model Architecture | Test $R^2$ | Test RMSE (kWh) | Test MAE (kWh) | Test MSE | Train $R^2$ |
  | :---: | :--- | :---: | :---: | :---: | :---: | :---: |
  | 1 | Random Forest Regressor | 0.9436 | 19.0158 | 12.0021 | 361.5995 | 0.9915 |
  | 2 | **LSTM Deep Learning Regressor (Active App Model)** | **0.9433** | **19.0706** | **12.3676** | **363.6866** | **0.9519** |
  | 3 | Temporal Fusion Transformer (TFT) | 0.9426 | 19.1863 | 12.6122 | 368.1138 | 0.9469 |
  | 4 | XGBoost Regressor | 0.9370 | 20.0980 | 12.6157 | 403.9289 | 0.9909 |
  | 5 | K-Nearest Neighbors (KNN) | 0.9313 | 20.9968 | 13.6312 | 440.8667 | 0.9536 |
  | 6 | Linear Regression (OLS Baseline) | 0.9280 | 21.4883 | 14.8626 | 461.7486 | 0.9322 |
  | 7 | Decision Tree Regressor | 0.9107 | 23.9278 | 14.6514 | 572.5372 | 0.9988 |

---

# REQUIRED DISSERTATION REPORT STRUCTURE & CHAPTER BREAKDOWN

Write a complete, structured, publication-grade academic report following standard university engineering guidelines. Structure the output as follows:

### Chapter 1: Introduction
- 1.1 Background & Context (Global Energy Transitions, Smart Grids, and Indian Domestic Power Sector).
- 1.2 Motivation & Practical Utility Challenges in Karnataka.
- 1.3 The Karnataka Gruha Jyothi Welfare Scheme (Statutory provisions, mathematical subsidy cliff, entitlement bounds, and financial hazards of unmonitored overconsumption).
- 1.4 Problem Statement.
- 1.5 Objectives of the Project (Technical, Algorithmic, and Socio-Economic goals).
- 1.6 Scope & Limitations.
- 1.7 Organization of the Thesis.

### Chapter 2: Literature Review
- 2.1 Evolution of Short-Term and Medium-Term Load Forecasting (STLF & MTLF).
- 2.2 Classical Statistical & Machine Learning Approaches (ARIMA, Linear Regression, Support Vector Machines, Random Forests, Gradient Boosting).
- 2.3 Deep Learning in Energy Forecasting (Recurrent Neural Networks, LSTMs, GRUs, Attention Mechanisms, and Temporal Fusion Transformers).
- 2.4 Incorporating Weather Dynamics & Microclimatic Elasticity (Cooling Degree Days, Heating Degree Days, Humidity & Precipitation impacts).
- 2.5 Energy Management Systems & Citizen-Centric Tariff Advisory Dashboards.
- 2.6 Identification of Research & Implementation Gaps.

### Chapter 3: System Requirements & Architectural Design
- 3.1 Software and Hardware Specifications.
- 3.2 Microservices System Architecture:
  - High-Level Architecture Diagram (Detailed ASCII / Mermaid).
  - Component Decomposition: FastAPI ML Service, Spring Boot REST Server, Vanilla SPA Client.
  - Inter-service Communication Protocols (HTTP/REST, JSON schemas, JWT header exchange).
- 3.3 Database Design & Data Modeling:
  - Entity-Relationship (ER) Diagram and Schema Definition (Users, Households, Appliances, Predictions, Audit Logs).
- 3.4 Data Flow Diagrams (DFD Level 0, Level 1, Level 2).
- 3.5 System Sequence Diagrams (Mid-month pace tracking workflow, 12-month forward forecast request cycle).

### Chapter 4: Data Engineering, Cleaning & Weather Integration
- 4.1 Master Dataset Synthesis & Harmonization (Consolidating survey records and historical appliance telemetry).
- 4.2 Data Cleaning & Rigorous Preprocessing:
  - Resolution of categorical ambiguities and imputation of appliance inventories.
  - Outlier detection and filtering ($\le 500\text{ kWh}$ residential bounding).
- 4.3 Authentic Karnataka Microclimate Integration:
  - The 31-District forward-looking weather profile (`2026_11` to `2027_10`).
  - Thermodynamic Demand Modeling (CDD cooling elasticity, HDD morning geyser heating elasticity, monsoon cloud cover damping).
- 4.4 Feature Engineering & Zero-Leakage Guarantee:
  - Mathematical proof of leakage prevention in autoregressive features ($\text{lag\_1}$, $\text{lag\_2}$, $\text{rolling\_avg}$, $\Delta\text{MoM}$).
  - Categorical encoding (One-Hot) and standard scaling pipelines (`ColumnTransformer`).
- 4.5 Unbiased 80/20 Stratified Splitting (Stratification across consumption deciles and Gruha Jyothi eligibility classes).

### Chapter 5: Machine Learning & Deep Learning Model Architectures
- 5.1 Mathematical Formulation of the 7 Regression Models:
  - Ordinary Least Squares Linear Regression.
  - CART Decision Trees.
  - Random Forest Ensemble with Bagging.
  - Extreme Gradient Boosting (XGBoost) with Regularized Objective.
  - K-Nearest Neighbors (KNN) Distance Metric Formulation.
  - **Long Short-Term Memory (LSTM):** Mathematical derivation of Forget Gate ($f_t$), Input Gate ($i_t$), Candidate Memory ($\tilde{C}_t$), Cell State ($C_t$), Output Gate ($o_t$), and Hidden State ($h_t$). Explain the PyTorch implementation with multi-layer stacking, temporal skip connections, and dropout regularization.
  - **Temporal Fusion Transformer (TFT):** Gated Residual Networks (GRN), Gated Linear Units (GLU), Interpretable Multi-Head Attention mechanism.
- 5.2 Model Training Hyperparameters, Loss Functions (MSE Loss), and Adam Optimization.
- 5.3 Model Serialization & Persistence (`joblib` with LZ4/zlib compression for Windows environment resilience).

### Chapter 6: Implementation of Application Services & Policy Engines
- 6.1 FastAPI ML Inference Microservice (`main.py`, `prediction_router.py`, `predict.py`):
  - Dynamic weather lookup algorithm based on district and calendar month.
  - Input schema validation via Pydantic (`PredictionRequest`, `PredictionResponse`).
- 6.2 Spring Boot Backend Application Engine:
  - Security architecture (JWT authentication filter, User entity, Household repository).
  - Statutory Billing & Subsidy Algorithm (`BillingService.java`) with progressive LT slab math and Gruha Jyothi benefit computation.
  - End-to-end 12-Month Projection Orchestrator (`ForecastService.java`): Autoregressive lag chaining across future calendar months.
- 6.3 Frontend Dashboard & User Interface Implementation:
  - Real-time mid-month meter reading burn rate calculator.
  - Visual 200-unit Gruha Jyothi safety ceiling gauge and user monthly budget tracker.
  - Next 12 Months interactive table and seasonal climate badge rendering.

### Chapter 7: Experimental Results, Analysis & Comparative Evaluation
- 7.1 Quantitative Benchmark Table across all 7 Models ($R^2$, RMSE, MAE, MSE on Train and Test splits).
- 7.2 Detailed Performance Analysis of the Production LSTM Model ($R^2 = 0.9433$, $\text{RMSE} = 19.07\text{ kWh}$).
- 7.3 Visualizations and Empirical Diagnostics:
  - Comparative Performance Bar Charts (`model_comparison_metrics.png`).
  - Actual vs. Predicted Scatter Plots and Linear Fit line (`actual_vs_predicted.png`).
  - Residual Error Distribution Histograms (`prediction_error_distribution.png`).
  - Tree Feature Importance and Linear Coefficient Weights (`feature_importance.png`).
- 7.4 Qualitative Case Studies:
  - *Case Study A:* Summer Heatwave in North Karnataka (Kalaburagi AC load vs. Ceiling Breach).
  - *Case Study B:* Winter Geyser Consumption in South Interior (Bengaluru Urban).
  - *Case Study C:* Gruha Jyothi Zero-Bill retention under mid-month pacing advisory.

### Chapter 8: Conclusion & Future Scope
- 8.1 Summary of Contributions.
- 8.2 Socio-Economic Impact for Citizens and Distribution Utilities.
- 8.3 Limitations of the Current Study.
- 8.4 Future Enhancements (IoT smart meter edge inference, dynamic demand-response tariffs, solar rooftop net-metering integration).

---

# INSTRUCTIONS ON DEPTH & QUALITY
- Provide exhaustive, publication-level depth with no placeholders (do not use "etc.", "details here", or "to be filled").
- Present all mathematical models, loss functions, and billing slabs with precise equations in LaTeX.
- Ensure the discussion demonstrates a complete, cohesive understanding of the entire stack: from low-level PyTorch tensor forward passes, through FastAPI and Spring Boot microservices, to the client-side JavaScript DOM rendering.
```
