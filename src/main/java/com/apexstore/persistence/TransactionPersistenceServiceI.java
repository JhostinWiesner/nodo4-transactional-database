package com.apexstore.persistence;

import com.apexstore.generated.TransactionPersistence.TransactionPersistenceService;
import com.zeroc.Ice.Current;

import java.sql.SQLException;

public class TransactionPersistenceServiceI
        implements TransactionPersistenceService {

    private final TransactionRepository repository;

    public TransactionPersistenceServiceI(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void persistPgTransaction(
            int transactionId,
            String orderId,
            String method,
            String amount,
            String currency,
            Current current) {

        try {
            repository.persistPgTransaction(
                transactionId,
                orderId,
                method,
                amount,
                currency
            );

        } catch (SQLException e) {
            System.err.println(
                "[Persistence Error] No se pudo persistir TxID "
                + transactionId
                + ": "
                + e.getMessage()
            );

            throw new RuntimeException(
                "No se pudo persistir la transaccion " + transactionId,
                e
            );
        }
    }

    @Override
    public void updateTransactionResult(
            int transactionId,
            String status,
            String externalRef,
            String detail,
            Current current) {

        try {
            repository.updateTransactionResult(
                transactionId,
                status,
                externalRef,
                detail
            );

        } catch (SQLException e) {
            System.err.println(
                "[Persistence Error] No se pudo actualizar TxID "
                + transactionId
                + ": "
                + e.getMessage()
            );

            throw new RuntimeException(
                "No se pudo actualizar la transaccion " + transactionId,
                e
            );
        }
    }
}