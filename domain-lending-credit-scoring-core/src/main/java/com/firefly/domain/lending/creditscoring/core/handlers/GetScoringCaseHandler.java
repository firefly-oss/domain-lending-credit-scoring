package com.firefly.domain.lending.creditscoring.core.handlers;

import com.firefly.core.lending.scoring.sdk.api.ScoringCaseApi;
import com.firefly.core.lending.scoring.sdk.model.ScoringCaseDTO;
import com.firefly.domain.lending.creditscoring.core.queries.GetScoringCaseQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.query.QueryHandler;
import org.fireflyframework.cqrs.annotations.QueryHandlerComponent;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * CQRS query handler that retrieves a scoring case from core-lending-credit-scoring.
 * GET calls do not require an idempotency key — {@code null} is acceptable.
 */
@Slf4j
@QueryHandlerComponent
@RequiredArgsConstructor
public class GetScoringCaseHandler extends QueryHandler<GetScoringCaseQuery, ScoringCaseDTO> {

    private final ScoringCaseApi scoringCaseApi;

    @Override
    protected Mono<ScoringCaseDTO> doHandle(GetScoringCaseQuery query) {
        return scoringCaseApi.getById1(query.getScoringCaseId(), UUID.randomUUID().toString());
    }
}
