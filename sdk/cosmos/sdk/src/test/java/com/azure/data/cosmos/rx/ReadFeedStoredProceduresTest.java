// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosStoredProcedureRequestOptions;
import com.azure.data.cosmos.CosmosStoredProcedureProperties;
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

//FIXME beforeClass times out inconsistently
@Ignore
public class ReadFeedStoredProceduresTest extends TestSuiteBase {

    private CosmosContainer createdCollection;
    private List<CosmosStoredProcedureProperties> createdStoredProcedures = new ArrayList<>();

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ReadFeedStoredProceduresTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readStoredProcedures() throws Exception {

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);

        Flux<FeedResponse<CosmosStoredProcedureProperties>> feedObservable = createdCollection.getScripts()
                .readAllStoredProcedures(options);

        int expectedPageSize = (createdStoredProcedures.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosStoredProcedureProperties> validator = new FeedResponseListValidator.Builder<CosmosStoredProcedureProperties>()
                .totalSize(createdStoredProcedures.size())
                .exactlyContainsInAnyOrder(
                        createdStoredProcedures.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosStoredProcedureProperties>()
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
            createdStoredProcedures.add(createStoredProcedures(createdCollection));
        }

        waitIfNeededForReplicasToCatchUp(clientBuilder());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    public CosmosStoredProcedureProperties createStoredProcedures(CosmosContainer cosmosContainer) {
        CosmosStoredProcedureProperties sproc = new CosmosStoredProcedureProperties();
        sproc.id(UUID.randomUUID().toString());
        sproc.body("function() {var x = 10;}");
        return cosmosContainer.getScripts().createStoredProcedure(sproc, new CosmosStoredProcedureRequestOptions())
                .block().properties();
    }
}
