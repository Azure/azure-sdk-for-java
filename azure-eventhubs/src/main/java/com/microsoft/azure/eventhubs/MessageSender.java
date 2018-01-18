/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.microsoft.azure.eventhubs.amqp.AmqpConstants;
import com.microsoft.azure.eventhubs.amqp.AmqpException;
import com.microsoft.azure.eventhubs.amqp.AmqpUtil;
import com.microsoft.azure.eventhubs.amqp.DispatchHandler;
import com.microsoft.azure.eventhubs.amqp.IAmqpSender;
import com.microsoft.azure.eventhubs.amqp.IOperationResult;
import com.microsoft.azure.eventhubs.amqp.SendLinkHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.apache.qpid.proton.message.Message;

/**
 * Abstracts all amqp related details
 * translates event-driven reactor model into async send Api
 */
public class MessageSender extends ClientEntity implements IAmqpSender, IErrorContextProvider {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(MessageSender.class);
    private static final String SEND_TIMED_OUT = "Send operation timed out";

    private final MessagingFactory underlyingFactory;
    private final String sendPath;
    private final Duration operationTimeout;
    private final RetryPolicy retryPolicy;
    private final CompletableFuture<Void> linkClose;
    private final Object pendingSendLock;
    private final ConcurrentHashMap<String, ReplayableWorkItem<Void>> pendingSendsData;
    private final PriorityQueue<WeightedDeliveryTag> pendingSends;
    private final DispatchHandler sendWork;
    private final ActiveClientTokenManager activeClientTokenManager;
    private final String tokenAudience;
    private final Object errorConditionLock;

    private volatile int maxMessageSize;

    private Sender sendLink;
    private CompletableFuture<MessageSender> linkFirstOpen;
    private int linkCredit;
    private TimeoutTracker openLinkTracker;
    private Exception lastKnownLinkError;
    private Instant lastKnownErrorReportedAt;
    private boolean creatingLink;
    private ScheduledFuture closeTimer;
    private ScheduledFuture openTimer;

    public static CompletableFuture<MessageSender> create(
            final MessagingFactory factory,
            final String sendLinkName,
            final String senderPath) {
        final MessageSender msgSender = new MessageSender(factory, sendLinkName, senderPath);
        msgSender.openLinkTracker = TimeoutTracker.create(factory.getOperationTimeout());
        msgSender.initializeLinkOpen(msgSender.openLinkTracker);

        try {
            msgSender.underlyingFactory.scheduleOnReactorThread(new DispatchHandler() {
                @Override
                public void onEvent() {
                    msgSender.createSendLink();
                }
            });
        } catch (IOException ioException) {
            msgSender.linkFirstOpen.completeExceptionally(new EventHubException(false, "Failed to create Sender, see cause for more details.", ioException));
        }

        return msgSender.linkFirstOpen;
    }

    private MessageSender(final MessagingFactory factory, final String sendLinkName, final String senderPath) {
        super(sendLinkName, factory, factory.executor);

        this.sendPath = senderPath;
        this.underlyingFactory = factory;
        this.operationTimeout = factory.getOperationTimeout();

        this.lastKnownLinkError = null;
        this.lastKnownErrorReportedAt = Instant.EPOCH;

        this.retryPolicy = factory.getRetryPolicy();
        this.maxMessageSize = ClientConstants.MAX_MESSAGE_LENGTH_BYTES;

        this.errorConditionLock = new Object();

        this.pendingSendLock = new Object();
        this.pendingSendsData = new ConcurrentHashMap<>();
        this.pendingSends = new PriorityQueue<>(1000, new DeliveryTagComparator());
        this.linkCredit = 0;

        this.linkClose = new CompletableFuture<>();

        this.sendWork = new DispatchHandler() {
            @Override
            public void onEvent() {
                MessageSender.this.processSendWork();
            }
        };

        this.tokenAudience = String.format(ClientConstants.TOKEN_AUDIENCE_FORMAT, underlyingFactory.getHostName(), sendPath);
        this.activeClientTokenManager = new ActiveClientTokenManager(
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            underlyingFactory.getCBSChannel().sendToken(
                                    underlyingFactory.getReactorScheduler(),
                                    underlyingFactory.getTokenProvider().getToken(tokenAudience, ClientConstants.TOKEN_VALIDITY),
                                    tokenAudience,
                                    new IOperationResult<Void, Exception>() {
                                        @Override
                                        public void onComplete(Void result) {
                                            if (TRACE_LOGGER.isDebugEnabled()) {
                                                TRACE_LOGGER.debug(String.format(Locale.US,
                                                        "path[%s], linkName[%s] - token renewed", sendPath, sendLink.getName()));
                                            }
                                        }

                                        @Override
                                        public void onError(Exception error) {
                                            if (TRACE_LOGGER.isInfoEnabled()) {
                                                TRACE_LOGGER.info(String.format(Locale.US,
                                                                "path[%s], linkName[%s] - tokenRenewalFailure[%s]", sendPath, sendLink.getName(), error.getMessage()));
                                            }
                                        }
                                    });
                        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | RuntimeException exception) {
                            if (TRACE_LOGGER.isWarnEnabled()) {
                                TRACE_LOGGER.warn(String.format(Locale.US,
                                                "path[%s], linkName[%s] - tokenRenewalScheduleFailure[%s]", sendPath, sendLink.getName(), exception.getMessage()));
                            }
                        }
                    }
                },
                ClientConstants.TOKEN_REFRESH_INTERVAL);
    }

    public String getSendPath() {
        return this.sendPath;
    }

    public int getMaxMessageSize() {
        return this.maxMessageSize;
    }

    private CompletableFuture<Void> send(byte[] bytes, int arrayOffset, int messageFormat) {
        return this.send(bytes, arrayOffset, messageFormat, null, null);
    }

    private CompletableFuture<Void> sendCore(
            final byte[] bytes,
            final int arrayOffset,
            final int messageFormat,
            final CompletableFuture<Void> onSend,
            final TimeoutTracker tracker,
            final Exception lastKnownError,
            final ScheduledFuture<?> timeoutTask) {
        this.throwIfClosed();

        final boolean isRetrySend = (onSend != null);

        final CompletableFuture<Void> onSendFuture = (onSend == null) ? new CompletableFuture<>() : onSend;

        final ReplayableWorkItem<Void> sendWaiterData = (tracker == null) ?
                new ReplayableWorkItem<>(bytes, arrayOffset, messageFormat, onSendFuture, this.operationTimeout) :
                new ReplayableWorkItem<>(bytes, arrayOffset, messageFormat, onSendFuture, tracker);

        final TimeoutTracker currentSendTracker = sendWaiterData.getTimeoutTracker();
        final String deliveryTag = UUID.randomUUID().toString().replace("-", StringUtil.EMPTY) + "_" + currentSendTracker.elapsed().getSeconds();

        if (lastKnownError != null) {
            sendWaiterData.setLastKnownException(lastKnownError);
        }

        if (timeoutTask != null)
            timeoutTask.cancel(false);

        final ScheduledFuture<?> timeoutTimerTask = Timer.schedule(
                new SendTimeout(deliveryTag, sendWaiterData),
                currentSendTracker.remaining(), TimerType.OneTimeRun);

        sendWaiterData.setTimeoutTask(timeoutTimerTask);

        synchronized (this.pendingSendLock) {
            this.pendingSendsData.put(deliveryTag, sendWaiterData);
            this.pendingSends.offer(new WeightedDeliveryTag(deliveryTag, isRetrySend ? 1 : 0));
        }

        try {
            this.underlyingFactory.scheduleOnReactorThread(this.sendWork);
        } catch (IOException ioException) {
            onSendFuture.completeExceptionally(
                    new OperationCancelledException("Send failed while dispatching to Reactor, see cause for more details.", ioException));
        }

        return onSendFuture;
    }

    private CompletableFuture<Void> send(
            final byte[] bytes,
            final int arrayOffset,
            final int messageFormat,
            final CompletableFuture<Void> onSend,
            final TimeoutTracker tracker) {
        return this.sendCore(bytes, arrayOffset, messageFormat, onSend, tracker, null, null);
    }

    public CompletableFuture<Void> send(final Iterable<Message> messages) {
        if (messages == null || IteratorUtil.sizeEquals(messages, 0)) {
            throw new IllegalArgumentException("Sending Empty batch of messages is not allowed.");
        }

        final Message firstMessage = messages.iterator().next();
        if (IteratorUtil.sizeEquals(messages, 1)) {
            return this.send(firstMessage);
        }

        // proton-j doesn't support multiple dataSections to be part of AmqpMessage
        // here's the alternate approach provided by them: https://github.com/apache/qpid-proton/pull/54
        final Message batchMessage = Proton.message();
        batchMessage.setMessageAnnotations(firstMessage.getMessageAnnotations());

        final int maxMessageSizeTemp = this.maxMessageSize;

        final byte[] bytes = new byte[maxMessageSizeTemp];
        int encodedSize = batchMessage.encode(bytes, 0, maxMessageSizeTemp);
        int byteArrayOffset = encodedSize;

        for (final Message amqpMessage : messages) {
            final Message messageWrappedByData = Proton.message();

            int payloadSize = AmqpUtil.getDataSerializedSize(amqpMessage);
            int allocationSize = Math.min(payloadSize + ClientConstants.MAX_EVENTHUB_AMQP_HEADER_SIZE_BYTES, maxMessageSizeTemp);

            byte[] messageBytes = new byte[allocationSize];
            int messageSizeBytes = amqpMessage.encode(messageBytes, 0, allocationSize);
            messageWrappedByData.setBody(new Data(new Binary(messageBytes, 0, messageSizeBytes)));

            try {
                encodedSize = messageWrappedByData.encode(bytes, byteArrayOffset, maxMessageSizeTemp - byteArrayOffset - 1);
            } catch (BufferOverflowException exception) {
                final CompletableFuture<Void> sendTask = new CompletableFuture<>();
                sendTask.completeExceptionally(new PayloadSizeExceededException(String.format("Size of the payload exceeded Maximum message size: %s kb", maxMessageSizeTemp / 1024), exception));
                return sendTask;
            }

            byteArrayOffset = byteArrayOffset + encodedSize;
        }

        return this.send(bytes, byteArrayOffset, AmqpConstants.AMQP_BATCH_MESSAGE_FORMAT);
    }

    public CompletableFuture<Void> send(Message msg) {
        int payloadSize = AmqpUtil.getDataSerializedSize(msg);

        final int maxMessageSizeTemp = this.maxMessageSize;
        int allocationSize = Math.min(payloadSize + ClientConstants.MAX_EVENTHUB_AMQP_HEADER_SIZE_BYTES, maxMessageSizeTemp);

        final byte[] bytes = new byte[allocationSize];
        int encodedSize = 0;
        try {
            encodedSize = msg.encode(bytes, 0, allocationSize);
        } catch (BufferOverflowException exception) {
            final CompletableFuture<Void> sendTask = new CompletableFuture<Void>();
            sendTask.completeExceptionally(new PayloadSizeExceededException(String.format("Size of the payload exceeded Maximum message size: %s kb", maxMessageSizeTemp / 1024), exception));
            return sendTask;
        }

        return this.send(bytes, encodedSize, DeliveryImpl.DEFAULT_MESSAGE_FORMAT);
    }

    @Override
    public void onOpenComplete(Exception completionException) {
        this.creatingLink = false;

        if (completionException == null) {
            if (this.getIsClosingOrClosed()) {

                this.sendLink.close();
                return;
            }

            this.openLinkTracker = null;

            synchronized (this.errorConditionLock) {
                this.lastKnownLinkError = null;
            }

            this.retryPolicy.resetRetryCount(this.getClientId());

            final UnsignedLong remoteMaxMessageSize = this.sendLink.getRemoteMaxMessageSize();
            if (remoteMaxMessageSize != null) {
                this.maxMessageSize = remoteMaxMessageSize.intValue();
            }

            if (!this.linkFirstOpen.isDone()) {
                this.linkFirstOpen.complete(this);
                if (this.openTimer != null)
                    this.openTimer.cancel(false);
            } else {
                synchronized (this.pendingSendLock) {
                    if (!this.pendingSendsData.isEmpty()) {
                        final List<String> unacknowledgedSends = new LinkedList<>();
                        unacknowledgedSends.addAll(this.pendingSendsData.keySet());

                        if (unacknowledgedSends.size() > 0) {
                            final Iterator<String> reverseReader = unacknowledgedSends.iterator();
                            while (reverseReader.hasNext()) {
                                final String unacknowledgedSend = reverseReader.next();
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
            if (!this.linkFirstOpen.isDone()) {
                this.setClosed();
                ExceptionUtil.completeExceptionally(this.linkFirstOpen, completionException, this);
                if (this.openTimer != null)
                    this.openTimer.cancel(false);
            }
        }
    }

    @Override
    public void onClose(final ErrorCondition condition) {
        final Exception completionException = (condition != null && condition.getCondition() != null) ? ExceptionUtil.toException(condition) : null;
        this.onError(completionException);
    }

    @Override
    public void onError(final Exception completionException) {
        this.linkCredit = 0;
        this.underlyingFactory.deregisterForConnectionError(this.sendLink);

        if (this.getIsClosingOrClosed()) {
            if (this.closeTimer != null && !this.closeTimer.isDone())
                this.closeTimer.cancel(false);

            synchronized (this.pendingSendLock) {
                for (Map.Entry<String, ReplayableWorkItem<Void>> pendingSend : this.pendingSendsData.entrySet()) {
                    ExceptionUtil.completeExceptionally(pendingSend.getValue().getWork(),
                            completionException == null
                                    ? new OperationCancelledException("Send cancelled as the Sender instance is Closed before the sendOperation completed.")
                                    : completionException,
                            this);
                }

                this.pendingSendsData.clear();
                this.pendingSends.clear();
            }

            this.linkClose.complete(null);

            return;
        } else {
            synchronized (this.errorConditionLock) {
                this.lastKnownLinkError = completionException == null ? this.lastKnownLinkError : completionException;
                this.lastKnownErrorReportedAt = Instant.now();
            }

            final Exception finalCompletionException = completionException == null
                    ? new EventHubException(true, "Client encountered transient error for unknown reasons, please retry the operation.") : completionException;

            this.onOpenComplete(finalCompletionException);

            final Map.Entry<String, ReplayableWorkItem<Void>> pendingSendEntry = IteratorUtil.getFirst(this.pendingSendsData.entrySet());
            if (pendingSendEntry != null && pendingSendEntry.getValue() != null) {
                final TimeoutTracker tracker = pendingSendEntry.getValue().getTimeoutTracker();
                if (tracker != null) {
                    final Duration nextRetryInterval = this.retryPolicy.getNextRetryInterval(this.getClientId(), finalCompletionException, tracker.remaining());
                    boolean scheduledRecreate = true;

                    if (nextRetryInterval != null) {
                        try {
                            this.underlyingFactory.scheduleOnReactorThread((int) nextRetryInterval.toMillis(), new DispatchHandler() {
                                @Override
                                public void onEvent() {
                                    if (!MessageSender.this.getIsClosingOrClosed()
                                            && (sendLink.getLocalState() == EndpointState.CLOSED || sendLink.getRemoteState() == EndpointState.CLOSED)) {
                                        recreateSendLink();
                                    }
                                }
                            });
                        } catch (IOException ignore) {
                            scheduledRecreate = false;
                        }
                    }

                    if (nextRetryInterval == null || !scheduledRecreate) {
                        synchronized (this.pendingSendLock) {
                            for (Map.Entry<String, ReplayableWorkItem<Void>> pendingSend : this.pendingSendsData.entrySet()) {
                                this.cleanupFailedSend(pendingSend.getValue(), finalCompletionException);
                            }

                            this.pendingSendsData.clear();
                            this.pendingSends.clear();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onSendComplete(final Delivery delivery) {
        final DeliveryState outcome = delivery.getRemoteState();
        final String deliveryTag = new String(delivery.getTag());

        if (TRACE_LOGGER.isTraceEnabled())
            TRACE_LOGGER.trace(
                    String.format(
                            Locale.US,
                            "path[%s], linkName[%s], deliveryTag[%s]", MessageSender.this.sendPath, this.sendLink.getName(), deliveryTag));

        final ReplayableWorkItem<Void> pendingSendWorkItem = this.pendingSendsData.remove(deliveryTag);

        if (pendingSendWorkItem != null) {
            if (outcome instanceof Accepted) {
                synchronized (this.errorConditionLock) {
                    this.lastKnownLinkError = null;
                }

                this.retryPolicy.resetRetryCount(this.getClientId());

                pendingSendWorkItem.getTimeoutTask().cancel(false);
                pendingSendWorkItem.getWork().complete(null);
            } else if (outcome instanceof Rejected) {
                final Rejected rejected = (Rejected) outcome;
                final ErrorCondition error = rejected.getError();

                final Exception exception = ExceptionUtil.toException(error);

                if (ExceptionUtil.isGeneralSendError(error.getCondition())) {
                    synchronized (this.errorConditionLock) {
                        this.lastKnownLinkError = exception;
                        this.lastKnownErrorReportedAt = Instant.now();
                    }
                }

                final Duration retryInterval = this.retryPolicy.getNextRetryInterval(
                        this.getClientId(), exception, pendingSendWorkItem.getTimeoutTracker().remaining());
                if (retryInterval == null) {
                    this.cleanupFailedSend(pendingSendWorkItem, exception);
                } else {
                    pendingSendWorkItem.setLastKnownException(exception);
                    try {
                        this.underlyingFactory.scheduleOnReactorThread((int) retryInterval.toMillis(),
                                new DispatchHandler() {
                                    @Override
                                    public void onEvent() {
                                        MessageSender.this.sendCore(
                                                pendingSendWorkItem.getMessage(),
                                                pendingSendWorkItem.getEncodedMessageSize(),
                                                pendingSendWorkItem.getMessageFormat(),
                                                pendingSendWorkItem.getWork(),
                                                pendingSendWorkItem.getTimeoutTracker(),
                                                pendingSendWorkItem.getLastKnownException(),
                                                pendingSendWorkItem.getTimeoutTask());
                                    }
                                });
                    } catch (IOException ioException) {
                        exception.initCause(ioException);
                        this.cleanupFailedSend(
                                pendingSendWorkItem,
                                new EventHubException(false, "Send operation failed while scheduling a retry on Reactor, see cause for more details.", ioException));
                    }
                }
            } else if (outcome instanceof Released) {
                this.cleanupFailedSend(pendingSendWorkItem, new OperationCancelledException(outcome.toString()));
            } else {
                this.cleanupFailedSend(pendingSendWorkItem, new EventHubException(false, outcome.toString()));
            }
        } else {
            if (TRACE_LOGGER.isDebugEnabled())
                TRACE_LOGGER.debug(
                        String.format(Locale.US, "path[%s], linkName[%s], delivery[%s] - mismatch (or send timedout)", this.sendPath, this.sendLink.getName(), deliveryTag));
        }
    }

    private void cleanupFailedSend(final ReplayableWorkItem<Void> failedSend, final Exception exception) {
        if (failedSend.getTimeoutTask() != null)
            failedSend.getTimeoutTask().cancel(false);

        ExceptionUtil.completeExceptionally(failedSend.getWork(), exception, this);
    }

    private void createSendLink() {
        if (this.creatingLink)
            return;

        this.creatingLink = true;

        final Consumer<Session> onSessionOpen = new Consumer<Session>() {
            @Override
            public void accept(Session session) {
                if (MessageSender.this.getIsClosingOrClosed()) {

                    session.close();
                    return;
                }

                final Sender sender = session.sender(TrackingUtil.getLinkName(session));
                final Target target = new Target();
                target.setAddress(sendPath);
                sender.setTarget(target);

                final Source source = new Source();
                sender.setSource(source);

                sender.setSenderSettleMode(SenderSettleMode.UNSETTLED);

                final SendLinkHandler handler = new SendLinkHandler(MessageSender.this);
                BaseHandler.setHandler(sender, handler);

                MessageSender.this.underlyingFactory.registerForConnectionError(sender);
                sender.open();

                synchronized (MessageSender.this.errorConditionLock) {
                    MessageSender.this.sendLink = sender;
                }
            }
        };

        final BiConsumer<ErrorCondition, Exception> onSessionOpenError = new BiConsumer<ErrorCondition, Exception>() {
            @Override
            public void accept(ErrorCondition t, Exception u) {
                if (t != null)
                    MessageSender.this.onError((t != null && t.getCondition() != null) ? ExceptionUtil.toException(t) : null);
                else if (u != null)
                    MessageSender.this.onError(u);
            }
        };

        try {
            this.underlyingFactory.getCBSChannel().sendToken(
                    this.underlyingFactory.getReactorScheduler(),
                    this.underlyingFactory.getTokenProvider().getToken(tokenAudience, ClientConstants.TOKEN_VALIDITY),
                    tokenAudience,
                    new IOperationResult<Void, Exception>() {
                        @Override
                        public void onComplete(Void result) {
                            if (MessageSender.this.getIsClosingOrClosed())
                                return;

                            underlyingFactory.getSession(
                                    sendPath,
                                    onSessionOpen,
                                    onSessionOpenError);
                        }

                        @Override
                        public void onError(Exception error) {
                            final Exception completionException;
                            if (error!= null && error instanceof AmqpException) {
                                completionException = ExceptionUtil.toException(((AmqpException) error).getError());
                                if (completionException != error && completionException.getCause() == null) {
                                    completionException.initCause(error);
                                }
                            }
                            else {
                                completionException = error;
                            }

                            MessageSender.this.onError(completionException);
                        }
                    });
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | RuntimeException exception) {
            MessageSender.this.onError(exception);
        }
    }

    // TODO: consolidate common-code written for timeouts in Sender/Receiver
    private void initializeLinkOpen(TimeoutTracker timeout) {
        this.linkFirstOpen = new CompletableFuture<>();

        // timer to signal a timeout if exceeds the operationTimeout on MessagingFactory
        this.openTimer = Timer.schedule(
                new Runnable() {
                    public void run() {
                        if (!MessageSender.this.linkFirstOpen.isDone()) {
                            final Exception lastReportedError;
                            final Instant lastErrorReportedAt;
                            final Sender link;
                            synchronized (MessageSender.this.errorConditionLock) {
                                lastReportedError = MessageSender.this.lastKnownLinkError;
                                lastErrorReportedAt = MessageSender.this.lastKnownErrorReportedAt;
                                link = MessageSender.this.sendLink;
                            }

                            final Exception operationTimedout = new TimeoutException(
                                    String.format(Locale.US, "Open operation on SendLink(%s) on Entity(%s) timed out at %s.", link.getName(), MessageSender.this.getSendPath(), ZonedDateTime.now().toString()),
                                    lastErrorReportedAt.isAfter(Instant.now().minusSeconds(ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS)) ? lastReportedError : null);

                            if (TRACE_LOGGER.isWarnEnabled()) {
                                TRACE_LOGGER.warn(
                                        String.format(Locale.US, "path[%s], linkName[%s], open call timedout", MessageSender.this.sendPath, MessageSender.this.sendLink.getName()),
                                        operationTimedout);
                            }

                            ExceptionUtil.completeExceptionally(MessageSender.this.linkFirstOpen, operationTimedout, MessageSender.this);
                        }
                    }
                }
                , timeout.remaining()
                , TimerType.OneTimeRun);
    }

    @Override
    public ErrorContext getContext() {
        final Sender link;
        synchronized (this.errorConditionLock) {
            link = this.sendLink;
        }

        final boolean isLinkOpened = this.linkFirstOpen != null && this.linkFirstOpen.isDone();
        final String referenceId = link != null && link.getRemoteProperties() != null && link.getRemoteProperties().containsKey(ClientConstants.TRACKING_ID_PROPERTY)
                ? link.getRemoteProperties().get(ClientConstants.TRACKING_ID_PROPERTY).toString()
                : ((link != null) ? link.getName() : null);

        final SenderContext errorContext = new SenderContext(
                this.underlyingFactory != null ? this.underlyingFactory.getHostName() : null,
                this.sendPath,
                referenceId,
                isLinkOpened && link != null ? link.getCredit() : null);
        return errorContext;
    }

    @Override
    public void onFlow(final int creditIssued) {
        synchronized (this.errorConditionLock) {
            this.lastKnownLinkError = null;
        }

        if (creditIssued <= 0)
            return;

        if (TRACE_LOGGER.isDebugEnabled()) {
            int numberOfSendsWaitingforCredit = this.pendingSends.size();
            TRACE_LOGGER.debug(String.format(Locale.US, "path[%s], linkName[%s], remoteLinkCredit[%s], pendingSendsWaitingForCredit[%s], pendingSendsWaitingDelivery[%s]",
                    this.sendPath, this.sendLink.getName(), creditIssued, numberOfSendsWaitingforCredit, this.pendingSendsData.size() - numberOfSendsWaitingforCredit));
        }

        this.linkCredit = this.linkCredit + creditIssued;
        this.sendWork.onEvent();
    }

    private void recreateSendLink() {
        this.createSendLink();
        this.retryPolicy.incrementRetryCount(this.getClientId());
    }

    // actual send on the SenderLink should happen only in this method & should run on Reactor Thread
    private void processSendWork() {
        if (this.sendLink.getLocalState() == EndpointState.CLOSED || this.sendLink.getRemoteState() == EndpointState.CLOSED) {
            if (!this.getIsClosingOrClosed())
                this.recreateSendLink();

            return;
        }

        while (this.sendLink.getLocalState() == EndpointState.ACTIVE && this.sendLink.getRemoteState() == EndpointState.ACTIVE
                && this.linkCredit > 0) {
            final WeightedDeliveryTag weightedDelivery;
            final ReplayableWorkItem<Void> sendData;
            final String deliveryTag;
            synchronized (this.pendingSendLock) {
                weightedDelivery = this.pendingSends.poll();
                if (weightedDelivery != null) {
                    deliveryTag = weightedDelivery.getDeliveryTag();
                    sendData = this.pendingSendsData.get(deliveryTag);
                } else {
                    sendData = null;
                    deliveryTag = null;
                }
            }

            if (sendData != null) {
                if (sendData.getWork() != null && sendData.getWork().isDone()) {
                    // CoreSend could enque Sends into PendingSends Queue and can fail the SendCompletableFuture
                    // (when It fails to schedule the ProcessSendWork on reactor Thread)
                    this.pendingSendsData.remove(deliveryTag);
                    continue;
                }

                Delivery delivery = null;
                boolean linkAdvance = false;
                int sentMsgSize = 0;
                Exception sendException = null;

                try {
                    delivery = this.sendLink.delivery(deliveryTag.getBytes());
                    delivery.setMessageFormat(sendData.getMessageFormat());

                    sentMsgSize = this.sendLink.send(sendData.getMessage(), 0, sendData.getEncodedMessageSize());
                    assert sentMsgSize == sendData.getEncodedMessageSize() : "Contract of the ProtonJ library for Sender.Send API changed";

                    linkAdvance = this.sendLink.advance();
                } catch (Exception exception) {
                    sendException = exception;
                }

                if (linkAdvance) {
                    this.linkCredit--;
                    sendData.setWaitingForAck();
                } else {
                    if (TRACE_LOGGER.isDebugEnabled()) {
                        TRACE_LOGGER.debug(
                                String.format(Locale.US, "path[%s], linkName[%s], deliveryTag[%s], sentMessageSize[%s], payloadActualSize[%s] - sendlink advance failed",
                                        this.sendPath, this.sendLink.getName(), deliveryTag, sentMsgSize, sendData.getEncodedMessageSize()));
                    }

                    if (delivery != null) {
                        delivery.free();
                    }

                    sendData.getWork().completeExceptionally(sendException != null
                            ? new OperationCancelledException("Send operation failed. Please see cause for more details", sendException)
                            : new OperationCancelledException(
                            String.format(Locale.US, "Send operation failed while advancing delivery(tag: %s) on SendLink(path: %s).", this.sendPath, deliveryTag)));
                }
            } else {
                if (deliveryTag != null) {
                    if (TRACE_LOGGER.isDebugEnabled()) {
                        TRACE_LOGGER.debug(
                                String.format(Locale.US, "path[%s], linkName[%s], deliveryTag[%s] - sendData not found for this delivery.",
                                        this.sendPath, this.sendLink.getName(), deliveryTag));
                    }
                }

                break;
            }
        }
    }

    private void throwSenderTimeout(final CompletableFuture<Void> pendingSendWork, final Exception lastKnownException) {

        Exception cause = lastKnownException;
        if (lastKnownException == null) {
            final Exception lastReportedLinkLevelError;
            final Instant lastLinkErrorReportedAt;
            synchronized (this.errorConditionLock) {
                lastReportedLinkLevelError = this.lastKnownLinkError;
                lastLinkErrorReportedAt = this.lastKnownErrorReportedAt;
            }

            if (lastReportedLinkLevelError != null) {
                boolean isServerBusy = ((lastReportedLinkLevelError instanceof ServerBusyException)
                        && (lastLinkErrorReportedAt.isAfter(Instant.now().minusSeconds(ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS))));
                cause = isServerBusy || (lastLinkErrorReportedAt.isAfter(Instant.now().minusMillis(this.operationTimeout.toMillis())))
                        ? lastReportedLinkLevelError
                        : null;
            }
        }

        final boolean isClientSideTimeout = (cause == null || !(cause instanceof EventHubException));
        final EventHubException exception = isClientSideTimeout
                ? new TimeoutException(String.format(Locale.US, "%s %s %s.", MessageSender.SEND_TIMED_OUT, " at ", ZonedDateTime.now(), cause))
                : (EventHubException) cause;

        ExceptionUtil.completeExceptionally(pendingSendWork, exception, this);
    }

    private void scheduleLinkCloseTimeout(final TimeoutTracker timeout) {
        // timer to signal a timeout if exceeds the operationTimeout on MessagingFactory
        this.closeTimer = Timer.schedule(
                new Runnable() {
                    public void run() {
                        if (!linkClose.isDone()) {
                            final Sender link;
                            synchronized (MessageSender.this.errorConditionLock) {
                                link = MessageSender.this.sendLink;
                            }

                            final Exception operationTimedout = new TimeoutException(String.format(Locale.US, "%s operation on Receive Link(%s) timed out at %s", "Close", link.getName(), ZonedDateTime.now()));
                            if (TRACE_LOGGER.isInfoEnabled()) {
                                TRACE_LOGGER.info(
                                        String.format(Locale.US, "message recever(linkName: %s, path: %s) %s call timedout", link.getName(), MessageSender.this.sendPath, "Close"),
                                        operationTimedout);
                            }

                            ExceptionUtil.completeExceptionally(linkClose, operationTimedout, MessageSender.this);
                            MessageSender.this.onError((Exception) null);
                        }
                    }
                }
                , timeout.remaining()
                , TimerType.OneTimeRun);
    }

    @Override
    protected CompletableFuture<Void> onClose() {
        if (!this.getIsClosed()) {
            try {
                this.activeClientTokenManager.cancel();
                scheduleLinkCloseTimeout(TimeoutTracker.create(operationTimeout));
                this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler() {
                    @Override
                    public void onEvent() {
                        if (sendLink != null && sendLink.getLocalState() != EndpointState.CLOSED) {
                            sendLink.close();
                        } else if (sendLink == null || sendLink.getRemoteState() == EndpointState.CLOSED) {
                            if (closeTimer != null)
                                closeTimer.cancel(false);

                            linkClose.complete(null);
                        }
                    }
                });

            } catch (IOException ioException) {
                this.linkClose.completeExceptionally(new EventHubException(false, "Scheduling close failed. See cause for more details.", ioException));
            }
        }

        return this.linkClose;
    }

    @Override
    protected Exception getLastKnownError() {
        synchronized (this.errorConditionLock) {
            return this.lastKnownLinkError;
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

    private static class DeliveryTagComparator implements Comparator<WeightedDeliveryTag> {
        @Override
        public int compare(WeightedDeliveryTag deliveryTag0, WeightedDeliveryTag deliveryTag1) {
            return deliveryTag1.getPriority() - deliveryTag0.getPriority();
        }
    }

    private class SendTimeout implements Runnable {
        private final String deliveryTag;
        private final ReplayableWorkItem<Void> sendWaiterData;

        public SendTimeout(
                final String deliveryTag,
                final ReplayableWorkItem<Void> sendWaiterData) {
            this.sendWaiterData = sendWaiterData;
            this.deliveryTag = deliveryTag;
        }

        @Override
        public void run() {
            if (!sendWaiterData.getWork().isDone()) {
                MessageSender.this.pendingSendsData.remove(deliveryTag);
                MessageSender.this.throwSenderTimeout(sendWaiterData.getWork(), sendWaiterData.getLastKnownException());
            }
        }
    }
}
