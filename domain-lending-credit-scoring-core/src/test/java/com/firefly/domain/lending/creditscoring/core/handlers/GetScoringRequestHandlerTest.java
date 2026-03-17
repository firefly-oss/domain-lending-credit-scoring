package com.firefly.domain.lending.creditscoring.core.handlers;

import com.firefly.core.lending.scoring.sdk.api.ScoringCaseApi;
import com.firefly.core.lending.scoring.sdk.model.ScoringCaseDTO;
import com.firefly.domain.lending.creditscoring.core.queries.GetScoringRequestQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GetScoringRequestHandler}.
 */
@ExtendWith(MockitoExtension.class)
class GetScoringRequestHandlerTest {

    @Mock
    private ScoringCaseApi scoringCaseApi;

    private GetScoringRequestHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetScoringRequestHandler(scoringCaseApi);
    }

    @Test
    void doHandle_returnsScoringCase() {
        // given
        UUID requestId = UUID.randomUUID();
        ScoringCaseDTO dto = new ScoringCaseDTO();

        when(scoringCaseApi.getById1(requestId, any())).thenReturn(Mono.just(dto));

        // when / then
        StepVerifier.create(handler.doHandle(GetScoringRequestQuery.builder().requestId(requestId).build()))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void doHandle_propagatesEmptyFromApi() {
        // given
        UUID requestId = UUID.randomUUID();
        when(scoringCaseApi.getById1(requestId, any())).thenReturn(Mono.empty());

        // when / then
        StepVerifier.create(handler.doHandle(GetScoringRequestQuery.builder().requestId(requestId).build()))
                .verifyComplete();
    }
}
