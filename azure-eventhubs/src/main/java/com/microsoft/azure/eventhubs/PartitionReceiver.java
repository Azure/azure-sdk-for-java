/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.qpid.proton.amqp.messaging.DeliveryAnnotations;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnknownDescribedType;
import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.eventhubs.amqp.AmqpConstants;

/**
 * This is a logical representation of receiving from a EventHub partition.
 * <p>
 * A {@link PartitionReceiver} is tied to a ConsumerGroup + EventHub Partition combination.
 * <ul>
 * <li>If an epoch based {@link PartitionReceiver} (i.e., PartitionReceiver.getEpoch != 0) is created, EventHubs service will guarantee only 1 active receiver exists per ConsumerGroup + Partition combo.
 * This is the recommended approach to create a {@link PartitionReceiver}.
 * <li>Multiple receivers per ConsumerGroup + Partition combo can be created using non-epoch receivers.
 * </ul>
 *
 * @see EventHubClient#createReceiver
 * @see EventHubClient#createEpochReceiver
 */
public final class PartitionReceiver extends ClientEntity implements IReceiverSettingsProvider {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(PartitionReceiver.class);
    public static final int MINIMUM_PREFETCH_COUNT = 10;

    public static final int DEFAULT_PREFETCH_COUNT = 999;
    static final long NULL_EPOCH = 0;


    // Both constants should be removed before 1.0.0 release
    public static String START_OF_STREAM = "-1";
    public static String END_OF_STREAM = "@latest";

    private final String partitionId;
    private final MessagingFactory underlyingFactory;
    private final String eventHubName;
    private final String consumerGroupName;
    private final Object receiveHandlerLock;

    private EventPosition eventPosition;
    private MessageReceiver internalReceiver;
    private Long epoch;
    private boolean isEpochReceiver;
    private ReceivePump receivePump;
    private ReceiverOptions receiverOptions;
    private ReceiverRuntimeInformation runtimeInformation;

    private PartitionReceiver(MessagingFactory factory,
                              final String eventHubName,
                              final String consumerGroupName,
                              final String partitionId,
                              final EventPosition eventPosition,
                              final Long epoch,
                              final boolean isEpochReceiver,
                              final ReceiverOptions receiverOptions,
                              final Executor executor)
            throws EventHubException {
        super(null, null, executor);

        this.underlyingFactory = factory;
        this.eventHubName = eventHubName;
        this.consumerGroupName = consumerGroupName;
        this.partitionId = partitionId;
        this.eventPosition = eventPosition;
        this.epoch = epoch;
        this.isEpochReceiver = isEpochReceiver;
        this.receiveHandlerLock = new Object();
        this.receiverOptions = receiverOptions;

        if (this.receiverOptions != null && this.receiverOptions.getReceiverRuntimeMetricEnabled())
            this.runtimeInformation = new ReceiverRuntimeInformation(partitionId);
    }

    static CompletableFuture<PartitionReceiver> create(MessagingFactory factory,
                                                       final String eventHubName,
                                                       final String consumerGroupName,
                                                       final String partitionId,
                                                       final EventPosition eventPosition,
                                                       final long epoch,
                                                       final boolean isEpochReceiver,
                                                       final ReceiverOptions receiverOptions,
                                                       final Executor executor)
            throws EventHubException {
        if (epoch < NULL_EPOCH) {
            throw new IllegalArgumentException("epoch cannot be a negative value. Please specify a zero or positive long value.");
        }

        if (StringUtil.isNullOrWhiteSpace(consumerGroupName)) {
            throw new IllegalArgumentException("specify valid string for argument - 'consumerGroupName'");
        }

        final PartitionReceiver receiver = new PartitionReceiver(factory, eventHubName, consumerGroupName, partitionId, eventPosition, epoch, isEpochReceiver, receiverOptions, executor);
        return receiver.createInternalReceiver().thenApplyAsync(new Function<Void, PartitionReceiver>() {
            public PartitionReceiver apply(Void a) {
                return receiver;
            }
        }, executor);
    }

    private CompletableFuture<Void> createInternalReceiver() throws EventHubException {
        return MessageReceiver.create(this.underlyingFactory,
                StringUtil.getRandomString(),
                String.format("%s/ConsumerGroups/%s/Partitions/%s", this.eventHubName, this.consumerGroupName, this.partitionId),
                PartitionReceiver.DEFAULT_PREFETCH_COUNT, this)
                .thenAcceptAsync(new Consumer<MessageReceiver>() {
                    public void accept(MessageReceiver r) {
                        PartitionReceiver.this.internalReceiver = r;
                    }
                }, this.executor);
    }

    /**
     * @return The Cursor from which this Receiver started receiving from
     */
    final EventPosition getStartingPosition() {
        return this.eventPosition;
    }

    /**
     * Get EventHubs partition identifier.
     *
     * @return The identifier representing the partition from which this receiver is fetching data
     */
    public final String getPartitionId() {
        return this.partitionId;
    }

    /**
     * Get Prefetch Count configured on the Receiver.
     *
     * @return the upper limit of events this receiver will actively receive regardless of whether a receive operation is pending.
     * @see #setPrefetchCount
     */
    public final int getPrefetchCount() {
        return this.internalReceiver.getPrefetchCount();
    }

    public final Duration getReceiveTimeout() {
        return this.internalReceiver.getReceiveTimeout();
    }

    public void setReceiveTimeout(Duration value) {
        this.internalReceiver.setReceiveTimeout(value);
    }

    /**
     * Set the number of events that can be pre-fetched and cached at the {@link PartitionReceiver}.
     * <p>By default the value is 300
     *
     * @param prefetchCount the number of events to pre-fetch. value must be between 10 and 999. Default is 300.
     * @throws EventHubException if setting prefetchCount encounters error
     */
    public final void setPrefetchCount(final int prefetchCount) throws EventHubException {
        if (prefetchCount < PartitionReceiver.MINIMUM_PREFETCH_COUNT) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "PrefetchCount has to be above %s", PartitionReceiver.MINIMUM_PREFETCH_COUNT));
        }

        this.internalReceiver.setPrefetchCount(prefetchCount);
    }

    /**
     * Get the epoch value that this receiver is currently using for partition ownership.
     * <p>
     * A value of 0 means this receiver is not an epoch-based receiver.
     *
     * @return the epoch value that this receiver is currently using for partition ownership.
     */
    public final long getEpoch() {
        return this.epoch;
    }

    /**
     * Gets the temporal {@link ReceiverRuntimeInformation} for this EventHub partition.
     * In general, this information is a representation of, where this {@link PartitionReceiver}'s end of stream is,
     * at the time {@link ReceiverRuntimeInformation#getRetrievalTime()}.
     *
     * @return receiver runtime information
     */
    public final ReceiverRuntimeInformation getRuntimeInformation() {

        return this.runtimeInformation;
    }

    /**
     * Synchronous version of {@link #receive}.
     *
     * @param maxEventCount maximum number of {@link EventData}'s that this call should return
     * @return Batch of {@link EventData}'s from the partition on which this receiver is created. Returns 'null' if no {@link EventData} is present.
     * @throws EventHubException if ServiceBus client encountered any unrecoverable/non-transient problems during {@link #receive}
     */
    public final Iterable<EventData> receiveSync(final int maxEventCount)
            throws EventHubException {
        try {
            return this.receive(maxEventCount).get();
        } catch (InterruptedException | ExecutionException exception) {
            if (exception instanceof InterruptedException) {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }

            Throwable throwable = exception.getCause();
            if (throwable != null) {
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                }

                if (throwable instanceof EventHubException) {
                    throw (EventHubException) throwable;
                }

                throw new EventHubException(true, throwable);
            }
        }

        return null;
    }

    /**
     * Receive a batch of {@link EventData}'s from an EventHub partition
     * <p>
     * Sample code (sample uses sync version of the api but concept are identical):
     * <pre>
     * EventHubClient client = EventHubClient.createFromConnectionStringSync("__connection__");
     * PartitionReceiver receiver = client.createPartitionReceiverSync("ConsumerGroup1", "1");
     * Iterable{@literal<}EventData{@literal>} receivedEvents = receiver.receiveSync();
     *
     * while (true)
     * {
     *     int batchSize = 0;
     *     if (receivedEvents != null)
     *     {
     *         for(EventData receivedEvent: receivedEvents)
     *         {
     *             System.out.println(String.format("Message Payload: %s", new String(receivedEvent.getBytes(), Charset.defaultCharset())));
     *             System.out.println(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s",
     *                 receivedEvent.getSystemProperties().getOffset(),
     *                 receivedEvent.getSystemProperties().getSequenceNumber(),
     *                 receivedEvent.getSystemProperties().getEnqueuedTime()));
     *             batchSize++;
     *         }
     *     }
     *
     *     System.out.println(String.format("ReceivedBatch Size: %s", batchSize));
     *     receivedEvents = receiver.receiveSync();
     * }
     * </pre>
     *
     * @param maxEventCount maximum number of {@link EventData}'s that this call should return
     * @return A completableFuture that will yield a batch of {@link EventData}'s from the partition on which this receiver is created. Returns 'null' if no {@link EventData} is present.
     */
    public CompletableFuture<Iterable<EventData>> receive(final int maxEventCount) {
        return this.internalReceiver.receive(maxEventCount).thenApplyAsync(new Function<Collection<Message>, Iterable<EventData>>() {
            @Override
            public Iterable<EventData> apply(Collection<Message> amqpMessages) {
                PassByRef<Message> lastMessageRef = null;
                if (PartitionReceiver.this.receiverOptions != null && PartitionReceiver.this.receiverOptions.getReceiverRuntimeMetricEnabled())
                    lastMessageRef = new PassByRef<>();

                final Iterable<EventData> events = EventDataUtil.toEventDataCollection(amqpMessages, lastMessageRef);

                if (lastMessageRef != null && lastMessageRef.get() != null) {

                    DeliveryAnnotations deliveryAnnotations = lastMessageRef.get().getDeliveryAnnotations();
                    if (deliveryAnnotations != null && deliveryAnnotations.getValue() != null) {

                        Map<Symbol, Object> deliveryAnnotationsMap = deliveryAnnotations.getValue();
                        PartitionReceiver.this.runtimeInformation.setRuntimeInformation(
                                (long) deliveryAnnotationsMap.get(ClientConstants.LAST_ENQUEUED_SEQUENCE_NUMBER),
                                ((Date) deliveryAnnotationsMap.get(ClientConstants.LAST_ENQUEUED_TIME_UTC)).toInstant(),
                                (String) deliveryAnnotationsMap.get(ClientConstants.LAST_ENQUEUED_OFFSET));
                    }
                }

                return events;
            }
        }, this.executor);
    }

    /**
     * Register a receive handler that will be called when an event is available. A
     * {@link PartitionReceiveHandler} is a handler that allows user to specify a callback
     * for event processing and error handling in a receive pump model.
     *
     * @param receiveHandler An implementation of {@link PartitionReceiveHandler}. Setting this handler to <code>null</code> will stop the receive pump.
     * @return A completableFuture which sets receiveHandler
     */
    public CompletableFuture<Void> setReceiveHandler(final PartitionReceiveHandler receiveHandler) {
        return this.setReceiveHandler(receiveHandler, false);
    }

    /**
     * Register a receive handler that will be called when an event is available. A
     * {@link PartitionReceiveHandler} is a handler that allows user to specify a callback
     * for event processing and error handling in a receive pump model.
     *
     * @param receiveHandler     An implementation of {@link PartitionReceiveHandler}
     * @param invokeWhenNoEvents flag to indicate whether the {@link PartitionReceiveHandler#onReceive(Iterable)} should be invoked when the receive call times out
     * @return A completableFuture which sets receiveHandler
     */
    public CompletableFuture<Void> setReceiveHandler(final PartitionReceiveHandler receiveHandler, final boolean invokeWhenNoEvents) {
        synchronized (this.receiveHandlerLock) {
            // user setting receiveHandler==null should stop the pump if its running
            if (receiveHandler == null) {
                if (this.receivePump != null && this.receivePump.isRunning()) {
                    return this.receivePump.stop();
                }
            } else {
                if (this.receivePump != null && this.receivePump.isRunning())
                    throw new IllegalArgumentException(
                            "Unexpected value for parameter 'receiveHandler'. PartitionReceiver was already registered with a PartitionReceiveHandler instance. Only 1 instance can be registered.");

                this.receivePump = new ReceivePump(
                        new ReceivePump.IPartitionReceiver() {
                            @Override
                            public Iterable<EventData> receive(int maxBatchSize) throws EventHubException {
                                return PartitionReceiver.this.receiveSync(maxBatchSize);
                            }

                            @Override
                            public String getPartitionId() {
                                return PartitionReceiver.this.getPartitionId();
                            }
                        },
                        receiveHandler,
                        invokeWhenNoEvents);

                final Thread onReceivePumpThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        receivePump.run();
                    }
                });

                onReceivePumpThread.start();
            }

            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> onClose() {
        if (this.receivePump != null && this.receivePump.isRunning()) {
            // set the state of receivePump to StopEventRaised
            // - but don't actually wait until the current user-code completes
            // if user intends to stop everything - setReceiveHandler(null) should be invoked before close
            this.receivePump.stop();
        }

        if (this.internalReceiver != null) {
            return this.internalReceiver.close();
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }


    @Override
    public Map<Symbol, UnknownDescribedType> getFilter(final Message lastReceivedMessage) {
        String expression;
        if (lastReceivedMessage != null) {
            String lastReceivedOffset = lastReceivedMessage.getMessageAnnotations().getValue().get(AmqpConstants.OFFSET).toString();
            expression = String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.OFFSET_ANNOTATION_NAME, StringUtil.EMPTY, lastReceivedOffset);
        } else {
            expression = this.eventPosition.getExpression();
        }

        if (TRACE_LOGGER.isInfoEnabled()) {
            String logReceivePath = "";
            if (this.internalReceiver == null) {
                // During startup, internalReceiver is still null. Need to handle this special case when logging during startup
                // or the reactor thread crashes with NPE when calling internalReceiver.getReceivePath() and no receiving occurs.
                logReceivePath = "receiverPath[RECEIVER IS NULL]";
            } else {
                logReceivePath = "receiverPath[" + this.internalReceiver.getReceivePath() + "]";
            }
            TRACE_LOGGER.info(String.format("%s, action[createReceiveLink], %s", logReceivePath, this.eventPosition));
        }

        return Collections.singletonMap(AmqpConstants.STRING_FILTER, new UnknownDescribedType(AmqpConstants.STRING_FILTER, expression));
    }

    @Override
    public Map<Symbol, Object> getProperties() {

        if (!this.isEpochReceiver &&
                (this.receiverOptions == null || this.receiverOptions.getIdentifier() == null)) {
            return null;
        }

        final Map<Symbol, Object> properties = new HashMap<>();

        if (this.isEpochReceiver) {
            properties.put(AmqpConstants.EPOCH, (Object) this.epoch);
        }

        if (this.receiverOptions != null && this.receiverOptions.getIdentifier() != null) {
            properties.put(AmqpConstants.RECEIVER_IDENTIFIER_NAME, (Object) this.receiverOptions.getIdentifier());
        }

        return properties;
    }

    @Override
    public Symbol[] getDesiredCapabilities() {

        return this.receiverOptions != null && this.receiverOptions.getReceiverRuntimeMetricEnabled()
                ? new Symbol[]{AmqpConstants.ENABLE_RECEIVER_RUNTIME_METRIC_NAME}
                : null;
    }
}