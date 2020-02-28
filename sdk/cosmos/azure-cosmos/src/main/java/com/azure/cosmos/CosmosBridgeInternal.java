// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DocumentCollection;
import reactor.core.publisher.Mono;

/**
 * DO NOT USE. For internal use only by the SDK. These methods might break at any time. No support will be provided.
 */
public class CosmosBridgeInternal {

    public static DocumentCollection toDocumentCollection(CosmosContainerProperties cosmosContainerProperties) {
        return new DocumentCollection(cosmosContainerProperties.toJson());
    }

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

    public static Mono<DatabaseAccount> getDatabaseAccount(CosmosAsyncClient client) {
        return client.readDatabaseAccount();
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
}
