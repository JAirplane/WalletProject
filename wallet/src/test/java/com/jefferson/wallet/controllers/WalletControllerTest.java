package com.jefferson.wallet.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefferson.wallet.controller.WalletController;
import com.jefferson.wallet.dto.BalanceDto;
import com.jefferson.wallet.dto.TransactionRequest;
import com.jefferson.wallet.enums.OperationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/test-data.sql")
public class WalletControllerTest extends TestContainersBase {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletController walletController;

    @Test
    void shouldReturnCreatedWallet() throws Exception {

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.balance").value(0.00))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldReturnWalletBalance() throws Exception {

        UUID walletId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        BalanceDto balanceDto = BalanceDto.buildBalanceDto(walletId, BigDecimal.valueOf(500.50));

        mockMvc.perform(get("/api/v1/wallet/{walletId}", walletId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(balanceDto.walletId().toString()))
                .andExpect(jsonPath("$.balance").value(balanceDto.balance()))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldThrowWalletNotFound_GetWalletBalance() throws Exception {

        UUID badWalletId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/wallet/{walletId}", badWalletId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.Error")
                        .value("Get balance request: wallet not found for id: " + badWalletId.toString()));
    }

    @Test
    void shouldReturnBadRequest_InvalidPathVariable_GetWalletBalance() throws Exception {

        mockMvc.perform(get("/api/v1/wallet/abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Error").value("Invalid format: abc"));
    }

    @Test
    void shouldDeleteWallet() throws Exception {

        UUID walletId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

        mockMvc.perform(delete("/api/v1/wallet/{walletId}", walletId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnBadRequest_InvalidPathVariable_userById() throws Exception {

        mockMvc.perform(delete("/api/v1/wallet/abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Error").value("Invalid format: abc"));
    }

    @Test
    void shouldReturnUpdatedBalance_processWalletOperation() throws Exception {

        UUID walletId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        BigDecimal amount = BigDecimal.valueOf(10.50);
        BigDecimal expectedBalance = BigDecimal.valueOf(490.00);
        TransactionRequest transactionRequest =
                TransactionRequest.buildTransactionRequest(walletId, OperationType.WITHDRAW, amount);
        BalanceDto balanceDto = BalanceDto.buildBalanceDto(walletId, expectedBalance);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(balanceDto.walletId().toString()))
                .andExpect(jsonPath("$.balance").value(balanceDto.balance()))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldThrowInsufficientFund_processWalletOperation() throws Exception {

        UUID walletId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        BigDecimal amount = BigDecimal.valueOf(1000.50);
        TransactionRequest transactionRequest =
                TransactionRequest.buildTransactionRequest(walletId, OperationType.WITHDRAW, amount);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.Error")
                        .value("Insufficient funds for wallet with id: " + walletId.toString() +
                                ". Transaction amount: " + transactionRequest.amount() +
                                        ". Transaction type: " + transactionRequest.operationType()));
    }

    @Test
    void shouldReturnBadRequest_NullTransactionRequest_processWalletOperation() throws Exception {

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorTitle").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.cause").value("Request body is invalid"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnBadRequest_NullTransactionRequestWalletId_processWalletOperation() throws Exception {

        TransactionRequest request = TransactionRequest.buildTransactionRequest(
                        null, OperationType.DEPOSIT, BigDecimal.valueOf(100.00));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.walletId").value("UUID mustn't be null"));
    }

    @Test
    void shouldReturnBadRequest_TransactionRequestAmountNotPositive_processWalletOperation() throws Exception {

        UUID walletId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

        TransactionRequest request = TransactionRequest.buildTransactionRequest(
                walletId, OperationType.DEPOSIT, BigDecimal.valueOf(-100.00));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").value("Amount field must be positive"));
    }
}
