package com.loanApplication.application.service;

import com.loanApplication.application.IEMICalculatorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class EMICalculatorServiceImpl implements IEMICalculatorService {

    public BigDecimal calculateEmi(BigDecimal principalAmt, BigDecimal rate , int tenure){
        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusRPowerN = BigDecimal.ONE.add(monthlyRate).pow(tenure);

        BigDecimal numerator =
                principalAmt.multiply(monthlyRate).multiply(onePlusRPowerN);

        BigDecimal denominator =
                onePlusRPowerN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}
