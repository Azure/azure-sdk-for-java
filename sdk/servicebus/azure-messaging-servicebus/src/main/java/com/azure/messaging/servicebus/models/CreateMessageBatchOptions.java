// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

/**
 * The set of options that can be specified when creating an batch of messages. This wrapper will help to limit
 * the messages with maximum allowed size.
 *
 * @see ServiceBusMessageBatch
 * @see ServiceBusSenderAsyncClient#createMessageBatch(CreateMessageBatchOptions)
 * @see ServiceBusSenderClient#createMessageBatch(CreateMessageBatchOptions)
 */
@Fluent
public final class CreateMessageBatchOptions {
    private int maximumSizeInBytes;

    /**
     * Sets the maximum size for the batch of messages.
     *
     * @param maximumSizeInBytes The maximum size to allow for the batch of messages.
     *
     * @return The updated {@link CreateMessageBatchOptions} object.
     */
    public CreateMessageBatchOptions setMaximumSizeInBytes(int maximumSizeInBytes) {
        this.maximumSizeInBytes = maximumSizeInBytes;
        return this;
    }

    /**
     * Gets the maximum size to allow for the batch of messages, in bytes.
     *
     * @return The maximum size to allow for a single batch of messages, in bytes.
     */
    public int getMaximumSizeInBytes() {
        return maximumSizeInBytes;
    }
}
