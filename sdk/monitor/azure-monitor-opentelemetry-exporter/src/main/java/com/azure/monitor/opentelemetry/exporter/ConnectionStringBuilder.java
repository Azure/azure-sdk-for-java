// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;

import java.util.Objects;

class ConnectionStringBuilder {

    private static final String APPLICATIONINSIGHTS_CONNECTION_STRING =
        "applicationinsights.connection.string";

    private ConnectionString connectionString;

    void connectionString(String connectionString) {
        this.connectionString = ConnectionString.parse(connectionString);
    }

    ConnectionString build(ConfigProperties configProperties) {
        if (connectionString == null) {
            // if connection string is not set, try loading from configuration
            connectionString = ConnectionString.parse(configProperties.getString(APPLICATIONINSIGHTS_CONNECTION_STRING));
            Objects.requireNonNull(connectionString, "'connectionString' cannot be null");
        }
        return connectionString;
    }
}
