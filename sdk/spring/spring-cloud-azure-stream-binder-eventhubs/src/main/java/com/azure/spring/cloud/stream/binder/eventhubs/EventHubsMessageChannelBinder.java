// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.eventhubs.core.EventHubsProcessorContainer;
import com.azure.spring.eventhubs.core.EventHubsTemplate;
import com.azure.spring.eventhubs.core.processor.DefaultEventHubsNamespaceProcessorFactory;
import com.azure.spring.eventhubs.core.producer.DefaultEventHubsNamespaceProducerFactory;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.eventhubs.core.properties.ProducerProperties;
import com.azure.spring.integration.eventhubs.inbound.EventHubsInboundChannelAdapter;
import com.azure.spring.integration.eventhubs.inbound.health.EventHusProcessorInstrumentation;
import com.azure.spring.integration.handler.DefaultMessageHandler;
import com.azure.spring.integration.instrumentation.DefaultInstrumentation;
import com.azure.spring.integration.instrumentation.DefaultInstrumentationManager;
import com.azure.spring.integration.instrumentation.Instrumentation;
import com.azure.spring.integration.instrumentation.InstrumentationManager;
import com.azure.spring.integration.instrumentation.InstrumentationSendCallback;
import com.azure.spring.messaging.ListenerMode;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.PubSubPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.spring.integration.instrumentation.Instrumentation.Type.CONSUMER;
import static com.azure.spring.integration.instrumentation.Instrumentation.Type.PRODUCER;

/**
 *
 */
public class EventHubsMessageChannelBinder extends
    // @formatter:off
    AbstractMessageChannelBinder<ExtendedConsumerProperties<EventHubsConsumerProperties>, ExtendedProducerProperties<EventHubsProducerProperties>, EventHubsChannelProvisioner>
    // @formatter:on
    implements
    ExtendedPropertiesBinder<MessageChannel, EventHubsConsumerProperties, EventHubsProducerProperties> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsMessageChannelBinder.class);
    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private NamespaceProperties namespaceProperties;
    private EventHubsTemplate eventHubsTemplate;
    private CheckpointStore checkpointStore;
    private EventHubsProcessorContainer processorContainer;
    private final InstrumentationManager instrumentationManager = new DefaultInstrumentationManager();
    private EventHubsExtendedBindingProperties bindingProperties = new EventHubsExtendedBindingProperties();
    private final Map<String, ExtendedProducerProperties<EventHubsProducerProperties>>
        extendedProducerPropertiesMap = new ConcurrentHashMap<>();
    private final Map<PubSubPair, ExtendedConsumerProperties<EventHubsConsumerProperties>>
        extendedConsumerPropertiesMap = new ConcurrentHashMap<>();

    /**
     * Construct a {@link EventHubsMessageChannelBinder} with the specified headers to embed and {@link EventHubsChannelProvisioner}.
     *
     * @param headersToEmbed the headers to embed
     * @param provisioningProvider the provisioning provider
     */
    public EventHubsMessageChannelBinder(String[] headersToEmbed, EventHubsChannelProvisioner provisioningProvider) {
        super(headersToEmbed, provisioningProvider);
    }

    @Override
    protected MessageHandler createProducerMessageHandler(
        ProducerDestination destination,
        ExtendedProducerProperties<EventHubsProducerProperties> producerProperties,
        MessageChannel errorChannel) {
        extendedProducerPropertiesMap.put(destination.getName(), producerProperties);
        Assert.notNull(getEventHubTemplate(), "eventHubsTemplate can't be null when create a producer");

        DefaultMessageHandler handler = new DefaultMessageHandler(destination.getName(), this.eventHubsTemplate);

        handler.setBeanFactory(getBeanFactory());
        handler.setSync(producerProperties.getExtension().isSync());
        handler.setSendTimeout(producerProperties.getExtension().getSendTimeout());
        handler.setSendFailureChannel(errorChannel);

        String instrumentationId = Instrumentation.buildId(PRODUCER, destination.getName());
        handler.setSendCallback(new InstrumentationSendCallback(instrumentationId, instrumentationManager));

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
                                                     ExtendedConsumerProperties<EventHubsConsumerProperties> properties) {
        extendedConsumerPropertiesMap.put(PubSubPair.of(destination.getName(), group), properties);
        Assert.notNull(getProcessorContainer(), "eventProcessorsContainer can't be null when create a consumer");

        boolean anonymous = !StringUtils.hasText(group);
        if (anonymous) {
            group = "anonymous." + UUID.randomUUID();
        }

        EventHubsInboundChannelAdapter inboundAdapter;
        if (properties.isBatchMode()) {
            inboundAdapter = new EventHubsInboundChannelAdapter(this.processorContainer,
                destination.getName(), group, ListenerMode.BATCH, properties.getExtension().getCheckpoint());
        } else {
            inboundAdapter = new EventHubsInboundChannelAdapter(this.processorContainer,
                destination.getName(), group, properties.getExtension().getCheckpoint());
        }
        inboundAdapter.setBeanFactory(getBeanFactory());
        String instrumentationId = Instrumentation.buildId(CONSUMER, destination.getName() + "/" +  group);
        inboundAdapter.setInstrumentationManager(instrumentationManager);
        inboundAdapter.setInstrumentationId(instrumentationId);
        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, group, properties);
        inboundAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());

        return inboundAdapter;
    }

    @Override
    public EventHubsConsumerProperties getExtendedConsumerProperties(String destination) {
        return this.bindingProperties.getExtendedConsumerProperties(destination);
    }

    @Override
    public EventHubsProducerProperties getExtendedProducerProperties(String destination) {
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

    /**
     * Set binding properties.
     *
     * @param bindingProperties the binding properties
     */
    public void setBindingProperties(EventHubsExtendedBindingProperties bindingProperties) {
        this.bindingProperties = bindingProperties;
    }

    private PropertiesSupplier<String, ProducerProperties> getProducerPropertiesSupplier() {
        return key -> {
            if (this.extendedProducerPropertiesMap.containsKey(key)) {
                EventHubsProducerProperties producerProperties = this.extendedProducerPropertiesMap.get(key)
                    .getExtension();
                producerProperties.setEventHubName(key);
                return producerProperties;
            } else {
                LOGGER.debug("Can't find extended properties for {}", key);
                return null;
            }
        };
    }

    private PropertiesSupplier<PubSubPair, ProcessorProperties> getProcessorPropertiesSupplier() {
        return key -> {
            if (this.extendedConsumerPropertiesMap.containsKey(key)) {
                EventHubsConsumerProperties consumerProperties = this.extendedConsumerPropertiesMap.get(key)
                    .getExtension();
                consumerProperties.setEventHubName(key.getPublisher());
                consumerProperties.setConsumerGroup(key.getSubscriber());
                return consumerProperties;
            } else {
                LOGGER.debug("Can't find extended properties for destination {}, group {}", key.getPublisher(), key.getSubscriber());
                return null;
            }
        };
    }

    private EventHubsTemplate getEventHubTemplate() {
        if (this.eventHubsTemplate == null) {
            DefaultEventHubsNamespaceProducerFactory factory = new DefaultEventHubsNamespaceProducerFactory(
                this.namespaceProperties, getProducerPropertiesSupplier());
            factory.addListener((name, producerAsyncClient) -> {
                DefaultInstrumentation instrumentation = new DefaultInstrumentation(name, PRODUCER);
                instrumentation.markUp();
                instrumentationManager.addHealthInstrumentation(instrumentation.getId(), instrumentation);
            });
            this.eventHubsTemplate = new EventHubsTemplate(factory);
        }
        return this.eventHubsTemplate;
    }

    private EventHubsProcessorContainer getProcessorContainer() {
        if (this.processorContainer == null) {
            DefaultEventHubsNamespaceProcessorFactory factory = new DefaultEventHubsNamespaceProcessorFactory(
                this.checkpointStore, this.namespaceProperties, getProcessorPropertiesSupplier());
            factory.addListener((name, consumerGroup, processorClient) -> {
                String instrumentationName = name + "/" + consumerGroup;
                Instrumentation instrumentation = new EventHusProcessorInstrumentation(instrumentationName, CONSUMER, Duration.ofMinutes(2));
                instrumentation.markUp();
                instrumentationManager.addHealthInstrumentation(instrumentation.getId(), instrumentation);
            });
            this.processorContainer = new EventHubsProcessorContainer(factory);
        }
        return this.processorContainer;
    }

    /**
     * Set namespace properties.
     *
     * @param namespaceProperties the namespace properties
     */
    public void setNamespaceProperties(NamespaceProperties namespaceProperties) {
        this.namespaceProperties = namespaceProperties;
    }

    /**
     * Set checkpoint store.
     *
     * @param checkpointStore the checkpoint store
     */
    public void setCheckpointStore(CheckpointStore checkpointStore) {
        this.checkpointStore = checkpointStore;
    }

    /**
     * Get instrumentation manager.
     *
     * @return instrumentationManager the instrumentation manager
     * @see InstrumentationManager
     */
    public InstrumentationManager getInstrumentationManager() {
        return instrumentationManager;
    }
}
