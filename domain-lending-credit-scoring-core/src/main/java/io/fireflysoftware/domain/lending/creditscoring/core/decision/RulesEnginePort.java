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
