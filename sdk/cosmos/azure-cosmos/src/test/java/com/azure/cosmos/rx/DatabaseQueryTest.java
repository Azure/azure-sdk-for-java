// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseQueryTest extends TestSuiteBase {

    public final String databaseId1 = CosmosDatabaseForTest.generateId();
    public final String databaseId2 = CosmosDatabaseForTest.generateId();

    private List<CosmosAsyncDatabase> createdDatabases = new ArrayList<>();

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public DatabaseQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDatabaseWithFilter() throws Exception {
        String query = String.format("SELECT * from c where c.id = '%s'", databaseId1);

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        int maxItemCount = 2;
        CosmosPagedFlux<CosmosDatabaseProperties> queryObservable = client.queryDatabases(query, options);

        List<CosmosDatabaseProperties> expectedDatabases = createdDatabases.stream()
                                                                           .filter(d -> StringUtils.equals(databaseId1, d.getId()) ).map(d -> d.read().block().getProperties()).collect(Collectors.toList());

        assertThat(expectedDatabases).isNotEmpty();

        int expectedPageSize = (expectedDatabases.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosDatabaseProperties> validator = new FeedResponseListValidator.Builder<CosmosDatabaseProperties>()
                .totalSize(expectedDatabases.size())
                .exactlyContainsInAnyOrder(expectedDatabases.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosDatabaseProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable.byPage(maxItemCount), validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAllDatabase() throws Exception {

        String query = String.format("SELECT * from c where c.id in ('%s', '%s')",
                                     databaseId1,
                                     databaseId2);

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        int maxItemCount = 2;
        CosmosPagedFlux<CosmosDatabaseProperties> queryObservable = client.queryDatabases(query, options);

        List<CosmosDatabaseProperties> expectedDatabases = createdDatabases.stream().map(d -> d.read().block().getProperties()).collect(Collectors.toList());

        assertThat(expectedDatabases).isNotEmpty();

        int expectedPageSize = (expectedDatabases.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosDatabaseProperties> validator = new FeedResponseListValidator.Builder<CosmosDatabaseProperties>()
                .totalSize(expectedDatabases.size())
                .exactlyContainsInAnyOrder(expectedDatabases.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosDatabaseProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable.byPage(maxItemCount), validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDatabases_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        CosmosPagedFlux<CosmosDatabaseProperties> queryObservable = client.queryDatabases(query, options);

        FeedResponseListValidator<CosmosDatabaseProperties> validator = new FeedResponseListValidator.Builder<CosmosDatabaseProperties>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosDatabaseProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable.byPage(), validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_DatabaseQueryTest() throws Exception {
        client = getClientBuilder().buildAsyncClient();
        createdDatabases.add(createDatabase(client, databaseId1));
        createdDatabases.add(createDatabase(client, databaseId2));
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabases.get(0));
        safeDeleteDatabase(createdDatabases.get(1));

        safeClose(client);
    }
}
