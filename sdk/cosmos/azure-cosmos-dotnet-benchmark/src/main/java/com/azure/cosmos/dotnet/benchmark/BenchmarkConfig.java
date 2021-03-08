// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.util.Arrays;

public class BenchmarkConfig {

    public final static String USER_AGENT_SUFFIX = "cosmosdbdotnetbenchmark";

    @Parameter(names = "-w", description = "Type of Workload:\n"
        + "\tInsert - run a Insert workload that prints both throughput and latency\n"
        + "\tInsertWithoutExplicitPKAndId - same as Insert, but parsing PK and id from ObjectNode\n"
        , converter = Operation.OperationTypeConverter.class)
    private Operation operation = Operation.Insert;

    @Parameter(names = "-e", description = "Cosmos account end point")
    private String endpoint;

    @Parameter(names = "-k", description = "Cosmos account master key")
    private String key;

    @Parameter(names = "--database", description = "Database to use")
    private String database = "db";

    @Parameter(names = "--container", description = "Test container to use")
    private String container;

    @Parameter(names = "-t", description = "Provisioned throughput for test container")
    private int throughput = 100_000;

    @Parameter(names = "-n", description = "Number of items to process")
    private int itemCount = 200_000;

    @Parameter(names = "--consistencyLevel", description = "Consistency Level", converter = ConsistencyLevelConverter.class)
    private ConsistencyLevel consistencyLevel = ConsistencyLevel.SESSION;

    @Parameter(names = "--enableLatencyPercentiles", description = "Enable latency percentiles")
    private boolean enableLatencyPercentiles = false;

    @Parameter(names = "--cleanupOnStart", description = "Start with new collection")
    private boolean cleanupOnStart = false;

    @Parameter(names = "--cleanupOnFinish", description = "Clean-up after run")
    private boolean cleanupOnFinish = false;

    @Parameter(names = "--partitionKeyPath", description = "Container partition key path")
    private String partitionKeyPath = "/partitionKey";

    @Parameter(names = "-pl", description = "Degree of parallism")
    private int degreeOfParallelism = -1;

    @Parameter(names = "--itemTemplateFile", description = "Item template")
    private String itemTemplateFile = "Player.json";

    @Parameter(names = "--minThreadPoolSize", description = "Min thread pool size - irrelevant in Java")
    private int minThreadPoolSize = 100;

    @Parameter(names = "--traceFailures", description = "Write the task execution failure to console. Useful for debugging failures")
    private boolean traceFailures = false;

    @Parameter(names = "--publishResults", description = "Publish run results")
    private boolean publishResults = false;

    @Parameter(names = "--runId", description = "Run ID, only for publish")
    private String runId;

    @Parameter(names = "--commitId", description = "Commit ID, only for publish")
    private String commitId;

    @Parameter(names = "--commitDate", description = "Commit date, only for publish")
    private String commitDate;

    @Parameter(names = "--commitTime", description = "Commit time, only for publish")
    private String commitTime;

    @Parameter(names = "--branchName", description = "Branch name, only for publish")
    private String branchName;

    @Parameter(names = "--resultsPartitionKeyValue", description = "Partitionkey, only for publish")
    private String resultsPartitionKeyValue;

    @Parameter(names = "--disableCoreSdkLogging", description = "Disable core SDK logging - irrelevant in Java")
    private boolean disableCoreSdkLogging = false;

    @Parameter(names = "--resultsContainer", description = "Container to publish results to")
    private String resultsContainer = "runsummary";

    @Parameter(names = {"-h", "-help", "--help", "/?", "-?", "--?"}, description = "Help", help = true)
    private boolean help = false;

    public Operation getOperation() {
        return this.operation;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public String getKey() {
        return this.key;
    }

    public void resetKey() {
        this.key = null;
    }

    public String getDatabase() {
        return this.database;
    }

    public String getContainer() {
        return this.container;
    }

    public int getThroughput() {
        return this.throughput;
    }

    public int getItemCount() {
        return this.itemCount;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    public boolean isEnableLatencyPercentiles() {
        return enableLatencyPercentiles;
    }

    public boolean isCleanupOnStart() {
        return cleanupOnStart;
    }

    public boolean isCleanupOnFinish() {
        return cleanupOnFinish;
    }

    public String getPartitionKeyPath() {
        return partitionKeyPath;
    }

    public int getDegreeOfParallelism() {
        return this.degreeOfParallelism;
    }

    public String getItemTemplateFile() {
        return this.itemTemplateFile;
    }

    public int getMinThreadPoolSize() {
        return minThreadPoolSize;
    }

    public boolean isTraceFailures() {
        return traceFailures;
    }

    public boolean isPublishResults() {
        return publishResults;
    }

    public String getRunId() {
        return runId;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getCommitDate() {
        return this.commitDate;
    }

    public String getCommitTime() {
        return commitTime;
    }

    public String getBranchName() {
        return this.branchName;
    }

    public String getResultsPartitionKeyValue() {
        return this.resultsPartitionKeyValue;
    }

    public String getResultsContainer() {
        return this.resultsContainer;
    }

    public boolean isDisableCoreSdkLogging() {
        return disableCoreSdkLogging;
    }

    public int getTaskCount(int containerThroughput) {
        if (this.degreeOfParallelism > 0) {
            return this.degreeOfParallelism;
        }

        // set TaskCount = 10 for each 10k RUs, minimum 1, maximum { #processor * 50 }
        return Math.min(
            Math.max(containerThroughput / 1000, 1),
            Runtime.getRuntime().availableProcessors() * 50);
    }

    public void print() {
        Utility.traceInformation("BenchmarkConfig arguments");
        Utility.traceInformation("--------------------------------------------------------------------- ");
        Utility.traceInformation(JsonHelper.toJsonString(this));
        Utility.traceInformation("--------------------------------------------------------------------- ");
        Utility.traceInformation("");
    }

    public void validate() {
        if (!this.isPublishResults()) {
            return;
        }

        if (StringUtils.isEmpty(this.getResultsContainer()) ||
            StringUtils.isEmpty(this.getResultsPartitionKeyValue()) ||
            StringUtils.isEmpty(this.getCommitId()) ||
            StringUtils.isEmpty(this.getCommitDate()) ||
            StringUtils.isEmpty(this.getCommitTime())) {
            throw new IllegalArgumentException(
                "Missing either ResultsContainer, ResultsPartitionKeyValue, CommitId, CommitDate or CommitTime.");
        }
    }

    public boolean isHelp() {
        return help;
    }

    public enum Operation {
        Insert,
        InsertWithoutExplicitPKAndId;

        static Operation fromString(String code) {

            for (Operation output : Operation.values()) {
                if (output.toString().equalsIgnoreCase(code)) {
                    return output;
                }
            }

            return null;
        }

        static class OperationTypeConverter implements IStringConverter<Operation> {

            /*
             * (non-Javadoc)
             *
             * @see com.beust.jcommander.IStringConverter#convert(java.lang.STRING)
             */
            @Override
            public Operation convert(String value) {
                Operation ret = fromString(value);
                if (ret == null) {
                    throw new ParameterException("Value " + value + " can not be converted to an Operation. "
                        + "Available values are: " + Arrays.toString(Operation.values()));
                }
                return ret;
            }
        }
    }

    private static ConsistencyLevel fromString(String code) {
        for (ConsistencyLevel output : ConsistencyLevel.values()) {
            if (output.toString().equalsIgnoreCase(code)) {
                return output;
            }
        }
        return null;
    }

    static class ConsistencyLevelConverter implements IStringConverter<ConsistencyLevel> {

        /*
         * (non-Javadoc)
         *
         * @see com.beust.jcommander.IStringConverter#convert(java.lang.STRING)
         */
        @Override
        public ConsistencyLevel convert(String value) {
            ConsistencyLevel ret = fromString(value);
            if (ret == null) {
                throw new ParameterException("Value " + value + " can not be converted to ClientType. "
                    + "Available values are: " + Arrays.toString(Operation.values()));
            }
            return ret;
        }
    }
}
