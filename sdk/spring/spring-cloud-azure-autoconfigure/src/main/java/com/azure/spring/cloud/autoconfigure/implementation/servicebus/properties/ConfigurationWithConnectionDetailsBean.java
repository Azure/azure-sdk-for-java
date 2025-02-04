// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties;

import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureServicePropertiesUtils;
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
@ConditionalOnBean(AzureServiceBusConnectionDetails.class)
class ConfigurationWithConnectionDetailsBean {

    private final Environment environment;
    private final AzureServiceBusConnectionDetails connectionDetails;

    ConfigurationWithConnectionDetailsBean(
        Environment environment,
        AzureServiceBusConnectionDetails connectionDetails) {
        this.environment = environment;
        this.connectionDetails = connectionDetails;
    }

    @Bean
    AzureServiceBusProperties azureServiceBusProperties(
        @Qualifier("azureServiceBusProperties") AzureServiceBusProperties azureServiceBusProperties) {
        AzureServiceBusProperties propertiesLoadFromServiceCommonProperties = AzureServicePropertiesUtils
            .loadServiceCommonProperties(azureServiceBusProperties, new AzureServiceBusProperties());
        BindResult<AzureServiceBusProperties> bindResult = Binder.get(environment)
            .bind(AzureServiceBusProperties.PREFIX, Bindable.ofInstance(propertiesLoadFromServiceCommonProperties));
        AzureServiceBusProperties properties = bindResult.isBound() ? bindResult.get()
            : propertiesLoadFromServiceCommonProperties;
        properties.setConnectionString(connectionDetails.getConnectionString());
        return properties;

    }

}
