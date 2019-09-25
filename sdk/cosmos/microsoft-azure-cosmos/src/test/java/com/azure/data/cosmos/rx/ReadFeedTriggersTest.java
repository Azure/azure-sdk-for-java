// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.*;
import com.azure.data.cosmos.CosmosAsyncClient;
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

//FIXME beforeClass times out inconsistently.
@Ignore
public class ReadFeedTriggersTest extends TestSuiteBase {

    private CosmosAsyncContainer createdCollection;
    private List<CosmosTriggerProperties> createdTriggers = new ArrayList<>();

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ReadFeedTriggersTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readTriggers() throws Exception {

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);

        Flux<FeedResponse<CosmosTriggerProperties>> feedObservable = createdCollection.getScripts().readAllTriggers(options);

        int expectedPageSize = (createdTriggers.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosTriggerProperties> validator = new FeedResponseListValidator.Builder<CosmosTriggerProperties>()
                .totalSize(createdTriggers.size())
                .exactlyContainsInAnyOrder(
                        createdTriggers.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosTriggerProperties>()
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
            this.createdTriggers.add(this.createTriggers(createdCollection));
        }

        waitIfNeededForReplicasToCatchUp(clientBuilder());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    public CosmosTriggerProperties createTriggers(CosmosAsyncContainer cosmosContainer) {
        CosmosTriggerProperties trigger = new CosmosTriggerProperties();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setBody("function() {var x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);
        return cosmosContainer.getScripts().createTrigger(trigger).block().getProperties();
    }
}
