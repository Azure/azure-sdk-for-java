// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class CosmosClientStore {
    // TODO[Public Preview]: revalidate how to get the active directory endpoint map. It suppose to come from management SDK.
    private static final Map<CosmosAzureEnvironments, String> ACTIVE_DIRECTORY_ENDPOINT_MAP;
    static {
        ACTIVE_DIRECTORY_ENDPOINT_MAP = new HashMap<>();
        ACTIVE_DIRECTORY_ENDPOINT_MAP.put(CosmosAzureEnvironments.AZURE, "https://login.microsoftonline.com/");
        ACTIVE_DIRECTORY_ENDPOINT_MAP.put(CosmosAzureEnvironments.AZURE_CHINA, "https://login.chinacloudapi.cn/");
        ACTIVE_DIRECTORY_ENDPOINT_MAP.put(CosmosAzureEnvironments.AZURE_US_GOVERNMENT, "https://login.microsoftonline.us/");
        ACTIVE_DIRECTORY_ENDPOINT_MAP.put(CosmosAzureEnvironments.AZURE_GERMANY, "https://login.microsoftonline.de/");
    }

    public static CosmosAsyncClient getCosmosClient(CosmosAccountConfig accountConfig) {
        if (accountConfig == null) {
            return null;
        }

        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(accountConfig.getEndpoint())
            .preferredRegions(accountConfig.getPreferredRegionsList())
            .throttlingRetryOptions(
                new ThrottlingRetryOptions()
                    .setMaxRetryAttemptsOnThrottledRequests(Integer.MAX_VALUE)
                    .setMaxRetryWaitTime(Duration.ofSeconds((Integer.MAX_VALUE / 1000) - 1)))
            .userAgentSuffix(getUserAgentSuffix(accountConfig));

        if (accountConfig.isUseGatewayMode()) {
            cosmosClientBuilder.gatewayMode(new GatewayConnectionConfig().setMaxConnectionPoolSize(10000));
        }

        if (accountConfig.getCosmosAuthConfig() instanceof CosmosMasterKeyAuthConfig) {
            cosmosClientBuilder.key(((CosmosMasterKeyAuthConfig) accountConfig.getCosmosAuthConfig()).getMasterKey());
        } else if (accountConfig.getCosmosAuthConfig() instanceof CosmosAadAuthConfig) {

            CosmosAadAuthConfig aadAuthConfig = (CosmosAadAuthConfig) accountConfig.getCosmosAuthConfig();
            ClientSecretCredential tokenCredential = new ClientSecretCredentialBuilder()
                .authorityHost(ACTIVE_DIRECTORY_ENDPOINT_MAP.get(aadAuthConfig.getAzureEnvironment()).replaceAll("/$", "") + "/")
                .tenantId(aadAuthConfig.getTenantId())
                .clientId(aadAuthConfig.getClientId())
                .clientSecret(aadAuthConfig.getClientSecret())
                .build();
            cosmosClientBuilder.credential(tokenCredential);
        } else {
            throw new IllegalArgumentException("Authorization type " + accountConfig.getCosmosAuthConfig().getClass() + "is not supported");
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
