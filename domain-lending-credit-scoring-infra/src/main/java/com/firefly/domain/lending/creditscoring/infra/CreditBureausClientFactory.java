package com.firefly.domain.lending.creditscoring.infra;

import com.firefly.core.lending.scoring.sdk.api.ScoringBureauCallApi;
import com.firefly.core.lending.scoring.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Creates and exposes bureau-report API clients.
 *
 * <p>Bureau call data (external credit-bureau interactions) is currently tracked as
 * {@code ScoringBureauCall} within {@code core-lending-credit-scoring}. When
 * {@code core-data-credit-bureaus-sdk} ships a dedicated {@code BureauReportsApi},
 * replace the return type below with that class.
 *
 * <p>Uses {@code @Component} (not {@code @Configuration}) per banking platform convention.
 */
@Component
public class CreditBureausClientFactory {

    private final ApiClient apiClient;

    /**
     * Constructs the factory and configures the shared {@link ApiClient} with the base path
     * from {@link CreditBureausProperties}.
     *
     * @param properties configuration properties supplying the downstream base URL
     */
    public CreditBureausClientFactory(CreditBureausProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Exposes a {@link ScoringBureauCallApi} bean backed by the shared {@link ApiClient}.
     *
     * @return configured bureau-call API client
     */
    @Bean
    public ScoringBureauCallApi bureauCallsApi() {
        return new ScoringBureauCallApi(apiClient);
    }
}
