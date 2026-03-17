package com.firefly.domain.lending.creditscoring.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the credit-bureaus downstream connection.
 * Bound from {@code api-configuration.core-data.credit-bureaus} in application.yaml.
 *
 * <p>Bureau call records are stored in {@code core-lending-credit-scoring} and exposed
 * through {@link CreditBureausClientFactory} via {@code ScoringBureauCallApi} until
 * {@code core-data-credit-bureaus-sdk} is populated with generated client classes.
 */
@ConfigurationProperties(prefix = "api-configuration.core-data.credit-bureaus")
@Data
public class CreditBureausProperties {

    private String basePath;
}
