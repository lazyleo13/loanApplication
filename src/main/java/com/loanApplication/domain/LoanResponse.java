package com.loanApplication.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponse {

    @Id
    private int uuid;
    private String status;
    private LoanResponse.RiskBand riskband;
    private Offer offer;
    private List<String> rejectedReasons;
    public enum RiskBand {
        LOW("LOW"),
        MEDIUM("MEDIUM"),
        HIGH("HIGH");

        private String value;

        RiskBand(String value){
            this.value = value;
        }

        @JsonValue
        public String getValue(){
            return value;
        }
    }
}
