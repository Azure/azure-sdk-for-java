// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.QueryFeedOperationState;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

class DocDBUtils {

    private DocDBUtils() {
    }

    private static final ConcurrentHashMap<AsyncDocumentClient, QueryFeedOperationState> dummyQueryStateForDBQuery = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<AsyncDocumentClient, QueryFeedOperationState> dummyQueryStateForColQuery = new ConcurrentHashMap<>();


    public static QueryFeedOperationState createDummyQueryFeedOperationState(
        ResourceType resourceType,
        OperationType operationType,
        CosmosQueryRequestOptions options,
        AsyncDocumentClient client) {
        CosmosAsyncClient cosmosClient = new CosmosClientBuilder()
            .key(client.getMasterKeyOrResourceToken())
            .endpoint(client.getServiceEndpoint().toString())
            .buildAsyncClient();
        return new QueryFeedOperationState(
            cosmosClient,
            "SomeSpanName",
            "SomeDBName",
            "SomeContainerName",
            resourceType,
            operationType,
            null,
            options,
            new CosmosPagedFluxOptions()
        );
    }

    static Database getDatabase(AsyncDocumentClient client, String databaseId) {
        QueryFeedOperationState state = dummyQueryStateForDBQuery.computeIfAbsent(
            client,
            (c) -> {
                return createDummyQueryFeedOperationState(
                    ResourceType.Database,
                    OperationType.Query,
                    new CosmosQueryRequestOptions(),
                    c
                );
            }
        );

        FeedResponse<Database> feedResponsePages = client
                .queryDatabases(new SqlQuerySpec("SELECT * FROM root r WHERE r.id=@id",
                    Collections.singletonList(new SqlParameter("@id", databaseId))), state)
                .single().block();

        if (feedResponsePages.getResults().isEmpty()) {
            throw new RuntimeException("cannot find datatbase " + databaseId);
        }
        return feedResponsePages.getResults().get(0);
    }

    static DocumentCollection getCollection(AsyncDocumentClient client, String databaseLink,
            String collectionId) {
        QueryFeedOperationState state = dummyQueryStateForColQuery.computeIfAbsent(
            client,
            (c) -> {
                return createDummyQueryFeedOperationState(
                    ResourceType.DocumentCollection,
                    OperationType.Query,
                    new CosmosQueryRequestOptions(),
                    c
                );
            }
        );

        FeedResponse<DocumentCollection> feedResponsePages = client
                .queryCollections(databaseLink,
                        new SqlQuerySpec("SELECT * FROM root r WHERE r.id=@id",
                                Collections.singletonList(new SqlParameter("@id", collectionId))),
                        state)
                .single().block();

        if (feedResponsePages.getResults().isEmpty()) {
            throw new RuntimeException("cannot find collection " + collectionId);
        }
        return feedResponsePages.getResults().get(0);
    }
}
