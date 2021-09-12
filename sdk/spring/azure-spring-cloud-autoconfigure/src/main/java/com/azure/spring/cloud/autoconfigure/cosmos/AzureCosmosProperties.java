// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.spring.cloud.autoconfigure.properties.AzureConfigurationProperties;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Configuration properties for Cosmos database, consistency, telemetry, connection, query metrics and diagnostics.
 */
@Validated
public class AzureCosmosProperties extends AzureConfigurationProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureCosmosProperties.class);

    public static final String PREFIX = "spring.cloud.azure.cosmos";

    @NotEmpty
    @Pattern(regexp = "http[s]{0,1}://.*.documents.azure.com.*")
    private String uri;

    @NotEmpty
    private String key;

    /**
     * Override enabled, session capturing is enabled by default for {@link ConsistencyLevel#SESSION}
     */
    private boolean sessionCapturingOverrideEnabled;
    private boolean connectionSharingAcrossClientsEnabled;
    private boolean contentResponseOnWriteEnabled;
    private CosmosPermissionProperties permissions;
    private GatewayConnectionConfig gatewayConnectionConfig;
    private DirectConnectionConfig directConnectionConfig;

    /**
     * Document DB consistency level.
     */
    private ConsistencyLevel consistencyLevel;

    // TODO (xiada): only for Spring Data Cosmos
    /**
     * Document DB database name.
     */
//    @NotEmpty
    private String database;

    /**
     * Populate Diagnostics Strings and Query metrics
     */
    private boolean populateQueryMetrics;

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

    public boolean isSessionCapturingOverrideEnabled() {
        return sessionCapturingOverrideEnabled;
    }

    public boolean isConnectionSharingAcrossClientsEnabled() {
        return connectionSharingAcrossClientsEnabled;
    }

    public boolean isContentResponseOnWriteEnabled() {
        return contentResponseOnWriteEnabled;
    }

    public CosmosPermissionProperties getPermissions() {
        return permissions;
    }

    public GatewayConnectionConfig getGatewayConnectionConfig() {
        return gatewayConnectionConfig;
    }

    public DirectConnectionConfig getDirectConnectionConfig() {
        return directConnectionConfig;
    }

}
