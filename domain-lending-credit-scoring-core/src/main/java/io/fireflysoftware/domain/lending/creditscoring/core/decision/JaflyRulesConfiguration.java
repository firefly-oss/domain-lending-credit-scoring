package io.fireflysoftware.domain.lending.creditscoring.core.decision;

import org.fireflyframework.rules.core.dsl.evaluation.ASTRulesEvaluationEngine;
import org.fireflyframework.rules.core.dsl.parser.ASTRulesDSLParser;
import org.fireflyframework.rules.core.dsl.parser.DSLParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the embedded Jafly rules engine without the rule-engine database.
 * Component scanning the Jafly package is intentionally avoided: that package
 * includes a PostgreSQL configuration this sandbox does not start.
 */
@Configuration
public class JaflyRulesConfiguration {

    @Bean
    public ASTRulesEvaluationEngine astRulesEvaluationEngine() {
        return createEngine();
    }

    public static ASTRulesEvaluationEngine createEngine() {
        return new ASTRulesEvaluationEngine(
                new ASTRulesDSLParser(new DSLParser()),
                new YamlDefaultConstantService());
    }
}
