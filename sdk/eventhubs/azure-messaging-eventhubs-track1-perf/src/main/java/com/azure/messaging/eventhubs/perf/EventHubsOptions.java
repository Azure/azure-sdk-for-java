// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;
import com.microsoft.azure.eventhubs.TransportType;

/**
 * Set of options for running event hubs performance tests.
 */
public class EventHubsOptions extends PerfStressOptions {
    @Parameter(names = {"--transportType"}, description = "TransportType for the connection",
        converter = TransportTypeConverter.class)
    private TransportType transportType;

    @Parameter(names = {"-cs", "--connectionString"}, description = "Connection string for Event Hubs namespace.",
        required = true)
    private String connectionString;

    @Parameter(names = {"-n", "--name"}, description = "Name of the Event Hub.", required = true)
    private String eventHubName;

    /**
     * Creates an instance with the default options.
     */
    public EventHubsOptions() {
        super();
        this.transportType = TransportType.AMQP;
    }

    /**
     * Gets the Event Hubs namespace connection string.
     *
     * @return the Event Hubs namespace connection string.
     */
    public String getConnectionString() {
        return connectionString;
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
        return eventHubName;
    }

    /**
     * Gets the transport type used for creating event hubs client.
     *
     * @return Transport type for Event Hubs connection.
     */
    public TransportType getTransportType() {
        return transportType;
    }

    /**
     * Parses the command line parameter --transportType into a value.
     */
    static class TransportTypeConverter extends BaseConverter<TransportType> {
        TransportTypeConverter(String optionName) {
            super(optionName);
        }

        @Override
        public TransportType convert(String s) {
            if (s == null) {
                throw new ParameterException(getErrorString("null", "AmqpTransportType"));
            }

            try {
                return TransportType.valueOf(s);
            } catch (IllegalArgumentException e) {
                throw new ParameterException(getErrorString(s, "AmqpTransportType"), e);
            }
        }
    }
}
