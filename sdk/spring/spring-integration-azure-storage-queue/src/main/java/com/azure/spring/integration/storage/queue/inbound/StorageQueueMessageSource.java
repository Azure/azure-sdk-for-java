// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.storage.queue.inbound;

import com.azure.spring.messaging.storage.queue.core.StorageQueueTemplate;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * Inbound Message Source to receive messages from Azure Storage Queue.
 *
 */
public class StorageQueueMessageSource extends AbstractMessageSource<Message<?>> {

    private final StorageQueueTemplate storageQueueTemplate;
    private final String destination;

    /**
     * Construct a {@link StorageQueueMessageSource} with the specified destination and {@link StorageQueueTemplate}.
     *
     * @param destination the destination
     * @param storageQueueTemplate the storage queue operation
     */
    public StorageQueueMessageSource(String destination, StorageQueueTemplate storageQueueTemplate) {
        Assert.hasText(destination, "destination can't be null or empty");
        this.storageQueueTemplate = storageQueueTemplate;
        this.destination = destination;
    }

    @Override
    public Object doReceive() {
        return storageQueueTemplate.receiveAsync(destination, null).block();
    }

    @Override
    public String getComponentType() {
        return "storage-queue:message-source";
    }
}
