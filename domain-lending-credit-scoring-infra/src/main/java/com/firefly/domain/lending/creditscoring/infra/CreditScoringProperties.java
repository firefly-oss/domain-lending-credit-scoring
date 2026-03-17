package com.firefly.domain.lending.creditscoring.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the core-lending-credit-scoring downstream service.
 * Bound from {@code api-configuration.core-platform.credit-scoring} in application.yaml.
 *
 * <p>No {@code @Configuration} annotation — the application class carries
 * {@code @ConfigurationPropertiesScan} to register all {@code @ConfigurationProperties} beans.
 */
@ConfigurationProperties(prefix = "api-configuration.core-platform.credit-scoring")
@Data
public class CreditScoringProperties {

    private String basePath;
}
