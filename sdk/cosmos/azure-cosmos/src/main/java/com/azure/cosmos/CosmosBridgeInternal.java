// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ConnectionPolicy;

/**
 * DO NOT USE. For internal use only by the SDK. These methods might break at any time. No support will be provided.
 */
public final class CosmosBridgeInternal {

    public static AsyncDocumentClient getAsyncDocumentClient(CosmosClient client) {
        return client.asyncClient().getDocClientWrapper();
    }

    public static AsyncDocumentClient getAsyncDocumentClient(CosmosAsyncClient client) {
        return client.getDocClientWrapper();
    }

    public static AsyncDocumentClient getAsyncDocumentClient(CosmosAsyncDatabase cosmosAsyncDatabase) {
        return cosmosAsyncDatabase.getDocClientWrapper();
    }

    public static CosmosAsyncDatabase getCosmosDatabaseWithNewClient(CosmosAsyncDatabase cosmosDatabase,
                                                                     CosmosAsyncClient client) {
        return new CosmosAsyncDatabase(cosmosDatabase.getId(), client);
    }

    public static CosmosAsyncContainer getCosmosContainerWithNewClient(CosmosAsyncContainer cosmosContainer,
                                                                       CosmosAsyncDatabase cosmosDatabase,
                                                                       CosmosAsyncClient client) {
        return new CosmosAsyncContainer(cosmosContainer.getId(),
            CosmosBridgeInternal.getCosmosDatabaseWithNewClient(cosmosDatabase, client));
    }

    public static AsyncDocumentClient getContextClient(CosmosAsyncDatabase database) {
        return database.getClient().getContextClient();
    }

    public static AsyncDocumentClient getContextClient(CosmosAsyncContainer container) {
        return container.getDatabase().getClient().getContextClient();
    }

    public static CosmosAsyncContainer getCosmosAsyncContainer(CosmosContainer container) {
        return container.asyncContainer;
    }

    public static ConsistencyLevel getConsistencyLevel(CosmosClientBuilder cosmosClientBuilder) {
        return cosmosClientBuilder.getConsistencyLevel();
    }

    public static ConnectionPolicy getConnectionPolicy(CosmosClientBuilder cosmosClientBuilder) {
        return cosmosClientBuilder.getConnectionPolicy();
    }

    public static CosmosClientBuilder cloneCosmosClientBuilder(CosmosClientBuilder builder) {
        CosmosClientBuilder copy = new CosmosClientBuilder();

        copy.endpoint(builder.getEndpoint())
            .key(builder.getKey())
            .directMode(builder.getDirectConnectionConfig())
            .gatewayMode(builder.getGatewayConnectionConfig())
            .consistencyLevel(builder.getConsistencyLevel())
            .keyCredential(builder.getKeyCredential())
            .permissions(builder.getPermissions())
            .authorizationTokenResolver(builder.getAuthorizationTokenResolver())
            .resourceToken(builder.getResourceToken())
            .contentResponseOnWriteEnabled(builder.isContentResponseOnWriteEnabled())
            .userAgentSuffix(builder.getUserAgentSuffix())
            .throttlingRetryOptions(builder.getThrottlingRetryOptions())
            .preferredRegions(builder.getPreferredRegions())
            .endpointDiscoveryEnabled(builder.isEndpointDiscoveryEnabled())
            .multipleWriteRegionsEnabled(builder.isMultipleWriteRegionsEnabled())
            .readRequestsFallbackEnabled(builder.isReadRequestsFallbackEnabled());

        return copy;
    }
}
