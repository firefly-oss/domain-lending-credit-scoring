package com.firefly.domain.lending.creditscoring.core.queries;

import com.firefly.core.lending.scoring.sdk.model.ScoringCaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fireflyframework.cqrs.query.Query;

import java.util.UUID;

/**
 * Query to retrieve the status of a scoring request by its domain identifier.
 *
 * <p>At the domain level a "scoring request" corresponds to a {@code ScoringCase}
 * in {@code core-lending-credit-scoring}; {@code requestId} == {@code scoringCaseId}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetScoringRequestQuery implements Query<ScoringCaseDTO> {

    /** Domain-level request identifier (equals {@code scoringCaseId} in the core service). */
    private UUID requestId;
}
