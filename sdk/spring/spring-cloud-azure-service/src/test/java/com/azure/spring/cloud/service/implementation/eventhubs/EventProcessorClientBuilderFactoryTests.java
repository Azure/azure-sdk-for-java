// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsBatchMessageListener;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsErrorHandler;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsRecordMessageListener;
import com.azure.spring.cloud.service.eventhubs.properties.StartPositionProperties;
import com.azure.spring.cloud.service.implementation.AzureServiceClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.eventhubs.factory.EventProcessorClientBuilderFactory;
import com.azure.spring.cloud.service.listener.MessageListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import java.time.Duration;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EventProcessorClientBuilderFactoryTests extends AzureServiceClientBuilderFactoryBaseTests<
    EventProcessorClientBuilder,
    AzureEventHubsTestProperties.Processor,
    EventProcessorClientBuilderFactoryTests.EventProcessorClientBuilderFactoryExt> {

    @Test
    void errorHandlerConfigured() {
        AzureEventHubsTestProperties.Processor properties = new AzureEventHubsTestProperties.Processor();
        EventHubsErrorHandler errorHandler = errorContext -> { };
        EventHubsRecordMessageListener messageListener = eventContext -> { };

        final EventProcessorClientBuilderFactoryExt builderFactory = new EventProcessorClientBuilderFactoryExt(properties, null, messageListener, errorHandler);
        final EventProcessorClientBuilder builder = builderFactory.build();

        verify(builder, times(1)).processError(errorHandler);
    }

    @Test
    void initializationContextConsumerConfigured() {
        AzureEventHubsTestProperties.Processor properties = new AzureEventHubsTestProperties.Processor();
        Consumer<InitializationContext> initializationContextConsumer = initializationContext -> { };

        final EventProcessorClientBuilderFactoryExt builderFactory = createClientBuilderFactoryWithMockBuilder(properties);
        builderFactory.setInitializationContextConsumer(initializationContextConsumer);
        final EventProcessorClientBuilder builder = builderFactory.build();

        verify(builder, times(1)).processPartitionInitialization(initializationContextConsumer);
    }

    @Test
    void closeContextConsumerConfigured() {
        AzureEventHubsTestProperties.Processor properties = new AzureEventHubsTestProperties.Processor();
        Consumer<CloseContext> closeContextConsumer = closeContext -> { };

        final EventProcessorClientBuilderFactoryExt builderFactory = createClientBuilderFactoryWithMockBuilder(properties);
        builderFactory.setCloseContextConsumer(closeContextConsumer);
        final EventProcessorClientBuilder builder = builderFactory.build();

        verify(builder, times(1)).processPartitionClose(closeContextConsumer);
    }

    @Test
    void checkpointStoreConfigured() {
        AzureEventHubsTestProperties.Processor properties = new AzureEventHubsTestProperties.Processor();
        EventHubsErrorHandler errorHandler = errorContext -> { };
        EventHubsRecordMessageListener messageListener = eventContext -> { };
        CheckpointStore checkpointStore = mock(CheckpointStore.class);

        final EventProcessorClientBuilderFactoryExt builderFactory = new EventProcessorClientBuilderFactoryExt(properties, checkpointStore, messageListener, errorHandler);
        final EventProcessorClientBuilder builder = builderFactory.build();

        verify(builder, times(1)).checkpointStore(checkpointStore);
    }

    @Test
    void messageListenerConfigured() {
        AzureEventHubsTestProperties.Processor properties = new AzureEventHubsTestProperties.Processor();
        EventHubsErrorHandler errorHandler = errorContext -> { };
        EventHubsRecordMessageListener messageListener = eventContext -> { };

        final EventProcessorClientBuilderFactoryExt builderFactory = new EventProcessorClientBuilderFactoryExt(properties, null, messageListener, errorHandler);
        final EventProcessorClientBuilder builder = builderFactory.build();

        verify(builder, times(1)).processEvent(any());
    }

    @Test
    void batchMessageListenerConfigured() {
        AzureEventHubsTestProperties.Processor properties = new AzureEventHubsTestProperties.Processor();
        properties.getBatch().setMaxSize(5);
        properties.getBatch().setMaxWaitTime(Duration.ofSeconds(3));

        EventHubsErrorHandler errorHandler = errorContext -> { };
        Consumer<EventBatchContext> consumer = eventBatchContext -> { };
        EventHubsBatchMessageListener messageListener = eventBatchContext -> { };

        final EventProcessorClientBuilderFactoryExt builderFactory = new EventProcessorClientBuilderFactoryExt(properties, null, messageListener, errorHandler);
        final EventProcessorClientBuilder builder = builderFactory.build();

        verify(builder, times(1)).processEventBatch(any(), eq(5), eq(Duration.ofSeconds(3)));
    }

    @Test
    void wrongMessageListenerTypeWillThrow() {
        AzureEventHubsTestProperties.Processor properties = new AzureEventHubsTestProperties.Processor();
        MessageListener<?> messageListener = eventContext -> { };

        final EventProcessorClientBuilderFactoryExt builderFactory = new EventProcessorClientBuilderFactoryExt(properties, null, messageListener, null);

        Assertions.assertThrows(IllegalArgumentException.class, () -> builderFactory.build());
    }

    @Override
    protected AzureEventHubsTestProperties.Processor createMinimalServiceProperties() {
        AzureEventHubsTestProperties.Processor properties = new AzureEventHubsTestProperties.Processor();
        properties.setNamespace("test-namespace");
        return properties;
    }

    @Override
    protected EventProcessorClientBuilderFactoryExt createClientBuilderFactoryWithMockBuilder(AzureEventHubsTestProperties.Processor properties) {
        return new EventProcessorClientBuilderFactoryExt(properties);
    }

    @Override
    protected void buildClient(EventProcessorClientBuilder builder) {
        builder.buildEventProcessorClient();
    }

    @Override
    protected void verifyCredentialCalled(EventProcessorClientBuilder builder,
                                          Class<? extends TokenCredential> tokenCredentialClass,
                                          VerificationMode mode) {
        verify(builder, mode).credential(any(tokenCredentialClass));
    }

    @Override
    protected void verifyServicePropertiesConfigured() {
        AzureEventHubsTestProperties.Processor properties = new AzureEventHubsTestProperties.Processor();

        properties.setNamespace("test-namespace");
        properties.setEventHubName("test-event-hub");
        properties.setCustomEndpointAddress("test-custom-endpoint-address");
        properties.setConsumerGroup("test-consumer-group");
        properties.setPrefetchCount(150);
        properties.setTrackLastEnqueuedEventProperties(true);
        properties.getLoadBalancing().setStrategy(LoadBalancingStrategy.GREEDY);
        properties.getLoadBalancing().setPartitionOwnershipExpirationInterval(Duration.ofMinutes(3));
        properties.getLoadBalancing().setUpdateInterval(Duration.ofHours(1));
        StartPositionProperties positionProperties = new StartPositionProperties();
        positionProperties.setOffset("earliest");
        properties.getInitialPartitionEventPosition().put("0", positionProperties);

        final EventProcessorClientBuilderFactoryExt builderFactory = createClientBuilderFactoryWithMockBuilder(properties);
        final EventProcessorClientBuilder builder = builderFactory.build();

        verify(builder, times(1)).fullyQualifiedNamespace(properties.getFullyQualifiedNamespace());
        verify(builder, times(1)).eventHubName("test-event-hub");
        verify(builder, times(1)).customEndpointAddress("test-custom-endpoint-address");
        verify(builder, times(1)).consumerGroup("test-consumer-group");
        verify(builder, times(1)).prefetchCount(150);
        verify(builder, times(1)).trackLastEnqueuedEventProperties(true);
        verify(builder, times(1)).loadBalancingStrategy(LoadBalancingStrategy.GREEDY);
        verify(builder, times(1)).loadBalancingUpdateInterval(Duration.ofHours(1));
        verify(builder, times(1)).partitionOwnershipExpirationInterval(Duration.ofMinutes(3));
        verify(builder, times(1)).initialPartitionEventPosition(anyMap());
    }

    static class EventProcessorClientBuilderFactoryExt extends EventProcessorClientBuilderFactory {

        EventProcessorClientBuilderFactoryExt(AzureEventHubsTestProperties.Processor properties) {
            super(properties, null, mock(EventHubsRecordMessageListener.class), errorContext -> { });
        }

        EventProcessorClientBuilderFactoryExt(AzureEventHubsTestProperties.Processor properties,
                                              CheckpointStore checkpointStore,
                                              MessageListener<?> listener,
                                              EventHubsErrorHandler errorHandler) {
            super(properties, checkpointStore, listener, errorHandler);
        }

        @Override
        public EventProcessorClientBuilder createBuilderInstance() {
            return mock(EventProcessorClientBuilder.class);
        }
    }
}
