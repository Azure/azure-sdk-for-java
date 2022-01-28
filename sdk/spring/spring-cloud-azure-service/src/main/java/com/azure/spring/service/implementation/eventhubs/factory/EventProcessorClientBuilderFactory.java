// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.eventhubs.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.NamedKeyAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.PropertyMapper;
import com.azure.spring.service.eventhubs.processor.BatchEventProcessingListener;
import com.azure.spring.service.eventhubs.processor.EventProcessingListener;
import com.azure.spring.service.eventhubs.processor.RecordEventProcessingListener;
import com.azure.spring.service.implementation.eventhubs.properties.EventProcessorClientProperties;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.azure.spring.service.implementation.converter.EventPositionConverter.EVENT_POSITION_CONVERTER;

/**
 * Event Hub client builder factory, it builds the {@link EventHubClientBuilder} according the configuration context and
 * blob properties.
 */
public class EventProcessorClientBuilderFactory extends AbstractAzureAmqpClientBuilderFactory<EventProcessorClientBuilder> {

    private final EventProcessorClientProperties eventProcessorClientProperties;
    private final CheckpointStore checkpointStore;
    private final EventProcessingListener processorListener;

    /**
     * Create a {@link EventProcessorClientBuilderFactory} with the {@link EventProcessorClientProperties} and a
     * {@link CheckpointStore} and a {@link EventProcessingListener}.
     * @param eventProcessorClientProperties the properties of the event processor client.
     * @param checkpointStore the checkpoint store.
     * @param listener the listener for event processing.
     */
    public EventProcessorClientBuilderFactory(EventProcessorClientProperties eventProcessorClientProperties,
                                              CheckpointStore checkpointStore,
                                              EventProcessingListener listener) {
        this.eventProcessorClientProperties = eventProcessorClientProperties;
        this.checkpointStore = checkpointStore;
        this.processorListener = listener;
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, ProxyOptions> consumeProxyOptions() {
        return EventProcessorClientBuilder::proxyOptions;
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, AmqpTransportType> consumeAmqpTransportType() {
        return EventProcessorClientBuilder::transportType;
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, AmqpRetryOptions> consumeAmqpRetryOptions() {
        return EventProcessorClientBuilder::retry;
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, ClientOptions> consumeClientOptions() {
        return EventProcessorClientBuilder::clientOptions;
    }

    @Override
    protected EventProcessorClientBuilder createBuilderInstance() {
        return new EventProcessorClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.eventProcessorClientProperties;
    }

    // Endpoint=sb://<FQDN>/;SharedAccessKeyName=<KeyName>;SharedAccessKey=<KeyValue>

    @Override
    protected void configureService(EventProcessorClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(eventProcessorClientProperties.getConsumerGroup()).to(builder::consumerGroup);
        map.from(eventProcessorClientProperties.getPrefetchCount()).to(builder::prefetchCount);
        map.from(eventProcessorClientProperties.getCustomEndpointAddress()).to(builder::customEndpointAddress);
        map.from(eventProcessorClientProperties.getTrackLastEnqueuedEventProperties()).to(builder::trackLastEnqueuedEventProperties);
        map.from(eventProcessorClientProperties.getLoadBalancing().getPartitionOwnershipExpirationInterval()).to(builder::partitionOwnershipExpirationInterval);
        map.from(eventProcessorClientProperties.getLoadBalancing().getStrategy()).to(builder::loadBalancingStrategy);
        map.from(eventProcessorClientProperties.getLoadBalancing().getUpdateInterval()).to(builder::loadBalancingUpdateInterval);
        // TODO kasobol-msft this requires latest EH version.
        // mapper.from(eventProcessorClientProperties.getFullyQualifiedNamespace()).to(builder::fullyQualifiedNamespace);
        // mapper.from(eventProcessorClientProperties.getEventHubName()).to(builder::eventHubName);

        map.from(eventProcessorClientProperties.getInitialPartitionEventPosition()).when(c -> !CollectionUtils.isEmpty(c))
                .to(m -> {
                    Map<String, EventPosition> eventPositionMap = m.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue() != null)
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> EVENT_POSITION_CONVERTER.convert(entry.getValue())));
                    builder.initialPartitionEventPosition(eventPositionMap);
                });
        configureCheckpointStore(builder);
        configureProcessorListener(builder);
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, Configuration> consumeConfiguration() {
        return EventProcessorClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return (builder, tokenCredential) -> builder.credential(eventProcessorClientProperties.getFullyQualifiedNamespace(),
            eventProcessorClientProperties.getEventHubName(),
            tokenCredential);
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, String> consumeConnectionString() {
        return (builder, s) -> builder.connectionString(s, this.eventProcessorClientProperties.getEventHubName());
    }

    private void configureCheckpointStore(EventProcessorClientBuilder builder) {
        builder.checkpointStore(this.checkpointStore);
    }

    private void configureProcessorListener(EventProcessorClientBuilder builder) {
        final EventProcessorClientProperties.EventBatch batch = this.eventProcessorClientProperties.getBatch();

        if (processorListener instanceof BatchEventProcessingListener) {
            Assert.notNull(batch.getMaxSize(), "Batch max size must be provided");
            builder.processEventBatch(((BatchEventProcessingListener) processorListener)::onEventBatch,
                batch.getMaxSize(), batch.getMaxWaitTime());
        } else if (processorListener instanceof RecordEventProcessingListener) {
            builder.processEvent(((RecordEventProcessingListener) processorListener)::onEvent);
        }
        builder.processError(processorListener.getErrorContextConsumer());
        builder.processPartitionClose(processorListener.getCloseContextConsumer());
        builder.processPartitionInitialization(processorListener.getInitializationContextConsumer());
    }

}
