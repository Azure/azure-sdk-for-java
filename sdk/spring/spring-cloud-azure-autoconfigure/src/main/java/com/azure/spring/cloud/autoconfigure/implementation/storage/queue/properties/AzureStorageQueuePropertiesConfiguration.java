// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@Import({
    AzureStorageQueuePropertiesWithConnectionDetailsClassWithConnectionDetailsBeanConfiguration.class,
    AzureStorageQueuePropertiesWithConnectionDetailsClassWithoutConnectionDetailsBeanConfiguration.class,
    AzureStorageQueuePropertiesWithoutConnectionDetailsClassConfiguration.class
})
@EnableConfigurationProperties
public class AzureStorageQueuePropertiesConfiguration {

}
