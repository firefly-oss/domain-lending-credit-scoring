package com.firefly.domain.lending.creditscoring.core.handlers;

import com.firefly.core.lending.scoring.sdk.api.ScoringCaseApi;
import com.firefly.core.lending.scoring.sdk.model.ScoringCaseDTO;
import com.firefly.domain.lending.creditscoring.core.commands.InitiateScoringCaseCommand;
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
