// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.performance.utils;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;

import java.util.Collections;

import static com.azure.spring.data.cosmos.common.TestConstants.ORDER_BY_STRING_PATH;

public class DatabaseUtils {
    public static void createDatabase(CosmosClient cosmosClient, String databaseName)
            throws CosmosException {
        try {
            // Can use sync api once ready
            cosmosClient.getDatabase(databaseName).delete();
        } catch (Exception e) {
            // Ignore delete failure
        }

        cosmosClient.createDatabase(databaseName);
    }

    public static void deleteContainer(CosmosClient cosmosClient, String databaseName, String containerName)
            throws CosmosException {
        cosmosClient.getDatabase(databaseName).getContainer(containerName).delete();
    }

    public static void createContainer(CosmosClient cosmosClient, String databaseName, String containerName)
            throws CosmosException {
        final CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerName,
                new PartitionKeyDefinition().setPaths(Collections.singletonList("/mypk")));

        final IndexingPolicy policy = new IndexingPolicy();
        policy.setIncludedPaths(Collections.singletonList(new IncludedPath(ORDER_BY_STRING_PATH)));
        containerProperties.setIndexingPolicy(policy);

        cosmosClient.getDatabase(databaseName).createContainer(containerProperties);
    }

    public static String getDocumentLink(String databaseName, String containerName, Object documentId) {
        return getContainerLink(databaseName, containerName) + "/docs/" + documentId;
    }

    public static String getDatabaseLink(String databaseName) {
        return "dbs/" + databaseName;
    }

    public static String getContainerLink(String databaseName, String containerName) {
        return getDatabaseLink(databaseName) + "/colls/" + containerName;
    }
}
