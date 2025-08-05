package com.jefferson.wallet.controller;

import com.jefferson.wallet.dto.BalanceDto;
import com.jefferson.wallet.dto.TransactionRequest;
import com.jefferson.wallet.dto.WalletDto;
import com.jefferson.wallet.service.PaymentService;
import com.jefferson.wallet.service.PaymentServiceImpl;
import com.jefferson.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1", produces = "application/json")
public class WalletController implements WalletApi {

    private final WalletService walletService;

    private final PaymentService paymentService;

    @Autowired
    public WalletController(WalletService walletService, PaymentService paymentService) {
        this.walletService = walletService;
        this.paymentService = paymentService;
    }

    @Override
    @PostMapping("/wallets")
    public ResponseEntity<WalletDto> createNewWallet() {
        log.info("Processing wallet creation request. Timestamp: {}", Instant.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createWallet());
    }

    @Override
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<BalanceDto> getWalletBalance(@PathVariable UUID walletId) {
        log.info("Processing get wallet balance request. Wallet id: {}.Timestamp: {}", walletId, Instant.now());

        BalanceDto balanceDto = walletService.getBalance(walletId);
        return ResponseEntity.ok(balanceDto);
    }

    @Override
    @DeleteMapping("/wallet/{walletId}")
    public ResponseEntity<Void> deleteWallet(@PathVariable UUID walletId) {
        log.info("Processing delete wallet request. Wallet id: {}. Timestamp: {}", walletId, Instant.now());

        walletService.softDeleteWallet(walletId);

        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/wallet")
    public ResponseEntity<BalanceDto> processWalletOperation(@RequestBody @Valid TransactionRequest transactionRequest) {
        log.info("Processing wallet operation balance request. Wallet id: {}. Operation type: {}. Amount: {}. Timestamp: {}",
                transactionRequest.walletId(), transactionRequest.operationType(), transactionRequest.amount(), Instant.now());

        BalanceDto balanceDto = paymentService.processTransaction(transactionRequest);

        return ResponseEntity.ok(balanceDto);
    }

    @GetMapping("/max-retries")
    public ResponseEntity<Integer> getMaxRetries() {
        return ResponseEntity.ok(((PaymentServiceImpl) paymentService).getTransactionMaxRetries());
    }
}
