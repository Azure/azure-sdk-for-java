// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.primitives.CoreMessageSender;
import com.microsoft.azure.servicebus.primitives.ExceptionUtil;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.StringUtil;

final class MessageSender extends InitializableEntity implements IMessageSender {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(MessageSender.class);
    private boolean ownsMessagingFactory;
    private String entityPath = null;
    private MessagingFactory messagingFactory = null;
    private CoreMessageSender internalSender = null;
    private boolean isInitialized = false;
    private URI namespaceEndpointURI;
    private ClientSettings clientSettings;

    private MessageSender() {
        super(StringUtil.getShortRandomString());
    }

    MessageSender(URI namespaceEndpointURI, String entityPath, ClientSettings clientSettings) {
        this();

        this.namespaceEndpointURI = namespaceEndpointURI;
        this.entityPath = entityPath;
        this.clientSettings = clientSettings;
        this.ownsMessagingFactory = true;
    }

    MessageSender(MessagingFactory messagingFactory, String entityPath) {
        this(messagingFactory, entityPath, false);
    }

    private MessageSender(MessagingFactory messagingFactory, String entityPath, boolean ownsMessagingFactory) {
        this();

        this.messagingFactory = messagingFactory;
        this.entityPath = entityPath;
        this.ownsMessagingFactory = ownsMessagingFactory;
    }

    @Override
    synchronized CompletableFuture<Void> initializeAsync() {
        if (this.isInitialized) {
            return CompletableFuture.completedFuture(null);
        } else {
            CompletableFuture<Void> factoryFuture;
            if (this.messagingFactory == null) {
                if (TRACE_LOGGER.isInfoEnabled()) {
                    TRACE_LOGGER.info("Creating MessagingFactory to namespace '{}'", this.namespaceEndpointURI.toString());
                }
                factoryFuture = MessagingFactory.createFromNamespaceEndpointURIAsyc(this.namespaceEndpointURI, this.clientSettings).thenAcceptAsync((f) ->
                {
                    this.messagingFactory = f;
                    if (TRACE_LOGGER.isInfoEnabled()) {
                        TRACE_LOGGER.info("Created MessagingFactory to namespace '{}'", this.namespaceEndpointURI.toString());
                    }
                });
            } else {
                factoryFuture = CompletableFuture.completedFuture(null);
            }

            return factoryFuture.thenComposeAsync((v) ->
            {
                TRACE_LOGGER.info("Creating MessageSender to entity '{}'", this.entityPath);
                CompletableFuture<CoreMessageSender> senderFuture = CoreMessageSender.create(this.messagingFactory, StringUtil.getShortRandomString(), this.entityPath);
                CompletableFuture<Void> postSenderCreationFuture = new CompletableFuture<Void>();
                senderFuture.handleAsync((s, coreSenderCreationEx) -> {
                    if (coreSenderCreationEx == null) {
                        this.internalSender = s;
                        this.isInitialized = true;
                        TRACE_LOGGER.info("Created MessageSender to entity '{}'", this.entityPath);
                        postSenderCreationFuture.complete(null);
                    } else {
                        Throwable cause = ExceptionUtil.extractAsyncCompletionCause(coreSenderCreationEx);
                        TRACE_LOGGER.error("Creating MessageSender to entity '{}' failed", this.entityPath, cause);
                        if (this.ownsMessagingFactory) {
                            // Close factory
                            this.messagingFactory.closeAsync();
                        }
                        postSenderCreationFuture.completeExceptionally(cause);
                    }
                    return null;
                });
                return postSenderCreationFuture;
            });
        }
    }

    final CoreMessageSender getInternalSender() {
        return this.internalSender;
    }

    @Override
    public void send(IMessage message) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.sendAsync(message));
    }

    @Override
    public void sendBatch(Collection<? extends IMessage> message) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.sendBatchAsync(message));
    }

    @Override
    public CompletableFuture<Void> sendAsync(IMessage message) {
        org.apache.qpid.proton.message.Message amqpMessage = MessageConverter.convertBrokeredMessageToAmqpMessage((Message) message);
        return this.internalSender.sendAsync(amqpMessage);
    }

    @Override
    public CompletableFuture<Void> sendBatchAsync(Collection<? extends IMessage> messages) {
        ArrayList<org.apache.qpid.proton.message.Message> convertedMessages = new ArrayList<org.apache.qpid.proton.message.Message>();
        for (IMessage message : messages) {
            convertedMessages.add(MessageConverter.convertBrokeredMessageToAmqpMessage((Message) message));
        }

        return this.internalSender.sendAsync(convertedMessages);
    }

    @Override
    protected CompletableFuture<Void> onClose() {
        if (this.isInitialized) {
            TRACE_LOGGER.info("Closing message sender to entity '{}'", this.entityPath);
            return this.internalSender.closeAsync().thenComposeAsync((v) ->
            {
                TRACE_LOGGER.info("Closed message sender to entity '{}'", this.entityPath);
                if (MessageSender.this.ownsMessagingFactory) {
                    if (TRACE_LOGGER.isInfoEnabled()) {
                        TRACE_LOGGER.info("Closing MessagingFactory associated with namespace '{}'", this.namespaceEndpointURI.toString());
                    }

                    return MessageSender.this.messagingFactory.closeAsync();
                } else {
                    return CompletableFuture.completedFuture(null);
                }
            });
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public String getEntityPath() {
        return this.entityPath;
    }

    @Override
    public CompletableFuture<Long> scheduleMessageAsync(IMessage message, Instant scheduledEnqueueTimeUtc) {
        message.setScheduledEnqueuedTimeUtc(scheduledEnqueueTimeUtc);
        org.apache.qpid.proton.message.Message amqpMessage = MessageConverter.convertBrokeredMessageToAmqpMessage((Message) message);
        return this.internalSender.scheduleMessageAsync(new org.apache.qpid.proton.message.Message[]{amqpMessage}, this.messagingFactory.getClientSetttings().getOperationTimeout()).thenApply(sequenceNumbers -> sequenceNumbers[0]);
    }

    @Override
    public CompletableFuture<Void> cancelScheduledMessageAsync(long sequenceNumber) {
        return this.internalSender.cancelScheduledMessageAsync(new Long[]{sequenceNumber}, this.messagingFactory.getClientSetttings().getOperationTimeout());
    }

    @Override
    public long scheduleMessage(IMessage message, Instant scheduledEnqueueTimeUtc) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(this.scheduleMessageAsync(message, scheduledEnqueueTimeUtc));
    }

    @Override
    public void cancelScheduledMessage(long sequenceNumber) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.cancelScheduledMessageAsync(sequenceNumber));
    }

    MessagingFactory getMessagingFactory() {
        return this.messagingFactory;
    }
}
