package com.loanApplication.application.service;

import com.loanApplication.application.IEligibilityCheckService;
import com.loanApplication.application.IInterestRateCalculationService;
import com.loanApplication.application.ILoanApplicationService;
import com.loanApplication.application.IRiskBandService;
import com.loanApplication.domain.LoanRequest;
import com.loanApplication.domain.LoanResponse;

import java.util.List;

public class LoanApplicationServiceImpl implements ILoanApplicationService {

    private IEligibilityCheckService eligibilityCheckService;
    private IInterestRateCalculationService interestRateCalculationService;
    private IRiskBandService riskBandService;


    public LoanApplicationServiceImpl(IEligibilityCheckService eligibilityCheckService,
                                      IInterestRateCalculationService interestRateCalculationService, IRiskBandService riskBandService) {
        this.eligibilityCheckService = eligibilityCheckService;
        this.interestRateCalculationService = interestRateCalculationService;
        this.riskBandService = riskBandService;
    }

    public LoanResponse processLoan(LoanRequest loanRequest){

        return new LoanResponse();

     }

}
