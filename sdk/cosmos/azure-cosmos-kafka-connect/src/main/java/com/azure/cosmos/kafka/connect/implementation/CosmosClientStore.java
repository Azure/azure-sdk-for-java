// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.time.Duration;

public class CosmosClientStore {
    public static CosmosAsyncClient getCosmosClient(CosmosAccountConfig accountConfig) {
        if (accountConfig == null) {
            return null;
        }

        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(accountConfig.getEndpoint())
            .key(accountConfig.getAccountKey())
            .preferredRegions(accountConfig.getPreferredRegionsList())
            .throttlingRetryOptions(
                new ThrottlingRetryOptions()
                    .setMaxRetryAttemptsOnThrottledRequests(Integer.MAX_VALUE)
                    .setMaxRetryWaitTime(Duration.ofSeconds((Integer.MAX_VALUE / 1000) - 1)))
            .userAgentSuffix(getUserAgentSuffix(accountConfig));

        if (accountConfig.isUseGatewayMode()) {
            cosmosClientBuilder.gatewayMode(new GatewayConnectionConfig().setMaxConnectionPoolSize(10000));
        }

        return cosmosClientBuilder.buildAsyncClient();
    }

    private static String getUserAgentSuffix(CosmosAccountConfig accountConfig) {
        if (StringUtils.isNotEmpty(accountConfig.getApplicationName())) {
            return KafkaCosmosConstants.USER_AGENT_SUFFIX + "|" + accountConfig.getApplicationName();
        }

        return KafkaCosmosConstants.USER_AGENT_SUFFIX;
    }
}
