// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.config;

import com.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureEnvironmentAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusProperties;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusTopicAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.ServiceBusUtils;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicSubscriptionManager;
import com.azure.spring.cloud.telemetry.TelemetryCollector;
import com.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.azure.spring.servicebus.stream.binder.ServiceBusTopicMessageChannelBinder;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusTopicExtendedBindingProperties;
import com.azure.spring.servicebus.stream.binder.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.servicebus.stream.binder.provisioning.ServiceBusTopicChannelResourceManagerProvisioner;
import org.springframework.beans.factory.annotation.Autowired;
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
@Import({
    AzureEnvironmentAutoConfiguration.class,
    AzureContextAutoConfiguration.class,
    AzureServiceBusAutoConfiguration.class,
    AzureServiceBusTopicAutoConfiguration.class
})
@EnableConfigurationProperties({ AzureServiceBusProperties.class, ServiceBusTopicExtendedBindingProperties.class })
public class ServiceBusTopicBinderConfiguration {

    private static final String SERVICE_BUS_TOPIC_BINDER = "ServiceBusTopicBinder";
    private static final String NAMESPACE = "Namespace";

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(SERVICE_BUS_TOPIC_BINDER);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusChannelProvisioner serviceBusChannelProvisioner(
        AzureServiceBusProperties serviceBusProperties,
        @Autowired(required = false) ServiceBusNamespaceManager serviceBusNamespaceManager,
        @Autowired(required = false) ServiceBusTopicManager serviceBusTopicManager,
        @Autowired(required = false) ServiceBusTopicSubscriptionManager serviceBusTopicSubscriptionManager) {

        if (serviceBusNamespaceManager != null
                && serviceBusTopicManager != null
                && serviceBusTopicSubscriptionManager != null) {
            return new ServiceBusTopicChannelResourceManagerProvisioner(serviceBusNamespaceManager,
                                                                        serviceBusTopicManager,
                                                                        serviceBusTopicSubscriptionManager,
                                                                        serviceBusProperties.getNamespace());
        } else {
            final String namespace = ServiceBusUtils.getNamespace(serviceBusProperties.getConnectionString());
            TelemetryCollector.getInstance()
                              .addProperty(SERVICE_BUS_TOPIC_BINDER, NAMESPACE, namespace);
        }
        return new ServiceBusChannelProvisioner();
    }

    @Bean
    public ServiceBusTopicMessageChannelBinder serviceBusTopicBinder(
        ServiceBusChannelProvisioner topicChannelProvisioner,
        ServiceBusTopicOperation serviceBusTopicOperation,
        ServiceBusTopicExtendedBindingProperties bindingProperties) {

        ServiceBusTopicMessageChannelBinder binder = new ServiceBusTopicMessageChannelBinder(null,
                                                                                             topicChannelProvisioner,
                                                                                             serviceBusTopicOperation);
        binder.setBindingProperties(bindingProperties);
        return binder;
    }
}
