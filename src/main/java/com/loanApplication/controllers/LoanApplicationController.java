package com.loanApplication.controllers;

import com.loanApplication.application.ILoanApplicationService;
import com.loanApplication.domain.LoanRequest;
import com.loanApplication.domain.LoanResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final ILoanApplicationService loanApplicationService;

    @PostMapping
    public ResponseEntity<LoanResponse> apply(
            @Valid @RequestBody LoanRequest request) {

        return ResponseEntity.ok(loanApplicationService.processLoan(request));
    }
}