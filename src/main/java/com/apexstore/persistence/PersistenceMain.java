package com.apexstore.persistence;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class PersistenceMain {

    public static void main(String[] args) {

        PersistenceConfig config = PersistenceConfig.load();

        String dbUrl = config.value("db.url", "DB_URL");
        String dbUser = config.value("db.user", "DB_USER");
        String dbPassword = config.value("db.password", "DB_PASSWORD");

        String persistencePort =
            config.value("persistence.port", "PERSISTENCE_PORT");

        String persistenceServant =
            config.value("persistence.servant", "PERSISTENCE_SERVANT");

        if (persistencePort == null || persistencePort.isBlank()) {
            persistencePort = "10002";
        }

        if (dbUrl == null || dbUrl.isBlank()) {
            throw new IllegalStateException("DB_URL no esta configurada");
        }

        if (dbUser == null || dbUser.isBlank()) {
            throw new IllegalStateException("DB_USER no esta configurada");
        }

        if (dbPassword == null) {
            throw new IllegalStateException("DB_PASSWORD no esta configurada");
        }

        try (Communicator communicator = Util.initialize(args)) {

            String endpoints =
                String.format("default -p %s", persistencePort);

            ObjectAdapter adapter =
                communicator.createObjectAdapterWithEndpoints(
                    "PersistenceAdapter",
                    endpoints
                );

            TransactionRepository repository =
                new TransactionRepository(
                    dbUrl,
                    dbUser,
                    dbPassword
                );

            TransactionPersistenceServiceI servant =
                new TransactionPersistenceServiceI(repository);

            adapter.add(
                servant,
                Util.stringToIdentity(persistenceServant)
            );

            adapter.activate();

            System.out.println(
                "=== TransactionPersistenceService activo ==="
            );

            System.out.println(
                "Escuchando en el puerto: " + persistencePort
            );

            communicator.waitForShutdown();
        }
    }
}