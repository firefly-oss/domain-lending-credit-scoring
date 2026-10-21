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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Walks the sample ruleset through the Jafly engine. Outcomes come from the YAML file.
 */
class DecisionServiceTest {

    private DecisionService service;

    @BeforeEach
    void setUp() {
        JaflyRulesEngineAdapter rules = new JaflyRulesEngineAdapter(
                JaflyRulesConfiguration.createEngine(),
                "");
        service = new DecisionService(rules, new SandboxCreditReportAdapter(), "openplatform-journey");
    }

    @Test
    void missingCallerScoreUsesSandboxReportAndRefers() {
        StepVerifier.create(service.evaluate(request(Map.of())))
                .assertNext(response -> {
                    assertThat(response.outcome()).isEqualTo(DecisionOutcome.REFER);
                    assertThat(response.reasonCodes()).containsExactly("CREDIT_SCORE_BELOW_THRESHOLD");
                })
                .verifyComplete();
    }

    @Test
    void missingCreditReportAndMissingScoreRefers() {
        DecisionService noReport = new DecisionService(
                new JaflyRulesEngineAdapter(JaflyRulesConfiguration.createEngine(), ""),
                partyId -> Mono.empty(),
                "openplatform-journey");

        StepVerifier.create(noReport.evaluate(request(Map.of("channel", "branch"))))
                .assertNext(response -> {
                    assertThat(response.outcome()).isEqualTo(DecisionOutcome.REFER);
                    assertThat(response.reasonCodes()).containsExactly("CREDIT_ATTRIBUTES_MISSING");
                })
                .verifyComplete();
    }

    @Test
    void scoreBelowDocumentedThresholdRefers() {
        StepVerifier.create(service.evaluate(request(Map.of("creditScore", 679, "highRisk", false))))
                .assertNext(response -> {
                    assertThat(response.outcome()).isEqualTo(DecisionOutcome.REFER);
                    assertThat(response.reasonCodes()).containsExactly("CREDIT_SCORE_BELOW_THRESHOLD");
                })
                .verifyComplete();
    }

    @Test
    void scoreAtThresholdApproves() {
        StepVerifier.create(service.evaluate(request(JourneyStep.INSURANCE_QUOTE,
                Map.of("creditScore", 680, "highRisk", false))))
                .assertNext(response -> {
                    assertThat(response.outcome()).isEqualTo(DecisionOutcome.APPROVE);
                    assertThat(response.reasonCodes()).containsExactly("APPROVED");
                })
                .verifyComplete();
    }

    @Test
    void highRiskAttributeDeclinesEvenWithAStrongScore() {
        StepVerifier.create(service.evaluate(request(JourneyStep.CLAIM,
                Map.of("creditScore", 800, "highRisk", true))))
                .assertNext(response -> {
                    assertThat(response.outcome()).isEqualTo(DecisionOutcome.DECLINE);
                    assertThat(response.reasonCodes()).containsExactly("HIGH_RISK");
                })
                .verifyComplete();
    }

    @Test
    void callerScoreOverridesTheSandboxReport() {
        StepVerifier.create(service.evaluate(request(JourneyStep.LENDING_APPLICATION,
                Map.of("creditScore", 720, "highRisk", false))))
                .assertNext(response -> assertThat(response.outcome()).isEqualTo(DecisionOutcome.APPROVE))
                .verifyComplete();
    }

    @Test
    void replacedRulesetFileChangesTheOutcome(@TempDir Path directory) throws Exception {
        Files.writeString(directory.resolve("installer.yaml"), """
                name: installer
                then:
                  - set decision to "DECLINE"
                  - set reasonCodes to "INSTALLER_RULE"
                output:
                  decision: text
                  reasonCodes: text
                """);
        DecisionService installed = new DecisionService(
                new JaflyRulesEngineAdapter(JaflyRulesConfiguration.createEngine(), directory.toString()),
                new SandboxCreditReportAdapter(),
                "installer");

        StepVerifier.create(installed.evaluate(request(Map.of("creditScore", 800, "highRisk", false))))
                .assertNext(response -> {
                    assertThat(response.outcome()).isEqualTo(DecisionOutcome.DECLINE);
                    assertThat(response.reasonCodes()).containsExactly("INSTALLER_RULE");
                })
                .verifyComplete();
    }

    private static DecisionRequest request(Map<String, Object> attributes) {
        return request(JourneyStep.ONBOARDING, attributes);
    }

    private static DecisionRequest request(JourneyStep step, Map<String, Object> attributes) {
        return new DecisionRequest(UUID.randomUUID(), UUID.randomUUID(), step, attributes);
    }
}
