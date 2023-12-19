// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

public class CosmosReadAllItemsTests extends TestSuiteBase {
    private final static int TIMEOUT = 30000;
    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public CosmosReadAllItemsTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "query" }, timeOut = 2 * TIMEOUT)
    public void readMany_UsePageSizeInPagedFluxOption() {
        // first creating few items
        String pkValue = UUID.randomUUID().toString();
        int itemCount = 10;
        for (int i = 0; i < itemCount; i++) {
            TestObject testObject = TestObject.create(pkValue);
            this.container.createItem(testObject).block();
        }

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        FeedResponseListValidator<TestObject> validator1 =
            new FeedResponseListValidator
                .Builder<TestObject>()
                .totalSize(itemCount)
                .numberOfPages(2)
                .build();
        CosmosPagedFlux<TestObject> queryObservable1 =
            this.container.readAllItems(new PartitionKey(pkValue), cosmosQueryRequestOptions, TestObject.class);

        validateQuerySuccess(queryObservable1.byPage(5), validator1, TIMEOUT);

        FeedResponseListValidator<TestObject> validator2 =
            new FeedResponseListValidator
                .Builder<TestObject>()
                .totalSize(itemCount)
                .numberOfPages(1)
                .build();
        validateQuerySuccess(queryObservable1.byPage(), validator2, TIMEOUT);
    }

    @AfterClass(groups = { "query" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @BeforeClass(groups = { "query" }, timeOut = SETUP_TIMEOUT)
    public void before_TopQueryTests() {
        this.client = getClientBuilder().buildAsyncClient();
        this.container = getSharedSinglePartitionCosmosContainer(client);
    }
}
