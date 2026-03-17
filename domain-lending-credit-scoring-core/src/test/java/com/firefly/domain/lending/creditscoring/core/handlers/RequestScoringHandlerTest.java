package com.firefly.domain.lending.creditscoring.core.handlers;

import com.firefly.domain.lending.creditscoring.core.commands.RequestScoringCommand;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RequestScoringHandler}.
 */
@ExtendWith(MockitoExtension.class)
class RequestScoringHandlerTest {

    @Mock
    private SagaEngine sagaEngine;

    private RequestScoringHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RequestScoringHandler(sagaEngine);
    }

    @Test
    void doHandle_executesCorrectSaga_andReturnsResult() {
        // given
        RequestScoringCommand cmd = RequestScoringCommand.builder()
                .applicationId(UUID.randomUUID())
                .partyId(UUID.randomUUID())
                .build();

        SagaResult expectedResult = mock(SagaResult.class);
        when(sagaEngine.execute(eq("ExecuteScoringProcessSaga"), any(StepInputs.class)))
                .thenReturn(Mono.just(expectedResult));

        // when / then
        StepVerifier.create(handler.doHandle(cmd))
                .expectNext(expectedResult)
                .verifyComplete();

        ArgumentCaptor<StepInputs> inputsCaptor = ArgumentCaptor.forClass(StepInputs.class);
        verify(sagaEngine).execute(eq("ExecuteScoringProcessSaga"), inputsCaptor.capture());
        assertThat(inputsCaptor.getValue()).isNotNull();
    }

    @Test
    void doHandle_propagatesSagaError() {
        // given
        RequestScoringCommand cmd = RequestScoringCommand.builder()
                .applicationId(UUID.randomUUID())
                .partyId(UUID.randomUUID())
                .build();

        when(sagaEngine.execute(eq("ExecuteScoringProcessSaga"), any(StepInputs.class)))
                .thenReturn(Mono.error(new RuntimeException("saga failed")));

        // when / then
        StepVerifier.create(handler.doHandle(cmd))
                .expectErrorMessage("saga failed")
                .verify();
    }
}
