// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosUserDefinedFunctionProperties;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import com.azure.data.cosmos.internal.FeedResponseValidator;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.annotations.Ignore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

//FIXME beforeClass times out.
@Ignore
public class ReadFeedUdfsTest extends TestSuiteBase {

    private Database createdDatabase;
    private CosmosContainer createdCollection;
    private List<CosmosUserDefinedFunctionProperties> createdUserDefinedFunctions = new ArrayList<>();

    private CosmosClient client;

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
                        createdUserDefinedFunctions.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosUserDefinedFunctionProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
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

    public CosmosUserDefinedFunctionProperties createUserDefinedFunctions(CosmosContainer cosmosContainer) {
        CosmosUserDefinedFunctionProperties udf = new CosmosUserDefinedFunctionProperties();
        udf.id(UUID.randomUUID().toString());
        udf.body("function() {var x = 10;}");
        return cosmosContainer.getScripts().createUserDefinedFunction(udf).block()
                .properties();
    }

    private String getCollectionLink() {
        return "dbs/" + getDatabaseId() + "/colls/" + getCollectionId();
    }

    private String getCollectionId() {
        return createdCollection.id();
    }

    private String getDatabaseId() {
        return createdDatabase.id();
    }
}
