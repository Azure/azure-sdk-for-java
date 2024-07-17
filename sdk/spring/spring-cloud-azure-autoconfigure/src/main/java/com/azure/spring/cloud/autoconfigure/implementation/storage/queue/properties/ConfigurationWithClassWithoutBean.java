// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties;

import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureServicePropertiesUtils;
import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@ConditionalOnClass(ConnectionDetails.class)
@ConditionalOnMissingBean(AzureStorageQueueConnectionDetails.class)
@ConditionalOnProperty(
    value = {"spring.cloud.azure.storage.queue.enabled", "spring.cloud.azure.storage.enabled"},
    havingValue = "true",
    matchIfMissing = true)
@ConditionalOnAnyProperty(
    prefixes = {"spring.cloud.azure.storage.queue", "spring.cloud.azure.storage"},
    name = {"account-name", "endpoint", "connection-string"})
class ConfigurationWithClassWithoutBean {
    Environment environment;

    ConfigurationWithClassWithoutBean(
        Environment environment) {
        this.environment = environment;
    }

    @Bean
    AzureStorageQueueProperties azureStorageQueueProperties(
        @Qualifier("azureStorageProperties") AzureStorageProperties azureStorageProperties) {
        AzureStorageQueueProperties propertiesLoadFromServiceCommonProperties = AzureServicePropertiesUtils
            .loadServiceCommonProperties(azureStorageProperties, new AzureStorageQueueProperties());
        BindResult<AzureStorageQueueProperties> bindResult = Binder.get(environment)
            .bind(AzureStorageQueueProperties.PREFIX, Bindable.ofInstance(propertiesLoadFromServiceCommonProperties));
        return bindResult.isBound() ? bindResult.get() : propertiesLoadFromServiceCommonProperties;
    }

}
