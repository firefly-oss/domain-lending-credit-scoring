package com.firefly.domain.lending.creditscoring.core.queries;

import com.firefly.core.lending.scoring.sdk.model.PaginationResponseScoringBureauCallDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fireflyframework.cqrs.query.Query;

import java.util.UUID;

/**
 * Query to retrieve all bureau call records associated with a scoring request.
 *
 * <p>Bureau calls ({@code ScoringBureauCall}) are stored within
 * {@code core-lending-credit-scoring} and scoped to a {@code ScoringCase};
 * {@code requestId} == {@code scoringCaseId}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetBureauReportsQuery implements Query<PaginationResponseScoringBureauCallDTO> {

    /** Domain-level request identifier (equals {@code scoringCaseId} in the core service). */
    private UUID requestId;
}
