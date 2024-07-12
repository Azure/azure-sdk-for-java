package com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties;

import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureServicePropertiesUtils;
import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConditionalOnMissingClass("org.springframework.boot.autoconfigure.service.connection.ConnectionDetails")
@ConditionalOnProperty(
    value = { "spring.cloud.azure.storage.queue.enabled",  "spring.cloud.azure.storage.enabled" },
    havingValue = "true",
    matchIfMissing = true)
@ConditionalOnAnyProperty(
    prefixes = { "spring.cloud.azure.storage.queue", "spring.cloud.azure.storage" },
    name = { "account-name", "endpoint", "connection-string" })
public class ConfigurationWithoutClass {

    @Bean
    @ConfigurationProperties(AzureStorageQueueProperties.PREFIX)
    AzureStorageQueueProperties azureStorageQueueProperties(@Qualifier("azureStorageProperties") AzureStorageProperties azureStorageProperties) {
        return AzureServicePropertiesUtils.loadServiceCommonProperties(azureStorageProperties, new AzureStorageQueueProperties());
    }
}
