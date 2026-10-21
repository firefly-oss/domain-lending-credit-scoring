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

import io.fireflysoftware.core.lending.scoring.sdk.api.ScoringModelApi;
import io.fireflysoftware.core.lending.scoring.sdk.model.FilterRequestScoringModelDTO;
import io.fireflysoftware.core.lending.scoring.sdk.model.PaginationResponseScoringModelDTO;
import io.fireflysoftware.domain.lending.creditscoring.core.queries.GetScoringModelsQuery;
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
