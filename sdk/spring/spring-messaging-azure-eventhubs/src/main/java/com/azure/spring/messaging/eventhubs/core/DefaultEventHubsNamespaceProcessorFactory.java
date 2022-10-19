// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsErrorHandler;
import com.azure.spring.cloud.service.implementation.eventhubs.factory.EventProcessorClientBuilderFactory;
import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.eventhubs.core.properties.EventHubsContainerProperties;
import com.azure.spring.messaging.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.messaging.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.messaging.eventhubs.implementation.properties.merger.ProcessorPropertiesMerger;
import com.azure.spring.messaging.eventhubs.implementation.properties.merger.ProcessorPropertiesParentMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The {@link EventHubsProcessorFactory} implementation to produce new {@link EventProcessorClient} instances
 * for provided {@link CheckpointStore} {@code checkpointStore} and optional {@link NamespaceProperties} and
 * processor {@link PropertiesSupplier} on each {@link #createProcessor} invocation.
 *
 * <p>
 * The created {@link EventProcessorClient}s are cached according to the event hub names and consumer groups.
 * </p>
 * <p>
 * {@link EventProcessorClient} produced by this factory will share the same namespace level configuration, but if a
 * configuration entry is provided at both processor and namespace level, the processor level configuration will take
 * advantage.
 * </p>
 */
public final class DefaultEventHubsNamespaceProcessorFactory implements EventHubsProcessorFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventHubsNamespaceProcessorFactory.class);

    private final List<Listener> listeners = new ArrayList<>();
    private final NamespaceProperties namespaceProperties;
    private final CheckpointStore checkpointStore;
    private final PropertiesSupplier<ConsumerIdentifier, ProcessorProperties> propertiesSupplier;
    private final Map<ConsumerIdentifier, EventProcessorClient> processorClientMap = new ConcurrentHashMap<>();
    private final List<AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder>> customizers = new ArrayList<>();
    private final Map<String, List<AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder>>> dedicatedCustomizers = new HashMap<>();
    private AzureCredentialResolver<TokenCredential> tokenCredentialResolver = null;
    private TokenCredential defaultCredential = null;

    /**
     * Construct a factory with the provided {@link CheckpointStore}.
     * @param checkpointStore the checkpoint store.
     */
    public DefaultEventHubsNamespaceProcessorFactory(CheckpointStore checkpointStore) {
        this(checkpointStore, null, null);
    }

    /**
     * Construct a factory with the provided {@link CheckpointStore} and namespace level properties.
     * @param checkpointStore the checkpoint store.
     * @param namespaceProperties the namespace properties.
     */
    public DefaultEventHubsNamespaceProcessorFactory(CheckpointStore checkpointStore,
                                                     NamespaceProperties namespaceProperties) {
        this(checkpointStore, namespaceProperties, key -> null);
    }

    /**
     * Construct a factory with the provided {@link CheckpointStore} and processor {@link PropertiesSupplier}.
     * @param checkpointStore the checkpoint store.
     * @param supplier the {@link PropertiesSupplier} to supply {@link ProcessorProperties} for each event hub.
     */
    public DefaultEventHubsNamespaceProcessorFactory(CheckpointStore checkpointStore,
                                                     PropertiesSupplier<ConsumerIdentifier, ProcessorProperties> supplier) {
        this(checkpointStore, null, supplier);
    }

    /**
     * Construct a factory with the provided {@link CheckpointStore}, namespace level properties and processor {@link PropertiesSupplier}.
     * @param checkpointStore the checkpoint store.
     * @param namespaceProperties the namespace properties.
     * @param supplier the {@link PropertiesSupplier} to supply {@link ProcessorProperties} for each event hub.
     */
    public DefaultEventHubsNamespaceProcessorFactory(CheckpointStore checkpointStore,
                                                     NamespaceProperties namespaceProperties,
                                                     PropertiesSupplier<ConsumerIdentifier, ProcessorProperties> supplier) {
        Assert.notNull(checkpointStore, "CheckpointStore must be provided.");
        this.checkpointStore = checkpointStore;
        this.namespaceProperties = namespaceProperties;
        this.propertiesSupplier = supplier == null ? key -> null : supplier;
    }

    @Override
    public EventProcessorClient createProcessor(@NonNull String eventHub, @NonNull String consumerGroup,
                                                @NonNull MessageListener<?> listener,
                                                @NonNull EventHubsErrorHandler errorHandler) {
        return doCreateProcessor(eventHub, consumerGroup, listener, errorHandler, null, null,
            this.propertiesSupplier.getProperties(new ConsumerIdentifier(eventHub, consumerGroup)));
    }

    @Override
    public EventProcessorClient createProcessor(String eventHub, String consumerGroup, EventHubsContainerProperties containerProperties) {
        ProcessorProperties propertiesSupplied = this.propertiesSupplier.getProperties(new ConsumerIdentifier(eventHub,
            consumerGroup));

        ProcessorPropertiesMerger propertiesMerger = new ProcessorPropertiesMerger();
        ProcessorProperties processorProperties = propertiesMerger.merge(containerProperties, propertiesSupplied);

        EventHubsErrorHandler errorHandler = containerProperties.getErrorHandler();
        MessageListener<?> messageListener = containerProperties.getMessageListener();

        Assert.notNull(errorHandler, "A error handler must be provided!");
        Assert.notNull(messageListener, "A message listener consumer must be provided!");

        return doCreateProcessor(eventHub, consumerGroup, messageListener, errorHandler,
            containerProperties.getInitializationContextConsumer(),
            containerProperties.getCloseContextConsumer(),
            processorProperties);
    }

    @Override
    public void destroy() {
        this.processorClientMap.forEach((t, client) -> {
            listeners.forEach(l -> l.processorRemoved(t.getDestination(), t.getGroup(), client));
            client.stop();
        });
        this.processorClientMap.clear();
        this.listeners.clear();
    }

    private EventProcessorClient doCreateProcessor(@NonNull String eventHub, @NonNull String consumerGroup,
                                                   @NonNull MessageListener<?> messageListener,
                                                   @NonNull EventHubsErrorHandler errorHandler,
                                                   @Nullable Consumer<InitializationContext> initializationContextConsumer,
                                                   @Nullable Consumer<CloseContext> closeContextConsumer,
                                                   @Nullable ProcessorProperties properties) {
        ConsumerIdentifier key = new ConsumerIdentifier(eventHub, consumerGroup);
        return processorClientMap.computeIfAbsent(key, k -> {

            ProcessorPropertiesParentMerger propertiesParentMerger = new ProcessorPropertiesParentMerger();
            ProcessorProperties processorProperties = propertiesParentMerger.merge(properties, this.namespaceProperties);
            processorProperties.setEventHubName(k.getDestination());
            processorProperties.setConsumerGroup(k.getGroup());

            EventProcessorClientBuilderFactory factory = new EventProcessorClientBuilderFactory(
                processorProperties, this.checkpointStore, messageListener, errorHandler);

            factory.setCloseContextConsumer(closeContextConsumer);
            factory.setInitializationContextConsumer(initializationContextConsumer);

            factory.setDefaultTokenCredential(this.defaultCredential);
            factory.setTokenCredentialResolver(this.tokenCredentialResolver);

            factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_EVENT_HUBS);
            EventProcessorClientBuilder builder = factory.build();
            customizeBuilder(eventHub, consumerGroup, builder);
            EventProcessorClient client = builder.buildEventProcessorClient();
            LOGGER.info("EventProcessor created for event hub '{}' with consumer group '{}'", k.getDestination(), k.getGroup());

            this.listeners.forEach(l -> l.processorAdded(k.getDestination(), k.getGroup(), client));

            return client;
        });
    }

    @Override
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    @Override
    public boolean removeListener(Listener listener) {
        return this.listeners.remove(listener);
    }

    /**
     * Set the token credential resolver.
     * @param tokenCredentialResolver The token credential resolver.
     */
    public void setTokenCredentialResolver(AzureCredentialResolver<TokenCredential> tokenCredentialResolver) {
        this.tokenCredentialResolver = tokenCredentialResolver;
    }

    /**
     * Set the default credential for all clients generated from this factory.
     *
     * @param defaultCredential The default credential.
     */
    public void setDefaultCredential(TokenCredential defaultCredential) {
        this.defaultCredential = defaultCredential;
    }

    /**
     * Add a service client builder customizer to customize all the clients created from this factory.
     * @param customizer the provided customizer.
     */
    public void addBuilderCustomizer(AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder> customizer) {
        if (customizer == null) {
            LOGGER.debug("The provided customizer is null, will ignore it.");
            return;
        }
        this.customizers.add(customizer);
    }

    /**
     * Add a service client builder customizer to customize the clients created from this factory with event hub name of
     * value {@code eventHub} and consumer group of value {@code consumerGroup}.
     * @param eventHub the event hub name of the client.
     * @param consumerGroup the consumer group of the client.
     * @param customizer the provided customizer.
     */
    public void addBuilderCustomizer(String eventHub, String consumerGroup, AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder> customizer) {
        if (customizer == null) {
            LOGGER.debug("The provided customizer is null, will ignore it.");
            return;
        }
        this.dedicatedCustomizers
            .computeIfAbsent(getCustomizerKey(eventHub, consumerGroup), key -> new ArrayList<>())
            .add(customizer);
    }

    private void customizeBuilder(String eventHub, String consumerGroup, EventProcessorClientBuilder builder) {
        this.customizers.forEach(customizer -> customizer.customize(builder));
        this.dedicatedCustomizers.getOrDefault(getCustomizerKey(eventHub, consumerGroup), new ArrayList<>())
                                 .forEach(customizer -> customizer.customize(builder));
    }

    private String getCustomizerKey(String eventHub, String consumerGroup) {
        return eventHub + "_" + consumerGroup;
    }

}
