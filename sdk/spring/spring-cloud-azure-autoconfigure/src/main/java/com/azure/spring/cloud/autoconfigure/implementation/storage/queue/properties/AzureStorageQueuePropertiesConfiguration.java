// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@Import({
    ConfigurationWithConnectionDetailsBean.class,
    ConfigurationWithoutConnectionDetailsBean.class,
})
@EnableConfigurationProperties({AzureStorageQueueProperties.class, AzureGlobalProperties.class})
public class AzureStorageQueuePropertiesConfiguration {

}
