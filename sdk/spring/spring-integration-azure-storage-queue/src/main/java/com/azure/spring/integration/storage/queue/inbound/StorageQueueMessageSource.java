// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.storage.queue.inbound;

import com.azure.spring.messaging.storage.queue.core.StorageQueueTemplate;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import java.time.Duration;

/**
 * Inbound Message Source to receive messages from Azure Storage Queue.
 *
 */
public class StorageQueueMessageSource extends AbstractMessageSource<Message<?>> {

    private final StorageQueueTemplate storageQueueTemplate;
    private final String destination;
    private final Duration visibilityTimeout;

    /**
     * Construct a {@link StorageQueueMessageSource} with the specified destination and {@link StorageQueueTemplate}.
     * Default visibility timeout of 30 seconds would apply.
     *
     * @param destination the destination
     * @param storageQueueTemplate the storage queue operation
     */
    public StorageQueueMessageSource(String destination, StorageQueueTemplate storageQueueTemplate) {
        this(destination, storageQueueTemplate, null);
    }

    /**
     * Construct a {@link StorageQueueMessageSource} with the specified destination, {@link StorageQueueTemplate}
     * and visibility timeout.
     *
     * @param destination the destination
     * @param storageQueueTemplate the storage queue operation
     * @param visibilityTimeout The timeout period for how long the message is invisible in the queue.
     *                          If left empty the dequeued messages will be invisible for 30 seconds.
     *                          The timeout must be between 1 second and 7 days
     */
    public StorageQueueMessageSource(String destination, StorageQueueTemplate storageQueueTemplate,
                                     Duration visibilityTimeout) {
        Assert.hasText(destination, "destination can't be null or empty");
        this.storageQueueTemplate = storageQueueTemplate;
        this.destination = destination;
        this.visibilityTimeout = visibilityTimeout;
    }

    @Override
    public Object doReceive() {
        return storageQueueTemplate.receiveAsync(destination, visibilityTimeout).block();
    }

    @Override
    public String getComponentType() {
        return "storage-queue:message-source";
    }
}
