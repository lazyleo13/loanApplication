package com.loanApplication.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class Loan {

    private double amount;
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
