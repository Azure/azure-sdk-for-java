// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

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

    @Parameter(names = { "-idm", "--isDeleteMode" }, description = "Receiver client is receive_and_delete mode or peek_lock mode")
    private boolean isDeleteMode = true;

    @Parameter(names = { "-mcc", "--maxConcurrentCalls" }, description = "Processor client max concurrent calls")
    private int maxConcurrentCalls = 1;

    @Parameter(names = { "-pc", "--prefetchCount"}, description = "Client prefetch count")
    private int prefetchCount = 1;

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

    /**
     * Get the configured isDeleteMode option for performance test.
     * @return Receive mod is receive_and_delete mode or not.
     */
    public boolean getIsDeleteMode() {
        return isDeleteMode;
    }

    /**
     * Get the configured maxConcurrentCalls option for performance test.
     * @return The max concurrent size.
     */
    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    /**
     * Get the configured prefetchCount option for performance tst.
     * @return
     */
    public int getPrefetchCount() {
        return prefetchCount;
    }
}
