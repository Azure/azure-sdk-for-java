// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.inbound;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.integration.instrumentation.Instrumentation;
import com.azure.spring.integration.instrumentation.InstrumentationManager;
import com.azure.spring.integration.servicebus.inbound.health.ServiceBusProcessorInstrumentation;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.ListenerMode;
import com.azure.spring.messaging.checkpoint.AzureCheckpointer;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.checkpoint.Checkpointer;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import com.azure.spring.service.servicebus.processor.RecordMessageProcessingListener;
import com.azure.spring.service.servicebus.processor.consumer.ErrorContextConsumer;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.servicebus.core.ServiceBusProcessorContainer;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import com.azure.spring.servicebus.support.ServiceBusMessageHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.service.servicebus.properties.ServiceBusEntityType.QUEUE;
import static com.azure.spring.service.servicebus.properties.ServiceBusEntityType.TOPIC;

/**
 * Inbound channel adapter for Service Bus Queue.
 */
public class ServiceBusInboundChannelAdapter extends MessageProducerSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusInboundChannelAdapter.class);

    private final IntegrationRecordMessageProcessingListener recordEventProcessor =
        new IntegrationRecordMessageProcessingListener();
    private MessageProcessingListener listener;
    private final String destination;
    private final ServiceBusEntityType type;
    private final String subscription;
    private final ServiceBusProcessorContainer processorContainer;
    private final ListenerMode listenerMode;
    private final CheckpointConfig checkpointConfig;
    private static final String MSG_FAIL_CHECKPOINT = "Failed to checkpoint %s";
    private static final String MSG_SUCCESS_CHECKPOINT = "Checkpointed %s in %s mode";

    public ServiceBusInboundChannelAdapter(ServiceBusProcessorContainer processorContainer, String queue,
                                           CheckpointConfig checkpointConfig) {
        this(processorContainer, queue, ListenerMode.RECORD, checkpointConfig);
    }

    public ServiceBusInboundChannelAdapter(ServiceBusProcessorContainer processorContainer, String queue,
                                           ListenerMode listenerMode, CheckpointConfig checkpointConfig) {
        this(processorContainer, QUEUE, queue, null, listenerMode, checkpointConfig);
    }

    public ServiceBusInboundChannelAdapter(ServiceBusProcessorContainer processorContainer, String topic,
                                           String subscription, CheckpointConfig checkpointConfig) {
        this(processorContainer, topic, subscription, ListenerMode.RECORD, checkpointConfig);
    }

    public ServiceBusInboundChannelAdapter(ServiceBusProcessorContainer processorContainer, String topic,
                                           String subscription, ListenerMode listenerMode,
                                           CheckpointConfig checkpointConfig) {
        this(processorContainer, TOPIC, topic, subscription, listenerMode, checkpointConfig);
    }

    private ServiceBusInboundChannelAdapter(ServiceBusProcessorContainer processorContainer, ServiceBusEntityType type,
                                            String destination, String subscription, ListenerMode listenerMode,
                                            CheckpointConfig checkpointConfig) {
        Assert.hasText(destination, "destination can't be null or empty");
        if (TOPIC == type) {
            Assert.hasText(subscription, "subscription can't be null or empty");
        }
        this.processorContainer = processorContainer;
        this.type = type;
        this.destination = destination;
        this.subscription = subscription;
        this.listenerMode = listenerMode;
        this.checkpointConfig = checkpointConfig;
    }

    @Override
    protected void onInit() {
        if (ListenerMode.RECORD.equals(this.listenerMode)) {
            this.listener = recordEventProcessor;
        }

        if (TOPIC == this.type) {
            this.processorContainer.subscribe(this.destination, this.subscription, this.listener);
        } else {
            this.processorContainer.subscribe(this.destination, this.listener);
        }
    }

    @Override
    public void doStart() {
        this.processorContainer.start();
    }

    public void setMessageConverter(ServiceBusMessageConverter messageConverter) {
        this.recordEventProcessor.setMessageConverter(messageConverter);
    }

    public void setPayloadType(Class<?> payloadType) {
        this.recordEventProcessor.setPayloadType(payloadType);
    }

    public void setInstrumentationManager(InstrumentationManager instrumentationManager) {
        this.recordEventProcessor.setInstrumentationManager(instrumentationManager);
    }

    public void setInstrumentationId(String instrumentationId) {
        this.recordEventProcessor.setInstrumentationId(instrumentationId);

    }
    private class IntegrationRecordMessageProcessingListener implements RecordMessageProcessingListener {

        private ServiceBusMessageConverter messageConverter = new ServiceBusMessageConverter();
        private Class<?> payloadType = byte[].class;
        private InstrumentationManager instrumentationManager;
        private String instrumentationId;

        @Override
        public ErrorContextConsumer getErrorContextConsumer() {
            return errorContext -> {
                LOGGER.error("Error occurred on entity {}. Error: {}",
                    errorContext.getEntityPath(),
                    errorContext.getException());

                Instrumentation instrumentation = instrumentationManager.getHealthInstrumentation(instrumentationId);
                if (instrumentation != null) {
                    if (instrumentation instanceof ServiceBusProcessorInstrumentation) {
                        ((ServiceBusProcessorInstrumentation) instrumentation).markError(errorContext);
                    } else {
                        instrumentation.markDown(errorContext.getException());
                    }
                }
            };
        }

        @Override
        public void onMessage(ServiceBusReceivedMessageContext messageContext) {
            Checkpointer checkpointer = new AzureCheckpointer(() -> Mono.fromRunnable(messageContext::complete),
                () -> Mono.fromRunnable(messageContext::abandon));
            Map<String, Object> headers = new HashMap<>();
            headers.put(ServiceBusMessageHeaders.RECEIVED_MESSAGE_CONTEXT, messageContext);

            if (checkpointConfig.getMode() == CheckpointMode.MANUAL) {
                headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
            }

            Message<?> message = messageConverter.toMessage(messageContext.getMessage(), new MessageHeaders(headers),
                payloadType);
            sendMessage(message);

            if (checkpointConfig.getMode() == CheckpointMode.RECORD) {
                checkpointer.success()
                            .doOnSuccess(t -> logCheckpointSuccess(message))
                            .doOnError(t -> logCheckpointFail(message, t))
                            .subscribe();
            }
        }

        public void setMessageConverter(ServiceBusMessageConverter converter) {
            this.messageConverter = converter;
        }

        public void setPayloadType(Class<?> payloadType) {
            this.payloadType = payloadType;
        }

        public void setInstrumentationManager(InstrumentationManager instrumentationManager) {
            this.instrumentationManager = instrumentationManager;
        }

        public void setInstrumentationId(String instrumentationId) {
            this.instrumentationId = instrumentationId;
        }
    }

    protected void logCheckpointFail(Message<?> message, Throwable t) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(String.format(MSG_FAIL_CHECKPOINT, message), t);
        }
    }

    protected void logCheckpointSuccess(Message<?> message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(MSG_SUCCESS_CHECKPOINT, message, this.checkpointConfig.getMode()));
        }
    }

}
