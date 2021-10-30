// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusTopicExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.messaging.core.SendOperation;
import com.azure.spring.integration.servicebus.inbound.ServiceBusTopicInboundChannelAdapter;
import com.azure.spring.servicebus.core.ServiceBusTemplate;
import com.azure.spring.servicebus.core.processor.DefaultServiceBusNamespaceQueueProcessorClientFactory;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueTemplate;
import com.azure.spring.servicebus.core.sender.DefaultServiceBusNamespaceQueueSenderClientFactory;
import com.azure.spring.servicebus.core.topic.ServiceBusTopicOperation;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Map;
import java.util.UUID;

/**
 * @author Warren Zhu
 */
public class ServiceBusTopicMessageChannelBinder extends
        ServiceBusMessageChannelBinder<ServiceBusTopicExtendedBindingProperties> {

    public ServiceBusTopicMessageChannelBinder(String[] headersToEmbed,
            @NonNull ServiceBusChannelProvisioner provisioningProvider) {
        super(headersToEmbed, provisioningProvider);
        this.bindingProperties = new ServiceBusTopicExtendedBindingProperties();
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
            ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {
        Assert.notNull(getServiceBusTemplate(), "ServiceBusTemplate can't be null when create a consumer");
        serviceBusInUse.put(destination.getName(), new ServiceBusInformation(group));

        this.serviceBusTemplate.setCheckpointConfig(buildCheckpointConfig(properties));
        boolean anonymous = !StringUtils.hasText(group);
        if (anonymous) {
            group = "anonymous." + UUID.randomUUID();
        }
        ServiceBusTopicInboundChannelAdapter inboundAdapter =
                new ServiceBusTopicInboundChannelAdapter(destination.getName(), this.serviceBusTemplate, group);
        inboundAdapter.setBeanFactory(getBeanFactory());
        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, group, properties);
        inboundAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());
        return inboundAdapter;
    }

    @Override
    protected ServiceBusTemplate getServiceBusTemplate() {
        if (this.serviceBusTemplate == null) {
            this.serviceBusTemplate = new ServiceBusQueueTemplate(
                new DefaultServiceBusNamespaceQueueSenderClientFactory(this.namespaceProperties,
                    getProducerPropertiesSupplier()),
                new DefaultServiceBusNamespaceQueueProcessorClientFactory(this.namespaceProperties,
                    getProcessorPropertiesSupplier()));
        }
        return this.serviceBusTemplate;
    }

    private PropertiesSupplier<Tuple2<String, String>, ProducerProperties> getProducerPropertiesSupplier() {
        return key -> {
            Map<String, ServiceBusBindingProperties> bindings = bindingProperties.getBindings();
            for (Map.Entry<String, ServiceBusBindingProperties> entry : bindings.entrySet()) {
                ProducerProperties properties = bindings.get(entry.getKey()).getProducer().getProducer();
                if (key.equalsIgnoreCase(properties.getTopicName())) {
                    return properties;
                }
            }
            return null;
        };
    }

    private PropertiesSupplier<Tuple2<String, String>, ProcessorProperties> getProcessorPropertiesSupplier() {
        return key -> {
            Map<String, ServiceBusBindingProperties> bindings = bindingProperties.getBindings();
            for (Map.Entry<String, ServiceBusBindingProperties> entry : bindings.entrySet()) {
                ProcessorProperties properties = bindings.get(entry.getKey()).getConsumer().getProcessor();
                if (key.equals(Tuples.of(properties.getTopicName(), properties.getSubscriptionName()))) {
                    return properties;
                }
            }
            return null;
        };
    }
}
