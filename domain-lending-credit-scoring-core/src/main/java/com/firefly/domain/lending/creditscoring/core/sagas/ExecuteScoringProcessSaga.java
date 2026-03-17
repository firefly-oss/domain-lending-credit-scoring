package com.firefly.domain.lending.creditscoring.core.sagas;

import com.firefly.core.lending.scoring.sdk.api.ScoringBureauCallApi;
import com.firefly.core.lending.scoring.sdk.api.ScoringCaseApi;
import com.firefly.core.lending.scoring.sdk.api.ScoringRequestApi;
import com.firefly.core.lending.scoring.sdk.model.ScoringBureauCallDTO;
import com.firefly.core.lending.scoring.sdk.model.ScoringCaseDTO;
import com.firefly.core.lending.scoring.sdk.model.ScoringRequestDTO;
import com.firefly.domain.lending.creditscoring.core.commands.RequestScoringCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.fireflyframework.orchestration.saga.annotation.Saga;
import org.fireflyframework.orchestration.saga.annotation.SagaStep;
import org.fireflyframework.orchestration.saga.annotation.StepEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

/**
 * Three-step saga that orchestrates the full credit scoring workflow:
 *
 * <ol>
 *   <li>{@code createScoringRequest} — opens a {@code ScoringCase} in core-lending-credit-scoring.</li>
 *   <li>{@code fetchBureauReport} — records a bureau-call interaction against the case.</li>
 *   <li>{@code computeResult} — triggers computation by creating a {@code ScoringRequest}.</li>
 * </ol>
 *
 * <p>On failure the compensation method {@code markRequestFailed} updates the case status
 * so that it is not left in a dangling PENDING state.
 *
 * <p><strong>MEMORY NOTE:</strong> Steps 2 and 3 carry {@code dependsOn} and therefore
 * must NOT rely on their injected {@code cmd} argument (may be {@code null} due to
 * {@code ArgumentResolver} behaviour). Context variables set in step 1 are used instead.
 */
@Slf4j
@Saga(name = "ExecuteScoringProcessSaga")
@Service
@RequiredArgsConstructor
public class ExecuteScoringProcessSaga {

    /** Saga name — must match the {@code name} attribute of {@link Saga} on this class. */
    public static final String SAGA_NAME = "ExecuteScoringProcessSaga";

    /** Step ID for the case-creation step. */
    public static final String STEP_CREATE_SCORING_REQUEST = "createScoringRequest";

    /** Step ID for the bureau-report step. */
    public static final String STEP_FETCH_BUREAU_REPORT = "fetchBureauReport";

    /** Step ID for the result-computation step. */
    public static final String STEP_COMPUTE_RESULT = "computeResult";

    /** Execution-context key used to share the scoring case ID across steps. */
    public static final String CTX_CASE_ID = "scoringCaseId";

    /** Event type emitted when a scoring request is created. */
    public static final String EVENT_SCORING_REQUEST_CREATED = "scoring.request.created";

    /** Event type emitted when a bureau report is fetched. */
    public static final String EVENT_BUREAU_FETCHED = "scoring.bureau.fetched";

    /** Event type emitted when the scoring result is computed. */
    public static final String EVENT_RESULT_COMPUTED = "scoring.result.computed";

    private final ScoringCaseApi scoringCaseApi;
    private final ScoringRequestApi scoringRequestsApi;
    private final ScoringBureauCallApi bureauCallsApi;

    // ─────────────────────────────────────────────────────────────────────────
    // Step 1 — create scoring request (= ScoringCase in core)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Opens a new {@code ScoringCase} and stores its ID as {@value CTX_CASE_ID}
     * in the execution context for use by downstream steps.
     *
     * @param cmd  incoming scoring command carrying application and party IDs
     * @param ctx  shared saga execution context
     * @return the {@code scoringCaseId} that becomes the domain-level {@code requestId}
     */
    @SagaStep(id = "createScoringRequest", compensate = "markRequestFailed")
    @StepEvent(type = "scoring.request.created")
    public Mono<UUID> createScoringRequest(RequestScoringCommand cmd, ExecutionContext ctx) {
        ScoringCaseDTO payload = new ScoringCaseDTO()
                .loanApplicationId(cmd.getApplicationId())
                .customerId(cmd.getPartyId());

        return scoringCaseApi.create1(payload, UUID.randomUUID().toString())
                .mapNotNull(dto -> Objects.requireNonNull(dto).getScoringCaseId())
                .doOnNext(caseId -> {
                    ctx.putVariable(CTX_CASE_ID, caseId);
                    log.info("Scoring case created: scoringCaseId={}", caseId);
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 2 — fetch bureau report (= create ScoringBureauCall in core)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Records a credit-bureau call against the scoring case.
     *
     * <p>Do NOT use an injected {@code cmd} parameter here — use {@code ctx} instead.
     *
     * @param ctx  shared saga execution context; must contain {@value CTX_CASE_ID}
     * @return the {@code scoringBureauCallId} (sentinel so the step never emits empty)
     */
    @SagaStep(id = "fetchBureauReport", dependsOn = "createScoringRequest")
    @StepEvent(type = "scoring.bureau.fetched")
    public Mono<UUID> fetchBureauReport(ExecutionContext ctx) {
        UUID caseId = ctx.getVariableAs(CTX_CASE_ID, UUID.class);

        ScoringBureauCallDTO payload = new ScoringBureauCallDTO()
                .scoringCaseId(caseId)
                .bureauName(ScoringBureauCallDTO.BureauNameEnum.EQUIFAX)
                .isSuccess(true);

        return bureauCallsApi.create4(caseId, payload, UUID.randomUUID().toString())
                .mapNotNull(dto -> Objects.requireNonNull(dto).getScoringBureauCallId())
                .doOnNext(callId -> log.info("Bureau call recorded: scoringBureauCallId={}", callId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 3 — compute result (= create ScoringRequest in core, triggers scoring)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a {@code ScoringRequest} which triggers computation of the
     * {@code ScoringResult} on the core service side.
     *
     * <p>Do NOT use an injected {@code cmd} parameter here — use {@code ctx} instead.
     *
     * @param ctx  shared saga execution context; must contain {@value CTX_CASE_ID}
     * @return the {@code scoringRequestId}
     */
    @SagaStep(id = "computeResult", dependsOn = "fetchBureauReport")
    @StepEvent(type = "scoring.result.computed")
    public Mono<UUID> computeResult(ExecutionContext ctx) {
        UUID caseId = ctx.getVariableAs(CTX_CASE_ID, UUID.class);

        ScoringRequestDTO payload = new ScoringRequestDTO()
                .scoringCaseId(caseId);

        return scoringRequestsApi.create2(caseId, payload, UUID.randomUUID().toString())
                .mapNotNull(dto -> Objects.requireNonNull(dto).getScoringRequestId())
                .doOnNext(reqId -> log.info("Scoring request created: scoringRequestId={}", reqId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Compensation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Compensation for {@code createScoringRequest}: marks the case as CANCELLED
     * so it is not left in a dangling PENDING state.
     *
     * <p>Compensation methods are NOT annotated with {@code @SagaStep} and may return
     * {@code Mono<Void>}.
     *
     * @param caseId  the {@code scoringCaseId} returned by step 1
     */
    public Mono<Void> markRequestFailed(UUID caseId) {
        log.warn("Compensating: marking scoring case as CANCELLED: scoringCaseId={}", caseId);
        ScoringCaseDTO patch = new ScoringCaseDTO()
                .caseStatus(ScoringCaseDTO.CaseStatusEnum.CANCELLED);
        return scoringCaseApi.update1(caseId, patch, UUID.randomUUID().toString()).then();
    }
}
