// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.implementation.core;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.core.credential.AzureCredentialResolver;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.service.implementation.servicebus.factory.ServiceBusProcessorClientBuilderFactory;
import com.azure.spring.service.implementation.servicebus.factory.ServiceBusSessionProcessorClientBuilderFactory;
import com.azure.spring.service.servicebus.processor.ServiceBusMessageListener;
import com.azure.spring.service.servicebus.processor.consumer.ServiceBusProcessorErrorContextConsumer;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.servicebus.core.properties.ServiceBusContainerProperties;
import com.azure.spring.servicebus.implementation.properties.merger.ProcessorPropertiesMerger;
import com.azure.spring.servicebus.implementation.properties.merger.ProcessorPropertiesParentMerger;
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
import java.util.function.Consumer;


/**
 * The {@link ServiceBusProcessorFactory} implementation to produce new {@link ServiceBusProcessorClient} instances
 * for provided {@link NamespaceProperties} and optional
 * processor {@link PropertiesSupplier} on each {@link #createProcessor} invocation.
 *
 * <p>
 * {@link ServiceBusProcessorClient} produced by this factory will share the same namespace level configuration, but if a
 * configuration entry is provided at both processor and namespace level, the processor level configuration will take
 * advantage.
 * </p>
 */
public final class DefaultServiceBusNamespaceProcessorFactory implements ServiceBusProcessorFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceBusNamespaceProcessorFactory.class);
    private final Map<ConsumerIdentifier, ServiceBusProcessorClient> processorMap = new ConcurrentHashMap<>();
    private final List<Listener> listeners = new ArrayList<>();
    private final NamespaceProperties namespaceProperties;
    private final PropertiesSupplier<ConsumerIdentifier, ProcessorProperties> propertiesSupplier;
    private AzureCredentialResolver<TokenCredential> tokenCredentialResolver = null;
    private DefaultAzureCredential defaultAzureCredential = null;

    /**
     * Construct a factory with the provided namespace level properties.
     * @param namespaceProperties the namespace properties.
     */
    public DefaultServiceBusNamespaceProcessorFactory(NamespaceProperties namespaceProperties) {
        this(namespaceProperties, key -> null);
    }

    /**
     * Construct a factory with the provided namespace level properties and processor {@link PropertiesSupplier}.
     * @param namespaceProperties the namespace properties.
     * @param supplier the {@link PropertiesSupplier} to supply {@link ProcessorProperties} for each queue/topic entity.
     */
    public DefaultServiceBusNamespaceProcessorFactory(NamespaceProperties namespaceProperties,
                                                      PropertiesSupplier<ConsumerIdentifier, ProcessorProperties> supplier) {
        this.namespaceProperties = namespaceProperties;
        this.propertiesSupplier = supplier == null ? key -> null : supplier;
    }

    private void close(Map<ConsumerIdentifier, ServiceBusProcessorClient> map, Consumer<ServiceBusProcessorClient> close) {
        map.forEach((t, p) -> {
            try {
                listeners.forEach(l -> l.processorRemoved(t.getDestination(), t.getGroup(), p));
                close.accept(p);
            } catch (Exception ex) {
                LOGGER.warn("Failed to clean service bus queue client factory", ex);
            }
        });
    }

    @Override
    public void destroy() {
        close(processorMap, ServiceBusProcessorClient::close);
        this.processorMap.clear();
        this.listeners.clear();
    }

    @Override
    public ServiceBusProcessorClient createProcessor(String queue,
                                                     ServiceBusMessageListener messageListener,
                                                     ServiceBusProcessorErrorContextConsumer errorContextConsumer) {
        return doCreateProcessor(queue, null, messageListener, errorContextConsumer, this.propertiesSupplier.getProperties(new ConsumerIdentifier(queue)));
    }

    @Override
    public ServiceBusProcessorClient createProcessor(String queue, ServiceBusContainerProperties containerProperties) {
        ProcessorProperties propertiesSupplied = this.propertiesSupplier.getProperties(new ConsumerIdentifier(queue));
        ProcessorPropertiesMerger propertiesMerger = new ProcessorPropertiesMerger();
        ProcessorProperties processorProperties = propertiesMerger.merge(containerProperties, propertiesSupplied);

        ServiceBusProcessorErrorContextConsumer errorContextConsumer = containerProperties.getErrorContextConsumer();
        ServiceBusMessageListener messageListener = containerProperties.getMessageListener();
        Assert.notNull(errorContextConsumer, "An errorContextConsumer must be provided!");
        Assert.notNull(messageListener, "A message listener must be provided!");

        return doCreateProcessor(queue, null, messageListener, errorContextConsumer, processorProperties);
    }

    @Override
    public ServiceBusProcessorClient createProcessor(String topic,
                                                     String subscription,
                                                     ServiceBusMessageListener messageListener,
                                                     ServiceBusProcessorErrorContextConsumer errorContextConsumer) {
        return doCreateProcessor(topic, subscription, messageListener, errorContextConsumer,
            this.propertiesSupplier.getProperties(new ConsumerIdentifier(topic, subscription)));
    }

    @Override
    public ServiceBusProcessorClient createProcessor(String topic, String subscription,
                                                     ServiceBusContainerProperties containerProperties) {
        ProcessorProperties propertiesSupplied = this.propertiesSupplier.getProperties(
            new ConsumerIdentifier(topic, subscription));
        ProcessorPropertiesMerger propertiesMerger = new ProcessorPropertiesMerger();
        ProcessorProperties processorProperties = propertiesMerger.merge(containerProperties, propertiesSupplied);

        ServiceBusProcessorErrorContextConsumer errorContextConsumer = containerProperties.getErrorContextConsumer();
        ServiceBusMessageListener messageListener = containerProperties.getMessageListener();
        Assert.notNull(errorContextConsumer, "An errorContextConsumer must be provided!");
        Assert.notNull(messageListener, "An message listener must be provided!");

        return doCreateProcessor(topic, subscription, messageListener, errorContextConsumer, processorProperties);
    }

    private ServiceBusProcessorClient doCreateProcessor(String name, String subscription,
                                                        @NonNull ServiceBusMessageListener messageListener,
                                                        @NonNull ServiceBusProcessorErrorContextConsumer errorContextConsumer,
                                                        @Nullable ProcessorProperties properties) {
        ConsumerIdentifier key = new ConsumerIdentifier(name, subscription);

        return processorMap.computeIfAbsent(key, k -> {
            ProcessorPropertiesParentMerger propertiesMerger = new ProcessorPropertiesParentMerger();
            ProcessorProperties processorProperties = propertiesMerger.merge(properties, this.namespaceProperties);
            processorProperties.setAutoComplete(false);
            processorProperties.setEntityName(k.getDestination());
            if (!k.hasGroup()) {
                processorProperties.setEntityType(ServiceBusEntityType.QUEUE);
            } else {
                processorProperties.setEntityType(ServiceBusEntityType.TOPIC);
                processorProperties.setSubscriptionName(k.getGroup());
            }

            ServiceBusProcessorClient client;
            //TODO(yiliu6): whether to use shared ServiceBusClientBuilder
            if (Boolean.TRUE.equals(processorProperties.getSessionEnabled())) {

                ServiceBusSessionProcessorClientBuilderFactory factory =
                    new ServiceBusSessionProcessorClientBuilderFactory(processorProperties, messageListener, errorContextConsumer);

                factory.setDefaultTokenCredential(this.defaultAzureCredential);
                factory.setTokenCredentialResolver(this.tokenCredentialResolver);
                factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_SERVICE_BUS);

                client = factory.build().buildProcessorClient();
            } else {
                ServiceBusProcessorClientBuilderFactory factory =
                    new ServiceBusProcessorClientBuilderFactory(processorProperties, messageListener, errorContextConsumer);

                factory.setDefaultTokenCredential(this.defaultAzureCredential);
                factory.setTokenCredentialResolver(this.tokenCredentialResolver);
                factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_SERVICE_BUS);

                client = factory.build().buildProcessorClient();
            }

            this.listeners.forEach(l -> l.processorAdded(k.getDestination(), k.getGroup(), client));
            return client;
        });
    }

    @Override
    public void addListener(Listener listener) {
        this.listeners.add(listener);
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
