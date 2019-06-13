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

package com.azure.data.cosmos.benchmark;

import com.azure.data.cosmos.AsyncDocumentClient;
import com.azure.data.cosmos.DataType;
import com.azure.data.cosmos.Database;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.IncludedPath;
import com.azure.data.cosmos.Index;
import com.azure.data.cosmos.IndexingPolicy;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.RequestOptions;
import com.azure.data.cosmos.rx.TestConfigurations;
import com.beust.jcommander.JCommander;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadMyWritesConsistencyTest {
    private final static Logger logger = LoggerFactory.getLogger(ReadMyWritesConsistencyTest.class);
    private final int initialCollectionThroughput = 10_000;
    private final int newCollectionThroughput = 100_000;
    private final int delayForInitiationCollectionScaleUpInSeconds = 60;
    private final Duration defaultMaxRunningTimeInSeconds = Duration.ofMinutes(45);

    private final String maxRunningTime =
                    System.getProperty("MAX_RUNNING_TIME", StringUtils.defaultString(Strings.emptyToNull(
                            System.getenv().get("MAX_RUNNING_TIME")), defaultMaxRunningTimeInSeconds.toString()));

    private final AtomicBoolean collectionScaleUpFailed = new AtomicBoolean(false);
    private final String desiredConsistency =
            System.getProperty("DESIRED_CONSISTENCY",
                               StringUtils.defaultString(Strings.emptyToNull(
                                       System.getenv().get("DESIRED_CONSISTENCY")), "SESSION"));

    private final String numberOfOperationsAsString =
            System.getProperty("NUMBER_OF_OPERATIONS",
                               StringUtils.defaultString(Strings.emptyToNull(
                                       System.getenv().get("NUMBER_OF_OPERATIONS")), "-1"));

    private Database database;
    private DocumentCollection collection;

    @Test(dataProvider = "collectionLinkTypeArgProvider", groups = "e2e")
    public void readMyWrites(boolean useNameLink) throws Exception {
        int concurrency = 5;
        String cmdFormat = "-serviceEndpoint %s -masterKey %s" +
                " -databaseId %s -collectionId %s" +
                " -consistencyLevel %s -concurrency %d" +
                " -numberOfOperations %s" +
                " -maxRunningTimeDuration %s" +
                " -operation ReadMyWrites -connectionMode DIRECT -numberOfPreCreatedDocuments 100 " +
                " -printingInterval 60";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.id(),
                                   collection.id(),
                                   desiredConsistency.toUpperCase(),
                                   concurrency,
                                   numberOfOperationsAsString,
                                   maxRunningTime)
                + (useNameLink ? " -useNameLink" : "");

        Configuration cfg = new Configuration();
        new JCommander(cfg, StringUtils.split(cmd));

        AtomicInteger success = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();

        ReadMyWriteWorkflow wf = new ReadMyWriteWorkflow(cfg) {
            @Override
            protected void onError(Throwable throwable) {
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

    @BeforeClass(groups = "e2e")
    public void beforeClass() {
        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(initialCollectionThroughput);
        AsyncDocumentClient housekeepingClient = Utils.housekeepingClient();
        database = Utils.createDatabaseForTest(housekeepingClient);
        collection = housekeepingClient.createCollection("dbs/" + database.id(),
                                                         getCollectionDefinitionWithRangeRangeIndex(),
                                                         options)
                .toBlocking().single().getResource();
        housekeepingClient.close();
    }

    @DataProvider(name = "collectionLinkTypeArgProvider")
    public Object[][] collectionLinkTypeArgProvider() {
        return new Object[][]{
                // is namebased
                {true},
        };
    }

    @AfterClass(groups = "e2e")
    public void afterClass() {
        AsyncDocumentClient housekeepingClient = Utils.housekeepingClient();
        Utils.safeCleanDatabases(housekeepingClient);
        Utils.safeClean(housekeepingClient, database);
        Utils.safeClose(housekeepingClient);
    }

    DocumentCollection getCollectionDefinitionWithRangeRangeIndex() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        Collection<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.path("/*");
        Collection<Index> indexes = new ArrayList<>();
        Index stringIndex = Index.Range(DataType.STRING);
        stringIndex.set("precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.NUMBER);
        numberIndex.set("precision", -1);
        indexes.add(numberIndex);
        includedPath.indexes(indexes);
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.id(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }

    private void scheduleScaleUp(int delayStartInSeconds, int newThroughput) {
        AsyncDocumentClient housekeepingClient = Utils.housekeepingClient();
        Observable.timer(delayStartInSeconds, TimeUnit.SECONDS, Schedulers.newThread()).flatMap(aVoid -> {

            // increase throughput to max for a single partition collection to avoid throttling
            // for bulk insert and later queries.
            return housekeepingClient.queryOffers(
                    String.format("SELECT * FROM r WHERE r.offerResourceId = '%s'",
                                  collection.resourceId())
                    , null).flatMap(page -> Observable.from(page.results()))
                    .first().flatMap(offer -> {
                        logger.info("going to scale up collection, newThroughput {}", newThroughput);
                        offer.setThroughput(newThroughput);
                        return housekeepingClient.replaceOffer(offer);
                    });
        }).doOnTerminate(() -> housekeepingClient.close())
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