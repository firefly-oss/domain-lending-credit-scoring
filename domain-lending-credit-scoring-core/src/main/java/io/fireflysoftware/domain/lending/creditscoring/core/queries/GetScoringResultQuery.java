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


package io.fireflysoftware.domain.lending.creditscoring.core.queries;

import io.fireflysoftware.core.lending.scoring.sdk.model.ScoringResultDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fireflyframework.cqrs.query.Query;

import java.util.UUID;

/**
 * Query to retrieve the computed scoring result for a given domain request.
 *
 * <p>The handler resolves the result by first listing scoring requests under the
 * case ({@code requestId} == {@code scoringCaseId}), then fetching the first
 * {@code ScoringResult} from that request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetScoringResultQuery implements Query<ScoringResultDTO> {

    /** Domain-level request identifier (equals {@code scoringCaseId} in the core service). */
    private UUID requestId;
}
