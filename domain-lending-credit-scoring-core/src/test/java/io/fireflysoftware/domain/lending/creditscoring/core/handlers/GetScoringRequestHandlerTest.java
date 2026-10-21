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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

        when(scoringCaseApi.getById1(eq(requestId), any())).thenReturn(Mono.just(dto));

        // when / then
        StepVerifier.create(handler.doHandle(GetScoringRequestQuery.builder().requestId(requestId).build()))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void doHandle_propagatesEmptyFromApi() {
        // given
        UUID requestId = UUID.randomUUID();
        when(scoringCaseApi.getById1(eq(requestId), any())).thenReturn(Mono.empty());

        // when / then
        StepVerifier.create(handler.doHandle(GetScoringRequestQuery.builder().requestId(requestId).build()))
                .verifyComplete();
    }
}
