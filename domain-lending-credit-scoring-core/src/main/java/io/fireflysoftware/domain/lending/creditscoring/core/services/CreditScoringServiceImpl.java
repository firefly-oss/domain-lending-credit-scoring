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


package io.fireflysoftware.domain.lending.creditscoring.core.services;

import io.fireflysoftware.core.lending.scoring.sdk.model.PaginationResponseScoringBureauCallDTO;
import io.fireflysoftware.core.lending.scoring.sdk.model.PaginationResponseScoringModelDTO;
import io.fireflysoftware.core.lending.scoring.sdk.model.ScoringCaseDTO;
import io.fireflysoftware.core.lending.scoring.sdk.model.ScoringResultDTO;
import io.fireflysoftware.domain.lending.creditscoring.core.commands.InitiateScoringCaseCommand;
import io.fireflysoftware.domain.lending.creditscoring.core.commands.RequestScoringCommand;
import io.fireflysoftware.domain.lending.creditscoring.core.queries.GetBureauReportsQuery;
import io.fireflysoftware.domain.lending.creditscoring.core.queries.GetScoringCaseQuery;
import io.fireflysoftware.domain.lending.creditscoring.core.queries.GetScoringModelsQuery;
import io.fireflysoftware.domain.lending.creditscoring.core.queries.GetScoringRequestQuery;
import io.fireflysoftware.domain.lending.creditscoring.core.queries.GetScoringResultQuery;
import io.fireflysoftware.domain.lending.creditscoring.core.workflows.InitiateScoringCaseSaga;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.cqrs.query.QueryBus;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementation of {@link CreditScoringService}.
 *
 * <p>State-changing operations run through {@link SagaEngine} or {@link CommandBus}
 * for automatic compensation and CQRS traceability. Read operations go directly through
 * {@link QueryBus}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditScoringServiceImpl implements CreditScoringService {

    private final SagaEngine engine;
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    // ── Legacy ───────────────────────────────────────────────────────────────

    @Override
    public Mono<SagaResult> initiateScoring(@Valid InitiateScoringCaseCommand command) {
        StepInputs inputs = StepInputs.builder()
                .forStepId(InitiateScoringCaseSaga.STEP_INITIATE_SCORING_CASE, command)
                .build();
        return engine.execute(InitiateScoringCaseSaga.SAGA_NAME, inputs);
    }

    @Override
    public Mono<ScoringCaseDTO> getScoringCase(UUID scoringCaseId) {
        return queryBus.query(GetScoringCaseQuery.builder()
                .scoringCaseId(scoringCaseId)
                .build());
    }

    // ── Full workflow ─────────────────────────────────────────────────────────

    @Override
    public Mono<SagaResult> requestScoring(@Valid RequestScoringCommand command) {
        return commandBus.send(command);
    }

    @Override
    public Mono<ScoringCaseDTO> getScoringRequest(UUID requestId) {
        return queryBus.query(GetScoringRequestQuery.builder()
                .requestId(requestId)
                .build());
    }

    @Override
    public Mono<ScoringResultDTO> getScoringResult(UUID requestId) {
        return queryBus.query(GetScoringResultQuery.builder()
                .requestId(requestId)
                .build());
    }

    @Override
    public Mono<PaginationResponseScoringBureauCallDTO> getBureauReports(UUID requestId) {
        return queryBus.query(GetBureauReportsQuery.builder()
                .requestId(requestId)
                .build());
    }

    @Override
    public Mono<PaginationResponseScoringModelDTO> getScoringModels() {
        return queryBus.query(new GetScoringModelsQuery());
    }
}
