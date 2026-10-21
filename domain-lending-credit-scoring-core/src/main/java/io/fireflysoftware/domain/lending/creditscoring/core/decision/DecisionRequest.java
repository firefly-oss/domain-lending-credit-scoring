package io.fireflysoftware.domain.lending.creditscoring.core.decision;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Inbound decision request.
 *
 * @param partyId     party the decision is about
 * @param productId   optional product the party is applying for
 * @param journeyStep journey step being decided
 * @param attributes  caller-supplied facts; credit facts may be absent
 */
public record DecisionRequest(
        @NotNull UUID partyId,
        UUID productId,
        @NotNull JourneyStep journeyStep,
        Map<String, Object> attributes
) {
}
