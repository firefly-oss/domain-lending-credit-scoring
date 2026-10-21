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


package io.fireflysoftware.domain.lending.creditscoring.core.commands;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fireflyframework.cqrs.command.Command;
import org.fireflyframework.orchestration.saga.engine.SagaResult;

import java.util.UUID;

/**
 * Command to execute the full three-step scoring process for a loan application.
 *
 * <p>Handled by {@code RequestScoringHandler}, which dispatches
 * {@code ExecuteScoringProcessSaga} (create case → fetch bureau report → compute result).
 *
 * @see io.fireflysoftware.domain.lending.creditscoring.core.handlers.RequestScoringHandler
 * @see io.fireflysoftware.domain.lending.creditscoring.core.sagas.ExecuteScoringProcessSaga
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestScoringCommand implements Command<SagaResult> {

    /** Loan application driving this scoring request (maps to {@code loanApplicationId} in core). */
    @NotNull(message = "applicationId is required")
    private UUID applicationId;

    /** Customer / party to be scored (maps to {@code customerId} in core). */
    @NotNull(message = "partyId is required")
    private UUID partyId;

    /** Optional scoring model override; if {@code null} the core service picks the active model. */
    private UUID scoringModelId;
}
