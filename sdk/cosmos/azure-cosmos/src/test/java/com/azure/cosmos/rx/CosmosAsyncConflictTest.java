// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosConflictProperties;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.HttpConstants;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosAsyncConflictTest extends TestSuiteBase {

    private CosmosAsyncContainer createdCollection;

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public CosmosAsyncConflictTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readConflicts_toBlocking_toIterator() {

        int requestPageSize = 3;
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosConflictProperties> conflictReadFeedFlux = createdCollection.readAllConflicts(options);

        Iterator<FeedResponse<CosmosConflictProperties>> it = conflictReadFeedFlux.byPage(requestPageSize).toIterable().iterator();

        int expectedNumberOfConflicts = 0;

        int numberOfResults = 0;
        while (it.hasNext()) {
            FeedResponse<CosmosConflictProperties> page = it.next();
            String pageSizeAsString = page.getResponseHeaders().get(HttpConstants.HttpHeaders.ITEM_COUNT);
            assertThat(pageSizeAsString).isNotNull();
            // assertThat("header item count must be present", pageSizeAsString, notNullValue());
            int pageSize = Integer.valueOf(pageSizeAsString);
            // Assert that Result size must match header getItem count
            assertThat(page.getResults().size()).isEqualTo(pageSize);
            numberOfResults += pageSize;
        }
        assertThat(numberOfResults).isEqualTo(expectedNumberOfConflicts);
    }


    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_CosmosAsyncConflictTest() {
        client = getClientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @BeforeMethod(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeMethod() {
        safeClose(client);
        client = getClientBuilder().buildAsyncClient();
    }
}
