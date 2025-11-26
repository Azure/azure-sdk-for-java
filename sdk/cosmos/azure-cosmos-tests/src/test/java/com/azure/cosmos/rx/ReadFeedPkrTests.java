// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;


public class ReadFeedPkrTests extends TestSuiteBase {

    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdCollection;

    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ReadFeedPkrTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "query" }, timeOut = FEED_TIMEOUT)
    public void readPartitionKeyRanges() throws Exception {

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options, 2);

        Flux<FeedResponse<PartitionKeyRange>> feedObservable = client.readPartitionKeyRanges(getCollectionLink(), options);

        FeedResponseListValidator<PartitionKeyRange> validator = new FeedResponseListValidator.Builder<PartitionKeyRange>()
                .totalSize(1)
                .numberOfPages(2) // when using changeFeed to get the pkRanges, first page is empty with continuationToken
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "query" }, timeOut = SETUP_TIMEOUT)
    public void before_ReadFeedPkrTests() {
        CosmosAsyncClient cosmosAsyncClient = getClientBuilder().buildAsyncClient();
        client = CosmosBridgeInternal.getAsyncDocumentClient(cosmosAsyncClient);
        createdDatabase = getSharedCosmosDatabase(cosmosAsyncClient);
        createdCollection = createCollection(createdDatabase,
                                             getCollectionDefinition(),
                                             new CosmosContainerRequestOptions());
    }

    @AfterClass(groups = { "query" }, timeOut = SETUP_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteCollection(createdCollection);
        client.close();
    }

    private String getCollectionLink() {
        return "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId();
    }
}
