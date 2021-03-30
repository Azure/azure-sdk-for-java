package com.azure.spring.integration.servicebus;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.integration.core.AzureCheckpointer;
import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.Checkpointer;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class InboundServiceBusMessageConsumer implements Consumer<ServiceBusReceivedMessageContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InboundServiceBusMessageConsumer.class);

    private static final String MSG_FAIL_CHECKPOINT = "Failed to checkpoint %s in queue '%s'";

    private static final String MSG_SUCCESS_CHECKPOINT = "Checkpointed %s in queue '%s' in %s mode";

    private CheckpointConfig checkpointConfig;
    private ServiceBusMessageConverter messageConverter;
    private Consumer<Message<?>> pump;
    private Class<?> payloadType;
    private String name;
    private String subscriptionName;

    public InboundServiceBusMessageConsumer(String name, String subscriptionName, CheckpointConfig checkpointConfig, ServiceBusMessageConverter messageConverter, Consumer<Message<?>> pump, Class<?> payloadType) {
        this.name = name;
        this.subscriptionName = subscriptionName;
        this.checkpointConfig = checkpointConfig;
        this.messageConverter = messageConverter;
        this.pump = pump;
        this.payloadType = payloadType;
    }

    @Override
    public void accept(ServiceBusReceivedMessageContext messageContext) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(AzureHeaders.LOCK_TOKEN, messageContext.getMessage().getLockToken());

        Checkpointer checkpointer = new AzureCheckpointer(() ->
            CompletableFuture.runAsync(messageContext::complete)
        , () -> CompletableFuture.runAsync(messageContext::abandon)); // TODO  may need to investigate whehter deadletter here.

        if (checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL) {
            headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
        }

        Message<?> message = messageConverter.toMessage(messageContext.getMessage(), new MessageHeaders(headers), payloadType);
        pump.accept(message);

        if (checkpointConfig.getCheckpointMode() == CheckpointMode.RECORD) {
            checkpointer.success().whenComplete((v, t) -> checkpointHandler(message, t));
        }

    }

    protected void checkpointHandler(Message<?> message, Throwable t) {
        if (t != null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(buildCheckpointFailMessage(message), t);
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(buildCheckpointSuccessMessage(message));
        }
    }

    private String buildCheckpointFailMessage(Message<?> message) {
        return String.format(MSG_FAIL_CHECKPOINT, message, name); //TODO: the logic of getting queue name
    }

    private String buildCheckpointSuccessMessage(Message<?> message) {
        return String.format(MSG_SUCCESS_CHECKPOINT, message, name, //  TODO: the logic of getting queue name
            checkpointConfig.getCheckpointMode());
    }
}
