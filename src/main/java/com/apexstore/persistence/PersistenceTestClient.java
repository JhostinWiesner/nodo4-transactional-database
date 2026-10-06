package com.apexstore.persistence;

import com.apexstore.generated.TransactionPersistence.TransactionPersistenceServicePrx;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

public class PersistenceTestClient {

    public static void main(String[] args) {
        
        String host = "localhost";
        String port = "10003";
        String servant = "TransactionPersistenceService";

        try (Communicator communicator = Util.initialize(args)) {

            String proxyString =
                String.format(
                    "%s:default -h %s -p %s",
                    servant,
                    host,
                    port
                );

            System.out.println(
                "[Test] Conectando a: " + proxyString
            );

            ObjectPrx baseProxy =
                communicator.stringToProxy(proxyString);

            TransactionPersistenceServicePrx persistenceProxy =
                TransactionPersistenceServicePrx.checkedCast(baseProxy);

            if (persistenceProxy == null) {
                throw new IllegalStateException(
                    "El objeto remoto no implementa " +
                    "TransactionPersistenceService"
                );
            }

            System.out.println(
                "[Test] Proxy obtenido correctamente."
            );

            int transactionId = 9999;

            persistenceProxy.persistPgTransaction(
                transactionId,
                "TEST-ORDER-9999",
                "STRIPE",
                "250.50",
                "USD"
            );

            System.out.println(
                "[Test] Transacción persistida."
            );

            persistenceProxy.updateTransactionResult(
                transactionId,
                "CONFIRMED",
                "TEST-EXT-9999",
                ""
            );

            System.out.println(
                "[Test] Resultado actualizado."
            );

            System.out.println(
                "[Test] Prueba completada correctamente."
            );
        }
    }
}