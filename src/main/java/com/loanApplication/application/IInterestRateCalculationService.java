package com.loanApplication.application;

import com.loanApplication.domain.LoanRequest;

import java.math.BigDecimal;

public interface IInterestRateCalculationService {


    public BigDecimal calculateInterestRate(LoanRequest request);

}
