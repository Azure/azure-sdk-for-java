// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.BatchOptions;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventDataBatch;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubClientOptions;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventHubRuntimeInformation;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.ITokenProvider;
import com.microsoft.azure.eventhubs.OperationCancelledException;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.PartitionRuntimeInformation;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.ReceiverOptions;
import com.microsoft.azure.eventhubs.RetryPolicy;
import com.microsoft.azure.eventhubs.impl.MessagingFactory.MessagingFactoryBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

public final class EventHubClientImpl extends ClientEntity implements EventHubClient {

    /**
     * It will be truncated to 128 characters
     */
    public static String USER_AGENT = null;

    private final String eventHubName;
    private final Object senderCreateSync;
    private volatile boolean isSenderCreateStarted;
    private volatile MessagingFactory underlyingFactory;
    private volatile MessageSender sender;
    private volatile Timer timer;

    private CompletableFuture<Void> createSender;

    private EventHubClientImpl(final String eventHubName, final ScheduledExecutorService executor) {
        super(StringUtil.getRandomString("EC"), null, executor);

        this.eventHubName = eventHubName;
        this.senderCreateSync = new Object();
    }

    public static CompletableFuture<EventHubClient> create(
            final String connectionString, final RetryPolicy retryPolicy, final ScheduledExecutorService executor)
            throws IOException {
        if (StringUtil.isNullOrWhiteSpace(connectionString)) {
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }
        Objects.requireNonNull(executor, "Executor cannot be null");
        
        final ConnectionStringBuilder connStr = new ConnectionStringBuilder(connectionString);
        final EventHubClientImpl eventHubClient = new EventHubClientImpl(connStr.getEventHubName(), executor);

        return MessagingFactory.createFromConnectionString(connectionString, retryPolicy, executor)
                .thenApplyAsync(new Function<MessagingFactory, EventHubClient>() {
                    @Override
                    public EventHubClient apply(MessagingFactory factory) {
                        eventHubClient.underlyingFactory = factory;
                        eventHubClient.timer = new Timer(factory);
                        return eventHubClient;
                    }
                }, executor);
    }

    public static CompletableFuture<EventHubClient> create(
            final URI endpoint,
            final String eventHubName,
            final ITokenProvider tokenProvider,
            final ScheduledExecutorService executor,
            final EventHubClientOptions options) throws IOException {
        if (StringUtil.isNullOrWhiteSpace(endpoint.getHost())) {
            throw new IllegalArgumentException("Endpoint must contain a hostname");
        }
        if (StringUtil.isNullOrWhiteSpace(eventHubName)) {
            throw new IllegalArgumentException("Event hub name cannot be null or empty");
        }
        Objects.requireNonNull(tokenProvider, "Token provider cannot be null");
        
        final EventHubClientImpl eventHubClient = new EventHubClientImpl(eventHubName, executor);
        final MessagingFactoryBuilder builder = new MessagingFactoryBuilder(endpoint.getHost(), tokenProvider, executor);
        if (options != null) {
            builder.setOperationTimeout(options.getOperationTimeout()).setTransportType(options.getTransportType()).
                    setRetryPolicy(options.getRetryPolicy());
        }

        return builder.build()
                .thenApplyAsync(new Function<MessagingFactory, EventHubClient>() {
                    @Override
                    public EventHubClient apply(MessagingFactory factory) {
                        eventHubClient.underlyingFactory = factory;
                        eventHubClient.timer = new Timer(factory);
                        return eventHubClient;
                    }
                }, executor);
    }

    public String getEventHubName() {
        return eventHubName;
    }

    public EventDataBatch createBatch(BatchOptions options) throws EventHubException {

        return ExceptionUtil.sync(() -> {
                int maxSize = this.createInternalSender().thenApplyAsync(
                    (aVoid) -> this.sender.getMaxMessageSize(),
                    this.executor).get();
                if (options.maxMessageSize == null) {
                    return new EventDataBatchImpl(maxSize, options.partitionKey);
                }

                if (options.maxMessageSize > maxSize) {
                    throw new IllegalArgumentException("The maxMessageSize set in BatchOptions is too large. You set a maxMessageSize of "
                            + options.maxMessageSize + ". The maximum allowed size is " + maxSize + ".");
                }

                return new EventDataBatchImpl(options.maxMessageSize, options.partitionKey);
            }
        );
    }

    @Override
    public CompletableFuture<Void> send(final EventData data) {
        if (data == null) {
            throw new IllegalArgumentException("EventData cannot be empty.");
        }

        return this.createInternalSender().thenComposeAsync(new Function<Void, CompletableFuture<Void>>() {
            @Override
            public CompletableFuture<Void> apply(Void voidArg) {
                return EventHubClientImpl.this.sender.send(((EventDataImpl) data).toAmqpMessage());
            }
        }, this.executor);
    }

    @Override
    public CompletableFuture<Void> send(final Iterable<EventData> eventDatas) {
        if (eventDatas == null || IteratorUtil.sizeEquals(eventDatas, 0)) {
            throw new IllegalArgumentException("Empty batch of EventData cannot be sent.");
        }

        return this.createInternalSender().thenComposeAsync(new Function<Void, CompletableFuture<Void>>() {
            @Override
            public CompletableFuture<Void> apply(Void voidArg) {
                return EventHubClientImpl.this.sender.send(EventDataUtil.toAmqpMessages(eventDatas));
            }
        }, this.executor);
    }

    @Override
    public CompletableFuture<Void> send(final EventDataBatch eventDatas) {
        if (eventDatas == null || Integer.compare(eventDatas.getSize(), 0) == 0) {
            throw new IllegalArgumentException("Empty batch of EventData cannot be sent.");
        }

        final EventDataBatchImpl eventDataBatch = (EventDataBatchImpl) eventDatas;
        return eventDataBatch.getPartitionKey() != null
                ? this.send(eventDataBatch.getInternalIterable(), eventDataBatch.getPartitionKey())
                : this.send(eventDataBatch.getInternalIterable());
    }

    @Override
    public CompletableFuture<Void> send(final EventData eventData, final String partitionKey) {
        if (eventData == null) {
            throw new IllegalArgumentException("EventData cannot be null.");
        }

        if (partitionKey == null) {
            throw new IllegalArgumentException("partitionKey cannot be null");
        }

        return this.createInternalSender().thenComposeAsync(new Function<Void, CompletableFuture<Void>>() {
            @Override
            public CompletableFuture<Void> apply(Void voidArg) {
                return EventHubClientImpl.this.sender.send(((EventDataImpl) eventData).toAmqpMessage(partitionKey));
            }
        }, this.executor);
    }

    @Override
    public CompletableFuture<Void> send(final Iterable<EventData> eventDatas, final String partitionKey) {
        if (eventDatas == null || IteratorUtil.sizeEquals(eventDatas, 0)) {
            throw new IllegalArgumentException("Empty batch of EventData cannot be sent.");
        }

        if (partitionKey == null) {
            throw new IllegalArgumentException("partitionKey cannot be null");
        }

        if (partitionKey.length() > ClientConstants.MAX_PARTITION_KEY_LENGTH) {
            throw new IllegalArgumentException(
                    String.format(Locale.US, "PartitionKey exceeds the maximum allowed length of partitionKey: %s", ClientConstants.MAX_PARTITION_KEY_LENGTH));
        }

        return this.createInternalSender().thenComposeAsync(new Function<Void, CompletableFuture<Void>>() {
            @Override
            public CompletableFuture<Void> apply(Void voidArg) {
                return EventHubClientImpl.this.sender.send(EventDataUtil.toAmqpMessages(eventDatas, partitionKey));
            }
        }, this.executor);
    }

    @Override
    public CompletableFuture<PartitionSender> createPartitionSender(final String partitionId)
            throws EventHubException {
        return PartitionSenderImpl.create(this.underlyingFactory, this.eventHubName, partitionId, this.executor);
    }

    @Override
    public CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final EventPosition eventPosition)
            throws EventHubException {
        return this.createReceiver(consumerGroupName, partitionId, eventPosition, null);
    }

    @Override
    public CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final EventPosition eventPosition, final ReceiverOptions receiverOptions)
            throws EventHubException {
        return PartitionReceiverImpl.create(this.underlyingFactory, this.eventHubName, consumerGroupName, partitionId, eventPosition, PartitionReceiverImpl.NULL_EPOCH, false, receiverOptions, this.executor);
    }

    @Override
    public CompletableFuture<PartitionReceiver> createEpochReceiver(final String consumerGroupName, final String partitionId, final EventPosition eventPosition, final long epoch)
            throws EventHubException {
        return this.createEpochReceiver(consumerGroupName, partitionId, eventPosition, epoch, null);
    }

    @Override
    public CompletableFuture<PartitionReceiver> createEpochReceiver(final String consumerGroupName, final String partitionId, final EventPosition eventPosition, final long epoch, final ReceiverOptions receiverOptions)
            throws EventHubException {
        return PartitionReceiverImpl.create(this.underlyingFactory, this.eventHubName, consumerGroupName, partitionId, eventPosition, epoch, true, receiverOptions, this.executor);
    }

    @Override
    public CompletableFuture<Void> onClose() {
        if (this.underlyingFactory != null) {
            synchronized (this.senderCreateSync) {
                final CompletableFuture<Void> internalSenderClose = this.sender != null
                        ? this.sender.close().thenComposeAsync(new Function<Void, CompletableFuture<Void>>() {
                                @Override
                                public CompletableFuture<Void> apply(Void voidArg) {
                                    return EventHubClientImpl.this.underlyingFactory.close();
                                }
                            }, this.executor)
                        : this.underlyingFactory.close();

                return internalSenderClose;
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> createInternalSender() {
        if (!this.isSenderCreateStarted) {
            synchronized (this.senderCreateSync) {
                if (!this.isSenderCreateStarted) {
                    String senderName = StringUtil.getRandomString("EC").concat(StringUtil.SEPARATOR + this.underlyingFactory.getClientId()).concat("-InternalSender");
                    this.createSender = MessageSender.create(this.underlyingFactory, senderName, this.eventHubName)
                            .thenAcceptAsync(new Consumer<MessageSender>() {
                                public void accept(MessageSender a) {
                                    EventHubClientImpl.this.sender = a;
                                }
                            }, this.executor);

                    this.isSenderCreateStarted = true;
                }
            }
        }

        return this.createSender;
    }

    @Override
    public CompletableFuture<EventHubRuntimeInformation> getRuntimeInformation() {
        throwIfClosed();

        Map<String, Object> request = new HashMap<String, Object>();
        request.put(ClientConstants.MANAGEMENT_ENTITY_TYPE_KEY, ClientConstants.MANAGEMENT_EVENTHUB_ENTITY_TYPE);
        request.put(ClientConstants.MANAGEMENT_ENTITY_NAME_KEY, this.eventHubName);
        request.put(ClientConstants.MANAGEMENT_OPERATION_KEY, ClientConstants.READ_OPERATION_VALUE);
        return addManagementToken(request).thenComposeAsync((requestWithToken) -> managementWithRetry(requestWithToken), this.executor).
                thenApplyAsync((rawdata) -> {
                    return new EventHubRuntimeInformation(
                            (String) rawdata.get(ClientConstants.MANAGEMENT_ENTITY_NAME_KEY),
                            ((Date) rawdata.get(ClientConstants.MANAGEMENT_RESULT_CREATED_AT)).toInstant(),
                            (int) rawdata.get(ClientConstants.MANAGEMENT_RESULT_PARTITION_COUNT),
                            (String[]) rawdata.get(ClientConstants.MANAGEMENT_RESULT_PARTITION_IDS));
                }, this.executor);
    }

    @Override
    public CompletableFuture<PartitionRuntimeInformation> getPartitionRuntimeInformation(String partitionId) {
        throwIfClosed();

        Map<String, Object> request = new HashMap<String, Object>();
        request.put(ClientConstants.MANAGEMENT_ENTITY_TYPE_KEY, ClientConstants.MANAGEMENT_PARTITION_ENTITY_TYPE);
        request.put(ClientConstants.MANAGEMENT_ENTITY_NAME_KEY, this.eventHubName);
        request.put(ClientConstants.MANAGEMENT_PARTITION_NAME_KEY, partitionId);
        request.put(ClientConstants.MANAGEMENT_OPERATION_KEY, ClientConstants.READ_OPERATION_VALUE);
        return addManagementToken(request).thenComposeAsync((requestWithToken) -> managementWithRetry(requestWithToken), this.executor).
                thenApplyAsync((rawdata) -> {
                    return new PartitionRuntimeInformation(
                            (String) rawdata.get(ClientConstants.MANAGEMENT_ENTITY_NAME_KEY),
                            (String) rawdata.get(ClientConstants.MANAGEMENT_PARTITION_NAME_KEY),
                            (long) rawdata.get(ClientConstants.MANAGEMENT_RESULT_BEGIN_SEQUENCE_NUMBER),
                            (long) rawdata.get(ClientConstants.MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER),
                            (String) rawdata.get(ClientConstants.MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET),
                            ((Date) rawdata.get(ClientConstants.MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC)).toInstant(),
                            (boolean) rawdata.get(ClientConstants.MANAGEMENT_RESULT_PARTITION_IS_EMPTY));
                }, this.executor);    
    }

    private CompletableFuture<Map<String, Object>> addManagementToken(Map<String, Object> request) {
        String audience = String.format(Locale.US, "amqp://%s/%s", this.underlyingFactory.getHostName(), this.eventHubName);
        return this.underlyingFactory.getTokenProvider().getToken(audience, ClientConstants.TOKEN_REFRESH_INTERVAL).thenApplyAsync((securityToken) -> {
            request.put(ClientConstants.MANAGEMENT_SECURITY_TOKEN_KEY, securityToken.getToken());
            return request;
        }, this.executor);
    }

    private CompletableFuture<Map<String, Object>> managementWithRetry(Map<String, Object> request) {
        final CompletableFuture<Map<String, Object>> rawdataFuture = new CompletableFuture<Map<String, Object>>();

        final ManagementRetry retrier = new ManagementRetry(
                rawdataFuture,
                new TimeoutTracker(this.underlyingFactory.getOperationTimeout(), true),
                this.underlyingFactory,
                request);

        final CompletableFuture<?> scheduledTask = this.timer.schedule(retrier, Duration.ZERO);
        if (scheduledTask.isCompletedExceptionally()) {
            rawdataFuture.completeExceptionally(ExceptionUtil.getExceptionFromCompletedFuture(scheduledTask));
        }

        return rawdataFuture;
    }

    private class ManagementRetry implements Runnable {
        private final CompletableFuture<Map<String, Object>> finalFuture;
        private final TimeoutTracker timeoutTracker;
        private final MessagingFactory mf;
        private final Map<String, Object> request;

        ManagementRetry(final CompletableFuture<Map<String, Object>> future,
                        final TimeoutTracker timeoutTracker,
                        final MessagingFactory mf,
                        final Map<String, Object> request) {
            this.finalFuture = future;
            this.timeoutTracker = timeoutTracker;
            this.mf = mf;
            this.request = request;
        }

        @Override
        public void run() {
            final long timeLeft = this.timeoutTracker.remaining().toMillis();
            final CompletableFuture<Map<String, Object>> intermediateFuture = this.mf.getManagementChannel()
                    .request(this.mf.getReactorDispatcher(),
                            this.request,
                            timeLeft > 0 ? timeLeft : 0);

            intermediateFuture.whenComplete((final Map<String, Object> result, final Throwable error) -> {
                if ((result != null) && (error == null)) {
                    // Success!
                    ManagementRetry.this.finalFuture.complete(result);
                } else {
                    final Exception lastException;
                    final Throwable completeWith;
                    if (error == null) {
                        // Timeout, so fake up an exception to keep getNextRetryInternal happy.
                        // It has to be a EventHubException that is set to retryable or getNextRetryInterval will halt the retries.
                        lastException = new EventHubException(true, "timed out");
                        completeWith = null;
                    } else if (error instanceof Exception) {
                        if (error instanceof EventHubException) {
                            lastException = (EventHubException) error;
                        } else if (error instanceof AmqpException) {
                            lastException = ExceptionUtil.toException(((AmqpException) error).getError());
                        } else if (error instanceof CompletionException || error instanceof ExecutionException) {
                            lastException = ExceptionUtil.stripOuterException((Exception) error);
                        } else {
                            lastException = (Exception) error;
                        }
                        completeWith = lastException;
                    } else {
                        lastException = new Exception("got a throwable: " + error.toString());
                        completeWith = error;
                    }

                    if (ManagementRetry.this.mf.getIsClosingOrClosed()) {
                        ManagementRetry.this.finalFuture.completeExceptionally(
                                new OperationCancelledException(
                                        "OperationCancelled as the underlying client instance was closed.",
                                        lastException));
                    } else {
                        final Duration waitTime = ManagementRetry.this.mf.getRetryPolicy().getNextRetryInterval(
                                ManagementRetry.this.mf.getClientId(), lastException, this.timeoutTracker.remaining());
                        if (waitTime == null) {
                            // Do not retry again, give up and report error.
                            if (completeWith == null) {
                                ManagementRetry.this.finalFuture.complete(null);
                            } else {
                                ManagementRetry.this.finalFuture.completeExceptionally(completeWith);
                            }
                        } else {
                            // The only thing needed here is to schedule a new attempt. Even if the RequestResponseChannel has croaked,
                            // ManagementChannel uses FaultTolerantObject, so the underlying RequestResponseChannel will be recreated
                            // the next time it is needed.
                            final ManagementRetry retrier = new ManagementRetry(ManagementRetry.this.finalFuture, ManagementRetry.this.timeoutTracker,
                                    ManagementRetry.this.mf, ManagementRetry.this.request);
                            EventHubClientImpl.this.timer.schedule(retrier, waitTime);
                        }
                    }
                }
            });
        }
    }
}
