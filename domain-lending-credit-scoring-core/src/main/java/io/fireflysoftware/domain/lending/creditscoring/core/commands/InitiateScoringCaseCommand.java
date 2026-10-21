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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fireflyframework.cqrs.command.Command;

import java.util.UUID;

/**
 * Command to open a new scoring case for a loan application.
 * The domain saga dispatches this command via the {@code CommandBus},
 * and the handler forwards it to {@code core-lending-credit-scoring}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateScoringCaseCommand implements Command<UUID> {

    private UUID loanApplicationId;
    private UUID customerId;
    private String caseType;
}
