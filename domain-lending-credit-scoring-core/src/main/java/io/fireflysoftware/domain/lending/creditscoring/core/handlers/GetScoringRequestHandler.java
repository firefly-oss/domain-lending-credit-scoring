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
