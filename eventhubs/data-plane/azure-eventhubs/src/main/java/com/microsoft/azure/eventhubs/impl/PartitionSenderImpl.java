// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.BatchOptions;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventDataBatch;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PartitionSender;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

final class PartitionSenderImpl extends ClientEntity implements PartitionSender {
    private final String partitionId;
    private final String eventHubName;
    private final MessagingFactory factory;

    private volatile MessageSender internalSender;

    private PartitionSenderImpl(final MessagingFactory factory, final String eventHubName, final String partitionId, final ScheduledExecutorService executor) {
        super(StringUtil.getRandomString("PS").concat(StringUtil.SEPARATOR + factory.getClientId()), null, executor);

        this.partitionId = partitionId;
        this.eventHubName = eventHubName;
        this.factory = factory;
    }

    static CompletableFuture<PartitionSender> create(final MessagingFactory factory,
                                                     final String eventHubName,
                                                     final String partitionId,
                                                     final ScheduledExecutorService executor) throws EventHubException {
        final PartitionSenderImpl sender = new PartitionSenderImpl(factory, eventHubName, partitionId, executor);
        return sender.createInternalSender()
                .thenApplyAsync(new Function<Void, PartitionSender>() {
                    public PartitionSender apply(Void a) {
                        return sender;
                    }
                }, executor);
    }

    private CompletableFuture<Void> createInternalSender() throws EventHubException {
        return MessageSender.create(this.factory, this.getClientId().concat("-InternalSender"),
                String.format(Locale.US, "%s/Partitions/%s", this.eventHubName, this.partitionId))
                .thenAcceptAsync(new Consumer<MessageSender>() {
                    public void accept(MessageSender a) {
                        PartitionSenderImpl.this.internalSender = a;
                    }
                }, this.executor);
    }

    public String getPartitionId() {
        return this.partitionId;
    }

    public EventDataBatch createBatch(BatchOptions options) {
        if (!StringUtil.isNullOrEmpty(options.partitionKey)) {
            throw new IllegalArgumentException("A partition key cannot be set when using PartitionSenderImpl. If you'd like to "
                    + "continue using PartitionSenderImpl with EventDataBatches, then please do not set a partition key in your BatchOptions.");
        }

        int maxSize = this.internalSender.getMaxMessageSize();

        if (options.maxMessageSize == null) {
            return new EventDataBatchImpl(maxSize, null);
        }

        if (options.maxMessageSize > maxSize) {
            throw new IllegalArgumentException("The maxMessageSize set in BatchOptions is too large. You set a maxMessageSize of "
                    + options.maxMessageSize + ". The maximum allowed size is " + maxSize + ".");
        }

        return new EventDataBatchImpl(options.maxMessageSize, null);
    }

    public CompletableFuture<Void> send(EventData data) {
        return this.internalSender.send(((EventDataImpl) data).toAmqpMessage());
    }

    public CompletableFuture<Void> send(Iterable<EventData> eventDatas) {
        if (eventDatas == null || IteratorUtil.sizeEquals(eventDatas, 0)) {
            throw new IllegalArgumentException("EventData batch cannot be empty.");
        }

        return this.internalSender.send(EventDataUtil.toAmqpMessages(eventDatas));
    }

    public CompletableFuture<Void> send(EventDataBatch eventDatas) {
        if (eventDatas == null || Integer.compare(eventDatas.getSize(), 0) == 0) {
            throw new IllegalArgumentException("EventDataBatch cannot be empty.");
        }

        if (!StringUtil.isNullOrEmpty(((EventDataBatchImpl) eventDatas).getPartitionKey())) {
            throw new IllegalArgumentException("A partition key cannot be set when using PartitionSenderImpl. If you'd like to "
                    + "continue using PartitionSenderImpl with EventDataBatches, then please do not set a partition key in your BatchOptions");
        }

        return this.internalSender.send(EventDataUtil.toAmqpMessages(((EventDataBatchImpl) eventDatas).getInternalIterable()));
    }

    @Override
    public CompletableFuture<Void> onClose() {
        if (this.internalSender == null) {
            return CompletableFuture.completedFuture(null);
        } else {
            return this.internalSender.close();
        }
    }
}
