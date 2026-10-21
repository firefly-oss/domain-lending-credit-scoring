package io.fireflysoftware.domain.lending.creditscoring.core.decision;

import java.util.List;

/**
 * Inbound decision response.
 *
 * @param outcome     APPROVE, REFER, or DECLINE
 * @param reasonCodes reason codes produced by the ruleset
 */
public record DecisionResponse(
        DecisionOutcome outcome,
        List<String> reasonCodes
) {
}
