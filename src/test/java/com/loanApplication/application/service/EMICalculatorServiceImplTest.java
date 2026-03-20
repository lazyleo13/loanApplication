package com.loanApplication.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EMICalculatorServiceImplTest {

    private EMICalculatorServiceImpl emiCalculatorService;

    @BeforeEach
    void setUp() {
        emiCalculatorService = new EMICalculatorServiceImpl();
    }

    // EMI = P * r * (1+r)^n / ((1+r)^n - 1)
    // where r = annual rate / (12 * 100)

    @Nested
    @DisplayName("Standard EMI Calculations")
    class StandardCalculations {

        @Test
        @DisplayName("Should calculate EMI correctly for 5,00,000 at 12% for 36 months")
        void shouldCalculateEmi_forStandardInputs() {
            // r = 12 / (12*100) = 0.01
            // (1+r)^n = (1.01)^36 ≈ 1.43077
            // EMI = 500000 * 0.01 * 1.43077 / (1.43077 - 1) = 16607.15 (approx)
            BigDecimal principal = new BigDecimal("500000");
            BigDecimal rate = new BigDecimal("12");
            int tenure = 36;

            BigDecimal emi = emiCalculatorService.calculateEmi(principal, rate, tenure);

            // Verify it's in a reasonable range and is a positive value
            assertThat(emi).isPositive();
            assertThat(emi).isGreaterThan(new BigDecimal("16000"));
            assertThat(emi).isLessThan(new BigDecimal("17500"));
        }

        @Test
        @DisplayName("Should calculate EMI correctly for 1,00,000 at 12% for 12 months")
        void shouldCalculateEmi_for12MonthTenure() {
            // r = 0.01, n = 12
            // EMI = 100000 * 0.01 * (1.01)^12 / ((1.01)^12 - 1)
            // (1.01)^12 ≈ 1.12683
            // EMI ≈ 8884.88
            BigDecimal principal = new BigDecimal("100000");
            BigDecimal rate = new BigDecimal("12");
            int tenure = 12;

            BigDecimal emi = emiCalculatorService.calculateEmi(principal, rate, tenure);

            assertThat(emi).isPositive();
            assertThat(emi).isGreaterThan(new BigDecimal("8800"));
            assertThat(emi).isLessThan(new BigDecimal("9000"));
        }

        @Test
        @DisplayName("Should return value with scale of 2 (BigDecimal precision)")
        void shouldReturnValueWithScaleOfTwo() {
            BigDecimal principal = new BigDecimal("500000");
            BigDecimal rate = new BigDecimal("12");
            int tenure = 36;

            BigDecimal emi = emiCalculatorService.calculateEmi(principal, rate, tenure);

            assertThat(emi.scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("EMI should scale proportionally with principal")
        void emiShouldScaleWithPrincipal() {
            BigDecimal rate = new BigDecimal("12");
            int tenure = 36;

            BigDecimal emi1 = emiCalculatorService.calculateEmi(new BigDecimal("500000"), rate, tenure);
            BigDecimal emi2 = emiCalculatorService.calculateEmi(new BigDecimal("1000000"), rate, tenure);

            // EMI for double the principal should be approximately double
            BigDecimal ratio = emi2.divide(emi1, 2, java.math.RoundingMode.HALF_UP);
            assertThat(ratio).isEqualByComparingTo(new BigDecimal("2.00"));
        }
    }

    @Nested
    @DisplayName("Higher Interest Rate Calculations")
    class HigherRateCalculations {

        @Test
        @DisplayName("Higher interest rate should produce higher EMI for same principal and tenure")
        void higherRate_shouldProduceLargerEmi() {
            BigDecimal principal = new BigDecimal("500000");
            int tenure = 36;

            BigDecimal emiLowRate  = emiCalculatorService.calculateEmi(principal, new BigDecimal("12"),   tenure);
            BigDecimal emiHighRate = emiCalculatorService.calculateEmi(principal, new BigDecimal("13.5"), tenure);

            assertThat(emiHighRate).isGreaterThan(emiLowRate);
        }

        @Test
        @DisplayName("Should calculate EMI correctly for 13.5% (HIGH risk band rate)")
        void shouldCalculateEmi_atHighRiskRate() {
            BigDecimal principal = new BigDecimal("500000");
            BigDecimal rate = new BigDecimal("13.5");
            int tenure = 36;

            BigDecimal emi = emiCalculatorService.calculateEmi(principal, rate, tenure);

            assertThat(emi).isPositive();
            assertThat(emi.scale()).isEqualTo(2);
            assertThat(emi).isGreaterThan(new BigDecimal("16000"));
        }
    }

    @Nested
    @DisplayName("Tenure Length Effects")
    class TenureEffects {

        @Test
        @DisplayName("Longer tenure should produce lower EMI for same principal and rate")
        void longerTenure_shouldProduceLowerEmi() {
            BigDecimal principal = new BigDecimal("500000");
            BigDecimal rate = new BigDecimal("12");

            BigDecimal emiShort = emiCalculatorService.calculateEmi(principal, rate, 36);
            BigDecimal emiLong  = emiCalculatorService.calculateEmi(principal, rate, 120);

            assertThat(emiLong).isLessThan(emiShort);
        }

        @Test
        @DisplayName("Should calculate EMI for minimum tenure of 6 months")
        void shouldHandleMinimumTenure() {
            BigDecimal principal = new BigDecimal("100000");
            BigDecimal rate = new BigDecimal("12");
            int tenure = 6;

            BigDecimal emi = emiCalculatorService.calculateEmi(principal, rate, tenure);

            assertThat(emi).isPositive();
            assertThat(emi.scale()).isEqualTo(2);
            // For short tenure EMI should be greater than principal / tenure
            assertThat(emi).isGreaterThan(new BigDecimal("100000").divide(new BigDecimal("6"), 2, java.math.RoundingMode.HALF_UP));
        }

        @Test
        @DisplayName("Should calculate EMI for maximum tenure of 360 months")
        void shouldHandleMaximumTenure() {
            BigDecimal principal = new BigDecimal("5000000");
            BigDecimal rate = new BigDecimal("12");
            int tenure = 360;

            BigDecimal emi = emiCalculatorService.calculateEmi(principal, rate, tenure);

            assertThat(emi).isPositive();
            assertThat(emi.scale()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Total Payable Reasonableness")
    class TotalPayableReasonableness {

        @Test
        @DisplayName("Total payable (EMI × tenure) should always exceed principal")
        void totalPayable_shouldExceedPrincipal() {
            BigDecimal principal = new BigDecimal("500000");
            BigDecimal rate = new BigDecimal("12");
            int tenure = 36;

            BigDecimal emi = emiCalculatorService.calculateEmi(principal, rate, tenure);
            BigDecimal totalPayable = emi.multiply(new BigDecimal(tenure));

            assertThat(totalPayable).isGreaterThan(principal);
        }
    }
}
