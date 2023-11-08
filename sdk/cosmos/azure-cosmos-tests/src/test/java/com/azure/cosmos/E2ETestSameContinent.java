// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosDaemonThreadFactory;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.fail;

public class E2ETestSameContinent {

    private final static ImplementationBridgeHelpers.CosmosSessionRetryOptionsHelper.CosmosSessionRetryOptionsAccessor
        sessionRetryOptionsAccessor = ImplementationBridgeHelpers
        .CosmosSessionRetryOptionsHelper
        .getCosmosSessionRetryOptionsAccessor();

    // *** START *** SETTINGS TO PLAY AROUND WITH

    // Indicates total runtime duration - each loop iteration will wait for 10 seconds

    // How many concurrent threads should keep writing to the secondary region - this will influence how
    // quickly the secondary region's session token makes progress - the higher the number of concurrent
    // writers the more likely that the thread writing into the primary region will run into 404/1002
    private static final int SECONDARY_REGION_WRITER_COUNT = 20;

    // minimum retry time for 404/1002 in reach region

    private static boolean shouldCleanUp = false;

    // *** END *** SETTINGS TO PLAY AROUND WITH

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(
        JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(),
        true
    );

    private static final String dbName = "E2ETestDB";
    private static final String containerName = "Data";
    private static Logger logger = LoggerFactory.getLogger(E2ETest.class.getSimpleName());
    private static ScheduledThreadPoolExecutor executorServiceWritesAgainstSecondRegion;
    private static ScheduledThreadPoolExecutor executorServiceWritesAgainstPrimaryRegion;
    private static final ScheduledFuture<?>[] scheduledFuturesForWritesAgainstSecondRegion =
        new ScheduledFuture[SECONDARY_REGION_WRITER_COUNT];
    private static ScheduledFuture<?> scheduledFutureForWritesAgainstPrimaryRegion;

    private static AtomicBoolean shouldClose = new AtomicBoolean(false);

    private static CosmosAsyncContainer container;
    private static CosmosAsyncClient client;

    public static void main(String[] args) {

        TestConfigProvider testConfigProvider = new TestConfigProvider();
        List<CosmosAsyncClient> clientsCreated = new ArrayList<>();

        String runId = UUID.randomUUID().toString();

        testConfigProvider.addConfig(
            new SessionRetryOptionsBuilder()
                .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
                .maxRetriesPerRegion(2)
                .minTimeoutPerRegion(Duration.ofSeconds(10))
                .build(),
            10,
            10);

        testConfigProvider.addConfig(
            new SessionRetryOptionsBuilder()
                .regionSwitchHint(CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED)
                .maxRetriesPerRegion(2)
                .minTimeoutPerRegion(Duration.ofSeconds(10))
                .build(),
            10,
            10);

        testConfigProvider.addConfig(
            new SessionRetryOptionsBuilder()
                .regionSwitchHint(CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED)
                .maxRetriesPerRegion(2)
                .minTimeoutPerRegion(Duration.ofSeconds(10))
                .build(),
            15,
            10);

        testConfigProvider.addConfig(
            new SessionRetryOptionsBuilder()
                .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
                .maxRetriesPerRegion(2)
                .minTimeoutPerRegion(Duration.ofSeconds(10))
                .build(),
            15,
            10);

        System.setProperty(Configs.IS_SESSION_CONSISTENCY_FOR_WRITE_ENABLED, "false");

        testConfigProvider.addConfig(
            new SessionRetryOptionsBuilder()
                .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
                .maxRetriesPerRegion(2)
                .minTimeoutPerRegion(Duration.ofSeconds(10))
                .build(),
            15,
        10,
        "scwd");

        testConfigProvider.addConfig(
            new SessionRetryOptionsBuilder()
                .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
                .maxRetriesPerRegion(2)
                .minTimeoutPerRegion(Duration.ofSeconds(10))
                .build(),
            15,
            10,
            "scwd-pkscoped-st");

        for (TestConfig testConfig : testConfigProvider.getTestConfigs()) {

            if (!clientsCreated.isEmpty()) {
//                clientsCreated.forEach(CosmosAsyncClient::close);
                clientsCreated.clear();
            }

            try {
                Thread.sleep(30_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            logger.info("Running test with config : {}", testConfig.toString());

            try {
                initializeServiceResources(testConfig, runId, clientsCreated);
                shouldClose.set(false);

                StringBuilder sb = new StringBuilder();
                for (String arg : args) {
                    if (sb.length() > 0) {
                        sb.append(" ");
                    }
                    sb.append(arg);
                }

                executorServiceWritesAgainstSecondRegion = new ScheduledThreadPoolExecutor(
                    20,
                    new CosmosDaemonThreadFactory("WriteAgainstSecondRegion"));
                executorServiceWritesAgainstPrimaryRegion = new ScheduledThreadPoolExecutor(
                    1,
                    new CosmosDaemonThreadFactory("WriteAgainstPrimaryRegion"));

                executorServiceWritesAgainstSecondRegion.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
                executorServiceWritesAgainstSecondRegion.setRemoveOnCancelPolicy(true);

                String secondarySource = "onWriteToSecondaryRegion";
                CosmosAsyncClient clientForWritesToSecondaryRegion = createClient(true, secondarySource, runId, testConfig, clientsCreated);

                for (int i = 0; i < testConfig.secondaryRegionWriterClientCount; i++) {
                    int finalI = i;
                    scheduledFuturesForWritesAgainstSecondRegion[i] = executorServiceWritesAgainstSecondRegion.schedule(
                        () -> onWriteToSecondaryRegion(finalI, clientForWritesToSecondaryRegion, secondarySource),
                        10,
                        TimeUnit.MILLISECONDS);
                }

                // q: what does delay mean here?
                //      - the time task will wait before starting execution
                scheduledFutureForWritesAgainstPrimaryRegion = executorServiceWritesAgainstPrimaryRegion.schedule(
                    () -> onWriteToPrimaryRegion(testConfig, runId, clientsCreated),
                    1000,
                    TimeUnit.MILLISECONDS);

                logger.info("Args: {}", args);

                for (int i = 0; i < testConfig.loops; i++) {
                    logger.info("LOOP {}", i);

                    if (i >= testConfig.loops - 1) {
                        logger.info("Shutting down.");
                        shouldClose.set(true);
                    }

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                for (ScheduledFuture<?> scheduledFutureForWritesAgainstSecondRegion : scheduledFuturesForWritesAgainstSecondRegion) {
                    if (scheduledFutureForWritesAgainstSecondRegion != null &&
                        !scheduledFutureForWritesAgainstSecondRegion.isDone()) {

                        scheduledFutureForWritesAgainstSecondRegion.cancel(true);
                    }
                }

                if (!scheduledFutureForWritesAgainstPrimaryRegion.isDone()) {
                    scheduledFutureForWritesAgainstPrimaryRegion.cancel(true);
                }
            }
            finally {

            }

            try {
                Thread.sleep(30_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                cleanupServiceResources(clientsCreated);
            }
        }

        logger.info("Good bye...");
    }

    private static void initializeServiceResources(TestConfig testConfig, String runId,  List<CosmosAsyncClient> clientsCreated) {

        client = createClient(true, "initializeServiceResources", runId, testConfig, clientsCreated);

        client.createDatabaseIfNotExists(dbName).block();
        client
            .getDatabase(dbName)
            .createContainerIfNotExists(
                containerName,
                "/id",
                ThroughputProperties.createAutoscaledThroughput(10_000)
            ).block();

        container = client
            .getDatabase(dbName)
            .getContainer(containerName);

        if (shouldCleanUp) {
            logger.info("Waiting for container {}/{} to be created across all regions...", dbName, containerName);
            try {
                Thread.sleep(20_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void cleanupServiceResources(List<CosmosAsyncClient> clientsCreated) {

        if (!shouldCleanUp) {
            logger.info("Cleanup suppressed!");
            return;
        }

        if (client == null || container == null) {
            logger.info("Nothing to cleanup - Client not initialized yet.");
            return;
        }

        try {
            container.getDatabase().delete().block();
            logger.info("Database {} deleted ...", container.getDatabase().getId());

        } catch (NotFoundException ignoredNotFoundError) {
            logger.info("Container {}/{} des not exist (yet)...", container.getDatabase().getId(), container.getId());
        }
    }

    private static void onWriteToSecondaryRegion(
        int i,
        CosmosAsyncClient clientForWritesToSecondaryRegion,
        String source) {
        logger.info("WRITE TO SECONDARY REGION [{}]STARTED...", i);

        writeLoop(
            clientForWritesToSecondaryRegion.getDatabase(dbName).getContainer(containerName),
            Arrays.asList("East US", "South Central US"),
            source,
            null);
    }

    private static CosmosAsyncClient createClient(
        boolean withDiagnostics,
        String clientId,
        String runId,
        TestConfig testConfig,
        List<CosmosAsyncClient> clientsCreated) {

        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .preferredRegions(Arrays.asList("West US", "South Central US", "East US"))
            .directMode()
            .sessionRetryOptions(testConfig.sessionRetryOptions);

        if (withDiagnostics) {
            CosmosDiagnosticsHandler diagnosticsHandler = (diagnosticsContext, traceContext) -> {
                boolean hasReadSessionNotAvailable =  diagnosticsContext
                    .getDistinctCombinedClientSideRequestStatistics()
                    .stream()
                    .anyMatch(
                        clientStats -> clientStats.getResponseStatisticsList().stream().anyMatch(
                            responseStats -> responseStats.getStoreResult().getStoreResponseDiagnostics().getStatusCode() == 404 &&
                                responseStats.getStoreResult().getStoreResponseDiagnostics().getSubStatusCode() == 1002
                        )
                    );

                if (diagnosticsContext.isFailure()
                    || diagnosticsContext.isThresholdViolated()
                    || diagnosticsContext.getContactedRegionNames().size() > 1
                    || hasReadSessionNotAvailable) {

                    logger.info(
                        "{} IsFailure: {}, IsThresholdViolated: {}, ContactedRegions: {}, hasReadSessionNotAvailable: {}  CTX: {}",
                        clientId,
                        diagnosticsContext.isFailure(),
                        diagnosticsContext.isThresholdViolated(),
                        String.join(", ", diagnosticsContext.getContactedRegionNames()),
                        hasReadSessionNotAvailable,
                        diagnosticsContext.toJson());
                }
            };

            int maxInRegionRetryCount = sessionRetryOptionsAccessor.getMaxInRegionRetryCount(testConfig.sessionRetryOptions);
            Duration minInRegionRetryTime = sessionRetryOptionsAccessor.getMinInRegionRetryTime(testConfig.sessionRetryOptions);
            CosmosRegionSwitchHint regionSwitchHint = sessionRetryOptionsAccessor.getRegionSwitchHint(testConfig.sessionRetryOptions);

            String clientCorrelationId = "runId-"
                + runId
                + "-mirc-"
                + maxInRegionRetryCount
                + "-mrrt-"
                + minInRegionRetryTime
                + "-rsh-"
                + (regionSwitchHint == CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED ? "lrp" : "rrp")
                + "-cc-"
                + testConfig.secondaryRegionWriterClientCount
                + "-lc-"
                + testConfig.loops
                + "-"
                + ((testConfig.clientCorrelationIdSubstring.isEmpty()) ? "" : ("sub-" + testConfig.clientCorrelationIdSubstring + "-"))
                + clientId;

            CosmosDiagnosticsThresholds thresholds = new CosmosDiagnosticsThresholds()
                .setNonPointOperationLatencyThreshold(Duration.ofMinutes(60))
                .setPointOperationLatencyThreshold(Duration.ofMillis(90));

            CosmosClientTelemetryConfig diagnosticsConfig = new CosmosClientTelemetryConfig()
                .clientCorrelationId(clientCorrelationId)
                .diagnosticsThresholds(thresholds)
                .diagnosticsHandler(diagnosticsHandler);

            clientBuilder
                .clientTelemetryConfig(diagnosticsConfig);
        }

        CosmosAsyncClient cosmosAsyncClient = clientBuilder.buildAsyncClient();

        clientsCreated.add(cosmosAsyncClient);

        return cosmosAsyncClient;
    }

    private static void onWriteToPrimaryRegion(TestConfig testConfig, String runId, List<CosmosAsyncClient> clientsCreated) {
        logger.info("WRITE TO PRIMARY REGION STARTED...");
        logger.info("WRITE single operation to Secondary region...");

        String source = "onWriteToPrimaryRegion";
        CosmosAsyncClient clientForWritesToPrimaryRegion = createClient(true, source, runId, testConfig, clientsCreated);

        int iterations = 1;
        while (!shouldClose.get()) {
            logger.info("{}: iteration {}", source, iterations);
            logger.info("First 100 writes to first region...");
            // Initially
            writeLoop(
                clientForWritesToPrimaryRegion.getDatabase(dbName).getContainer(containerName),
                Arrays.asList("West US", "East US"),
                source,
                1000);

            logger.info("Simulating fail-over - 100 writes in second region...");
            // Initially
            writeLoop(
                clientForWritesToPrimaryRegion.getDatabase(dbName).getContainer(containerName),
                Arrays.asList("South Central US"),
                source,
                1000);

            logger.info("Failing back...");
            logger.info("Writing to first region now...");
            writeLoop(
                clientForWritesToPrimaryRegion.getDatabase(dbName).getContainer(containerName),
                Arrays.asList("West US", "East US"),
                source,
                1000);

            iterations += 1;
        }
    }

    private static void writeLoop(CosmosAsyncContainer container, List<String> excludedRegions, String callbackName, Integer maxIterations) {
        int iterations = 0;
        while (!shouldClose.get() && (maxIterations == null || iterations < maxIterations)) {
            try {
                write(container, excludedRegions);
                iterations += 1;
            } catch (Throwable t) {
                logger.error("Callback invocation '" + callbackName + "' failed.", t);
            }
        }
    }

    private static void write(CosmosAsyncContainer container, List<String> excludedRegions) {
        String id = UUID.randomUUID().toString();
        CosmosItemRequestOptions options = new CosmosItemRequestOptions().setExcludedRegions(excludedRegions);
        try {
            logger.debug("--> write {}", id);
            container.createItem(
                getDocumentDefinition(id),
                new PartitionKey(id),
                options).block();
            logger.debug("<-- write {}", id);
        } catch (CosmosException error) {
            logger.info("COSMOS EXCEPTION - CTX: {}", error.getDiagnostics().getDiagnosticsContext().toJson(), error);
            throw error;
        }
    }

    private static ObjectNode getDocumentDefinition(String documentId) {
        String json = String.format(
            "{ \"id\": \"%s\", \"mypk\": \"%s\" }",
            documentId,
            documentId);

        try {
            return
                OBJECT_MAPPER.readValue(json, ObjectNode.class);
        } catch (JsonProcessingException jsonError) {
            fail("No json processing error expected", jsonError);

            throw new IllegalStateException("No json processing error expected", jsonError);
        }
    }

    private static class TestConfigProvider {
        private List<TestConfig> testConfigs = new ArrayList<>();

        public void addConfig(
            SessionRetryOptions sessionRetryOptions,
            int secondaryRegionWriterClientCount,
            int loops) {
            this.addConfig(sessionRetryOptions, secondaryRegionWriterClientCount, loops, "");
        }

        public void addConfig(
            SessionRetryOptions sessionRetryOptions,
            int secondaryRegionWriterClientCount,
            int loops,
            String clientCorrelationIdSubstring) {
            TestConfig testConfig = new TestConfig();

            testConfig.sessionRetryOptions = sessionRetryOptions;
            testConfig.secondaryRegionWriterClientCount = secondaryRegionWriterClientCount;
            testConfig.loops = loops;
            testConfig.clientCorrelationIdSubstring = clientCorrelationIdSubstring;

            testConfigs.add(testConfig);
        }

        public List<TestConfig> getTestConfigs() {
            return testConfigs;
        }
    }

    private static class TestConfig {
        private SessionRetryOptions sessionRetryOptions;
        private int secondaryRegionWriterClientCount;
        private int loops;
        private String clientCorrelationIdSubstring;
        private boolean partitionKeyScopedSessionTokenCapturingEnabled;
        private boolean sessionConsistencyDisabledForWrites;

        @Override
        public String toString() {
            return "TestConfig{" +
                "sessionRetryOptions=" + sessionRetryOptions.toString() +
                ", secondaryRegionWriterClientCount=" + secondaryRegionWriterClientCount +
                ", loops=" + loops +
                '}';
        }
    }
}