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
import io.fireflysoftware.domain.lending.creditscoring.core.commands.InitiateScoringCaseCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.command.CommandHandler;
import org.fireflyframework.cqrs.annotations.CommandHandlerComponent;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

/**
 * CQRS command handler that bridges {@link InitiateScoringCaseCommand} to the
 * {@code core-lending-credit-scoring} REST API.
 *
 * <p>Every mutating SDK call passes a freshly generated idempotency key so that
 * retries (saga compensation, Resilience4j) are safe.
 */
@Slf4j
@CommandHandlerComponent
@RequiredArgsConstructor
public class InitiateScoringCaseHandler extends CommandHandler<InitiateScoringCaseCommand, UUID> {

    private final ScoringCaseApi scoringCaseApi;

    @Override
    protected Mono<UUID> doHandle(InitiateScoringCaseCommand cmd) {
        ScoringCaseDTO payload = new ScoringCaseDTO()
                .loanApplicationId(cmd.getLoanApplicationId())
                .customerId(cmd.getCustomerId())
                .caseType(ScoringCaseDTO.CaseTypeEnum.fromValue(cmd.getCaseType()));

        return scoringCaseApi.create1(payload, UUID.randomUUID().toString())
                .mapNotNull(dto -> Objects.requireNonNull(dto).getScoringCaseId());
    }
}
