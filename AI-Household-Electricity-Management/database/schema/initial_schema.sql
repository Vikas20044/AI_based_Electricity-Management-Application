-- =============================================================================
-- AI-BASED HOUSEHOLD ELECTRICITY MANAGEMENT APPLICATION
-- Streamlined PostgreSQL Database Schema
-- USN: 4MT25MC109 | Candidate: Vikas K R
-- =============================================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(25),
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_CONSUMER',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- 2. Households Table (Setup, Peoples, Past Month Bill, Gruha Jyothi Limits)
CREATE TABLE IF NOT EXISTS households (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    household_name VARCHAR(100) NOT NULL DEFAULT 'My Residence',
    consumer_number VARCHAR(50) NOT NULL UNIQUE,
    meter_number VARCHAR(50) DEFAULT 'MTR-BESCOM-8839',
    state VARCHAR(50) NOT NULL DEFAULT 'Karnataka',
    district VARCHAR(50) NOT NULL DEFAULT 'Tumakuru',
    area_type VARCHAR(30) NOT NULL DEFAULT 'Urban',
    dwelling_type VARCHAR(50) NOT NULL DEFAULT 'Apartment/flat',
    household_size INT NOT NULL DEFAULT 4, -- Number of household peoples/occupants
    sanctioned_load_kw DOUBLE PRECISION NOT NULL DEFAULT 2.0,
    past_month_name VARCHAR(50) DEFAULT 'Last Month',
    past_month_units_kwh DOUBLE PRECISION DEFAULT 160.0,
    past_month_bill_amount DOUBLE PRECISION DEFAULT 1120.0,
    historical_12_month_avg_kwh DOUBLE PRECISION NOT NULL DEFAULT 160.0,
    entitled_units_kwh DOUBLE PRECISION NOT NULL DEFAULT 176.0,
    scheme_registered BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_households_user_id ON households(user_id);
CREATE INDEX IF NOT EXISTS idx_households_consumer_no ON households(consumer_number);

-- 3. Appliances Inventory Table
CREATE TABLE IF NOT EXISTS appliances (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'General',
    wattage INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    daily_hours DOUBLE PRECISION NOT NULL DEFAULT 2.0,
    monthly_kwh DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_appliances_household_id ON appliances(household_id);

-- 4. Electricity Forecast Predictions Table
CREATE TABLE IF NOT EXISTS predictions (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    target_month VARCHAR(20) NOT NULL,
    predicted_monthly_kwh DOUBLE PRECISION NOT NULL,
    estimated_bill_inr DOUBLE PRECISION NOT NULL,
    gruha_jyothi_status VARCHAR(30) NOT NULL, -- ELIGIBLE, APPROACHING_THRESHOLD, EXCEEDED
    subsidy_benefit_inr DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    net_payable_inr DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    model_algorithm VARCHAR(100) DEFAULT 'LinearRegression',
    weather_temperature_c DOUBLE PRECISION,
    weather_humidity_pct DOUBLE PRECISION,
    season VARCHAR(50),
    prediction_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_predictions_hh_date ON predictions(household_id, prediction_date DESC);


-- 5. Payments Table
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    consumer_number VARCHAR(50) NOT NULL,
    billing_month VARCHAR(30) NOT NULL,
    transaction_reference VARCHAR(100) NOT NULL UNIQUE,
    amount_inr DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'UPI',
    payment_status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS',
    payment_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_consumer_no ON payments(consumer_number);
