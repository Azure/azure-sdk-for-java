// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosUserDefinedFunctionProperties;
import com.azure.cosmos.FeedOptions;
import com.azure.cosmos.FeedResponse;
import com.azure.cosmos.internal.Database;
import com.azure.cosmos.internal.FeedResponseListValidator;
import com.azure.cosmos.internal.FeedResponseValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

//FIXME beforeClass times out.
@Ignore
public class ReadFeedUdfsTest extends TestSuiteBase {

    private Database createdDatabase;
    private CosmosAsyncContainer createdCollection;
    private List<CosmosUserDefinedFunctionProperties> createdUserDefinedFunctions = new ArrayList<>();

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ReadFeedUdfsTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readUserDefinedFunctions() throws Exception {

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);

        Flux<FeedResponse<CosmosUserDefinedFunctionProperties>> feedObservable = createdCollection.getScripts()
                .readAllUserDefinedFunctions(options);

        int expectedPageSize = (createdUserDefinedFunctions.size() + options.maxItemCount() - 1)
                / options.maxItemCount();

        FeedResponseListValidator<CosmosUserDefinedFunctionProperties> validator = new FeedResponseListValidator.Builder<CosmosUserDefinedFunctionProperties>()
                .totalSize(createdUserDefinedFunctions.size())
                .exactlyContainsInAnyOrder(
                        createdUserDefinedFunctions.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosUserDefinedFunctionProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        for (int i = 0; i < 5; i++) {
            createdUserDefinedFunctions.add(createUserDefinedFunctions(createdCollection));
        }

        waitIfNeededForReplicasToCatchUp(clientBuilder());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    public CosmosUserDefinedFunctionProperties createUserDefinedFunctions(CosmosAsyncContainer cosmosContainer) {
        CosmosUserDefinedFunctionProperties udf = new CosmosUserDefinedFunctionProperties();
        udf.setId(UUID.randomUUID().toString());
        udf.setBody("function() {var x = 10;}");
        return cosmosContainer.getScripts().createUserDefinedFunction(udf).block()
                .getProperties();
    }

    private String getCollectionLink() {
        return "dbs/" + getDatabaseId() + "/colls/" + getCollectionId();
    }

    private String getCollectionId() {
        return createdCollection.getId();
    }

    private String getDatabaseId() {
        return createdDatabase.getId();
    }
}
