package com.firefly.domain.lending.creditscoring.core.handlers;

import com.firefly.domain.lending.creditscoring.core.commands.RequestScoringCommand;
import com.firefly.domain.lending.creditscoring.core.sagas.ExecuteScoringProcessSaga;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.annotations.CommandHandlerComponent;
import org.fireflyframework.cqrs.command.CommandHandler;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import reactor.core.publisher.Mono;

/**
 * CQRS command handler that dispatches {@link ExecuteScoringProcessSaga}.
 *
 * <p>The saga name string must match the {@code name} attribute of
 * {@code @Saga} on {@link ExecuteScoringProcessSaga}.
 */
@Slf4j
@CommandHandlerComponent
@RequiredArgsConstructor
public class RequestScoringHandler extends CommandHandler<RequestScoringCommand, SagaResult> {

    private final SagaEngine sagaEngine;

    @Override
    protected Mono<SagaResult> doHandle(RequestScoringCommand cmd) {
        log.info("Dispatching ExecuteScoringProcessSaga: applicationId={}", cmd.getApplicationId());
        StepInputs inputs = StepInputs.builder()
                .forStepId(ExecuteScoringProcessSaga.STEP_CREATE_SCORING_REQUEST, cmd)
                .build();
        return sagaEngine.execute(ExecuteScoringProcessSaga.SAGA_NAME, inputs);
    }
}
