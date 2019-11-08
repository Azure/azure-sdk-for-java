// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.primitives.ClientConstants;
import com.microsoft.azure.servicebus.primitives.CoreMessageReceiver;
import com.microsoft.azure.servicebus.primitives.MessageWithDeliveryTag;
import com.microsoft.azure.servicebus.primitives.MessageWithLockToken;
import com.microsoft.azure.servicebus.primitives.MessagingEntityType;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.SettleModePair;
import com.microsoft.azure.servicebus.primitives.StringUtil;
import com.microsoft.azure.servicebus.primitives.Timer;
import com.microsoft.azure.servicebus.primitives.TimerType;
import com.microsoft.azure.servicebus.primitives.Util;

class MessageReceiver extends InitializableEntity implements IMessageReceiver, IMessageBrowser {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(MessageReceiver.class);
    // Using 0 pre-fetch count for both receive modes, to avoid message lock lost exceptions in application receiving messages at a slow rate.
    // Applications can set it to a higher value if they need better performance.
    private static final int DEFAULT_PREFETCH_COUNT_PEEKLOCK = 0;
    private static final int DEFAULT_PREFETCH_COUNT_RECEIVEANDDELETE = 0;

    private final ReceiveMode receiveMode;
    private boolean ownsMessagingFactory;
    private URI namespaceEndpointURI;
    private ClientSettings clientSettings;
    private String entityPath = null;
    private MessagingEntityType entityType = null;
    private MessagingFactory messagingFactory = null;
    private CoreMessageReceiver internalReceiver = null;
    private boolean isInitialized = false;
    private MessageBrowser browser = null;
    private int messagePrefetchCount;
    private ScheduledFuture<?> requestResponseLockTokenPruner = null;

    private final ConcurrentHashMap<UUID, Instant> requestResponseLockTokensToLockTimesMap;

    private MessageReceiver(ReceiveMode receiveMode) {
        super(StringUtil.getShortRandomString());
        this.receiveMode = receiveMode;
        this.requestResponseLockTokensToLockTimesMap = new ConcurrentHashMap<>();
        if (receiveMode == ReceiveMode.PEEKLOCK) {
            this.messagePrefetchCount = DEFAULT_PREFETCH_COUNT_PEEKLOCK;
        } else {
            this.messagePrefetchCount = DEFAULT_PREFETCH_COUNT_RECEIVEANDDELETE;
        }
    }

    private MessageReceiver(MessagingFactory messagingFactory, String entityPath, MessagingEntityType entityType, boolean ownsMessagingFactory, ReceiveMode receiveMode) {
        this(receiveMode);

        this.messagingFactory = messagingFactory;
        this.entityPath = entityPath;
        this.entityType = entityType;
        this.ownsMessagingFactory = ownsMessagingFactory;
    }
    
    MessageReceiver(URI namespaceEndpointURI, String entityPath, MessagingEntityType entityType, ClientSettings clientSettings, ReceiveMode receiveMode) {
        this(receiveMode);

        this.namespaceEndpointURI = namespaceEndpointURI;
        this.clientSettings = clientSettings;
        this.entityPath = entityPath;
        this.entityType = entityType;
        this.ownsMessagingFactory = true;
    }

    MessageReceiver(MessagingFactory messagingFactory, String entityPath, MessagingEntityType entityType, ReceiveMode receiveMode) {
        this(messagingFactory, entityPath, entityType, false, receiveMode);
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
                factoryFuture = MessagingFactory.createFromNamespaceEndpointURIAsyc(this.namespaceEndpointURI, this.clientSettings).thenAcceptAsync((f) -> {
                    this.messagingFactory = f;
                    if (TRACE_LOGGER.isInfoEnabled()) {
                        TRACE_LOGGER.info("Created MessagingFactory to namespace '{}'", this.namespaceEndpointURI.toString());
                    }
                });
            } else {
                factoryFuture = CompletableFuture.completedFuture(null);
            }

            return factoryFuture.thenComposeAsync((v) -> {
                CompletableFuture<CoreMessageReceiver> acceptReceiverFuture;
                if (this.internalReceiver == null) {

                    CompletableFuture<CoreMessageReceiver> receiverFuture;
                    if (MessageReceiver.this.isSessionReceiver()) {
                        TRACE_LOGGER.info("Creating SessionReceiver to entity '{}', requestedSessionId '{}', browsable session '{}', ReceiveMode '{}'", this.entityPath, this.getRequestedSessionId(), this.isBrowsableSession(), this.receiveMode);
                        receiverFuture = CoreMessageReceiver.create(this.messagingFactory, StringUtil.getShortRandomString(), this.entityPath, this.getRequestedSessionId(), this.isBrowsableSession(), this.messagePrefetchCount, getSettleModePairForRecevieMode(this.receiveMode), this.entityType);
                    } else {
                        TRACE_LOGGER.info("Creating MessageReceiver to entity '{}', ReceiveMode '{}'", this.entityPath, this.receiveMode);
                        receiverFuture = CoreMessageReceiver.create(this.messagingFactory, StringUtil.getShortRandomString(), this.entityPath, this.messagePrefetchCount, getSettleModePairForRecevieMode(this.receiveMode), this.entityType);
                    }

                    acceptReceiverFuture = receiverFuture.whenCompleteAsync((r, coreReceiverCreationEx) -> {
                        if (coreReceiverCreationEx == null) {
                            this.internalReceiver = r;
                            if (MessageReceiver.this.isSessionReceiver()) {
                                TRACE_LOGGER.info("Created SessionReceiver to entity '{}', requestedSessionId '{}', browsable session '{}', acceptedSessionId '{}'", this.entityPath, this.getRequestedSessionId(), this.isBrowsableSession(), this.internalReceiver.getSessionId());
                            } else {
                                TRACE_LOGGER.info("Created MessageReceiver to entity '{}'", this.entityPath);
                            }
                        } else {
                            if (this.ownsMessagingFactory) {
                                //Close factory
                                this.messagingFactory.closeAsync();
                            }
                        }
                    }, MessagingFactory.INTERNAL_THREAD_POOL);
                } else {
                    acceptReceiverFuture = CompletableFuture.completedFuture(null);
                }

                return acceptReceiverFuture.thenRunAsync(() -> {
                    this.isInitialized = true;
                    this.schedulePruningRequestResponseLockTokens();
                    this.browser = new MessageBrowser(this);
                    if (MessageReceiver.this.isSessionReceiver()) {
                        TRACE_LOGGER.info("Created MessageBrowser to entity '{}', sessionid '{}'", this.entityPath, this.internalReceiver.getSessionId());
                    } else {
                        TRACE_LOGGER.info("Created MessageBrowser to entity '{}'", this.entityPath);
                    }
                }, MessagingFactory.INTERNAL_THREAD_POOL);
            }, MessagingFactory.INTERNAL_THREAD_POOL);
        }
    }

    protected boolean isSessionReceiver() {
        return false;
    }

    protected boolean isBrowsableSession() {
        return false;
    }

    protected String getRequestedSessionId() {
        return null;
    }

    protected final CoreMessageReceiver getInternalReceiver() {
        return this.internalReceiver;
    }

    @Override
    public String getEntityPath() {
        return this.entityPath;
    }

    @Override
    public ReceiveMode getReceiveMode() {
        return this.receiveMode;
    }

    @Override
    public void abandon(UUID lockToken) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.abandonAsync(lockToken));
    }

    @Override
    public void abandon(UUID lockToken, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.abandonAsync(lockToken, transaction));
    }

    @Override
    public void abandon(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.abandonAsync(lockToken, propertiesToModify));
    }

    @Override
    public void abandon(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.abandonAsync(lockToken, propertiesToModify, transaction));
    }

    @Override
    public CompletableFuture<Void> abandonAsync(UUID lockToken) {
        return this.abandonAsync(lockToken, TransactionContext.NULL_TXN);
    }

    @Override
    public CompletableFuture<Void> abandonAsync(UUID lockToken, TransactionContext transaction) {
        return this.abandonAsync(lockToken, null, transaction);
    }

    @Override
    public CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
        return this.abandonAsync(lockToken, propertiesToModify, TransactionContext.NULL_TXN);
    }

    @Override
    public CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) {
        this.ensurePeekLockReceiveMode();
        TRACE_LOGGER.debug("Abandoning message with lock token '{}'", lockToken);
        return this.checkIfValidRequestResponseLockTokenAsync(lockToken).thenCompose((requestResponseLocked) -> {
            if (requestResponseLocked) {
                return this.internalReceiver.abandonMessageAsync(lockToken, propertiesToModify, transaction)
                        .thenRun(() -> this.disposeLockToken(lockToken, transaction));
            } else {
                return this.internalReceiver.abandonMessageAsync(Util.convertUUIDToDotNetBytes(lockToken), propertiesToModify, transaction);
            }
        });
    }

    @Override
    public void complete(UUID lockToken) throws InterruptedException, ServiceBusException {
        this.complete(lockToken, TransactionContext.NULL_TXN);
    }

    @Override
    public void complete(UUID lockToken, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.completeAsync(lockToken, transaction));
    }

    /*
    @Override
    public void completeBatch(Collection<? extends IMessage> messages) {
    }
    */

    @Override
    public CompletableFuture<Void> completeAsync(UUID lockToken) {
        return this.completeAsync(lockToken, TransactionContext.NULL_TXN);
    }

    @Override
    public CompletableFuture<Void> completeAsync(UUID lockToken, TransactionContext transaction) {
        this.ensurePeekLockReceiveMode();
        TRACE_LOGGER.debug("Completing message with lock token '{}'", lockToken);
        return this.checkIfValidRequestResponseLockTokenAsync(lockToken).thenCompose((requestResponseLocked) -> {
            if (requestResponseLocked) {
                return this.internalReceiver.completeMessageAsync(lockToken, transaction)
                        .thenRun(() -> this.disposeLockToken(lockToken, transaction));
            } else {
                return this.internalReceiver.completeMessageAsync(Util.convertUUIDToDotNetBytes(lockToken), transaction);
            }
        });
    }

    /*
    @Override
    public CompletableFuture<Void> completeBatchAsync(Collection<? extends IMessage> messages) {
        // TODO: Auto-generated method stub
        return null;
    }
    */

    @Override
    public void defer(UUID lockToken) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.deferAsync(lockToken));
    }

    @Override
    public void defer(UUID lockToken, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.deferAsync(lockToken, transaction));
    }

    @Override
    public void defer(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.deferAsync(lockToken, propertiesToModify));
    }

    @Override
    public void defer(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.deferAsync(lockToken, propertiesToModify, transaction));
    }

    @Override
    public CompletableFuture<Void> deferAsync(UUID lockToken) {
        return this.deferAsync(lockToken, null, TransactionContext.NULL_TXN);
    }

    @Override
    public CompletableFuture<Void> deferAsync(UUID lockToken, TransactionContext transaction) {
        return this.deferAsync(lockToken, null, transaction);
    }

    @Override
    public CompletableFuture<Void> deferAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
        return this.deferAsync(lockToken, propertiesToModify, TransactionContext.NULL_TXN);
    }

    @Override
    public CompletableFuture<Void> deferAsync(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) {
        this.ensurePeekLockReceiveMode();
        TRACE_LOGGER.debug("Deferring message with lock token '{}'", lockToken);
        return this.checkIfValidRequestResponseLockTokenAsync(lockToken).thenCompose((requestResponseLocked) -> {
            if (requestResponseLocked) {
                return this.internalReceiver.deferMessageAsync(lockToken, propertiesToModify, transaction)
                        .thenRun(() -> this.disposeLockToken(lockToken, transaction));
            } else {
                return this.internalReceiver.deferMessageAsync(Util.convertUUIDToDotNetBytes(lockToken), propertiesToModify, transaction);
            }
        });
    }

    @Override
    public void deadLetter(UUID lockToken) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.deadLetterAsync(lockToken));
    }

    @Override
    public void deadLetter(UUID lockToken, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.deadLetterAsync(lockToken, transaction));
    }

    @Override
    public void deadLetter(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.deadLetterAsync(lockToken, propertiesToModify));
    }

    @Override
    public void deadLetter(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.deadLetterAsync(lockToken, propertiesToModify, transaction));
    }

    @Override
    public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription));
    }

    @Override
    public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, transaction));
    }

    @Override
    public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify));
    }

    @Override
    public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify, TransactionContext transaction) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify, transaction));
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken) {
        return this.deadLetterAsync(lockToken, null, null, null, TransactionContext.NULL_TXN);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken, TransactionContext transaction) {
        return this.deadLetterAsync(lockToken, null, null, null, transaction);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
        return this.deadLetterAsync(lockToken, null, null, propertiesToModify, TransactionContext.NULL_TXN);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) {
        return this.deadLetterAsync(lockToken, null, null, propertiesToModify, transaction);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription) {
        return this.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, null, TransactionContext.NULL_TXN);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, TransactionContext transaction) {
        return this.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, null, TransactionContext.NULL_TXN);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) {
        return this.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify, TransactionContext.NULL_TXN);
    }

    @Override
    public CompletableFuture<Void> deadLetterAsync(
            UUID lockToken,
            String deadLetterReason,
            String deadLetterErrorDescription,
            Map<String, Object> propertiesToModify,
            TransactionContext transaction) {
        this.ensurePeekLockReceiveMode();
        TRACE_LOGGER.debug("Deadlettering message with lock token '{}'", lockToken);
        return this.checkIfValidRequestResponseLockTokenAsync(lockToken).thenCompose((requestResponseLocked) -> {
            if (requestResponseLocked) {
                return this.internalReceiver.deadLetterMessageAsync(
                        lockToken,
                        deadLetterReason,
                        deadLetterErrorDescription,
                        propertiesToModify,
                        transaction)
                        .thenRun(() -> this.disposeLockToken(lockToken, transaction));
            } else {
                return this.internalReceiver.deadLetterMessageAsync(
                        Util.convertUUIDToDotNetBytes(lockToken),
                        deadLetterReason,
                        deadLetterErrorDescription,
                        propertiesToModify,
                        transaction);
            }
        });
    }

    @Override
    public IMessage receive() throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(this.receiveAsync());
    }

    @Override
    public IMessage receive(Duration serverWaitTime) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(this.receiveAsync(serverWaitTime));
    }

    @Override
    public IMessage receiveDeferredMessage(long sequenceNumber) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.receiveDeferredMessageAsync(sequenceNumber));
    }

    @Override
    public Collection<IMessage> receiveBatch(int maxMessageCount) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(this.receiveBatchAsync(maxMessageCount));
    }

    @Override
    public Collection<IMessage> receiveBatch(int maxMessageCount, Duration serverWaitTime) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(this.receiveBatchAsync(maxMessageCount, serverWaitTime));
    }

    @Override
    public Collection<IMessage> receiveDeferredMessageBatch(Collection<Long> sequenceNumbers) throws ServiceBusException, InterruptedException {
        return Utils.completeFuture(this.receiveDeferredMessageBatchAsync(sequenceNumbers));
    }

    @Override
    public CompletableFuture<IMessage> receiveAsync() {
        return this.receiveAsync(this.messagingFactory.getOperationTimeout());
    }

    @Override
    public CompletableFuture<IMessage> receiveAsync(Duration serverWaitTime) {
        return this.internalReceiver.receiveAsync(1, serverWaitTime).thenApplyAsync(c -> {
            if (c == null) {
                return null;
            } else if (c.isEmpty()) {
                return null;
            } else {
                return MessageConverter.convertAmqpMessageToBrokeredMessage(c.toArray(new MessageWithDeliveryTag[0])[0]);
            }
        }, MessagingFactory.INTERNAL_THREAD_POOL);
    }

    @Override
    public CompletableFuture<Collection<IMessage>> receiveBatchAsync(int maxMessageCount) {
        return this.receiveBatchAsync(maxMessageCount, this.messagingFactory.getOperationTimeout());
    }

    @Override
    public CompletableFuture<Collection<IMessage>> receiveBatchAsync(int maxMessageCount, Duration serverWaitTime) {
        return this.internalReceiver.receiveAsync(maxMessageCount, serverWaitTime).thenApplyAsync(c -> {
            if (c == null) {
                return null;
            } else if (c.isEmpty()) {
                return null;
            } else {
                return convertAmqpMessagesWithDeliveryTagsToBrokeredMessages(c);
            }
        }, MessagingFactory.INTERNAL_THREAD_POOL);
    }

    @Override
    public CompletableFuture<IMessage> receiveDeferredMessageAsync(long sequenceNumber) {
        ArrayList<Long> list = new ArrayList<>();
        list.add(sequenceNumber);
        return this.receiveDeferredMessageBatchAsync(list).thenApplyAsync(c -> {
            if (c == null) {
                return null;
            } else if (c.isEmpty()) {
                return null;
            } else {
                return c.toArray(new Message[0])[0];
            }
        }, MessagingFactory.INTERNAL_THREAD_POOL);
    }

    @Override
    public CompletableFuture<Collection<IMessage>> receiveDeferredMessageBatchAsync(Collection<Long> sequenceNumbers) {
        TRACE_LOGGER.debug("Receiving messages by sequence numbers '{}' from entity '{}'", sequenceNumbers, this.entityPath);
        return this.internalReceiver.receiveDeferredMessageBatchAsync(sequenceNumbers.toArray(new Long[0])).thenApplyAsync(c -> {
            if (c == null) {
                return null;
            } else if (c.isEmpty()) {
                return null;
            } else {
                return convertAmqpMessagesWithLockTokensToBrokeredMessages(c);
            }
        }, MessagingFactory.INTERNAL_THREAD_POOL);
    }

    @Override
    protected CompletableFuture<Void> onClose() {
        if (this.isInitialized) {
            if (MessageReceiver.this.isSessionReceiver()) {
                TRACE_LOGGER.info("Closing SessionReceiver to entity '{}', browsable session '{}', sessionId '{}'", this.entityPath, this.isBrowsableSession(), this.internalReceiver.getSessionId());
            } else {
                TRACE_LOGGER.info("Closing MessageReceiver to entity '{}'", this.entityPath);
            }
			if (this.requestResponseLockTokenPruner != null) {
				this.requestResponseLockTokenPruner.cancel(false);
			}
            CompletableFuture<Void> closeReceiverFuture = this.internalReceiver.closeAsync();

            return closeReceiverFuture.thenComposeAsync((v) -> {
                if (MessageReceiver.this.isSessionReceiver()) {
                    TRACE_LOGGER.info("Closed SessionReceiver to entity '{}', browsable session '{}', sessionId '{}'", this.entityPath, this.isBrowsableSession(), this.internalReceiver.getSessionId());
                } else {
                    TRACE_LOGGER.info("Closed MessageReceiver to entity '{}'", this.entityPath);
                }
                if (MessageReceiver.this.ownsMessagingFactory) {
                    if (TRACE_LOGGER.isInfoEnabled()) {
                        TRACE_LOGGER.info("Closing MessagingFactory associated with namespace '{}'", this.namespaceEndpointURI.toString());
                    }
                    return MessageReceiver.this.messagingFactory.closeAsync();
                } else {
                    return CompletableFuture.completedFuture(null);
                }
            }, MessagingFactory.INTERNAL_THREAD_POOL);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public int getPrefetchCount() {
        return this.messagePrefetchCount;
    }

    @Override
    public void setPrefetchCount(int prefetchCount) throws ServiceBusException {
        this.messagePrefetchCount = prefetchCount;
        if (this.isInitialized) {
            if (MessageReceiver.this.isSessionReceiver()) {
                TRACE_LOGGER.info("Setting prefetch count on session receiver to entity '{}', sessionid '{}' to '{}'", this.entityPath, this.internalReceiver.getSessionId(), prefetchCount);
            } else {
                TRACE_LOGGER.info("Setting prefetch count on session receiver to entity '{}' to '{}'", this.entityPath, prefetchCount);
            }

            this.internalReceiver.setPrefetchCount(prefetchCount);
        }
    }

    private void disposeLockToken(UUID lockToken, TransactionContext transaction) {
        if (transaction != TransactionContext.NULL_TXN) {
            transaction.registerHandler((commit) -> {
                if (commit) {
                    MessageReceiver.this.requestResponseLockTokensToLockTimesMap.remove(lockToken);
                }
            });
        } else {
            MessageReceiver.this.requestResponseLockTokensToLockTimesMap.remove(lockToken);
        }
    }

    private static SettleModePair getSettleModePairForRecevieMode(ReceiveMode receiveMode) {
        if (receiveMode == ReceiveMode.RECEIVEANDDELETE) {
            return new SettleModePair(SenderSettleMode.SETTLED, ReceiverSettleMode.FIRST);
        } else {
            return new SettleModePair(SenderSettleMode.UNSETTLED, ReceiverSettleMode.SECOND);
        }
    }

    private Collection<IMessage> convertAmqpMessagesWithDeliveryTagsToBrokeredMessages(Collection<MessageWithDeliveryTag> amqpMessages) {
        ArrayList<IMessage> convertedMessages = new ArrayList<IMessage>();
        for (MessageWithDeliveryTag amqpMessageWithDeliveryTag : amqpMessages) {
            convertedMessages.add(MessageConverter.convertAmqpMessageToBrokeredMessage(amqpMessageWithDeliveryTag));
        }

        return convertedMessages;
    }

    private Collection<IMessage> convertAmqpMessagesWithLockTokensToBrokeredMessages(Collection<MessageWithLockToken> amqpMessages) {
        ArrayList<IMessage> convertedMessages = new ArrayList<IMessage>();
        for (MessageWithLockToken amqpMessageWithLockToken : amqpMessages) {
            Message convertedMessage = MessageConverter.convertAmqpMessageToBrokeredMessage(amqpMessageWithLockToken);
            convertedMessages.add(convertedMessage);
            if (!convertedMessage.getLockToken().equals(ClientConstants.ZEROLOCKTOKEN)) {
                this.requestResponseLockTokensToLockTimesMap.put(convertedMessage.getLockToken(), convertedMessage.getLockedUntilUtc());
            }
        }

        return convertedMessages;
    }

    private void ensurePeekLockReceiveMode() {
        if (this.receiveMode != ReceiveMode.PEEKLOCK) {
            throw new UnsupportedOperationException("Operations Complete/Abandon/DeadLetter/Defer cannot be called on a receiver opened in ReceiveAndDelete mode.");
        }
    }

    private CompletableFuture<Boolean> checkIfValidRequestResponseLockTokenAsync(UUID lockToken) {
        CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
        Instant lockedUntilUtc = this.requestResponseLockTokensToLockTimesMap.get(lockToken);
        if (lockedUntilUtc == null) {
            future.complete(false);
        } else {
            // Should we check for lock expiration here?
            if (lockedUntilUtc.isBefore(Instant.now())) {
                future.completeExceptionally(new ServiceBusException(false, "Lock already expired for the lock token."));
            } else {
                future.complete(true);
            }
        }

        return future;
    }

    @Override
    public CompletableFuture<Instant> renewMessageLockAsync(IMessage message) {
        return this.renewMessageLockAsync(message.getLockToken()).thenApply((newLockedUntilUtc) -> {
            ((Message) message).setLockedUntilUtc(newLockedUntilUtc);
            return newLockedUntilUtc;
        });
    }

    @Override
    public CompletableFuture<Instant> renewMessageLockAsync(UUID lockToken) {
        if (lockToken.equals(ClientConstants.ZEROLOCKTOKEN)) {
            throw new UnsupportedOperationException("Lock of a message received in ReceiveAndDelete mode cannot be renewed.");
        }

        return this.renewMessageLockBatchAsync(new UUID[] {lockToken}).thenApply((c) -> c.toArray(new Instant[0])[0]);
    }

    public CompletableFuture<Collection<Instant>> renewMessageLockBatchAsync(Collection<? extends IMessage> messages) {
        this.ensurePeekLockReceiveMode();

        if (messages == null || messages.size() == 0) {
            throw new UnsupportedOperationException("Message collection is null or empty. Locks cannot be renewed.");
        }

        UUID[] lockTokens = new UUID[messages.size()];
        int messageIndex = 0;
        for (IMessage message : messages) {
            UUID lockToken = message.getLockToken();
            if (lockToken.equals(ClientConstants.ZEROLOCKTOKEN)) {
                throw new UnsupportedOperationException("Lock of a message received in ReceiveAndDelete mode cannot be renewed.");
            }
            lockTokens[messageIndex++] = lockToken;
        }

        return this.renewMessageLockBatchAsync(lockTokens);
    }

    private CompletableFuture<Collection<Instant>> renewMessageLockBatchAsync(UUID[] lockTokens) {
        this.ensurePeekLockReceiveMode();

        if (TRACE_LOGGER.isDebugEnabled()) {
            TRACE_LOGGER.debug("Renewing message locks of lock tokens '{}'", Arrays.toString(lockTokens));
        }
        return this.internalReceiver.renewMessageLocksAsync(lockTokens).thenApplyAsync(
            (newLockedUntilTimes) -> {
                if (TRACE_LOGGER.isDebugEnabled()) {
                    TRACE_LOGGER.debug("Renewed message locks of lock tokens '{}'", Arrays.toString(lockTokens));
                }

                Iterator<Instant> lockTimeIterator = newLockedUntilTimes.iterator();
                for (UUID lockToken : lockTokens) {
                    if (lockTimeIterator.hasNext()) {
                        Instant lockedUntilUtc = lockTimeIterator.next();
                        this.requestResponseLockTokensToLockTimesMap.computeIfPresent(lockToken, (k, v) -> lockedUntilUtc);
                    }
                }
                return newLockedUntilTimes;
            },
            MessagingFactory.INTERNAL_THREAD_POOL
        );
    }

    public Collection<Instant> renewMessageLockBatch(Collection<? extends IMessage> messages) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(this.renewMessageLockBatchAsync(messages));
    }

    @Override
    public Instant renewMessageLock(IMessage message) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(this.renewMessageLockAsync(message));
    }

    @Override
    public Instant renewMessageLock(UUID lockToken)  throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(this.renewMessageLockAsync(lockToken));
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

    private void schedulePruningRequestResponseLockTokens() {
        // Run it every 1 hour
        this.requestResponseLockTokenPruner = Timer.schedule(() -> {
        	if (MessageReceiver.this.getIsClosed())	{
        		MessageReceiver.this.requestResponseLockTokenPruner.cancel(true);
	    		return;
	    	}
            Instant systemTime = Instant.now();
            Entry<UUID, Instant>[] copyOfEntries = (Entry<UUID, Instant>[]) MessageReceiver.this.requestResponseLockTokensToLockTimesMap.entrySet().toArray();
            for (Entry<UUID, Instant> entry : copyOfEntries) {
                if (entry.getValue().isBefore(systemTime)) {
                    // lock expired
                    MessageReceiver.this.requestResponseLockTokensToLockTimesMap.remove(entry.getKey());
                }
            }
        }, Duration.ofSeconds(3600), TimerType.RepeatRun);
    }

    MessagingFactory getMessagingFactory() {
        return this.messagingFactory;
    }
}
