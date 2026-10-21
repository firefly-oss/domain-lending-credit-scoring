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

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Outbound port. A rules engine evaluates a named ruleset against facts.
 * The shipped adapter calls the Jafly rules engine. An installer replaces the
 * ruleset file, or replaces the adapter when a different runtime is required.
 */
public interface RulesEnginePort {

    /**
     * Evaluates the named ruleset.
     *
     * @param rulesetName ruleset identifier, matching the data file name without an extension
     * @param facts       facts the ruleset reads, including journey and credit attributes
     * @return engine outputs, including {@code decision} and {@code reasonCodes}
     */
    Mono<RulesEvaluation> evaluate(String rulesetName, Map<String, Object> facts);

    /**
     * Result of one ruleset evaluation.
     *
     * @param success     whether the engine completed without an error
     * @param decision    ruleset output {@code decision}, or null when the engine failed
     * @param reasonCodes ruleset output {@code reasonCodes}
     * @param error       engine error message when {@code success} is false
     */
    record RulesEvaluation(
            boolean success,
            String decision,
            List<String> reasonCodes,
            String error
    ) {
    }
}
