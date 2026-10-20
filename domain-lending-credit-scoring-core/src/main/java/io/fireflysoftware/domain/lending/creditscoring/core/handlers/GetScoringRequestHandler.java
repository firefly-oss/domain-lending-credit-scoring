package io.fireflysoftware.domain.lending.creditscoring.core.handlers;

import io.fireflysoftware.core.lending.scoring.sdk.api.ScoringCaseApi;
import io.fireflysoftware.core.lending.scoring.sdk.model.ScoringCaseDTO;
import io.fireflysoftware.domain.lending.creditscoring.core.queries.GetScoringRequestQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.annotations.QueryHandlerComponent;
import org.fireflyframework.cqrs.query.QueryHandler;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * CQRS query handler that fetches a scoring request (= {@code ScoringCase})
 * from {@code core-lending-credit-scoring} by its domain-level request ID.
 *
 * <p>GET calls do not require an idempotency key.
 */
@Slf4j
@QueryHandlerComponent
@RequiredArgsConstructor
public class GetScoringRequestHandler extends QueryHandler<GetScoringRequestQuery, ScoringCaseDTO> {

    private final ScoringCaseApi scoringCaseApi;

    @Override
    protected Mono<ScoringCaseDTO> doHandle(GetScoringRequestQuery query) {
        return scoringCaseApi.getById1(query.getRequestId(), UUID.randomUUID().toString());
    }
}
