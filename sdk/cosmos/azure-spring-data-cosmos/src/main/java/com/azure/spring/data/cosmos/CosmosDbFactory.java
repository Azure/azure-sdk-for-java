// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos;

import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.sync.CosmosSyncClient;
import com.azure.spring.data.cosmos.common.MacAddress;
import com.azure.spring.data.cosmos.common.PropertyLoader;
import com.azure.spring.data.cosmos.common.TelemetrySender;
import com.azure.spring.data.cosmos.config.CosmosDBConfig;
import org.springframework.lang.NonNull;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * Factory class for cosmosdb to create client
 */
public class CosmosDbFactory {

    private final CosmosDBConfig config;

    private static final boolean IS_TELEMETRY_ALLOWED = PropertyLoader.isApplicationTelemetryAllowed();

    private static final String USER_AGENT_SUFFIX = Constants.USER_AGENT_SUFFIX + PropertyLoader.getProjectVersion();

    private String getUserAgentSuffix() {
        String suffix = ";" + USER_AGENT_SUFFIX;

        if (IS_TELEMETRY_ALLOWED || config.isAllowTelemetry()) {
            suffix += ";" + MacAddress.getHashMac();
        }

        return suffix;
    }

    /**
     * Validate config and initialization
     *
     * @param config cosmosdb config
     */
    public CosmosDbFactory(@NonNull CosmosDBConfig config) {
        validateConfig(config);

        this.config = config;
    }

    /**
     * To create a CosmosClient
     *
     * @return CosmosClient
     */
    public CosmosClient getCosmosClient() {
        final ConnectionPolicy policy = config.getConnectionPolicy();
        final String userAgent = getUserAgentSuffix() + ";" + policy.userAgentSuffix();

        policy.userAgentSuffix(userAgent);
        return CosmosClient.builder()
                           .endpoint(config.getUri())
                           .key(config.getKey())
                           .cosmosKeyCredential(config.getCosmosKeyCredential())
                           .connectionPolicy(policy)
                           .consistencyLevel(config.getConsistencyLevel())
                           .build();
    }

    /**
     * To create a CosmosSyncClient
     *
     * @return CosmosSyncClient
     */
    public CosmosSyncClient getCosmosSyncClient() {
        final ConnectionPolicy policy = config.getConnectionPolicy();
        final String userAgent = getUserAgentSuffix() + ";" + policy.userAgentSuffix();

        policy.userAgentSuffix(userAgent);
        return CosmosClient.builder()
                       .endpoint(config.getUri())
                       .key(config.getKey())
                       .cosmosKeyCredential(config.getCosmosKeyCredential())
                       .connectionPolicy(policy)
                       .consistencyLevel(config.getConsistencyLevel())
                       .buildSyncClient();
    }

    private void validateConfig(@NonNull CosmosDBConfig config) {
        Assert.hasText(config.getUri(), "cosmosdb host url should have text!");
        if (config.getCosmosKeyCredential() == null) {
            Assert.hasText(config.getKey(), "cosmosdb host key should have text!");
        } else if (StringUtils.isEmpty(config.getKey())) {
            Assert.hasText(config.getCosmosKeyCredential().key(),
                "cosmosdb credential host key should have text!");
        }
        Assert.hasText(config.getDatabase(), "cosmosdb database should have text!");
        Assert.notNull(config.getConnectionPolicy(), "cosmosdb connection policy should not be null!");
    }

    @PostConstruct
    private void sendTelemetry() {
        //  If any one of them is enabled, send telemetry data
        if (IS_TELEMETRY_ALLOWED || config.isAllowTelemetry()) {
            final TelemetrySender sender = new TelemetrySender();

            sender.send(this.getClass().getSimpleName());
        }
    }

    /**
     * To get config object of cosmosdb
     *
     * @return CosmosDBConfig
     */
    public CosmosDBConfig getConfig() {
        return config;
    }
}
