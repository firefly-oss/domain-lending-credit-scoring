package com.firefly.domain.lending.creditscoring.core.commands;

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
