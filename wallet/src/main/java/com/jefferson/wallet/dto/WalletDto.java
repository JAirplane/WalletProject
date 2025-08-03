package com.jefferson.wallet.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletDto(UUID id, BigDecimal balance, Instant timestamp) {

    public static WalletDto buildWallet(UUID id, BigDecimal balance) {
        return new WalletDto(id, balance, Instant.now());
    }
}
