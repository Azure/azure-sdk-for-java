// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosPagedFlux;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosUserProperties;
import com.azure.cosmos.FeedOptions;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import com.azure.cosmos.implementation.TestUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class UserQueryTest extends TestSuiteBase {

    public final String databaseId = CosmosDatabaseForTest.generateId();

    private List<CosmosUserProperties> createdUsers = new ArrayList<>();

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public UserQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryUsersWithFilter() throws Exception {

        String filterUserId = createdUsers.get(0).getId();
        String query = String.format("SELECT * from c where c.id = '%s'", filterUserId);

        FeedOptions options = new FeedOptions();
        int maxItemCount = 5;
        CosmosPagedFlux<CosmosUserProperties> queryObservable = createdDatabase.queryUsers(query, options);

        List<CosmosUserProperties> expectedUsers = createdUsers.stream()
                                                               .filter(c -> StringUtils.equals(filterUserId, c.getId()) ).collect(Collectors.toList());

        assertThat(expectedUsers).isNotEmpty();

        int expectedPageSize = (expectedUsers.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosUserProperties> validator = new FeedResponseListValidator.Builder<CosmosUserProperties>()
                .totalSize(expectedUsers.size())
                .exactlyContainsInAnyOrder(expectedUsers.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosUserProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable.byPage(maxItemCount), validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAllUsers() throws Exception {

        String query = "SELECT * from c";

        FeedOptions options = new FeedOptions();
        int maxItemCount = 2;
        String databaseLink = TestUtils.getDatabaseNameLink(databaseId);
        CosmosPagedFlux<CosmosUserProperties> queryObservable = createdDatabase.queryUsers(query, options);

        List<CosmosUserProperties> expectedUsers = createdUsers;

        assertThat(expectedUsers).isNotEmpty();

        int expectedPageSize = (expectedUsers.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosUserProperties> validator = new FeedResponseListValidator.Builder<CosmosUserProperties>()
                .totalSize(expectedUsers.size())
                .exactlyContainsInAnyOrder(expectedUsers.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosUserProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable.byPage(maxItemCount), validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryUsers_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        CosmosPagedFlux<CosmosUserProperties> queryObservable = createdDatabase.queryUsers(query, options);

        FeedResponseListValidator<CosmosUserProperties> validator = new FeedResponseListValidator.Builder<CosmosUserProperties>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosUserProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable.byPage(), validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_UserQueryTest() throws Exception {
        client = clientBuilder().buildAsyncClient();

        createdDatabase = createDatabase(client, databaseId);

        for(int i = 0; i < 5; i++) {
            CosmosUserProperties user = new CosmosUserProperties();
            user.setId(UUID.randomUUID().toString());
            createdUsers.add(createUser(client, databaseId, user).read().block().getProperties());
        }

        waitIfNeededForReplicasToCatchUp(clientBuilder());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }
}
