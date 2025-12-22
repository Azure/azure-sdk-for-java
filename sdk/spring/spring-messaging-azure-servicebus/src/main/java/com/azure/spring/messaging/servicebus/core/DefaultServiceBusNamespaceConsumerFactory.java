// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusSessionReceiverClientBuilderFactory;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.servicebus.core.properties.ConsumerProperties;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.implementation.properties.merger.ConsumerPropertiesParentMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link ServiceBusConsumerFactory} implementation to produce new {@link ServiceBusSessionReceiverClient} instances
 * for provided {@link NamespaceProperties} and optional producer {@link PropertiesSupplier} on each
 * {@link #createReceiver} invocation.
 * <p>
 * {@link ServiceBusSessionReceiverClient} produced by this factory will share the same namespace level configuration, but
 * if a configuration entry is provided at both producer and namespace level, the producer level configuration will
 * take advantage.
 * </p>
 * @since 5.22.0
 */
@SuppressWarnings("deprecation")
public final class DefaultServiceBusNamespaceConsumerFactory implements ServiceBusConsumerFactory, DisposableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceBusNamespaceConsumerFactory.class);
    private final List<Listener> listeners = new ArrayList<>();
    private ApplicationContext applicationContext;
    private final NamespaceProperties namespaceProperties;
    private final PropertiesSupplier<ConsumerIdentifier, ConsumerProperties> propertiesSupplier;
    private final Map<String, ServiceBusSessionReceiverClient> clients = new ConcurrentHashMap<>();
    private final ConsumerPropertiesParentMerger parentMerger = new ConsumerPropertiesParentMerger();
    private final List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> customizers = new ArrayList<>();
    private final List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder>> sessionReceiverCustomizers = new ArrayList<>();
    private final Map<String, List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder>>> dedicatedSessionReceiverCustomizers = new HashMap<>();
    private AzureCredentialResolver<TokenCredential> tokenCredentialResolver = null;
    private TokenCredential defaultCredential = null;

    /**
     * Construct a factory with the provided namespace level configuration.
     * @param namespaceProperties the namespace properties
     */
    public DefaultServiceBusNamespaceConsumerFactory(NamespaceProperties namespaceProperties) {
        this(namespaceProperties, key -> null);
    }

    /**
     * Construct a factory with the provided namespace level configuration and producer {@link PropertiesSupplier}.
     * @param namespaceProperties the namespace properties.
     * @param supplier the {@link PropertiesSupplier} to supply {@link ConsumerProperties} for each queue/topic entity.
     */
    public DefaultServiceBusNamespaceConsumerFactory(NamespaceProperties namespaceProperties,
                                                     PropertiesSupplier<ConsumerIdentifier, ConsumerProperties> supplier) {
        this.namespaceProperties = namespaceProperties;
        this.propertiesSupplier = supplier == null ? key -> null : supplier;
    }

    @Override
    public ServiceBusSessionReceiverClient createReceiver(String name) {
        return doCreateReceiver(name, null);
    }

    @Override
    public ServiceBusSessionReceiverClient createReceiver(String name, ServiceBusEntityType entityType) {
        ConsumerProperties consumerProperties = this.propertiesSupplier.getProperties(new ConsumerIdentifier(name)) != null
            ? this.propertiesSupplier.getProperties(new ConsumerIdentifier(name)) : new ConsumerProperties();
        // Set the entityType only if it is not already defined in consumerProperties.
        // This ensures that the entityType provided as a method argument is used as a fallback
        // when consumerProperties does not specify one.
        if (consumerProperties.getEntityType() == null && entityType != null) {
            consumerProperties.setEntityType(entityType);
        }
        return doCreateReceiver(name, consumerProperties);
    }

    @Override
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    @Override
    public boolean removeListener(Listener listener) {
        return this.listeners.remove(listener);
    }

    @Override
    public void destroy() {
        clients.forEach((name, receiver) -> {
            listeners.forEach(l -> l.consumerRemoved(name, receiver));
            receiver.close();
        });
        this.clients.clear();
        this.listeners.clear();
    }

    private ServiceBusSessionReceiverClient doCreateReceiver(String name, @Nullable ConsumerProperties properties) {
        return clients.computeIfAbsent(name, entityName -> {
            ConsumerProperties consumerProperties = parentMerger.merge(properties, this.namespaceProperties);
            consumerProperties.setEntityName(entityName);

            ServiceBusSessionReceiverClient receiverClient;
            if (Boolean.TRUE.equals(consumerProperties.getSessionEnabled())) {
                ServiceBusSessionReceiverClientBuilderFactory factory =
                    new ServiceBusSessionReceiverClientBuilderFactory(consumerProperties, this.customizers);

                factory.setDefaultTokenCredential(this.defaultCredential);
                factory.setTokenCredentialResolver(this.tokenCredentialResolver);
                factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_SERVICE_BUS);
                factory.setApplicationContext(this.applicationContext);

                ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder = factory.build();

                customizeBuilder(name, builder);

                builder.receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE);
                builder.disableAutoComplete();
                LOGGER.debug("Set RECEIVE_AND_DELETE mode for request-reply-pattern receiver client, "
                    + "'enableAutoComplete' is not needed in for RECEIVE_AND_DELETE mode.");

                receiverClient = builder.buildClient();

                this.listeners.forEach(l -> l.consumerAdded(entityName, receiverClient));
            } else {
                receiverClient = null;
                LOGGER.warn("Receiver client is null. Define a bean PropertiesSupplier<ConsumerIdentifier, ConsumerProperties> to enable consumer 'session-enabled'.");
            }
            return receiverClient;
        });
    }

    /**
     * Set the token credential resolver.
     *
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
     * Add a {@link ServiceBusClientBuilder}
     * customizer to customize the shared client builder created in this factory, it's used to build other sender clients.
     *
     * @param customizer the provided builder customizer.
     */
    public void addServiceBusClientBuilderCustomizer(AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder> customizer) {
        if (customizer == null) {
            LOGGER.debug("The provided '{}' customizer is null, will ignore it.", ServiceBusClientBuilder.class.getName());
        } else {
            this.customizers.add(customizer);
        }
    }

    /**
     * Add a {@link ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder}
     * customizer to customize all the session clients created from this factory.
     * @param customizer the provided builder customizer.
     */
    public void addBuilderCustomizer(AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder> customizer) {
        if (customizer == null) {
            LOGGER.debug("The provided '{}' customizer is null, will ignore it.",
                ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class.getName());
            return;
        }
        this.sessionReceiverCustomizers.add(customizer);
    }

    /**
     * Add a session receiver client builder customizer to customize the clients created from this factory with Service Bus
     * entity name of value {@code entityName}.
     *
     * @param entityName the entity name of the client.
     * @param customizer the provided customizer.
     */
    public void addBuilderCustomizer(String entityName,
                                     AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder> customizer) {
        if (customizer == null) {
            LOGGER.debug("The provided '{}' dedicated customizer is null, will ignore it.",
                ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class.getName());
        } else {
            this.dedicatedSessionReceiverCustomizers
                .computeIfAbsent(entityName, key -> new ArrayList<>())
                .add(customizer);
        }
    }

    private void customizeBuilder(String entityName, ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder) {
        this.sessionReceiverCustomizers.forEach(customizer -> customizer.customize(builder));
        this.dedicatedSessionReceiverCustomizers.getOrDefault(entityName, new ArrayList<>())
                                         .forEach(customizer -> customizer.customize(builder));
    }

    /**
     * Set the application context.
     * @param applicationContext the application context.
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
