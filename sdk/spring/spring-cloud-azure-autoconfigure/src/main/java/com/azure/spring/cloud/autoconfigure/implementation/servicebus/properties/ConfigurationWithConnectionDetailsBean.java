// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureGlobalPropertiesUtils;
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
    private final AzureGlobalProperties globalProperties;
    private final AzureServiceBusConnectionDetails connectionDetails;

    ConfigurationWithConnectionDetailsBean(
        Environment environment,
        AzureGlobalProperties globalProperties,
        AzureServiceBusConnectionDetails connectionDetails) {
        this.environment = environment;
        this.globalProperties = globalProperties;
        this.connectionDetails = connectionDetails;
    }

    @Bean
    AzureServiceBusProperties azureServiceBusProperties() {
        AzureServiceBusProperties propertiesLoadFromGlobalProperties =
            AzureGlobalPropertiesUtils.loadProperties(globalProperties, new AzureServiceBusProperties());
        BindResult<AzureServiceBusProperties> bindResult = Binder.get(environment)
            .bind(AzureServiceBusProperties.PREFIX, Bindable.ofInstance(propertiesLoadFromGlobalProperties));
        AzureServiceBusProperties properties = bindResult.isBound() ? bindResult.get()
            : propertiesLoadFromGlobalProperties;
        properties.setConnectionString(connectionDetails.getConnectionString());
        properties.setNamespace(null);
        return properties;
    }

}
