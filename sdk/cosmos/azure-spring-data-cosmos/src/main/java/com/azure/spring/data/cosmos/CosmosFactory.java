// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.common.MacAddress;
import com.azure.spring.data.cosmos.common.PropertyLoader;
import com.azure.spring.data.cosmos.common.TelemetrySender;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

/**
 * Factory class for CosmosDb to create client
 */
public class CosmosFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosFactory.class);

    private final CosmosConfig config;

    private static final boolean IS_TELEMETRY_ALLOWED =
        PropertyLoader.isApplicationTelemetryAllowed();

    private static final String USER_AGENT_SUFFIX =
        Constants.USER_AGENT_SUFFIX + PropertyLoader.getProjectVersion();

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
     * @param cosmosConfig cosmosConfig
     */
    public CosmosFactory(@NonNull CosmosConfig cosmosConfig) {
        validateConfig(cosmosConfig);

        this.config = cosmosConfig;
    }

    /**
     * To create a CosmosAsyncClient
     *
     * @return CosmosClient
     */
    public CosmosAsyncClient getCosmosAsyncClient() {
        final CosmosClientBuilder cosmosClientBuilderFromConfig =
            getCosmosClientBuilderFromConfig(config);
        return cosmosClientBuilderFromConfig.buildAsyncClient();
    }

    /**
     * To create a CosmosClient
     *
     * @return CosmosSyncClient
     */
    public CosmosClient getCosmosSyncClient() {
        final CosmosClientBuilder cosmosClientBuilderFromConfig =
            getCosmosClientBuilderFromConfig(config);
        return cosmosClientBuilderFromConfig.buildClient();
    }

    private CosmosClientBuilder getCosmosClientBuilderFromConfig(CosmosConfig cosmosConfig) {
        final CosmosClientBuilder cosmosClientBuilder = cosmosConfig.getCosmosClientBuilder();
        cosmosClientBuilder.contentResponseOnWriteEnabled(true);
        final String userAgentSuffixValue = getUserAgentSuffixValue(cosmosClientBuilder);
        String userAgentSuffix = getUserAgentSuffix();
        if (!userAgentSuffixValue.contains(userAgentSuffix)) {
            userAgentSuffix += userAgentSuffixValue;
        }

        return cosmosConfig.getCosmosClientBuilder().userAgentSuffix(userAgentSuffix);
    }

    private String getUserAgentSuffixValue(CosmosClientBuilder cosmosClientBuilder) {
        final Field userAgentSuffix = FieldUtils.getDeclaredField(CosmosClientBuilder.class,
            "userAgentSuffix", true);
        try {
            return (String) userAgentSuffix.get(cosmosClientBuilder);
        } catch (IllegalAccessException e) {
            LOGGER.error("Error occurred while getting userAgentSuffix from CosmosClientBuilder",
                e);
        }
        return "";
    }

    private void validateConfig(@NonNull CosmosConfig config) {
        Assert.hasText(config.getDatabase(), "cosmos database should have text!");
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
     * To get config object of Cosmos
     *
     * @return CosmosConfig
     */
    public CosmosConfig getConfig() {
        return config;
    }
}
