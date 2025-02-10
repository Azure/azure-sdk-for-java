// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties;

import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureServicePropertiesUtils;
import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@ConditionalOnClass(ConnectionDetails.class)
@ConditionalOnBean(AzureStorageQueueConnectionDetails.class)
class ConfigurationWithConnectionDetailsBean {
    private final Environment environment;
    private final AzureStorageQueueConnectionDetails connectionDetails;

    ConfigurationWithConnectionDetailsBean(
        Environment environment,
        AzureStorageQueueConnectionDetails connectionDetails) {
        this.environment = environment;
        this.connectionDetails = connectionDetails;
    }

    @Bean
    AzureStorageQueueProperties azureStorageQueueProperties(
        @Qualifier("azureStorageProperties") AzureStorageProperties azureStorageProperties) {
        AzureStorageQueueProperties propertiesLoadFromServiceCommonProperties = AzureServicePropertiesUtils
            .loadServiceCommonProperties(azureStorageProperties, new AzureStorageQueueProperties());
        BindResult<AzureStorageQueueProperties> bindResult = Binder.get(environment)
            .bind(AzureStorageQueueProperties.PREFIX, Bindable.ofInstance(propertiesLoadFromServiceCommonProperties));
        AzureStorageQueueProperties properties = bindResult.isBound() ? bindResult.get()
            : propertiesLoadFromServiceCommonProperties;
        properties.setConnectionString(connectionDetails.getConnectionString());
        properties.setEndpoint(connectionDetails.getEndpoint());
        return properties;

    }

}
