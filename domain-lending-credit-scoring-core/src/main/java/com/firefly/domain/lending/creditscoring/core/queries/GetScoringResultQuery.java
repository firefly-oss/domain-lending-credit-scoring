package com.firefly.domain.lending.creditscoring.core.queries;

import com.firefly.core.lending.scoring.sdk.model.ScoringResultDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fireflyframework.cqrs.query.Query;

import java.util.UUID;

/**
 * Query to retrieve the computed scoring result for a given domain request.
 *
 * <p>The handler resolves the result by first listing scoring requests under the
 * case ({@code requestId} == {@code scoringCaseId}), then fetching the first
 * {@code ScoringResult} from that request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetScoringResultQuery implements Query<ScoringResultDTO> {

    /** Domain-level request identifier (equals {@code scoringCaseId} in the core service). */
    private UUID requestId;
}
