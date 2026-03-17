package com.firefly.domain.lending.creditscoring.core.handlers;

import com.firefly.core.lending.scoring.sdk.api.ScoringRequestApi;
import com.firefly.core.lending.scoring.sdk.api.ScoringResultApi;
import com.firefly.core.lending.scoring.sdk.model.FilterRequestScoringRequestDTO;
import com.firefly.core.lending.scoring.sdk.model.FilterRequestScoringResultDTO;
import com.firefly.core.lending.scoring.sdk.model.ScoringResultDTO;
import com.firefly.domain.lending.creditscoring.core.queries.GetScoringResultQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.annotations.QueryHandlerComponent;
import org.fireflyframework.cqrs.query.QueryHandler;
import reactor.core.publisher.Mono;

/**
 * CQRS query handler that retrieves the computed scoring result for a request.
 *
 * <p>The resolution chain:
 * <ol>
 *   <li>List {@code ScoringRequest}s for the case (first page, 1 item).</li>
 *   <li>List {@code ScoringResult}s for that request (first page, 1 item).</li>
 * </ol>
 *
 * <p>GET calls do not require an idempotency key.
 */
@Slf4j
@QueryHandlerComponent
@RequiredArgsConstructor
public class GetScoringResultHandler extends QueryHandler<GetScoringResultQuery, ScoringResultDTO> {

    private final ScoringRequestApi scoringRequestsApi;
    private final ScoringResultApi scoringResultsApi;

    @Override
    protected Mono<ScoringResultDTO> doHandle(GetScoringResultQuery query) {
        return scoringRequestsApi.findAll2(
                        query.getRequestId(),
                        new FilterRequestScoringRequestDTO(),
                        null)
                .flatMap(page -> {
                    if (page.getContent() == null || page.getContent().isEmpty()) {
                        return Mono.empty();
                    }
                    var scoringRequestId = page.getContent().get(0).getScoringRequestId();
                    return scoringResultsApi.findAll3(
                                    query.getRequestId(),
                                    scoringRequestId,
                                    new FilterRequestScoringResultDTO(),
                                    null)
                            .mapNotNull(resultPage ->
                                    resultPage.getContent() != null && !resultPage.getContent().isEmpty()
                                            ? resultPage.getContent().get(0)
                                            : null);
                });
    }
}
