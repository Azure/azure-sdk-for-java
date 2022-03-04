// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.integration.eventhubs.inbound.EventHubsInboundChannelAdapter;
import com.azure.spring.integration.core.handler.DefaultMessageHandler;
import com.azure.spring.integration.core.implementation.instrumentation.DefaultInstrumentation;
import com.azure.spring.integration.core.instrumentation.Instrumentation;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

public class EventHubsMessageChannelTestBinder extends EventHubsMessageChannelBinder {

    private DefaultMessageHandler messageHandler;
    private MessageProducer messageProducer;
    private EventHubsInboundChannelAdapter inboundAdapter;

    public EventHubsMessageChannelTestBinder(String[] headersToEmbed,
                                             EventHubsChannelProvisioner provisioningProvider,
                                             DefaultMessageHandler messageHandler,
                                             MessageProducer messageProducer) {
        super(headersToEmbed, provisioningProvider);
        this.messageHandler = messageHandler;
        this.messageProducer = messageProducer;
    }

    @Override
    public MessageHandler createProducerMessageHandler(ProducerDestination destination,
                                                       ExtendedProducerProperties<EventHubsProducerProperties> producerProperties,
                                                       MessageChannel errorChannel) {
        MessageHandler handler;
        if (messageHandler == null) {
            handler = super.createProducerMessageHandler(destination, producerProperties,
                errorChannel);
        } else {
            handler = messageHandler;
        }
        return handler;
    }

    @Override
    public MessageProducer createConsumerEndpoint(ConsumerDestination destination,
                                                  String group,
                                                  ExtendedConsumerProperties<EventHubsConsumerProperties> properties) {
        MessageProducer messageProducer;
        if (this.messageProducer == null) {
            messageProducer = super.createConsumerEndpoint(destination, group, properties);
        } else {
            messageProducer = this.messageProducer;
        }
        return messageProducer;
    }

    @Override
    public MessageHandler createProducerMessageHandler(ProducerDestination destination,
                                                       ExtendedProducerProperties<EventHubsProducerProperties> producerProperties,
                                                       MessageChannel channel,
                                                       MessageChannel errorChannel) throws Exception {
        MessageHandler handler;
        if (messageHandler == null) {
            handler = super.createProducerMessageHandler(destination, producerProperties, channel, errorChannel);
        } else {
            handler = messageHandler;
        }
        return handler;
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
