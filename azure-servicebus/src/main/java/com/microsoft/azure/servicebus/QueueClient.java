// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.net.URI;
import java.sql.Date;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ExceptionUtil;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.MiscRequestResponseOperationHandler;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.StringUtil;
import com.microsoft.azure.servicebus.primitives.Util;

public final class QueueClient extends InitializableEntity implements IQueueClient {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(QueueClient.class);
    private final ReceiveMode receiveMode;
    private final String queuePath;
    private final Object senderCreationLock;
    private MessagingFactory factory;
    private IMessageSender sender;
    private CompletableFuture<Void> senderCreationFuture;
    
    private MessageAndSessionPump messageAndSessionPump;
    private SessionBrowser sessionBrowser;
    private MiscRequestResponseOperationHandler miscRequestResponseHandler;

    private QueueClient(ReceiveMode receiveMode, String queuePath) {
        super(StringUtil.getShortRandomString());
        this.receiveMode = receiveMode;
        this.queuePath = queuePath;
        this.senderCreationLock = new Object();
    }
    
    public QueueClient(ConnectionStringBuilder amqpConnectionStringBuilder, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException {
        this(receiveMode, amqpConnectionStringBuilder.getEntityPath());
        CompletableFuture<MessagingFactory> factoryFuture = MessagingFactory.createFromConnectionStringBuilderAsync(amqpConnectionStringBuilder);
        Utils.completeFuture(factoryFuture.thenComposeAsync((f) -> this.createInternals(f, amqpConnectionStringBuilder.getEntityPath(), receiveMode)));
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("Created queue client to connection string '{}'", amqpConnectionStringBuilder.toLoggableString());
        }
    }
    
    public QueueClient(String namespace, String queuePath, ClientSettings clientSettings, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
    {
        this(Util.convertNamespaceToEndPointURI(namespace), queuePath, clientSettings, receiveMode);
    }
    
    public QueueClient(URI namespaceEndpointURI, String queuePath, ClientSettings clientSettings, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
    {
        this(receiveMode, queuePath);
        CompletableFuture<MessagingFactory> factoryFuture = MessagingFactory.createFromNamespaceEndpointURIAsyc(namespaceEndpointURI, clientSettings);
        Utils.completeFuture(factoryFuture.thenComposeAsync((f) -> this.createInternals(f, queuePath, receiveMode)));
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("Created queue client to queue '{}/{}'", namespaceEndpointURI.toString(), queuePath);
        }
    }

    QueueClient(MessagingFactory factory, String queuePath, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException {
        this(receiveMode, queuePath);
        Utils.completeFuture(this.createInternals(factory, queuePath, receiveMode));
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("Created queue client to queue '{}'", queuePath);
        }
    }

    private CompletableFuture<Void> createInternals(MessagingFactory factory, String queuePath, ReceiveMode receiveMode) {
        this.factory = factory;

        CompletableFuture<Void> postSessionBrowserFuture = MiscRequestResponseOperationHandler.create(factory, queuePath).thenAcceptAsync((msoh) -> {
            this.miscRequestResponseHandler = msoh;
            this.sessionBrowser = new SessionBrowser(factory, queuePath, msoh);
        });

        this.messageAndSessionPump = new MessageAndSessionPump(factory, queuePath, receiveMode);
        CompletableFuture<Void> messagePumpInitFuture = this.messageAndSessionPump.initializeAsync();

        return CompletableFuture.allOf(postSessionBrowserFuture, messagePumpInitFuture);
    }
    
    private CompletableFuture<Void> createSenderAsync()
    {
        synchronized (this.senderCreationLock) {
            if(this.senderCreationFuture == null)
            {
                this.senderCreationFuture = new CompletableFuture<Void>();
                ClientFactory.createMessageSenderFromEntityPathAsync(this.factory, this.queuePath).handleAsync((sender, ex) ->
                {
                    if(ex == null)
                    {
                        this.sender = sender;
                        this.senderCreationFuture.complete(null);
                    }
                    else
                    {
                        Throwable cause = ExceptionUtil.extractAsyncCompletionCause(ex);
                        this.senderCreationFuture.completeExceptionally(cause);
                        // Set it to null so next call will retry sender creation
                        synchronized (this.senderCreationLock)
                        {
                            this.senderCreationFuture = null;
                        }
                    }
                    return null;
                });
            }
            
            return this.senderCreationFuture;
        }
    }
    
    private CompletableFuture<Void> closeSenderAsync()
    {
        synchronized (this.senderCreationLock)
        {
            if(this.senderCreationFuture != null)
            {
                CompletableFuture<Void> senderCloseFuture = this.senderCreationFuture.thenComposeAsync((v) -> {
                    return this.sender.closeAsync();
                });
                this.senderCreationFuture = null;
                return senderCloseFuture;
            }
            else
            {
                return CompletableFuture.completedFuture(null);
            }
        }
    }

    @Override
    public ReceiveMode getReceiveMode() {
        return this.receiveMode;
    }

    @Override
    public void send(IMessage message) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.sendAsync(message));
    }

    @Override
    public void send(IMessage message, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.sendAsync(message, transaction));
    }

    @Override
    public void sendBatch(Collection<? extends IMessage> messages) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.sendBatchAsync(messages));
    }

    @Override
    public void sendBatch(Collection<? extends IMessage> messages, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.sendBatchAsync(messages, transaction));
    }

    @Override
    public CompletableFuture<Void> sendAsync(IMessage message) {
        return this.createSenderAsync().thenComposeAsync((v) ->
        {
            return this.sender.sendAsync(message);
        });
    }

    @Override
    public CompletableFuture<Void> sendAsync(IMessage message, TransactionContext transaction) {
        return this.createSenderAsync().thenComposeAsync((v) ->
        {
            return this.sender.sendAsync(message, transaction);
        });
    }

    @Override
    public CompletableFuture<Void> sendBatchAsync(Collection<? extends IMessage> messages) {
        return this.sendBatchAsync(messages, TransactionContext.NULL_TXN);
    }

    @Override
    public CompletableFuture<Void> sendBatchAsync(Collection<? extends IMessage> messages, TransactionContext transaction) {
        return this.createSenderAsync().thenComposeAsync((v) ->
        {
            return this.sender.sendBatchAsync(messages, transaction);
        });
    }

    @Override
    public CompletableFuture<Long> scheduleMessageAsync(IMessage message, Instant scheduledEnqueueTimeUtc) {
        return this.scheduleMessageAsync(message, scheduledEnqueueTimeUtc, TransactionContext.NULL_TXN);
    }

    @Override
    public CompletableFuture<Long> scheduleMessageAsync(IMessage message, Instant scheduledEnqueueTimeUtc, TransactionContext transaction) {
        return this.createSenderAsync().thenComposeAsync((v) ->
        {
            return this.sender.scheduleMessageAsync(message, scheduledEnqueueTimeUtc, transaction);
        });
    }

    @Override
    public CompletableFuture<Void> cancelScheduledMessageAsync(long sequenceNumber) {
        return this.cancelScheduledMessageAsync(sequenceNumber, TransactionContext.NULL_TXN);
    }

    @Override
    public CompletableFuture<Void> cancelScheduledMessageAsync(long sequenceNumber, TransactionContext transaction) {
        return this.createSenderAsync().thenComposeAsync((v) ->
        {
            return this.sender.cancelScheduledMessageAsync(sequenceNumber, transaction);
        });
    }

    @Override
    public long scheduleMessage(IMessage message, Instant scheduledEnqueueTimeUtc) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(this.scheduleMessageAsync(message, scheduledEnqueueTimeUtc));
    }

    @Override
    public long scheduleMessage(IMessage message, Instant scheduledEnqueueTimeUtc, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(this.scheduleMessageAsync(message, scheduledEnqueueTimeUtc, transaction));
    }

    @Override
    public void cancelScheduledMessage(long sequenceNumber) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.cancelScheduledMessageAsync(sequenceNumber));
    }

    @Override
    public void cancelScheduledMessage(long sequenceNumber, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.cancelScheduledMessageAsync(sequenceNumber, transaction));
    }

    @Override
    public String getEntityPath() {
        return this.queuePath;
    }

    @Override
    public void registerMessageHandler(IMessageHandler handler) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.registerMessageHandler(handler);
    }

    @Override
    public void registerMessageHandler(IMessageHandler handler, MessageHandlerOptions handlerOptions) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.registerMessageHandler(handler, handlerOptions);
    }

    @Override
    public void registerSessionHandler(ISessionHandler handler) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.registerSessionHandler(handler);
    }

    @Override
    public void registerSessionHandler(ISessionHandler handler, SessionHandlerOptions handlerOptions) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.registerSessionHandler(handler, handlerOptions);
    }

    // No op now
    @Override
    CompletableFuture<Void> initializeAsync() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected CompletableFuture<Void> onClose() {
        return this.messageAndSessionPump.closeAsync().thenCompose((v) -> this.closeSenderAsync().thenCompose((u) -> this.miscRequestResponseHandler.closeAsync().thenCompose((w) -> this.factory.closeAsync())));
    }

    //	@Override
    Collection<IMessageSession> getMessageSessions() throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(this.getMessageSessionsAsync());
    }

    //	@Override
    Collection<IMessageSession> getMessageSessions(Instant lastUpdatedTime) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(this.getMessageSessionsAsync(lastUpdatedTime));
    }

    //	@Override
    CompletableFuture<Collection<IMessageSession>> getMessageSessionsAsync() {
        return this.sessionBrowser.getMessageSessionsAsync();
    }

    //	@Override
    CompletableFuture<Collection<IMessageSession>> getMessageSessionsAsync(Instant lastUpdatedTime) {
        return this.sessionBrowser.getMessageSessionsAsync(Date.from(lastUpdatedTime));
    }

    @Override
    public void abandon(UUID lockToken) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.abandon(lockToken);
    }

    @Override
    public void abandon(UUID lockToken, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.abandon(lockToken, transaction);
    }

    @Override
    public void abandon(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.abandon(lockToken, propertiesToModify);
    }

    @Override
    public void abandon(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.abandon(lockToken, propertiesToModify, transaction);
    }

    @Override
    public CompletableFuture<Void> abandonAsync(UUID lockToken) {
        return this.messageAndSessionPump.abandonAsync(lockToken);
    }

    @Override
    public CompletableFuture<Void> abandonAsync(UUID lockToken, TransactionContext transaction) {
        return this.messageAndSessionPump.abandonAsync(lockToken, transaction);
    }

    @Override
    public CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
        return this.messageAndSessionPump.abandonAsync(lockToken, propertiesToModify);
    }

    @Override
    public CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) {
        return this.messageAndSessionPump.abandonAsync(lockToken, propertiesToModify, transaction);
    }

    @Override
    public void complete(UUID lockToken) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.complete(lockToken);
    }

    @Override
    public void complete(UUID lockToken, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.complete(lockToken, transaction);
    }

    @Override
    public CompletableFuture<Void> completeAsync(UUID lockToken) {
        return this.messageAndSessionPump.completeAsync(lockToken);
    }

    @Override
    public CompletableFuture<Void> completeAsync(UUID lockToken, TransactionContext transaction) {
        return this.messageAndSessionPump.completeAsync(lockToken, transaction);
    }

    //	@Override
    void defer(UUID lockToken) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.defer(lockToken);
    }

    //	@Override
    void defer(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.defer(lockToken, propertiesToModify);
    }

//    @Override
//    public CompletableFuture<Void> deferAsync(UUID lockToken) {
//        return this.messageAndSessionPump.deferAsync(lockToken);
//    }
//
//    @Override
//    public CompletableFuture<Void> deferAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
//        return this.messageAndSessionPump.deferAsync(lockToken, propertiesToModify);
//    }

    @Override
    public void deadLetter(UUID lockToken) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.deadLetter(lockToken);
    }

    @Override
    public void deadLetter(UUID lockToken, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.deadLetter(lockToken, transaction);
    }

    @Override
    public void deadLetter(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.deadLetter(lockToken, propertiesToModify);
    }

    @Override
    public void deadLetter(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.deadLetter(lockToken, propertiesToModify, transaction);
    }

    @Override
    public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.deadLetter(lockToken, deadLetterReason, deadLetterErrorDescription);
    }

    @Override
    public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.deadLetter(lockToken, deadLetterReason, deadLetterErrorDescription, transaction);
    }

    @Override
    public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.deadLetter(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify);
    }

    @Override
    public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        this.messageAndSessionPump.deadLetter(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify, transaction);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken) {
        return this.messageAndSessionPump.deadLetterAsync(lockToken);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken, TransactionContext transaction) {
        return this.messageAndSessionPump.deadLetterAsync(lockToken, transaction);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
        return this.messageAndSessionPump.deadLetterAsync(lockToken, propertiesToModify);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) {
        return this.messageAndSessionPump.deadLetterAsync(lockToken, propertiesToModify, transaction);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription) {
        return this.messageAndSessionPump.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, TransactionContext transaction) {
        return this.messageAndSessionPump.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, transaction);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) {
        return this.messageAndSessionPump.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify, TransactionContext transaction) {
        return this.messageAndSessionPump.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify, transaction);
    }

    @Override
    public int getPrefetchCount() {
        return this.messageAndSessionPump.getPrefetchCount();
    }

    @Override
    public void setPrefetchCount(int prefetchCount) throws ServiceBusException {
        this.messageAndSessionPump.setPrefetchCount(prefetchCount);
    }

    @Override
    public String getQueueName() {
        return this.getEntityPath();
    }
}
