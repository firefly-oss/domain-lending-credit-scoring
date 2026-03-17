package com.firefly.domain.lending.creditscoring.core.services;

import com.firefly.core.lending.scoring.sdk.model.PaginationResponseScoringBureauCallDTO;
import com.firefly.core.lending.scoring.sdk.model.PaginationResponseScoringModelDTO;
import com.firefly.core.lending.scoring.sdk.model.ScoringCaseDTO;
import com.firefly.core.lending.scoring.sdk.model.ScoringResultDTO;
import com.firefly.domain.lending.creditscoring.core.commands.InitiateScoringCaseCommand;
import com.firefly.domain.lending.creditscoring.core.commands.RequestScoringCommand;
import jakarta.validation.Valid;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Domain service interface for credit scoring orchestration.
 *
 * <p>Coordinates multi-step scoring workflows via CQRS sagas and exposes read
 * operations for the experience layer.
 */
public interface CreditScoringService {

    // ── Legacy single-step case initiation ──────────────────────────────────

    /**
     * Initiates a new scoring case for a loan application (single-step saga).
     *
     * @param command initiation command carrying loan application and customer context
     * @return saga result
     */
    Mono<SagaResult> initiateScoring(@Valid InitiateScoringCaseCommand command);

    /**
     * Retrieves a scoring case by its core-service identifier.
     *
     * @param scoringCaseId scoring case identifier
     * @return the scoring case DTO
     */
    Mono<ScoringCaseDTO> getScoringCase(UUID scoringCaseId);

    // ── Full three-step scoring workflow ─────────────────────────────────────

    /**
     * Executes the full scoring process: create case → fetch bureau report → compute result.
     *
     * @param command scoring request with application ID, party ID, and optional model override
     * @return saga result
     */
    Mono<SagaResult> requestScoring(@Valid RequestScoringCommand command);

    /**
     * Retrieves the current status of a scoring request.
     *
     * @param requestId domain-level request ID (equals {@code scoringCaseId} in core)
     * @return scoring case DTO reflecting current status
     */
    Mono<ScoringCaseDTO> getScoringRequest(UUID requestId);

    /**
     * Retrieves the computed scoring result for a given request.
     *
     * @param requestId domain-level request ID
     * @return latest scoring result DTO
     */
    Mono<ScoringResultDTO> getScoringResult(UUID requestId);

    /**
     * Retrieves all bureau call records associated with a scoring request.
     *
     * @param requestId domain-level request ID
     * @return paginated list of bureau call DTOs
     */
    Mono<PaginationResponseScoringBureauCallDTO> getBureauReports(UUID requestId);

    /**
     * Retrieves available scoring models from the core service.
     *
     * @return paginated list of scoring model DTOs
     */
    Mono<PaginationResponseScoringModelDTO> getScoringModels();
}
