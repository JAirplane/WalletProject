package com.jefferson.wallet.service;

import com.jefferson.wallet.dto.BalanceDto;
import com.jefferson.wallet.dto.TransactionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface PaymentService {
    BalanceDto processTransaction(@NotNull(message = "Transaction request mustn't be null")
                                  @Valid
                                  TransactionRequest transactionRequest);
}
