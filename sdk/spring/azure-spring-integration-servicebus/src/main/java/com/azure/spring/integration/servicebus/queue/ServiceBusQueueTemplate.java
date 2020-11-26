// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.queue;

import com.azure.spring.integration.core.AzureCheckpointer;
import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.Checkpointer;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.ServiceBusMessageHandler;
import com.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import com.azure.spring.integration.servicebus.ServiceBusTemplate;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.google.common.collect.Sets;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageSession;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.ISessionHandler;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * Default implementation of {@link ServiceBusQueueOperation}.
 *
 * @author Warren Zhu
 * @author Eduardo Sciullo
 */
public class ServiceBusQueueTemplate extends ServiceBusTemplate<ServiceBusQueueClientFactory>
    implements ServiceBusQueueOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusQueueTemplate.class);

    private static final String MSG_FAIL_CHECKPOINT = "Failed to checkpoint %s in queue '%s'";

    private static final String MSG_SUCCESS_CHECKPOINT = "Checkpointed %s in queue '%s' in %s mode";

    private final Set<String> subscribedQueues = Sets.newConcurrentHashSet();

    public ServiceBusQueueTemplate(ServiceBusQueueClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean subscribe(String destination,
                             @NonNull Consumer<Message<?>> consumer,
                             @NonNull Class<?> targetPayloadClass) {
        Assert.hasText(destination, "destination can't be null or empty");

        if (subscribedQueues.contains(destination)) {
            return false;
        }

        subscribedQueues.add(destination);

        internalSubscribe(destination, consumer, targetPayloadClass);

        return true;
    }

    @Override
    public boolean unsubscribe(String destination) {
        // TODO: unregister message handler but service bus sdk unsupported

        return subscribedQueues.remove(destination);
    }

    @Override
    public <T> void deadLetter(String destination, Message<T> message, String deadLetterReason,
                               String deadLetterErrorDescription) {
        Assert.hasText(destination, "destination can't be null or empty");
        IQueueClient queueClient = this.senderFactory.getOrCreateClient(destination);
        Object lockToken = message.getHeaders().get(AzureHeaders.LOCK_TOKEN);
        if (lockToken != null) {
            UUID uuid = UUID.fromString(lockToken.toString());

            try {
                if (!clientConfig.isSessionsEnabled()) {
                    queueClient.deadLetter(uuid, deadLetterReason, deadLetterErrorDescription);
                } else {
                    IMessageSession session = (IMessageSession) message.getHeaders().get(AzureHeaders.MESSAGE_SESSION);
                    Assert.notNull(session, "IMessageSession cannot be null");
                    session.deadLetter(uuid, deadLetterReason, deadLetterErrorDescription);
                }
            } catch (ServiceBusException | InterruptedException e) {
                LOGGER.error("Failed to register queue message handler", e);
                throw new ServiceBusRuntimeException("Failed to register queue message handler", e);
            }
        } else {
            LOGGER.error("Failed to send message to dead letter queue");
            throw new ServiceBusRuntimeException("Failed to send message to dead letter queue");
        }
    }

    @Override
    public <T> void abandon(String destination, Message<T> message) {
        Assert.hasText(destination, "destination can't be null or empty");
        IQueueClient queueClient = this.senderFactory.getOrCreateClient(destination);
        Object lockToken = message.getHeaders().get(AzureHeaders.LOCK_TOKEN);

        if (lockToken != null) {
            UUID uuid = UUID.fromString(lockToken.toString());

            try {
                if (!clientConfig.isSessionsEnabled()) {
                    queueClient.abandon(uuid);
                } else {
                    IMessageSession session = (IMessageSession) message.getHeaders().get(AzureHeaders.MESSAGE_SESSION);
                    Assert.notNull(session, "IMessageSession cannot be null");
                    session.abandon(uuid);
                }
            } catch (ServiceBusException | InterruptedException e) {
                LOGGER.error("Failed to register queue message handler", e);
                throw new ServiceBusRuntimeException("Failed to register queue message handler", e);
            }

        } else {
            LOGGER.error("Failed to send message to dead letter queue");
            throw new ServiceBusRuntimeException("Failed to send message to dead letter queue");
        }
    }

    /**
     * Register a message handler to receive message from the queue. A session handler will be registered if session is
     * enabled.
     *
     * @param name The queue name.
     * @param consumer The consumer method.
     * @param payloadType The type of the message payload.
     * @throws ServiceBusRuntimeException If fail to register the queue message handler.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void internalSubscribe(String name, Consumer<Message<?>> consumer, Class<?> payloadType) {

        IQueueClient queueClient = this.senderFactory.getOrCreateClient(name);

        String threadPrefix = String.format("%s-handler", name);

        try {
            queueClient.setPrefetchCount(this.clientConfig.getPrefetchCount());

            final QueueMessageHandler messageHandler = new QueueMessageHandler(consumer, payloadType, queueClient);
            final ExecutorService executors = buildHandlerExecutors(threadPrefix);

            // Register SessionHandler if sessions are enabled.
            // Handlers are mutually exclusive.
            if (this.clientConfig.isSessionsEnabled()) {
                queueClient.registerSessionHandler(messageHandler, buildSessionHandlerOptions(), executors);
            } else {
                queueClient.registerMessageHandler(messageHandler, buildHandlerOptions(), executors);
            }
        } catch (ServiceBusException | InterruptedException e) {
            LOGGER.error("Failed to register queue message handler", e);
            throw new ServiceBusRuntimeException("Failed to register queue message handler", e);
        }
    }

    @Override
    public void setClientConfig(@NonNull ServiceBusClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    protected class QueueMessageHandler<U> extends ServiceBusMessageHandler<U> implements ISessionHandler {
        private final IQueueClient queueClient;

        public QueueMessageHandler(Consumer<Message<U>> consumer, Class<U> payloadType, IQueueClient queueClient) {
            super(consumer, payloadType, ServiceBusQueueTemplate.this.getCheckpointConfig(),
                ServiceBusQueueTemplate.this.getMessageConverter());
            this.queueClient = queueClient;
        }

        @Override
        protected CompletableFuture<Void> success(UUID uuid) {
            return queueClient.completeAsync(uuid);
        }

        @Override
        protected CompletableFuture<Void> failure(UUID uuid) {
            return queueClient.abandonAsync(uuid);
        }

        @Override
        protected String buildCheckpointFailMessage(Message<?> message) {
            return String.format(MSG_FAIL_CHECKPOINT, message, queueClient.getQueueName());
        }

        @Override
        protected String buildCheckpointSuccessMessage(Message<?> message) {
            return String.format(MSG_SUCCESS_CHECKPOINT, message, queueClient.getQueueName(),
                getCheckpointConfig().getCheckpointMode());
        }

        // ISessionHandler
        @Override
        public CompletableFuture<Void> onMessageAsync(IMessageSession session, IMessage serviceBusMessage) {
            Map<String, Object> headers = new HashMap<>();
            headers.put(AzureHeaders.LOCK_TOKEN, serviceBusMessage.getLockToken());
            headers.put(AzureHeaders.MESSAGE_SESSION, session);

            Checkpointer checkpointer = new AzureCheckpointer(
                () -> session.completeAsync(serviceBusMessage.getLockToken()),
                () -> session.abandonAsync(serviceBusMessage.getLockToken()));

            if (checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL) {
                headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
            }

            Message<U> message = messageConverter.toMessage(serviceBusMessage,
                new MessageHeaders(headers), payloadType);
            consumer.accept(message);

            if (checkpointConfig.getCheckpointMode() == CheckpointMode.RECORD) {
                return checkpointer.success().whenComplete((v, t) -> super.checkpointHandler(message, t));
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> OnCloseSessionAsync(IMessageSession session) {
            LOGGER.info("Closed session '" + session.getSessionId() + "' for subscription: " + session.getEntityPath());
            return CompletableFuture.completedFuture(null);
        }
    }

}
