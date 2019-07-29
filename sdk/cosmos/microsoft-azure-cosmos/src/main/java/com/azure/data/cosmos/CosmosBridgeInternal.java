// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.internal.DatabaseAccount;
import com.azure.data.cosmos.internal.DocumentCollection;
import reactor.core.publisher.Mono;

/**
 * DO NOT USE. For internal use only by the SDK. These methods might break at any time. No support will be provided.
 */
public class CosmosBridgeInternal {
    
    public static DocumentCollection toDocumentCollection(CosmosContainerProperties cosmosContainerProperties) {
        return new DocumentCollection(cosmosContainerProperties.toJson());
    }

    public static AsyncDocumentClient getAsyncDocumentClient(CosmosClient client) {
        return client.getDocClientWrapper();
    }
    
    public static CosmosDatabase getCosmosDatabaseWithNewClient(CosmosDatabase cosmosDatabase, CosmosClient client) {
        return new CosmosDatabase(cosmosDatabase.id(), client);
    }
    
    public static CosmosContainer getCosmosContainerWithNewClient(CosmosContainer cosmosContainer, CosmosDatabase cosmosDatabase, CosmosClient client) {
        return new CosmosContainer(cosmosContainer.id(), CosmosBridgeInternal.getCosmosDatabaseWithNewClient(cosmosDatabase, client));
    }

    public static Mono<DatabaseAccount> getDatabaseAccount(CosmosClient client) {
        return client.getDatabaseAccount();
    }

    public static AsyncDocumentClient getContextClient(CosmosDatabase database) {
        return database.getClient().getContextClient();
    }

    public static AsyncDocumentClient getContextClient(CosmosContainer container) {
        return container.getDatabase().getClient().getContextClient();
    }
}
