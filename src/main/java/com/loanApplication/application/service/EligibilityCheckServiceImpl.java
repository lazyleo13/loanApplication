package com.loanApplication.application.service;

import com.loanApplication.application.IEMICalculatorService;
import com.loanApplication.application.IEligibilityCheckService;
import com.loanApplication.application.IRiskBandService;
import com.loanApplication.domain.LoanRequest;
import com.loanApplication.domain.LoanResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EligibilityCheckServiceImpl implements IEligibilityCheckService {

    private final IEMICalculatorService emiCalculatorService;

    public EligibilityCheckServiceImpl(IEMICalculatorService emiCalculatorService) {
        this.emiCalculatorService = emiCalculatorService;
    }

    public List<String> checkEligibility(LoanRequest loanRequest){

        List<String> rejectReasonList = new ArrayList<>();

        if(loanRequest.getApplicant().getCreditScore() < 600){
            rejectReasonList.add("CREDIT SCORE IS LESS THAN 600");
        }

        int currentAge = loanRequest.getApplicant().getAge();
        int tenure = loanRequest.getLoan().getTenureMonths();

        int tenureInYears= tenure/12;

        if((tenureInYears+currentAge) > 60){
            rejectReasonList.add("AGE_TENURE_LIMIT_EXCEEDED");
        }


        BigDecimal incomeValSixtyPercent = loanRequest.getApplicant().getIncome().multiply(new BigDecimal(0.6));
        BigDecimal principalAmt = loanRequest.getLoan().getAmount();
        BigDecimal emi = emiCalculatorService.calculateEmi(principalAmt,new BigDecimal(12),tenure);

        if(emi.compareTo(incomeValSixtyPercent) > 0){
            rejectReasonList.add("EMI_EXCEEDS_60_PERCENT");
        }

        return rejectReasonList;
    }
}
