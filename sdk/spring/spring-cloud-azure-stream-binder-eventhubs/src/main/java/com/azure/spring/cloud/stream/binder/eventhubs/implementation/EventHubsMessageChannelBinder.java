// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.implementation;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.stream.binder.eventhubs.config.EventHubsProcessorFactoryCustomizer;
import com.azure.spring.cloud.stream.binder.eventhubs.config.EventHubsProducerFactoryCustomizer;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.implementation.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.integration.core.handler.DefaultMessageHandler;
import com.azure.spring.integration.core.implementation.instrumentation.DefaultInstrumentation;
import com.azure.spring.integration.core.implementation.instrumentation.DefaultInstrumentationManager;
import com.azure.spring.integration.core.implementation.instrumentation.InstrumentationSendCallback;
import com.azure.spring.integration.core.instrumentation.Instrumentation;
import com.azure.spring.integration.core.instrumentation.InstrumentationManager;
import com.azure.spring.integration.eventhubs.implementation.health.EventHubsProcessorInstrumentation;
import com.azure.spring.integration.eventhubs.inbound.EventHubsInboundChannelAdapter;
import com.azure.spring.messaging.ListenerMode;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.EventHubsTemplate;
import com.azure.spring.messaging.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.messaging.eventhubs.core.properties.EventHubsContainerProperties;
import com.azure.spring.messaging.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.messaging.eventhubs.core.properties.ProducerProperties;
import com.azure.spring.messaging.eventhubs.core.DefaultEventHubsNamespaceProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.DefaultEventHubsNamespaceProducerFactory;
import com.azure.spring.messaging.eventhubs.implementation.properties.merger.ProcessorPropertiesMerger;
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
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.spring.integration.core.instrumentation.Instrumentation.Type.CONSUMER;
import static com.azure.spring.integration.core.instrumentation.Instrumentation.Type.PRODUCER;

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
    private DefaultEventHubsNamespaceProcessorFactory processorFactory;
    private final List<EventHubsMessageListenerContainer> eventHubsMessageListenerContainers = new ArrayList<>();
    private final InstrumentationManager instrumentationManager = new DefaultInstrumentationManager();
    private EventHubsExtendedBindingProperties bindingProperties = new EventHubsExtendedBindingProperties();
    private final Map<String, ExtendedProducerProperties<EventHubsProducerProperties>>
        extendedProducerPropertiesMap = new ConcurrentHashMap<>();

    private final List<EventHubsProducerFactoryCustomizer> producerFactoryCustomizers = new ArrayList<>();
    private final List<EventHubsProcessorFactoryCustomizer> processorFactoryCustomizers = new ArrayList<>();

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
        handler.setSendTimeout(producerProperties.getExtension().getSendTimeout().toMillis());
        handler.setSendFailureChannel(errorChannel);

        String instrumentationId = Instrumentation.buildId(PRODUCER, destination.getName());
        handler.setSendCallback(new InstrumentationSendCallback(instrumentationId, instrumentationManager));

        if (producerProperties.isPartitioned()) {
            handler.setPartitionIdExpression(
                EXPRESSION_PARSER.parseExpression("headers['" + BinderHeaders.PARTITION_HEADER + "']"));
        }
        return handler;
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
                                                     ExtendedConsumerProperties<EventHubsConsumerProperties> properties) {
        Assert.notNull(getProcessorFactory(), "processor factory can't be null when create a consumer");

        boolean anonymous = !StringUtils.hasText(group);
        if (anonymous) {
            group = "anonymous." + UUID.randomUUID();
        }

        EventHubsContainerProperties containerProperties = createContainerProperties(destination, group, properties);
        EventHubsMessageListenerContainer listenerContainer = new EventHubsMessageListenerContainer(
            getProcessorFactory(), containerProperties);

        this.eventHubsMessageListenerContainers.add(listenerContainer);

        EventHubsInboundChannelAdapter inboundAdapter;
        if (properties.isBatchMode()) {
            inboundAdapter = new EventHubsInboundChannelAdapter(listenerContainer, ListenerMode.BATCH);
        } else {
            inboundAdapter = new EventHubsInboundChannelAdapter(listenerContainer);
        }
        inboundAdapter.setBeanFactory(getBeanFactory());
        String instrumentationId = Instrumentation.buildId(CONSUMER, destination.getName() + "/" +  group);
        inboundAdapter.setInstrumentationManager(instrumentationManager);
        inboundAdapter.setInstrumentationId(instrumentationId);
        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, group, properties);
        inboundAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());

        return inboundAdapter;
    }

    /**
     * Create {@link EventHubsContainerProperties} from the extended {@link EventHubsConsumerProperties}.
     * @param destination reference to the consumer destination.
     * @param group the consumer group.
     * @param properties the consumer properties.
     * @return the {@link EventHubsContainerProperties}.
     */
    private EventHubsContainerProperties createContainerProperties(
        ConsumerDestination destination,
        String group,
        ExtendedConsumerProperties<EventHubsConsumerProperties> properties) {
        EventHubsContainerProperties containerProperties = new EventHubsContainerProperties();
        AzurePropertiesUtils.copyAzureCommonProperties(properties.getExtension(), containerProperties);
        ProcessorPropertiesMerger.copyProcessorPropertiesIfNotNull(properties.getExtension(), containerProperties);
        containerProperties.setEventHubName(destination.getName());
        containerProperties.setConsumerGroup(group);
        containerProperties.setCheckpointConfig(properties.getExtension().getCheckpoint());
        return containerProperties;
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

    private EventHubsTemplate getEventHubTemplate() {
        if (this.eventHubsTemplate == null) {
            DefaultEventHubsNamespaceProducerFactory factory = new DefaultEventHubsNamespaceProducerFactory(
                this.namespaceProperties, getProducerPropertiesSupplier());

            producerFactoryCustomizers.forEach(customizer -> customizer.customize(factory));

            factory.addListener((name, producerAsyncClient) -> {
                DefaultInstrumentation instrumentation = new DefaultInstrumentation(name, PRODUCER);
                instrumentation.setStatus(Instrumentation.Status.UP);
                instrumentationManager.addHealthInstrumentation(instrumentation);
            });
            this.eventHubsTemplate = new EventHubsTemplate(factory);
        }
        return this.eventHubsTemplate;
    }

    private EventHubsProcessorFactory getProcessorFactory() {
        if (this.processorFactory == null) {
            this.processorFactory = new DefaultEventHubsNamespaceProcessorFactory(
                this.checkpointStore, this.namespaceProperties);

            processorFactoryCustomizers.forEach(customizer -> customizer.customize(processorFactory));

            processorFactory.addListener((name, consumerGroup, processorClient) -> {
                String instrumentationName = name + "/" + consumerGroup;
                Instrumentation instrumentation = new EventHubsProcessorInstrumentation(instrumentationName, CONSUMER, Duration.ofMinutes(2));
                instrumentation.setStatus(Instrumentation.Status.UP);
                instrumentationManager.addHealthInstrumentation(instrumentation);
            });
        }
        return this.processorFactory;
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
    InstrumentationManager getInstrumentationManager() {
        return instrumentationManager;
    }

    /**
     * Add a producer factory customizer.
     *
     * @param producerFactoryCustomizer The producer factory customizer to add.
     */
    public void addProducerFactoryCustomizer(EventHubsProducerFactoryCustomizer producerFactoryCustomizer) {
        if (producerFactoryCustomizer != null) {
            this.producerFactoryCustomizers.add(producerFactoryCustomizer);
        }
    }

    /**
     * Add a processor factory customizer.
     *
     * @param processorFactoryCustomizer The processor factory customizer to add.
     */
    public void addProcessorFactoryCustomizer(EventHubsProcessorFactoryCustomizer processorFactoryCustomizer) {
        if (processorFactoryCustomizer != null) {
            this.processorFactoryCustomizers.add(processorFactoryCustomizer);
        }
    }

}
