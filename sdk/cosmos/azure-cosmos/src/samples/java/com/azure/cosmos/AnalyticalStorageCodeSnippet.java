// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosContainerProperties;

public class AnalyticalStorageCodeSnippet {

    private static final String DATABASE_NAME = "testDB";
    private static final String CONTAINER_NAME = "testContainer";

    public static void main(String[] args) throws Exception {

        CosmosAsyncClient client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .buildAsyncClient();

        client.createDatabaseIfNotExists(DATABASE_NAME).block();
        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(CONTAINER_NAME, "/id");
        cosmosContainerProperties.setAnalyticalStoreTimeToLiveInSeconds(-1);
        client.getDatabase(DATABASE_NAME).createContainer(cosmosContainerProperties).block();

        client.close();
    }
}
