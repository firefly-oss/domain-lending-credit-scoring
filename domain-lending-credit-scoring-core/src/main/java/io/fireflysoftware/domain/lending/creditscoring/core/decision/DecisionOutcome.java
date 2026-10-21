package io.fireflysoftware.domain.lending.creditscoring.core.decision;

/**
 * Outcome of a decision evaluation. The sample ruleset chooses the value.
 */
public enum DecisionOutcome {
    APPROVE,
    REFER,
    DECLINE
}
