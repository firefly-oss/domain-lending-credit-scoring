/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.domain.lending.creditscoring.core.handlers;

import io.fireflysoftware.core.lending.scoring.sdk.api.ScoringRequestApi;
import io.fireflysoftware.core.lending.scoring.sdk.api.ScoringResultApi;
import io.fireflysoftware.core.lending.scoring.sdk.model.FilterRequestScoringRequestDTO;
import io.fireflysoftware.core.lending.scoring.sdk.model.FilterRequestScoringResultDTO;
import io.fireflysoftware.core.lending.scoring.sdk.model.ScoringResultDTO;
import io.fireflysoftware.domain.lending.creditscoring.core.queries.GetScoringResultQuery;
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
