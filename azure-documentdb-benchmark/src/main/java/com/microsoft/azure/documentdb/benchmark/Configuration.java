package com.microsoft.azure.documentdb.benchmark;

import java.util.Arrays;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.microsoft.azure.documentdb.ConnectionMode;
import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.benchmark.Configuration.ClientType.ClientTypeConverter;

public class Configuration {

    @Parameter(names = "-serviceEndpoint", description = "Service Endpoint", required = true)
    private String serviceEndpoint;

    @Parameter(names = "-masterKey", description = "Master Key", required = true)
    private String masterKey;

    @Parameter(names = "-databaseId", description = "Database ID", required = true)
    private String databaseId;

    @Parameter(names = "-collectionId", description = "Collection ID", required = true)
    private String collectionId;
    
    @Parameter(names = "-partitionKey", description = "Partition Key", required = true)
    private String partitionKey;
    
    @Parameter(names = "-documentDataFieldSize", description = "Length of a document data field in characters (16-bit)")
    private int documentDataFieldSize = 1;

    @Parameter(names = "-maxConnectionPoolSize", description = "Max Connection Pool Size")
    private Integer maxConnectionPoolSize = 1000;

    @Parameter(names = "-consistencyLevel", description = "Consistency Level")
    private ConsistencyLevel consistencyLevel = ConsistencyLevel.Session;

    @Parameter(names = "-connectionMode", description = "Connection Mode")
    private ConnectionMode connectionMode = ConnectionMode.Gateway;

    @Parameter(names = "-clientType", description = "Type of Document Client", converter = ClientTypeConverter.class)
    private ClientType clientType = ClientType.rxNonBlocking;
    
    @Parameter(names = "-concurrency", description = "Degree of Concurrency in Inserting Documents (only applies to blocking client)."
            + " If this value is not specified, the max connection pool size will be used as the concurrency level.")
    private Integer concurrency;

    @Parameter(names = "-numberOfDocumentsToInsert", description = "Total Number Of Documents To Insert")
    private int numberOfDocumentsToInsert = 100000;

    @Parameter(names = "-rxEnableNativeLinuxEpoll", description = "Use Native Linux Epoll (only applies to rxNonBlocking in Gateway connection mode on Linux)."
            + " If on Linux enable this feature to get better performance.")
    private boolean rxEnableNativeLinuxEpoll = false;

    @Parameter(names = "-rxEventLoopSize", description = "Event Loop Size (only applies to rxNonBlocking)."
            + " For higher throughput the following adjustment is suggested:"
            + " rxEventLoopSize = 3/4(# CPU cores) and rxComputationPoolSize = 1/4(# CPU cores)")
    private Integer rxEventLoopSize;

    @Parameter(names = "-rxComputationPoolSize", description = "Computation Pool Size (only applies to rxNonBlocking)."
            + " For higher throughput the following adjustment is suggested:"
            + " rxEventLoopSize = 3/4(# CPU cores) and rxComputationPoolSize = 1/4(# CPU cores)")
    private Integer rxComputationPoolSize;
    
    @Parameter(names = {"-h", "-help", "--help"}, description = "Help", help = true)
    private boolean help = false;
    
    enum ClientType {
        blocking, rxNonBlocking;

        public static ClientType fromString(String code) {

            for(ClientType output : ClientType.values()) {
                if(output.toString().equalsIgnoreCase(code)) {
                    return output;
                }
            }

            return null;
        }

        public static class ClientTypeConverter implements IStringConverter<ClientType> {

            /* (non-Javadoc)
             * @see com.beust.jcommander.IStringConverter#convert(java.lang.String)
             */
            @Override
            public ClientType convert(String value) {
                ClientType ret = fromString(value);
                if (ret == null) {
                    throw new ParameterException("Value " + value + " can not be converted to ClientType. " +
                            "Available values are: " + Arrays.toString(ClientType.values()));
                }
                return ret;
            } 
        }
    }

    public ClientType getClientType() {
        return clientType;
    }
    
    public boolean isRxEnableNativeLinuxEpoll() {
        return rxEnableNativeLinuxEpoll;
    }

    public int getNumberOfDocumentsToInsert() {
        return numberOfDocumentsToInsert;
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

    public String getPartitionKey() {
        return partitionKey;
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
    
    public Integer getRxEventLoopSize() {
        return rxEventLoopSize;
    }

    public Integer getRxComputationPoolSize() {
        return rxComputationPoolSize;
    }
    
    public void validate() {
        switch (getClientType()) {
        case blocking: 
            String template = "%s only applies to " + ClientType.rxNonBlocking;
            validate(rxComputationPoolSize == null, String.format(template, "rxComputationPoolSize"));
            validate(rxEventLoopSize == null, String.format(template, "rxEventLoopSize"));
            validate(!rxEnableNativeLinuxEpoll, String.format(template, "rxEnableNativeLinuxEpoll"));
            break;

        case rxNonBlocking:
            template = "%s only applies to " + ClientType.blocking;
            validate(concurrency == null, String.format(template, "concurrency"));
            validate(!rxEnableNativeLinuxEpoll || ConnectionMode.DirectHttps != connectionMode, 
                    "rxEnableNativeLinuxEpoll only applies to Gateway connection mode");
        default:
            break;
        }
    }
    
    private void validate(boolean val, String msg) {
        if (!val) {
            throw new ParameterException(msg);
        }
    }
   
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
