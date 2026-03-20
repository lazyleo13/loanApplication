package com.loanApplication.application;

import com.loanApplication.domain.LoanRequest;

import java.util.List;

public interface IEligibilityCheckService {

    public List<String> checkEligibility(LoanRequest request);
}
