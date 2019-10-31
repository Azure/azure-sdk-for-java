// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.ReceiverOptions;
import com.microsoft.azure.eventhubs.ReceiverRuntimeInformation;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnknownDescribedType;
import org.apache.qpid.proton.amqp.messaging.DeliveryAnnotations;
import org.apache.qpid.proton.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

final class PartitionReceiverImpl extends ClientEntity implements ReceiverSettingsProvider, PartitionReceiver {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(PartitionReceiverImpl.class);

    private final String partitionId;
    private final MessagingFactory underlyingFactory;
    private final String eventHubName;
    private final String consumerGroupName;
    private final Object receiveHandlerLock;
    private final EventPositionImpl eventPosition;
    private final Long epoch;
    private final boolean isEpochReceiver;
    private final ReceiverOptions receiverOptions;
    private final ReceiverRuntimeInformation runtimeInformation;

    private volatile MessageReceiver internalReceiver;

    private ReceivePump receivePump;
    private EventPosition currentEventPosition;

    private PartitionReceiverImpl(MessagingFactory factory,
                                  final String eventHubName,
                                  final String consumerGroupName,
                                  final String partitionId,
                                  final EventPositionImpl eventPosition,
                                  final Long epoch,
                                  final boolean isEpochReceiver,
                                  final ReceiverOptions receiverOptions,
                                  final ScheduledExecutorService executor) {
        super(StringUtil.getRandomString("PR").concat(StringUtil.SEPARATOR + factory.getClientId()), null, executor);

        this.underlyingFactory = factory;
        this.eventHubName = eventHubName;
        this.consumerGroupName = consumerGroupName;
        this.partitionId = partitionId;
        this.eventPosition = eventPosition;
        this.epoch = epoch;
        this.isEpochReceiver = isEpochReceiver;
        this.receiveHandlerLock = new Object();
        this.receiverOptions = receiverOptions;
        this.runtimeInformation = (this.receiverOptions != null && this.receiverOptions.getReceiverRuntimeMetricEnabled())
                ? new ReceiverRuntimeInformation(partitionId)
                : null;
        this.currentEventPosition = EventPosition.fromStartOfStream();
    }

    static CompletableFuture<PartitionReceiver> create(MessagingFactory factory,
                                                       final String eventHubName,
                                                       final String consumerGroupName,
                                                       final String partitionId,
                                                       final EventPosition eventPosition,
                                                       final long epoch,
                                                       final boolean isEpochReceiver,
                                                       ReceiverOptions receiverOptions,
                                                       final ScheduledExecutorService executor) {
        if (epoch < NULL_EPOCH) {
            throw new IllegalArgumentException("epoch cannot be a negative value. Please specify a zero or positive long value.");
        }

        if (StringUtil.isNullOrWhiteSpace(consumerGroupName)) {
            throw new IllegalArgumentException("specify valid string for argument - 'consumerGroupName'");
        }

        if (receiverOptions == null) {
            receiverOptions = new ReceiverOptions();
        }

        final PartitionReceiverImpl receiver = new PartitionReceiverImpl(factory, eventHubName, consumerGroupName, partitionId, (EventPositionImpl) eventPosition, epoch, isEpochReceiver, receiverOptions, executor);
        return receiver.createInternalReceiver().thenApplyAsync(new Function<Void, PartitionReceiver>() {
            public PartitionReceiver apply(Void a) {
                return receiver;
            }
        }, executor);
    }

    private CompletableFuture<Void> createInternalReceiver() {
        return MessageReceiver.create(this.underlyingFactory,
                this.getClientId().concat("-InternalReceiver"),
                String.format(Locale.US, "%s/ConsumerGroups/%s/Partitions/%s", this.eventHubName, this.consumerGroupName, this.partitionId),
                this.receiverOptions.getPrefetchCount(), this)
                .thenAcceptAsync(new Consumer<MessageReceiver>() {
                    public void accept(MessageReceiver r) {
                        PartitionReceiverImpl.this.internalReceiver = r;
                    }
                }, this.executor);
    }

    EventPosition getStartingPosition() {
        return this.eventPosition;
    }

    public String getPartitionId() {
        return this.partitionId;
    }

    public Duration getReceiveTimeout() {
        return this.internalReceiver.getReceiveTimeout();
    }

    public void setReceiveTimeout(Duration value) {
        this.internalReceiver.setReceiveTimeout(value);
    }

    public boolean getIsOpen() {
        return !this.getIsClosingOrClosed();
    }

    public long getEpoch() {
        return this.epoch;
    }

    public ReceiverRuntimeInformation getRuntimeInformation() {
        return this.runtimeInformation;
    }

    public EventPosition getEventPosition() {
        return this.currentEventPosition;
    }

    public CompletableFuture<Iterable<EventData>> receive(final int maxEventCount) {
        return this.internalReceiver.receive(maxEventCount).thenApplyAsync(new Function<Collection<Message>, Iterable<EventData>>() {
            @Override
            public Iterable<EventData> apply(Collection<Message> amqpMessages) {
                PassByRef<MessageWrapper> lastMessageRef = null;
                if (PartitionReceiverImpl.this.receiverOptions != null && PartitionReceiverImpl.this.receiverOptions.getReceiverRuntimeMetricEnabled()) {
                    lastMessageRef = new PassByRef<>();
                }

                final Iterable<EventData> events = EventDataUtil.toEventDataCollection(amqpMessages, lastMessageRef);

                if (lastMessageRef != null && lastMessageRef.get() != null) {
                    final DeliveryAnnotations deliveryAnnotations = lastMessageRef.get().getMessage().getDeliveryAnnotations();
                    if (deliveryAnnotations != null && deliveryAnnotations.getValue() != null) {
                        final Map<Symbol, Object> deliveryAnnotationsMap = deliveryAnnotations.getValue();
                        PartitionReceiverImpl.this.runtimeInformation.setRuntimeInformation(
                                (long) deliveryAnnotationsMap.get(ClientConstants.LAST_ENQUEUED_SEQUENCE_NUMBER),
                                ((Date) deliveryAnnotationsMap.get(ClientConstants.LAST_ENQUEUED_TIME_UTC)).toInstant(),
                                (String) deliveryAnnotationsMap.get(ClientConstants.LAST_ENQUEUED_OFFSET));
                    }

                    PartitionReceiverImpl.this.currentEventPosition = lastMessageRef.get().getEventPosition();
                }

                return events;
            }
        }, this.executor);
    }

    public CompletableFuture<Void> setReceiveHandler(final PartitionReceiveHandler receiveHandler) {
        return this.setReceiveHandler(receiveHandler, false);
    }

    public CompletableFuture<Void> setReceiveHandler(final PartitionReceiveHandler receiveHandler, final boolean invokeWhenNoEvents) {
        synchronized (this.receiveHandlerLock) {
            // user setting receiveHandler==null should stop the pump if its running
            if (receiveHandler == null) {
                if (this.receivePump != null && this.receivePump.isRunning()) {
                    return this.receivePump.stop();
                }
            } else {
                if (this.receivePump != null && this.receivePump.isRunning()) {
                    throw new IllegalArgumentException(
                        "Unexpected value for parameter 'receiveHandler'. PartitionReceiver was already registered with a PartitionReceiveHandler instance. Only 1 instance can be registered.");
                }
                this.receivePump = new ReceivePump(
                        this.eventHubName,
                        this.consumerGroupName,
                        new ReceivePump.IPartitionReceiver() {
                            @Override
                            public CompletableFuture<Iterable<EventData>> receive(int maxBatchSize) {
                                return PartitionReceiverImpl.this.receive(maxBatchSize);
                            }

                            @Override
                            public String getPartitionId() {
                                return PartitionReceiverImpl.this.getPartitionId();
                            }
                        },
                        receiveHandler,
                        invokeWhenNoEvents,
                        this.executor);

                this.executor.execute(this.receivePump);
            }

            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> onClose() {
        synchronized (this.receiveHandlerLock) {
            if (this.receivePump != null && this.receivePump.isRunning()) {
                // set the state of receivePump to StopEventRaised
                // - but don't actually wait until the current user-code completes
                // if user intends to stop everything - setReceiveHandler(null) should be invoked before close
                this.receivePump.stop();
            }
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
            TRACE_LOGGER.info(String.format(Locale.US, "%s, action[createReceiveLink], %s", logReceivePath, this.eventPosition));
        }

        return Collections.singletonMap(AmqpConstants.STRING_FILTER, new UnknownDescribedType(AmqpConstants.STRING_FILTER, expression));
    }

    @Override
    public Map<Symbol, Object> getProperties() {

        if (!this.isEpochReceiver
                &&  (this.receiverOptions == null || this.receiverOptions.getIdentifier() == null)) {
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
