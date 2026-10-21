package io.fireflysoftware.domain.lending.creditscoring.core.decision;

/**
 * Journey step a decision is evaluated for. The same decision service serves every journey.
 */
public enum JourneyStep {
    ONBOARDING,
    LENDING_APPLICATION,
    INSURANCE_QUOTE,
    INSURANCE_BIND,
    CLAIM
}
