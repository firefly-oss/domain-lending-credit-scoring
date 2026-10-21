package io.fireflysoftware.domain.lending.creditscoring.core.decision;

import org.fireflyframework.rules.core.dsl.evaluation.ASTRulesEvaluationEngine;
import org.fireflyframework.rules.core.dsl.evaluation.ASTRulesEvaluationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * {@link RulesEnginePort} adapter that evaluates a named ruleset with
 * {@link ASTRulesEvaluationEngine} from {@code org.fireflyframework:fireflyframework-rule-engine-core}.
 *
 * <p>The ruleset is data. By default it is {@code classpath:rulesets/{name}.yaml}.
 * Set {@code firefly.decision.rulesets-directory} to a directory of {@code {name}.yaml}
 * files to replace the sample without changing this class.
 */
@Component
public class JaflyRulesEngineAdapter implements RulesEnginePort {

    private static final Pattern RULESET_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,80}");

    private final ASTRulesEvaluationEngine engine;
    private final String rulesetsDirectory;

    public JaflyRulesEngineAdapter(
            ASTRulesEvaluationEngine engine,
            @Value("${firefly.decision.rulesets-directory:}") String rulesetsDirectory) {
        this.engine = engine;
        this.rulesetsDirectory = rulesetsDirectory;
    }

    @Override
    public Mono<RulesEvaluation> evaluate(String rulesetName, Map<String, Object> facts) {
        final String yaml;
        try {
            yaml = load(rulesetName);
        } catch (IOException | IllegalArgumentException ex) {
            return Mono.error(ex);
        }
        Map<String, Object> input = facts == null ? Map.of() : facts;
        return engine.evaluateRulesReactive(yaml, input).map(this::toEvaluation);
    }

    private String load(String rulesetName) throws IOException {
        if (rulesetName == null || !RULESET_NAME.matcher(rulesetName).matches()) {
            throw new IllegalArgumentException("Ruleset name is required and must be a file name");
        }
        String fileName = rulesetName + ".yaml";
        if (rulesetsDirectory != null && !rulesetsDirectory.isBlank()) {
            Path path = Path.of(rulesetsDirectory).resolve(fileName);
            if (!Files.isRegularFile(path)) {
                throw new IllegalArgumentException("Ruleset file not found: " + path);
            }
            return Files.readString(path);
        }
        String resource = "rulesets/" + fileName;
        try (InputStream in = JaflyRulesEngineAdapter.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalArgumentException("Ruleset not found on the classpath: " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private RulesEvaluation toEvaluation(ASTRulesEvaluationResult result) {
        if (result == null || !result.isSuccess()) {
            String error = result == null ? "Rules engine returned no result" : result.getError();
            return new RulesEvaluation(false, null, List.of(), error);
        }
        Object decision = result.getOutputValue("decision");
        return new RulesEvaluation(
                true,
                decision == null ? null : decision.toString(),
                reasonCodes(result.getOutputValue("reasonCodes")),
                null);
    }

    private List<String> reasonCodes(Object raw) {
        if (raw instanceof List<?> list) {
            List<String> codes = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    codes.add(item.toString());
                }
            }
            return List.copyOf(codes);
        }
        if (raw instanceof String code && !code.isBlank()) {
            return List.of(code);
        }
        return List.of();
    }
}
