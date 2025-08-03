package com.jefferson.wallet.repository;

import com.jefferson.wallet.model.Transaction;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {
}
