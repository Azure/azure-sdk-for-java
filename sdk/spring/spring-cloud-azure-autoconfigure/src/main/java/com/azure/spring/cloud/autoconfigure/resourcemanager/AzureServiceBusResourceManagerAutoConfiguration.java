// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.implementation.resourcemanager.ServiceBusResourceMetadata;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.resourcemanager.implementation.connectionstring.ServiceBusArmConnectionStringProvider;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.DefaultServiceBusProvisioner;
import com.azure.spring.cloud.resourcemanager.provisioning.ServiceBusProvisioner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * An auto-configuration for Service Bus
 *
 */
@ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, value = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(AzureResourceManager.class)
@AutoConfigureAfter(AzureResourceManagerAutoConfiguration.class)
@EnableConfigurationProperties(ServiceBusResourceMetadata.class)
public class AzureServiceBusResourceManagerAutoConfiguration extends AzureServiceResourceManagerConfigurationBase {

    private final ServiceBusResourceMetadata resourceMetadata;

    /**
     * Create {@link AzureServiceBusResourceManagerAutoConfiguration} instance
     * @param azureResourceManager the azure resource manager
     * @param resourceMetadata the Service Bus resource metadata
     */
    AzureServiceBusResourceManagerAutoConfiguration(AzureResourceManager azureResourceManager,
                                                    ServiceBusResourceMetadata resourceMetadata) {
        super(azureResourceManager);
        this.resourceMetadata = resourceMetadata;
    }

    /**
     * Autoconfigure the {@link ServiceBusProvisioner} instance.
     * @return the Service Bus provisioner.
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceBusProvisioner serviceBusProvisioner() {
        return new DefaultServiceBusProvisioner(this.azureResourceManager, this.resourceMetadata);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, value = "namespace")
    @ConditionalOnMissingProperty(prefix = AzureServiceBusProperties.PREFIX, value = "connection-string")
    @Order
    ServiceBusArmConnectionStringProvider serviceBusArmConnectionStringProvider() {
        return new ServiceBusArmConnectionStringProvider(this.azureResourceManager,
                                                         this.resourceMetadata,
                                                         this.resourceMetadata.getName());
    }

}

