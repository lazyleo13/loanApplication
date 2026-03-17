package com.loanApplication.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class LoanRequest {

    private Applicant applicant;
    private Loan loan;
}
