// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.topic;

import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.ServiceBusMessageHandler;
import com.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import com.azure.spring.integration.servicebus.ServiceBusTemplate;
import com.google.common.collect.Sets;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageSession;
import com.microsoft.azure.servicebus.ISessionHandler;
import com.microsoft.azure.servicebus.ISubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.azure.spring.cloud.context.core.util.Tuple;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * Default implementation of {@link ServiceBusTopicOperation}.
 *
 * @author Warren Zhu
 * @author Eduardo Sciullo
 */
public class ServiceBusTopicTemplate extends ServiceBusTemplate<ServiceBusTopicClientFactory>
    implements ServiceBusTopicOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusTopicTemplate.class);

    private static final String MSG_FAIL_CHECKPOINT = "Consumer group '%s' of topic '%s' failed to checkpoint %s";

    private static final String MSG_SUCCESS_CHECKPOINT = "Consumer group '%s' of topic '%s' checkpointed %s in %s mode";

    private final Set<Tuple<String, String>> nameAndConsumerGroups = Sets.newConcurrentHashSet();

    public ServiceBusTopicTemplate(ServiceBusTopicClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public boolean subscribe(String destination, String consumerGroup, @NonNull Consumer<Message<?>> consumer,
                             Class<?> payloadType) {
        Assert.hasText(destination, "destination can't be null or empty");

        Tuple<String, String> nameAndConsumerGroup = Tuple.of(destination, consumerGroup);

        if (nameAndConsumerGroups.contains(nameAndConsumerGroup)) {
            return false;
        }

        nameAndConsumerGroups.add(nameAndConsumerGroup);

        internalSubscribe(destination, consumerGroup, consumer, payloadType);
        return true;
    }

    @Override
    public boolean unsubscribe(String destination, String consumerGroup) {
        // TODO: unregister message handler but service bus sdk unsupported

        return nameAndConsumerGroups.remove(Tuple.of(destination, consumerGroup));
    }

    /**
     * Register a message handler to receive message from the topic. A session handler will be registered if session is
     * enabled.
     * @param name The topic name.
     * @param consumerGroup The consumer group.
     * @param consumer The consumer method.
     * @param payloadType The type of the message payload.
     * @throws ServiceBusRuntimeException If fail to register the topic message handler.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void internalSubscribe(String name, String consumerGroup, Consumer<Message<?>> consumer,
                                     Class<?> payloadType) {
        ISubscriptionClient subscriptionClient = this.senderFactory.getOrCreateSubscriptionClient(name, consumerGroup);

        String threadPrefix = String.format("%s-%s-handler", name, consumerGroup);

        try {
            subscriptionClient.setPrefetchCount(this.clientConfig.getPrefetchCount());

            // Register SessionHandler id sessions are enabled.
            // Handlers are mutually exclusive.
            final TopicMessageHandler msgHandler = new TopicMessageHandler(consumer, payloadType, subscriptionClient);
            final ExecutorService executors = buildHandlerExecutors(threadPrefix);

            if (this.clientConfig.isSessionsEnabled()) {
                subscriptionClient.registerSessionHandler(msgHandler, buildSessionHandlerOptions(), executors);
            } else {
                subscriptionClient.registerMessageHandler(msgHandler, buildHandlerOptions(), executors);
            }
        } catch (ServiceBusException | InterruptedException e) {
            LOGGER.error("Failed to register topic message handler", e);
            throw new ServiceBusRuntimeException("Failed to register topic message handler", e);
        }
    }

    @Override
    public void setClientConfig(@NonNull ServiceBusClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    protected class TopicMessageHandler<U> extends ServiceBusMessageHandler<U> implements ISessionHandler {
        private final ISubscriptionClient subscriptionClient;

        public TopicMessageHandler(Consumer<Message<U>> consumer, Class<U> payloadType,
                                   ISubscriptionClient subscriptionClient) {
            super(consumer, payloadType, ServiceBusTopicTemplate.this.getCheckpointConfig(),
                ServiceBusTopicTemplate.this.getMessageConverter());
            this.subscriptionClient = subscriptionClient;
        }

        @Override
        protected CompletableFuture<Void> success(UUID uuid) {
            return subscriptionClient.completeAsync(uuid);
        }

        @Override
        protected CompletableFuture<Void> failure(UUID uuid) {
            return subscriptionClient.abandonAsync(uuid);
        }

        @Override
        protected String buildCheckpointFailMessage(Message<?> message) {
            return String.format(MSG_FAIL_CHECKPOINT, subscriptionClient.getSubscriptionName(),
                subscriptionClient.getTopicName(), message);
        }

        @Override
        protected String buildCheckpointSuccessMessage(Message<?> message) {
            return String.format(MSG_SUCCESS_CHECKPOINT, subscriptionClient.getSubscriptionName(),
                subscriptionClient.getTopicName(), message, getCheckpointConfig().getCheckpointMode());
        }

        // ISessionHandler
        @Override
        public CompletableFuture<Void> onMessageAsync(IMessageSession session, IMessage message) {
            return super.onMessageAsync(message);
        }


        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public CompletableFuture<Void> OnCloseSessionAsync(IMessageSession session) {
            LOGGER.info("Closed session '" + session.getSessionId() + "' for subscription: " + session.getEntityPath());
            return CompletableFuture.completedFuture(null);
        }
    }

}
