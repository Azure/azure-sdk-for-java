// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhubs.inbound;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsErrorHandler;
import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.integration.core.instrumentation.Instrumentation;
import com.azure.spring.integration.core.instrumentation.InstrumentationManager;
import com.azure.spring.integration.eventhubs.implementation.health.EventHubsProcessorInstrumentation;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.ListenerMode;
import com.azure.spring.messaging.checkpoint.AzureCheckpointer;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointMode;
import com.azure.spring.messaging.checkpoint.Checkpointer;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.messaging.eventhubs.implementation.checkpoint.CheckpointManagers;
import com.azure.spring.messaging.eventhubs.implementation.checkpoint.EventCheckpointManager;
import com.azure.spring.messaging.eventhubs.implementation.core.listener.adapter.BatchMessagingMessageListenerAdapter;
import com.azure.spring.messaging.eventhubs.implementation.core.listener.adapter.RecordMessagingMessageListenerAdapter;
import com.azure.spring.messaging.eventhubs.support.EventHubsHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * Message driven inbound channel adapter for Azure Event Hubs.
 * <p>
 * Example:
 * <pre> <code>
 *   {@literal @}ServiceActivator(inputChannel = "input")
 *     public void messageReceiver(byte[] payload, @Header(AzureHeaders.CHECKPOINTER) Checkpointer checkpointer) {
 *         String message = new String(payload);
 *         LOGGER.info("New message received: '{}'", message);
 *         checkpointer.success()
 *                 .doOnSuccess(s -&gt; LOGGER.info("Message '{}' successfully checkpointed", message))
 *                 .doOnError(e -&gt; LOGGER.error("Error found", e))
 *                 .block();
 *     }
 *
 *    {@literal @}Bean
 *     public EventHubsInboundChannelAdapter messageChannelAdapter(
 *         {@literal @}Qualifier("input") MessageChannel inputChannel, EventHubsMessageListenerContainer container) {
 *         EventHubsInboundChannelAdapter adapter =
 *             new EventHubsInboundChannelAdapter(container);
 *         adapter.setOutputChannel(inputChannel);
 *         return adapter;
 *     }
 *
 *    {@literal @}Bean
 *     public EventHubsMessageListenerContainer listener(
 *     EventHubsProcessorFactory processorFactory) {
 *         EventHubsContainerProperties containerProperties = new EventHubsContainerProperties();
 *         containerProperties.setEventHubName("eventhub-1");
 *         containerProperties.setConsumerGroup("consumer-group-1");
 *         containerProperties.setCheckpointConfig(new CheckpointConfig(CheckpointMode.MANUAL));
 *         return new EventHubsMessageListenerContainer(processorFactory, containerProperties);
 *     }
 *
 *    {@literal @}Bean
 *     public MessageChannel input() {
 *         return new DirectChannel();
 *     }
 * </code> </pre>
 */
public class EventHubsInboundChannelAdapter extends MessageProducerSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsInboundChannelAdapter.class);
    private final EventHubsMessageListenerContainer listenerContainer;
    private final ListenerMode listenerMode;
    private final IntegrationRecordMessageListener recordListener = new IntegrationRecordMessageListener();
    private final IntegrationBatchMessageListener batchListener = new IntegrationBatchMessageListener();

    private final CheckpointConfig checkpointConfig;
    private EventCheckpointManager checkpointManager;
    private InstrumentationManager instrumentationManager;
    private String instrumentationId;


    /**
     * Construct a {@link EventHubsInboundChannelAdapter} with the specified {@link EventHubsMessageListenerContainer},
     * event Hub Name, consumer Group and {@link CheckpointConfig}.
     *
     * @param listenerContainer the processor container
     */
    public EventHubsInboundChannelAdapter(EventHubsMessageListenerContainer listenerContainer) {
        this(listenerContainer, ListenerMode.RECORD);
    }

    /**
     * Construct a {@link EventHubsInboundChannelAdapter} with the specified {@link EventHubsMessageListenerContainer},
     * {@link ListenerMode} and {@link CheckpointConfig}.
     *
     * @param listenerContainer the event processors container
     * @param listenerMode the listener mode
     */
    public EventHubsInboundChannelAdapter(EventHubsMessageListenerContainer listenerContainer,
                                          ListenerMode listenerMode) {
        this.listenerContainer = listenerContainer;
        this.listenerMode = listenerMode;
        CheckpointConfig containerCheckpointConfig = listenerContainer.getContainerProperties().getCheckpointConfig();
        this.checkpointConfig = containerCheckpointConfig == null ? new CheckpointConfig() : containerCheckpointConfig;
    }

    @Override
    protected void onInit() {
        MessageListener<?> listener;
        if (ListenerMode.BATCH == this.listenerMode) {
            listener = batchListener;
        } else {
            listener = recordListener;
        }

        this.checkpointManager = CheckpointManagers.of(checkpointConfig, this.listenerMode);
        this.listenerContainer.setupMessageListener(listener);
        this.listenerContainer.setErrorHandler(new IntegrationErrorHandler());
        enhanceListenerContainer();
    }

    @Override
    public void doStart() {
        this.listenerContainer.start();
    }

    @Override
    protected void doStop() {
        this.listenerContainer.stop();
    }

    /**
     * Set message converter.
     *
     * @param messageConverter the message converter
     */
    public void setMessageConverter(AzureMessageConverter<EventData, EventData> messageConverter) {
        this.recordListener.setMessageConverter(messageConverter);
    }

    /**
     * Set message converter.
     *
     * @param messageConverter the message converter
     */
    public void setBatchMessageConverter(AzureMessageConverter<EventBatchContext, EventData> messageConverter) {
        this.batchListener.setMessageConverter(messageConverter);
    }

    /**
     * Set payload Type.
     *
     * @param payloadType the payload Type
     */
    public void setPayloadType(Class<?> payloadType) {
        if (ListenerMode.BATCH == this.listenerMode) {
            this.batchListener.setPayloadType(payloadType);
        } else {
            this.recordListener.setPayloadType(payloadType);
        }
    }

    /**
     * Set instrumentation Manager.
     *
     * @param instrumentationManager the instrumentation Manager
     */
    public void setInstrumentationManager(InstrumentationManager instrumentationManager) {
        this.instrumentationManager = instrumentationManager;
    }

    /**
     * Set instrumentation id.
     *
     * @param instrumentationId the instrumentation id
     */
    public void setInstrumentationId(String instrumentationId) {
        this.instrumentationId = instrumentationId;
    }

    private void enhanceListenerContainer() {
        this.listenerContainer.getContainerProperties().setCloseContextConsumer(closeContext ->
            LOGGER.info("Stopped receiving on partition: {}. Reason: {}",
                closeContext.getPartitionContext().getPartitionId(), closeContext.getCloseReason()));

        this.listenerContainer.getContainerProperties().setInitializationContextConsumer(initializationContext ->
            LOGGER.info("Started receiving on partition: {}",
                initializationContext.getPartitionContext().getPartitionId()));
    }

    private class IntegrationErrorHandler implements EventHubsErrorHandler {

        @Override
        public void accept(ErrorContext errorContext) {
            LOGGER.error("Error occurred on partition: {}. Error: {}",
                errorContext.getPartitionContext().getPartitionId(),
                errorContext.getThrowable());
            updateInstrumentation(errorContext);
        }

        private void updateInstrumentation(ErrorContext errorContext) {
            if (instrumentationManager == null) {
                return;
            }

            Instrumentation instrumentation = instrumentationManager.getHealthInstrumentation(instrumentationId);
            if (instrumentation != null) {
                if (instrumentation instanceof EventHubsProcessorInstrumentation) {
                    ((EventHubsProcessorInstrumentation) instrumentation).markError(errorContext);
                } else {
                    instrumentation.setStatus(Instrumentation.Status.DOWN, errorContext.getThrowable());
                }
            }
        }
    }

    private class IntegrationRecordMessageListener extends RecordMessagingMessageListenerAdapter {

        @Override
        public void onMessage(EventContext eventContext) {
            PartitionContext partition = eventContext.getPartitionContext();

            Map<String, Object> headers = new HashMap<>();
            headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());
            headers.put(EventHubsHeaders.LAST_ENQUEUED_EVENT_PROPERTIES, eventContext.getLastEnqueuedEventProperties());

            final EventData event = eventContext.getEventData();

            if (CheckpointMode.MANUAL == checkpointConfig.getMode()) {
                Checkpointer checkpointer = new AzureCheckpointer(eventContext::updateCheckpointAsync);
                headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
            }

            Message<?> message = this.getMessageConverter().toMessage(event, new MessageHeaders(headers), payloadType);

            sendMessage(message);

            checkpointManager.checkpoint(eventContext);
        }

    }

    private class IntegrationBatchMessageListener extends BatchMessagingMessageListenerAdapter {

        @Override
        public void onMessage(EventBatchContext eventBatchContext) {
            PartitionContext partition = eventBatchContext.getPartitionContext();

            Map<String, Object> headers = new HashMap<>();
            headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());
            headers.put(EventHubsHeaders.LAST_ENQUEUED_EVENT_PROPERTIES, eventBatchContext.getLastEnqueuedEventProperties());

            if (CheckpointMode.MANUAL == checkpointConfig.getMode()) {
                Checkpointer checkpointer = new AzureCheckpointer(eventBatchContext::updateCheckpointAsync);
                headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
            }

            Message<?> message = this.getMessageConverter().toMessage(eventBatchContext, new MessageHeaders(headers), payloadType);

            sendMessage(message);
            if (checkpointConfig.getMode() == CheckpointMode.BATCH) {
                checkpointManager.checkpoint(eventBatchContext);
            }
        }
    }

}
