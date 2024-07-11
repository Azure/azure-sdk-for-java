package com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties;

import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureServicePropertiesUtils;
import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@ConditionalOnClass(ConnectionDetails.class)
@ConditionalOnBean(AzureStorageBlobConnectionDetails.class)
public class AzureStorageBlobPropertiesWithConnectionDetailsClassWithConnectionDetailsBeanConfiguration {
    private final Environment environment;
    private final AzureStorageBlobConnectionDetails connectionDetails;

    public AzureStorageBlobPropertiesWithConnectionDetailsClassWithConnectionDetailsBeanConfiguration(
        Environment environment,
        AzureStorageBlobConnectionDetails connectionDetails) {
        this.environment = environment;
        this.connectionDetails = connectionDetails;
    }

    @Bean
    AzureStorageBlobProperties azureStorageBlobProperties(
        @Qualifier("azureStorageProperties") AzureStorageProperties azureStorageProperties) {
        AzureStorageBlobProperties propertiesLoadFromServiceCommonProperties = AzureServicePropertiesUtils
            .loadServiceCommonProperties(azureStorageProperties, new AzureStorageBlobProperties());
        AzureStorageBlobProperties properties = Binder.get(environment)
            .bind(AzureStorageBlobProperties.PREFIX, Bindable.ofInstance(propertiesLoadFromServiceCommonProperties))
            .get();
        properties.setConnectionString(connectionDetails.getConnectionString());
        return properties;

    }

}
