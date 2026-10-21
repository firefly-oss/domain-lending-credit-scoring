package io.fireflysoftware.domain.lending.creditscoring.core.decision;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Decision service for every journey. It assembles facts and asks {@link RulesEnginePort}
 * to evaluate the configured ruleset. Score thresholds are not decided here.
 */
@Service
public class DecisionService implements DecisionPort {

    private final RulesEnginePort rulesEnginePort;
    private final CreditReportPort creditReportPort;
    private final String rulesetName;

    public DecisionService(
            RulesEnginePort rulesEnginePort,
            CreditReportPort creditReportPort,
            @Value("${firefly.decision.ruleset:openplatform-journey}") String rulesetName) {
        this.rulesEnginePort = rulesEnginePort;
        this.creditReportPort = creditReportPort;
        this.rulesetName = rulesetName;
    }

    @Override
    public Mono<DecisionResponse> evaluate(DecisionRequest request) {
        if (request == null || request.partyId() == null || request.journeyStep() == null) {
            return Mono.error(new IllegalArgumentException("partyId and journeyStep are required"));
        }

        Map<String, Object> facts = new LinkedHashMap<>();
        if (request.attributes() != null) {
            request.attributes().forEach((key, value) -> {
                if (key != null && value != null) {
                    facts.put(key, value);
                }
            });
        }
        facts.put("journeyStep", request.journeyStep().name());
        facts.put("partyId", request.partyId().toString());
        if (request.productId() != null) {
            facts.put("productId", request.productId().toString());
        }

        return creditReportPort.findByPartyId(request.partyId())
                .map(report -> mergeMissingCreditFacts(facts, report))
                .defaultIfEmpty(facts)
                .flatMap(merged -> rulesEnginePort.evaluate(rulesetName, merged))
                .flatMap(this::toResponse);
    }

    /**
     * Copies credit facts the caller did not already supply. Does not interpret them.
     */
    private Map<String, Object> mergeMissingCreditFacts(
            Map<String, Object> facts,
            CreditReportPort.CreditReport report) {
        Map<String, Object> merged = new LinkedHashMap<>(facts);
        if (!merged.containsKey("creditScore") && report.creditScore() != null) {
            merged.put("creditScore", report.creditScore());
        }
        if (!merged.containsKey("highRisk") && report.highRisk() != null) {
            merged.put("highRisk", report.highRisk());
        }
        return merged;
    }

    private Mono<DecisionResponse> toResponse(RulesEnginePort.RulesEvaluation evaluation) {
        if (!evaluation.success() || evaluation.decision() == null || evaluation.decision().isBlank()) {
            String message = evaluation.error() == null || evaluation.error().isBlank()
                    ? "Ruleset did not produce a decision"
                    : evaluation.error();
            return Mono.error(new IllegalStateException(message));
        }
        DecisionOutcome outcome;
        try {
            outcome = DecisionOutcome.valueOf(evaluation.decision().trim());
        } catch (IllegalArgumentException ex) {
            return Mono.error(new IllegalStateException(
                    "Ruleset returned an unknown decision: " + evaluation.decision()));
        }
        return Mono.just(new DecisionResponse(outcome, evaluation.reasonCodes()));
    }
}
