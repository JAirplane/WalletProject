package com.jefferson.wallet.service;

import com.jefferson.wallet.dto.BalanceDto;
import com.jefferson.wallet.dto.TransactionRequest;
import com.jefferson.wallet.dto.WalletDto;
import com.jefferson.wallet.enums.OperationType;
import com.jefferson.wallet.exceptions.InsufficientFundsException;
import com.jefferson.wallet.exceptions.WalletNotFoundException;
import com.jefferson.wallet.model.Transaction;
import com.jefferson.wallet.model.Wallet;
import com.jefferson.wallet.repository.TransactionRepository;
import com.jefferson.wallet.repository.WalletRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Validated
public class PaymentServiceImpl implements PaymentService {

    @Getter
    @Setter
    @Value("${wallet.transaction.processing.max-retry}")
    private int transactionMaxRetries;

    private final TransactionRepository transactionRepository;

    private final WalletRepository walletRepository;

    @Autowired
    public PaymentServiceImpl(TransactionRepository transactionRepository,
                          WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional
    public BalanceDto processTransaction(@NotNull(message = "Transaction request mustn't be null")
                                       @Valid TransactionRequest transactionRequest) {

        log.debug("Transaction processing started. WalletId: {}, Operation type: {}, Amount: {}.",
                transactionRequest.walletId(), transactionRequest.operationType(), transactionRequest.amount());

        int retryCount = 0;
        BalanceDto balanceDto = null;
        while (true) {
            try {
                log.debug("Transaction processing try: {}. " +
                                "WalletId: {}, Operation type: {}, Amount: {}. ",
                        retryCount, transactionRequest.walletId(), transactionRequest.operationType(),
                        transactionRequest.amount());
                balanceDto = doProcess(transactionRequest);
                break;
            } catch (OptimisticLockException e) {
                log.debug("Transaction optimistic locking exception. " +
                                "WalletId: {}, Operation type: {}, Amount: {}.",
                        transactionRequest.walletId(), transactionRequest.operationType(), transactionRequest.amount());
                if (++retryCount > transactionMaxRetries) {
                    throw new RuntimeException("Too many retries, failed to process transaction", e);
                }
            }
        }

        log.debug("Transaction processing finished successfully. " +
                        "WalletId: {}, Operation type: {}, Amount: {}.",
                transactionRequest.walletId(), transactionRequest.operationType(), transactionRequest.amount());
        return balanceDto;
    }

    private BalanceDto doProcess(TransactionRequest transactionRequest) {
        Optional<Wallet> wallet = walletRepository.findActiveById(transactionRequest.walletId());
        if(wallet.isEmpty()) {
            log.debug("Transaction processing failed: wallet not found." +
                            " WalletId: {}, Operation type: {}, Amount: {}",
                    transactionRequest.walletId(), transactionRequest.operationType(), transactionRequest.amount());
            throw new WalletNotFoundException("Wallet not found for id: " + transactionRequest.walletId());
        }

        Wallet existWallet = wallet.get();
        if(transactionRequest.operationType().equals(OperationType.WITHDRAW)) {
            if(existWallet.getBalance().compareTo(transactionRequest.amount()) < 0) {
                log.debug("Transaction processing failed: insufficient funds." +
                                " WalletId: {}, Operation type: {}, Amount: {}.",
                        transactionRequest.walletId(), transactionRequest.operationType(), transactionRequest.amount());
                throw new InsufficientFundsException("Insufficient funds for wallet with id: " + transactionRequest.walletId() +
                        ". Transaction amount: " + transactionRequest.amount() +
                        ". Transaction type: " + transactionRequest.operationType());
            }
            else {
                existWallet.setBalance(existWallet.getBalance().subtract(transactionRequest.amount()));
            }
        }
        else {
            existWallet.setBalance(existWallet.getBalance().add(transactionRequest.amount()));
        }

        log.debug("Transaction processing: balance changed successfully. " +
                        "WalletId: {}, Operation type: {}, Amount: {}.",
                transactionRequest.walletId(), transactionRequest.operationType(), transactionRequest.amount());

        transactionRepository.save(buildTransaction(transactionRequest.walletId(),
                transactionRequest.operationType(), transactionRequest.amount()));
        walletRepository.save(existWallet);

        return BalanceDto.buildBalanceDto(existWallet.getId(), existWallet.getBalance());
    }

    private Transaction buildTransaction(UUID walletId, OperationType operationType,
                                         BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setWalletId(walletId);
        transaction.setOperationType(operationType);
        transaction.setAmount(amount);
        transaction.setCreatedAt(Instant.now());
        return transaction;
    }
}
