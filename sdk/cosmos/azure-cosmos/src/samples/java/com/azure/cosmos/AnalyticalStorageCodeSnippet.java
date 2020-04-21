package com.azure.cosmos;

import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosAsyncDatabaseResponse;
import com.azure.cosmos.models.CosmosContainerProperties;

import java.time.Instant;

public class AnalyticalStorageCodeSnippet {

    public static void main(String[] args) throws Exception {

        CosmosAsyncClient client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .buildAsyncClient();

        CosmosAsyncDatabaseResponse database = client.createDatabaseIfNotExists("testDB").block();
        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(Instant.now().toString(), "/id");
        cosmosContainerProperties.setAnalyticalStorageTimeToLiveInSeconds(-1);

        database.getDatabase().createContainer(cosmosContainerProperties).block();

        client.close();
    }

}
