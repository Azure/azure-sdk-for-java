// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.integration.core.implementation.instrumentation.DefaultInstrumentation;
import com.azure.spring.integration.core.instrumentation.Instrumentation;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

public class ServiceBusMessageChannelTestBinder extends ServiceBusMessageChannelBinder {

    /**
     * Construct a {@link ServiceBusMessageChannelBinder} with the specified headersToEmbed and {@link
     * ServiceBusChannelProvisioner}.
     *
     * @param headersToEmbed the headers to embed
     * @param provisioningProvider the provisioning provider
     */
    public ServiceBusMessageChannelTestBinder(String[] headersToEmbed,
                                              ServiceBusChannelProvisioner provisioningProvider) {
        super(headersToEmbed, provisioningProvider);
    }

    public MessageHandler createProducerMessageHandler(ProducerDestination destination, ExtendedProducerProperties<ServiceBusProducerProperties> producerProperties, MessageChannel errorChannel) {
        return super.createProducerMessageHandler(destination, producerProperties, errorChannel);
    }

    public MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group, ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {
        return super.createConsumerEndpoint(destination, group, properties);
    }

    public void addProducerDownInstrumentation() {
        DefaultInstrumentation producer = new DefaultInstrumentation("producer", Instrumentation.Type.PRODUCER);
        producer.setStatus(Instrumentation.Status.DOWN, new IllegalArgumentException("Producer exception"));
        getInstrumentationManager().addHealthInstrumentation(producer);
    }

    public void addProcessorDownInstrumentation() {
        DefaultInstrumentation processor = new DefaultInstrumentation("Processor", Instrumentation.Type.PRODUCER);
        processor.setStatus(Instrumentation.Status.DOWN, new IllegalArgumentException("Processor exception"));
        getInstrumentationManager().addHealthInstrumentation(processor);
    }
}
