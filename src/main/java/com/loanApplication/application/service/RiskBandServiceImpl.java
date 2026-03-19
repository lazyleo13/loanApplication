package com.loanApplication.application.service;

import com.loanApplication.application.IRiskBandService;
import com.loanApplication.domain.LoanResponse;

public class RiskBandServiceImpl implements IRiskBandService {

    @Override
    public LoanResponse.RiskBand checkRiskBand(int creditScore) {

        if (creditScore >= 750)
            return LoanResponse.RiskBand.LOW;

        if (creditScore >= 650)
            return LoanResponse.RiskBand.MEDIUM;

        return LoanResponse.RiskBand.HIGH;    }
}
