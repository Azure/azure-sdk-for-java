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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkflowTest {
    private static final int TIMEOUT = 120_000;  // 2 minutes
    private Database database;
    private DocumentCollection collection;

    @Test(groups = "fast", timeOut = TIMEOUT)
    public void readMyWritesCLI() throws Exception {
        File configFile = createWorkloadConfigFile(
            TestConfigurations.HOST,
            TestConfigurations.MASTER_KEY,
            database.getId(),
            collection.getId(),
            "ReadMyWrites",
            "DIRECT",
            "SESSION",
            2,
            123,
            100);
        try {
            Main.main(new String[]{"-workloadConfig", configFile.getAbsolutePath()});
        } finally {
            configFile.delete();
        }
    }

    @Test(dataProvider = "collectionLinkTypeArgProvider", groups = "fast", timeOut = TIMEOUT)
    public void readMyWrites(boolean useNameLink) throws Exception {
        int numberOfOperations = 123;

        TenantWorkloadConfig cfg = new TenantWorkloadConfig();
        cfg.setServiceEndpoint(TestConfigurations.HOST);
        cfg.setMasterKey(TestConfigurations.MASTER_KEY);
        cfg.setDatabaseId(database.getId());
        cfg.setContainerId(collection.getId());
        cfg.setConsistencyLevel("SESSION");
        cfg.setConcurrency(2);
        cfg.setNumberOfOperations(numberOfOperations);
        cfg.setOperation("ReadMyWrites");
        cfg.setConnectionMode("DIRECT");
        cfg.setNumberOfPreCreatedDocuments(100);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();

        ReadMyWriteWorkflow wf = new ReadMyWriteWorkflow(cfg, Schedulers.parallel()) {
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

    @Test(groups = "fast", timeOut = TIMEOUT)
    public void writeThroughputCLI() throws Exception {
        File configFile = createWorkloadConfigFile(
            TestConfigurations.HOST,
            TestConfigurations.MASTER_KEY,
            database.getId(),
            collection.getId(),
            "WriteThroughput",
            "DIRECT",
            "SESSION",
            2,
            1000,
            0);
        try {
            Main.main(new String[]{"-workloadConfig", configFile.getAbsolutePath()});
        } finally {
            configFile.delete();
        }
    }

    @Test(dataProvider = "collectionLinkTypeArgProvider", groups = "fast", timeOut = TIMEOUT)
    public void writeThroughputWithDataProvider(boolean useNameLink) throws Exception {
        int numberOfOperations = 123;

        TenantWorkloadConfig cfg = new TenantWorkloadConfig();
        cfg.setServiceEndpoint(TestConfigurations.HOST);
        cfg.setMasterKey(TestConfigurations.MASTER_KEY);
        cfg.setDatabaseId(database.getId());
        cfg.setContainerId(collection.getId());
        cfg.setConsistencyLevel("SESSION");
        cfg.setConcurrency(2);
        cfg.setNumberOfOperations(numberOfOperations);
        cfg.setOperation("WriteThroughput");
        cfg.setConnectionMode("DIRECT");

        AtomicInteger success = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();

        AsyncWriteBenchmark wf = new AsyncWriteBenchmark(cfg, Schedulers.parallel()) {
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

    @Test(dataProvider = "collectionLinkTypeArgProvider", groups = "fast", timeOut = TIMEOUT)
    public void writeThroughput(boolean useNameLink) throws Exception {
        int numberOfOperations = 123;

        TenantWorkloadConfig cfg = new TenantWorkloadConfig();
        cfg.setServiceEndpoint(TestConfigurations.HOST);
        cfg.setMasterKey(TestConfigurations.MASTER_KEY);
        cfg.setDatabaseId(database.getId());
        cfg.setContainerId(collection.getId());
        cfg.setConsistencyLevel("SESSION");
        cfg.setConcurrency(2);
        cfg.setNumberOfOperations(numberOfOperations);
        cfg.setOperation("WriteThroughput");
        cfg.setConnectionMode("DIRECT");

        AtomicInteger success = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();

        AsyncWriteBenchmark wf = new AsyncWriteBenchmark(cfg, Schedulers.parallel()) {
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

    @Test(dataProvider = "collectionLinkTypeArgProvider", groups = "fast", timeOut = TIMEOUT)
    public void readThroughputWithDataProvider(boolean useNameLink) throws Exception {
        int numberOfOperations = 123;

        TenantWorkloadConfig cfg = new TenantWorkloadConfig();
        cfg.setServiceEndpoint(TestConfigurations.HOST);
        cfg.setMasterKey(TestConfigurations.MASTER_KEY);
        cfg.setDatabaseId(database.getId());
        cfg.setContainerId(collection.getId());
        cfg.setConsistencyLevel("SESSION");
        cfg.setConcurrency(2);
        cfg.setNumberOfOperations(numberOfOperations);
        cfg.setOperation("ReadThroughput");
        cfg.setConnectionMode("DIRECT");

        AtomicInteger success = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();

        AsyncReadBenchmark wf = new AsyncReadBenchmark(cfg, Schedulers.parallel()) {
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

    @Test(dataProvider = "collectionLinkTypeArgProvider", groups = "fast", timeOut = TIMEOUT)
    public void readThroughput(boolean useNameLink) throws Exception {
        int numberOfOperations = 123;

        TenantWorkloadConfig cfg = new TenantWorkloadConfig();
        cfg.setServiceEndpoint(TestConfigurations.HOST);
        cfg.setMasterKey(TestConfigurations.MASTER_KEY);
        cfg.setDatabaseId(database.getId());
        cfg.setContainerId(collection.getId());
        cfg.setConsistencyLevel("SESSION");
        cfg.setConcurrency(2);
        cfg.setNumberOfOperations(numberOfOperations);
        cfg.setOperation("ReadThroughput");
        cfg.setConnectionMode("DIRECT");

        AtomicInteger success = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();

        AsyncReadBenchmark wf = new AsyncReadBenchmark(cfg, Schedulers.parallel()) {
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

    @BeforeClass(groups = "fast", timeOut = TIMEOUT)
    public void before_WorkflowTest() {
        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(10000);
        AsyncDocumentClient housekeepingClient = Utils.housekeepingClient();
        database = Utils.createDatabaseForTest(housekeepingClient);
        // Retry collection creation on transient failures (408, 429, 503)
        int maxRetries = 3;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                collection = housekeepingClient.createCollection("dbs/" + database.getId(),
                    getCollectionDefinitionWithRangeRangeIndex(),
                    options).block().getResource();
                break;
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    throw e;
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
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

    @AfterClass(groups = "fast", timeOut = TIMEOUT)
    public void afterClass() {
        AsyncDocumentClient housekeepingClient = Utils.housekeepingClient();
        Utils.safeCleanDatabases(housekeepingClient);
        Utils.safeClean(housekeepingClient, database);
        Utils.safeClose(housekeepingClient);
    }

    private File createWorkloadConfigFile(
        String serviceEndpoint,
        String masterKey,
        String databaseId,
        String containerId,
        String operation,
        String connectionMode,
        String consistencyLevel,
        int concurrency,
        int numberOfOperations,
        int numberOfPreCreatedDocuments) throws Exception {

        String json = String.format(
            "{ \"tenants\": [{ "
                + "\"serviceEndpoint\": \"%s\", "
                + "\"masterKey\": \"%s\", "
                + "\"databaseId\": \"%s\", "
                + "\"containerId\": \"%s\", "
                + "\"operation\": \"%s\", "
                + "\"connectionMode\": \"%s\", "
                + "\"consistencyLevel\": \"%s\", "
                + "\"concurrency\": %d, "
                + "\"numberOfOperations\": %d, "
                + "\"numberOfPreCreatedDocuments\": %d "
                + "}] }",
            serviceEndpoint, masterKey, databaseId, containerId,
            operation, connectionMode, consistencyLevel,
            concurrency, numberOfOperations, numberOfPreCreatedDocuments);

        File tempFile = File.createTempFile("workload-config-", ".json");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(json);
        }
        return tempFile;
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
}
