// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.storage.queue.support.converter;

import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.converter.UnaryAzureMessageConverterTests;
import com.azure.storage.queue.models.QueueMessageItem;
import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StorageQueueMessageConverterTests extends UnaryAzureMessageConverterTests<QueueMessageItem> {

    @Override
    protected AzureMessageConverter<QueueMessageItem, QueueMessageItem> getConverter() {
        return new StorageQueueMessageConverter();
    }

    @Override
    protected Class<QueueMessageItem> getTargetClass() {
        return QueueMessageItem.class;
    }

    @Override
    protected void assertMessageHeadersEqual(QueueMessageItem azureMessage, Message<?> message) {
        Class<?> payloadClass = message.getPayload().getClass();
        if (payloadClass == String.class) {
            assertEquals(azureMessage.getBody().toString(), message.getPayload());
        } else if (payloadClass == byte[].class) {
            assertEquals(azureMessage.getBody().toString(), new String((byte[]) message.getPayload(), StandardCharsets.UTF_8));
        } else {
            Object msg = azureMessage.getBody().toObject(payloadClass);
            assertEquals(msg, message.getPayload());
        }
    }
}
