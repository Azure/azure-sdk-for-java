// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.cosmosdb;

import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.microsoft.azure.spring.data.cosmosdb.core.ResponseDiagnosticsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Configuration properties for CosmosDB database, consistency, telemetry, connection, query metrics and diagnostics.
 */
@Validated
@ConfigurationProperties("azure.cosmosdb")
public class CosmosDBProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosDBProperties.class);
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
     * Response Diagnostics processor
     * Default implementation is to log the response diagnostics string
     */
    private ResponseDiagnosticsProcessor responseDiagnosticsProcessor =
        responseDiagnostics -> {
            if (populateQueryMetrics) {
                LOGGER.info("Response Diagnostics {}", responseDiagnostics);
            }
        };

    private ConnectionPolicy connectionPolicy = ConnectionPolicy.defaultPolicy();

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

    public ConnectionPolicy getConnectionPolicy() {
        return connectionPolicy;
    }

    public void setConnectionPolicy(ConnectionPolicy connectionPolicy) {
        this.connectionPolicy = connectionPolicy;
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
}
