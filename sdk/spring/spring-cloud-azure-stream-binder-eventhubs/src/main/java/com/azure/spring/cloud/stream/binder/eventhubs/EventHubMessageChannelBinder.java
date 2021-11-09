// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubChannelProvisioner;
import com.azure.spring.eventhubs.core.EventHubProcessorContainer;
import com.azure.spring.eventhubs.core.EventHubsTemplate;
import com.azure.spring.eventhubs.core.processor.DefaultEventHubNamespaceProcessorFactory;
import com.azure.spring.eventhubs.core.producer.DefaultEventHubNamespaceProducerFactory;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.eventhubs.core.properties.ProducerProperties;
import com.azure.spring.integration.eventhubs.inbound.EventHubInboundChannelAdapter;
import com.azure.spring.integration.handler.DefaultMessageHandler;
import com.azure.spring.messaging.PropertiesSupplier;
import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.expression.FunctionExpression;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class EventHubMessageChannelBinder extends
    // @formatter:off
    AbstractMessageChannelBinder<ExtendedConsumerProperties<EventHubConsumerProperties>, ExtendedProducerProperties<EventHubProducerProperties>, EventHubChannelProvisioner>
    // @formatter:on
    implements
    ExtendedPropertiesBinder<MessageChannel, EventHubConsumerProperties, EventHubProducerProperties> {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private NamespaceProperties namespaceProperties;
    private EventHubsTemplate eventHubsTemplate;
    private CheckpointStore checkpointStore;
    private EventHubProcessorContainer processorContainer;
    private EventHubExtendedBindingProperties bindingProperties = new EventHubExtendedBindingProperties();

    private final Map<String, EventHubInformation> eventHubsInUse = new ConcurrentHashMap<>();

    public EventHubMessageChannelBinder(String[] headersToEmbed, EventHubChannelProvisioner provisioningProvider) {
        super(headersToEmbed, provisioningProvider);
    }

    @Override
    protected MessageHandler createProducerMessageHandler(
        ProducerDestination destination,
        ExtendedProducerProperties<EventHubProducerProperties> producerProperties,
        MessageChannel errorChannel) {
        Assert.notNull(getEventHubTemplate(), "eventHubsTemplate can't be null when create a producer");

        eventHubsInUse.put(destination.getName(), new EventHubInformation(null));

        DefaultMessageHandler handler = new DefaultMessageHandler(destination.getName(), this.eventHubsTemplate);

        handler.setBeanFactory(getBeanFactory());
        handler.setSync(producerProperties.getExtension().isSync());
        handler.setSendTimeout(producerProperties.getExtension().getSendTimeout());
        handler.setSendFailureChannel(errorChannel);
        handler.setBatchSendingConfig(producerProperties.getExtension().getBatchConfig());

        if (producerProperties.isPartitioned()) {
            handler.setPartitionIdExpression(
                EXPRESSION_PARSER.parseExpression("headers['" + BinderHeaders.PARTITION_HEADER + "']"));
        } else {
            handler.setPartitionKeyExpression(new FunctionExpression<Message<?>>(m -> m.getPayload().hashCode()));
        }
        return handler;
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
            ExtendedConsumerProperties<EventHubConsumerProperties> properties) {
        Assert.notNull(getProcessorContainer(), "eventProcessorsContainer can't be null when create a consumer");

        eventHubsInUse.put(destination.getName(), new EventHubInformation(group));

        boolean anonymous = !StringUtils.hasText(group);
        if (anonymous) {
            group = "anonymous." + UUID.randomUUID();
        }

        EventHubInboundChannelAdapter inboundAdapter = new EventHubInboundChannelAdapter(this.processorContainer,
            destination.getName(), group, properties.getExtension().getCheckpoint());

        inboundAdapter.setBeanFactory(getBeanFactory());

        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, group, properties);
        inboundAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());

        return inboundAdapter;
    }

    @Override
    public EventHubConsumerProperties getExtendedConsumerProperties(String destination) {
        return this.bindingProperties.getExtendedConsumerProperties(destination);
    }

    @Override
    public EventHubProducerProperties getExtendedProducerProperties(String destination) {
        return this.bindingProperties.getExtendedProducerProperties(destination);
    }

    @Override
    public String getDefaultsPrefix() {
        return this.bindingProperties.getDefaultsPrefix();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return this.bindingProperties.getExtendedPropertiesEntryClass();
    }

    public void setBindingProperties(EventHubExtendedBindingProperties bindingProperties) {
        this.bindingProperties = bindingProperties;
    }

    Map<String, EventHubInformation> getEventHubsInUse() {
        return eventHubsInUse;
    }

    static class EventHubInformation {

        private final String consumerGroup;

        EventHubInformation(String consumerGroup) {
            this.consumerGroup = consumerGroup;
        }

        public String getConsumerGroup() {
            return consumerGroup;
        }
    }

    private PropertiesSupplier<String, ProducerProperties> getProducerPropertiesSupplier() {
        return key -> {
            Map<String, EventHubBindingProperties> bindings = bindingProperties.getBindings();
            for (Map.Entry<String, EventHubBindingProperties> entry : bindings.entrySet()) {
                ProducerProperties properties = bindings.get(entry.getKey()).getProducer().getProducer();
                if (properties.getEventHubName() == null) {
                    continue;
                }
                if (key.equalsIgnoreCase(properties.getEventHubName())) {
                    return properties;
                }
            }
            return null;
        };
    }

    private PropertiesSupplier<Tuple2<String, String>, ProcessorProperties> getProcessorPropertiesSupplier() {
        return key -> {
            Map<String, EventHubBindingProperties> bindings = bindingProperties.getBindings();
            for (Map.Entry<String, EventHubBindingProperties> entry : bindings.entrySet()) {
                ProcessorProperties properties = bindings.get(entry.getKey()).getConsumer().getProcessor();
                if (properties.getEventHubName() == null || properties.getConsumerGroup() == null) {
                    continue;
                }
                if (key.equals(Tuples.of(properties.getEventHubName(), properties.getConsumerGroup()))) {
                    return properties;
                }
            }
            return null;
        };
    }

    private EventHubsTemplate getEventHubTemplate() {
        if (this.eventHubsTemplate == null) {
            this.eventHubsTemplate = new EventHubsTemplate(new DefaultEventHubNamespaceProducerFactory(this.namespaceProperties, getProducerPropertiesSupplier()));
        }
        return this.eventHubsTemplate;
    }

    private EventHubProcessorContainer getProcessorContainer() {
        if (this.processorContainer == null) {
            this.processorContainer = new EventHubProcessorContainer(new DefaultEventHubNamespaceProcessorFactory(this.checkpointStore, this.namespaceProperties, getProcessorPropertiesSupplier()));
        }
        return this.processorContainer;
    }

    public void setNamespaceProperties(NamespaceProperties namespaceProperties) {
        this.namespaceProperties = namespaceProperties;
    }

    public void setCheckpointStore(CheckpointStore checkpointStore) {
        this.checkpointStore = checkpointStore;
    }
}
