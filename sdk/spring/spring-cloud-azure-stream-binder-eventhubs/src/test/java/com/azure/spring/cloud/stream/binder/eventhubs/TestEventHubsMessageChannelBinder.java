// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.integration.eventhubs.inbound.EventHubsInboundChannelAdapter;
import com.azure.spring.integration.handler.DefaultMessageHandler;
import com.azure.spring.integration.instrumentation.DefaultInstrumentation;
import com.azure.spring.integration.instrumentation.Instrumentation;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

public class TestEventHubsMessageChannelBinder extends EventHubsMessageChannelBinder {

    private DefaultMessageHandler messageHandler;
    private MessageProducer messageProducer;
    private EventHubsInboundChannelAdapter inboundAdapter;

    public TestEventHubsMessageChannelBinder(String[] headersToEmbed,
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
        MessageProducer producer;
        if (messageProducer == null) {
            producer = super.createConsumerEndpoint(destination, group, properties);
        } else {
            producer = messageProducer;
        }
        return producer;
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
        producer.markDown(new IllegalArgumentException("Producer exception"));
        getInstrumentationManager().addHealthInstrumentation("producer", producer);
    }

    public void addProcessorDownInstrumentation() {
        DefaultInstrumentation processor = new DefaultInstrumentation("Processor", Instrumentation.Type.PRODUCER);
        processor.markDown(new IllegalArgumentException("Processor exception"));
        getInstrumentationManager().addHealthInstrumentation("Processor", processor);
    }
}
