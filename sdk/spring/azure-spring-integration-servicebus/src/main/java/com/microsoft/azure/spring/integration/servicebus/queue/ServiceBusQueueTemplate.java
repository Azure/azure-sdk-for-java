// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus.queue;

import com.google.common.collect.Sets;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageSession;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.ISessionHandler;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.integration.core.AzureCheckpointer;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusMessageHandler;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusTemplate;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
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
import java.util.function.Consumer;

/**
 * Default implementation of {@link ServiceBusQueueOperation}.
 *
 * @author Warren Zhu
 * @author Eduardo Sciullo
 */
public class ServiceBusQueueTemplate extends ServiceBusTemplate<ServiceBusQueueClientFactory>
    implements ServiceBusQueueOperation {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceBusQueueTemplate.class);

    private static final String MSG_FAIL_CHECKPOINT = "Failed to checkpoint %s in queue '%s'";

    private static final String MSG_SUCCESS_CHECKPOINT = "Checkpointed %s in queue '%s' in %s mode";

    private final Set<String> subscribedQueues = Sets.newConcurrentHashSet();

    public ServiceBusQueueTemplate(ServiceBusQueueClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean subscribe(String destination, @NonNull Consumer<Message<?>> consumer,
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void internalSubscribe(String name, Consumer<Message<?>> consumer, Class<?> payloadType) {

        IQueueClient queueClient = this.senderFactory.getOrCreateClient(name);

        String threadPrefix = String.format("%s-handler", name);

        try {
            queueClient.setPrefetchCount(this.clientConfig.getPrefetchCount());

            // Register SessionHandler if sessions are enabled.
            // Handlers are mutually exclusive.
            if (this.clientConfig.isSessionsEnabled()) {
                queueClient.registerSessionHandler(
                    new QueueMessageHandler(consumer, payloadType, queueClient), buildSessionHandlerOptions(),
                    buildHandlerExecutors(threadPrefix));
            } else {
                queueClient.registerMessageHandler(new QueueMessageHandler(consumer, payloadType, queueClient),
                    buildHandlerOptions(), buildHandlerExecutors(threadPrefix));
            }
        } catch (ServiceBusException | InterruptedException e) {
            LOG.error("Failed to register queue message handler", e);
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
            LOG.info("Closed session '" + session.getSessionId() + "' for subscription: " + session.getEntityPath());
            return CompletableFuture.completedFuture(null);
        }
    }

}
