package com.microsoft.azure.cosmosdb.benchmark;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Strings;
import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.benchmark.Configuration.Operation.OperationTypeConverter;

public class Configuration {

    @Parameter(names = "-serviceEndpoint", description = "Service Endpoint")
    private String serviceEndpoint;

    @Parameter(names = "-masterKey", description = "Master Key")
    private String masterKey;

    @Parameter(names = "-databaseId", description = "Database ID")
    private String databaseId;

    @Parameter(names = "-collectionId", description = "Collection ID")
    private String collectionId;

    @Parameter(names = "-documentDataFieldSize", description = "Length of a document data field in characters (16-bit)")
    private int documentDataFieldSize = 20;

    @Parameter(names = "-maxConnectionPoolSize", description = "Max Connection Pool Size")
    private Integer maxConnectionPoolSize = 1000;

    @Parameter(names = "-consistencyLevel", description = "Consistency Level", converter = ConsistencyLevelConverter.class)
    private ConsistencyLevel consistencyLevel = ConsistencyLevel.Session;

    @Parameter(names = "-connectionMode", description = "Connection Mode")
    private ConnectionMode connectionMode = ConnectionMode.Gateway;

    @Parameter(names = "-operation", description = "Type of Document Client", converter = OperationTypeConverter.class)
    private Operation operation = Operation.Write;

    @Parameter(names = "-concurrency", description = "Degree of Concurrency in Inserting Documents."
            + " If this value is not specified, the max connection pool size will be used as the concurrency level.")
    private Integer concurrency;

    @Parameter(names = "-numberOfOperations", description = "Total Number Of Documents To Insert")
    private int numberOfOperations = 100000;

    @Parameter(names = {"-h", "-help", "--help"}, description = "Help", help = true)
    private boolean help = false;

    enum Operation {
        Read, Write, QueryCross, QuerySingle, QuerySingleMany,
        QueryParallel, QueryOrderby, QueryAggregate, 
        QueryAggregateTopOrderby, QueryTopOrderby, Mixed;

        public static Operation fromString(String code) {

            for(Operation output : Operation.values()) {
                if(output.toString().equalsIgnoreCase(code)) {
                    return output;
                }
            }

            return null;
        }

        public static class OperationTypeConverter implements IStringConverter<Operation> {

            /* (non-Javadoc)
             * @see com.beust.jcommander.IStringConverter#convert(java.lang.String)
             */
            @Override
            public Operation convert(String value) {
                Operation ret = fromString(value);
                if (ret == null) {
                    throw new ParameterException("Value " + value + " can not be converted to ClientType. " +
                            "Available values are: " + Arrays.toString(Operation.values()));
                }
                return ret;
            } 
        }
    }

    public static ConsistencyLevel fromString(String code) {
        for(ConsistencyLevel output : ConsistencyLevel.values()) {
            if(output.toString().equalsIgnoreCase(code)) {
                return output;
            }
        }
        return null;
    }

    public static class ConsistencyLevelConverter implements IStringConverter<ConsistencyLevel> {

        /* (non-Javadoc)
         * @see com.beust.jcommander.IStringConverter#convert(java.lang.String)
         */
        @Override
        public ConsistencyLevel convert(String value) {
            ConsistencyLevel ret = fromString(value);
            if (ret == null) {
                throw new ParameterException("Value " + value + " can not be converted to ClientType. " +
                        "Available values are: " + Arrays.toString(Operation.values()));
            }
            return ret;
        } 
    }

    public Operation getOperationType() {
        return operation;
    }

    public int getNumberOfOperations() {
        return numberOfOperations;
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

    public ConnectionPolicy getConnectionPolicy() {
        ConnectionPolicy policy = new ConnectionPolicy();
        policy.setConnectionMode(connectionMode);
        policy.setMaxPoolSize(maxConnectionPoolSize);
        return policy;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public int getConcurrency() {
        if (this.concurrency != null) {
            return concurrency;
        } else {
            return this.maxConnectionPoolSize;
        }
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public void tryGetValuesFromSystem() {
        serviceEndpoint = StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("SERVICE_END_POINT")), 
                serviceEndpoint);

        masterKey = StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("MASTER_KEY")), 
                masterKey);

        databaseId = StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("DATABASE_ID")), 
                databaseId);

        collectionId = StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("COLLECTION_ID")), 
                collectionId);

        documentDataFieldSize = Integer.parseInt(StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("DOCUMENT_DATA_FIELD_SIZE")), 
                Integer.toString(documentDataFieldSize)));

        maxConnectionPoolSize = Integer.parseInt(StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("MAX_CONNECTION_POOL_SIZE")), 
                Integer.toString(maxConnectionPoolSize)));

        ConsistencyLevelConverter consistencyLevelConverter = new ConsistencyLevelConverter();
        consistencyLevel =  consistencyLevelConverter.convert(StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("CONSISTENCY_LEVEL")), 
                consistencyLevel.name()));

        OperationTypeConverter operationTypeConverter = new OperationTypeConverter();
        operation = operationTypeConverter.convert(StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("OPERATION")), 
                operation.name()));

        String concurrencyValue = StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("CONCURRENCY")), 
                concurrency == null ? null : Integer.toString(concurrency));
        concurrency = concurrencyValue == null ? null : Integer.parseInt(concurrencyValue);

        String numberOfOperationsValue = StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("NUMBER_OF_OPERATIONS")), 
                Integer.toString(numberOfOperations));
        numberOfOperations = numberOfOperationsValue == null ? null : Integer.parseInt(numberOfOperationsValue);
    }
}
