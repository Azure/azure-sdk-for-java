// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.CosmosUserProperties;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import com.azure.data.cosmos.internal.FeedResponseValidator;
import com.azure.data.cosmos.internal.TestUtils;
import org.apache.commons.lang3.StringUtils;
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

import static org.assertj.core.api.Assertions.assertThat;

public class UserQueryTest extends TestSuiteBase {

    public final String databaseId = CosmosDatabaseForTest.generateId();

    private List<CosmosUserProperties> createdUsers = new ArrayList<>();

    private CosmosClient client;
    private CosmosDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public UserQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    //FIXME test times out inconsistently
    @Ignore
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryUsersWithFilter() throws Exception {
        
        String filterUserId = createdUsers.get(0).id();
        String query = String.format("SELECT * from c where c.id = '%s'", filterUserId);

        FeedOptions options = new FeedOptions();
        options.maxItemCount(5);
        Flux<FeedResponse<CosmosUserProperties>> queryObservable = createdDatabase.queryUsers(query, options);

        List<CosmosUserProperties> expectedUsers = createdUsers.stream()
                                                               .filter(c -> StringUtils.equals(filterUserId, c.id()) ).collect(Collectors.toList());

        assertThat(expectedUsers).isNotEmpty();

        int expectedPageSize = (expectedUsers.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosUserProperties> validator = new FeedResponseListValidator.Builder<CosmosUserProperties>()
                .totalSize(expectedUsers.size())
                .exactlyContainsInAnyOrder(expectedUsers.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosUserProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAllUsers() throws Exception {

        String query = "SELECT * from c";

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);
        String databaseLink = TestUtils.getDatabaseNameLink(databaseId);
        Flux<FeedResponse<CosmosUserProperties>> queryObservable = createdDatabase.queryUsers(query, options);

        List<CosmosUserProperties> expectedUsers = createdUsers;

        assertThat(expectedUsers).isNotEmpty();

        int expectedPageSize = (expectedUsers.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosUserProperties> validator = new FeedResponseListValidator.Builder<CosmosUserProperties>()
                .totalSize(expectedUsers.size())
                .exactlyContainsInAnyOrder(expectedUsers.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosUserProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryUsers_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        Flux<FeedResponse<CosmosUserProperties>> queryObservable = createdDatabase.queryUsers(query, options);

        FeedResponseListValidator<CosmosUserProperties> validator = new FeedResponseListValidator.Builder<CosmosUserProperties>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosUserProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder().build();

        createdDatabase = createDatabase(client, databaseId);

        for(int i = 0; i < 5; i++) {
            CosmosUserProperties user = new CosmosUserProperties();
            user.id(UUID.randomUUID().toString());
            createdUsers.add(createUser(client, databaseId, user).read().block().properties());
        }

        waitIfNeededForReplicasToCatchUp(clientBuilder());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }
}
