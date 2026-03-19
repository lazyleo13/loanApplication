package com.loanApplication.application;

import java.math.BigDecimal;

public interface IEMICalculatorService {

    public BigDecimal calculateEmi(BigDecimal principalAmt, BigDecimal rate , int tenure);
}
