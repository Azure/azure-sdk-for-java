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
    private static final int TIMEOUT = 120_000;  // 2 minutes
    private Database database;
    private DocumentCollection collection;

    @Test(groups = "simple", timeOut = TIMEOUT)
    public void readMyWritesCLI() throws Exception {
        String cmdFormat = "-serviceEndpoint %s -masterKey %s" +
                " -databaseId %s -collectionId %s" +
                " -consistencyLevel SESSION -concurrency 2 -numberOfOperations 123" +
                " -operation ReadMyWrites -connectionMode DIRECT -numberOfPreCreatedDocuments 100";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.id(),
                                   collection.id());
        Main.main(StringUtils.split(cmd));
    }

    @Test(dataProvider = "collectionLinkTypeArgProvider", groups = "simple", timeOut = TIMEOUT)
    public void readMyWrites(boolean useNameLink) throws Exception {
        int numberOfOperations = 123;
        String cmdFormat = "-serviceEndpoint %s -masterKey %s" +
                " -databaseId %s -collectionId %s" +
                " -consistencyLevel SESSION -concurrency 2 -numberOfOperations %s" +
                " -operation ReadMyWrites -connectionMode DIRECT -numberOfPreCreatedDocuments 100";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.id(),
                                   collection.id(),
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
                " -consistencyLevel SESSION -concurrency 2 -numberOfOperations 1000" +
                " -operation WriteLatency -connectionMode DIRECT";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.id(),
                                   collection.id());
        Main.main(StringUtils.split(cmd));
    }

    @Test(dataProvider = "collectionLinkTypeArgProvider", groups = "simple", timeOut = TIMEOUT)
    public void writeLatency(boolean useNameLink) throws Exception {
        int numberOfOperations = 123;
        String cmdFormat = "-serviceEndpoint %s -masterKey %s" +
                " -databaseId %s -collectionId %s" +
                " -consistencyLevel SESSION -concurrency 2 -numberOfOperations %s" +
                " -operation WriteLatency -connectionMode DIRECT";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.id(),
                                   collection.id(),
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
                " -consistencyLevel SESSION -concurrency 2 -numberOfOperations %s" +
                " -operation WriteThroughput -connectionMode DIRECT";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.id(),
                                   collection.id(),
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
                " -consistencyLevel SESSION -concurrency 2 -numberOfOperations %s" +
                " -operation ReadLatency -connectionMode DIRECT";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.id(),
                                   collection.id(),
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
                " -consistencyLevel SESSION -concurrency 2 -numberOfOperations %s" +
                " -operation ReadThroughput -connectionMode DIRECT";

        String cmd = String.format(cmdFormat,
                                   TestConfigurations.HOST,
                                   TestConfigurations.MASTER_KEY,
                                   database.id(),
                                   collection.id(),
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
        collection = housekeepingClient.createCollection("dbs/"+ database.id(),
                                                         getCollectionDefinitionWithRangeRangeIndex(),
                                                         options)
                .single().block().getResource();
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
}