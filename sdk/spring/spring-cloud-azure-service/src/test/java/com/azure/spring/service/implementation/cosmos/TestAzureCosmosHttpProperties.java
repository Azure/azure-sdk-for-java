// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.spring.service.implementation.core.properties.AbstractHttpProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for Cosmos database, consistency, telemetry, connection, query metrics and diagnostics.
 */
class TestAzureCosmosHttpProperties extends AbstractHttpProperties implements CosmosClientProperties {

    private String endpoint;

    private String key;

    private String database;

    private String resourceToken;

    private Boolean clientTelemetryEnabled;
    private Boolean endpointDiscoveryEnabled;
    private Boolean connectionSharingAcrossClientsEnabled;
    private Boolean contentResponseOnWriteEnabled;
    private Boolean multipleWriteRegionsEnabled;
    /**
     * Override enabled, session capturing is enabled by default for {@link ConsistencyLevel#SESSION}
     */
    private Boolean sessionCapturingOverrideEnabled;
    private Boolean readRequestsFallbackEnabled;

    private final List<CosmosPermissionProperties> permissions = new ArrayList<>();

    private final List<String> preferredRegions = new ArrayList<>();

    private final ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();

    private ConsistencyLevel consistencyLevel;
    private ConnectionMode connectionMode;
    private final GatewayConnectionConfig gatewayConnection = new GatewayConnectionConfig();
    private final DirectConnectionConfig directConnection = new DirectConnectionConfig();

    /**
     * Populate Diagnostics Strings and Query metrics
     */
    private boolean populateQueryMetrics;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getResourceToken() {
        return resourceToken;
    }

    public void setResourceToken(String resourceToken) {
        this.resourceToken = resourceToken;
    }

    public Boolean getClientTelemetryEnabled() {
        return clientTelemetryEnabled;
    }

    public void setClientTelemetryEnabled(Boolean clientTelemetryEnabled) {
        this.clientTelemetryEnabled = clientTelemetryEnabled;
    }

    public Boolean getEndpointDiscoveryEnabled() {
        return endpointDiscoveryEnabled;
    }

    public void setEndpointDiscoveryEnabled(Boolean endpointDiscoveryEnabled) {
        this.endpointDiscoveryEnabled = endpointDiscoveryEnabled;
    }

    public Boolean getConnectionSharingAcrossClientsEnabled() {
        return connectionSharingAcrossClientsEnabled;
    }

    public void setConnectionSharingAcrossClientsEnabled(Boolean connectionSharingAcrossClientsEnabled) {
        this.connectionSharingAcrossClientsEnabled = connectionSharingAcrossClientsEnabled;
    }

    public Boolean getContentResponseOnWriteEnabled() {
        return contentResponseOnWriteEnabled;
    }

    public void setContentResponseOnWriteEnabled(Boolean contentResponseOnWriteEnabled) {
        this.contentResponseOnWriteEnabled = contentResponseOnWriteEnabled;
    }

    public Boolean getMultipleWriteRegionsEnabled() {
        return multipleWriteRegionsEnabled;
    }

    public void setMultipleWriteRegionsEnabled(Boolean multipleWriteRegionsEnabled) {
        this.multipleWriteRegionsEnabled = multipleWriteRegionsEnabled;
    }

    public Boolean getSessionCapturingOverrideEnabled() {
        return sessionCapturingOverrideEnabled;
    }

    public void setSessionCapturingOverrideEnabled(Boolean sessionCapturingOverrideEnabled) {
        this.sessionCapturingOverrideEnabled = sessionCapturingOverrideEnabled;
    }

    public Boolean getReadRequestsFallbackEnabled() {
        return readRequestsFallbackEnabled;
    }

    public void setReadRequestsFallbackEnabled(Boolean readRequestsFallbackEnabled) {
        this.readRequestsFallbackEnabled = readRequestsFallbackEnabled;
    }

    public List<CosmosPermissionProperties> getPermissions() {
        return permissions;
    }

    public List<String> getPreferredRegions() {
        return preferredRegions;
    }

    public GatewayConnectionConfig getGatewayConnection() {
        return gatewayConnection;
    }

    public DirectConnectionConfig getDirectConnection() {
        return directConnection;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public boolean isPopulateQueryMetrics() {
        return populateQueryMetrics;
    }

    public void setPopulateQueryMetrics(boolean populateQueryMetrics) {
        this.populateQueryMetrics = populateQueryMetrics;
    }

    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    public void setConnectionMode(ConnectionMode connectionMode) {
        this.connectionMode = connectionMode;
    }

    public ThrottlingRetryOptions getThrottlingRetryOptions() {
        return throttlingRetryOptions;
    }
}
