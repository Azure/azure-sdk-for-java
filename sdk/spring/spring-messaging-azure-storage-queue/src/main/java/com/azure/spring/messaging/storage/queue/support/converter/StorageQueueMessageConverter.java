// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.storage.queue.support.converter;

import com.azure.core.util.BinaryData;
import com.azure.spring.messaging.converter.AbstractAzureMessageConverter;
import com.azure.storage.queue.models.QueueMessageItem;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import static com.azure.spring.messaging.implementation.converter.ObjectMapperHolder.OBJECT_MAPPER;

/**
 * A converter to turn a {@link org.springframework.messaging.Message} to {@link QueueMessageItem} and vice versa.
 */
public class StorageQueueMessageConverter extends AbstractAzureMessageConverter<QueueMessageItem, QueueMessageItem> {

    private final ObjectMapper objectMapper;

    /**
     * Create an instance of {@link StorageQueueMessageConverter}.
     */
    public StorageQueueMessageConverter() {
        this(OBJECT_MAPPER);
    }

    /**
     * Create an instance of {@link StorageQueueMessageConverter}.
     * @param objectMapper the object mapper.
     */
    public StorageQueueMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected byte[] getPayload(QueueMessageItem azureMessage) {
        return azureMessage.getBody().toBytes();
    }

    @Override
    protected QueueMessageItem fromString(String payload) {
        final QueueMessageItem queueMessageItem = new QueueMessageItem();
        queueMessageItem.setBody(BinaryData.fromString(payload));
        return queueMessageItem;
    }

    @Override
    protected QueueMessageItem fromByte(byte[] payload) {
        final QueueMessageItem queueMessageItem = new QueueMessageItem();
        queueMessageItem.setBody(BinaryData.fromString(new String(payload, StandardCharsets.UTF_8)));
        return queueMessageItem;
    }

}
