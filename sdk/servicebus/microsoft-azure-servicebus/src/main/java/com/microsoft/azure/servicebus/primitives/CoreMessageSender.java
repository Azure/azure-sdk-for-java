// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.primitives;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.microsoft.azure.servicebus.TransactionContext;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Outcome;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transaction.Declared;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.apache.qpid.proton.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.amqp.AmqpConstants;
import com.microsoft.azure.servicebus.amqp.DispatchHandler;
import com.microsoft.azure.servicebus.amqp.IAmqpSender;
import com.microsoft.azure.servicebus.amqp.SendLinkHandler;
import com.microsoft.azure.servicebus.amqp.SessionHandler;

import static java.nio.charset.StandardCharsets.UTF_8;

/*
 * Abstracts all amqp related details
 * translates event-driven reactor model into async send Api
 */
public class CoreMessageSender extends ClientEntity implements IAmqpSender, IErrorContextProvider {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(CoreMessageSender.class);
    private static final String SEND_TIMED_OUT = "Send operation timed out";
    private static final Duration LINK_REOPEN_TIMEOUT = Duration.ofMinutes(5); // service closes link long before this timeout expires

    private final Object requestResonseLinkCreationLock = new Object();
    private final MessagingFactory underlyingFactory;
    private final String sendPath;
    private final String sasTokenAudienceURI;
    private final Duration operationTimeout;
    private final RetryPolicy retryPolicy;
    private final CompletableFuture<Void> linkClose;
    private final Object pendingSendLock;
    private final ConcurrentHashMap<String, SendWorkItem<DeliveryState>> pendingSendsData;
    private final PriorityQueue<WeightedDeliveryTag> pendingSends;
    private final DispatchHandler sendWork;
    private final MessagingEntityType entityType;
    private boolean isSendLoopRunning;

    private Sender sendLink;
    private RequestResponseLink requestResponseLink;
    private CompletableFuture<CoreMessageSender> linkFirstOpen;
    private int linkCredit;
    private Exception lastKnownLinkError;
    private Instant lastKnownErrorReportedAt;
    private ScheduledFuture<?> sasTokenRenewTimerFuture;
    private CompletableFuture<Void> requestResponseLinkCreationFuture;
    private CompletableFuture<Void> sendLinkReopenFuture;
    private SenderLinkSettings linkSettings;
    private String transferDestinationPath;
    private String transferSasTokenAudienceURI;
    private boolean isSendVia;
    private int maxMessageSize;
    private boolean shouldRetryLinkOpenIfConnectionIsClosedAfterCBSTokenSent = true;

    @Deprecated
    public static CompletableFuture<CoreMessageSender> create(
            final MessagingFactory factory,
            final String clientId,
            final String senderPath,
            final String transferDestinationPath) {
        return CoreMessageSender.create(factory, clientId, senderPath, transferDestinationPath, null);
    }

    public static CompletableFuture<CoreMessageSender> create(
            final MessagingFactory factory,
            final String clientId,
            final String senderPath,
            final String transferDestinationPath,
            final MessagingEntityType entityType) {
        return CoreMessageSender.create(factory, clientId, entityType, CoreMessageSender.getDefaultLinkProperties(senderPath, transferDestinationPath, factory, entityType));
    }

    static CompletableFuture<CoreMessageSender> create(
            final MessagingFactory factory,
            final String clientId,
            final MessagingEntityType entityType,
            final SenderLinkSettings linkSettings) {
        TRACE_LOGGER.info("Creating core message sender to '{}'", linkSettings.linkPath);

        final Connection connection = factory.getActiveConnectionCreateIfNecessary();
        final String sendLinkNamePrefix = "Sender".concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(StringUtil.getShortRandomString());
        linkSettings.linkName = !StringUtil.isNullOrEmpty(connection.getRemoteContainer())
            ? sendLinkNamePrefix.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(connection.getRemoteContainer())
            : sendLinkNamePrefix;

        final CoreMessageSender msgSender = new CoreMessageSender(factory, clientId, entityType, linkSettings);
        TimeoutTracker openLinkTracker = TimeoutTracker.create(factory.getOperationTimeout());
        msgSender.initializeLinkOpen(openLinkTracker);

        CompletableFuture<Void> authenticationFuture = null;
        if (linkSettings.requiresAuthentication) {
            authenticationFuture = msgSender.sendTokenAndSetRenewTimer(false);
        } else {
            authenticationFuture = CompletableFuture.completedFuture(null);
        }

        authenticationFuture.handleAsync((v, sasTokenEx) -> {
            if (sasTokenEx != null) {
                Throwable cause = ExceptionUtil.extractAsyncCompletionCause(sasTokenEx);
                TRACE_LOGGER.info("Sending SAS Token to '{}' failed.", msgSender.sendPath, cause);
                msgSender.linkFirstOpen.completeExceptionally(cause);
            } else {
                try {
                    msgSender.underlyingFactory.scheduleOnReactorThread(new DispatchHandler() {
                        @Override
                        public void onEvent() {
                            msgSender.createSendLink(msgSender.linkSettings);
                        }
                    });
                } catch (IOException ioException) {
                    msgSender.cancelSASTokenRenewTimer();
                    msgSender.linkFirstOpen.completeExceptionally(new ServiceBusException(false, "Failed to create Sender, see cause for more details.", ioException));
                }
            }

            return null;
        }, MessagingFactory.INTERNAL_THREAD_POOL);

        return msgSender.linkFirstOpen;
    }

    private CompletableFuture<Void> createRequestResponseLink() {
        synchronized (this.requestResonseLinkCreationLock) {
            if (this.requestResponseLinkCreationFuture == null) {
                this.requestResponseLinkCreationFuture = new CompletableFuture<Void>();
                this.underlyingFactory.obtainRequestResponseLinkAsync(this.sendPath, this.transferDestinationPath, this.entityType).handleAsync((rrlink, ex) -> {
                    if (ex == null) {
                        this.requestResponseLink = rrlink;
                        this.requestResponseLinkCreationFuture.complete(null);
                    } else {
                        Throwable cause = ExceptionUtil.extractAsyncCompletionCause(ex);
                        this.requestResponseLinkCreationFuture.completeExceptionally(cause);
                        // Set it to null so next call will retry rr link creation
                        synchronized (this.requestResonseLinkCreationLock) {
                            this.requestResponseLinkCreationFuture = null;
                        }
                    }
                    return null;
                }, MessagingFactory.INTERNAL_THREAD_POOL);
            }

            return this.requestResponseLinkCreationFuture;
        }
    }

    private void closeRequestResponseLink() {
        synchronized (this.requestResonseLinkCreationLock) {
            if (this.requestResponseLinkCreationFuture != null) {
                this.requestResponseLinkCreationFuture.thenRun(() -> {
                    this.underlyingFactory.releaseRequestResponseLink(this.sendPath, this.transferDestinationPath);
                    this.requestResponseLink = null;
                });
                this.requestResponseLinkCreationFuture = null;
            }
        }
    }

    private CoreMessageSender(final MessagingFactory factory, final String sendLinkName, final MessagingEntityType entityType, final SenderLinkSettings linkSettings) {
        super(sendLinkName);

        this.sendPath = linkSettings.linkPath;
        this.entityType = entityType;
        if (linkSettings.linkProperties != null) {
            String transferPath = (String) linkSettings.linkProperties.getOrDefault(ClientConstants.LINK_TRANSFER_DESTINATION_PROPERTY, null);
            if (transferPath != null && !transferPath.isEmpty()) {
                this.transferDestinationPath = transferPath;
                this.isSendVia = true;
                this.transferSasTokenAudienceURI = String.format(ClientConstants.SAS_TOKEN_AUDIENCE_FORMAT, factory.getHostName(), transferDestinationPath);
            } else {
                // Ensure it is null.
                this.transferDestinationPath = null;
            }
        }

        this.sasTokenAudienceURI = String.format(ClientConstants.SAS_TOKEN_AUDIENCE_FORMAT, factory.getHostName(), linkSettings.linkPath);
        this.underlyingFactory = factory;
        this.operationTimeout = factory.getOperationTimeout();
        this.linkSettings = linkSettings;

        this.lastKnownLinkError = null;
        this.lastKnownErrorReportedAt = Instant.EPOCH;

        this.retryPolicy = factory.getRetryPolicy();

        this.pendingSendLock = new Object();
        this.pendingSendsData = new ConcurrentHashMap<String, SendWorkItem<DeliveryState>>();
        this.pendingSends = new PriorityQueue<WeightedDeliveryTag>(1000, new DeliveryTagComparator());
        this.linkCredit = 0;

        this.linkClose = new CompletableFuture<Void>();
        this.sendLinkReopenFuture = null;
        this.isSendLoopRunning = false;
        this.sendWork = new DispatchHandler() {
            @Override
            public void onEvent() {
                CoreMessageSender.this.processSendWork();
            }
        };
    }

    public String getSendPath() {
        return this.sendPath;
    }

    private static String generateRandomDeliveryTag() {
        return UUID.randomUUID().toString().replace("-", StringUtil.EMPTY);
    }

    CompletableFuture<DeliveryState> sendCoreAsync(
            final byte[] bytes,
            final int arrayOffset,
            final int messageFormat,
            final TransactionContext transaction) {
        this.throwIfClosed(this.lastKnownLinkError);
        TRACE_LOGGER.debug("Sending message to '{}'", this.sendPath);
        String deliveryTag = CoreMessageSender.generateRandomDeliveryTag();
        CompletableFuture<DeliveryState> onSendFuture = new CompletableFuture<DeliveryState>();
        SendWorkItem<DeliveryState> sendWorkItem = new SendWorkItem<DeliveryState>(bytes, arrayOffset, messageFormat, deliveryTag, transaction, onSendFuture, this.operationTimeout);
        this.enlistSendRequest(deliveryTag, sendWorkItem, false);
        this.scheduleSendTimeout(sendWorkItem);
        return onSendFuture;
    }

    private void scheduleSendTimeout(SendWorkItem<DeliveryState> sendWorkItem) {
        // Timer to timeout the request
        ScheduledFuture<?> timeoutTask = Timer.schedule(() -> {
            if (!sendWorkItem.getWork().isDone()) {
                TRACE_LOGGER.info("Delivery '{}' to '{}' did not receive ack from service. Throwing timeout.", sendWorkItem.getDeliveryTag(), CoreMessageSender.this.sendPath);
                CoreMessageSender.this.pendingSendsData.remove(sendWorkItem.getDeliveryTag());
                CoreMessageSender.this.throwSenderTimeout(sendWorkItem.getWork(), sendWorkItem.getLastKnownException());
                // Weighted delivery tag not removed from the pending sends queue, but send loop will ignore it anyway if it is present
            }
        },
            sendWorkItem.getTimeoutTracker().remaining(),
            TimerType.OneTimeRun);
        sendWorkItem.setTimeoutTask(timeoutTask);
    }

    private void enlistSendRequest(String deliveryTag, SendWorkItem<DeliveryState> sendWorkItem, boolean isRetrySend) {
        synchronized (this.pendingSendLock) {
            this.pendingSendsData.put(deliveryTag, sendWorkItem);
            this.pendingSends.offer(new WeightedDeliveryTag(deliveryTag, isRetrySend ? 1 : 0));

            if (!this.isSendLoopRunning) {
                try {
                    this.underlyingFactory.scheduleOnReactorThread(this.sendWork);
                } catch (IOException ioException) {
                    AsyncUtil.completeFutureExceptionally(sendWorkItem.getWork(), new ServiceBusException(false, "Send failed while dispatching to Reactor, see cause for more details.", ioException));
                }
            }
        }
    }

    private void reSendAsync(String deliveryTag, SendWorkItem<DeliveryState> retryingSendWorkItem, boolean reuseDeliveryTag) {
        if (!retryingSendWorkItem.getWork().isDone() && retryingSendWorkItem.cancelTimeoutTask(false)) {
            Duration remainingTime = retryingSendWorkItem.getTimeoutTracker().remaining();
            if (!remainingTime.isNegative() && !remainingTime.isZero()) {
                if (!reuseDeliveryTag) {
                    deliveryTag = CoreMessageSender.generateRandomDeliveryTag();
                    retryingSendWorkItem.setDeliveryTag(deliveryTag);
                }

                this.enlistSendRequest(deliveryTag, retryingSendWorkItem, true);
                this.scheduleSendTimeout(retryingSendWorkItem);
            }
        }
    }

    public CompletableFuture<Void> sendAsync(final Iterable<Message> messages, TransactionContext transaction) {
        if (messages == null || IteratorUtil.sizeEquals(messages, 0)) {
            throw new IllegalArgumentException("Sending Empty batch of messages is not allowed.");
        }

        TRACE_LOGGER.debug("Sending a batch of messages to '{}'", this.sendPath);

        Message firstMessage = messages.iterator().next();
        if (IteratorUtil.sizeEquals(messages, 1)) {
            return this.sendAsync(firstMessage, transaction);
        }

        // proton-j doesn't support multiple dataSections to be part of AmqpMessage
        // here's the alternate approach provided by them: https://github.com/apache/qpid-proton/pull/54
        Message batchMessage = Proton.message();
        
        // Set partition identifier properties of the first message on batch message
        batchMessage.setMessageAnnotations(firstMessage.getMessageAnnotations());
        if (StringUtil.isNullOrWhiteSpace((String)firstMessage.getMessageId())) {
        	batchMessage.setMessageId(firstMessage.getMessageId());
        }
        
        if (StringUtil.isNullOrWhiteSpace(firstMessage.getGroupId())) {
        	batchMessage.setGroupId(firstMessage.getGroupId());
        }        

        byte[] bytes = null;
        int byteArrayOffset = 0;
        try {
            Pair<byte[], Integer> encodedPair = Util.encodeMessageToMaxSizeArray(batchMessage, this.maxMessageSize);
            bytes = encodedPair.getFirstItem();
            byteArrayOffset = encodedPair.getSecondItem();

            for (Message amqpMessage: messages) {
                Message messageWrappedByData = Proton.message();
                encodedPair = Util.encodeMessageToOptimalSizeArray(amqpMessage, this.maxMessageSize);
                messageWrappedByData.setBody(new Data(new Binary(encodedPair.getFirstItem(), 0, encodedPair.getSecondItem())));

                int encodedSize = Util.encodeMessageToCustomArray(messageWrappedByData, bytes, byteArrayOffset, this.maxMessageSize - byteArrayOffset - 1);
                byteArrayOffset = byteArrayOffset + encodedSize;
            }
        } catch (PayloadSizeExceededException ex) {
            TRACE_LOGGER.info("Payload size of batch of messages exceeded limit", ex);
            final CompletableFuture<Void> sendTask = new CompletableFuture<Void>();
            sendTask.completeExceptionally(ex);
            return sendTask;
        }

        return this.sendCoreAsync(bytes, byteArrayOffset, AmqpConstants.AMQP_BATCH_MESSAGE_FORMAT, transaction).thenAccept((x) -> { /*Do nothing*/ });
    }

    public CompletableFuture<Void> sendAsync(Message msg, TransactionContext transaction) {
        return this.sendAndReturnDeliveryStateAsync(msg, transaction).thenAccept((x) -> { /*Do nothing*/ });
    }

    // To be used only by internal components like TransactionController
    CompletableFuture<DeliveryState> sendAndReturnDeliveryStateAsync(Message msg, TransactionContext transaction) {
        try {
            Pair<byte[], Integer> encodedPair = Util.encodeMessageToOptimalSizeArray(msg, this.maxMessageSize);
            return this.sendCoreAsync(encodedPair.getFirstItem(), encodedPair.getSecondItem(), DeliveryImpl.DEFAULT_MESSAGE_FORMAT, transaction);
        } catch (PayloadSizeExceededException exception) {
            TRACE_LOGGER.info("Payload size of message exceeded limit", exception);
            final CompletableFuture<DeliveryState> sendTask = new CompletableFuture<DeliveryState>();
            sendTask.completeExceptionally(exception);
            return sendTask;
        }
    }

    @Override
    public void onOpenComplete(Exception completionException) {
    	this.shouldRetryLinkOpenIfConnectionIsClosedAfterCBSTokenSent = true;
        if (completionException == null) {
            this.maxMessageSize = Util.getMaxMessageSizeFromLink(this.sendLink);
            this.lastKnownLinkError = null;
            this.retryPolicy.resetRetryCount(this.getClientId());

            if (this.sendLinkReopenFuture != null && !this.sendLinkReopenFuture.isDone()) {
                AsyncUtil.completeFuture(this.sendLinkReopenFuture, null);
            }

            if (!this.linkFirstOpen.isDone()) {
                TRACE_LOGGER.info("Opened send link to '{}'", this.sendPath);
                AsyncUtil.completeFuture(this.linkFirstOpen, this);
            } else {
                synchronized (this.pendingSendLock) {
                    if (!this.pendingSendsData.isEmpty()) {
                        LinkedList<String> unacknowledgedSends = new LinkedList<String>();
                        unacknowledgedSends.addAll(this.pendingSendsData.keySet());

                        if (unacknowledgedSends.size() > 0) {
                            Iterator<String> reverseReader = unacknowledgedSends.iterator();
                            while (reverseReader.hasNext()) {
                                String unacknowledgedSend = reverseReader.next();
                                if (this.pendingSendsData.get(unacknowledgedSend).isWaitingForAck()) {
                                    this.pendingSends.offer(new WeightedDeliveryTag(unacknowledgedSend, 1));
                                }
                            }
                        }

                        unacknowledgedSends.clear();
                    }
                }
            }
        } else {
            this.cancelSASTokenRenewTimer();
            if (!this.linkFirstOpen.isDone()) {
                TRACE_LOGGER.info("Opening send link '{}' to '{}' failed", this.sendLink.getName(), this.sendPath, completionException);
                this.setClosed();
                ExceptionUtil.completeExceptionally(this.linkFirstOpen, completionException, this, true);
            }

            if (this.sendLinkReopenFuture != null && !this.sendLinkReopenFuture.isDone()) {
                TRACE_LOGGER.info("Opening send link '{}' to '{}' failed", this.sendLink.getName(), this.sendPath, completionException);
                AsyncUtil.completeFutureExceptionally(this.sendLinkReopenFuture, completionException);
            }
        }
    }

    @Override
    public void onClose(ErrorCondition condition) {
        Exception completionException = condition != null ? ExceptionUtil.toException(condition)
                : new ServiceBusException(ClientConstants.DEFAULT_IS_TRANSIENT,
                "The entity has been closed due to transient failures (underlying link closed), please retry the operation.");
        this.onError(completionException);
    }

    @Override
    public void onError(Exception completionException) {
        this.linkCredit = 0;
        if (this.getIsClosingOrClosed()) {
            Exception failureException = completionException == null
                    ? new OperationCancelledException("Send cancelled as the Sender instance is Closed before the sendOperation completed.")
                    : completionException;
            this.clearAllPendingSendsWithException(failureException);

            TRACE_LOGGER.info("Send link to '{}' closed", this.sendPath);
            AsyncUtil.completeFuture(this.linkClose, null);
            return;
        } else {
            this.underlyingFactory.deregisterForConnectionError(this.sendLink);
            this.lastKnownLinkError = completionException;
            this.lastKnownErrorReportedAt = Instant.now();

            this.onOpenComplete(completionException);

            if (completionException != null
                && (!(completionException instanceof ServiceBusException) || !((ServiceBusException) completionException).getIsTransient())) {
                TRACE_LOGGER.info("Send link '{}' to '{}' closed. Failing all pending send requests.", this.sendLink.getName(), this.sendPath);
                this.clearAllPendingSendsWithException(completionException);
            } else {
                final Map.Entry<String, SendWorkItem<DeliveryState>> pendingSendEntry = IteratorUtil.getFirst(this.pendingSendsData.entrySet());
                if (pendingSendEntry != null && pendingSendEntry.getValue() != null) {
                    final TimeoutTracker tracker = pendingSendEntry.getValue().getTimeoutTracker();
                    if (tracker != null) {
                        final Duration nextRetryInterval = this.retryPolicy.getNextRetryInterval(this.getClientId(), completionException, tracker.remaining());
                        if (nextRetryInterval != null) {
                            TRACE_LOGGER.info("Send link '{}' to '{}' closed. Will retry link creation after '{}'.", this.sendLink.getName(), this.sendPath, nextRetryInterval);
                            Timer.schedule(() -> CoreMessageSender.this.ensureLinkIsOpen(), nextRetryInterval, TimerType.OneTimeRun);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onSendComplete(final Delivery delivery) {
        DeliveryState outcome = delivery.getRemoteState();
        final String deliveryTag = new String(delivery.getTag(), UTF_8);

        TRACE_LOGGER.debug("Received ack for delivery. path:{}, linkName:{}, deliveryTag:{}, outcome:{}", CoreMessageSender.this.sendPath, this.sendLink.getName(), deliveryTag, outcome);
        final SendWorkItem<DeliveryState> pendingSendWorkItem = this.pendingSendsData.remove(deliveryTag);

        if (pendingSendWorkItem != null) {
            if (outcome instanceof TransactionalState) {
                TRACE_LOGGER.trace("State of delivery is Transactional, retrieving outcome: {}", outcome);
                Outcome transactionalOutcome = ((TransactionalState) outcome).getOutcome();
                if (transactionalOutcome instanceof DeliveryState) {
                    outcome = (DeliveryState) transactionalOutcome;
                } else {
                    this.cleanupFailedSend(pendingSendWorkItem, new ServiceBusException(false, "Unknown delivery state: " + outcome.toString()));
                    return;
                }
            }

            if (outcome instanceof Accepted) {
                this.lastKnownLinkError = null;
                this.retryPolicy.resetRetryCount(this.getClientId());

                pendingSendWorkItem.cancelTimeoutTask(false);
                AsyncUtil.completeFuture(pendingSendWorkItem.getWork(), outcome);
            } else if (outcome instanceof Declared) {
                AsyncUtil.completeFuture(pendingSendWorkItem.getWork(), outcome);
            } else if (outcome instanceof Rejected) {
                Rejected rejected = (Rejected) outcome;
                ErrorCondition error = rejected.getError();
                Exception exception = ExceptionUtil.toException(error);

                if (ExceptionUtil.isGeneralError(error.getCondition())) {
                    this.lastKnownLinkError = exception;
                    this.lastKnownErrorReportedAt = Instant.now();
                }

                Duration retryInterval = this.retryPolicy.getNextRetryInterval(
                        this.getClientId(), exception, pendingSendWorkItem.getTimeoutTracker().remaining());
                if (retryInterval == null) {
                    this.cleanupFailedSend(pendingSendWorkItem, exception);
                } else {
                    TRACE_LOGGER.info("Send failed for delivery '{}'. Will retry after '{}'", deliveryTag, retryInterval);
                    pendingSendWorkItem.setLastKnownException(exception);
                    Timer.schedule(() -> CoreMessageSender.this.reSendAsync(deliveryTag, pendingSendWorkItem, false), retryInterval, TimerType.OneTimeRun);
                }
            } else if (outcome instanceof Released) {
                this.cleanupFailedSend(pendingSendWorkItem, new OperationCancelledException(outcome.toString()));
            } else {
                this.cleanupFailedSend(pendingSendWorkItem, new ServiceBusException(false, outcome.toString()));
            }
        } else {
            TRACE_LOGGER.info("Delivery mismatch. path:{}, linkName:{}, delivery:{}", this.sendPath, this.sendLink.getName(), deliveryTag);
        }
    }

    private void clearAllPendingSendsWithException(Throwable failureException) {
        synchronized (this.pendingSendLock) {
            for (Map.Entry<String, SendWorkItem<DeliveryState>> pendingSend: this.pendingSendsData.entrySet()) {
                this.cleanupFailedSend(pendingSend.getValue(), failureException);
            }

            this.pendingSendsData.clear();
            this.pendingSends.clear();
        }
    }

    private void cleanupFailedSend(final SendWorkItem<DeliveryState> failedSend, final Throwable exception) {
        failedSend.cancelTimeoutTask(false);
        ExceptionUtil.completeExceptionally(failedSend.getWork(), exception, this, true);
    }

    private static SenderLinkSettings getDefaultLinkProperties(String sendPath, String transferDestinationPath, MessagingFactory underlyingFactory, MessagingEntityType entityType) {
        SenderLinkSettings linkSettings = new SenderLinkSettings();
        linkSettings.linkPath = sendPath;

        final Target target = new Target();
        target.setAddress(sendPath);
        linkSettings.target = target;
        linkSettings.source = new Source();
        linkSettings.settleMode = SenderSettleMode.UNSETTLED;
        linkSettings.requiresAuthentication = true;

        Map<Symbol, Object> linkProperties = new HashMap<>();
        // ServiceBus expects timeout to be of type unsignedint
        linkProperties.put(ClientConstants.LINK_TIMEOUT_PROPERTY, UnsignedInteger.valueOf(Util.adjustServerTimeout(underlyingFactory.getOperationTimeout()).toMillis()));
        if (entityType != null) {
            linkProperties.put(ClientConstants.ENTITY_TYPE_PROPERTY, entityType.getIntValue());
        }
        if (transferDestinationPath != null && !transferDestinationPath.isEmpty()) {
            linkProperties.put(ClientConstants.LINK_TRANSFER_DESTINATION_PROPERTY, transferDestinationPath);
        }

        linkSettings.linkProperties = linkProperties;

        return linkSettings;
    }

    private void createSendLink(SenderLinkSettings linkSettings) {
        TRACE_LOGGER.info("Creating send link to '{}'", this.sendPath);
        Connection connection = this.underlyingFactory.getActiveConnectionOrNothing();
		if (connection == null) {
			// Connection closed after sending CBS token. Happens only in the rare case of azure service bus closing idle connection, just right after sending
			// CBS token but before opening a link.
			TRACE_LOGGER.warn("Idle connection closed by service just after sending CBS token. Very rare case. Will retry.");
			ServiceBusException exception = new ServiceBusException(true, "Idle connection closed by service just after sending CBS token. Please retry.");
			if (this.linkFirstOpen != null && !this.linkFirstOpen.isDone()) {
				// Should never happen
				AsyncUtil.completeFutureExceptionally(this.linkFirstOpen, exception);
			}

			if (this.sendLinkReopenFuture != null && !this.sendLinkReopenFuture.isDone()) {
				// Complete the future and re-attempt link creation
				AsyncUtil.completeFutureExceptionally(this.sendLinkReopenFuture, exception);
				if(this.shouldRetryLinkOpenIfConnectionIsClosedAfterCBSTokenSent) {
					this.shouldRetryLinkOpenIfConnectionIsClosedAfterCBSTokenSent = false;
					Timer.schedule(() -> {this.ensureLinkIsOpen();}, Duration.ZERO, TimerType.OneTimeRun);
				}
			}

			return;
		}
		
        final Session session = connection.session();
        session.setOutgoingWindow(Integer.MAX_VALUE);
        session.open();
        BaseHandler.setHandler(session, new SessionHandler(sendPath));

        final Sender sender = session.sender(linkSettings.linkName);
        sender.setTarget(linkSettings.target);
        sender.setSource(linkSettings.source);
        sender.setProperties(linkSettings.linkProperties);

        TRACE_LOGGER.debug("Send link settle mode '{}'", linkSettings.settleMode);
        sender.setSenderSettleMode(linkSettings.settleMode);

        SendLinkHandler handler = new SendLinkHandler(CoreMessageSender.this);
        BaseHandler.setHandler(sender, handler);
        sender.open();
        this.sendLink = sender;
        this.underlyingFactory.registerForConnectionError(this.sendLink);
    }

    CompletableFuture<Void> sendTokenAndSetRenewTimer(boolean retryOnFailure) {
        if (this.getIsClosingOrClosed()) {
            return CompletableFuture.completedFuture(null);
        } else {
            CompletableFuture<ScheduledFuture<?>> sendTokenFuture = this.underlyingFactory.sendSecurityTokenAndSetRenewTimer(this.sasTokenAudienceURI, retryOnFailure, () -> this.sendTokenAndSetRenewTimer(true));
            CompletableFuture<Void> sasTokenFuture = sendTokenFuture.thenAccept((f) -> this.sasTokenRenewTimerFuture = f);

            if (this.transferDestinationPath != null && !this.transferDestinationPath.isEmpty()) {
                CompletableFuture<Void> transferSendTokenFuture = this.underlyingFactory.sendSecurityToken(this.transferSasTokenAudienceURI);
                return CompletableFuture.allOf(sasTokenFuture, transferSendTokenFuture);
            }

            return sasTokenFuture;
        }
    }

    private void cancelSASTokenRenewTimer() {
        if (this.sasTokenRenewTimerFuture != null && !this.sasTokenRenewTimerFuture.isDone()) {
            this.sasTokenRenewTimerFuture.cancel(true);
            TRACE_LOGGER.debug("Cancelled SAS Token renew timer");
        }
    }

    // TODO: consolidate common-code written for timeouts in Sender/Receiver
    private void initializeLinkOpen(TimeoutTracker timeout) {
        this.linkFirstOpen = new CompletableFuture<CoreMessageSender>();

        // timer to signal a timeout if exceeds the operationTimeout on MessagingFactory
        Timer.schedule(
            () -> {
                if (!CoreMessageSender.this.linkFirstOpen.isDone()) {
                    Exception operationTimedout = new TimeoutException(
                            String.format(Locale.US, "Open operation on SendLink(%s) on Entity(%s) timed out at %s.", CoreMessageSender.this.sendLink.getName(), CoreMessageSender.this.getSendPath(), ZonedDateTime.now().toString()),
                            CoreMessageSender.this.lastKnownErrorReportedAt.isAfter(Instant.now().minusSeconds(ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS)) ? CoreMessageSender.this.lastKnownLinkError : null);
                    TRACE_LOGGER.info(operationTimedout.getMessage());
                    ExceptionUtil.completeExceptionally(CoreMessageSender.this.linkFirstOpen, operationTimedout, CoreMessageSender.this, true);
                    
                	CoreMessageSender.this.setClosing();
                    CoreMessageSender.this.closeInternals(false);
                    CoreMessageSender.this.setClosed();
                }
            },
            timeout.remaining(),
            TimerType.OneTimeRun);
    }

    @Override
    public ErrorContext getContext() {
        final boolean isLinkOpened = this.linkFirstOpen != null && this.linkFirstOpen.isDone();
        final String referenceId = this.sendLink != null && this.sendLink.getRemoteProperties() != null && this.sendLink.getRemoteProperties().containsKey(ClientConstants.TRACKING_ID_PROPERTY)
                ? this.sendLink.getRemoteProperties().get(ClientConstants.TRACKING_ID_PROPERTY).toString()
                : ((this.sendLink != null) ? this.sendLink.getName() : null);

        SenderErrorContext errorContext = new SenderErrorContext(
                this.underlyingFactory != null ? this.underlyingFactory.getHostName() : null,
                this.sendPath,
                referenceId,
                isLinkOpened && this.sendLink != null ? this.sendLink.getCredit() : null);
        return errorContext;
    }

    @Override
    public void onFlow(final int creditIssued) {
        this.lastKnownLinkError = null;

        if (creditIssued <= 0) {
            return;
        }

        TRACE_LOGGER.debug("Received flow frame. path:{}, linkName:{}, remoteLinkCredit:{}, pendingSendsWaitingForCredit:{}, pendingSendsWaitingDelivery:{}",
                this.sendPath, this.sendLink.getName(), creditIssued, this.pendingSends.size(), this.pendingSendsData.size() - this.pendingSends.size());

        this.linkCredit = this.linkCredit + creditIssued;
        this.sendWork.onEvent();
    }

    private synchronized CompletableFuture<Void> ensureLinkIsOpen() {
        // Send SAS token before opening a link as connection might have been closed and reopened
        if (!(this.sendLink.getLocalState() == EndpointState.ACTIVE && this.sendLink.getRemoteState() == EndpointState.ACTIVE)) {
            if (this.sendLinkReopenFuture == null || this.sendLinkReopenFuture.isDone()) {
                TRACE_LOGGER.info("Recreating send link to '{}'", this.sendPath);
                this.retryPolicy.incrementRetryCount(CoreMessageSender.this.getClientId());
                this.sendLinkReopenFuture = new CompletableFuture<>();
                // Variable just to closed over by the scheduled runnable. The runnable should cancel only the closed over future, not the parent's instance variable which can change
                final CompletableFuture<Void> linkReopenFutureThatCanBeCancelled = this.sendLinkReopenFuture;
                Timer.schedule(
                    () -> {
                        if (!linkReopenFutureThatCanBeCancelled.isDone()) {
                            CoreMessageSender.this.cancelSASTokenRenewTimer();
                            Exception operationTimedout = new TimeoutException(
                                    String.format(Locale.US, "%s operation on SendLink(%s) to path(%s) timed out at %s.", "Open", CoreMessageSender.this.sendLink.getName(), CoreMessageSender.this.sendPath, ZonedDateTime.now()));

                            TRACE_LOGGER.info(operationTimedout.getMessage());
                            linkReopenFutureThatCanBeCancelled.completeExceptionally(operationTimedout);
                        }
                    },
                    CoreMessageSender.LINK_REOPEN_TIMEOUT,
                    TimerType.OneTimeRun);
                this.cancelSASTokenRenewTimer();

                CompletableFuture<Void> authenticationFuture = null;
                if (linkSettings.requiresAuthentication) {
                    authenticationFuture = this.sendTokenAndSetRenewTimer(false);
                } else {
                    authenticationFuture = CompletableFuture.completedFuture(null);
                }

                authenticationFuture.handleAsync((v, sendTokenEx) -> {
                    if (sendTokenEx != null) {
                        Throwable cause = ExceptionUtil.extractAsyncCompletionCause(sendTokenEx);
                        TRACE_LOGGER.info("Sending SAS Token to '{}' failed.", this.sendPath, cause);
                        this.sendLinkReopenFuture.completeExceptionally(sendTokenEx);
                        this.clearAllPendingSendsWithException(sendTokenEx);
                    } else {
                        try {
                            this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler() {
                                @Override
                                public void onEvent() {
                                    CoreMessageSender.this.createSendLink(CoreMessageSender.this.linkSettings);
                                }
                            });
                        } catch (IOException ioEx) {
                            this.sendLinkReopenFuture.completeExceptionally(ioEx);
                        }
                    }
                    return null;
                }, MessagingFactory.INTERNAL_THREAD_POOL);
            }

            return this.sendLinkReopenFuture;
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    // actual send on the SenderLink should happen only in this method & should run on Reactor Thread
    private void processSendWork() {
        synchronized (this.pendingSendLock) {
            if (!this.isSendLoopRunning) {
                this.isSendLoopRunning = true;
            } else {
                return;
            }
        }

        TRACE_LOGGER.debug("Processing pending sends to '{}'. Available link credit '{}'", this.sendPath, this.linkCredit);
        try {
            if (!this.ensureLinkIsOpen().isDone()) {
                // Link recreation is pending
                return;
            }

            final Sender sendLinkCurrent = this.sendLink;
            while (sendLinkCurrent != null
                    && sendLinkCurrent.getLocalState() == EndpointState.ACTIVE && sendLinkCurrent.getRemoteState() == EndpointState.ACTIVE
                    && this.linkCredit > 0) {
                final WeightedDeliveryTag deliveryTag;
                final SendWorkItem<DeliveryState> sendData;
                synchronized (this.pendingSendLock) {
                    deliveryTag = this.pendingSends.poll();
                    if (deliveryTag == null) {
                        TRACE_LOGGER.debug("There are no pending sends to '{}'.", this.sendPath);
                        // Must be done inside this synchronized block
                        this.isSendLoopRunning = false;
                        break;
                    } else {
                        sendData = this.pendingSendsData.get(deliveryTag.getDeliveryTag());
                        if (sendData == null) {
                            TRACE_LOGGER.debug("SendData not found for this delivery. path:{}, linkName:{}, deliveryTag:{}", this.sendPath, this.sendLink.getName(), deliveryTag);
                            continue;
                        }
                    }
                }

                if (sendData.getWork() != null && sendData.getWork().isDone()) {
                    // CoreSend could enqueue Sends into PendingSends Queue and can fail the SendCompletableFuture
                    // (when It fails to schedule the ProcessSendWork on reactor Thread)
                    this.pendingSendsData.remove(deliveryTag.getDeliveryTag());
                    continue;
                }

                Delivery delivery = null;
                boolean linkAdvance = false;
                int sentMsgSize = 0;
                Exception sendException = null;

                try {
                    delivery = sendLinkCurrent.delivery(deliveryTag.getDeliveryTag().getBytes(UTF_8));
                    delivery.setMessageFormat(sendData.getMessageFormat());

                    TransactionContext transaction = sendData.getTransaction();
                    if (transaction != TransactionContext.NULL_TXN) {
                        TransactionalState transactionalState = new TransactionalState();
                        transactionalState.setTxnId(new Binary(transaction.getTransactionId().array()));
                        delivery.disposition(transactionalState);
                    }

                    TRACE_LOGGER.debug("Sending message delivery '{}' to '{}'", deliveryTag.getDeliveryTag(), this.sendPath);
                    sentMsgSize = sendLinkCurrent.send(sendData.getMessage(), 0, sendData.getEncodedMessageSize());
                    assert sentMsgSize == sendData.getEncodedMessageSize() : "Contract of the ProtonJ library for Sender.Send API changed";

                    linkAdvance = sendLinkCurrent.advance();
                } catch (Exception exception) {
                    sendException = exception;
                }

                if (linkAdvance) {
                    this.linkCredit--;
                    sendData.setWaitingForAck();
                } else {
                    TRACE_LOGGER.info("Sendlink advance failed. path:{}, linkName:{}, deliveryTag:{}, sentMessageSize:{}, payloadActualSiz:{}",
                            this.sendPath, this.sendLink.getName(), deliveryTag, sentMsgSize, sendData.getEncodedMessageSize());

                    if (delivery != null) {
                        delivery.free();
                    }

                    Exception completionException = sendException != null ? new OperationCancelledException("Send operation failed. Please see cause for more details", sendException)
                            : new OperationCancelledException(String.format(Locale.US, "Send operation failed while advancing delivery(tag: %s) on SendLink(path: %s).", this.sendPath, deliveryTag));
                    AsyncUtil.completeFutureExceptionally(sendData.getWork(), completionException);
                }
            }
        } finally {
            synchronized (this.pendingSendLock) {
                if (this.isSendLoopRunning) {
                    this.isSendLoopRunning = false;
                }
            }
        }
    }

    private void throwSenderTimeout(CompletableFuture<DeliveryState> pendingSendWork, Exception lastKnownException) {
        Exception cause = lastKnownException;
        if (lastKnownException == null && this.lastKnownLinkError != null) {
            cause = this.lastKnownErrorReportedAt.isAfter(Instant.now().minusMillis(this.operationTimeout.toMillis())) ? this.lastKnownLinkError : null;
        }

        boolean isClientSideTimeout = (cause == null || !(cause instanceof ServiceBusException));
        ServiceBusException exception = isClientSideTimeout
                ? new TimeoutException(String.format(Locale.US, "%s %s %s.", CoreMessageSender.SEND_TIMED_OUT, " at ", ZonedDateTime.now(), cause))
                : (ServiceBusException) cause;

        TRACE_LOGGER.info("Send timed out", exception);
        ExceptionUtil.completeExceptionally(pendingSendWork, exception, this, true);
    }

    private void scheduleLinkCloseTimeout(final TimeoutTracker timeout) {
        // timer to signal a timeout if exceeds the operationTimeout on MessagingFactory
        Timer.schedule(
            () -> {
                if (!linkClose.isDone()) {
                    Exception operationTimedout = new TimeoutException(String.format(Locale.US, "%s operation on Send Link(%s) timed out at %s", "Close", CoreMessageSender.this.sendLink.getName(), ZonedDateTime.now()));
                    TRACE_LOGGER.info(operationTimedout.getMessage());

                    ExceptionUtil.completeExceptionally(linkClose, operationTimedout, CoreMessageSender.this, true);
                }
            },
            timeout.remaining(),
            TimerType.OneTimeRun);
    }

    @Override
    protected CompletableFuture<Void> onClose() {
        this.closeInternals(true);
        return this.linkClose;
    }

    private void closeInternals(boolean waitForCloseCompletion) {
        if (!this.getIsClosed()) {
            if (this.sendLink != null && this.sendLink.getLocalState() != EndpointState.CLOSED) {
                try {
                    this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler() {

                        @Override
                        public void onEvent() {
                            if (CoreMessageSender.this.sendLink != null && CoreMessageSender.this.sendLink.getLocalState() != EndpointState.CLOSED) {
                                TRACE_LOGGER.info("Closing send link to '{}'", CoreMessageSender.this.sendPath);
                                CoreMessageSender.this.underlyingFactory.deregisterForConnectionError(CoreMessageSender.this.sendLink);
                                CoreMessageSender.this.sendLink.close();
                                if (waitForCloseCompletion) {
                                    CoreMessageSender.this.scheduleLinkCloseTimeout(TimeoutTracker.create(CoreMessageSender.this.operationTimeout));
                                } else {
                                    AsyncUtil.completeFuture(CoreMessageSender.this.linkClose, null);
                                }
                            }
                        }
                    });
                } catch (IOException e) {
                    AsyncUtil.completeFutureExceptionally(this.linkClose, e);
                }
            } else {
                AsyncUtil.completeFuture(this.linkClose, null);
            }

            this.cancelSASTokenRenewTimer();
            this.closeRequestResponseLink();
        }
    }

    private static class WeightedDeliveryTag {
        private final String deliveryTag;
        private final int priority;

        WeightedDeliveryTag(final String deliveryTag, final int priority) {
            this.deliveryTag = deliveryTag;
            this.priority = priority;
        }

        public String getDeliveryTag() {
            return this.deliveryTag;
        }

        public int getPriority() {
            return this.priority;
        }
    }

    private static class DeliveryTagComparator implements Comparator<WeightedDeliveryTag>, Serializable {
        private static final long serialVersionUID = -7057500582037295636L;
        @Override
        public int compare(WeightedDeliveryTag deliveryTag0, WeightedDeliveryTag deliveryTag1) {
            return deliveryTag1.getPriority() - deliveryTag0.getPriority();
        }
    }

    public CompletableFuture<long[]> scheduleMessageAsync(Message[] messages, TransactionContext transaction, Duration timeout) {
        TRACE_LOGGER.debug("Sending '{}' scheduled message(s) to '{}'", messages.length, this.sendPath);
        return this.createRequestResponseLink().thenComposeAsync((v) -> {
            HashMap requestBodyMap = new HashMap();
            Collection<HashMap> messageList = new LinkedList<HashMap>();
            for (Message message : messages) {
                HashMap messageEntry = new HashMap();

                Pair<byte[], Integer> encodedPair;
                try {
                    encodedPair = Util.encodeMessageToOptimalSizeArray(message, this.maxMessageSize);
                } catch (PayloadSizeExceededException exception) {
                    TRACE_LOGGER.info("Payload size of message exceeded limit", exception);
                    final CompletableFuture<long[]> scheduleMessagesTask = new CompletableFuture<long[]>();
                    scheduleMessagesTask.completeExceptionally(exception);
                    return scheduleMessagesTask;
                }

                messageEntry.put(ClientConstants.REQUEST_RESPONSE_MESSAGE, new Binary(encodedPair.getFirstItem(), 0, encodedPair.getSecondItem()));
                messageEntry.put(ClientConstants.REQUEST_RESPONSE_MESSAGE_ID, message.getMessageId());

                String sessionId = message.getGroupId();
                if (!StringUtil.isNullOrEmpty(sessionId)) {
                    messageEntry.put(ClientConstants.REQUEST_RESPONSE_SESSION_ID, sessionId);
                }

                Object partitionKey = message.getMessageAnnotations().getValue().get(Symbol.valueOf(ClientConstants.PARTITIONKEYNAME));
                if (partitionKey != null && !((String) partitionKey).isEmpty()) {
                    messageEntry.put(ClientConstants.REQUEST_RESPONSE_PARTITION_KEY, partitionKey);
                }

                Object viaPartitionKey = message.getMessageAnnotations().getValue().get(Symbol.valueOf(ClientConstants.VIAPARTITIONKEYNAME));
                if (viaPartitionKey != null && !((String) viaPartitionKey).isEmpty()) {
                    messageEntry.put(ClientConstants.REQUEST_RESPONSE_VIA_PARTITION_KEY, viaPartitionKey);
                }

                messageList.add(messageEntry);
            }
            requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_MESSAGES, messageList);
            Message requestMessage = RequestResponseUtils.createRequestMessageFromPropertyBag(ClientConstants.REQUEST_RESPONSE_SCHEDULE_MESSAGE_OPERATION, requestBodyMap, Util.adjustServerTimeout(timeout), this.sendLink.getName());
            CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, transaction, timeout);
            return responseFuture.thenComposeAsync((responseMessage) -> {
                CompletableFuture<long[]> returningFuture = new CompletableFuture<>();
                int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
                if (statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE) {
                    long[] sequenceNumbers = (long[]) RequestResponseUtils.getResponseBody(responseMessage).get(ClientConstants.REQUEST_RESPONSE_SEQUENCE_NUMBERS);
                    if (TRACE_LOGGER.isDebugEnabled()) {
                        TRACE_LOGGER.debug("Scheduled messages sent. Received sequence numbers '{}'", Arrays.toString(sequenceNumbers));
                    }

                    returningFuture.complete(sequenceNumbers);
                } else {
                    // error response
                    Exception scheduleException = RequestResponseUtils.genereateExceptionFromResponse(responseMessage);
                    TRACE_LOGGER.info("Sending scheduled messages to '{}' failed.", this.sendPath, scheduleException);
                    returningFuture.completeExceptionally(scheduleException);
                }
                return returningFuture;
            }, MessagingFactory.INTERNAL_THREAD_POOL);
        }, MessagingFactory.INTERNAL_THREAD_POOL);
    }

    public CompletableFuture<Void> cancelScheduledMessageAsync(Long[] sequenceNumbers, Duration timeout) {
        if (TRACE_LOGGER.isDebugEnabled()) {
            TRACE_LOGGER.debug("Cancelling scheduled message(s) '{}' to '{}'", Arrays.toString(sequenceNumbers), this.sendPath);
        }

        return this.createRequestResponseLink().thenComposeAsync((v) -> {
            HashMap requestBodyMap = new HashMap();
            requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_SEQUENCE_NUMBERS, sequenceNumbers);

            Message requestMessage = RequestResponseUtils.createRequestMessageFromPropertyBag(ClientConstants.REQUEST_RESPONSE_CANCEL_CHEDULE_MESSAGE_OPERATION, requestBodyMap, Util.adjustServerTimeout(timeout), this.sendLink.getName());
            CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, TransactionContext.NULL_TXN, timeout);
            return responseFuture.thenComposeAsync((responseMessage) -> {
                CompletableFuture<Void> returningFuture = new CompletableFuture<Void>();
                int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
                if (statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE) {
                    TRACE_LOGGER.debug("Cancelled scheduled messages in '{}'", this.sendPath);
                    returningFuture.complete(null);
                } else {
                    // error response
                    Exception failureException = RequestResponseUtils.genereateExceptionFromResponse(responseMessage);
                    TRACE_LOGGER.info("Cancelling scheduled messages in '{}' failed.", this.sendPath, failureException);
                    returningFuture.completeExceptionally(failureException);
                }
                return returningFuture;
            }, MessagingFactory.INTERNAL_THREAD_POOL);
        }, MessagingFactory.INTERNAL_THREAD_POOL);
    }

    // In case we need to support peek on a topic, don't associate a send link
    public CompletableFuture<Collection<Message>> peekMessagesAsync(long fromSequenceNumber, int messageCount) {
        TRACE_LOGGER.debug("Peeking '{}' messages in '{}' from sequence number '{}'", messageCount, this.sendPath, fromSequenceNumber);
        return this.createRequestResponseLink().thenComposeAsync((v) -> CommonRequestResponseOperations.peekMessagesAsync(this.requestResponseLink, this.operationTimeout, fromSequenceNumber, messageCount, null, null), MessagingFactory.INTERNAL_THREAD_POOL);
    }
}
