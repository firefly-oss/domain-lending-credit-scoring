package com.firefly.domain.lending.creditscoring.core.handlers;

import com.firefly.core.lending.scoring.sdk.api.ScoringBureauCallApi;
import com.firefly.core.lending.scoring.sdk.model.FilterRequestScoringBureauCallDTO;
import com.firefly.core.lending.scoring.sdk.model.PaginationResponseScoringBureauCallDTO;
import com.firefly.domain.lending.creditscoring.core.queries.GetBureauReportsQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.annotations.QueryHandlerComponent;
import org.fireflyframework.cqrs.query.QueryHandler;
import reactor.core.publisher.Mono;

/**
 * CQRS query handler that retrieves all bureau call records for a scoring case.
 *
 * <p>GET calls do not require an idempotency key.
 */
@Slf4j
@QueryHandlerComponent
@RequiredArgsConstructor
public class GetBureauReportsHandler
        extends QueryHandler<GetBureauReportsQuery, PaginationResponseScoringBureauCallDTO> {

    private final ScoringBureauCallApi bureauCallsApi;

    @Override
    protected Mono<PaginationResponseScoringBureauCallDTO> doHandle(GetBureauReportsQuery query) {
        return bureauCallsApi.findAll4(
                query.getRequestId(),
                new FilterRequestScoringBureauCallDTO(),
                null);
    }
}
