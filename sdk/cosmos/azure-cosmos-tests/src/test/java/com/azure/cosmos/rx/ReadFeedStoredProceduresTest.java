// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReadFeedStoredProceduresTest extends TestSuiteBase {

    private static final Duration STORED_PROCEDURE_READ_FEED_RETRY_DELAY = Duration.ofSeconds(1);

    private static final int STORED_PROCEDURE_READ_FEED_ATTEMPT_TIMEOUT = 5_000;

    private static final Duration STORED_PROCEDURE_READ_FEED_MAX_RETRY_DURATION = Duration.ofSeconds(30);

    private CosmosAsyncContainer createdCollection;
    private List<CosmosStoredProcedureProperties> createdStoredProcedures = new ArrayList<>();

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ReadFeedStoredProceduresTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "query" }, timeOut = FEED_TIMEOUT)
    public void readStoredProcedures() throws Exception {
        int maxItemCount = 2;
        int expectedPageSize = (createdStoredProcedures.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosStoredProcedureProperties> validator = new FeedResponseListValidator.Builder<CosmosStoredProcedureProperties>()
                .totalSize(createdStoredProcedures.size())
                .exactlyContainsIdsInAnyOrder(
                        createdStoredProcedures.stream().map(CosmosStoredProcedureProperties::getId).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosStoredProcedureProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateReadStoredProceduresWithRetry(maxItemCount, validator);
    }

    private void validateReadStoredProceduresWithRetry(
        int maxItemCount,
        FeedResponseListValidator<CosmosStoredProcedureProperties> validator) throws InterruptedException {

        long retryStartNanos = System.nanoTime();
        AssertionError lastAssertionError;

        do {
            try {
                CosmosPagedFlux<CosmosStoredProcedureProperties> feedObservable = createdCollection.getScripts()
                    .readAllStoredProcedures();
                validateQuerySuccess(feedObservable.byPage(maxItemCount), validator, STORED_PROCEDURE_READ_FEED_ATTEMPT_TIMEOUT);
                return;
            } catch (AssertionError assertionError) {
                lastAssertionError = assertionError;
                Duration elapsed = Duration.ofNanos(System.nanoTime() - retryStartNanos);
                if (elapsed.compareTo(STORED_PROCEDURE_READ_FEED_MAX_RETRY_DURATION) >= 0) {
                    throw lastAssertionError;
                }

                logger.warn(
                    "Stored procedure read feed did not return all created stored procedures yet. Retrying after {}.",
                    STORED_PROCEDURE_READ_FEED_RETRY_DELAY);
                Thread.sleep(STORED_PROCEDURE_READ_FEED_RETRY_DELAY.toMillis());
            }
        } while (true);
    }

    @BeforeClass(groups = { "query" }, timeOut = SETUP_TIMEOUT)
    public void before_ReadFeedStoredProceduresTest() {
        client = getClientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        cleanUpContainer(createdCollection);

        for (int i = 0; i < 5; i++) {
            createdStoredProcedures.add(createStoredProcedures(createdCollection));
        }

        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    @AfterClass(groups = { "query" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    public CosmosStoredProcedureProperties createStoredProcedures(CosmosAsyncContainer cosmosContainer) {
        CosmosStoredProcedureProperties sproc = new CosmosStoredProcedureProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );

        return cosmosContainer.getScripts().createStoredProcedure(sproc, new CosmosStoredProcedureRequestOptions())
                .block().getProperties();
    }
}
