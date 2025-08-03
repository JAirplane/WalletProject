package com.jefferson.wallet.service;

import com.jefferson.wallet.dto.BalanceDto;
import com.jefferson.wallet.dto.WalletDto;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {
    WalletDto createWallet();
    BalanceDto getBalance(@NotNull(message = "UUID mustn't be null") UUID walletId);
    void softDeleteWallet(@NotNull(message = "UUID mustn't be null") UUID walletId);
}
