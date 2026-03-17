package com.firefly.domain.lending.creditscoring.core.queries;

import com.firefly.core.lending.scoring.sdk.model.ScoringCaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fireflyframework.cqrs.query.Query;

import java.util.UUID;

/**
 * Query to retrieve a scoring case by its identifier from core-lending-credit-scoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetScoringCaseQuery implements Query<ScoringCaseDTO> {

    private UUID scoringCaseId;
}
