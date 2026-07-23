// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReadFeedDatabasesTest extends TestSuiteBase {

    private List<CosmosDatabaseProperties> createdDatabases = new ArrayList<>();
    private List<CosmosDatabaseProperties> allDatabases = new ArrayList<>();

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public ReadFeedDatabasesTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "query" }, timeOut = FEED_TIMEOUT)
    public void readDatabases() throws Exception {
        int maxItemCount = 2;

        CosmosPagedFlux<CosmosDatabaseProperties> feedObservable = client.readAllDatabases();

        int expectedPageSize = (allDatabases.size() + maxItemCount - 1) / maxItemCount;
        FeedResponseListValidator<CosmosDatabaseProperties> validator = new FeedResponseListValidator.Builder<CosmosDatabaseProperties>()
                .totalSize(allDatabases.size())
                .exactlyContainsInAnyOrder(allDatabases.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosDatabaseProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(feedObservable.byPage(maxItemCount), validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "query" }, timeOut = SETUP_TIMEOUT)
    public void before_ReadFeedDatabasesTest() throws URISyntaxException {
        client = getClientBuilder().buildAsyncClient();
        allDatabases = client.readAllDatabases()
                             .collectList()
                             .block();
        for(int i = 0; i < 5; i++) {
            createdDatabases.add(createDatabase(client));
        }
        allDatabases.addAll(createdDatabases);
    }

    public CosmosDatabaseProperties createDatabase(CosmosAsyncClient client) {
        CosmosDatabaseProperties db = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        return client.createDatabase(db, new CosmosDatabaseRequestOptions()).block().getProperties();
    }

    @AfterClass(groups = { "query" }, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        // Delete the created databases in parallel, mirroring safeDeleteDatabase's contract:
        // deletion is best-effort and no error is ever propagated out of this cleanup method.
        // A bounded per-operation timeout ensures a single slow/hung control-plane delete (e.g.
        // throttling on a shared account) self-terminates well within the method timeout instead
        // of consuming it. onErrorResume swallows each op's failure so the stream never errors,
        // and the outer try/catch is a final backstop. Any database left undeleted is named with
        // the shared test prefix, so the stale-database janitor (@AfterSuite) will reclaim it.
        try {
            Flux.fromIterable(createdDatabases)
                .flatMap(properties -> client.getDatabase(properties.getId())
                    .delete()
                    .timeout(Duration.ofSeconds(20))
                    .onErrorResume(error -> {
                        logger.warn("Failed to delete database {} during cleanup; the stale-database "
                            + "janitor will reclaim it.", properties.getId(), error);
                        return Mono.empty();
                    }))
                .blockLast();
        } catch (Exception e) {
            logger.warn("Unexpected error during database cleanup; the stale-database janitor will reclaim any leaks.", e);
        }
        safeClose(client);
    }
}
