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
package com.microsoft.azure.cosmosdb.rx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.microsoft.azure.cosmos.CosmosClientBuilder;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosDatabase;
import com.microsoft.azure.cosmos.CosmosDatabaseSettings;
import com.microsoft.azure.cosmos.CosmosDatabaseForTest;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;

import reactor.core.publisher.Flux;

public class DatabaseQueryTest extends TestSuiteBase {

    public final String databaseId1 = CosmosDatabaseForTest.generateId();
    public final String databaseId2 = CosmosDatabaseForTest.generateId();

    private List<CosmosDatabase> createdDatabases = new ArrayList<>();

    private CosmosClient client;
    
    @Factory(dataProvider = "clientBuilders")
    public DatabaseQueryTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDatabaseWithFilter() throws Exception {
        String query = String.format("SELECT * from c where c.id = '%s'", databaseId1);

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(2);
        Flux<FeedResponse<CosmosDatabaseSettings>> queryObservable = client.queryDatabases(query, options);

        List<CosmosDatabaseSettings> expectedDatabases = createdDatabases.stream()
                .filter(d -> StringUtils.equals(databaseId1, d.getId()) ).map(d -> d.read().block().getCosmosDatabaseSettings()).collect(Collectors.toList());

        assertThat(expectedDatabases).isNotEmpty();

        int expectedPageSize = (expectedDatabases.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<CosmosDatabaseSettings> validator = new FeedResponseListValidator.Builder<CosmosDatabaseSettings>()
                .totalSize(expectedDatabases.size())
                .exactlyContainsInAnyOrder(expectedDatabases.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosDatabaseSettings>()
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
        options.setMaxItemCount(2);
        Flux<FeedResponse<CosmosDatabaseSettings>> queryObservable = client.queryDatabases(query, options);

        List<CosmosDatabaseSettings> expectedDatabases = createdDatabases.stream().map(d -> d.read().block().getCosmosDatabaseSettings()).collect(Collectors.toList());

        assertThat(expectedDatabases).isNotEmpty();

        int expectedPageSize = (expectedDatabases.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<CosmosDatabaseSettings> validator = new FeedResponseListValidator.Builder<CosmosDatabaseSettings>()
                .totalSize(expectedDatabases.size())
                .exactlyContainsInAnyOrder(expectedDatabases.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosDatabaseSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDatabases_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosDatabaseSettings>> queryObservable = client.queryDatabases(query, options);

        FeedResponseListValidator<CosmosDatabaseSettings> validator = new FeedResponseListValidator.Builder<CosmosDatabaseSettings>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosDatabaseSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();
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
