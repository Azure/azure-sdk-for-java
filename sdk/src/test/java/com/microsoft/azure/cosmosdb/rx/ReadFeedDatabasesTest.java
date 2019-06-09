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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.microsoft.azure.cosmos.CosmosClientBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosDatabaseRequestOptions;
import com.microsoft.azure.cosmos.CosmosDatabaseSettings;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;

import reactor.core.publisher.Flux;

public class ReadFeedDatabasesTest extends TestSuiteBase {

    private List<CosmosDatabaseSettings> createdDatabases = new ArrayList<>();
    private List<CosmosDatabaseSettings> allDatabases = new ArrayList<>();

    private CosmosClient client;

    @Factory(dataProvider = "clientBuilders")
    public ReadFeedDatabasesTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readDatabases() throws Exception {

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(2);

        Flux<FeedResponse<CosmosDatabaseSettings>> feedObservable = client.listDatabases(options);

        int expectedPageSize = (allDatabases.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();
        FeedResponseListValidator<CosmosDatabaseSettings> validator = new FeedResponseListValidator.Builder<CosmosDatabaseSettings>()
                .totalSize(allDatabases.size())
                .exactlyContainsInAnyOrder(allDatabases.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosDatabaseSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws URISyntaxException {
        client = clientBuilder.build();
        allDatabases = client.listDatabases(null)
                             .map(frp -> frp.getResults())
                             .collectList()
                             .map(list -> list.stream().flatMap(x -> x.stream()).collect(Collectors.toList()))
                             .block();
        for(int i = 0; i < 5; i++) {
            createdDatabases.add(createDatabase(client));
        }
        allDatabases.addAll(createdDatabases);
    }

    public CosmosDatabaseSettings createDatabase(CosmosClient client) {
        CosmosDatabaseSettings db = new CosmosDatabaseSettings(UUID.randomUUID().toString());
        return client.createDatabase(db, new CosmosDatabaseRequestOptions()).block().getCosmosDatabaseSettings();
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        for (int i = 0; i < 5; i ++) {
            safeDeleteDatabase(client.getDatabase(createdDatabases.get(i).getId()));
        }
        safeClose(client);
    }
}
