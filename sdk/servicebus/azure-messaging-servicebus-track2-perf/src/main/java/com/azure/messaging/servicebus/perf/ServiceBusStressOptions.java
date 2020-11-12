// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.messaging.servicebus.models.ReceiveMode;
import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents the command line configurable options for a performance test.
 */
@JsonPropertyOrder(alphabetic = true)
public class ServiceBusStressOptions extends PerfStressOptions {
    @Parameter(names = { "--mode" }, description = "The receive mode.")
    private ReceiveMode receiveMode = ReceiveMode.PEEK_LOCK;

    @Parameter(names = { "--autocomplete" }, description = "Enables autocomplete when receiving messages.")
    private boolean autoComplete = false;

    /**
     * Gets the receive mode for a test.
     *
     * @return The receive mode for a test.
     */
    public ReceiveMode getReceiveMode() {
        return receiveMode;
    }

    /**
     * Gets whether or not to autocomplete messages when receiving.
     *
     * @return {@code true} to autocomplete messages, {@code false} to manually complete message.
     */
    public boolean isAutoComplete() {
        return autoComplete;
    }
}
