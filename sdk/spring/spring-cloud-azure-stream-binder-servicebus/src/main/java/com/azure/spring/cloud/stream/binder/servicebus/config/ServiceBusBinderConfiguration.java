// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.config;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureServiceBusResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusMessagingAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.stream.binder.servicebus.ServiceBusMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelResourceManagerProvisioner;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.provisioning.ServiceBusProvisioner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnMissingBean(Binder.class)
@Import({
    AzureGlobalPropertiesAutoConfiguration.class,
    AzureResourceManagerAutoConfiguration.class,
    AzureServiceBusResourceManagerAutoConfiguration.class,
    AzureServiceBusAutoConfiguration.class,
    AzureServiceBusMessagingAutoConfiguration.class,
    ServiceBusBinderHealthIndicatorConfiguration.class
})
@EnableConfigurationProperties(ServiceBusExtendedBindingProperties.class)
public class ServiceBusBinderConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ ServiceBusProvisioner.class, AzureServiceBusProperties.class })
    public ServiceBusChannelProvisioner serviceBusChannelArmProvisioner(AzureServiceBusProperties serviceBusProperties,
                                                                        ServiceBusProvisioner serviceBusProvisioner) {


        return new ServiceBusChannelResourceManagerProvisioner(serviceBusProperties.getNamespace(),
            serviceBusProvisioner);
    }

    @Bean
    @ConditionalOnMissingBean({ServiceBusProvisioner.class, ServiceBusChannelProvisioner.class})
    public ServiceBusChannelProvisioner serviceBusChannelProvisioner() {
        return new ServiceBusChannelProvisioner();
    }


    @Bean
    @ConditionalOnMissingBean
    public ServiceBusMessageChannelBinder serviceBusBinder(ServiceBusChannelProvisioner channelProvisioner,
                                                           ServiceBusExtendedBindingProperties bindingProperties,
                                                           ObjectProvider<NamespaceProperties> namespaceProperties) {

        ServiceBusMessageChannelBinder binder = new ServiceBusMessageChannelBinder(null, channelProvisioner);
        binder.setBindingProperties(bindingProperties);
        binder.setNamespaceProperties(namespaceProperties.getIfAvailable());
        return binder;
    }

}
