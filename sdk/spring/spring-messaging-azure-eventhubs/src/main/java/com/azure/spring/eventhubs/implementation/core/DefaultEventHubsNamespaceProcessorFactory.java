// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.implementation.core;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.core.credential.AzureCredentialResolver;
import com.azure.spring.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.eventhubs.core.properties.EventHubsContainerProperties;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.eventhubs.implementation.properties.merger.ProcessorPropertiesMerger;
import com.azure.spring.eventhubs.implementation.properties.merger.ProcessorPropertiesParentMerger;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.service.eventhubs.processor.EventHubsMessageListener;
import com.azure.spring.service.eventhubs.processor.consumer.EventProcessorCloseContextConsumer;
import com.azure.spring.service.eventhubs.processor.consumer.EventProcessorErrorContextConsumer;
import com.azure.spring.service.eventhubs.processor.consumer.EventProcessorInitializationContextConsumer;
import com.azure.spring.service.implementation.eventhubs.factory.EventProcessorClientBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private AzureCredentialResolver<TokenCredential> tokenCredentialResolver = null;
    private DefaultAzureCredential defaultAzureCredential = null;

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
                                                @NonNull EventHubsMessageListener listener,
                                                @NonNull EventProcessorErrorContextConsumer errorContextConsumer) {
        return doCreateProcessor(eventHub, consumerGroup, listener, errorContextConsumer, null, null,
            this.propertiesSupplier.getProperties(new ConsumerIdentifier(eventHub, consumerGroup)));
    }

    @Override
    public EventProcessorClient createProcessor(String eventHub, String consumerGroup, EventHubsContainerProperties containerProperties) {
        ProcessorProperties propertiesSupplied = this.propertiesSupplier.getProperties(new ConsumerIdentifier(eventHub,
            consumerGroup));

        ProcessorPropertiesMerger propertiesMerger = new ProcessorPropertiesMerger();
        ProcessorProperties processorProperties = propertiesMerger.merge(containerProperties, propertiesSupplied);

        EventProcessorErrorContextConsumer errorContextConsumer = containerProperties.getErrorContextConsumer();
        EventHubsMessageListener messageListener = containerProperties.getMessageListener();

        Assert.notNull(errorContextConsumer, "A error context consumer must be provided!");
        Assert.notNull(messageListener, "A message listener consumer must be provided!");

        return doCreateProcessor(eventHub, consumerGroup, messageListener, errorContextConsumer,
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
                                                   @NonNull EventHubsMessageListener messageListener,
                                                   @NonNull EventProcessorErrorContextConsumer errorContextConsumer,
                                                   @Nullable EventProcessorInitializationContextConsumer initializationContextConsumer,
                                                   @Nullable EventProcessorCloseContextConsumer closeContextConsumer,
                                                   @Nullable ProcessorProperties properties) {
        ConsumerIdentifier key = new ConsumerIdentifier(eventHub, consumerGroup);
        return processorClientMap.computeIfAbsent(key, k -> {

            ProcessorPropertiesParentMerger propertiesParentMerger = new ProcessorPropertiesParentMerger();
            ProcessorProperties processorProperties = propertiesParentMerger.merge(properties, this.namespaceProperties);
            processorProperties.setEventHubName(k.getDestination());
            processorProperties.setConsumerGroup(k.getGroup());

            EventProcessorClientBuilderFactory factory = new EventProcessorClientBuilderFactory(
                processorProperties, this.checkpointStore, messageListener, errorContextConsumer);

            factory.setCloseContextConsumer(closeContextConsumer);
            factory.setInitializationContextConsumer(initializationContextConsumer);

            factory.setDefaultTokenCredential(this.defaultAzureCredential);
            factory.setTokenCredentialResolver(this.tokenCredentialResolver);

            factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_EVENT_HUBS);
            EventProcessorClient client = factory.build().buildEventProcessorClient();
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
     * Set the default Azure credential.
     * @param defaultAzureCredential The default Azure Credential.
     */
    public void setDefaultAzureCredential(DefaultAzureCredential defaultAzureCredential) {
        this.defaultAzureCredential = defaultAzureCredential;
    }

}
