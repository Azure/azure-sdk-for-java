// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReadFeedUdfsTest extends TestSuiteBase {

    private CosmosAsyncContainer createdCollection;
    private List<CosmosUserDefinedFunctionProperties> createdUserDefinedFunctions = new ArrayList<>();

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ReadFeedUdfsTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "query" }, timeOut = FEED_TIMEOUT)
    public void readUserDefinedFunctions() throws Exception {
        int maxItemCount = 2;

        CosmosPagedFlux<CosmosUserDefinedFunctionProperties> feedObservable = createdCollection.getScripts()
                                                                                               .readAllUserDefinedFunctions();

        int expectedPageSize = (createdUserDefinedFunctions.size() + maxItemCount - 1)
                / maxItemCount;

        FeedResponseListValidator<CosmosUserDefinedFunctionProperties> validator = new FeedResponseListValidator.Builder<CosmosUserDefinedFunctionProperties>()
                .totalSize(createdUserDefinedFunctions.size())
                .exactlyContainsIdsInAnyOrder(
                        createdUserDefinedFunctions.stream().map(CosmosUserDefinedFunctionProperties::getId).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosUserDefinedFunctionProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable.byPage(maxItemCount), validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "query" }, timeOut = SETUP_TIMEOUT)
    public void before_ReadFeedUdfsTest() {
        client = getClientBuilder().buildAsyncClient();
        CosmosAsyncDatabase database = getSharedCosmosDatabase(client);
        createdCollection = createCollection(
            database,
            getCollectionDefinitionWithRangeRangeIndex(),
            new CosmosContainerRequestOptions(),
            400);

        for (int i = 0; i < 5; i++) {
            createdUserDefinedFunctions.add(createUserDefinedFunctions(createdCollection));
        }

        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    @AfterClass(groups = { "query" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteCollection(createdCollection);
        safeClose(client);
    }

    public CosmosUserDefinedFunctionProperties createUserDefinedFunctions(CosmosAsyncContainer cosmosContainer) {
        CosmosUserDefinedFunctionProperties udf = new CosmosUserDefinedFunctionProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );

        return cosmosContainer.getScripts().createUserDefinedFunction(udf).block()
                .getProperties();
    }

}
