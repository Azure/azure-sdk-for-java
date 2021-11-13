// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.resourcemanager.connectionstring.ServiceBusArmConnectionStringProvider;
import com.azure.spring.servicebus.provisioning.ServiceBusProvisioner;
import com.azure.spring.servicebus.provisioning.arm.DefaultServiceBusProvisioner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * An auto-configuration for Service Bus
 *
 */
@ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, value = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(ServiceBusClientBuilder.class)
@ConditionalOnBean(AzureResourceManager.class)
@AutoConfigureAfter(AzureResourceManagerAutoConfiguration.class)
public class AzureServiceBusResourceManagerAutoConfiguration extends AzureServiceResourceManagerConfigurationBase {

    private final AzureServiceBusProperties serviceBusProperties;

    public AzureServiceBusResourceManagerAutoConfiguration(AzureResourceManager azureResourceManager,
                                                           AzureServiceBusProperties serviceBusProperties) {
        super(azureResourceManager);
        this.serviceBusProperties = serviceBusProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusProvisioner serviceBusProvisioner() {
        return new DefaultServiceBusProvisioner(this.azureResourceManager, this.serviceBusProperties.getResource());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, value = "namespace")
    @ConditionalOnMissingProperty(prefix = AzureServiceBusProperties.PREFIX, value = "connection-string")
    @Order
    public ServiceBusArmConnectionStringProvider serviceBusArmConnectionStringProvider() {
        return new ServiceBusArmConnectionStringProvider(this.azureResourceManager,
                                                         this.serviceBusProperties.getResource(),
                                                         this.serviceBusProperties.getNamespace());
    }

}

