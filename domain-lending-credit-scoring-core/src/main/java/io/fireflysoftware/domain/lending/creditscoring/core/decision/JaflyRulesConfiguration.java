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


package io.fireflysoftware.domain.lending.creditscoring.core.decision;

import org.fireflyframework.rules.core.dsl.evaluation.ASTRulesEvaluationEngine;
import org.fireflyframework.rules.core.dsl.parser.ASTRulesDSLParser;
import org.fireflyframework.rules.core.dsl.parser.DSLParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the embedded Jafly rules engine without the rule-engine database.
 * Component scanning the Jafly package is intentionally avoided: that package
 * includes a PostgreSQL configuration this sandbox does not start.
 */
@Configuration
public class JaflyRulesConfiguration {

    @Bean
    public ASTRulesEvaluationEngine astRulesEvaluationEngine() {
        return createEngine();
    }

    public static ASTRulesEvaluationEngine createEngine() {
        return new ASTRulesEvaluationEngine(
                new ASTRulesDSLParser(new DSLParser()),
                new YamlDefaultConstantService());
    }
}
