// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf.models;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.TransportType;

/**
 * Set of options for running event hubs performance tests.
 */
public class EventHubsOptions extends PerfStressOptions {
    private static final String AZURE_EVENTHUBS_CONNECTION_STRING = "AZURE_EVENTHUBS_CONNECTION_STRING";
    private static final String AZURE_EVENTHUBS_EVENTHUB_NAME = "AZURE_EVENTHUBS_EVENT_HUB_NAME";

    @Parameter(names = {"--transportType"}, description = "TransportType for the connection",
        converter = TransportTypeConverter.class)
    private TransportType transportType;

    @Parameter(names = {"-cs", "--connectionString"}, description = "Connection string for Event Hubs namespace.")
    private String connectionString;

    @Parameter(names = {"-n", "--name"}, description = "Name of the Event Hub.")
    private String eventHubName;

    @Parameter(names = {"-cg", "--consumerGroup"}, description = "Name of the consumer group.")
    private String consumerGroup;

    @Parameter(names = {"-p", "--partitionId"}, description = "Partition to send events to or receive from.")
    private String partitionId;

    /**
     * Creates an instance with the default options.
     */
    public EventHubsOptions() {
        super();
        this.transportType = TransportType.AMQP;
        this.consumerGroup = EventHubClient.DEFAULT_CONSUMER_GROUP_NAME;
    }

    /**
     * Gets the Event Hubs namespace connection string.
     *
     * @return the Event Hubs namespace connection string.
     */
    public String getConnectionString() {
        return connectionString != null ? connectionString : System.getenv(AZURE_EVENTHUBS_CONNECTION_STRING);
    }

    /**
     * Gets the consumer group for receiving messages.
     *
     * @return The consumer group for receiving messages.
     */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     * Gets the number of events to send in a single iteration.
     *
     * @return The number of events to send in a single iteration.
     */
    @Override
    public int getCount() {
        return super.getCount();
    }

    /**
     * Gets the name of the Event Hub.
     *
     * @return The name of the Event Hub.
     */
    public String getEventHubName() {
        return eventHubName != null ? eventHubName : System.getenv(AZURE_EVENTHUBS_EVENTHUB_NAME);
    }

    /**
     * Gets the partition to receive events from or send events to.
     *
     * @return The partition to receive events from or send events to.
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     * Gets the transport type used for creating event hubs client.
     *
     * @return Transport type for Event Hubs connection.
     */
    public TransportType getTransportType() {
        return transportType;
    }

    static class TransportTypeConverter implements IStringConverter<TransportType> {
        @Override
        public TransportType convert(String s) {
            if (s == null) {
                throw new ParameterException(String.format("'%s' cannot be parsed into a TransportType", s));
            }

            return TransportType.valueOf(s);
        }
    }
}
