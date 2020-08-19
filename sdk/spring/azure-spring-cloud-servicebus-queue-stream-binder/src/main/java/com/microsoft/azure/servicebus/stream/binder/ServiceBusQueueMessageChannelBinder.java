// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusQueueExtendedBindingProperties;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusChannelProvisioner;
import com.microsoft.azure.spring.integration.core.api.SendOperation;
import com.microsoft.azure.spring.integration.servicebus.inbound.ServiceBusQueueInboundChannelAdapter;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.lang.NonNull;

/**
 * @author Warren Zhu
 */
public class ServiceBusQueueMessageChannelBinder extends
        ServiceBusMessageChannelBinder<ServiceBusQueueExtendedBindingProperties> {

    private final ServiceBusQueueOperation serviceBusQueueOperation;

    public ServiceBusQueueMessageChannelBinder(String[] headersToEmbed,
            @NonNull ServiceBusChannelProvisioner provisioningProvider,
            @NonNull ServiceBusQueueOperation serviceBusQueueOperation) {
        super(headersToEmbed, provisioningProvider);
        this.serviceBusQueueOperation = serviceBusQueueOperation;
        this.bindingProperties = new ServiceBusQueueExtendedBindingProperties();
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
            ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {

        this.serviceBusQueueOperation.setCheckpointConfig(buildCheckpointConfig(properties));
        this.serviceBusQueueOperation.setClientConfig(buildClientConfig(properties));
        ServiceBusQueueInboundChannelAdapter inboundAdapter =
                new ServiceBusQueueInboundChannelAdapter(destination.getName(), this.serviceBusQueueOperation);
        inboundAdapter.setBeanFactory(getBeanFactory());
        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, group, properties);
        inboundAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());
        return inboundAdapter;
    }

    @Override
    SendOperation getSendOperation() {
        return this.serviceBusQueueOperation;
    }
}
