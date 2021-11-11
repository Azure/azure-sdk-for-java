// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.processor;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.eventhubs.core.properties.merger.ProcessorPropertiesParentMerger;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.service.eventhubs.factory.EventProcessorClientBuilderFactory;
import com.azure.spring.service.eventhubs.processor.EventProcessingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link EventProcessorClient} produced by this factory will share the same namespace level configuration, but if a
 * configuration entry is provided at both processor and namespace level, the processor level configuration will take
 * advantage.
 */
public class DefaultEventHubNamespaceProcessorFactory implements EventHubProcessorFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventHubNamespaceProcessorFactory.class);

    private final List<Listener> listeners = new ArrayList<>();
    private final NamespaceProperties namespaceProperties;
    private final CheckpointStore checkpointStore;
    private final PropertiesSupplier<Tuple2<String, String>, ProcessorProperties> propertiesSupplier;
    private final Map<Tuple2<String, String>, EventProcessorClient> processorClientMap = new ConcurrentHashMap<>();
    private final ProcessorPropertiesParentMerger propertiesMerger = new ProcessorPropertiesParentMerger();

    public DefaultEventHubNamespaceProcessorFactory(CheckpointStore checkpointStore) {
        this(checkpointStore, null, null);
    }

    public DefaultEventHubNamespaceProcessorFactory(CheckpointStore checkpointStore,
                                                    NamespaceProperties namespaceProperties) {
        this(checkpointStore, namespaceProperties, key -> null);
    }

    public DefaultEventHubNamespaceProcessorFactory(CheckpointStore checkpointStore,
                                                    PropertiesSupplier<Tuple2<String, String>,
                                                        ProcessorProperties> supplier) {
        this(checkpointStore, null, supplier);
    }

    public DefaultEventHubNamespaceProcessorFactory(CheckpointStore checkpointStore,
                                                    NamespaceProperties namespaceProperties,
                                                    PropertiesSupplier<Tuple2<String, String>,
                                                        ProcessorProperties> supplier) {
        Assert.notNull(checkpointStore, "CheckpointStore must be provided.");
        this.checkpointStore = checkpointStore;
        this.namespaceProperties = namespaceProperties;
        this.propertiesSupplier = supplier == null ? key -> null : supplier;
    }

    @Override
    public EventProcessorClient createProcessor(@NonNull String eventHub, @NonNull String consumerGroup,
                                                @NonNull EventProcessingListener listener) {
        return doCreateProcessor(eventHub, consumerGroup, listener,
            this.propertiesSupplier.getProperties(Tuples.of(eventHub, consumerGroup)));
    }

    @Override
    public void destroy() {
        this.processorClientMap.values().forEach(EventProcessorClient::stop);
        this.processorClientMap.clear();
    }

    private EventProcessorClient doCreateProcessor(@NonNull String eventHub, @NonNull String consumerGroup,
                                                   @NonNull EventProcessingListener listener,
                                                   @Nullable ProcessorProperties properties) {
        Tuple2<String, String> key = Tuples.of(eventHub, consumerGroup);
        if (this.processorClientMap.containsKey(key)) {
            return this.processorClientMap.get(key);
        }

        ProcessorProperties processorProperties = propertiesMerger.mergeParent(properties, this.namespaceProperties);
        processorProperties.setEventHubName(eventHub);
        processorProperties.setConsumerGroup(consumerGroup);

        EventProcessorClient client = new EventProcessorClientBuilderFactory(processorProperties, this.checkpointStore,
            listener).build().buildEventProcessorClient();
        LOGGER.info("EventProcessor created for event hub '{}' with consumer group '{}'", eventHub, consumerGroup);

        this.listeners.forEach(l -> l.processorAdded(eventHub, consumerGroup));

        this.processorClientMap.put(key, client);

        return client;
    }

    @Override
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    @Override
    public boolean removeListener(Listener listener) {
        return this.listeners.remove(listener);
    }

}
