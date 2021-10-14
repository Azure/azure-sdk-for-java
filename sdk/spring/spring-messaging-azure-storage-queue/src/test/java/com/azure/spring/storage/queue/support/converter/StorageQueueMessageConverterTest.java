// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.storage.queue.support.converter;

import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.converter.UnaryAzureMessageConverterTest;
import com.azure.storage.queue.models.QueueMessageItem;
import org.springframework.messaging.Message;

public class StorageQueueMessageConverterTest extends UnaryAzureMessageConverterTest<QueueMessageItem> {

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

    }
}
