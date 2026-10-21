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

package io.fireflysoftware.domain.lending.creditscoring.sdk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fireflysoftware.domain.lending.creditscoring.sdk.invoker.ApiClient;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Calls {@code POST /api/v1/decisions} on domain-lending-credit-scoring.
 */
public class DecisionsApi {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final WebClient webClient;
    private final String basePath;

    public DecisionsApi(ApiClient apiClient) {
        this.webClient = apiClient.getWebClient();
        this.basePath = trim(apiClient.getBasePath());
    }

    public Mono<DecisionView> evaluateDecision(UUID partyId, UUID productId, String journeyStep, Map<String, String> attributes) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("partyId", partyId);
        if (productId != null) {
            body.put("productId", productId);
        }
        body.put("journeyStep", journeyStep);
        body.put("attributes", attributes == null ? Map.of() : attributes);
        return webClient.post()
                .uri(basePath + "/api/v1/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .map(DecisionsApi::toView);
    }

    public record DecisionView(String outcome, List<String> reasonCodes) {
    }

    private static DecisionView toView(String raw) {
        try {
            JsonNode body = raw == null || raw.isBlank() ? MAPPER.createObjectNode() : MAPPER.readTree(raw);
            List<String> reasons = new ArrayList<>();
            JsonNode codes = body.path("reasonCodes");
            if (codes.isArray()) {
                codes.forEach(code -> reasons.add(code.asText("")));
            }
            return new DecisionView(body.path("outcome").asText(""), List.copyOf(reasons));
        } catch (Exception exception) {
            throw new IllegalStateException("DOMAIN_RESPONSE_UNREADABLE");
        }
    }

    private static String trim(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("PROPERTY_NOT_SET");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
