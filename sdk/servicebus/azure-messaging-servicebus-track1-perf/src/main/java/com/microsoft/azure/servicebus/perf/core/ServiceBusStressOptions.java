// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents the command line configurable options for a performance test.
 */
@JsonPropertyOrder(alphabetic = true)
public class ServiceBusStressOptions extends PerfStressOptions {

    @Parameter(names = { "-mr", "--maxReceive" }, description = "MaxReceive messages")
    private int messagesToReceive = 10;

    @Parameter(names = { "-ms", "--messageSend" }, description = "Messages to send")
    private int messagesToSend = 10;

    @Parameter(names = { "-msb", "--messageSizeBytes" }, description = "Size(in bytes) of one Message")
    private int messagesSizeBytesToSend = 10;

    /**
     * Get the configured messagesToSend option for performance test.
     * @return The size.
     */
    public int getMessagesToSend() {
        return messagesToSend;
    }

    /**
     * Get the configured messagesToReceive option for performance test.
     * @return The size.
     */
    public int getMessagesToReceive() {
        return messagesToReceive;
    }

    /**
     * Get the configured messagesSizeBytesToSend option for performance test.
     * @return The size.
     */
    public int getMessagesSizeBytesToSend() {
        return messagesSizeBytesToSend;
    }
}
