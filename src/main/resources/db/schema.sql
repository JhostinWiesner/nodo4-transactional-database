DROP TABLE IF EXISTS productos;
DROP TABLE IF EXISTS transacciones;

CREATE SEQUENCE transacciones_id_seq
    START WITH 1000
    INCREMENT BY 1;

-- Tabla de Transacciones (persistida por el servicio persistPgTransaction)
CREATE TABLE transacciones (
    id_transaccion INT PRIMARY KEY
        DEFAULT nextval('transacciones_id_seq'),                    -- Alineado con 'int transactionId'
    id_orden VARCHAR(64) NOT NULL,                        -- Asociado al CheckoutService
    metodo_pago VARCHAR(30) NOT NULL,                     -- STRIPE, PSE, CRYPTO
    monto NUMERIC(10, 2) NOT NULL,                        -- Mapeado desde 'string amount'
    moneda VARCHAR(10) NOT NULL DEFAULT 'USD',             -- Alineado con 'string currency'
    estado VARCHAR(20) NOT NULL
        CHECK (estado IN ('PENDIENTE', 'CONFIRMED', 'FAILED')),             -- PENDIENTE, CONFIRMED, FAILED
    referencia_externa VARCHAR(100),                      -- Alineado con 'externalRef'
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    detalle_respuesta TEXT                                -- rejectedCause / failureCause
);