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
    private String hostName;

    @Parameter(names = {"-pw", "--password"}, description = "The primary key for Azure Redis Cache.")
    private String password;

    @Parameter(names = {"-u", "--userName"}, description = "The username required to configure a JedisPool object.")
    private String userName = "$Default";

    @Parameter(names = {"-meps", "--maxEventsPerSecond"}, description = "Maximum Events to send per second.")
    private int maxEventsPerSecond = 0;

    @Parameter(names = {"-ea", "--errorAfter"}, description = "Error After duration in seconds.")
    private int errorAfterInSeconds = 0;

    @Parameter(names = {"-pt", "--partitions"}, description = "Number of Partitions.")
    private int partitions;

    @Parameter(names = {"-cg", "--consumerGroup"}, description = "Name of the consumer group.")
    private String consumerGroup = "$Default";

    @Parameter(names = {"-cs", "--connectionString"}, description = "The EventHub namespace connection string")
    private String connectionString;

    @Parameter(names = {"-ehn", "--eventHubName"}, description = "Name of the event hub.")
    private String eventHubName;

    /**
     *
     * @return the number of seconds after which the error was thrown
     */
    public int getErrorAfterInSeconds() {
        return errorAfterInSeconds;
    }

    /**
     *
     * @return the maximum number of events to be processed by the event hub
     */
    public int getMaxEventsPerSecond() {
        return maxEventsPerSecond;
    }

    /**
     *
     * @return the number of partitions
     */
    public int getPartitions() {
        return partitions;
    }

    /**
     *
     * @return the name of the consumer group
     */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     *
     * @return the connection string required for the event hub
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     *
     * @return the name of the eventhub
     */
    public String getEventHubName() {
        return eventHubName;
    }

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

}
