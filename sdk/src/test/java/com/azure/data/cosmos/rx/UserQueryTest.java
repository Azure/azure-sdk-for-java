/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.CosmosUserSettings;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class UserQueryTest extends TestSuiteBase {

    public final String databaseId = CosmosDatabaseForTest.generateId();

    private List<CosmosUserSettings> createdUsers = new ArrayList<>();

    private CosmosClient client;
    private CosmosDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public UserQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryUsersWithFilter() throws Exception {
        
        String filterUserId = createdUsers.get(0).id();
        String query = String.format("SELECT * from c where c.id = '%s'", filterUserId);

        FeedOptions options = new FeedOptions();
        options.maxItemCount(5);
        Flux<FeedResponse<CosmosUserSettings>> queryObservable = createdDatabase.queryUsers(query, options);

        List<CosmosUserSettings> expectedUsers = createdUsers.stream()
                .filter(c -> StringUtils.equals(filterUserId, c.id()) ).collect(Collectors.toList());

        assertThat(expectedUsers).isNotEmpty();

        int expectedPageSize = (expectedUsers.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosUserSettings> validator = new FeedResponseListValidator.Builder<CosmosUserSettings>()
                .totalSize(expectedUsers.size())
                .exactlyContainsInAnyOrder(expectedUsers.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosUserSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAllUsers() throws Exception {

        String query = "SELECT * from c";

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);
        String databaseLink = Utils.getDatabaseNameLink(databaseId);
        Flux<FeedResponse<CosmosUserSettings>> queryObservable = createdDatabase.queryUsers(query, options);

        List<CosmosUserSettings> expectedUsers = createdUsers;

        assertThat(expectedUsers).isNotEmpty();

        int expectedPageSize = (expectedUsers.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosUserSettings> validator = new FeedResponseListValidator.Builder<CosmosUserSettings>()
                .totalSize(expectedUsers.size())
                .exactlyContainsInAnyOrder(expectedUsers.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosUserSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryUsers_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        Flux<FeedResponse<CosmosUserSettings>> queryObservable = createdDatabase.queryUsers(query, options);

        FeedResponseListValidator<CosmosUserSettings> validator = new FeedResponseListValidator.Builder<CosmosUserSettings>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosUserSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder().build();

        createdDatabase = createDatabase(client, databaseId);

        for(int i = 0; i < 5; i++) {
            CosmosUserSettings user = new CosmosUserSettings();
            user.id(UUID.randomUUID().toString());
            createdUsers.add(createUser(client, databaseId, user).read().block().settings());
        }

        waitIfNeededForReplicasToCatchUp(clientBuilder());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }
}
