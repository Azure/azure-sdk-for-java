// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.dotnet.benchmark.operations.InsertBenchmarkOperation;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Supplier;

public class Main {

    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        CosmosAsyncClient cosmosClient = null;

        try {
            LOGGER.debug("Parsing the arguments ...");
            BenchmarkConfig cfg = new BenchmarkConfig();

            JCommander jcommander = new JCommander(cfg, args);
            if (cfg.isHelp()) {
                // prints out the usage help
                jcommander.usage();
                return;
            }

            cfg.validate();

            if (cfg.isEnableLatencyPercentiles()) {
                TelemetrySpan.setIncludePercentiles(true);
                TelemetrySpan.resetLatencyHistogram(cfg.getItemCount());
            }

            String accountKey = cfg.getKey();
            cfg.resetKey();
            cfg.print();

            cosmosClient = new CosmosClientBuilder()
                .endpoint(cfg.getEndpoint())
                .key(accountKey)
                .directMode()
                .userAgentSuffix(BenchmarkConfig.USER_AGENT_SUFFIX)
                .contentResponseOnWriteEnabled(true)
                .consistencyLevel(cfg.getConsistencyLevel())
                .throttlingRetryOptions(new ThrottlingRetryOptions().setMaxRetryAttemptsOnThrottledRequests(0))
                .buildAsyncClient();

            PartitionKeyDefinition partitionKeyDef = Objects.requireNonNull(cosmosClient
                .getDatabase(cfg.getDatabase())
                .getContainer(cfg.getContainer())
                .read()
                .block())
                                                            .getProperties()
                                                            .getPartitionKeyDefinition();

            String partitionKeyPath = partitionKeyDef.getPaths().get(0);

            Integer currentContainerThroughput = Objects.requireNonNull(cosmosClient
                .getDatabase(cfg.getDatabase())
                .getContainer(cfg.getContainer())
                .readThroughput()
                .block())
                                                        .getProperties()
                                                        .getManualThroughput();

            Utility.traceInformation(
                String.format(
                    "Using container %s with %d RU/s",
                    cfg.getContainer(),
                    currentContainerThroughput));

            int taskCount = cfg.getTaskCount(currentContainerThroughput);

            Utility.traceInformation(
                String.format(
                    "Starting Inserts with %d tasks",
                    taskCount));

            Utility.traceInformation("");

            int opsPerTask = cfg.getItemCount() / taskCount;
            Supplier<IBenchmarkOperation> benchmarkFactory =
                getBenchmarkFactory(cfg, partitionKeyPath, cosmosClient);

            IExecutionStrategy execution = IExecutionStrategy.startNew(cfg, benchmarkFactory);
            RunSummary runSummary = execution.execute(
                taskCount,
                opsPerTask,
                cfg.isTraceFailures(),
                0.01d);

            Instant now = Instant.now();
            String dateString =
                DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC).format(now);
            String timeString =
                DateTimeFormatter.ofPattern("HH-mm").withZone(ZoneOffset.UTC).format(now);

            runSummary.setWorkloadType(cfg.getOperation().toString());
            runSummary.setId(
                String.format(
                    "%s:%s--%s",
                    dateString,
                    timeString,
                    cfg.getCommitId()));
            runSummary.setCommit(cfg.getCommitId());
            runSummary.setCommitDate(cfg.getCommitDate());
            runSummary.setCommitTime(cfg.getCommitTime());
            runSummary.setDate(dateString);
            runSummary.setTime(timeString);
            runSummary.setBranchName(cfg.getBranchName());
            runSummary.setTotalOps(cfg.getItemCount());
            runSummary.setConcurrency(taskCount);
            runSummary.setDatabase(cfg.getDatabase());
            runSummary.setContainer(cfg.getContainer());
            runSummary.setAccountName(cfg.getEndpoint());
            runSummary.setPk(cfg.getResultsPartitionKeyValue());
            runSummary.setConsistencyLevel(cfg.getConsistencyLevel().toString());
            runSummary.setOs(System.getProperty("os.name"));
            runSummary.setOsVersion(System.getProperty("os.version"));
            runSummary.setMachineName(InetAddress.getLocalHost().getHostName());
            runSummary.setCores(Runtime.getRuntime().availableProcessors());
            runSummary.setRuntimeVersion(System.getProperty("java.version"));

            if (cfg.isPublishResults()) {
                CosmosAsyncContainer resultsContainer = cosmosClient
                    .getDatabase(cfg.getDatabase())
                    .getContainer(cfg.getResultsContainer());

                CosmosItemResponse<RunSummary> resultsResponse = resultsContainer
                    .createItem(
                        runSummary,
                        new PartitionKey(runSummary.getPk()),
                        null)
                    .block();

                assert resultsResponse != null;
                Utility.traceInformation(
                    "Uploaded results successfully:" + JsonHelper.toJsonString(resultsResponse.getItem()));
            }
        } catch (ParameterException e) {
            // if any error in parsing the cmd-line options print out the usage help
            System.err.println("INVALID Usage: " + e.getMessage());
            System.err.println("Try '-help' for more information.");
            throw e;
        } finally {
            if (cosmosClient != null) {
                cosmosClient.close();
            }
        }
    }

    private static Supplier<IBenchmarkOperation> getBenchmarkFactory(
        BenchmarkConfig cfg,
        String partitionKeyPath,
        CosmosAsyncClient cosmosClient) throws IOException {

        String sampleItem = new String(Files.readAllBytes(Paths.get(cfg.getItemTemplateFile())));

        switch (cfg.getOperation())
        {
            case InsertWithoutExplicitPKAndId:
                return () -> new InsertBenchmarkOperation(
                    cosmosClient,
                    cfg.getDatabase(),
                    cfg.getContainer(),
                    partitionKeyPath,
                    sampleItem,
                    false);
            default:
                return () -> new InsertBenchmarkOperation(
                    cosmosClient,
                    cfg.getDatabase(),
                    cfg.getContainer(),
                    partitionKeyPath,
                    sampleItem,
                    true);
        }


    }
}
