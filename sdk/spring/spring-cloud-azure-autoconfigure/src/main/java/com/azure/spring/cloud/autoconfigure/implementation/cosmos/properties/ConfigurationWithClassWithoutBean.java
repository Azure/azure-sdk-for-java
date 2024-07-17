// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties;

import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureGlobalPropertiesUtils;
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
@ConditionalOnMissingBean(AzureCosmosConnectionDetails.class)
@ConditionalOnProperty(value = "spring.cloud.azure.cosmos.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.cosmos", name = "endpoint")
class ConfigurationWithClassWithoutBean {
    private final Environment environment;
    private final AzureGlobalProperties globalProperties;

    ConfigurationWithClassWithoutBean(
        Environment environment,
        AzureGlobalProperties globalProperties) {
        this.environment = environment;
        this.globalProperties = globalProperties;
    }

    @Bean
    AzureCosmosProperties azureCosmosProperties() {
        AzureCosmosProperties propertiesLoadFromGlobalProperties =
            AzureGlobalPropertiesUtils.loadProperties(globalProperties, new AzureCosmosProperties());
        BindResult<AzureCosmosProperties> bindResult = Binder.get(environment)
            .bind(AzureCosmosProperties.PREFIX, Bindable.ofInstance(propertiesLoadFromGlobalProperties));
        return bindResult.isBound() ? bindResult.get() : propertiesLoadFromGlobalProperties;
    }
}
