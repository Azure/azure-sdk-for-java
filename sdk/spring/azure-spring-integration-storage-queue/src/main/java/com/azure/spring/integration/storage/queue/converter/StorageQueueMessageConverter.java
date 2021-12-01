// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.storage.queue.converter;

import com.azure.spring.integration.core.converter.AbstractAzureMessageConverter;
import com.azure.storage.queue.models.QueueMessageItem;

import java.nio.charset.StandardCharsets;

/**
 * A converter to turn a {@link org.springframework.messaging.Message} to {@link QueueMessageItem} and vice versa.
 */
public class StorageQueueMessageConverter extends AbstractAzureMessageConverter<QueueMessageItem, QueueMessageItem> {

    /**
     *
     * @param azureMessage The azure message
     * @return The payload bytes.
     */
    @Override
    protected byte[] getPayload(QueueMessageItem azureMessage) {
        return azureMessage.getMessageText().getBytes(StandardCharsets.UTF_8);
    }

    /**
     *
     * @param payload The payload.
     * @return The queue message item.
     */
    @Override
    protected QueueMessageItem fromString(String payload) {
        final QueueMessageItem queueMessageItem = new QueueMessageItem();
        queueMessageItem.setMessageText(payload);
        return queueMessageItem;
    }

    /**
     *
     * @param payload The payload
     * @return The queue message item.
     */
    @Override
    protected QueueMessageItem fromByte(byte[] payload) {
        final QueueMessageItem queueMessageItem = new QueueMessageItem();
        queueMessageItem.setMessageText(new String(payload, StandardCharsets.UTF_8));
        return queueMessageItem;
    }

}
