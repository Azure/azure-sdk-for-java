// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.models.CosmosUserProperties;
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

public class ReadFeedUsersTest extends TestSuiteBase {

    public final String databaseId = CosmosDatabaseForTest.generateId();
    private CosmosAsyncDatabase createdDatabase;

    private CosmosAsyncClient client;
    private List<CosmosUserProperties> createdUsers = new ArrayList<>();

    @Factory(dataProvider = "clientBuilders")
    public ReadFeedUsersTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readUsers() throws Exception {
        int maxItemCount = 2;

        CosmosPagedFlux<CosmosUserProperties> feedObservable = createdDatabase.readAllUsers();

        int expectedPageSize = (createdUsers.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosUserProperties> validator = new FeedResponseListValidator.Builder<CosmosUserProperties>()
                .totalSize(createdUsers.size())
                .exactlyContainsIdsInAnyOrder(createdUsers.stream().map(CosmosUserProperties::getId).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosUserProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable.byPage(maxItemCount), validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_ReadFeedUsersTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = createDatabase(client, databaseId);

        for(int i = 0; i < 5; i++) {
            createdUsers.add(createUsers(createdDatabase));
        }

        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }

    public CosmosUserProperties createUsers(CosmosAsyncDatabase cosmosDatabase) {
        CosmosUserProperties user = new CosmosUserProperties();
        user.setId(UUID.randomUUID().toString());
        return cosmosDatabase.createUser(user).block().getProperties();
    }
}
