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

    public int persistPgTransaction(
        String orderId,
        String method,
        String amount,
        String currency)
        throws SQLException {

        String sql =
            "INSERT INTO transacciones " +
            "(id_orden, metodo_pago, monto, moneda, estado) " +
            "VALUES (?, ?, ?::numeric, ?, 'PENDIENTE') " +
            "RETURNING id_transaccion";

        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderId);
            stmt.setString(2, method);
            stmt.setString(3, amount);
            stmt.setString(4, currency);

            try (var rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                        "PostgreSQL no genero un transactionId"
                    );
                }

                int transactionId =
                    rs.getInt("id_transaccion");

                System.out.println(
                    "[DB] Transaccion registrada: TxID "
                    + transactionId
                );

                return transactionId;
            }
        }
    }

    public void updateTransactionResult(
            int transactionId,
            String status,
            String externalRef,
            String detail) throws SQLException {

        if (!status.equals("CONFIRMED") && !status.equals("FAILED")) {
            throw new SQLException(
                "Estado final invalido: " + status
            );
        }

        String selectSql =
            "SELECT estado " +
            "FROM transacciones " +
            "WHERE id_transaccion = ? " +
            "FOR UPDATE";

        String updateSql =
            "UPDATE transacciones " +
            "SET estado = ?, " +
            "    referencia_externa = ?, " +
            "    detalle_respuesta = ? " +
            "WHERE id_transaccion = ?";

        try (Connection conn = getConnection()) {

            conn.setAutoCommit(false);

            try {
                String currentStatus;

                // 1. Buscar y bloquear la transacción
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

                    selectStmt.setInt(1, transactionId);

                    try (var rs = selectStmt.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException("No existe la transaccion con TxID " + transactionId);
                        }

                        currentStatus = rs.getString("estado");
                    }
                }

                // 2. Si ya tiene exactamente el mismo estado,
                //    el callback es un duplicado válido.
                if (currentStatus.equals(status)) {

                    System.out.println("[DB] Callback duplicado ignorado. TxID " + transactionId + " ya estaba en " + status);
                    conn.commit();
                    return;
                }

                // 3. Solo PENDIENTE puede pasar a estado final.
                if (!currentStatus.equals("PENDIENTE")) {

                    throw new SQLException("Transicion de estado invalida para TxID " + transactionId + ": " + currentStatus + " -> " + status);
                }

                // 4. PENDIENTE -> CONFIRMED/FAILED
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

                    updateStmt.setString(1, status);
                    updateStmt.setString(2, externalRef);
                    updateStmt.setString(3, detail);
                    updateStmt.setInt(4, transactionId);

                    int affectedRows = updateStmt.executeUpdate();

                    if (affectedRows != 1) {
                        throw new SQLException("No se pudo actualizar la transaccion " + transactionId);
                    }
                }

                conn.commit();

                System.out.println("[DB] Transaccion actualizada: TxID " + transactionId + " -> " + status);

            } catch (Exception e) {

                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }

                throw new SQLException(
                    "Error actualizando TxID " + transactionId,
                    e
                );
            }
        }
    }
}