// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.*;
import com.azure.data.cosmos.CosmosAsyncClient;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import com.azure.data.cosmos.internal.FeedResponseValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReadFeedDatabasesTest extends TestSuiteBase {

    private List<CosmosDatabaseProperties> createdDatabases = new ArrayList<>();
    private List<CosmosDatabaseProperties> allDatabases = new ArrayList<>();

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public ReadFeedDatabasesTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readDatabases() throws Exception {

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);

        Flux<FeedResponse<CosmosDatabaseProperties>> feedObservable = client.readAllDatabases(options);

        int expectedPageSize = (allDatabases.size() + options.maxItemCount() - 1) / options.maxItemCount();
        FeedResponseListValidator<CosmosDatabaseProperties> validator = new FeedResponseListValidator.Builder<CosmosDatabaseProperties>()
                .totalSize(allDatabases.size())
                .exactlyContainsInAnyOrder(allDatabases.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosDatabaseProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws URISyntaxException {
        client = clientBuilder().buildAsyncClient();
        allDatabases = client.readAllDatabases(null)
                             .map(frp -> frp.getResults())
                             .collectList()
                             .map(list -> list.stream().flatMap(x -> x.stream()).collect(Collectors.toList()))
                             .block();
        for(int i = 0; i < 5; i++) {
            createdDatabases.add(createDatabase(client));
        }
        allDatabases.addAll(createdDatabases);
    }

    public CosmosDatabaseProperties createDatabase(CosmosAsyncClient client) {
        CosmosDatabaseProperties db = new CosmosDatabaseProperties(UUID.randomUUID().toString());
        return client.createDatabase(db, new CosmosDatabaseRequestOptions()).block().getProperties();
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        for (int i = 0; i < 5; i ++) {
            safeDeleteDatabase(client.getDatabase(createdDatabases.get(i).getId()));
        }
        safeClose(client);
    }
}
