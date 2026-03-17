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
public class Applicant {

    private String name;
    private int age;
    private double income;
    private int creditScore;
    private Applicant.EmploymentType employmentType;

    public enum EmploymentType{
        SALARIED("SALARIED"),
        SELF_EMPLOYED("SELF_EMPLOYED");

        private String value;

        EmploymentType(String value){
            this.value = value;
        }

        @JsonValue
        public String getValue(){
            return value;
        }
    }
}
