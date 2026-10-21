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


package io.fireflysoftware.domain.lending.creditscoring.web.controllers;

import io.fireflysoftware.domain.lending.creditscoring.core.decision.DecisionPort;
import io.fireflysoftware.domain.lending.creditscoring.core.decision.DecisionRequest;
import io.fireflysoftware.domain.lending.creditscoring.core.decision.DecisionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * HTTP adapter for {@link DecisionPort}. Experience services may call this endpoint
 * or bind to the same request and response fields directly.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Decision", description = "Evaluate an APPROVE, REFER, or DECLINE decision for any journey")
public class DecisionController {

    private final DecisionPort decisionPort;

    @Operation(
            summary = "Evaluate a decision",
            description = "Evaluates a named ruleset for a party, optional product, journey step, and attributes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Decision evaluated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content)
    })
    @PostMapping(
            value = "/api/v1/decisions",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<DecisionResponse>> evaluate(@Valid @RequestBody DecisionRequest request) {
        return decisionPort.evaluate(request).map(ResponseEntity::ok);
    }
}
