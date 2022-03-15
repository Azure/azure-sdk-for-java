// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.core;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusSenderClientBuilderFactory;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.servicebus.core.ServiceBusProducerFactory;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProducerProperties;
import com.azure.spring.messaging.servicebus.implementation.properties.merger.SenderPropertiesParentMerger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.spring.messaging.implementation.config.AzureMessagingBootstrapConfiguration.LOGGER;

/**
 * The {@link ServiceBusProducerFactory} implementation to produce new {@link ServiceBusSenderAsyncClient} instances
 * for provided {@link NamespaceProperties} and optional producer {@link PropertiesSupplier} on each
 * {@link #createProducer} invocation.
 * <p>
 * {@link ServiceBusSenderAsyncClient} produced by this factory will share the same namespace level configuration, but
 * if a configuration entry is provided at both producer and namespace level, the producer level configuration will
 * take advantage.
 * </p>
 */
public final class DefaultServiceBusNamespaceProducerFactory implements ServiceBusProducerFactory, DisposableBean {

    private final List<Listener> listeners = new ArrayList<>();
    private final NamespaceProperties namespaceProperties;
    private final PropertiesSupplier<String, ProducerProperties> propertiesSupplier;
    private final Map<String, ServiceBusSenderAsyncClient> clients = new ConcurrentHashMap<>();
    private final SenderPropertiesParentMerger parentMerger = new SenderPropertiesParentMerger();
    private final List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder>> customizers = new ArrayList<>();
    private final Map<String, List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder>>> dedicatedCustomizers = new HashMap<>();
    private AzureCredentialResolver<TokenCredential> tokenCredentialResolver = null;
    private TokenCredential defaultAzureCredential = null;

    /**
     * Construct a factory with the provided namespace level configuration.
     * @param namespaceProperties the namespace properties
     */
    public DefaultServiceBusNamespaceProducerFactory(NamespaceProperties namespaceProperties) {
        this(namespaceProperties, key -> null);
    }

    /**
     * Construct a factory with the provided namespace level configuration and producer {@link PropertiesSupplier}.
     * @param namespaceProperties the namespace properties.
     * @param supplier the {@link PropertiesSupplier} to supply {@link ProducerProperties} for each queue/topic entity.
     */
    public DefaultServiceBusNamespaceProducerFactory(NamespaceProperties namespaceProperties,
                                                     PropertiesSupplier<String, ProducerProperties> supplier) {
        this.namespaceProperties = namespaceProperties;
        this.propertiesSupplier = supplier == null ? key -> null : supplier;
    }

    @Override
    public ServiceBusSenderAsyncClient createProducer(String name) {
        return createProducer(name, null);
    }

    @Override
    public ServiceBusSenderAsyncClient createProducer(String name, ServiceBusEntityType entityType) {
        ProducerProperties producerProperties = this.propertiesSupplier.getProperties(name) != null
            ? this.propertiesSupplier.getProperties(name) : new ProducerProperties();
        if (entityType != null) {
            producerProperties.setEntityType(entityType);
        }
        return doCreateProducer(name, producerProperties);
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
        clients.forEach((name, producer) -> {
            listeners.forEach(l -> l.producerRemoved(name, producer));
            producer.close();
        });
        this.clients.clear();
        this.listeners.clear();
    }

    private ServiceBusSenderAsyncClient doCreateProducer(String name, @Nullable ProducerProperties properties) {
        return clients.computeIfAbsent(name, entityName -> {
            ProducerProperties producerProperties = parentMerger.merge(properties, this.namespaceProperties);
            producerProperties.setEntityName(entityName);

            //TODO(yiliu6): whether to make the producer client share the same service bus client builder
            ServiceBusSenderClientBuilderFactory factory = new ServiceBusSenderClientBuilderFactory(producerProperties);

            factory.setDefaultTokenCredential(this.defaultAzureCredential);
            factory.setTokenCredentialResolver(this.tokenCredentialResolver);
            factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_SERVICE_BUS);

            ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder = factory.build();
            customizeBuilder(name, builder);
            ServiceBusSenderAsyncClient producerClient = builder.buildAsyncClient();

            this.listeners.forEach(l -> l.producerAdded(entityName, producerClient));
            return producerClient;
        });
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

    /**
     * Add a service client builder customizer to customize all the clients created from this factory.
     * @param customizer the provided customizer.
     */
    public void addBuilderCustomizer(AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder> customizer) {
        if (customizer == null) {
            LOGGER.debug("The provided customizer is null, will ignore it.");
            return;
        }
        this.customizers.add(customizer);
    }

    /**
     * Add a service client builder customizer to customize the clients created from this factory with service bus
     * entity name of value {@code entityName}.
     * @param entityName the entity name of the client.
     * @param customizer the provided customizer.
     */
    public void addBuilderCustomizer(String entityName,
                                     AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder> customizer) {
        if (customizer == null) {
            LOGGER.debug("The provided customizer is null, will ignore it.");
            return;
        }
        this.dedicatedCustomizers
            .computeIfAbsent(entityName, key -> new ArrayList<>())
            .add(customizer);
    }

    private void customizeBuilder(String entityName, ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder) {
        this.customizers.forEach(customizer -> customizer.customize(builder));
        this.dedicatedCustomizers.getOrDefault(entityName, new ArrayList<>())
                                 .forEach(customizer -> customizer.customize(builder));
    }

}
