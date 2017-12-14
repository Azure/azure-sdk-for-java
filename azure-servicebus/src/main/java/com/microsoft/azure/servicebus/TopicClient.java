// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.StringUtil;
import com.microsoft.azure.servicebus.primitives.Util;

/**
 * The topic client that interacts with service bus topic.
 */
public final class TopicClient extends InitializableEntity implements ITopicClient {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(TopicClient.class);
    private IMessageSender sender;
    private MessageBrowser browser;

    private TopicClient() {
        super(StringUtil.getShortRandomString(), null);
    }

    public TopicClient(ConnectionStringBuilder amqpConnectionStringBuilder) throws InterruptedException, ServiceBusException {
        this();
        this.sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(amqpConnectionStringBuilder);
        this.browser = new MessageBrowser((MessageSender) sender);
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("Created topic client to connection string '{}'", amqpConnectionStringBuilder.toLoggableString());
        }
    }
    
    public TopicClient(String namespace, String topicPath, ClientSettings clientSettings) throws InterruptedException, ServiceBusException
    {
        this(Util.convertNamespaceToEndPointURI(namespace), topicPath, clientSettings);
    }
    
    public TopicClient(URI namespaceEndpointURI, String topicPath, ClientSettings clientSettings) throws InterruptedException, ServiceBusException
    {
        this();
        this.sender = ClientFactory.createMessageSenderFromEntityPath(namespaceEndpointURI, topicPath, clientSettings);
        this.browser = new MessageBrowser((MessageSender) sender);
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("Created topic client to topic '{}/{}'", namespaceEndpointURI.toString(), topicPath);
        }
    }

    TopicClient(MessagingFactory factory, String topicPath) throws InterruptedException, ServiceBusException {
        this();
        this.sender = ClientFactory.createMessageSenderFromEntityPath(factory, topicPath);
        this.browser = new MessageBrowser((MessageSender) sender);
        TRACE_LOGGER.info("Created topic client to topic '{}'", topicPath);
    }

    @Override
    public void send(IMessage message) throws InterruptedException, ServiceBusException {
        this.sender.send(message);
    }

    @Override
    public void sendBatch(Collection<? extends IMessage> messages) throws InterruptedException, ServiceBusException {
        this.sender.sendBatch(messages);
    }

    @Override
    public CompletableFuture<Void> sendAsync(IMessage message) {
        return this.sender.sendAsync(message);
    }

    @Override
    public CompletableFuture<Void> sendBatchAsync(Collection<? extends IMessage> messages) {
        return this.sender.sendBatchAsync(messages);
    }

    @Override
    public CompletableFuture<Long> scheduleMessageAsync(IMessage message, Instant scheduledEnqueueTimeUtc) {
        return this.sender.scheduleMessageAsync(message, scheduledEnqueueTimeUtc);
    }

    @Override
    public CompletableFuture<Void> cancelScheduledMessageAsync(long sequenceNumber) {
        return this.sender.cancelScheduledMessageAsync(sequenceNumber);
    }

    @Override
    public long scheduleMessage(IMessage message, Instant scheduledEnqueueTimeUtc) throws InterruptedException, ServiceBusException {
        return this.sender.scheduleMessage(message, scheduledEnqueueTimeUtc);
    }

    @Override
    public void cancelScheduledMessage(long sequenceNumber) throws InterruptedException, ServiceBusException {
        this.sender.cancelScheduledMessage(sequenceNumber);
    }

    @Override
    public String getEntityPath() {
        return this.sender.getEntityPath();
    }

    @Override
    public IMessage peek() throws InterruptedException, ServiceBusException {
        return this.browser.peek();
    }

    @Override
    public IMessage peek(long fromSequenceNumber) throws InterruptedException, ServiceBusException {
        return this.browser.peek(fromSequenceNumber);
    }

    @Override
    public Collection<IMessage> peekBatch(int messageCount) throws InterruptedException, ServiceBusException {
        return this.browser.peekBatch(messageCount);
    }

    @Override
    public Collection<IMessage> peekBatch(long fromSequenceNumber, int messageCount) throws InterruptedException, ServiceBusException {
        return this.browser.peekBatch(fromSequenceNumber, messageCount);
    }

    @Override
    public CompletableFuture<IMessage> peekAsync() {
        return this.browser.peekAsync();
    }

    @Override
    public CompletableFuture<IMessage> peekAsync(long fromSequenceNumber) {
        return this.browser.peekAsync(fromSequenceNumber);
    }

    @Override
    public CompletableFuture<Collection<IMessage>> peekBatchAsync(int messageCount) {
        return this.browser.peekBatchAsync(messageCount);
    }

    @Override
    public CompletableFuture<Collection<IMessage>> peekBatchAsync(long fromSequenceNumber, int messageCount) {
        return this.browser.peekBatchAsync(fromSequenceNumber, messageCount);
    }

    // No Op now
    @Override
    CompletableFuture<Void> initializeAsync() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected CompletableFuture<Void> onClose() {
        return this.sender.closeAsync();
    }

    @Override
    public String getTopicName() {
        return this.getEntityPath();
    }
}
