package com.loanApplication.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class Offer {

    private float interestRate;
    private int tenureMonths;
    private double emi;
    private double totalPayable;
}
