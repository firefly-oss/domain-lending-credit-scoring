package io.fireflysoftware.domain.lending.creditscoring.core.decision;

import org.fireflyframework.core.filters.FilterRequest;
import org.fireflyframework.core.queries.PaginationResponse;
import org.fireflyframework.rules.core.services.ConstantService;
import org.fireflyframework.rules.interfaces.dtos.crud.ConstantDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Constant lookup that does not call a database.
 * Jafly then uses the {@code defaultValue} declared on each constant in the ruleset file.
 */
final class YamlDefaultConstantService implements ConstantService {

    private static final String MESSAGE =
            "This decision sandbox does not store rule constants. Thresholds come from the ruleset file.";

    @Override
    public Mono<PaginationResponse<ConstantDTO>> filterConstants(FilterRequest<ConstantDTO> filterRequest) {
        return Mono.error(new UnsupportedOperationException(MESSAGE));
    }

    @Override
    public Mono<ConstantDTO> createConstant(ConstantDTO constantDTO) {
        return Mono.error(new UnsupportedOperationException(MESSAGE));
    }

    @Override
    public Mono<ConstantDTO> updateConstant(UUID constantId, ConstantDTO constantDTO) {
        return Mono.error(new UnsupportedOperationException(MESSAGE));
    }

    @Override
    public Mono<Void> deleteConstant(UUID constantId) {
        return Mono.error(new UnsupportedOperationException(MESSAGE));
    }

    @Override
    public Mono<ConstantDTO> getConstantById(UUID constantId) {
        return Mono.error(new UnsupportedOperationException(MESSAGE));
    }

    @Override
    public Mono<ConstantDTO> getConstantByCode(String code) {
        return Mono.empty();
    }

    @Override
    public Flux<ConstantDTO> getConstantsByCodes(List<String> codes) {
        return Flux.empty();
    }
}
