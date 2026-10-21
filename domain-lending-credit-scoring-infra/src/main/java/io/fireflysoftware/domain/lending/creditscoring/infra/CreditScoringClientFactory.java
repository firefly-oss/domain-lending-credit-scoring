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


package io.fireflysoftware.domain.lending.creditscoring.infra;

import io.fireflysoftware.core.lending.scoring.sdk.api.ScoringCaseApi;
import io.fireflysoftware.core.lending.scoring.sdk.api.ScoringModelApi;
import io.fireflysoftware.core.lending.scoring.sdk.api.ScoringRequestApi;
import io.fireflysoftware.core.lending.scoring.sdk.api.ScoringResultApi;
import io.fireflysoftware.core.lending.scoring.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Creates and exposes WebClient-based API clients for {@code core-lending-credit-scoring}.
 *
 * <p>Uses {@code @Component} (not {@code @Configuration}) per banking platform convention.
 * Base path is injected via {@link CreditScoringProperties}.
 */
@Component
public class CreditScoringClientFactory {

    private final ApiClient apiClient;

    /**
     * Constructs the factory and configures the shared {@link ApiClient} with the base path
     * from {@link CreditScoringProperties}.
     *
     * @param properties configuration properties supplying the downstream base URL
     */
    public CreditScoringClientFactory(CreditScoringProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Exposes a {@link ScoringCaseApi} bean for scoring case lifecycle operations.
     *
     * @return configured scoring case API client
     */
    @Bean
    public ScoringCaseApi scoringCaseApi() {
        return new ScoringCaseApi(apiClient);
    }

    /**
     * Exposes a {@link ScoringRequestApi} bean for scoring request operations.
     *
     * @return configured scoring request API client
     */
    @Bean
    public ScoringRequestApi scoringRequestsApi() {
        return new ScoringRequestApi(apiClient);
    }

    /**
     * Exposes a {@link ScoringResultApi} bean for scoring result retrieval.
     *
     * @return configured scoring result API client
     */
    @Bean
    public ScoringResultApi scoringResultsApi() {
        return new ScoringResultApi(apiClient);
    }

    /**
     * Exposes a {@link ScoringModelApi} bean for scoring model queries.
     *
     * @return configured scoring model API client
     */
    @Bean
    public ScoringModelApi scoringModelsApi() {
        return new ScoringModelApi(apiClient);
    }
}
