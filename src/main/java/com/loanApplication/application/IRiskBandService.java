package com.loanApplication.application;

import com.loanApplication.domain.LoanResponse;

public interface IRiskBandService {

    public LoanResponse.RiskBand checkRiskBand(int creditScore);
}
