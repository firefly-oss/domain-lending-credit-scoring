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

import org.fireflyframework.core.filters.FilterRequest;
import org.fireflyframework.core.queries.PaginationResponse;
import org.fireflyframework.rules.core.services.ConstantService;
import org.fireflyframework.rules.interfaces.dtos.crud.ConstantDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps rule constants in memory for this decision service.
 * A missing code still returns empty so the ruleset file default is used.
 */
final class YamlDefaultConstantService implements ConstantService {

    private final ConcurrentHashMap<UUID, ConstantDTO> byId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UUID> idByCode = new ConcurrentHashMap<>();

    @Override
    public Mono<PaginationResponse<ConstantDTO>> filterConstants(FilterRequest<ConstantDTO> filterRequest) {
        List<ConstantDTO> matches = new ArrayList<>(byId.values());
        if (filterRequest != null && filterRequest.getFilters() != null && filterRequest.getFilters().getCode() != null) {
            String code = filterRequest.getFilters().getCode();
            matches = matches.stream().filter(constant -> code.equals(constant.getCode())).toList();
        }
        PaginationResponse<ConstantDTO> page = new PaginationResponse<>(matches, matches.size(), 1, 0);
        return Mono.just(page);
    }

    @Override
    public Mono<ConstantDTO> createConstant(ConstantDTO constantDTO) {
        if (constantDTO.getId() == null) {
            constantDTO.setId(UUID.randomUUID());
        }
        byId.put(constantDTO.getId(), constantDTO);
        if (constantDTO.getCode() != null) {
            idByCode.put(constantDTO.getCode(), constantDTO.getId());
        }
        return Mono.just(constantDTO);
    }

    @Override
    public Mono<ConstantDTO> updateConstant(UUID constantId, ConstantDTO constantDTO) {
        ConstantDTO existing = byId.get(constantId);
        if (existing == null) {
            return Mono.empty();
        }
        constantDTO.setId(constantId);
        if (existing.getCode() != null) {
            idByCode.remove(existing.getCode());
        }
        byId.put(constantId, constantDTO);
        if (constantDTO.getCode() != null) {
            idByCode.put(constantDTO.getCode(), constantId);
        }
        return Mono.just(constantDTO);
    }

    @Override
    public Mono<Void> deleteConstant(UUID constantId) {
        ConstantDTO removed = byId.remove(constantId);
        if (removed != null && removed.getCode() != null) {
            idByCode.remove(removed.getCode(), constantId);
        }
        return Mono.empty();
    }

    @Override
    public Mono<ConstantDTO> getConstantById(UUID constantId) {
        ConstantDTO constant = byId.get(constantId);
        return constant == null ? Mono.empty() : Mono.just(constant);
    }

    @Override
    public Mono<ConstantDTO> getConstantByCode(String code) {
        UUID id = idByCode.get(code);
        if (id == null) {
            return Mono.empty();
        }
        return getConstantById(id);
    }

    @Override
    public Flux<ConstantDTO> getConstantsByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Flux.empty();
        }
        return Flux.fromIterable(codes).flatMap(this::getConstantByCode);
    }
}
