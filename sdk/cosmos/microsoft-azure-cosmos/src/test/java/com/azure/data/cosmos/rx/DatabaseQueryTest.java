// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.CosmosDatabaseProperties;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import com.azure.data.cosmos.internal.FeedResponseValidator;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseQueryTest extends TestSuiteBase {

    public final String databaseId1 = CosmosDatabaseForTest.generateId();
    public final String databaseId2 = CosmosDatabaseForTest.generateId();

    private List<CosmosDatabase> createdDatabases = new ArrayList<>();

    private CosmosClient client;
    
    @Factory(dataProvider = "clientBuilders")
    public DatabaseQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDatabaseWithFilter() throws Exception {
        String query = String.format("SELECT * from c where c.id = '%s'", databaseId1);

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);
        Flux<FeedResponse<CosmosDatabaseProperties>> queryObservable = client.queryDatabases(query, options);

        List<CosmosDatabaseProperties> expectedDatabases = createdDatabases.stream()
                                                                           .filter(d -> StringUtils.equals(databaseId1, d.id()) ).map(d -> d.read().block().properties()).collect(Collectors.toList());

        assertThat(expectedDatabases).isNotEmpty();

        int expectedPageSize = (expectedDatabases.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosDatabaseProperties> validator = new FeedResponseListValidator.Builder<CosmosDatabaseProperties>()
                .totalSize(expectedDatabases.size())
                .exactlyContainsInAnyOrder(expectedDatabases.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosDatabaseProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAllDatabase() throws Exception {

        String query = String.format("SELECT * from c where c.id in ('%s', '%s')",
                                     databaseId1,
                                     databaseId2);

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);
        Flux<FeedResponse<CosmosDatabaseProperties>> queryObservable = client.queryDatabases(query, options);

        List<CosmosDatabaseProperties> expectedDatabases = createdDatabases.stream().map(d -> d.read().block().properties()).collect(Collectors.toList());

        assertThat(expectedDatabases).isNotEmpty();

        int expectedPageSize = (expectedDatabases.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosDatabaseProperties> validator = new FeedResponseListValidator.Builder<CosmosDatabaseProperties>()
                .totalSize(expectedDatabases.size())
                .exactlyContainsInAnyOrder(expectedDatabases.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosDatabaseProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDatabases_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosDatabaseProperties>> queryObservable = client.queryDatabases(query, options);

        FeedResponseListValidator<CosmosDatabaseProperties> validator = new FeedResponseListValidator.Builder<CosmosDatabaseProperties>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosDatabaseProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder().build();
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
