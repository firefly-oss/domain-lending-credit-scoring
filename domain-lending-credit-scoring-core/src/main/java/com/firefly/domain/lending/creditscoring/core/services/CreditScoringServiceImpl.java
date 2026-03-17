package com.firefly.domain.lending.creditscoring.core.services;

import com.firefly.core.lending.scoring.sdk.model.PaginationResponseScoringBureauCallDTO;
import com.firefly.core.lending.scoring.sdk.model.PaginationResponseScoringModelDTO;
import com.firefly.core.lending.scoring.sdk.model.ScoringCaseDTO;
import com.firefly.core.lending.scoring.sdk.model.ScoringResultDTO;
import com.firefly.domain.lending.creditscoring.core.commands.InitiateScoringCaseCommand;
import com.firefly.domain.lending.creditscoring.core.commands.RequestScoringCommand;
import com.firefly.domain.lending.creditscoring.core.queries.GetBureauReportsQuery;
import com.firefly.domain.lending.creditscoring.core.queries.GetScoringCaseQuery;
import com.firefly.domain.lending.creditscoring.core.queries.GetScoringModelsQuery;
import com.firefly.domain.lending.creditscoring.core.queries.GetScoringRequestQuery;
import com.firefly.domain.lending.creditscoring.core.queries.GetScoringResultQuery;
import com.firefly.domain.lending.creditscoring.core.workflows.InitiateScoringCaseSaga;
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
