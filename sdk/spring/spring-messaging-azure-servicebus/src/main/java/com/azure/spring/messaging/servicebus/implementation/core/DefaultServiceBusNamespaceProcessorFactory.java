// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.core;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusProcessorClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusSessionProcessorClientBuilderFactory;
import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.listener.ServiceBusMessageListenerContainer;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.messaging.servicebus.core.properties.ServiceBusContainerProperties;
import com.azure.spring.messaging.servicebus.implementation.properties.merger.ProcessorPropertiesMerger;
import com.azure.spring.messaging.servicebus.implementation.properties.merger.ProcessorPropertiesParentMerger;
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
    private final List<ServiceBusProcessClientBuilderCustomizer> customizers = new ArrayList<>();
    private final Map<ConsumerIdentifier, List<ServiceBusProcessClientBuilderCustomizer>> dedicatedCustomizers = new HashMap<>();
    private AzureCredentialResolver<TokenCredential> tokenCredentialResolver = null;
    private TokenCredential defaultAzureCredential = null;

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

    @Override
    public void destroy() {
        close(processorMap, ServiceBusProcessorClient::close);
        this.processorMap.clear();
        this.listeners.clear();
    }

    @Override
    public ServiceBusProcessorClient createProcessor(String queue,
                                                     MessageListener<?> messageListener,
                                                     ServiceBusErrorHandler errorHandler) {
        return doCreateProcessor(queue, null, messageListener, errorHandler, this.propertiesSupplier.getProperties(new ConsumerIdentifier(queue)));
    }

    @Override
    public ServiceBusProcessorClient createProcessor(String queue, ServiceBusContainerProperties containerProperties) {
        ProcessorProperties propertiesSupplied = this.propertiesSupplier.getProperties(new ConsumerIdentifier(queue));
        ProcessorPropertiesMerger propertiesMerger = new ProcessorPropertiesMerger();
        ProcessorProperties processorProperties = propertiesMerger.merge(containerProperties, propertiesSupplied);

        ServiceBusErrorHandler errorHandler = containerProperties.getErrorHandler();
        MessageListener<?> messageListener = containerProperties.getMessageListener();
        Assert.notNull(errorHandler, "An errorHandler must be provided!");
        Assert.notNull(messageListener, "A message listener must be provided!");

        return doCreateProcessor(queue, null, messageListener, errorHandler, processorProperties);
    }

    @Override
    public ServiceBusProcessorClient createProcessor(String topic,
                                                     String subscription,
                                                     MessageListener<?> messageListener,
                                                     ServiceBusErrorHandler errorHandler) {
        return doCreateProcessor(topic, subscription, messageListener, errorHandler,
            this.propertiesSupplier.getProperties(new ConsumerIdentifier(topic, subscription)));
    }

    @Override
    public ServiceBusProcessorClient createProcessor(String topic, String subscription,
                                                     ServiceBusContainerProperties containerProperties) {
        ProcessorProperties propertiesSupplied = this.propertiesSupplier.getProperties(
            new ConsumerIdentifier(topic, subscription));
        ProcessorPropertiesMerger propertiesMerger = new ProcessorPropertiesMerger();
        ProcessorProperties processorProperties = propertiesMerger.merge(containerProperties, propertiesSupplied);

        ServiceBusErrorHandler errorHandler = containerProperties.getErrorHandler();
        MessageListener<?> messageListener = containerProperties.getMessageListener();
        Assert.notNull(errorHandler, "An errorHandler must be provided!");
        Assert.notNull(messageListener, "An message listener must be provided!");

        return doCreateProcessor(topic, subscription, messageListener, errorHandler, processorProperties);
    }

    /**
     * Create the {@link ServiceBusProcessorClient} with given name, subscription, message listener, error handler, and
     * properties.
     *
     * <p>
     * This {@link ServiceBusProcessorClient} created from this method will disable the autocomplete, because this
     * processor client is used as the delegate in {@link ServiceBusMessageListenerContainer} and we want the listener
     * container or any upper layer of the {@link ServiceBusMessageListenerContainer} to handle the settlement of a
     * Service Bus message.
     *
     * @param name the queue name of topic name.
     * @param subscription the subscription name.
     * @param messageListener the message listener.
     * @param errorHandler the error handler.
     * @param properties the properties of the processor.
     *
     * @return the processor client.
     */
    private ServiceBusProcessorClient doCreateProcessor(String name,
                                                        @Nullable String subscription,
                                                        @NonNull MessageListener<?> messageListener,
                                                        @NonNull ServiceBusErrorHandler errorHandler,
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
                    new ServiceBusSessionProcessorClientBuilderFactory(processorProperties, messageListener, errorHandler);

                factory.setDefaultTokenCredential(this.defaultAzureCredential);
                factory.setTokenCredentialResolver(this.tokenCredentialResolver);
                factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_SERVICE_BUS);

                ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder = factory.build();
                customizeBuilder(name, subscription, builder);

                client = builder.buildProcessorClient();
            } else {
                ServiceBusProcessorClientBuilderFactory factory =
                    new ServiceBusProcessorClientBuilderFactory(processorProperties, messageListener, errorHandler);

                factory.setDefaultTokenCredential(this.defaultAzureCredential);
                factory.setTokenCredentialResolver(this.tokenCredentialResolver);
                factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_SERVICE_BUS);

                ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder = factory.build();
                customizeBuilder(name, subscription, builder);

                client = builder.buildProcessorClient();
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
    public void setDefaultAzureCredential(TokenCredential defaultAzureCredential) {
        this.defaultAzureCredential = defaultAzureCredential;
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

    /**
     * Add a service client builder customizer to customize all the clients created from this factory.
     * @param customizer the provided customizer.
     */
    public void addBuilderCustomizer(ServiceBusProcessClientBuilderCustomizer customizer) {
        if (customizer == null || !customizer.isAnyCustomizerSet()) {
            LOGGER.debug("The provided customizer is null, will ignore it.");
            return;
        }
        this.customizers.add(customizer);
    }

    /**
     * Add a service client builder customizer to customize the clients created from this factory with entity name of
     * value {@code entityName} and subscription of value {@code subscription}.
     *
     * @param entityName the entity name, could either be the queue name or topic name.
     * @param subscription the subscription name of the topic, could be null if it is a queue.
     * @param customizer the provided customizer.
     */
    public void addBuilderCustomizer(String entityName, String subscription, ServiceBusProcessClientBuilderCustomizer customizer) {
        if (customizer == null || !customizer.isAnyCustomizerSet()) {
            LOGGER.debug("The provided customizer is null, will ignore it.");
            return;
        }
        this.dedicatedCustomizers
            .computeIfAbsent(new ConsumerIdentifier(entityName, subscription), key -> new ArrayList<>())
            .add(customizer);
    }

    private void customizeBuilder(String entityName, String subscription,
                                  ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder) {
        this.customizers.stream()
                        .filter(c -> c.getNoneSessionCustomizer() != null)
                        .forEach(customizer -> customizer.getNoneSessionCustomizer().customize(builder));
        this.dedicatedCustomizers.getOrDefault(new ConsumerIdentifier(entityName, subscription), new ArrayList<>())
                                 .stream()
                                 .filter(c -> c.getNoneSessionCustomizer() != null)
                                 .forEach(customizer -> customizer.getNoneSessionCustomizer().customize(builder));
    }

    private void customizeBuilder(String entityName, String subscription,
                                  ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder) {
        this.customizers.stream()
                        .filter(c -> c.getSessionCustomizer() != null)
                        .forEach(customizer -> customizer.getSessionCustomizer().customize(builder));
        this.dedicatedCustomizers.getOrDefault(new ConsumerIdentifier(entityName, subscription), new ArrayList<>())
                                 .stream()
                                 .filter(c -> c.getSessionCustomizer() != null)
                                 .forEach(customizer -> customizer.getSessionCustomizer().customize(builder));
    }

    public static class ServiceBusProcessClientBuilderCustomizer {

        private final AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder> noneSessionCustomizer;
        private final AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder> sessionCustomizer;

        public ServiceBusProcessClientBuilderCustomizer(
            AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder> noneSessionCustomizer,
            AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder> sessionCustomizer) {
            this.noneSessionCustomizer = noneSessionCustomizer;
            this.sessionCustomizer = sessionCustomizer;
        }

        AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder> getNoneSessionCustomizer() {
            return noneSessionCustomizer;
        }

        AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder> getSessionCustomizer() {
            return sessionCustomizer;
        }

        boolean isAnyCustomizerSet() {
            return this.noneSessionCustomizer != null || this.sessionCustomizer != null;
        }
    }
}
