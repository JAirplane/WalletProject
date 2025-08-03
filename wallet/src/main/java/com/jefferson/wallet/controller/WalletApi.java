package com.jefferson.wallet.controller;

import com.jefferson.wallet.dto.BalanceDto;
import com.jefferson.wallet.dto.TransactionRequest;
import com.jefferson.wallet.dto.WalletDto;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface WalletApi {
    ResponseEntity<WalletDto> createNewWallet();
    ResponseEntity<BalanceDto> getWalletBalance(UUID walletId);
    ResponseEntity<Void> deleteWallet(UUID walletId);
    ResponseEntity<BalanceDto> processWalletOperation(TransactionRequest transactionRequest);
}
