package com.loanApplication.application;

import com.loanApplication.domain.LoanRequest;
import com.loanApplication.domain.LoanResponse;

public interface ILoanApplicationService {

    LoanResponse processLoan(LoanRequest loanRequest);
}
