// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhubs.inbound;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.eventhubs.checkpoint.CheckpointManagers;
import com.azure.spring.eventhubs.checkpoint.EventCheckpointManager;
import com.azure.spring.eventhubs.core.EventHubsProcessorContainer;
import com.azure.spring.eventhubs.support.converter.EventHubBatchMessageConverter;
import com.azure.spring.eventhubs.support.converter.EventHubsMessageConverter;
import com.azure.spring.integration.eventhubs.inbound.health.EventHusProcessorInstrumentation;
import com.azure.spring.integration.instrumentation.Instrumentation;
import com.azure.spring.integration.instrumentation.InstrumentationManager;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.ListenerMode;
import com.azure.spring.messaging.checkpoint.AzureCheckpointer;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.checkpoint.Checkpointer;
import com.azure.spring.service.eventhubs.processor.BatchEventProcessingListener;
import com.azure.spring.service.eventhubs.processor.EventProcessingListener;
import com.azure.spring.service.eventhubs.processor.RecordEventProcessingListener;
import com.azure.spring.service.eventhubs.processor.consumer.CloseContextConsumer;
import com.azure.spring.service.eventhubs.processor.consumer.ErrorContextConsumer;
import com.azure.spring.service.eventhubs.processor.consumer.InitializationContextConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Inbound channel adapter for Azure Event Hubs.
 */
public class EventHubsInboundChannelAdapter extends MessageProducerSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsInboundChannelAdapter.class);
    private final EventHubsProcessorContainer processorContainer;
    private final String eventHubName;
    private final String consumerGroup;
    private final ListenerMode listenerMode;
    private final IntegrationRecordEventProcessingListener recordEventProcessor = new IntegrationRecordEventProcessingListener();
    private final IntegrationBatchEventProcessingListener batchEventProcessor =
        new IntegrationBatchEventProcessingListener();

    private final CheckpointConfig checkpointConfig;
    private InstrumentationEventProcessingListener listener;
    private EventCheckpointManager checkpointManager;

    public EventHubsInboundChannelAdapter(EventHubsProcessorContainer processorContainer,
                                          String eventHubName, String consumerGroup,
                                          CheckpointConfig checkpointConfig) {
        this(processorContainer, eventHubName, consumerGroup, ListenerMode.RECORD, checkpointConfig);
    }

    public EventHubsInboundChannelAdapter(EventHubsProcessorContainer eventProcessorsContainer,
                                          String eventHubName, String consumerGroup,
                                          ListenerMode listenerMode,
                                          CheckpointConfig checkpointConfig) {
        Assert.notNull(eventHubName, "eventhubName must be provided");
        Assert.notNull(consumerGroup, "consumerGroup must be provided");

        this.processorContainer = eventProcessorsContainer;
        this.eventHubName = eventHubName;
        this.consumerGroup = consumerGroup;
        this.listenerMode = listenerMode;
        this.checkpointConfig = checkpointConfig;
    }

    @Override
    protected void onInit() {
        if (ListenerMode.BATCH.equals(this.listenerMode)) {
            this.listener = batchEventProcessor;
        } else {
            this.listener = recordEventProcessor;
        }

        this.checkpointManager = CheckpointManagers.of(checkpointConfig, this.listenerMode);
        this.processorContainer.subscribe(this.eventHubName, this.consumerGroup, this.listener);

    }

    @Override
    public void doStart() {
        this.processorContainer.start();
    }

    @Override
    protected void doStop() {
        this.processorContainer.stop();
    }

    public void setMessageConverter(EventHubsMessageConverter messageConverter) {
        this.recordEventProcessor.setMessageConverter(messageConverter);
    }

    public void setPayloadType(Class<?> payloadType) {
        this.recordEventProcessor.setPayloadType(payloadType);
    }

    public void setInstrumentationManager(InstrumentationManager instrumentationManager) {
        if (ListenerMode.BATCH.equals(this.listenerMode)) {
            this.batchEventProcessor.setInstrumentationManager(instrumentationManager);
        } else {
            this.recordEventProcessor.setInstrumentationManager(instrumentationManager);
        }
    }

    public void setInstrumentationId(String instrumentationId) {
        if (ListenerMode.BATCH.equals(this.listenerMode)) {
            this.batchEventProcessor.setInstrumentationId(instrumentationId);
        } else {
            this.recordEventProcessor.setInstrumentationId(instrumentationId);
        }
    }

    /**
     *
     */
    public interface InstrumentationEventProcessingListener extends EventProcessingListener {
        void setInstrumentationManager(InstrumentationManager instrumentationManager);
        void setInstrumentationId(String instrumentationId);
        default void updateInstrumentation(ErrorContext errorContext,
                                           InstrumentationManager instrumentationManager,
                                           String instrumentationId) {
            Instrumentation instrumentation = instrumentationManager.getHealthInstrumentation(instrumentationId);
            if (instrumentation != null) {
                if (instrumentation instanceof EventHusProcessorInstrumentation) {
                    ((EventHusProcessorInstrumentation) instrumentation).markError(errorContext);
                } else {
                    instrumentation.markDown(errorContext.getThrowable());
                }
            }
        }
    }

    private class IntegrationRecordEventProcessingListener implements InstrumentationEventProcessingListener, RecordEventProcessingListener {

        private EventHubsMessageConverter messageConverter = new EventHubsMessageConverter();
        private Class<?> payloadType = byte[].class;
        private InstrumentationManager instrumentationManager;
        private String instrumentationId;

        @Override
        public ErrorContextConsumer getErrorContextConsumer() {
            return errorContext -> {
                LOGGER.error("Record event error occurred on partition: {}. Error: {}",
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable());
                updateInstrumentation(errorContext, instrumentationManager, instrumentationId);
            };
        }

        @Override
        public void onEvent(EventContext eventContext) {
            PartitionContext partition = eventContext.getPartitionContext();

            Map<String, Object> headers = new HashMap<>();
            headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());

            final EventData event = eventContext.getEventData();

            Checkpointer checkpointer = new AzureCheckpointer(eventContext::updateCheckpointAsync);
            if (CheckpointMode.MANUAL.equals(checkpointConfig.getMode())) {
                headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
            }

            Message<?> message = this.messageConverter.toMessage(event, new MessageHeaders(headers), payloadType);

            sendMessage(message);

            checkpointManager.checkpoint(eventContext);

        }

        @Override
        public CloseContextConsumer getCloseContextConsumer() {
            return closeContext -> LOGGER.info("Stopped receiving on partition: {}. Reason: {}",
                closeContext.getPartitionContext().getPartitionId(),
                closeContext.getCloseReason());
        }

        @Override
        public InitializationContextConsumer getInitializationContextConsumer() {
            return initializationContext -> LOGGER.info("Started receiving on partition: {}",
                initializationContext.getPartitionContext().getPartitionId());
        }

        public void setMessageConverter(EventHubsMessageConverter converter) {
            this.messageConverter = converter;
        }

        public void setPayloadType(Class<?> payloadType) {
            this.payloadType = payloadType;
        }

        @Override
        public void setInstrumentationManager(InstrumentationManager instrumentationManager) {
            this.instrumentationManager = instrumentationManager;
        }

        @Override
        public void setInstrumentationId(String instrumentationId) {
            this.instrumentationId = instrumentationId;
        }
    }

    private class IntegrationBatchEventProcessingListener implements InstrumentationEventProcessingListener, BatchEventProcessingListener {

        private EventHubBatchMessageConverter messageConverter = new EventHubBatchMessageConverter();
        private Class<?> payloadType = byte[].class;
        private InstrumentationManager instrumentationManager;
        private String instrumentationId;

        @Override
        public ErrorContextConsumer getErrorContextConsumer() {
            return errorContext -> {
                LOGGER.error("Error occurred on partition: {}. Error: {}",
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable());
                updateInstrumentation(errorContext, instrumentationManager, instrumentationId);
            };
        }

        @Override
        public CloseContextConsumer getCloseContextConsumer() {
            return closeContext -> LOGGER.info("Stopped receiving on partition: {}. Reason: {}",
                closeContext.getPartitionContext().getPartitionId(),
                closeContext.getCloseReason());
        }

        @Override
        public InitializationContextConsumer getInitializationContextConsumer() {
            return initializationContext -> LOGGER.info("Started receiving on partition: {}",
                initializationContext.getPartitionContext().getPartitionId());
        }

        public void setMessageConverter(EventHubBatchMessageConverter converter) {
            this.messageConverter = converter;
        }

        public void setPayloadType(Class<?> payloadType) {
            this.payloadType = payloadType;
        }

        @Override
        public void onEventBatch(EventBatchContext eventBatchContext) {
            PartitionContext partition = eventBatchContext.getPartitionContext();

            Map<String, Object> headers = new HashMap<>();
            headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());

            Checkpointer checkpointer = new AzureCheckpointer(eventBatchContext::updateCheckpointAsync);
            if (CheckpointMode.MANUAL.equals(checkpointConfig.getMode())) {
                headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
            }

            Message<?> message = this.messageConverter.toMessage(eventBatchContext, new MessageHeaders(headers), payloadType);

            sendMessage(message);
            if (checkpointConfig.getMode().equals(CheckpointMode.BATCH)) {
                checkpointManager.checkpoint(eventBatchContext);
            }
        }

        @Override
        public void setInstrumentationManager(InstrumentationManager instrumentationManager) {
            this.instrumentationManager = instrumentationManager;
        }

        @Override
        public void setInstrumentationId(String instrumentationId) {
            this.instrumentationId = instrumentationId;
        }
    }

}
