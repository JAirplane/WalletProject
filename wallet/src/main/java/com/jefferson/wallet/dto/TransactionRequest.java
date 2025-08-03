package com.jefferson.wallet.dto;

import com.jefferson.wallet.enums.OperationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionRequest(@NotNull(message = "UUID mustn't be null")
                                 UUID walletId,

                                 @NotNull(message = "Operation type mustn't be null")
                                 OperationType operationType,

                                 @NotNull(message = "Amount field mustn't be null")
                                 @Positive(message = "Amount field must be positive")
                                 BigDecimal amount) {

    public static TransactionRequest buildTransactionRequest(UUID walletId,
                                                      OperationType operationType,
                                                      BigDecimal amount) {
        return new TransactionRequest(walletId, operationType, amount);
    }
}
