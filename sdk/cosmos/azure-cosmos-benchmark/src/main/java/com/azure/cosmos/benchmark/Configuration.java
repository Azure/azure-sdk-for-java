// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;
import com.google.common.net.PercentEscaper;
import io.micrometer.azuremonitor.AzureMonitorConfig;
import io.micrometer.azuremonitor.AzureMonitorMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.lang.Nullable;
import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteMeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Configuration {

    public final static String DEFAULT_PARTITION_KEY_PATH = "/pk";
    private final static int DEFAULT_GRAPHITE_SERVER_PORT = 2003;
    private MeterRegistry azureMonitorMeterRegistry;
    private MeterRegistry graphiteMeterRegistry;

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

    @Parameter(names = "-documentDataFieldCount", description = "Number of data fields in document")
    private int documentDataFieldCount = 5;

    @Parameter(names = "-maxConnectionPoolSize", description = "Max Connection Pool Size")
    private Integer maxConnectionPoolSize = 1000;

    @Parameter(names = "-diagnosticsThresholdDuration", description = "Latency threshold for printing diagnostics", converter = DurationConverter.class)
    private Duration diagnosticsThresholdDuration = Duration.ofSeconds(60);

    @Parameter(names = "-disablePassingPartitionKeyAsOptionOnWrite", description = "Disables passing partition in request options for write operation;" +
        " in this case, json will be parsed and partition key will be extracted (this requires more computational overhead).")
    private boolean disablePassingPartitionKeyAsOptionOnWrite = false;

    @Parameter(names = "-consistencyLevel", description = "Consistency Level", converter = ConsistencyLevelConverter.class)
    private ConsistencyLevel consistencyLevel = ConsistencyLevel.SESSION;

    @Parameter(names = "-connectionMode", description = "Connection Mode")
    private ConnectionMode connectionMode = ConnectionMode.DIRECT;

    @Parameter(names = "-graphiteEndpoint", description = "Graphite endpoint")
    private String graphiteEndpoint;

    @Parameter(names = "-enableJvmStats", description = "Enables JVM Stats")
    private boolean enableJvmStats;

    @Parameter(names = "-throughput", description = "provisioned throughput for test container")
    private int throughput = 100000;

    @Parameter(names = "-numberOfCollectionForCtl", description = "Number of collections for ctl load")
    private int numberOfCollectionForCtl = 4;

    @Parameter(names = "-readWriteQueryPct", description = "Comma separated read write query workload percent")
    private String readWriteQueryPct = "90,9,1";

    @Parameter(names = "-manageDatabase", description = "Control switch for creating/deleting underlying database resource")
    private boolean manageDatabase = false;

    @Parameter(names = "-preferredRegionsList", description = "Comma separated preferred regions list")
    private String preferredRegionsList;

    @Parameter(names = "-encryptedStringFieldCount", description = "Number of string field that need to be encrypted")
    private int encryptedStringFieldCount = 1;

    @Parameter(names = "-encryptedLongFieldCount", description = "Number of long field that need to be encrypted")
    private int encryptedLongFieldCount = 0;

    @Parameter(names = "-encryptedDoubleFieldCount", description = "Number of double field that need to be encrypted")
    private int encryptedDoubleFieldCount = 0;

    @Parameter(names = "-encryptionEnabled", description = "Control switch to enable the encryption operation")
    private boolean encryptionEnabled = false;

    @Parameter(names = "-operation", description = "Type of Workload:\n"
        + "\tReadThroughput- run a READ workload that prints only throughput *\n"
        + "\tReadThroughputWithMultipleClients - run a READ workload that prints throughput and latency for multiple client read.*\n"
        + "\tWriteThroughput - run a Write workload that prints only throughput\n"
        + "\tReadLatency - run a READ workload that prints both throughput and latency *\n"
        + "\tWriteLatency - run a Write workload that prints both throughput and latency\n"
        + "\tQueryInClauseParallel - run a 'Select * from c where c.pk in (....)' workload that prints latency\n"
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
        + "\tCtlWorkload - run a ctl workflow.*\n"
        + "\tReadAllItemsOfLogicalPartition - run a workload that uses readAllItems for a logical partition and prints throughput\n"
        + "\n\t* writes 10k documents initially, which are used in the reads"
        + "\tLinkedInCtlWorkload - ctl for LinkedIn workload.*\n",
        converter = Operation.OperationTypeConverter.class)
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

    @Parameter(names = "-reportingDirectory", description = "Location of a directory to which metrics should be printed as comma-separated values")
    private String reportingDirectory = null;

    @Parameter(names = "-numberOfPreCreatedDocuments", description = "Total NUMBER Of Documents To pre create for a read workload to use")
    private int numberOfPreCreatedDocuments = 1000;

    @Parameter(names = "-sparsityWaitTime", description = "Sleep time before making each request. Default is no sleep time."
        + " NOTE: For now only ReadLatency and ReadThroughput support this."
        + " Format: A string representation of this duration using ISO-8601 seconds based representation, such as "
        + "PT20.345S (20.345 seconds), PT15M (15 minutes)", converter = DurationConverter.class)
    private Duration sparsityWaitTime = null;

    @Parameter(names = "-skipWarmUpOperations", description = "the number of operations to be skipped before starting perf numbers.")
    private int skipWarmUpOperations = 0;

    @Parameter(names = "-useSync", description = "Uses Sync API")
    private boolean useSync = false;

    @Parameter(names = "-contentResponseOnWriteEnabled", description = "if set to false, does not returns content response on document write operations")
    private String contentResponseOnWriteEnabled = String.valueOf(true);

    @Parameter(names = "-bulkloadBatchSize", description = "Control the number of documents uploaded in each BulkExecutor load iteration (Only supported for the LinkedInCtlWorkload)")
    private int bulkloadBatchSize = 200000;

    @Parameter(names = "-testScenario", description = "The test scenario (GET, QUERY) for the LinkedInCtlWorkload")
    private String testScenario = "GET";

    @Parameter(names = "-accountNameInGraphiteReporter", description = "if set, account name with be appended in graphite reporter")
    private boolean accountNameInGraphiteReporter = false;

    public enum Environment {
        Daily,   // This is the CTL environment where we run the workload for a fixed number of hours
        Staging; // This is the CTL environment where the worload runs as a long running job

        static class EnvironmentConverter implements IStringConverter<Environment> {
            @Override
            public Environment convert(String value) {
                if (value == null) {
                    return Environment.Daily;
                }

                return Environment.valueOf(value);
            }
        }
    }

    @Parameter(names = "-environment", description = "The CTL Environment we are validating the workload",
        converter = Environment.EnvironmentConverter.class)
    private Environment environment = Environment.Daily;

    @Parameter(names = {"-h", "-help", "--help"}, description = "Help", help = true)
    private boolean help = false;

    public enum Operation {
        ReadThroughput,
        WriteThroughput,
        ReadLatency,
        WriteLatency,
        QueryInClauseParallel,
        QueryCross,
        QuerySingle,
        QuerySingleMany,
        QueryParallel,
        QueryOrderby,
        QueryAggregate,
        QueryAggregateTopOrderby,
        QueryTopOrderby,
        Mixed,
        ReadMyWrites,
        ReadThroughputWithMultipleClients,
        CtlWorkload,
        ReadAllItemsOfLogicalPartition,
        LinkedInCtlWorkload;

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

    public int getSkipWarmUpOperations() {
        return skipWarmUpOperations;
    }

    public Duration getSparsityWaitTime() {
        return sparsityWaitTime;
    }

    public boolean isDisablePassingPartitionKeyAsOptionOnWrite() {
        return disablePassingPartitionKeyAsOptionOnWrite;
    }

    public boolean isSync() {
        return useSync;
    }

    public boolean isAccountNameInGraphiteReporter() {
        return accountNameInGraphiteReporter;
    }

    public Duration getMaxRunningTimeDuration() {
        return maxRunningTimeDuration;
    }

    public Operation getOperationType() {
        return operation;
    }

    public int getNumberOfOperations() {
        return numberOfOperations;
    }

    public int getThroughput() {
        return throughput;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public String getMasterKey() {
        return masterKey;
    }

    public boolean isHelp() {
        return help;
    }

    public int getDocumentDataFieldSize() {
        return documentDataFieldSize;
    }

    public int getDocumentDataFieldCount() {
        return documentDataFieldCount;
    }

    public Integer getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    public String isContentResponseOnWriteEnabled() {
        return contentResponseOnWriteEnabled;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public int getNumberOfPreCreatedDocuments() {
        return numberOfPreCreatedDocuments;
    }

    public int getPrintingInterval() {
        return printingInterval;
    }

    public Duration getDiagnosticsThresholdDuration() {
        return diagnosticsThresholdDuration;
    }

    public File getReportingDirectory() {
        return reportingDirectory == null ? null : new File(reportingDirectory);
    }

    public int getConcurrency() {
        if (this.concurrency != null) {
            return concurrency;
        } else {
            return this.maxConnectionPoolSize;
        }
    }

    public boolean isUseNameLink() {
        return useNameLink;
    }

    public boolean isEnableJvmStats() {
        return enableJvmStats;
    }

    public MeterRegistry getAzureMonitorMeterRegistry() {
        String instrumentationKey = System.getProperty("azure.cosmos.monitoring.azureMonitor.instrumentationKey",
            StringUtils.defaultString(Strings.emptyToNull(
                System.getenv().get("AZURE_INSTRUMENTATION_KEY")), null));
        return instrumentationKey == null ? null : this.azureMonitorMeterRegistry(instrumentationKey);
    }

    public MeterRegistry getGraphiteMeterRegistry() {
        String serviceAddress = System.getProperty("azure.cosmos.monitoring.graphite.serviceAddress",
            StringUtils.defaultString(Strings.emptyToNull(
                System.getenv().get("GRAPHITE_SERVICE_ADDRESS")), null));
        return serviceAddress == null ? null : this.graphiteMeterRegistry(serviceAddress);
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
            return DEFAULT_GRAPHITE_SERVER_PORT;
        } else {
            return Integer.parseInt(portAsString);
        }
    }

    public int getNumberOfCollectionForCtl(){
        return this.numberOfCollectionForCtl;
    }

    public String getReadWriteQueryPct() {
        return this.readWriteQueryPct;
    }

    public boolean shouldManageDatabase() {
        return this.manageDatabase;
    }

    public int getBulkloadBatchSize() {
        return this.bulkloadBatchSize;
    }

    public String getTestScenario() {
        return this.testScenario;
    }

    public Environment getEnvironment() {
        return this.environment;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public List<String> getPreferredRegionsList() {
        List<String> preferredRegions = null;
        if (StringUtils.isNotEmpty(preferredRegionsList)) {
            String[] preferredArray = preferredRegionsList.split(",");
            if (preferredArray != null && preferredArray.length > 0) {
                preferredRegions = new ArrayList<>(Arrays.asList(preferredArray));
            }
        }
        return preferredRegions;
    }

    public int getEncryptedStringFieldCount() {
        return encryptedStringFieldCount;
    }

    public int getEncryptedLongFieldCount() {
        return encryptedLongFieldCount;
    }

    public int getEncryptedDoubleFieldCount() {
        return encryptedDoubleFieldCount;
    }

    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    public void tryGetValuesFromSystem() {
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

        Operation.OperationTypeConverter operationTypeConverter = new Operation.OperationTypeConverter();
        operation = operationTypeConverter.convert(
                StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("OPERATION")), operation.name()));

        String concurrencyValue = StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("CONCURRENCY")),
                                                            concurrency == null ? null : Integer.toString(concurrency));
        concurrency = concurrencyValue == null ? null : Integer.parseInt(concurrencyValue);

        String numberOfOperationsValue = StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("NUMBER_OF_OPERATIONS")), Integer.toString(numberOfOperations));
        numberOfOperations = Integer.parseInt(numberOfOperationsValue);

        String throughputValue = StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("THROUGHPUT")), Integer.toString(throughput));
        throughput = Integer.parseInt(throughputValue);

        preferredRegionsList = StringUtils.defaultString(Strings.emptyToNull(System.getenv().get(
            "PREFERRED_REGIONS_LIST")), preferredRegionsList);

        encryptedStringFieldCount = Integer.parseInt(
            StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("ENCRYPTED_STRING_FIELD_COUNT")),
                Integer.toString(encryptedStringFieldCount)));

        encryptedLongFieldCount = Integer.parseInt(
            StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("ENCRYPTED_LONG_FIELD_COUNT")),
                Integer.toString(encryptedLongFieldCount)));

        encryptedDoubleFieldCount = Integer.parseInt(
            StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("ENCRYPTED_DOUBLE_FIELD_COUNT")),
                Integer.toString(encryptedDoubleFieldCount)));

        encryptionEnabled = Boolean.parseBoolean(StringUtils.defaultString(Strings.emptyToNull(System.getenv().get(
            "ENCRYPTED_ENABLED")),
            Boolean.toString(encryptionEnabled)));
    }

    private synchronized MeterRegistry azureMonitorMeterRegistry(String instrumentationKey) {

        if (this.azureMonitorMeterRegistry == null) {

            Duration step = Duration.ofSeconds(Integer.getInteger("azure.cosmos.monitoring.azureMonitor.step", this.printingInterval));
            boolean enabled = !Boolean.getBoolean("azure.cosmos.monitoring.azureMonitor.disabled");

            final AzureMonitorConfig config = new AzureMonitorConfig() {

                @Override
                @Nullable
                public String get(@Nullable String key) {
                    return null;
                }

                @Override
                @Nullable
                public String instrumentationKey() {
                    return instrumentationKey;
                }

                @Override
                public Duration step() {
                    return step;
                }

                @Override
                public boolean enabled() {
                    return enabled;
                }
            };

            this.azureMonitorMeterRegistry = new AzureMonitorMeterRegistry(config, Clock.SYSTEM);
        }

        return this.azureMonitorMeterRegistry;
    }

    @SuppressWarnings("UnstableApiUsage")
    private synchronized MeterRegistry graphiteMeterRegistry(String serviceAddress) {

        if (this.graphiteMeterRegistry == null) {

            HostAndPort address = HostAndPort.fromString(serviceAddress);

            String host = address.getHost();
            int port = address.getPortOrDefault(DEFAULT_GRAPHITE_SERVER_PORT);
            boolean enabled = !Boolean.getBoolean("azure.cosmos.monitoring.graphite.disabled");
            Duration step = Duration.ofSeconds(Integer.getInteger("azure.cosmos.monitoring.graphite.step", this.printingInterval));

            final GraphiteConfig config = new GraphiteConfig() {

                private String[] tagNames = { "source" };

                @Override
                @Nullable
                public String get(@Nullable String key) {
                    return null;
                }

                @Override
                public boolean enabled() {
                    return enabled;
                }

                @Override
                @Nullable
                public String host() {
                    return host;
                }

                @Override
                @Nullable
                public int port() {
                    return port;
                }

                @Override
                @Nullable
                public Duration step() {
                    return step;
                }

                @Override
                @Nullable
                public String[] tagsAsPrefix() {
                    return this.tagNames;
                }
            };

            this.graphiteMeterRegistry = new GraphiteMeterRegistry(config, Clock.SYSTEM);
            String source;

            try {
                PercentEscaper escaper = new PercentEscaper("_-", false);
                source = escaper.escape(InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException error) {
                source = "unknown-host";
            }

            this.graphiteMeterRegistry.config()
                .namingConvention(NamingConvention.dot)
                .commonTags("source", source);
        }

        return this.graphiteMeterRegistry;
    }
}
