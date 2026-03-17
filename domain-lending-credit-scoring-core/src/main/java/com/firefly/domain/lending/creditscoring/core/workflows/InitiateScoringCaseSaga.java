package com.firefly.domain.lending.creditscoring.core.workflows;

import com.firefly.core.lending.scoring.sdk.api.ScoringCaseApi;
import com.firefly.core.lending.scoring.sdk.model.ScoringCaseDTO;
import com.firefly.domain.lending.creditscoring.core.commands.InitiateScoringCaseCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.orchestration.saga.annotation.Saga;
import org.fireflyframework.orchestration.saga.annotation.SagaStep;
import org.fireflyframework.orchestration.saga.annotation.StepEvent;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.fireflyframework.cqrs.command.CommandBus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Saga that initiates a credit scoring case in core-lending-credit-scoring.
 *
 * <p>The {@code compensate} method removes the created case if a downstream
 * step fails, ensuring no orphaned records are left in the core service.
 */
@Slf4j
@Saga(name = "InitiateScoringCaseSaga")
@Service
@RequiredArgsConstructor
public class InitiateScoringCaseSaga {

    /** Saga name — must match the {@code name} attribute of {@link Saga} on this class. */
    public static final String SAGA_NAME = "InitiateScoringCaseSaga";

    /** Step ID for the case-initiation step. */
    public static final String STEP_INITIATE_SCORING_CASE = "initiateScoringCase";

    /** Event type emitted when a scoring case is initiated. */
    public static final String EVENT_SCORING_CASE_INITIATED = "scoringCase.initiated";

    /** Execution-context key used to share the scoring case ID with dependent steps. */
    public static final String CTX_SCORING_CASE_ID = "scoringCaseId";

    private final CommandBus commandBus;
    private final ScoringCaseApi scoringCaseApi;

    /**
     * Step 1: Open the scoring case via the CQRS command bus.
     * The resulting {@code scoringCaseId} is stored in the saga context
     * for use by dependent steps.
     */
    @SagaStep(id = "initiateScoringCase", compensate = "removeScoringCase")
    @StepEvent(type = "scoringCase.initiated")
    public Mono<UUID> initiateScoringCase(InitiateScoringCaseCommand cmd, ExecutionContext ctx) {
        return commandBus.send(cmd)
                .doOnNext(id -> {
                    ctx.putVariable(CTX_SCORING_CASE_ID, id);
                    log.info("Scoring case initiated: scoringCaseId={}", id);
                });
    }

    /**
     * Compensation: remove the scoring case if a downstream saga step fails.
     * Compensation methods are NOT annotated with {@code @SagaStep} and may
     * return {@code Mono<Void>}.
     */
    public Mono<Void> removeScoringCase(UUID scoringCaseId) {
        log.warn("Compensating: marking scoring case as CANCELLED: scoringCaseId={}", scoringCaseId);
        ScoringCaseDTO patch = new ScoringCaseDTO()
                .caseStatus(ScoringCaseDTO.CaseStatusEnum.CANCELLED);
        return scoringCaseApi.update1(scoringCaseId, patch, UUID.randomUUID().toString()).then();
    }
}
