package com.loanApplication.application.service;

import com.loanApplication.application.IEMICalculatorService;
import com.loanApplication.domain.Applicant;
import com.loanApplication.domain.Loan;
import com.loanApplication.domain.LoanRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EligibilityCheckServiceImplTest {

    @Mock
    private IEMICalculatorService emiCalculatorService;

    @InjectMocks
    private EligibilityCheckServiceImpl eligibilityCheckService;

    // Default "safe" values — override per test as needed
    private static final int    SAFE_CREDIT_SCORE  = 700;
    private static final int    SAFE_AGE           = 30;
    private static final int    SAFE_TENURE_MONTHS = 120;          // 10 years → age+tenure = 40 ≤ 60
    private static final BigDecimal SAFE_INCOME    = new BigDecimal("100000");
    private static final BigDecimal SAFE_AMOUNT    = new BigDecimal("500000");
    private static final BigDecimal SAFE_EMI       = new BigDecimal("30000"); // 30% of income, well below 60%

    private LoanRequest buildRequest(int creditScore, int age, int tenureMonths,
                                     BigDecimal income, BigDecimal amount) {
        Applicant applicant = Applicant.builder()
                .creditScore(creditScore)
                .age(age)
                .income(income)
                .build();

        Loan loan = Loan.builder()
                .amount(amount)
                .tenureMonths(tenureMonths)
                .build();

        return LoanRequest.builder()
                .applicant(applicant)
                .loan(loan)
                .build();
    }

    private LoanRequest safeRequest() {
        return buildRequest(SAFE_CREDIT_SCORE, SAFE_AGE, SAFE_TENURE_MONTHS, SAFE_INCOME, SAFE_AMOUNT);
    }

    // -----------------------------------------------------------------------
    // HAPPY PATH
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Eligible Applications — Empty Rejection List")
    class EligibleApplications {

        @Test
        @DisplayName("Should return empty list when all checks pass")
        void shouldReturnEmptyList_whenAllChecksPass() {
            when(emiCalculatorService.calculateEmi(SAFE_AMOUNT, new BigDecimal(12), SAFE_TENURE_MONTHS))
                    .thenReturn(SAFE_EMI);

            List<String> result = eligibilityCheckService.checkEligibility(safeRequest());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should pass when credit score is exactly 600 (boundary)")
        void shouldPass_whenCreditScoreIsExactly600() {
            LoanRequest request = buildRequest(600, SAFE_AGE, SAFE_TENURE_MONTHS, SAFE_INCOME, SAFE_AMOUNT);
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(SAFE_EMI);

            List<String> result = eligibilityCheckService.checkEligibility(request);

            assertThat(result).doesNotContain("CREDIT SCORE IS LESS THAN 600");
        }

        @Test
        @DisplayName("Should pass when age + tenure years equals exactly 60 (boundary)")
        void shouldPass_whenAgePlusTenureYearsIsExactly60() {
            // age=40, tenure=240 months (20 years) → 40+20 = 60, NOT > 60
            LoanRequest request = buildRequest(SAFE_CREDIT_SCORE, 40, 240, SAFE_INCOME, SAFE_AMOUNT);
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(SAFE_EMI);

            List<String> result = eligibilityCheckService.checkEligibility(request);

            assertThat(result).doesNotContain("AGE_TENURE_LIMIT_EXCEEDED");
        }

        @Test
        @DisplayName("Should pass when EMI equals exactly 60% of income (boundary)")
        void shouldPass_whenEmiIsExactlySixtyPercentOfIncome() {
            // income=100,000 → 60% = 60,000; compareTo returns 0, not > 0 → no rejection
            BigDecimal emiAtBoundary = new BigDecimal("60000");
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(emiAtBoundary);

            List<String> result = eligibilityCheckService.checkEligibility(safeRequest());

            assertThat(result).doesNotContain("EMI_EXCEEDS_60_PERCENT");
        }
    }

    // -----------------------------------------------------------------------
    // CREDIT SCORE CHECK
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Credit Score Validation")
    class CreditScoreValidation {

        @ParameterizedTest(name = "credit score = {0}")
        @ValueSource(ints = {0, 300, 500, 599})
        @DisplayName("Should reject when credit score is below 600")
        void shouldReject_whenCreditScoreBelow600(int creditScore) {
            LoanRequest request = buildRequest(creditScore, SAFE_AGE, SAFE_TENURE_MONTHS, SAFE_INCOME, SAFE_AMOUNT);
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(SAFE_EMI);

            List<String> result = eligibilityCheckService.checkEligibility(request);

            assertThat(result).contains("CREDIT SCORE IS LESS THAN 600");
        }

        @ParameterizedTest(name = "credit score = {0}")
        @ValueSource(ints = {600, 650, 750, 850, 900})
        @DisplayName("Should not reject for credit score at or above 600")
        void shouldNotReject_whenCreditScoreAtOrAbove600(int creditScore) {
            LoanRequest request = buildRequest(creditScore, SAFE_AGE, SAFE_TENURE_MONTHS, SAFE_INCOME, SAFE_AMOUNT);
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(SAFE_EMI);

            List<String> result = eligibilityCheckService.checkEligibility(request);

            assertThat(result).doesNotContain("CREDIT SCORE IS LESS THAN 600");
        }
    }

    // -----------------------------------------------------------------------
    // AGE + TENURE CHECK
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Age + Tenure Limit Validation")
    class AgeTenureLimitValidation {

        @Test
        @DisplayName("Should reject when age + tenure years exceeds 60")
        void shouldReject_whenAgePlusTenureYearsExceeds60() {
            // age=50, tenure=180 months (15 years) → 50+15 = 65 > 60
            LoanRequest request = buildRequest(SAFE_CREDIT_SCORE, 50, 180, SAFE_INCOME, SAFE_AMOUNT);
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(SAFE_EMI);

            List<String> result = eligibilityCheckService.checkEligibility(request);

            assertThat(result).contains("AGE_TENURE_LIMIT_EXCEEDED");
        }

        @Test
        @DisplayName("Should reject when age alone is 61 with minimum tenure")
        void shouldReject_whenApplicantAlreadyOlderThan60() {
            // age=61, tenure=12 months (1 year) → 61+1 = 62 > 60
            LoanRequest request = buildRequest(SAFE_CREDIT_SCORE, 61, 12, SAFE_INCOME, SAFE_AMOUNT);
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(SAFE_EMI);

            List<String> result = eligibilityCheckService.checkEligibility(request);

            assertThat(result).contains("AGE_TENURE_LIMIT_EXCEEDED");
        }

        @Test
        @DisplayName("Should use integer division when converting tenure months to years")
        void shouldUseIntegerDivision_forTenureConversion() {
            // age=41, tenure=229 months → 229/12 = 19 (integer division) → 41+19 = 60, NOT > 60
            LoanRequest request = buildRequest(SAFE_CREDIT_SCORE, 41, 229, SAFE_INCOME, SAFE_AMOUNT);
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(SAFE_EMI);

            List<String> result = eligibilityCheckService.checkEligibility(request);

            assertThat(result).doesNotContain("AGE_TENURE_LIMIT_EXCEEDED");
        }

        @Test
        @DisplayName("Should reject when tenure is 240 months and age is 41 (41+20=61)")
        void shouldReject_whenAge41AndTenure240Months() {
            // age=41, tenure=240 months (20 years) → 41+20 = 61 > 60
            LoanRequest request = buildRequest(SAFE_CREDIT_SCORE, 41, 240, SAFE_INCOME, SAFE_AMOUNT);
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(SAFE_EMI);

            List<String> result = eligibilityCheckService.checkEligibility(request);

            assertThat(result).contains("AGE_TENURE_LIMIT_EXCEEDED");
        }
    }

    // -----------------------------------------------------------------------
    // EMI EXCEEDS 60% CHECK
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("EMI vs Income (60%) Validation")
    class EmiIncomeValidation {

        @Test
        @DisplayName("Should reject when EMI exceeds 60% of income")
        void shouldReject_whenEmiExceedsSixtyPercentOfIncome() {
            // income=100,000 → 60% = 60,000; EMI=70,000 → exceeds
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt()))
                    .thenReturn(new BigDecimal("70000"));

            List<String> result = eligibilityCheckService.checkEligibility(safeRequest());

            assertThat(result).contains("EMI_EXCEEDS_60_PERCENT");
        }

        @Test
        @DisplayName("Should pass correct arguments to EMI calculator")
        void shouldPassCorrectArguments_toEmiCalculator() {
            LoanRequest request = buildRequest(SAFE_CREDIT_SCORE, SAFE_AGE, SAFE_TENURE_MONTHS, SAFE_INCOME, SAFE_AMOUNT);
            when(emiCalculatorService.calculateEmi(SAFE_AMOUNT, new BigDecimal(12), SAFE_TENURE_MONTHS))
                    .thenReturn(SAFE_EMI);

            eligibilityCheckService.checkEligibility(request);

            verify(emiCalculatorService).calculateEmi(SAFE_AMOUNT, new BigDecimal(12), SAFE_TENURE_MONTHS);
        }

        @Test
        @DisplayName("Should call EMI calculator exactly once per eligibility check")
        void shouldCallEmiCalculator_exactlyOnce() {
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(SAFE_EMI);

            eligibilityCheckService.checkEligibility(safeRequest());

            verify(emiCalculatorService, times(1)).calculateEmi(any(), any(), anyInt());
        }
    }

    // -----------------------------------------------------------------------
    // MULTIPLE REJECTION REASONS
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Multiple Simultaneous Rejection Reasons")
    class MultipleRejectionReasons {

        @Test
        @DisplayName("Should return all three rejection reasons when all checks fail")
        void shouldReturnAllThreeReasons_whenAllChecksFail() {
            // credit score < 600, age+tenure > 60, EMI > 60% income
            LoanRequest request = buildRequest(500, 55, 120, SAFE_INCOME, SAFE_AMOUNT);
            // age=55, tenure=120 months (10 years) → 55+10 = 65 > 60
            // EMI=80,000 > 60,000 (60% of 100,000)
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt()))
                    .thenReturn(new BigDecimal("80000"));

            List<String> result = eligibilityCheckService.checkEligibility(request);

            assertThat(result)
                    .hasSize(3)
                    .contains("CREDIT SCORE IS LESS THAN 600")
                    .contains("AGE_TENURE_LIMIT_EXCEEDED")
                    .contains("EMI_EXCEEDS_60_PERCENT");
        }

        @Test
        @DisplayName("Should return two reasons when credit score and EMI checks both fail")
        void shouldReturnTwoReasons_whenCreditScoreAndEmiFail() {
            LoanRequest request = buildRequest(500, SAFE_AGE, SAFE_TENURE_MONTHS, SAFE_INCOME, SAFE_AMOUNT);
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt()))
                    .thenReturn(new BigDecimal("70000")); // > 60% of income

            List<String> result = eligibilityCheckService.checkEligibility(request);

            assertThat(result)
                    .hasSize(2)
                    .contains("CREDIT SCORE IS LESS THAN 600")
                    .contains("EMI_EXCEEDS_60_PERCENT");
        }

        @Test
        @DisplayName("Should return two reasons when age-tenure and EMI checks both fail")
        void shouldReturnTwoReasons_whenAgeTenureAndEmiFail() {
            // age=55, tenure=120 months (10y) → 55+10=65 > 60
            LoanRequest request = buildRequest(SAFE_CREDIT_SCORE, 55, 120, SAFE_INCOME, SAFE_AMOUNT);
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt()))
                    .thenReturn(new BigDecimal("70000"));

            List<String> result = eligibilityCheckService.checkEligibility(request);

            assertThat(result)
                    .hasSize(2)
                    .contains("AGE_TENURE_LIMIT_EXCEEDED")
                    .contains("EMI_EXCEEDS_60_PERCENT");
        }

        @Test
        @DisplayName("Should return two reasons when credit score and age-tenure checks both fail")
        void shouldReturnTwoReasons_whenCreditScoreAndAgeTenureFail() {
            // credit score < 600, age=55, tenure=120 months (10y) → 55+10=65 > 60, EMI within limit
            LoanRequest request = buildRequest(500, 55, 120, SAFE_INCOME, SAFE_AMOUNT);
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(SAFE_EMI);

            List<String> result = eligibilityCheckService.checkEligibility(request);

            assertThat(result)
                    .hasSize(2)
                    .contains("CREDIT SCORE IS LESS THAN 600")
                    .contains("AGE_TENURE_LIMIT_EXCEEDED");
        }

        @Test
        @DisplayName("Should always evaluate all checks regardless of earlier failures")
        void shouldEvaluateAllChecks_regardlessOfEarlierFailures() {
            // Failing credit score should not prevent age/EMI checks from running
            LoanRequest request = buildRequest(400, 55, 120, SAFE_INCOME, SAFE_AMOUNT);
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt()))
                    .thenReturn(new BigDecimal("80000"));

            List<String> result = eligibilityCheckService.checkEligibility(request);

            // All three checks must have run and contributed to the result
            assertThat(result).hasSize(3);
            verify(emiCalculatorService, times(1)).calculateEmi(any(), any(), anyInt());
        }
    }
}
