// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties;

import com.azure.cosmos.CosmosClientBuilder;
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

@ConditionalOnClass({CosmosClientBuilder.class, ConnectionDetails.class})
@ConditionalOnBean(AzureCosmosConnectionDetails.class)
public class ConfigurationWithClassWithBean {
    private final Environment environment;
    private final AzureGlobalProperties globalProperties;
    private final AzureCosmosConnectionDetails connectionDetails;

    public ConfigurationWithClassWithBean(
        Environment environment,
        AzureGlobalProperties globalProperties,
        AzureCosmosConnectionDetails connectionDetails) {
        this.environment = environment;
        this.globalProperties = globalProperties;
        this.connectionDetails = connectionDetails;
    }

    @Bean
    AzureCosmosProperties azureCosmosProperties() {
        AzureCosmosProperties propertiesLoadFromGlobalProperties =
            AzureGlobalPropertiesUtils.loadProperties(globalProperties, new AzureCosmosProperties());
        BindResult<AzureCosmosProperties> bindResult = Binder.get(environment)
            .bind(AzureCosmosProperties.PREFIX, Bindable.ofInstance(propertiesLoadFromGlobalProperties));
        AzureCosmosProperties properties = bindResult.isBound() ? bindResult.get() : propertiesLoadFromGlobalProperties;
        properties.setEndpoint(connectionDetails.getEndpoint());
        properties.setDatabase(connectionDetails.getDatabase());
        properties.setKey(connectionDetails.getKey());
        properties.setConnectionMode(connectionDetails.getConnectionMode());
        properties.setEndpointDiscoveryEnabled(connectionDetails.getEndpointDiscoveryEnabled());
        return properties;
    }
}
