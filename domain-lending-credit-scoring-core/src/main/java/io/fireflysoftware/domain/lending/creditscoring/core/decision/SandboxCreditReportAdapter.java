package io.fireflysoftware.domain.lending.creditscoring.core.decision;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Sandbox credit report. Returns one fixed illustrative report and does not call the network.
 *
 * <p>The illustrative score is {@value #EXAMPLE_CREDIT_SCORE}. The sample ruleset's
 * {@code MIN_CREDIT_SCORE} is 680, so a journey that does not supply its own score is referred.
 * Replace this class with another {@link CreditReportPort} and set
 * {@code firefly.decision.credit-report} to a value other than {@code sandbox}.
 * {@code domain-data-credit-bureaus} does not expose an HTTP API this service can call,
 * so the sandbox profile does not require that process to be running.
 */
@Component
@ConditionalOnProperty(prefix = "firefly.decision", name = "credit-report", havingValue = "sandbox", matchIfMissing = true)
public class SandboxCreditReportAdapter implements CreditReportPort {

    /** Illustrative score returned for every party. Below the sample ruleset threshold of 680. */
    public static final int EXAMPLE_CREDIT_SCORE = 640;

    @Override
    public Mono<CreditReport> findByPartyId(UUID partyId) {
        return Mono.just(new CreditReport(EXAMPLE_CREDIT_SCORE, Boolean.FALSE, "sandbox"));
    }
}
