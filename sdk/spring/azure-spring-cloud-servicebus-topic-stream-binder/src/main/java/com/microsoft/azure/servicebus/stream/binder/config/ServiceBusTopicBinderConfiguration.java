// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.stream.binder.config;

import com.microsoft.azure.servicebus.stream.binder.ServiceBusTopicMessageChannelBinder;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusTopicExtendedBindingProperties;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusChannelProvisioner;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusTopicChannelResourceManagerProvisioner;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureEnvironmentAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusProperties;
import com.microsoft.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusTopicAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.servicebus.ServiceBusUtils;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

/**
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnMissingBean(Binder.class)
@Import({AzureServiceBusTopicAutoConfiguration.class, AzureEnvironmentAutoConfiguration.class})
@EnableConfigurationProperties({AzureServiceBusProperties.class, ServiceBusTopicExtendedBindingProperties.class})
public class ServiceBusTopicBinderConfiguration {

    private static final String SERVICE_BUS_TOPIC_BINDER = "ServiceBusTopicBinder";
    private static final String NAMESPACE = "Namespace";

    @Autowired(required = false)
    private ResourceManagerProvider resourceManagerProvider;

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(SERVICE_BUS_TOPIC_BINDER);
    }

    @Bean
    @ConditionalOnBean(ResourceManagerProvider.class)
    @ConditionalOnMissingBean
    public ServiceBusChannelProvisioner serviceBusChannelProvisioner(AzureServiceBusProperties serviceBusProperties) {
        if (this.resourceManagerProvider != null) {
            return new ServiceBusTopicChannelResourceManagerProvisioner(resourceManagerProvider,
                    serviceBusProperties.getNamespace());
        } else {
            TelemetryCollector.getInstance().addProperty(SERVICE_BUS_TOPIC_BINDER, NAMESPACE,
                    ServiceBusUtils.getNamespace(serviceBusProperties.getConnectionString()));
        }
        return new ServiceBusChannelProvisioner();
    }

    @Bean
    @ConditionalOnMissingBean({ResourceManagerProvider.class, ServiceBusChannelProvisioner.class})
    public ServiceBusChannelProvisioner serviceBusChannelProvisionerWithResourceManagerProvider() {
        return new ServiceBusChannelProvisioner();
    }

    @Bean
    public ServiceBusTopicMessageChannelBinder serviceBusTopicBinder(
            ServiceBusChannelProvisioner topicChannelProvisioner, ServiceBusTopicOperation serviceBusTopicOperation,
            ServiceBusTopicExtendedBindingProperties bindingProperties) {
        ServiceBusTopicMessageChannelBinder binder =
                new ServiceBusTopicMessageChannelBinder(null, topicChannelProvisioner, serviceBusTopicOperation);
        binder.setBindingProperties(bindingProperties);
        return binder;
    }
}
