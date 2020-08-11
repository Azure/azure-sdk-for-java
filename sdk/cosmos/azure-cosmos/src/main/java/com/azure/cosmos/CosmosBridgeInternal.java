// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.Warning;
import com.azure.cosmos.implementation.query.Transformer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.function.Function;

import static com.azure.cosmos.implementation.Warning.INTERNAL_USE_ONLY_WARNING;

/**
 * DO NOT USE. For internal use only by the SDK. These methods might break at any time. No support will be provided.
 */
@Warning(value = INTERNAL_USE_ONLY_WARNING)
public final class CosmosBridgeInternal {

    private CosmosBridgeInternal() {}

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static AsyncDocumentClient getAsyncDocumentClient(CosmosClient client) {
        return client.asyncClient().getDocClientWrapper();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static AsyncDocumentClient getAsyncDocumentClient(CosmosAsyncClient client) {
        return client.getDocClientWrapper();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static AsyncDocumentClient getAsyncDocumentClient(CosmosAsyncDatabase cosmosAsyncDatabase) {
        return cosmosAsyncDatabase.getDocClientWrapper();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosAsyncDatabase getCosmosDatabaseWithNewClient(CosmosAsyncDatabase cosmosDatabase,
                                                                     CosmosAsyncClient client) {
        return new CosmosAsyncDatabase(cosmosDatabase.getId(), client);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosAsyncContainer getCosmosContainerWithNewClient(CosmosAsyncContainer cosmosContainer,
                                                                       CosmosAsyncDatabase cosmosDatabase,
                                                                       CosmosAsyncClient client) {
        return new CosmosAsyncContainer(cosmosContainer.getId(),
            CosmosBridgeInternal.getCosmosDatabaseWithNewClient(cosmosDatabase, client));
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static AsyncDocumentClient getContextClient(CosmosAsyncDatabase database) {
        return database.getClient().getContextClient();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static AsyncDocumentClient getContextClient(CosmosAsyncContainer container) {
        return container.getDatabase().getClient().getContextClient();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosAsyncContainer getCosmosAsyncContainer(CosmosContainer container) {
        return container.asyncContainer;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static ConsistencyLevel getConsistencyLevel(CosmosClientBuilder cosmosClientBuilder) {
        return cosmosClientBuilder.getConsistencyLevel();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static ConnectionPolicy getConnectionPolicy(CosmosClientBuilder cosmosClientBuilder) {
        return cosmosClientBuilder.getConnectionPolicy();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosClientBuilder cloneCosmosClientBuilder(CosmosClientBuilder builder) {
        CosmosClientBuilder copy = new CosmosClientBuilder();

        copy.endpoint(builder.getEndpoint())
            .key(builder.getKey())
            .directMode(builder.getDirectConnectionConfig())
            .gatewayMode(builder.getGatewayConnectionConfig())
            .consistencyLevel(builder.getConsistencyLevel())
            .credential(builder.getCredential())
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

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> CosmosPagedFlux<T> queryItemsInternal(CosmosAsyncContainer container,
                                                            SqlQuerySpec sqlQuerySpec,
                                                            CosmosQueryRequestOptions cosmosQueryRequestOptions,
                                                            Transformer<T> transformer) {
        return UtilBridgeInternal.createCosmosPagedFlux(transformer.transform(container.queryItemsInternalFunc(
            sqlQuerySpec,
            cosmosQueryRequestOptions,
            JsonNode.class)));
    }
}
