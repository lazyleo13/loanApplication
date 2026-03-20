package com.loanApplication.application.service;

import com.loanApplication.application.*;
import com.loanApplication.domain.LoanRequest;
import com.loanApplication.domain.LoanResponse;
import com.loanApplication.domain.Offer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class LoanApplicationServiceImpl implements ILoanApplicationService {

    private final IEligibilityCheckService eligibilityCheckService;
    private final IInterestRateCalculationService interestRateCalculationService;
    private final IRiskBandService riskBandService;
    private final IEMICalculatorService emiCalculatorService;


    public LoanApplicationServiceImpl(IEligibilityCheckService eligibilityCheckService,
                                      IInterestRateCalculationService interestRateCalculationService, IRiskBandService riskBandService,
                                    IEMICalculatorService emiCalculatorService) {
        this.eligibilityCheckService = eligibilityCheckService;
        this.interestRateCalculationService = interestRateCalculationService;
        this.riskBandService = riskBandService;
        this.emiCalculatorService = emiCalculatorService;
    }

    public LoanResponse processLoan(LoanRequest loanRequest){

        LoanResponse loanResponse = new LoanResponse();

        List<String> nonEligibilityReason = eligibilityCheckService.checkEligibility(loanRequest);
        if(!nonEligibilityReason.isEmpty()){
            return LoanResponse.builder().rejectedReasons(nonEligibilityReason).uuid(UUID.randomUUID()).status("REJECTED").build();
        }

        LoanResponse.RiskBand riskBand = riskBandService.checkRiskBand(loanRequest.getApplicant().getCreditScore());

        BigDecimal rate = interestRateCalculationService.calculateInterestRate(loanRequest);

        BigDecimal emi = emiCalculatorService
                .calculateEmi(loanRequest.getLoan().getAmount(), new BigDecimal(12),loanRequest.getLoan().getTenureMonths());

        BigDecimal fiftyPercent =
                loanRequest.getApplicant().getIncome()
                        .multiply(new BigDecimal("0.5"));

        BigDecimal totalPayable = emi.multiply(new BigDecimal(loanRequest.getLoan().getTenureMonths()));

        if (emi.compareTo(fiftyPercent) > 0) {
            return LoanResponse.builder().uuid(UUID.randomUUID()).rejectedReasons(List.of("EMI_EXCEEDS_50_PERCENT")).build();
        }

        return LoanResponse.builder().uuid(UUID.randomUUID())
                .status("APPROVED")
                .riskBand(riskBand)
                .offer(Offer.builder()
                        .emi(emi).interestRate(rate)
                        .tenureMonths(loanRequest.getLoan().getTenureMonths()).totalPayable(totalPayable).build()).build();



     }

}
