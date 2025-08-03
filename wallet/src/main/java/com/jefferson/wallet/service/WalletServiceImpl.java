package com.jefferson.wallet.service;

import com.jefferson.wallet.dto.BalanceDto;
import com.jefferson.wallet.dto.WalletDto;
import com.jefferson.wallet.exceptions.WalletNotFoundException;
import com.jefferson.wallet.model.Wallet;
import com.jefferson.wallet.repository.WalletRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Autowired
    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional
    public WalletDto createWallet() {
        log.debug("Creating new wallet.");
        Wallet wallet = walletRepository.save(new Wallet());
        log.debug("Wallet created with id: {}.", wallet.getId());
        return WalletDto.buildWallet(wallet.getId(), wallet.getBalance());
    }


    @Override
    public BalanceDto getBalance(@NotNull(message = "UUID mustn't be null") UUID walletId) {

        log.debug("Get wallet balance for id: {}.", walletId);

        Wallet wallet = walletRepository.findActiveById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Get balance request: wallet not found for id: " + walletId));

        return BalanceDto.buildBalanceDto(wallet.getId(), wallet.getBalance());
    }

    @Override
    @Transactional
    public void softDeleteWallet(@NotNull(message = "UUID mustn't be null") UUID walletId) {

        log.debug("Soft delete wallet for id: {}.", walletId);

        Optional<Wallet> optionalWallet = walletRepository.findActiveById(walletId);

        if(optionalWallet.isPresent()) {
            optionalWallet.get().setIsActive(false);
            walletRepository.save(optionalWallet.get());
            log.debug("Soft delete wallet for id: {} failed. Wallet was deactivated.", walletId);
            return;
        }

        log.debug("Soft delete wallet for id: {} failed. Wallet wasn't found.", walletId);
    }
}
