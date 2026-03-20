package com.loanApplication.application.service;

import com.loanApplication.application.IInterestRateCalculationService;
import com.loanApplication.application.IRiskBandService;
import com.loanApplication.domain.Applicant;
import com.loanApplication.domain.LoanRequest;
import com.loanApplication.domain.LoanResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class InterestRateCalculationServiceImpl implements IInterestRateCalculationService {

    public final BigDecimal baseRate = new BigDecimal(12);
    private final IRiskBandService riskBandService;

    public InterestRateCalculationServiceImpl(IRiskBandService riskBandService){
        this.riskBandService = riskBandService;
    }

    public BigDecimal calculateInterestRate(LoanRequest loanRequest){

        BigDecimal finalInterestRate = baseRate;

        finalInterestRate = finalInterestRate.add(getRiskPremium(loanRequest.getApplicant().getCreditScore()));
        finalInterestRate = finalInterestRate.add(getEmploymentPremium(loanRequest.getApplicant().getEmploymentType()));
        finalInterestRate = finalInterestRate.add(getLoanSizePremium(loanRequest.getLoan().getAmount()));

        return finalInterestRate;
    }

       private BigDecimal getEmploymentPremium(Applicant.EmploymentType type) {

        return type == Applicant.EmploymentType.SELF_EMPLOYED
                ? new BigDecimal("1"): BigDecimal.ZERO;
    }

    private BigDecimal getLoanSizePremium(BigDecimal amount) {

        return amount.compareTo(new BigDecimal("1000000")) > 0
                ? new BigDecimal("0.5") : BigDecimal.ZERO;
    }

    private BigDecimal getRiskPremium(int creditScore) {

        LoanResponse.RiskBand band = riskBandService.checkRiskBand(creditScore);

        return switch (band) {
            case LOW -> BigDecimal.ZERO;
            case MEDIUM -> new BigDecimal("1.5");
            case HIGH -> new BigDecimal("3");
        };
    }
}
