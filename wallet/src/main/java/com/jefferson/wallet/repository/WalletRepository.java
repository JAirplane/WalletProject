package com.jefferson.wallet.repository;

import com.jefferson.wallet.model.Wallet;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends CrudRepository<Wallet, UUID> {

    @Query("SELECT w FROM Wallet w WHERE w.isActive = true AND w.id = :id")
    Optional<Wallet> findActiveById(@Param("id") UUID id);
}
