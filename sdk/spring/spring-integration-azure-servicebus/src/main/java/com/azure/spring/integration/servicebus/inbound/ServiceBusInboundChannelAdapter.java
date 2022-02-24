// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.inbound;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
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
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import com.azure.spring.service.servicebus.processor.RecordMessageProcessingListener;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.servicebus.core.ServiceBusProcessorContainer;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import com.azure.spring.servicebus.support.ServiceBusMessageHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.azure.spring.service.servicebus.properties.ServiceBusEntityType.QUEUE;
import static com.azure.spring.service.servicebus.properties.ServiceBusEntityType.TOPIC;

/**
 * Inbound channel adapter for Service Bus.
 * <p>
 * Example:
 * <pre> <code>
 *   {@literal @}ServiceActivator(inputChannel = "input")
 *     public void messageReceiver(byte[] payload, {@literal @}Header(AzureHeaders.CHECKPOINTER) Checkpointer checkpointer) {
 *         String message = new String(payload);
 *         LOGGER.info("New message received: '{}'", message);
 *         checkpointer.success()
 *                 .doOnSuccess(s -&gt; LOGGER.info("Message '{}' successfully checkpointed", message))
 *                 .doOnError(e -&gt; LOGGER.error("Error found", e))
 *                 .subscribe();
 *     }
 *
 *    {@literal @}Bean
 *     public ServiceBusInboundChannelAdapter queueMessageChannelAdapter(
 *         {@literal @}Qualifier("input") MessageChannel inputChannel, ServiceBusProcessorContainer processorContainer) {
 *         ServiceBusInboundChannelAdapter adapter = new ServiceBusInboundChannelAdapter(processorContainer, "queue-name",
 *             null, new CheckpointConfig(CheckpointMode.MANUAL));
 *         adapter.setOutputChannel(inputChannel);
 *         return adapter;
 *     }
 *
 *    {@literal @}Bean(name = INPUT_CHANNEL)
 *     public MessageChannel input() {
 *         return new DirectChannel();
 *     }
 * </code> </pre>
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

    /**
     * Construct a {@link ServiceBusInboundChannelAdapter} with the specified {@link ServiceBusProcessorContainer}, destination name,
     * subscription name if topic is used and {@link CheckpointConfig}.
     *
     * @param processorContainer the processor container
     * @param destination the Service Bus entity name
     * @param subscription the subscription name if topic is used
     * @param checkpointConfig the checkpoint config
     */
    public ServiceBusInboundChannelAdapter(ServiceBusProcessorContainer processorContainer, String destination,
                                           @Nullable String subscription, CheckpointConfig checkpointConfig) {
        this(processorContainer, destination, subscription, ListenerMode.RECORD, checkpointConfig);
    }

    /**
     * Construct a {@link ServiceBusInboundChannelAdapter} with the specified {@link ServiceBusProcessorContainer},
     * destination name, subscription name if topic is used, {@link ListenerMode} and {@link CheckpointConfig}.
     * @param processorContainer the processor container
     * @param destination the Service Bus entity name
     * @param subscription the subscription name if topic is used
     * @param listenerMode the listen mode
     * @param checkpointConfig the checkpoint config
     */
    public ServiceBusInboundChannelAdapter(ServiceBusProcessorContainer processorContainer, String destination,
                                           @Nullable String subscription, ListenerMode listenerMode,
                                           CheckpointConfig checkpointConfig) {
        Assert.hasText(destination, "destination can't be null or empty");
        this.type = subscription == null ? QUEUE : TOPIC;
        this.processorContainer = processorContainer;
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

    /**
     * Set message converter.
     *
     * @param messageConverter the message converter
     */
    public void setMessageConverter(AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> messageConverter) {
        this.recordEventProcessor.setMessageConverter(messageConverter);
    }

    /**
     * Set payload type.
     *
     * @param payloadType the payload type
     */
    public void setPayloadType(Class<?> payloadType) {
        this.recordEventProcessor.setPayloadType(payloadType);
    }

    /**
     * Set instrumentation manager.
     *
     * @param instrumentationManager the instrumentation manager
     */
    public void setInstrumentationManager(InstrumentationManager instrumentationManager) {
        this.recordEventProcessor.setInstrumentationManager(instrumentationManager);
    }

    /**
     * Set instrumentation id.
     *
     * @param instrumentationId the instrumentation id
     */
    public void setInstrumentationId(String instrumentationId) {
        this.recordEventProcessor.setInstrumentationId(instrumentationId);

    }

    protected class IntegrationRecordMessageProcessingListener implements RecordMessageProcessingListener {

        private AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> messageConverter = new ServiceBusMessageConverter();
        private Class<?> payloadType = byte[].class;
        private InstrumentationManager instrumentationManager;
        private String instrumentationId;

        @Override
        public Consumer<ServiceBusErrorContext> getErrorContextConsumer() {
            return errorContext -> {
                LOGGER.error("Error occurred on entity {}. Error: {}",
                    errorContext.getEntityPath(),
                    errorContext.getException());
                if (instrumentationManager == null) {
                    return;
                }

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
                            .doOnSuccess(t ->
                                LOGGER.debug(String.format(MSG_SUCCESS_CHECKPOINT, message, checkpointConfig.getMode())))
                            .doOnError(t ->
                                LOGGER.warn(String.format(MSG_FAIL_CHECKPOINT, message), t))
                            .subscribe();
            }
        }

        /**
         * Set message converter.
         *
         * @param converter the converter
         */
        public void setMessageConverter(AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> converter) {
            this.messageConverter = converter;
        }

        /**
         * Set payload type.
         *
         * @param payloadType the payload type
         */
        public void setPayloadType(Class<?> payloadType) {
            this.payloadType = payloadType;
        }

        /**
         * Set instrumentation manager.
         *
         * @param instrumentationManager the instrumentation manager
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
    }
}
