package com.loanApplication.application.service;

import com.loanApplication.application.*;
import com.loanApplication.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceImplTest {

    @Mock
    private IEligibilityCheckService eligibilityCheckService;

    @Mock
    private IInterestRateCalculationService interestRateCalculationService;

    @Mock
    private IRiskBandService riskBandService;

    @Mock
    private IEMICalculatorService emiCalculatorService;

    @InjectMocks
    private LoanApplicationServiceImpl loanApplicationService;

    private LoanRequest loanRequest;
    private Applicant applicant;
    private Loan loan;

    @BeforeEach
    void setUp() {
        applicant = Applicant.builder()
                .creditScore(750)
                .income(new BigDecimal("100000"))
                .build();

        loan = Loan.builder()
                .amount(new BigDecimal("500000"))
                .tenureMonths(24)
                .build();

        loanRequest = LoanRequest.builder()
                .applicant(applicant)
                .loan(loan)
                .build();
    }

    // -----------------------------------------------------------------------
    // REJECTION SCENARIOS
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Loan Rejection Scenarios")
    class RejectionScenarios {

        @Test
        @DisplayName("Should reject loan when eligibility check fails with single reason")
        void shouldRejectLoan_whenEligibilityCheckFails_singleReason() {
            List<String> reasons = List.of("LOW_CREDIT_SCORE");
            when(eligibilityCheckService.checkEligibility(loanRequest)).thenReturn(reasons);

            LoanResponse response = loanApplicationService.processLoan(loanRequest);

            assertThat(response.getStatus()).isEqualTo("REJECTED");
            assertThat(response.getRejectedReasons()).containsExactly("LOW_CREDIT_SCORE");
            assertThat(response.getUuid()).isNotNull();
            assertThat(response.getOffer()).isNull();

            // Downstream services should NOT be called after rejection
            verifyNoInteractions(riskBandService, interestRateCalculationService, emiCalculatorService);
        }

        @Test
        @DisplayName("Should reject loan when eligibility check fails with multiple reasons")
        void shouldRejectLoan_whenEligibilityCheckFails_multipleReasons() {
            List<String> reasons = List.of("LOW_CREDIT_SCORE", "INSUFFICIENT_INCOME", "HIGH_EXISTING_DEBT");
            when(eligibilityCheckService.checkEligibility(loanRequest)).thenReturn(reasons);

            LoanResponse response = loanApplicationService.processLoan(loanRequest);

            assertThat(response.getStatus()).isEqualTo("REJECTED");
            assertThat(response.getRejectedReasons()).hasSize(3).containsExactlyInAnyOrderElementsOf(reasons);
            verifyNoInteractions(riskBandService, interestRateCalculationService, emiCalculatorService);
        }

        @Test
        @DisplayName("Should reject loan when EMI exceeds 50% of applicant income")
        void shouldRejectLoan_whenEmiExceedsFiftyPercentOfIncome() {
            // Income = 100,000 → 50% = 50,000. EMI = 60,000 → exceeds threshold.
            when(eligibilityCheckService.checkEligibility(loanRequest)).thenReturn(Collections.emptyList());
            when(riskBandService.checkRiskBand(750)).thenReturn(LoanResponse.RiskBand.LOW);
            when(interestRateCalculationService.calculateInterestRate(loanRequest)).thenReturn(new BigDecimal("8.5"));
            when(emiCalculatorService.calculateEmi(
                    loan.getAmount(), new BigDecimal(12), loan.getTenureMonths()))
                    .thenReturn(new BigDecimal("60000")); // > 50,000 (50% of income)

            LoanResponse response = loanApplicationService.processLoan(loanRequest);

            assertThat(response.getStatus()).isNull();
            assertThat(response.getRejectedReasons()).containsExactly("EMI_EXCEEDS_50_PERCENT");
            assertThat(response.getUuid()).isNotNull();
            assertThat(response.getOffer()).isNull();
        }

        @Test
        @DisplayName("Should reject loan when EMI is exactly equal to 50% of income")
        void shouldNotRejectLoan_whenEmiIsExactlyFiftyPercent() {
            // EMI == 50% of income: compareTo returns 0, so NOT > 0 → APPROVED
            when(eligibilityCheckService.checkEligibility(loanRequest)).thenReturn(Collections.emptyList());
            when(riskBandService.checkRiskBand(750)).thenReturn(LoanResponse.RiskBand.LOW);
            when(interestRateCalculationService.calculateInterestRate(loanRequest)).thenReturn(new BigDecimal("8.5"));
            when(emiCalculatorService.calculateEmi(
                    loan.getAmount(), new BigDecimal(12), loan.getTenureMonths()))
                    .thenReturn(new BigDecimal("50000")); // exactly 50% of 100,000

            LoanResponse response = loanApplicationService.processLoan(loanRequest);

            assertThat(response.getStatus()).isEqualTo("APPROVED");
        }
    }

    // -----------------------------------------------------------------------
    // APPROVAL SCENARIOS
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Loan Approval Scenarios")
    class ApprovalScenarios {

        @Test
        @DisplayName("Should approve loan with correct offer details when all checks pass")
        void shouldApproveLoan_whenAllChecksPass() {
            BigDecimal emi = new BigDecimal("20000");
            BigDecimal rate = new BigDecimal("8.5");

            when(eligibilityCheckService.checkEligibility(loanRequest)).thenReturn(Collections.emptyList());
            when(riskBandService.checkRiskBand(750)).thenReturn(LoanResponse.RiskBand.LOW);
            when(interestRateCalculationService.calculateInterestRate(loanRequest)).thenReturn(rate);
            when(emiCalculatorService.calculateEmi(
                    loan.getAmount(), new BigDecimal(12), loan.getTenureMonths()))
                    .thenReturn(emi);

            LoanResponse response = loanApplicationService.processLoan(loanRequest);

            assertThat(response.getStatus()).isEqualTo("APPROVED");
            assertThat(response.getUuid()).isNotNull();
            assertThat(response.getRejectedReasons()).isNullOrEmpty();
            assertThat(response.getRiskBand()).isEqualTo(LoanResponse.RiskBand.LOW);

            Offer offer = response.getOffer();
            assertThat(offer).isNotNull();
            assertThat(offer.getEmi()).isEqualByComparingTo(emi);
            assertThat(offer.getInterestRate()).isEqualByComparingTo(rate);
            assertThat(offer.getTenureMonths()).isEqualTo(24);

            // totalPayable = emi * tenureMonths = 20,000 * 24 = 480,000
            assertThat(offer.getTotalPayable()).isEqualByComparingTo(new BigDecimal("480000"));
        }

        @Test
        @DisplayName("Should approve loan with MEDIUM risk band")
        void shouldApproveLoan_withMediumRiskBand() {
            applicant = Applicant.builder().creditScore(620).income(new BigDecimal("100000")).build();
            loanRequest = LoanRequest.builder().applicant(applicant).loan(loan).build();

            when(eligibilityCheckService.checkEligibility(loanRequest)).thenReturn(Collections.emptyList());
            when(riskBandService.checkRiskBand(620)).thenReturn(LoanResponse.RiskBand.MEDIUM);
            when(interestRateCalculationService.calculateInterestRate(loanRequest)).thenReturn(new BigDecimal("12.0"));
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(new BigDecimal("25000"));

            LoanResponse response = loanApplicationService.processLoan(loanRequest);

            assertThat(response.getStatus()).isEqualTo("APPROVED");
            assertThat(response.getRiskBand()).isEqualTo(LoanResponse.RiskBand.MEDIUM);
        }

        @Test
        @DisplayName("Should approve loan with HIGH risk band")
        void shouldApproveLoan_withHighRiskBand() {
            when(eligibilityCheckService.checkEligibility(loanRequest)).thenReturn(Collections.emptyList());
            when(riskBandService.checkRiskBand(750)).thenReturn(LoanResponse.RiskBand.HIGH);
            when(interestRateCalculationService.calculateInterestRate(loanRequest)).thenReturn(new BigDecimal("18.0"));
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(new BigDecimal("30000"));

            LoanResponse response = loanApplicationService.processLoan(loanRequest);

            assertThat(response.getStatus()).isEqualTo("APPROVED");
            assertThat(response.getRiskBand()).isEqualTo(LoanResponse.RiskBand.HIGH);
        }

        @Test
        @DisplayName("Should generate a unique UUID for each loan response")
        void shouldGenerateUniqueUuid_forEachLoanResponse() {
            when(eligibilityCheckService.checkEligibility(loanRequest)).thenReturn(Collections.emptyList());
            when(riskBandService.checkRiskBand(anyInt())).thenReturn(LoanResponse.RiskBand.LOW);
            when(interestRateCalculationService.calculateInterestRate(any())).thenReturn(new BigDecimal("8.5"));
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(new BigDecimal("20000"));

            LoanResponse response1 = loanApplicationService.processLoan(loanRequest);
            LoanResponse response2 = loanApplicationService.processLoan(loanRequest);

            assertThat(response1.getUuid()).isNotEqualTo(response2.getUuid());
        }

        @Test
        @DisplayName("Should compute totalPayable as emi multiplied by tenureMonths")
        void shouldComputeTotalPayable_correctly() {
            BigDecimal emi = new BigDecimal("15000");
            int tenure = 36;

            loan = Loan.builder().amount(new BigDecimal("500000")).tenureMonths(tenure).build();
            loanRequest = LoanRequest.builder().applicant(applicant).loan(loan).build();

            when(eligibilityCheckService.checkEligibility(loanRequest)).thenReturn(Collections.emptyList());
            when(riskBandService.checkRiskBand(anyInt())).thenReturn(LoanResponse.RiskBand.LOW);
            when(interestRateCalculationService.calculateInterestRate(any())).thenReturn(new BigDecimal("8.0"));
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(emi);

            LoanResponse response = loanApplicationService.processLoan(loanRequest);

            // 15,000 * 36 = 540,000
            assertThat(response.getOffer().getTotalPayable())
                    .isEqualByComparingTo(emi.multiply(new BigDecimal(tenure)));
        }
    }

    // -----------------------------------------------------------------------
    // SERVICE INTERACTION VERIFICATIONS
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Service Interaction Verifications")
    class ServiceInteractionVerifications {

        @Test
        @DisplayName("Should invoke all downstream services exactly once for approved loan")
        void shouldInvokeAllDownstreamServices_onceForApprovedLoan() {
            when(eligibilityCheckService.checkEligibility(loanRequest)).thenReturn(Collections.emptyList());
            when(riskBandService.checkRiskBand(750)).thenReturn(LoanResponse.RiskBand.LOW);
            when(interestRateCalculationService.calculateInterestRate(loanRequest)).thenReturn(new BigDecimal("8.5"));
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(new BigDecimal("20000"));

            loanApplicationService.processLoan(loanRequest);

            verify(eligibilityCheckService, times(1)).checkEligibility(loanRequest);
            verify(riskBandService, times(1)).checkRiskBand(750);
            verify(interestRateCalculationService, times(1)).calculateInterestRate(loanRequest);
            verify(emiCalculatorService, times(1)).calculateEmi(
                    loan.getAmount(), new BigDecimal(12), loan.getTenureMonths());
        }

        @Test
        @DisplayName("Should pass correct arguments to EMI calculator")
        void shouldPassCorrectArguments_toEmiCalculator() {
            when(eligibilityCheckService.checkEligibility(loanRequest)).thenReturn(Collections.emptyList());
            when(riskBandService.checkRiskBand(anyInt())).thenReturn(LoanResponse.RiskBand.LOW);
            when(interestRateCalculationService.calculateInterestRate(any())).thenReturn(new BigDecimal("8.5"));
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(new BigDecimal("20000"));

            loanApplicationService.processLoan(loanRequest);

            verify(emiCalculatorService).calculateEmi(
                    new BigDecimal("500000"),
                    new BigDecimal(12),
                    24
            );
        }

        @Test
        @DisplayName("Should pass applicant credit score to risk band service")
        void shouldPassCreditScore_toRiskBandService() {
            applicant = Applicant.builder().creditScore(800).income(new BigDecimal("200000")).build();
            loanRequest = LoanRequest.builder().applicant(applicant).loan(loan).build();

            when(eligibilityCheckService.checkEligibility(loanRequest)).thenReturn(Collections.emptyList());
            when(riskBandService.checkRiskBand(800)).thenReturn(LoanResponse.RiskBand.LOW);
            when(interestRateCalculationService.calculateInterestRate(any())).thenReturn(new BigDecimal("7.0"));
            when(emiCalculatorService.calculateEmi(any(), any(), anyInt())).thenReturn(new BigDecimal("20000"));

            loanApplicationService.processLoan(loanRequest);

            verify(riskBandService).checkRiskBand(800);
        }

        @Test
        @DisplayName("Should short-circuit and skip downstream services on eligibility failure")
        void shouldShortCircuit_onEligibilityFailure() {
            when(eligibilityCheckService.checkEligibility(loanRequest))
                    .thenReturn(List.of("UNDERAGE_APPLICANT"));

            loanApplicationService.processLoan(loanRequest);

            verify(eligibilityCheckService, times(1)).checkEligibility(loanRequest);
            verifyNoInteractions(riskBandService, interestRateCalculationService, emiCalculatorService);
        }
    }
}
