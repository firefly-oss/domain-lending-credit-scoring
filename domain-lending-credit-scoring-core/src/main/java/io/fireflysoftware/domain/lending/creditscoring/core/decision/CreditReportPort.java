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
