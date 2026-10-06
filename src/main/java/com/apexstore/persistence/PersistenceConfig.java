package com.apexstore.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class PersistenceConfig {

    private final Properties properties = new Properties();

    private PersistenceConfig() {
        try (InputStream input = PersistenceConfig.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                    "No se encontró config.properties en el classpath"
                );
            }

            properties.load(input);

        } catch (IOException e) {
            throw new IllegalStateException(
                "No se pudo leer config.properties",
                e
            );
        }
    }

    static PersistenceConfig load() {
        return new PersistenceConfig();
    }

    String value(String property, String environmentVariable) {

        String environmentValue =
            System.getenv(environmentVariable);

        if (environmentValue != null &&
            !environmentValue.isBlank()) {

            return environmentValue;
        }

        String propertyValue =
            properties.getProperty(property);

        if (propertyValue == null ||
            propertyValue.isBlank()) {

            throw new IllegalStateException(
                "Falta la configuración '" + property + "' " +
                "(o la variable de entorno " +
                environmentVariable + ")"
            );
        }

        return propertyValue.trim();
    }
}