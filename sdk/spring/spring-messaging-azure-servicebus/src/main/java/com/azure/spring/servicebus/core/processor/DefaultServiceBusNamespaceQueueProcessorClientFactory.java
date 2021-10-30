// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.processor;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.service.core.PropertyMapper;
import com.azure.spring.service.servicebus.factory.ServiceBusClientBuilderFactory;
import com.azure.spring.servicebus.core.ServiceBusMessageProcessor;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.servicebus.core.properties.merger.ProcessorPropertiesParentMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.Nullable;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


/**
 * Default implementation of {@link ServiceBusNamespaceQueueProcessorClientFactory}. Client will be cached to improve performance
 *
 * @author Warren Zhu
 */
public class DefaultServiceBusNamespaceQueueProcessorClientFactory implements ServiceBusNamespaceQueueProcessorClientFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceBusNamespaceQueueProcessorClientFactory.class);
    private final Map<String, ServiceBusProcessorClient> queueProcessorMap = new ConcurrentHashMap<>();
    private final List<Listener> listeners = new ArrayList<>();
    private final NamespaceProperties namespaceProperties;
    private final PropertiesSupplier<Tuple2<String, String>, ProcessorProperties> propertiesSupplier;
    private final ProcessorPropertiesParentMerger propertiesMerger = new ProcessorPropertiesParentMerger();


    // TODO (xiada) the application id should be different for spring integration
    public DefaultServiceBusNamespaceQueueProcessorClientFactory(NamespaceProperties namespaceProperties) {
        this(namespaceProperties, key -> null);
    }

    public DefaultServiceBusNamespaceQueueProcessorClientFactory(
        PropertiesSupplier<Tuple2<String, String>, ProcessorProperties> supplier) {
        this(null, supplier);
    }

    public DefaultServiceBusNamespaceQueueProcessorClientFactory(NamespaceProperties namespaceProperties,
                                                                 PropertiesSupplier<Tuple2<String, String>, ProcessorProperties> supplier) {
        this.namespaceProperties = namespaceProperties;
        this.propertiesSupplier = supplier == null ? key -> null : supplier;
    }

    private <K, V> void close(Map<K, V> map, Consumer<V> close) {
        map.values().forEach(it -> {
            try {
                close.accept(it);
            } catch (Exception ex) {
                LOGGER.warn("Failed to clean service bus queue client factory", ex);
            }
        });
    }

    @Override
    public void destroy() {
        close(queueProcessorMap, ServiceBusProcessorClient::close);
    }

    @Override
    public ServiceBusProcessorClient createProcessor(String queue, ServiceBusMessageProcessor messageProcessor) {
        return doCreateProcessor(queue, messageProcessor, this.propertiesSupplier.getProperties(name));
    }

    private ServiceBusProcessorClient doCreateProcessor(String queue, ServiceBusMessageProcessor messageProcessor,
                                                        @Nullable ProcessorProperties properties) {

        if (this.queueProcessorMap.containsKey(queue)) {
            return this.queueProcessorMap.get(queue);
        }

        ProcessorProperties processorProperties = propertiesMerger.mergeParent(properties, this.namespaceProperties);
        processorProperties.setQueueName(queue);

        ServiceBusProcessorClient client;
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilderFactory(properties).build();
        if (properties.getSessionAware()) {
            client = buildSessionProcessorClientBuilder(serviceBusClientBuilder, processorProperties, messageProcessor);
        } else {
            client = buildProcessorClientBuilder(serviceBusClientBuilder, processorProperties, messageProcessor);
        }

        this.listeners.forEach(l -> l.processorAdded(queue));
        this.queueProcessorMap.put(queue, client);
        return client;
    }

    private ServiceBusProcessorClient buildSessionProcessorClientBuilder(ServiceBusClientBuilder serviceBusClientBuilder,
                                                                         ProcessorProperties processorProperties, ServiceBusMessageProcessor messageProcessor) {

        final ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder sessionProcessorClientBuilder = serviceBusClientBuilder.sessionProcessor();
        PropertyMapper propertyMapper = new PropertyMapper();

        propertyMapper.from(processorProperties.getQueueName()).to(sessionProcessorClientBuilder::queueName);
        propertyMapper.from(processorProperties.getReceiveMode()).to(sessionProcessorClientBuilder::receiveMode);
        propertyMapper.from(processorProperties.getSubQueue()).to(sessionProcessorClientBuilder::subQueue);
        propertyMapper.from(processorProperties.getPrefetchCount()).to(sessionProcessorClientBuilder::prefetchCount);
        propertyMapper.from(processorProperties.getMaxAutoLockRenewDuration()).to(sessionProcessorClientBuilder::maxAutoLockRenewDuration);
        propertyMapper.from(processorProperties.getAutoComplete()).whenFalse().to(t -> sessionProcessorClientBuilder.disableAutoComplete());
        propertyMapper.from(processorProperties.getMaxConcurrentCalls()).to(sessionProcessorClientBuilder::maxConcurrentCalls);
        propertyMapper.from(processorProperties.getMaxConcurrentSessions()).to(sessionProcessorClientBuilder::maxConcurrentSessions);
        propertyMapper.from(messageProcessor.processError()).to(sessionProcessorClientBuilder::processError);
        propertyMapper.from(messageProcessor.processMessage()).whenNonNull().to(sessionProcessorClientBuilder::processMessage);

        ServiceBusProcessorClient client = sessionProcessorClientBuilder.buildProcessorClient();
        LOGGER.info("ServiceBusProcessorClient created for queue '{}'", processorProperties.getQueueName());
        return client;
    }

    private ServiceBusProcessorClient buildProcessorClientBuilder(ServiceBusClientBuilder serviceBusClientBuilder,
                                                                  ProcessorProperties processorProperties, ServiceBusMessageProcessor messageProcessor) {

        final ServiceBusClientBuilder.ServiceBusProcessorClientBuilder processorClientBuilder = serviceBusClientBuilder.processor();
        PropertyMapper propertyMapper = new PropertyMapper();

        propertyMapper.from(processorProperties.getQueueName()).to(processorClientBuilder::queueName);
        propertyMapper.from(processorProperties.getReceiveMode()).to(processorClientBuilder::receiveMode);
        propertyMapper.from(processorProperties.getSubQueue()).to(processorClientBuilder::subQueue);
        propertyMapper.from(processorProperties.getPrefetchCount()).to(processorClientBuilder::prefetchCount);
        propertyMapper.from(processorProperties.getMaxAutoLockRenewDuration()).to(processorClientBuilder::maxAutoLockRenewDuration);
        propertyMapper.from(processorProperties.getAutoComplete()).whenFalse().to(t -> processorClientBuilder.disableAutoComplete());
        propertyMapper.from(processorProperties.getMaxConcurrentCalls()).to(processorClientBuilder::maxConcurrentCalls);
        propertyMapper.from(messageProcessor.processError()).to(processorClientBuilder::processError);
        propertyMapper.from(messageProcessor.processMessage()).whenNonNull().to(processorClientBuilder::processMessage);

        ServiceBusProcessorClient client = processorClientBuilder.buildProcessorClient();
        LOGGER.info("ServiceBusProcessorClient created for queue '{}'", processorProperties.getQueueName());
        return client;
    }
}
