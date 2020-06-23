// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosAsyncDatabaseResponse;
import com.azure.cosmos.models.CosmosContainerProperties;

public class AnalyticalStorageCodeSnippet {

    public static void main(String[] args) throws Exception {

        CosmosAsyncClient client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .buildAsyncClient();

        CosmosAsyncDatabaseResponse database = client.createDatabaseIfNotExists("testDB").block();
        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties("testContainer", "/id");
        cosmosContainerProperties.setAnalyticalStoreTimeToLiveInSeconds(-1);

        database.getDatabase().createContainer(cosmosContainerProperties).block();

        client.close();
    }
}
