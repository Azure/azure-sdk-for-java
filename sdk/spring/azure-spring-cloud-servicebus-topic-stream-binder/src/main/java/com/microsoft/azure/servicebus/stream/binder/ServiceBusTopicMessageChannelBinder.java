// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusTopicExtendedBindingProperties;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusChannelProvisioner;
import com.microsoft.azure.spring.integration.core.api.SendOperation;
import com.microsoft.azure.spring.integration.servicebus.inbound.ServiceBusTopicInboundChannelAdapter;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * @author Warren Zhu
 */
public class ServiceBusTopicMessageChannelBinder extends
        ServiceBusMessageChannelBinder<ServiceBusTopicExtendedBindingProperties> {

    private final ServiceBusTopicOperation serviceBusTopicOperation;

    public ServiceBusTopicMessageChannelBinder(String[] headersToEmbed,
            @NonNull ServiceBusChannelProvisioner provisioningProvider,
            @NonNull ServiceBusTopicOperation serviceBusTopicOperation) {
        super(headersToEmbed, provisioningProvider);
        this.serviceBusTopicOperation = serviceBusTopicOperation;
        this.bindingProperties = new ServiceBusTopicExtendedBindingProperties();
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
            ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {

        this.serviceBusTopicOperation.setCheckpointConfig(buildCheckpointConfig(properties));
        this.serviceBusTopicOperation.setClientConfig(buildClientConfig(properties));
        boolean anonymous = !StringUtils.hasText(group);
        if (anonymous) {
            group = "anonymous." + UUID.randomUUID().toString();
        }
        ServiceBusTopicInboundChannelAdapter inboundAdapter =
                new ServiceBusTopicInboundChannelAdapter(destination.getName(), this.serviceBusTopicOperation, group);
        inboundAdapter.setBeanFactory(getBeanFactory());
        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, group, properties);
        inboundAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());
        return inboundAdapter;
    }

    @Override
    SendOperation getSendOperation() {
        return this.serviceBusTopicOperation;
    }
}
