package com.azure.messaging.eventhubs.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

/**
 * Options for Event Processor Jedis tests.
 *
 * @see EventProcessorJedisTest
 */
public class EventProcessorJedisOptions extends PerfStressOptions {
    @Parameter(names = {"-hn", "--hostName"}, description = "Host name of Azure Redis cache.")
    private String hostName = "$Default";

    @Parameter(names = {"-pw", "--password"}, description = "The primary key for Azure Redis Cache.")
    private String password = "$Default";

    @Parameter(names = {"-u", "--userName"}, description = "The username required to configure a JedisPool object.")
    private String userName = "$Default";

    @Parameter(names = { "-meps", "--maxEventsPerSecond" }, description = "Maximum Events to send per second.")
    private int maxEventsPerSecond = 0;

    @Parameter(names = { "-ea", "--errorAfter" }, description = "Error After duration in seconds.")
    private int errorAfterInSeconds = 0;

    @Parameter(names = { "-pt", "--partitions" }, description = "Number of Partitions.")
    private int partitions = 1;

    @Parameter(names = {"-cg", "--consumerGroup"}, description = "Name of the consumer group.")
    private String consumerGroup = "$Default";

    @Parameter(names = {"-cs", "--connectionString"}, description = "The EventHub namespace connection string")
    private String connectionString = "$Default";

    @Parameter(names = {"-ehn", "--eventHubName"}, description = "Name of the event hub.")
    private String eventHubName = "$Default";

    /**
     * Gets the host name of the Azure Redis Cache to connect to.
     *
     * @return The host name of the Azure Redis Cache.
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Gets the primary key of the Azure Redis Cache to connect to.
     *
     * @return The primary ket of the Azure Redis Cache.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the username for making the JedisPool object.
     *
     * @return The username for JedisPool object
     */
     public String getUserName() {
        return userName;
    }
    /**
     * Get Error after duration in seconds.
     * @return the error after duration in seconds.
     */
    public int getErrorAfterInSeconds() {
        return errorAfterInSeconds;
    }

    /**
     * Get Maximum events per second.
     * @return the max events per second.
     */
    public int getMaxEventsPerSecond() {
        return maxEventsPerSecond;
    }


    public int getPartitions() {
        return partitions;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public String getEventHubName() {
        return eventHubName;
    }

    public String getConnectionString() {
        return connectionString;
    }
}
