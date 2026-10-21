package io.fireflysoftware.domain.lending.creditscoring.core.decision;

import reactor.core.publisher.Mono;

/**
 * Inbound port. Experience services evaluate a decision through this contract.
 */
public interface DecisionPort {

    /**
     * Evaluates a decision for one party at one journey step.
     *
     * @param request party id, optional product id, journey step, and attributes
     * @return outcome and reason codes
     */
    Mono<DecisionResponse> evaluate(DecisionRequest request);
}
