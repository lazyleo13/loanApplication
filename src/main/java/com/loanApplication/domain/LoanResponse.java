package com.loanApplication.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponse {

    @Id
    @JsonProperty("applicationId")
    private UUID uuid;
    private String status;
    private LoanResponse.RiskBand riskBand;
    private Offer offer;
    @JsonProperty("rejectionReasons")
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
