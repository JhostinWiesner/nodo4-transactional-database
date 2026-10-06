package com.apexstore.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransactionRepository {

    private final String dbUrl;
    private final String user;
    private final String password;

    public TransactionRepository(String dbUrl, String user, String password) {
        this.dbUrl = dbUrl;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, user, password);
    }

    public void persistPgTransaction(
            int transactionId,
            String orderId,
            String method,
            String amount,
            String currency) throws SQLException {

        String sql =
            "INSERT INTO transacciones " +
            "(id_transaccion, id_orden, metodo_pago, monto, moneda, estado) " +
            "VALUES (?, ?, ?, ?::numeric, ?, 'PENDIENTE') " +
            "ON CONFLICT (id_transaccion) DO NOTHING";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, transactionId);
            stmt.setString(2, orderId);
            stmt.setString(3, method);
            stmt.setString(4, amount);
            stmt.setString(5, currency);

            int affectedRows = stmt.executeUpdate();

            System.out.println(
                "[DB] Transaccion registrada: TxID "
                + transactionId
                + " | filas afectadas: "
                + affectedRows
            );
        }
    }

    public void updateTransactionResult(
            int transactionId,
            String status,
            String externalRef,
            String detail) throws SQLException {

        String sql =
            "UPDATE transacciones " +
            "SET estado = ?, " +
            "referencia_externa = ?, " +
            "detalle_respuesta = ? " +
            "WHERE id_transaccion = ? " +
            "AND estado = 'PENDIENTE'";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, externalRef);
            stmt.setString(3, detail);
            stmt.setInt(4, transactionId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException(
                    "No existe la transaccion con TxID " + transactionId
                );
            }

            System.out.println(
                "[DB] Transaccion actualizada: TxID "
                + transactionId
                + " -> "
                + status
            );
        }
    }
}