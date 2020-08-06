// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.common.PropertyLoader;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.Field;

/**
 * Factory class for CosmosDb to create client
 */
public class CosmosFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosFactory.class);

    private final CosmosAsyncClient cosmosAsyncClient;

    private final String databaseName;

    private static final String USER_AGENT_SUFFIX =
        Constants.USER_AGENT_SUFFIX + PropertyLoader.getProjectVersion();

    private static String getUserAgentSuffix() {
        return ";" + USER_AGENT_SUFFIX;
    }

    /**
     * Validate config and initialization
     *
     * @param cosmosAsyncClient cosmosAsyncClient
     * @param databaseName databaseName
     */
    public CosmosFactory(CosmosAsyncClient cosmosAsyncClient, String databaseName) {
        Assert.notNull(cosmosAsyncClient, "cosmosAsyncClient must not be null!");
        Assert.notNull(databaseName, "databaseName must not be null!");

        this.cosmosAsyncClient = cosmosAsyncClient;
        this.databaseName = databaseName;
    }

    /**
     * To create a CosmosAsyncClient
     *
     * @return CosmosAsyncClient
     */
    public CosmosAsyncClient getCosmosAsyncClient() {
        return this.cosmosAsyncClient;
    }

    /**
     * Get Cosmos Database Name
     * @return Cosmos Database Name
     */
    public String getDatabaseName() {
        return this.databaseName;
    }

    /**
     * Create Cosmos Async Client
     *
     * @param cosmosClientBuilder CosmosClientBuilder
     * @return CosmosAsyncClient
     */
    public static CosmosAsyncClient createCosmosAsyncClient(CosmosClientBuilder cosmosClientBuilder) {
        return updateCosmosClientBuilderWithUASuffix(cosmosClientBuilder).buildAsyncClient();
    }

    private static CosmosClientBuilder updateCosmosClientBuilderWithUASuffix(CosmosClientBuilder cosmosClientBuilder) {
        cosmosClientBuilder.contentResponseOnWriteEnabled(true);
        final String userAgentSuffixValue = getUserAgentSuffixValue(cosmosClientBuilder);
        String userAgentSuffix = getUserAgentSuffix();
        if (!userAgentSuffixValue.contains(userAgentSuffix)) {
            userAgentSuffix += userAgentSuffixValue;
        }

        return cosmosClientBuilder.userAgentSuffix(userAgentSuffix);
    }

    private static String getUserAgentSuffixValue(CosmosClientBuilder cosmosClientBuilder) {
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
}
