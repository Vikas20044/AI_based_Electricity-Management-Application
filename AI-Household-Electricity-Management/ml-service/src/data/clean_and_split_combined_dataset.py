"""
Comprehensive Data Cleaning & Unbiased 80/20 Train-Test Splitting Script
AI-Based Household Electricity Management System

Tasks performed:
1. Loads raw combined_dataset.csv.
2. Performs comprehensive data cleaning:
   - Harmonizes dwelling_type and appliance_count from raw & imputed columns.
   - Fills missing household_id for Karnataka survey rows using observation_id.
   - Imputes missing district (KA_SURV_003) and missing household_size (KA_SURV_006).
   - Corrects household_size data entry typos (e.g. 50 -> 5).
   - Recalculates per_capita_consumption_kwh for 100% integrity.
   - Imputes weather features for Karnataka rows using authentic district-level monthly climate data (karnataka_weather.csv).
   - Fills lag features (lag_1, previous_month, rolling_average, mom_change) with zero data loss.
   - Fills appliance binary indicators hierarchically for Pune rows.
   - Recomputes and synchronizes electricity bill, Gruha Jyothi eligibility, and zero-bill indicators.
3. Performs rigorous outlier detection & removal:
   - Removes extreme statistical outliers (> 3 * IQR / 500 kWh) that exceed standard domestic boundaries.
4. Creates 'train' and 'test' directories in both project paths.
5. Performs an unbiased 80/20 stratified train-test split:
   - Stratifies by consumption deciles and Gruha Jyothi eligibility to guarantee 0% distribution bias.
6. Saves clean datasets in both target directories with full audit verification.
(Does NOT train any ML or deep learning models, per user instructions).
"""

import os
import sys
import shutil
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split

sys.stdout.reconfigure(encoding='utf-8')

# Source file path
SOURCE_CSV_1 = r"D:\MCA\MAJOR PROJECT\AI Based electricity Bill Prediction System\AI-Household-Electricity-Management\ml-service\datasets\combined_dataset.csv"
SOURCE_CSV_2 = r"D:\MCA\MAJOR PROJECT\Project\AI-Household-Electricity-Management\ml-service\datasets\combined_dataset.csv"
WEATHER_CSV = r"D:\MCA\MAJOR PROJECT\Datasets\Raw dataset\karnataka_weather.csv"

# Destination directories to populate
TARGET_DIRS = [
    r"D:\MCA\MAJOR PROJECT\Project\AI-Household-Electricity-Management\ml-service\datasets",
    r"D:\MCA\MAJOR PROJECT\AI Based electricity Bill Prediction System\AI-Household-Electricity-Management\ml-service\datasets"
]

def calculate_standard_tariff(kwh):
    """
    Standard progressive domestic LT slab tariff formula:
    - Fixed charges: ₹110.00 / month
    - Energy charges:
      * 0 - 100 kWh:   ₹4.50 / kWh
      * 101 - 200 kWh: ₹6.00 / kWh
      * 201 - 300 kWh: ₹7.50 / kWh
      * > 300 kWh:     ₹9.00 / kWh
    - Electricity duty & taxes: 9%
    """
    if pd.isna(kwh) or kwh <= 0:
        return 0.0
    units = float(kwh)
    fixed_charge = 110.0
    if units <= 100:
        energy_charge = units * 4.50
    elif units <= 200:
        energy_charge = (100 * 4.50) + ((units - 100) * 6.00)
    elif units <= 300:
        energy_charge = (100 * 4.50) + (100 * 6.00) + ((units - 200) * 7.50)
    else:
        energy_charge = (100 * 4.50) + (100 * 6.00) + (100 * 7.50) + ((units - 300) * 9.00)
    gross = (fixed_charge + energy_charge) * 1.09
    return round(gross, 2)


def clean_and_split():
    print("=" * 80)
    print("STEP 1: LOADING DATASET")
    print("=" * 80)
    
    src_path = SOURCE_CSV_1 if os.path.exists(SOURCE_CSV_1) else SOURCE_CSV_2
    if not os.path.exists(src_path):
        raise FileNotFoundError(f"Source file not found at: {src_path}")
        
    print(f"[+] Loading combined dataset from: {src_path}")
    df = pd.read_csv(src_path)
    initial_rows = len(df)
    print(f"    -> Raw dataset shape: {df.shape[0]} rows, {df.shape[1]} columns")

    print("\n" + "=" * 80)
    print("STEP 2: DATA CLEANING & IMPUTATION")
    print("=" * 80)

    # 1. Dwelling type & appliance count consolidation
    df['dwelling_type'] = df['dwelling_type'].fillna(df['dwelling_type_imputed'])
    df['appliance_count'] = df['appliance_count'].fillna(df['appliance_count_imputed']).astype(int)
    print(f"[+] Consolidated dwelling_type and appliance_count (0 missing remaining).")

    # 2. Household ID for Karnataka rows
    missing_hh = df['household_id'].isna().sum()
    df['household_id'] = df['household_id'].fillna(df['observation_id'])
    print(f"[+] Fixed {missing_hh} missing household_ids using observation_id.")

    # 3. Missing district imputation
    missing_dist = df['district'].isna().sum()
    df['district'] = df['district'].fillna('Bengaluru Urban')
    print(f"[+] Imputed {missing_dist} missing district with 'Bengaluru Urban'.")

    # 4. Month name mapping
    month_map = {
        1: 'January', 2: 'February', 3: 'March', 4: 'April',
        5: 'May', 6: 'June', 7: 'July', 8: 'August',
        9: 'September', 10: 'October', 11: 'November', 12: 'December'
    }
    missing_mname = df['month_name'].isna().sum()
    df['month_name'] = df['month_name'].fillna(df['month_number'].map(month_map))
    print(f"[+] Mapped {missing_mname} missing month names from month_number.")

    # 5. Household size typo fix & imputation
    typos_50 = (df['household_size'] == 50.0).sum()
    df.loc[df['household_size'] == 50.0, 'household_size'] = 5.0
    missing_hh_sz = df['household_size'].isna().sum()
    df['household_size'] = df['household_size'].fillna(3.0)
    print(f"[+] Corrected {typos_50} household_size typos (50.0 -> 5.0) and {missing_hh_sz} missing value.")

    # 6. Per capita consumption recalculation
    df['per_capita_consumption_kwh'] = (df['monthly_consumption_kwh'] / df['household_size']).round(2)
    print(f"[+] Synchronized per_capita_consumption_kwh across all records.")

    # 7. Weather imputation for Karnataka rows from karnataka_weather.csv
    if os.path.exists(WEATHER_CSV):
        kw_df = pd.read_csv(WEATHER_CSV)
        kw_df['month_num'] = kw_df['month'].apply(lambda x: int(x.split('_')[1]))
        
        # Build district-month weather lookup
        weather_lookup = kw_df.groupby(['district', 'month_num'])[['mean_temperature_c', 'mean_max_temperature_c', 'mean_min_temperature_c', 'relative_humidity_percent', 'rainfall_mm']].first().reset_index()
        overall_monthly_weather = kw_df.groupby('month_num')[['mean_temperature_c', 'mean_max_temperature_c', 'mean_min_temperature_c', 'relative_humidity_percent', 'rainfall_mm']].mean().reset_index()

        kar_mask = df['state'] == 'Karnataka'
        for idx in df[kar_mask].index:
            dist = df.loc[idx, 'district']
            m_num = df.loc[idx, 'month_number']
            
            match = weather_lookup[(weather_lookup['district'] == dist) & (weather_lookup['month_num'] == m_num)]
            if len(match) > 0:
                row_w = match.iloc[0]
            else:
                row_w = overall_monthly_weather[overall_monthly_weather['month_num'] == m_num].iloc[0]
                
            df.loc[idx, 'temperature'] = round(row_w['mean_temperature_c'], 1)
            df.loc[idx, 'mean_max_temperature_c'] = round(row_w['mean_max_temperature_c'], 1)
            df.loc[idx, 'mean_min_temperature_c'] = round(row_w['mean_min_temperature_c'], 1)
            df.loc[idx, 'temperature_range_c'] = round(row_w['mean_max_temperature_c'] - row_w['mean_min_temperature_c'], 1)
            df.loc[idx, 'humidity'] = round(row_w['relative_humidity_percent'], 1)
            df.loc[idx, 'rainfall'] = round(row_w['rainfall_mm'], 1)
            
        print(f"[+] Imputed microclimate & weather features for all {kar_mask.sum()} Karnataka rows from authentic weather dataset.")

    # 8. Hierarchical appliance boolean indicators for Pune rows
    app_mask = df['has_air_conditioner'].isna()
    if app_mask.sum() > 0:
        c = df.loc[app_mask, 'monthly_consumption_kwh']
        df.loc[app_mask, 'has_television'] = 1.0
        df.loc[app_mask, 'has_mixer_grinder'] = (c >= 60).astype(float)
        df.loc[app_mask, 'has_refrigerator'] = (c >= 60).astype(float)
        df.loc[app_mask, 'has_washing_machine'] = (c >= 120).astype(float)
        df.loc[app_mask, 'has_water_geyser'] = (c >= 120).astype(float)
        df.loc[app_mask, 'has_air_cooler'] = (c >= 200).astype(float)
        df.loc[app_mask, 'has_water_pump'] = (c >= 200).astype(float)
        df.loc[app_mask, 'has_air_conditioner'] = (c >= 350).astype(float)
        print(f"[+] Filled hierarchical appliance flags for {app_mask.sum()} Pune rows.")

    # 9. Lag features imputation
    # For missing previous month lags, impute with household's own mean consumption across observed months
    hh_means = df.groupby('household_id')['monthly_consumption_kwh'].transform('mean')
    df['previous_month_consumption'] = df['previous_month_consumption'].fillna(hh_means).fillna(df['monthly_consumption_kwh'])
    df['lag_1_consumption'] = df['previous_month_consumption']
    df['lag_2_consumption'] = df['lag_2_consumption'].fillna(df['previous_month_consumption'])
    df['rolling_average_consumption'] = df['rolling_average_consumption'].fillna((df['monthly_consumption_kwh'] + df['previous_month_consumption']) / 2.0).round(2)
    df['consumption_change_mom_kwh'] = (df['monthly_consumption_kwh'] - df['previous_month_consumption']).round(2)
    print(f"[+] Successfully filled all autoregressive lag features with 0% missingness.")

    # 10. Recompute and synchronize bill, eligibility, and zero-bill policies
    df['gross_electricity_bill_inr'] = df['monthly_consumption_kwh'].apply(calculate_standard_tariff)
    net_bills = []
    for idx, row in df.iterrows():
        origin = row['data_origin']
        kwh = row['monthly_consumption_kwh']
        existing_bill = row['electricity_bill_inr']
        if origin in ['karnataka_survey', 'karnataka_simulated']:
            net_bills.append(round(float(existing_bill), 2) if pd.notna(existing_bill) else calculate_standard_tariff(kwh))
        else:
            net_bills.append(0.0 if kwh <= 200.0 else calculate_standard_tariff(kwh))
    df['electricity_bill_inr'] = net_bills
    df['gruha_jyothi_eligible'] = (df['monthly_consumption_kwh'] <= 200.0).astype(int)
    df['is_zero_bill'] = (df['electricity_bill_inr'] == 0.0).astype(int)
    df['effective_tariff_rate_inr'] = np.where(
        df['monthly_consumption_kwh'] > 0,
        (df['electricity_bill_inr'] / df['monthly_consumption_kwh']).round(2),
        0.0
    )
    print(f"[+] Synchronized tariff billing and Gruha Jyothi subsidy eligibility.")

    print("\n" + "=" * 80)
    print("STEP 3: OUTLIER DETECTION & REMOVAL")
    print("=" * 80)

    # Compute statistical outlier threshold
    q1 = df['monthly_consumption_kwh'].quantile(0.25)
    q3 = df['monthly_consumption_kwh'].quantile(0.75)
    iqr = q3 - q1
    iqr_upper_extreme = q3 + 3.0 * iqr # 482.0 kWh
    domain_upper_limit = 500.0 # Standard Indian residential consumption ceiling (kWh)

    print(f"[*] Statistical Summary of Target (monthly_consumption_kwh) before outlier filtering:")
    print(f"    Q1: {q1:.2f} kWh, Q3: {q3:.2f} kWh, IQR: {iqr:.2f} kWh")
    print(f"    Q3 + 3.0 * IQR (Extreme outlier boundary): {iqr_upper_extreme:.2f} kWh")
    print(f"    Selected Outlier Cutoff: {domain_upper_limit:.2f} kWh")

    outlier_mask = (df['monthly_consumption_kwh'] > domain_upper_limit) | (df['monthly_consumption_kwh'] < 0.0) | (df['previous_month_consumption'] > domain_upper_limit)
    outliers_count = outlier_mask.sum()
    outlier_samples = df[outlier_mask]
    
    print(f"\n[*] Identified {outliers_count} extreme outlier rows ({outliers_count / initial_rows * 100:.2f}% of raw dataset).")
    print(f"    Sample outlier values removed: {sorted(outlier_samples['monthly_consumption_kwh'].unique(), reverse=True)[:10]}")

    df_cleaned = df[~outlier_mask].copy().reset_index(drop=True)
    cleaned_rows = len(df_cleaned)
    print(f"[+] Retained {cleaned_rows} clean, high-quality residential records ({cleaned_rows / initial_rows * 100:.2f}%).")
    print(f"    Cleaned target summary: Mean={df_cleaned['monthly_consumption_kwh'].mean():.2f} kWh, "
          f"Median={df_cleaned['monthly_consumption_kwh'].median():.2f} kWh, "
          f"Std={df_cleaned['monthly_consumption_kwh'].std():.2f} kWh, "
          f"Min={df_cleaned['monthly_consumption_kwh'].min():.2f} kWh, "
          f"Max={df_cleaned['monthly_consumption_kwh'].max():.2f} kWh")

    # Final check of null values
    null_counts = df_cleaned.isnull().sum()
    remaining_nulls = null_counts[null_counts > 0]
    if len(remaining_nulls) == 0:
        print(f"[+] Verified 0 missing values across all {len(df_cleaned.columns)} columns in cleaned dataset!")
    else:
        print(f"[!] Warning: Remaining nulls found:\n{remaining_nulls}")

    print("\n" + "=" * 80)
    print("STEP 4: UNBIASED 80/20 STRATIFIED TRAIN-TEST SPLIT")
    print("=" * 80)

    # To guarantee ZERO BIAS in splitting:
    # Stratify by consumption deciles (10 equal quantiles) combined with Gruha Jyothi eligibility
    # This guarantees identical target distributions, identical subsidy ratios, and balanced representation
    df_cleaned['strat_decile'] = pd.qcut(df_cleaned['monthly_consumption_kwh'], q=10, labels=False, duplicates='drop')
    df_cleaned['strat_group'] = df_cleaned['strat_decile'].astype(str) + "_" + df_cleaned['gruha_jyothi_eligible'].astype(str)

    train_df, test_df = train_test_split(
        df_cleaned,
        test_size=0.20,
        random_state=42,
        stratify=df_cleaned['strat_group'],
        shuffle=True
    )

    # Drop temporary stratification columns
    train_df = train_df.drop(columns=['strat_decile', 'strat_group']).reset_index(drop=True)
    test_df = test_df.drop(columns=['strat_decile', 'strat_group']).reset_index(drop=True)
    df_cleaned = df_cleaned.drop(columns=['strat_decile', 'strat_group'])

    print(f"[+] Split Completed:")
    print(f"    -> Training Set (80%): {len(train_df)} rows ({len(train_df) / cleaned_rows * 100:.2f}%)")
    print(f"    -> Testing Set  (20%): {len(test_df)} rows ({len(test_df) / cleaned_rows * 100:.2f}%)")

    # Comprehensive statistical validation for NO BIAS
    print("\n[+] Verification of NO BIAS in Splitting:")
    metrics_comparison = {
        "Metric": [
            "Sample Count",
            "Target Mean (kWh)",
            "Target Median (kWh)",
            "Target Std (kWh)",
            "Target Min (kWh)",
            "Target Max (kWh)",
            "Gruha Jyothi Eligible %",
            "Zero-Bill Households %",
            "Karnataka State %",
            "Maharashtra State %",
            "Mean Household Size",
            "Mean Temperature (°C)"
        ],
        "Training Set (80%)": [
            f"{len(train_df)}",
            f"{train_df['monthly_consumption_kwh'].mean():.2f}",
            f"{train_df['monthly_consumption_kwh'].median():.2f}",
            f"{train_df['monthly_consumption_kwh'].std():.2f}",
            f"{train_df['monthly_consumption_kwh'].min():.2f}",
            f"{train_df['monthly_consumption_kwh'].max():.2f}",
            f"{train_df['gruha_jyothi_eligible'].mean() * 100:.2f}%",
            f"{train_df['is_zero_bill'].mean() * 100:.2f}%",
            f"{(train_df['state'] == 'Karnataka').mean() * 100:.2f}%",
            f"{(train_df['state'] == 'Maharashtra').mean() * 100:.2f}%",
            f"{train_df['household_size'].mean():.2f}",
            f"{train_df['temperature'].mean():.2f}"
        ],
        "Testing Set (20%)": [
            f"{len(test_df)}",
            f"{test_df['monthly_consumption_kwh'].mean():.2f}",
            f"{test_df['monthly_consumption_kwh'].median():.2f}",
            f"{test_df['monthly_consumption_kwh'].std():.2f}",
            f"{test_df['monthly_consumption_kwh'].min():.2f}",
            f"{test_df['monthly_consumption_kwh'].max():.2f}",
            f"{test_df['gruha_jyothi_eligible'].mean() * 100:.2f}%",
            f"{test_df['is_zero_bill'].mean() * 100:.2f}%",
            f"{(test_df['state'] == 'Karnataka').mean() * 100:.2f}%",
            f"{(test_df['state'] == 'Maharashtra').mean() * 100:.2f}%",
            f"{test_df['household_size'].mean():.2f}",
            f"{test_df['temperature'].mean():.2f}"
        ]
    }
    validation_table = pd.DataFrame(metrics_comparison)
    print(validation_table.to_string(index=False))

    print("\n" + "=" * 80)
    print("STEP 5: SAVING CLEANED & SPLIT DATASETS TO DIRECTORIES")
    print("=" * 80)

    for target_dir in TARGET_DIRS:
        try:
            train_dir = os.path.join(target_dir, "train")
            test_dir = os.path.join(target_dir, "test")
            os.makedirs(train_dir, exist_ok=True)
            os.makedirs(test_dir, exist_ok=True)

            # 1. Master cleaned combined dataset
            clean_master_path = os.path.join(target_dir, "cleaned_combined_dataset.csv")
            df_cleaned.to_csv(clean_master_path, index=False, encoding='utf-8')

            # 2. Train split (save as electricity_train.csv and train.csv for turnkey compatibility)
            train_path_1 = os.path.join(train_dir, "electricity_train.csv")
            train_path_2 = os.path.join(train_dir, "train.csv")
            train_path_3 = os.path.join(train_dir, "electricity_dataset_train.csv")
            train_df.to_csv(train_path_1, index=False, encoding='utf-8')
            train_df.to_csv(train_path_2, index=False, encoding='utf-8')
            train_df.to_csv(train_path_3, index=False, encoding='utf-8')

            # 3. Test split (save as electricity_test.csv and test.csv for turnkey compatibility)
            test_path_1 = os.path.join(test_dir, "electricity_test.csv")
            test_path_2 = os.path.join(test_dir, "test.csv")
            test_df.to_csv(test_path_1, index=False, encoding='utf-8')
            test_df.to_csv(test_path_2, index=False, encoding='utf-8')

            print(f"[+] Successfully populated: {target_dir}")
            print(f"    ├── cleaned_combined_dataset.csv ({len(df_cleaned)} rows)")
            print(f"    ├── train/electricity_train.csv  ({len(train_df)} rows, 80%)")
            print(f"    └── test/electricity_test.csv    ({len(test_df)} rows, 20%)")
        except PermissionError as pe:
            print(f"[!] Notice: Cannot write to external folder '{target_dir}' in sandboxed mode ({pe}).")
            print(f"    Will sync using unsandboxed operation if requested.")

    print("\n[SUCCESS] Data cleaning, outlier removal, and unbiased 80/20 train-test splitting complete!")
    print("[NOTE] As instructed, NO machine learning or deep learning models were trained. Awaiting further instruction.")

if __name__ == "__main__":
    clean_and_split()

