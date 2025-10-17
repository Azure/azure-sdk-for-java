// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties;

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
@ConditionalOnBean(AzureEventHubsConnectionDetails.class)
class ConfigurationWithConnectionDetailsBean {

    private final Environment environment;
    private final AzureGlobalProperties globalProperties;
    private final AzureEventHubsConnectionDetails connectionDetails;

    ConfigurationWithConnectionDetailsBean(
        Environment environment,
        AzureGlobalProperties globalProperties,
        AzureEventHubsConnectionDetails connectionDetails) {
        this.environment = environment;
        this.globalProperties = globalProperties;
        this.connectionDetails = connectionDetails;
    }

    @Bean
    AzureEventHubsProperties azureEventHubsProperties() {
        AzureEventHubsProperties propertiesLoadFromGlobalProperties =
            AzureGlobalPropertiesUtils.loadProperties(globalProperties, new AzureEventHubsProperties());
        BindResult<AzureEventHubsProperties> bindResult = Binder.get(environment)
            .bind(AzureEventHubsProperties.PREFIX, Bindable.ofInstance(propertiesLoadFromGlobalProperties));
        AzureEventHubsProperties properties = bindResult.isBound() ? bindResult.get() : propertiesLoadFromGlobalProperties;
        properties.setConnectionString(connectionDetails.getConnectionString());
        return properties;
    }

}
