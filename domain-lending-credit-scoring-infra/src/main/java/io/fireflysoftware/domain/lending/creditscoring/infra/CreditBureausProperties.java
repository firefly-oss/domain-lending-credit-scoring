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
