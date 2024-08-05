// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.inbound;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.integration.core.instrumentation.Instrumentation;
import com.azure.spring.integration.core.instrumentation.InstrumentationManager;
import com.azure.spring.integration.servicebus.implementation.health.ServiceBusProcessorInstrumentation;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.ListenerMode;
import com.azure.spring.messaging.implementation.checkpoint.AzureCheckpointer;
import com.azure.spring.messaging.checkpoint.Checkpointer;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.servicebus.core.listener.ServiceBusMessageListenerContainer;
import com.azure.spring.messaging.servicebus.implementation.core.listener.adapter.RecordMessagingMessageListenerAdapter;
import com.azure.spring.messaging.servicebus.support.ServiceBusMessageHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

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
 *                 .block();
 *     }
 *
 *    {@literal @}Bean
 *     public ServiceBusInboundChannelAdapter queueMessageChannelAdapter(
 *         {@literal @}Qualifier("input") MessageChannel inputChannel,
 *         ServiceBusMessageListenerContainer container) {
 *         ServiceBusInboundChannelAdapter adapter =
 *             new ServiceBusInboundChannelAdapter(container);
 *         adapter.setOutputChannel(inputChannel);
 *         return adapter;
 *     }
 *
 *    {@literal @}Bean
 *     public ServiceBusMessageListenerContainer container(
 *     ServiceBusProcessorFactory processorFactory) {
 *         ServiceBusContainerProperties containerProperties = new ServiceBusContainerProperties();
 *         containerProperties.setEntityName("RECEIVE_QUEUE_NAME");
 *         containerProperties.setAutoComplete(false);
 *         return new ServiceBusMessageListenerContainer(processorFactory, containerProperties);
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

    private final IntegrationRecordMessageListener recordListener = new IntegrationRecordMessageListener();
    private final ServiceBusMessageListenerContainer listenerContainer;
    private final ListenerMode listenerMode;
    private InstrumentationManager instrumentationManager;
    private String instrumentationId;
    private final boolean isAutoComplete;
    private static final String MSG_FAIL_CHECKPOINT = "Failed to checkpoint %s";

    /**
     * Construct a {@link ServiceBusInboundChannelAdapter} with the specified {@link ServiceBusMessageListenerContainer}.
     *
     * @param listenerContainer the message listener container.
     */
    public ServiceBusInboundChannelAdapter(ServiceBusMessageListenerContainer listenerContainer) {
        this(listenerContainer, ListenerMode.RECORD);
    }

    /**
     * Construct a {@link ServiceBusInboundChannelAdapter} with the specified {@link ServiceBusMessageListenerContainer}
     *  ,{@link ListenerMode}.
     *
     * @param listenerContainer the message listener container.
     * @param listenerMode the listen mode
     */
    public ServiceBusInboundChannelAdapter(ServiceBusMessageListenerContainer listenerContainer,
                                           ListenerMode listenerMode) {
        this.listenerContainer = listenerContainer;
        this.listenerMode = listenerMode;
        this.isAutoComplete = !Boolean.FALSE.equals(listenerContainer.getContainerProperties().getAutoComplete());
    }

    @Override
    protected void onInit() {
        Assert.state(ListenerMode.RECORD == this.listenerMode, "Only record mode is supported!");

        this.listenerContainer.setupMessageListener(this.recordListener);

        this.listenerContainer.getContainerProperties().setErrorHandler(new IntegrationErrorHandler());
    }

    @Override
    public void doStart() {
        this.listenerContainer.start();
    }

    /**
     * Set message converter.
     *
     * @param messageConverter the message converter
     */
    public void setMessageConverter(AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> messageConverter) {
        this.recordListener.setMessageConverter(messageConverter);
    }

    /**
     * Set payload type.
     *
     * @param payloadType the payload type
     */
    public void setPayloadType(Class<?> payloadType) {
        this.recordListener.setPayloadType(payloadType);
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

    private class IntegrationErrorHandler implements ServiceBusErrorHandler {

        @Override
        public void accept(ServiceBusErrorContext errorContext) {
            LOGGER.error("Error in the operation {} occurred on entity {}. Error: {}",
                errorContext.getErrorSource(),
                errorContext.getEntityPath(),
                errorContext.getException());
            updateInstrumentation(errorContext);
        }

        private void updateInstrumentation(ServiceBusErrorContext errorContext) {
            if (instrumentationManager == null || instrumentationId == null) {
                LOGGER.debug("InstrumentationManager or instrumentationId is null, skip updateInstrumentation.");
                return;
            }

            Instrumentation instrumentation = instrumentationManager.getHealthInstrumentation(instrumentationId);
            if (instrumentation != null) {
                if (instrumentation instanceof ServiceBusProcessorInstrumentation) {
                    ((ServiceBusProcessorInstrumentation) instrumentation).markError(errorContext);
                } else {
                    instrumentation.setStatus(Instrumentation.Status.DOWN, errorContext.getException());
                }
            }
        }
    }

    private class IntegrationRecordMessageListener extends RecordMessagingMessageListenerAdapter {

        @Override
        public void onMessage(ServiceBusReceivedMessageContext messageContext) {
            Map<String, Object> headers = new HashMap<>();
            headers.put(ServiceBusMessageHeaders.RECEIVED_MESSAGE_CONTEXT, messageContext);

            if (!isAutoComplete) {
                Checkpointer checkpointer = new AzureCheckpointer(() -> Mono.fromRunnable(messageContext::complete),
                    () -> Mono.fromRunnable(messageContext::abandon));
                headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
            }

            Message<?> message = getMessageConverter().toMessage(messageContext.getMessage(), new MessageHeaders(headers),
                payloadType);
            sendMessage(message);
        }

    }
}
