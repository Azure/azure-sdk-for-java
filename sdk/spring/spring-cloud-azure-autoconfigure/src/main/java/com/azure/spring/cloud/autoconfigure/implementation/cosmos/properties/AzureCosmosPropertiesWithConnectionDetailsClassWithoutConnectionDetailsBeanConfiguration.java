package com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureGlobalPropertiesUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass({CosmosClientBuilder.class, ConnectionDetails.class})
@ConditionalOnMissingBean(ConnectionDetails.class)
@ConditionalOnProperty(value = "spring.cloud.azure.cosmos.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.cosmos", name = "endpoint")
public class AzureCosmosPropertiesWithConnectionDetailsClassWithoutConnectionDetailsBeanConfiguration {
    private final AzureGlobalProperties globalProperties;

    public AzureCosmosPropertiesWithConnectionDetailsClassWithoutConnectionDetailsBeanConfiguration(
        AzureGlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
    }

    @Bean
    @ConfigurationProperties(AzureCosmosProperties.PREFIX)
    AzureCosmosProperties azureCosmosProperties() {
        return AzureGlobalPropertiesUtils.loadProperties(globalProperties, new AzureCosmosProperties());
    }
}
