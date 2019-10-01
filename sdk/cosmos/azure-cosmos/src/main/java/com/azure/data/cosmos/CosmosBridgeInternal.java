// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.internal.DocumentCollection;
import reactor.core.publisher.Mono;

/**
 * DO NOT USE. For internal use only by the SDK. These methods might break at any time. No support will be provided.
 */
public class CosmosBridgeInternal {

    public static DocumentCollection toDocumentCollection(CosmosContainerProperties cosmosContainerProperties) {
        return new DocumentCollection(cosmosContainerProperties.toJson());
    }

    public static AsyncDocumentClient getAsyncDocumentClient(CosmosAsyncClient client) {
        return client.getDocClientWrapper();
    }

    public static CosmosAsyncDatabase getCosmosDatabaseWithNewClient(CosmosAsyncDatabase cosmosDatabase, CosmosAsyncClient client) {
        return new CosmosAsyncDatabase(cosmosDatabase.getId(), client);
    }

    public static CosmosAsyncContainer getCosmosContainerWithNewClient(CosmosAsyncContainer cosmosContainer, CosmosAsyncDatabase cosmosDatabase, CosmosAsyncClient client) {
        return new CosmosAsyncContainer(cosmosContainer.getId(), CosmosBridgeInternal.getCosmosDatabaseWithNewClient(cosmosDatabase, client));
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
}
