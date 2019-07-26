// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.benchmark;

import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.benchmark.Configuration.Operation.OperationTypeConverter;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.Duration;
import java.util.Arrays;

class Configuration {
    private final static int GRAPHITE_SERVER_DEFAULT_PORT = 2003;

    @Parameter(names = "-serviceEndpoint", description = "Service Endpoint")
    private String serviceEndpoint;

    @Parameter(names = "-masterKey", description = "Master Key")
    private String masterKey;

    @Parameter(names = "-databaseId", description = "Database ID")
    private String databaseId;

    @Parameter(names = "-collectionId", description = "Collection ID")
    private String collectionId;

    @Parameter(names = "-useNameLink", description = "Use name Link")
    private boolean useNameLink = false;

    @Parameter(names = "-documentDataFieldSize", description = "Length of a document data field in characters (16-bit)")
    private int documentDataFieldSize = 20;

    @Parameter(names = "-maxConnectionPoolSize", description = "Max Connection Pool Size")
    private Integer maxConnectionPoolSize = 1000;

    @Parameter(names = "-consistencyLevel", description = "Consistency Level", converter = ConsistencyLevelConverter.class)
    private ConsistencyLevel consistencyLevel = ConsistencyLevel.SESSION;

    @Parameter(names = "-connectionMode", description = "Connection Mode")
    private ConnectionMode connectionMode = ConnectionMode.DIRECT;

    @Parameter(names = "-graphiteEndpoint", description = "Graphite endpoint")
    private String graphiteEndpoint;

    @Parameter(names = "-enableJvmStats", description = "Enables JVM Stats")
    private boolean enableJvmStats;

    @Parameter(names = "-operation", description = "Type of Workload:\n"
            + "\tReadThroughput- run a READ workload that prints only throughput *\n"
            + "\tWriteThroughput - run a Write workload that prints only throughput\n"
            + "\tReadLatency - run a READ workload that prints both throughput and latency *\n"
            + "\tWriteLatency - run a Write workload that prints both throughput and latency\n"
            + "\tQueryCross - run a 'Select * from c where c._rid = SOME_RID' workload that prints throughput\n"
            + "\tQuerySingle - run a 'Select * from c where c.pk = SOME_PK' workload that prints throughput\n"
            + "\tQuerySingleMany - run a 'Select * from c where c.pk = \"pk\"' workload that prints throughput\n"
            + "\tQueryParallel - run a 'Select * from c' workload that prints throughput\n"
            + "\tQueryOrderby - run a 'Select * from c order by c._ts' workload that prints throughput\n"
            + "\tQueryAggregate - run a 'Select value max(c._ts) from c' workload that prints throughput\n"
            + "\tQueryAggregateTopOrderby - run a 'Select top 1 value count(c) from c order by c._ts' workload that prints throughput\n"
            + "\tQueryTopOrderby - run a 'Select top 1000 * from c order by c._ts' workload that prints throughput\n"
            + "\tMixed - runa workload of 90 reads, 9 writes and 1 QueryTopOrderby per 100 operations *\n"
            + "\tReadMyWrites - run a workflow of writes followed by reads and queries attempting to read the write.*\n"
            + "\n\t* writes 10k documents initially, which are used in the reads", converter = OperationTypeConverter.class)
    private Operation operation = Operation.WriteThroughput;

    @Parameter(names = "-concurrency", description = "Degree of Concurrency in Inserting Documents."
            + " If this value is not specified, the max connection pool size will be used as the concurrency level.")
    private Integer concurrency;

    @Parameter(names = "-numberOfOperations", description = "Total NUMBER Of Documents To Insert")
    private int numberOfOperations = 100000;

    static class DurationConverter implements IStringConverter<Duration> {
        @Override
        public Duration convert(String value) {
            if (value == null) {
                return null;
            }

            return Duration.parse(value);
        }
    }

    @Parameter(names = "-maxRunningTimeDuration", description = "Max Running Time Duration", converter = DurationConverter.class)
    private Duration maxRunningTimeDuration;

    @Parameter(names = "-printingInterval", description = "Interval of time after which Metrics should be printed (seconds)")
    private int printingInterval = 10;

    @Parameter(names = "-numberOfPreCreatedDocuments", description = "Total NUMBER Of Documents To pre create for a read workload to use")
    private int numberOfPreCreatedDocuments = 1000;

    @Parameter(names = {"-h", "-help", "--help"}, description = "Help", help = true)
    private boolean help = false;

    enum Operation {
        ReadThroughput,
        WriteThroughput,
        ReadLatency,
        WriteLatency,
        QueryCross,
        QuerySingle,
        QuerySingleMany,
        QueryParallel,
        QueryOrderby,
        QueryAggregate,
        QueryAggregateTopOrderby,
        QueryTopOrderby,
        Mixed,
        ReadMyWrites;

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
                    throw new ParameterException("Value " + value + " can not be converted to ClientType. "
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

    Duration getMaxRunningTimeDuration() {
        return maxRunningTimeDuration;
    }

    Operation getOperationType() {
        return operation;
    }

    int getNumberOfOperations() {
        return numberOfOperations;
    }

    String getServiceEndpoint() {
        return serviceEndpoint;
    }

    String getMasterKey() {
        return masterKey;
    }

    boolean isHelp() {
        return help;
    }

    int getDocumentDataFieldSize() {
        return documentDataFieldSize;
    }

    ConnectionPolicy getConnectionPolicy() {
        ConnectionPolicy policy = new ConnectionPolicy();
        policy.connectionMode(connectionMode);
        policy.maxPoolSize(maxConnectionPoolSize);
        return policy;
    }

    ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    String getDatabaseId() {
        return databaseId;
    }

    String getCollectionId() {
        return collectionId;
    }

    int getNumberOfPreCreatedDocuments() {
        return numberOfPreCreatedDocuments;
    }

    int getPrintingInterval() {
        return printingInterval;
    }

    int getConcurrency() {
        if (this.concurrency != null) {
            return concurrency;
        } else {
            return this.maxConnectionPoolSize;
        }
    }

    boolean isUseNameLink() {
        return useNameLink;
    }

    public boolean isEnableJvmStats() {
        return enableJvmStats;
    }

    public String getGraphiteEndpoint() {
        if (graphiteEndpoint == null) {
            return null;
        }

        return StringUtils.substringBeforeLast(graphiteEndpoint, ":");
    }

    public int getGraphiteEndpointPort() {
        if (graphiteEndpoint == null) {
            return -1;
        }

        String portAsString = Strings.emptyToNull(StringUtils.substringAfterLast(graphiteEndpoint, ":"));
        if (portAsString == null) {
            return GRAPHITE_SERVER_DEFAULT_PORT;
        } else {
            return Integer.parseInt(portAsString);
        }
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    void tryGetValuesFromSystem() {
        serviceEndpoint = StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("SERVICE_END_POINT")),
                                                    serviceEndpoint);

        masterKey = StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("MASTER_KEY")), masterKey);

        databaseId = StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("DATABASE_ID")), databaseId);

        collectionId = StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("COLLECTION_ID")),
                                                 collectionId);

        documentDataFieldSize = Integer.parseInt(
                StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("DOCUMENT_DATA_FIELD_SIZE")),
                                          Integer.toString(documentDataFieldSize)));

        maxConnectionPoolSize = Integer.parseInt(
                StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("MAX_CONNECTION_POOL_SIZE")),
                                          Integer.toString(maxConnectionPoolSize)));

        ConsistencyLevelConverter consistencyLevelConverter = new ConsistencyLevelConverter();
        consistencyLevel = consistencyLevelConverter.convert(StringUtils
                                                                     .defaultString(Strings.emptyToNull(System.getenv().get("CONSISTENCY_LEVEL")), consistencyLevel.name()));

        OperationTypeConverter operationTypeConverter = new OperationTypeConverter();
        operation = operationTypeConverter.convert(
                StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("OPERATION")), operation.name()));

        String concurrencyValue = StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("CONCURRENCY")),
                                                            concurrency == null ? null : Integer.toString(concurrency));
        concurrency = concurrencyValue == null ? null : Integer.parseInt(concurrencyValue);

        String numberOfOperationsValue = StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("NUMBER_OF_OPERATIONS")), Integer.toString(numberOfOperations));
        numberOfOperations = Integer.parseInt(numberOfOperationsValue);
    }
}
