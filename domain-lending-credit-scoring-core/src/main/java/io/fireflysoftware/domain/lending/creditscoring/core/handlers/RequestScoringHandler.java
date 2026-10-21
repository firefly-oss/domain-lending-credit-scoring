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

import io.fireflysoftware.domain.lending.creditscoring.core.commands.RequestScoringCommand;
import io.fireflysoftware.domain.lending.creditscoring.core.sagas.ExecuteScoringProcessSaga;
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
