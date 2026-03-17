package com.firefly.domain.lending.creditscoring.infra;

import com.firefly.core.lending.scoring.sdk.api.ScoringCaseApi;
import com.firefly.core.lending.scoring.sdk.api.ScoringModelApi;
import com.firefly.core.lending.scoring.sdk.api.ScoringRequestApi;
import com.firefly.core.lending.scoring.sdk.api.ScoringResultApi;
import com.firefly.core.lending.scoring.sdk.invoker.ApiClient;
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
