// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.storage.queue;

import com.azure.spring.integration.core.converter.AzureMessageConverter;
import com.azure.spring.integration.storage.queue.converter.StorageQueueMessageConverter;
import com.azure.spring.integration.test.support.UnaryAzureMessageConverterTest;
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
