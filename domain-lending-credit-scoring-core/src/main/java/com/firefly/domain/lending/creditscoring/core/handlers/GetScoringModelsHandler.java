package com.firefly.domain.lending.creditscoring.core.handlers;

import com.firefly.core.lending.scoring.sdk.api.ScoringModelApi;
import com.firefly.core.lending.scoring.sdk.model.FilterRequestScoringModelDTO;
import com.firefly.core.lending.scoring.sdk.model.PaginationResponseScoringModelDTO;
import com.firefly.domain.lending.creditscoring.core.queries.GetScoringModelsQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.annotations.QueryHandlerComponent;
import org.fireflyframework.cqrs.query.QueryHandler;
import reactor.core.publisher.Mono;

/**
 * CQRS query handler that retrieves all available scoring models.
 *
 * <p>GET calls do not require an idempotency key.
 */
@Slf4j
@QueryHandlerComponent
@RequiredArgsConstructor
public class GetScoringModelsHandler
        extends QueryHandler<GetScoringModelsQuery, PaginationResponseScoringModelDTO> {

    private final ScoringModelApi scoringModelsApi;

    @Override
    protected Mono<PaginationResponseScoringModelDTO> doHandle(GetScoringModelsQuery query) {
        return scoringModelsApi.findAll(
                new FilterRequestScoringModelDTO(),
                null);
    }
}
