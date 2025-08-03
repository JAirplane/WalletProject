package com.jefferson.wallet.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BalanceDto(UUID walletId, BigDecimal balance, Instant timestamp) {

    public static BalanceDto buildBalanceDto(UUID walletId, BigDecimal balance) {
        return new BalanceDto(walletId, balance, Instant.now());
    }
}
