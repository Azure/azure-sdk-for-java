// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.context.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.context.core.api.AzureResourceMetadata;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * An auto-configuration for Service Bus
 *
 * @author Warren Zhu
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(AzureResourceManagerAutoConfiguration.class)
@ConditionalOnClass(ServiceBusReceivedMessage.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureServiceBusProperties.class)
public class AzureServiceBusOperationAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusClientAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ AzureResourceManager.class, AzureResourceMetadata.class })
    public ServiceBusNamespaceManager serviceBusNamespaceManager(AzureResourceManager azureResourceManager,
                                                                 AzureResourceMetadata azureResourceMetadata) {
        return new ServiceBusNamespaceManager(azureResourceManager, azureResourceMetadata);
    }

    /**
     * Create a {@link ServiceBusConnectionStringProvider} bean. The bean will hold the connection string to the service
     * bus. If connection-string property is configured in the property files, it will use it. Otherwise, it will try to
     * construct the connection-string using the resource manager.
     *
     * @param namespaceManager The resource manager for Service Bus namespaces.
     * @param properties The Service Bus properties.
     * @return The {@link ServiceBusConnectionStringProvider} bean.
     * @throws IllegalArgumentException If connection string is empty.
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceBusConnectionStringProvider serviceBusConnectionStringProvider(
        @Autowired(required = false) ServiceBusNamespaceManager namespaceManager,
        AzureServiceBusProperties properties) {

        final String namespace = properties.getNamespace();
        final String connectionString = properties.getConnectionString();

        if (StringUtils.hasText(connectionString)) {
            return new ServiceBusConnectionStringProvider(connectionString);
        } else if (namespaceManager != null && namespace != null) {
            LOGGER.info("'spring.cloud.azure.servicebus.connection-string' auto configured");

            return new ServiceBusConnectionStringProvider(namespaceManager.getOrCreate(namespace));
        }

        LOGGER.warn("Can't construct the ServiceBusConnectionStringProvider, namespace: {}, connectionString: {}",
            namespace, connectionString);

        return null;
    }
}
