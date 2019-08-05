// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.CosmosBridgeInternal;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerRequestOptions;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;


public class ReadFeedPkrTests extends TestSuiteBase {

    private CosmosDatabase createdDatabase;
    private CosmosContainer createdCollection;

    private AsyncDocumentClient client;
    
    @Factory(dataProvider = "clientBuildersWithDirect")
    public ReadFeedPkrTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "emulator" }, timeOut = FEED_TIMEOUT)
    public void readPartitionKeyRanges() throws Exception {

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);

        Flux<FeedResponse<PartitionKeyRange>> feedObservable = client.readPartitionKeyRanges(getCollectionLink(), options);

        FeedResponseListValidator<PartitionKeyRange> validator = new FeedResponseListValidator.Builder<PartitionKeyRange>()
                .totalSize(1)
                .numberOfPages(1)
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = CosmosBridgeInternal.getAsyncDocumentClient(clientBuilder().build());
        createdDatabase = getSharedCosmosDatabase(clientBuilder().build());
        createdCollection = createCollection(createdDatabase,
                                             getCollectionDefinition(),
                                             new CosmosContainerRequestOptions());
    }

    @AfterClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteCollection(createdCollection);
        client.close();
    }

    private String getCollectionLink() {
        return "dbs/" + createdDatabase.id() + "/colls/" + createdCollection.id();
    }
}
