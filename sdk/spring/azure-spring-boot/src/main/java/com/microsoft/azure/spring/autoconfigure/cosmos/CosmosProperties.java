// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Configuration properties for Cosmos database, consistency, telemetry, connection, query metrics and diagnostics.
 */
@Validated
@ConfigurationProperties("azure.cosmos")
public class CosmosProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosProperties.class);
    /**
     * Document DB URI.
     */
    @NotEmpty
    private String uri;

    /**
     * Document DB key.
     */
    @NotEmpty
    private String key;

    /**
     * Document DB consistency level.
     */
    private ConsistencyLevel consistencyLevel;

    /**
     * Document DB database name.
     */
    @NotEmpty
    private String database;

    /**
     * Populate Diagnostics Strings and Query metrics
     */
    private boolean populateQueryMetrics;

    /**
     * Whether allow Microsoft to collect telemetry data.
     */
    private boolean allowTelemetry = true;

    /**
     * Represents the connection mode to be used by the client in the Azure Cosmos DB database service.
     */
    private ConnectionMode connectionMode;

    /**
     * Response Diagnostics processor
     * Default implementation is to log the response diagnostics string
     */
    private ResponseDiagnosticsProcessor responseDiagnosticsProcessor =
        responseDiagnostics -> {
            if (populateQueryMetrics) {
                LOGGER.info("Response Diagnostics {}", responseDiagnostics);
            }
        };

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String databaseName) {
        this.database = databaseName;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public boolean isAllowTelemetry() {
        return allowTelemetry;
    }

    public void setAllowTelemetry(boolean allowTelemetry) {
        this.allowTelemetry = allowTelemetry;
    }

    public boolean isPopulateQueryMetrics() {
        return populateQueryMetrics;
    }

    public void setPopulateQueryMetrics(boolean populateQueryMetrics) {
        this.populateQueryMetrics = populateQueryMetrics;
    }

    public ResponseDiagnosticsProcessor getResponseDiagnosticsProcessor() {
        return responseDiagnosticsProcessor;
    }

    public void setResponseDiagnosticsProcessor(ResponseDiagnosticsProcessor responseDiagnosticsProcessor) {
        this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
    }

    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    public void setConnectionMode(ConnectionMode connectionMode) {
        this.connectionMode = connectionMode;
    }
}
