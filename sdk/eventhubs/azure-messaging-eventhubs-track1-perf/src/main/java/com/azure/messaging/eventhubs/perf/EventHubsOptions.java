// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.microsoft.azure.eventhubs.TransportType;

/**
 * Set of options for running event hubs performance tests.
 */
public class EventHubsOptions extends PerfStressOptions {
    @Parameter(names = { "--transportType" }, description = "TransportType for the connection",
        converter = TransportTypeConverter.class)
    private TransportType transportType;

    @Parameter(names = { "--batchSize" }, description = "The number of messages in EventDataBatch.")
    private int messagesInBatch;

    /**
     * Gets the transport type used for creating event hubs client.
     *
     * @return Transport type for Event Hubs connection.
     */
    public TransportType getTransportType() {
        return transportType;
    }

    /**
     * Gets the number of messages to put in an EventDataBatch.
     *
     * @return The number of messages to put inside a batch.
     */
    public int getMessagesInBatch() {
        return messagesInBatch;
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
