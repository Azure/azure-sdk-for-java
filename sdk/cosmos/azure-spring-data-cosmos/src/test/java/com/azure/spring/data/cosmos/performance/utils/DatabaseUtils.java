// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.performance.utils;

import com.azure.data.cosmos.*;
import com.azure.data.cosmos.internal.RequestOptions;
import com.azure.data.cosmos.sync.CosmosSyncClient;

import java.util.Collections;

import static com.azure.spring.data.cosmos.common.TestConstants.ORDER_BY_STRING_PATH;

public class DatabaseUtils {
    public static void createDatabase(CosmosSyncClient documentClient, String databaseName)
            throws CosmosClientException {
        try {
            // Can use sync api once ready
            documentClient.getDatabase(databaseName).delete();
        } catch (Exception e) {
            // Ignore delete failure
        }

        documentClient.createDatabase(databaseName);
    }

    public static void deleteContainer(CosmosSyncClient documentClient, String databaseName, String containerName)
            throws CosmosClientException {
        final RequestOptions requestOptions = new RequestOptions();
        requestOptions.setOfferThroughput(1000);

        documentClient.getDatabase(databaseName).getContainer(containerName).delete();
    }

    public static void createContainer(CosmosSyncClient documentClient, String databaseName, String containerName)
            throws CosmosClientException {
        final CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerName,
                new PartitionKeyDefinition().paths(Collections.singletonList("/mypk")));

        final IndexingPolicy policy = new IndexingPolicy();
        policy.setIncludedPaths(Collections.singletonList(new IncludedPath(ORDER_BY_STRING_PATH)));
        containerProperties.indexingPolicy(policy);

        documentClient.getDatabase(databaseName).createContainer(containerProperties);
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
