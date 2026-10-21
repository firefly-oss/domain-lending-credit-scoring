package io.fireflysoftware.domain.lending.creditscoring.core.decision;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Outbound port for an optional credit report.
 * The sandbox adapter never calls the network. A deployment replaces that adapter
 * with one that calls domain-data-credit-bureaus or a real bureau.
 */
public interface CreditReportPort {

    /**
     * Loads a credit report for the party.
     * An empty publisher means no report is available and credit facts stay absent.
     *
     * @param partyId party to look up
     * @return a report, or empty when the adapter has nothing to add
     */
    Mono<CreditReport> findByPartyId(UUID partyId);

    /**
     * Credit facts a report may contribute. Null fields are omitted from the ruleset input.
     *
     * @param creditScore illustrative or bureau score, or null when the report has no score
     * @param highRisk    whether the report marks the party high risk, or null when unknown
     * @param source      adapter or bureau name, for operators reading logs
     */
    record CreditReport(Integer creditScore, Boolean highRisk, String source) {
    }
}
