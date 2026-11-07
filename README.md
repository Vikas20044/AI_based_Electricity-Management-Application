# AI-Based Household Electricity Management Application

**Master of Computer Applications (MCA) – Major Project (2026–2027)**  
**Student**: Vikas K R (USN: 4MT25MC109)

---

## 1. Project Overview & Clean Architecture

The **AI-Based Household Electricity Management Application** provides a **simple, minimalist, black-and-white** utility forecasting and welfare assessment workflow:

1. **Nav Bar Removal**: Complex top navigation bars and distracting tabs have been replaced by a clean, lightweight app header and direct contextual action buttons for an intuitive, distraction-free user flow.
2. **Integrated Sign-Up & Data Entry**: All household connection parameters (Consumer Account No., Meter ID, District, Dwelling Type, Occupants, Sanctioned Load) and past electricity consumption baselines are collected directly during user registration (`register.html`).
3. **Strict Registration Enforced**: Users cannot log in without registering first. Pre-seeded demo bypasses have been removed to ensure complete data integrity.
4. **Autocomplete Disabled**: All form fields enforce `autocomplete="off"` to prevent browser caching or pre-filled input pollution.
5. **Mid-Month Forecasting**: The AI forecast system allows consumers to enter partial-month meter readings and days elapsed to evaluate intra-month burn rates and warn against Karnataka's 200 kWh ceiling or Gruha Jyothi entitlement breach.

The core **Machine Learning Microservice (Python FastAPI on Port 8000)** operates with pre-trained regression models to forecast electricity demand using 29 validated features.

---

## 2. Minimalist Architecture: The 4 Core Stages

### Stage 1: Registration with Household & Electricity Baseline (`register.html`)
- **User Account Credentials**: Full Name, Email Address, Mobile Number, Password.
- **Connection & Dwelling Details**: Consumer Account Number, Meter Identifier, District (Karnataka), Dwelling Architecture, Sanctioned Load (kW), and Occupants Count (People).
- **Consumption Baseline Entry**: Reference Past Billing Month (e.g., October 2026), Units Consumed (kWh), and Bill Paid (₹).
- **Entitlement Calculation**: Automatically computes the Gruha Jyothi baseline entitlement ($Baseline + 10\%$ buffer, max 200 kWh) in real time.
- Direct API integration with Spring Boot (`POST /api/v1/auth/register`).

### Stage 2: AI Electricity Forecasting & Mid-Month Tracking (`ai-forecast.html`)
- **Mid-Month Real-time Tracking**: Enter mid-cycle partial meter readings (kWh) and days elapsed to compute observed daily burn rate ($\text{kWh/day}$) and calibrate projected end-of-month demand.
- **ML Microservice Serving**: Inquires the Python FastAPI microservice (`POST /api/v1/predict` on Port 8000) using 29 trained features.
- **Core Forecast Metrics**:
  - AI Predicted Consumption (kWh)
  - Estimated Daily Burn Rate (kWh/day)
  - Gross Commercial Bill (KERC domestic tariff calculation)
  - Safety Margin to Karnataka's 200 kWh Ceiling
- **Minimalist Ceiling Meter**: High-contrast, clean monochrome tracking line comparing usage against 200 units.

### Stage 3: Karnataka Gruha Jyothi Scheme Assessment (`gruha-jyothi.html`)
- **Welfare Eligibility Evaluation**:
  - `ELIGIBLE: 100% ZERO BILL` (Usage $\le$ Entitlement and $\le 200\text{ kWh}$)
  - `APPROACHING THRESHOLD` (Usage > Entitlement, but $\le 200\text{ kWh}$; excess units billed)
  - `CEILING EXCEEDED` (Usage $> 200\text{ kWh}$; subsidy completely forfeited)
- **Financial Breakdown**: Gross Commercial Bill, Government Subsidy Deduction, and Net Payable by Consumer.
- **Interactive What-If Simulator**: Test arbitrary monthly units (e.g. 130 kWh, 175 kWh, 210 kWh) with instant real-time bill and subsidy consequences.

### Stage 4: Payment & Clearance Module (`payment.html`)
- **Outstanding Bill Settlement**: Review current billing cycle dues with itemized deductions.
- **Zero-Bill Waiver Voucher**: If net payable is ₹0.00, one click generates an official Zero-Bill Clearance Voucher under the Gruha Jyothi Scheme.
- **Instant Payment Gateway**: If net payable is > ₹0.00, consumers can pay via UPI (GPay/PhonePe), Debit/Credit Card, or Net Banking.
- **Printable Transaction Receipt**: Formatted receipt with Transaction Reference ID, Consumer Number, Timestamp, Amount Paid, and Print button (`window.print()`).
- **Recorded Transaction Ledger**: Persistent history of all past payments and clearance vouchers.

---

## 3. Technology Stack

- **Machine Learning**: Python 3.13, Scikit-learn, XGBoost, Pandas, Joblib (Port 8000)
- **ML Microservice**: Python FastAPI, Uvicorn, Pydantic (Port 8000)
- **Backend Core**: Java 17+, Spring Boot 3.3.4, Spring Data JPA, Spring Security with JWT (Port 8080)
- **Database**: PostgreSQL with embedded H2 fallback (PostgreSQL-compatibility mode)
- **Frontend Client**: Minimalist Black & White HTML5, CSS3, ES6+ JavaScript, Bootstrap 5.3 (Port 3000)

---

## 4. How to Run the Application

### 1-Click Master Launcher:
Double-click `start_all_services.bat` (or in PowerShell run `.\start_all_services.ps1`).

This launches:
- **FastAPI ML Microservice**: http://localhost:8000/docs
- **Spring Boot Backend**: http://localhost:8080/api/v1/health
- **Web Frontend**: http://localhost:3000 (Redirects to Login)

### User Flow:
1. Open http://localhost:3000 $\rightarrow$ redirects to `login.html`.
2. Click **"Register with Household Details"** to sign up and configure your household.
3. Once registered, sign in with your email and password.
4. View your **AI Forecast**, test **Mid-Month Tracking**, assess **Gruha Jyothi Eligibility**, and settle your bill or issue a **Zero-Bill Clearance Voucher**.
