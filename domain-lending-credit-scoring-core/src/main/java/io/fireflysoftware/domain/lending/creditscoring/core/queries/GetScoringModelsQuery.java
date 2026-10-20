package io.fireflysoftware.domain.lending.creditscoring.core.queries;

import io.fireflysoftware.core.lending.scoring.sdk.model.PaginationResponseScoringModelDTO;
import org.fireflyframework.cqrs.query.Query;

/**
 * Query to retrieve the list of available scoring models from core-lending-credit-scoring.
 */
public class GetScoringModelsQuery implements Query<PaginationResponseScoringModelDTO> {
}
