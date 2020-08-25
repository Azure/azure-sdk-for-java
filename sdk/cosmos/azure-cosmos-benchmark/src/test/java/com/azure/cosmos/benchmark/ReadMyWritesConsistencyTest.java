// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.beust.jcommander.JCommander;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;
import static org.assertj.core.api.Assertions.assertThat;

public class ReadMyWritesConsistencyTest {

    private final static Logger logger = LoggerFactory.getLogger(ReadMyWritesConsistencyTest.class);

    private final AtomicBoolean collectionScaleUpFailed = new AtomicBoolean(false);
    private final Duration defaultMaxRunningTime = Duration.ofMinutes(45);
    private final int delayForInitiationCollectionScaleUpInSeconds = 60;

    private final String desiredConsistency =
        System.getProperty("DESIRED_CONSISTENCY",
            StringUtils.defaultString(Strings.emptyToNull(
                System.getenv().get("DESIRED_CONSISTENCY")), "Session"));

    private final int initialCollectionThroughput = 10_000;

    private final String maxRunningTime =
        System.getProperty("MAX_RUNNING_TIME", StringUtils.defaultString(Strings.emptyToNull(
            System.getenv().get("MAX_RUNNING_TIME")), defaultMaxRunningTime.toString()));

    private final int newCollectionThroughput = 100_000;

    private final String numberOfOperationsAsString =
        System.getProperty("NUMBER_OF_OPERATIONS",
            StringUtils.defaultString(Strings.emptyToNull(
                System.getenv().get("NUMBER_OF_OPERATIONS")), "-1"));

    private DocumentCollection collection;
    private Database database;

    @AfterClass(groups = "e2e")
    public void afterClass() {
        AsyncDocumentClient housekeepingClient = Utils.housekeepingClient();
        Utils.safeCleanDatabases(housekeepingClient);
        Utils.safeClean(housekeepingClient, database);
        Utils.safeClose(housekeepingClient);
    }

    @BeforeClass(groups = "e2e")
    public void before_ReadMyWritesConsistencyTest() {
        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(initialCollectionThroughput);
        AsyncDocumentClient housekeepingClient = Utils.housekeepingClient();
        database = Utils.createDatabaseForTest(housekeepingClient);
        collection = housekeepingClient.createCollection("dbs/" + database.getId(),
            getCollectionDefinitionWithRangeRangeIndex(),
            options).block().getResource();
        housekeepingClient.close();
    }

    @DataProvider(name = "collectionLinkTypeArgProvider")
    public Object[][] collectionLinkTypeArgProvider() {
        return new Object[][] {
            // is namebased
            { true },
        };
    }

    @Test(dataProvider = "collectionLinkTypeArgProvider", groups = "e2e")
    public void readMyWrites(boolean useNameLink) throws Exception {

        int concurrency = 5;

        String cmdFormat = "-serviceEndpoint %s -masterKey %s" +
            " -databaseId %s" +
            " -collectionId %s" +
            " -consistencyLevel %s" +
            " -concurrency %s" +
            " -numberOfOperations %s" +
            " -maxRunningTimeDuration %s" +
            " -operation ReadMyWrites" +
            " -connectionMode Direct" +
            " -numberOfPreCreatedDocuments 100" +
            " -printingInterval 60" +
            "%s";

        String cmd = lenientFormat(cmdFormat,
            TestConfigurations.HOST,
            TestConfigurations.MASTER_KEY,
            database.getId(),
            collection.getId(),
            desiredConsistency,
            concurrency,
            numberOfOperationsAsString,
            maxRunningTime,
            (useNameLink ? " -useNameLink" : ""));

        Configuration cfg = new Configuration();
        new JCommander(cfg, StringUtils.split(cmd));

        AtomicInteger success = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();

        ReadMyWriteWorkflow wf = new ReadMyWriteWorkflow(cfg) {
            @Override
            protected void onError(Throwable throwable) {
                logger.error("Error occurred in ReadMyWriteWorkflow", throwable);
                error.incrementAndGet();
            }

            @Override
            protected void onSuccess() {
                success.incrementAndGet();
            }
        };

        // schedules a collection scale up after a delay
        scheduleScaleUp(delayForInitiationCollectionScaleUpInSeconds, newCollectionThroughput);

        wf.run();
        wf.shutdown();

        int numberOfOperations = Integer.parseInt(numberOfOperationsAsString);

        assertThat(error).hasValue(0);
        assertThat(collectionScaleUpFailed).isFalse();

        if (numberOfOperations > 0) {
            assertThat(success).hasValue(numberOfOperations);
        }
    }

    DocumentCollection getCollectionDefinitionWithRangeRangeIndex() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath("/*");
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }

    private void scheduleScaleUp(int delayStartInSeconds, int newThroughput) {
        AsyncDocumentClient housekeepingClient = Utils.housekeepingClient();
        Flux.just(0L).delayElements(Duration.ofSeconds(delayStartInSeconds), Schedulers.newSingle("ScaleUpThread")).flatMap(aVoid -> {

            // increase throughput to max for a single partition collection to avoid throttling
            // for bulk insert and later queries.
            return housekeepingClient.queryOffers(
                String.format("SELECT * FROM r WHERE r.offerResourceId = '%s'",
                    collection.getResourceId())
                , null).flatMap(page -> Flux.fromIterable(page.getResults()))
                                     .take(1).flatMap(offer -> {
                    logger.info("going to scale up collection, newThroughput {}", newThroughput);
                    offer.setThroughput(newThroughput);
                    return housekeepingClient.replaceOffer(offer);
                });
        }).doOnTerminate(housekeepingClient::close)
            .subscribe(aVoid -> {
                }, e -> {
                    logger.error("collectionScaleUpFailed to scale up collection", e);
                    collectionScaleUpFailed.set(true);
                },
                () -> {
                    logger.info("Collection Scale up request sent to the service");

                }
            );
    }
}
