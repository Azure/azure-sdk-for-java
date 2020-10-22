// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.storage.queue;

import com.azure.spring.integration.storage.queue.converter.StorageQueueMessageConverter;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.spring.integration.core.converter.AzureMessageConverter;
import com.azure.spring.integration.test.support.AzureMessageConverterTest;

public class StorageQueueMessageConverterTest extends AzureMessageConverterTest<QueueMessageItem> {
    @Override
    protected QueueMessageItem getInstance() {
        final QueueMessageItem queueMessageItem = new QueueMessageItem();
        queueMessageItem.setMessageText(this.payload);
        return queueMessageItem;
    }

    @Override
    protected AzureMessageConverter<QueueMessageItem> getConverter() {
        return new StorageQueueMessageConverter();
    }

    @Override
    protected Class<QueueMessageItem> getTargetClass() {
        return QueueMessageItem.class;
    }
}
