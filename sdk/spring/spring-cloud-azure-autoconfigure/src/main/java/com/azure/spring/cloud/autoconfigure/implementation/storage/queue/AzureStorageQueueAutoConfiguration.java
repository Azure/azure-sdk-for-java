// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.queue;

import com.azure.spring.cloud.autoconfigure.implementation.storage.AzureStorageConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueuePropertiesConfiguration;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Storage Queue support.
 *
 * @since 4.0.0
 */
@Import({
    AzureStorageConfiguration.class,
    AzureStorageQueuePropertiesConfiguration.class,
    QueueClientConfiguration.class
})
@ConditionalOnClass(QueueServiceClientBuilder.class)
public class AzureStorageQueueAutoConfiguration {

}
