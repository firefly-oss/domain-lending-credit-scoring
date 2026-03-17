package com.firefly.domain.lending.creditscoring.core.queries;

import com.firefly.core.lending.scoring.sdk.model.PaginationResponseScoringModelDTO;
import org.fireflyframework.cqrs.query.Query;

/**
 * Query to retrieve the list of available scoring models from core-lending-credit-scoring.
 */
public class GetScoringModelsQuery implements Query<PaginationResponseScoringModelDTO> {
}
