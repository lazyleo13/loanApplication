package com.loanApplication.application.service;

import com.loanApplication.domain.LoanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class RiskBandServiceImplTest {

    private RiskBandServiceImpl riskBandService;

    @BeforeEach
    void setUp() {
        riskBandService = new RiskBandServiceImpl();
    }

    @Nested
    @DisplayName("LOW Risk Band — credit score >= 750")
    class LowRiskBand {

        @Test
        @DisplayName("Should return LOW for credit score exactly 750 (boundary)")
        void shouldReturnLow_forScoreExactly750() {
            assertThat(riskBandService.checkRiskBand(750)).isEqualTo(LoanResponse.RiskBand.LOW);
        }

        @ParameterizedTest(name = "credit score = {0}")
        @ValueSource(ints = {751, 800, 850, 900})
        @DisplayName("Should return LOW for credit score above 750")
        void shouldReturnLow_forScoreAbove750(int score) {
            assertThat(riskBandService.checkRiskBand(score)).isEqualTo(LoanResponse.RiskBand.LOW);
        }
    }

    @Nested
    @DisplayName("MEDIUM Risk Band — credit score 650–749")
    class MediumRiskBand {

        @Test
        @DisplayName("Should return MEDIUM for credit score exactly 650 (lower boundary)")
        void shouldReturnMedium_forScoreExactly650() {
            assertThat(riskBandService.checkRiskBand(650)).isEqualTo(LoanResponse.RiskBand.MEDIUM);
        }

        @Test
        @DisplayName("Should return MEDIUM for credit score exactly 749 (upper boundary)")
        void shouldReturnMedium_forScoreExactly749() {
            assertThat(riskBandService.checkRiskBand(749)).isEqualTo(LoanResponse.RiskBand.MEDIUM);
        }

        @ParameterizedTest(name = "credit score = {0}")
        @ValueSource(ints = {651, 700, 720, 748})
        @DisplayName("Should return MEDIUM for credit scores in the 650–749 range")
        void shouldReturnMedium_forScoresInRange(int score) {
            assertThat(riskBandService.checkRiskBand(score)).isEqualTo(LoanResponse.RiskBand.MEDIUM);
        }
    }

    @Nested
    @DisplayName("HIGH Risk Band — credit score 600–649")
    class HighRiskBand {

        @Test
        @DisplayName("Should return HIGH for credit score exactly 600 (lower boundary)")
        void shouldReturnHigh_forScoreExactly600() {
            assertThat(riskBandService.checkRiskBand(600)).isEqualTo(LoanResponse.RiskBand.HIGH);
        }

        @Test
        @DisplayName("Should return HIGH for credit score exactly 649 (upper boundary)")
        void shouldReturnHigh_forScoreExactly649() {
            assertThat(riskBandService.checkRiskBand(649)).isEqualTo(LoanResponse.RiskBand.HIGH);
        }

        @ParameterizedTest(name = "credit score = {0}")
        @ValueSource(ints = {601, 620, 640, 648})
        @DisplayName("Should return HIGH for credit scores in the 600–649 range")
        void shouldReturnHigh_forScoresInRange(int score) {
            assertThat(riskBandService.checkRiskBand(score)).isEqualTo(LoanResponse.RiskBand.HIGH);
        }
    }

    @Nested
    @DisplayName("Band Boundary Transitions")
    class BoundaryTransitions {

        @Test
        @DisplayName("Score 649 should be HIGH and 650 should be MEDIUM")
        void shouldTransitionFromHighToMedium_at650() {
            assertThat(riskBandService.checkRiskBand(649)).isEqualTo(LoanResponse.RiskBand.HIGH);
            assertThat(riskBandService.checkRiskBand(650)).isEqualTo(LoanResponse.RiskBand.MEDIUM);
        }

        @Test
        @DisplayName("Score 749 should be MEDIUM and 750 should be LOW")
        void shouldTransitionFromMediumToLow_at750() {
            assertThat(riskBandService.checkRiskBand(749)).isEqualTo(LoanResponse.RiskBand.MEDIUM);
            assertThat(riskBandService.checkRiskBand(750)).isEqualTo(LoanResponse.RiskBand.LOW);
        }
    }
}
