package com.loanApplication.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Loan {

    @DecimalMin(value = "10000.00", message = "Loan amount must be greater than 10,000")
    @DecimalMax(value = "5000000.00", message = "Loan amount must not exceed 5,000,000")
    private BigDecimal amount;

    @Range(min=6,max=360,message = "Tenure for loan should be between 6 to 360 months")
    private int tenureMonths;
    private Loan.Purpose purpose;


    public enum Purpose{
        PERSONAL("PERSONAL"),
        AUTO("AUTO"),
        HOME("HOME");

        private String value;

        Purpose(String value){
            this.value = value;
        }

        @JsonValue
        public String getValue(){
            return value;
        }
    }
}
