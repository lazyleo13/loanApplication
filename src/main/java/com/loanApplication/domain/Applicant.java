package com.loanApplication.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class Applicant {

    @NotNull
    private String name;

    @Min(value =21 , message = "Minimum Age for Loan Application is 21")
    @Max(value =60 , message = "Maximum Age for Loan Application is 60")
    private int age;

    @DecimalMin(value = "0.00", message = "Income must be greater than 10,000")
    private BigDecimal income;

    @Range(min=300,max=900,message = "Credit Score is not between 300-900")
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
