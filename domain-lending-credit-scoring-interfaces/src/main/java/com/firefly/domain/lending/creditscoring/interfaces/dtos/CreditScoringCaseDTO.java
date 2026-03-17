package com.firefly.domain.lending.creditscoring.interfaces.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain-level DTO representing a credit scoring case, composed from
 * core-lending-credit-scoring data for consumption by experience-layer clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Credit scoring case with associated request and result summary")
public class CreditScoringCaseDTO {

    @Schema(description = "Scoring case identifier")
    private UUID scoringCaseId;

    @Schema(description = "Loan application this case belongs to")
    private UUID loanApplicationId;

    @Schema(description = "Customer identifier")
    private UUID customerId;

    @Schema(description = "Current status of the scoring case")
    private String caseStatus;

    @Schema(description = "Type of scoring case")
    private String caseType;

    @Schema(description = "Aggregate score value (0.0 – 1.0)")
    private BigDecimal scoreValue;

    @Schema(description = "Outcome recommendation")
    private String scoreOutcome;
}
