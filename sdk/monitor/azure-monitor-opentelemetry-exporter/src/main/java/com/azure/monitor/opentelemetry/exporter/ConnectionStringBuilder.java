package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.util.Configuration;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;

import java.util.Objects;

class ConnectionStringBuilder {

    private static final String APPLICATIONINSIGHTS_CONNECTION_STRING =
        "APPLICATIONINSIGHTS_CONNECTION_STRING";

    private final ConnectionString connectionString;

    ConnectionStringBuilder(ConnectionString connectionString) {
        this.connectionString = connectionString;
    }

    ConnectionString build(ConfigProperties configProperties) {
        if (connectionString != null) {
            return connectionString;
        }

        // if connection string is not set, try loading from configuration
        Configuration configuration = Configuration.getGlobalConfiguration();
        ConnectionString connectionString = ConnectionString.parse(configuration.get(APPLICATIONINSIGHTS_CONNECTION_STRING));
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null");
        return connectionString;
    }
}
