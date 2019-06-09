package com.microsoft.azure.cosmos;

import com.microsoft.azure.cosmosdb.DatabaseAccount;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import reactor.core.publisher.Mono;

public class CosmosBridgeInternal {

    public static String getLink(CosmosResource resource) {
        return resource.getLink();
    }
    
    public static DocumentCollection toDocumentCollection(CosmosContainerSettings cosmosContainerSettings) {
        return new DocumentCollection(cosmosContainerSettings.toJson());
    }

    public static AsyncDocumentClient getAsyncDocumentClient(CosmosClient client) {
        return client.getDocClientWrapper();
    }
    
    public static CosmosDatabase getCosmosDatabaseWithNewClient(CosmosDatabase cosmosDatabase, CosmosClient client) {
        return new CosmosDatabase(cosmosDatabase.getId(), client);
    }
    
    public static CosmosContainer getCosmosContainerWithNewClient(CosmosContainer cosmosContainer, CosmosDatabase cosmosDatabase, CosmosClient client) {
        return new CosmosContainer(cosmosContainer.getId(), CosmosBridgeInternal.getCosmosDatabaseWithNewClient(cosmosDatabase, client));
    }

    public static Mono<DatabaseAccount> getDatabaseAccount(CosmosClient client) {
        return client.getDatabaseAccount();
    }
}
