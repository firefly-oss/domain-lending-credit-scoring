package com.firefly.domain.lending.creditscoring.web.controllers;

import com.firefly.core.lending.scoring.sdk.model.PaginationResponseScoringBureauCallDTO;
import com.firefly.core.lending.scoring.sdk.model.PaginationResponseScoringModelDTO;
import com.firefly.core.lending.scoring.sdk.model.ScoringCaseDTO;
import com.firefly.core.lending.scoring.sdk.model.ScoringResultDTO;
import com.firefly.domain.lending.creditscoring.core.commands.InitiateScoringCaseCommand;
import com.firefly.domain.lending.creditscoring.core.commands.RequestScoringCommand;
import com.firefly.domain.lending.creditscoring.core.services.CreditScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller exposing the credit scoring domain API.
 *
 * <p>All endpoints are fully reactive ({@code Mono}) and follow the platform convention
 * of using UUID path variables for resource identification.
 *
 * <p>The {@code /api/v1/scoring/requests} resource models a scoring request as a domain
 * concept that maps to a {@code ScoringCase} in the core service.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Credit Scoring", description = "Domain-level orchestration for credit scoring workflows")
public class CreditScoringController {

    private final CreditScoringService creditScoringService;

    // ── Full scoring workflow ─────────────────────────────────────────────────

    /**
     * Executes the full credit scoring workflow (create case, fetch bureau data, compute result).
     *
     * @param command scoring request containing application ID, party ID, and optional model override
     * @return {@code 200 OK} with the saga result, or {@code 400} for invalid input
     */
    @Operation(
            summary = "Request scoring",
            description = "Executes the full credit scoring process: creates a scoring case, " +
                          "fetches bureau data, and computes the scoring result."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scoring process initiated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content),
            @ApiResponse(responseCode = "500", description = "Scoring process failed", content = @Content)
    })
    @PostMapping(
            value = "/api/v1/scoring/requests",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<SagaResult>> requestScoring(
            @Valid @RequestBody RequestScoringCommand command) {
        return creditScoringService.requestScoring(command)
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves the current status of a scoring request.
     *
     * @param requestId unique identifier of the scoring request
     * @return {@code 200 OK} with the scoring case DTO, or {@code 404} if not found
     */
    @Operation(
            summary = "Get scoring request status",
            description = "Retrieves the current status of a scoring request by its identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scoring request found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ScoringCaseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Scoring request not found", content = @Content)
    })
    @GetMapping(value = "/api/v1/scoring/requests/{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ScoringCaseDTO>> getScoringRequest(
            @Parameter(description = "Unique identifier of the scoring request", required = true)
            @PathVariable UUID requestId) {
        return creditScoringService.getScoringRequest(requestId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Retrieves the computed scoring result for a given scoring request.
     *
     * @param requestId unique identifier of the scoring request
     * @return {@code 200 OK} with the scoring result DTO, or {@code 404} if not yet available
     */
    @Operation(
            summary = "Get scoring result",
            description = "Retrieves the computed scoring result for a given scoring request."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scoring result found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ScoringResultDTO.class))),
            @ApiResponse(responseCode = "404", description = "Result not yet available", content = @Content)
    })
    @GetMapping(value = "/api/v1/scoring/requests/{requestId}/result", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ScoringResultDTO>> getScoringResult(
            @Parameter(description = "Unique identifier of the scoring request", required = true)
            @PathVariable UUID requestId) {
        return creditScoringService.getScoringResult(requestId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Retrieves all credit bureau call records associated with a scoring request.
     *
     * @param requestId unique identifier of the scoring request
     * @return {@code 200 OK} with paginated bureau call DTOs, or {@code 404} if not found
     */
    @Operation(
            summary = "Get bureau reports",
            description = "Retrieves all credit bureau call records associated with a scoring request."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bureau reports retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PaginationResponseScoringBureauCallDTO.class))),
            @ApiResponse(responseCode = "404", description = "Scoring request not found", content = @Content)
    })
    @GetMapping(value = "/api/v1/scoring/requests/{requestId}/bureau-reports", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PaginationResponseScoringBureauCallDTO>> getBureauReports(
            @Parameter(description = "Unique identifier of the scoring request", required = true)
            @PathVariable UUID requestId) {
        return creditScoringService.getBureauReports(requestId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Retrieves the list of available scoring models from the core service.
     *
     * @return {@code 200 OK} with paginated scoring model DTOs
     */
    @Operation(
            summary = "Get scoring models",
            description = "Retrieves the list of available scoring models from the core service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scoring models retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PaginationResponseScoringModelDTO.class)))
    })
    @GetMapping(value = "/api/v1/scoring/models", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PaginationResponseScoringModelDTO>> getScoringModels() {
        return creditScoringService.getScoringModels()
                .map(ResponseEntity::ok);
    }

    // ── Legacy single-step endpoints (retained for backward compatibility) ────

    /**
     * Opens a new credit scoring case (legacy single-step endpoint).
     * Prefer {@code POST /api/v1/scoring/requests} for the full workflow.
     *
     * @param command initiation command carrying loan application and customer context
     * @return {@code 200 OK} on success
     */
    @Operation(
            summary = "Initiate scoring case (legacy)",
            description = "Opens a new credit scoring case. Prefer POST /api/v1/scoring/requests for the full workflow."
    )
    @PostMapping(
            value = "/api/v1/credit-scoring/cases",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<Object>> initiateScoring(
            @Valid @RequestBody InitiateScoringCaseCommand command) {
        return creditScoringService.initiateScoring(command)
                .thenReturn(ResponseEntity.ok().build());
    }

    /**
     * Retrieves a credit scoring case by its identifier (legacy endpoint).
     * Prefer {@code GET /api/v1/scoring/requests/{requestId}}.
     *
     * @param scoringCaseId unique identifier of the scoring case
     * @return {@code 200 OK} with the scoring case DTO, or {@code 404} if not found
     */
    @Operation(
            summary = "Get scoring case (legacy)",
            description = "Retrieves a credit scoring case by its identifier. Prefer GET /api/v1/scoring/requests/{requestId}."
    )
    @GetMapping(value = "/api/v1/credit-scoring/cases/{scoringCaseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ScoringCaseDTO>> getScoringCase(
            @Parameter(description = "Unique identifier of the scoring case", required = true)
            @PathVariable UUID scoringCaseId) {
        return creditScoringService.getScoringCase(scoringCaseId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
