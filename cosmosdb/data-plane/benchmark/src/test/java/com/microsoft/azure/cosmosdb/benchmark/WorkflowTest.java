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

package com.microsoft.azure.cosmosdb.benchmark;

import com.beust.jcommander.JCommander;
import com.microsoft.azure.cosmosdb.DataType;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.IncludedPath;
import com.microsoft.azure.cosmosdb.Index;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.TestConfigurations;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkflowTest {
    private static final int TIMEOUT = 120000;
    private Database database;
    private DocumentCollection collection;

    @Test(groups = "simple", timeOut = TIMEOUT)
    public void readMyWritesCLI() throws Exception {
        String cmdFormat = "-serviceEndpoint %s -masterKey %s" +
                " -databaseId %s -collectionId %s" +
                " -consistencyLevel Session -concurrency 2 -numberOfOperations 123" +
                " -operation ReadMyWrites -connectionMode Direct -numberOfPreCreatedDocuments 100";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.getId(),
                                   collection.getId());
        Main.main(StringUtils.split(cmd));
    }

    @Test(dataProvider = "collectionLinkTypeArgProvider", groups = "simple", timeOut = TIMEOUT)
    public void readMyWrites(boolean useNameLink) throws Exception {
        int numberOfOperations = 123;
        String cmdFormat = "-serviceEndpoint %s -masterKey %s" +
                " -databaseId %s -collectionId %s" +
                " -consistencyLevel Session -concurrency 2 -numberOfOperations %s" +
                " -operation ReadMyWrites -connectionMode Direct -numberOfPreCreatedDocuments 100";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.getId(),
                                   collection.getId(),
                                   numberOfOperations)
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

        wf.run();
        wf.shutdown();

        assertThat(error).hasValue(0);
        assertThat(success).hasValue(numberOfOperations);
    }

    @Test(groups = "simple", timeOut = TIMEOUT)
    public void writeLatencyCLI() throws Exception {
        String cmdFormat = "-serviceEndpoint %s -masterKey %s" +
                " -databaseId %s -collectionId %s" +
                " -consistencyLevel Session -concurrency 2 -numberOfOperations 1000" +
                " -operation WriteLatency -connectionMode Direct";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.getId(),
                                   collection.getId());
        Main.main(StringUtils.split(cmd));
    }

    @Test(dataProvider = "collectionLinkTypeArgProvider", groups = "simple", timeOut = TIMEOUT)
    public void writeLatency(boolean useNameLink) throws Exception {
        int numberOfOperations = 123;
        String cmdFormat = "-serviceEndpoint %s -masterKey %s" +
                " -databaseId %s -collectionId %s" +
                " -consistencyLevel Session -concurrency 2 -numberOfOperations %s" +
                " -operation WriteLatency -connectionMode Direct";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.getId(),
                                   collection.getId(),
                                   numberOfOperations)
                + (useNameLink ? " -useNameLink" : "");

        Configuration cfg = new Configuration();
        new JCommander(cfg, StringUtils.split(cmd));

        AtomicInteger success = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();

        AsyncWriteBenchmark wf = new AsyncWriteBenchmark(cfg) {
            @Override
            protected void onError(Throwable throwable) {
                error.incrementAndGet();
            }

            @Override
            protected void onSuccess() {
                success.incrementAndGet();
            }
        };

        wf.run();
        wf.shutdown();

        assertThat(error).hasValue(0);
        assertThat(success).hasValue(numberOfOperations);
    }

    @Test(dataProvider = "collectionLinkTypeArgProvider", groups = "simple", timeOut = TIMEOUT)
    public void writeThroughput(boolean useNameLink) throws Exception {
        int numberOfOperations = 123;
        String cmdFormat = "-serviceEndpoint %s -masterKey %s" +
                " -databaseId %s -collectionId %s" +
                " -consistencyLevel Session -concurrency 2 -numberOfOperations %s" +
                " -operation WriteThroughput -connectionMode Direct";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.getId(),
                                   collection.getId(),
                                   numberOfOperations)
                + (useNameLink ? " -useNameLink" : "");

        Configuration cfg = new Configuration();
        new JCommander(cfg, StringUtils.split(cmd));

        AtomicInteger success = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();

        AsyncWriteBenchmark wf = new AsyncWriteBenchmark(cfg) {
            @Override
            protected void onError(Throwable throwable) {
                error.incrementAndGet();
            }

            @Override
            protected void onSuccess() {
                success.incrementAndGet();
            }
        };

        wf.run();
        wf.shutdown();

        assertThat(error).hasValue(0);
        assertThat(success).hasValue(numberOfOperations);
    }

    @Test(dataProvider = "collectionLinkTypeArgProvider", groups = "simple", timeOut = TIMEOUT)
    public void readLatency(boolean useNameLink) throws Exception {
        int numberOfOperations = 123;
        String cmdFormat = "-serviceEndpoint %s -masterKey %s" +
                " -databaseId %s -collectionId %s" +
                " -consistencyLevel Session -concurrency 2 -numberOfOperations %s" +
                " -operation ReadLatency -connectionMode Direct";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.getId(),
                                   collection.getId(),
                                   numberOfOperations)
                + (useNameLink ? " -useNameLink" : "");

        Configuration cfg = new Configuration();
        new JCommander(cfg, StringUtils.split(cmd));

        AtomicInteger success = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();

        AsyncReadBenchmark wf = new AsyncReadBenchmark(cfg) {
            @Override
            protected void onError(Throwable throwable) {
                error.incrementAndGet();
            }

            @Override
            protected void onSuccess() {
                success.incrementAndGet();
            }
        };

        wf.run();
        wf.shutdown();

        assertThat(error).hasValue(0);
        assertThat(success).hasValue(numberOfOperations);
    }

    @Test(dataProvider = "collectionLinkTypeArgProvider", groups = "simple", timeOut = TIMEOUT)
    public void readThroughput(boolean useNameLink) throws Exception {
        int numberOfOperations = 123;
        String cmdFormat = "-serviceEndpoint %s -masterKey %s" +
                " -databaseId %s -collectionId %s" +
                " -consistencyLevel Session -concurrency 2 -numberOfOperations %s" +
                " -operation ReadThroughput -connectionMode Direct";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.getId(),
                                   collection.getId(),
                                   numberOfOperations)
                + (useNameLink ? " -useNameLink" : "");

        Configuration cfg = new Configuration();
        new JCommander(cfg, StringUtils.split(cmd));

        AtomicInteger success = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();

        AsyncReadBenchmark wf = new AsyncReadBenchmark(cfg) {
            @Override
            protected void onError(Throwable throwable) {
                error.incrementAndGet();
            }

            @Override
            protected void onSuccess() {
                success.incrementAndGet();
            }
        };

        wf.run();
        wf.shutdown();

        assertThat(error).hasValue(0);
        assertThat(success).hasValue(numberOfOperations);
    }

    @BeforeClass(groups = "simple", timeOut = TIMEOUT)
    public void beforeClass() {
        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(10000);
        AsyncDocumentClient housekeepingClient = Utils.housekeepingClient();
        database = Utils.createDatabaseForTest(housekeepingClient);
        collection = housekeepingClient.createCollection("dbs/"+ database.getId(),
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
                {false},
        };
    }

    @AfterClass(groups = "simple", timeOut = TIMEOUT)
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
        partitionKeyDef.setPaths(paths);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        Collection<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.setPath("/*");
        Collection<Index> indexes = new ArrayList<>();
        Index stringIndex = Index.Range(DataType.String);
        stringIndex.set("precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.Number);
        numberIndex.set("precision", -1);
        indexes.add(numberIndex);
        includedPath.setIndexes(indexes);
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }
}
