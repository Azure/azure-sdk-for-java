/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public interface IEventHubClient {
    void sendSync(EventData data)
            throws EventHubException;

    CompletableFuture<Void> send(EventData data);

    void sendSync(Iterable<EventData> eventDatas)
            throws EventHubException;

    CompletableFuture<Void> send(Iterable<EventData> eventDatas);

    void sendSync(EventData eventData, String partitionKey)
            throws EventHubException;

    CompletableFuture<Void> send(EventData eventData, String partitionKey);

    void sendSync(Iterable<EventData> eventDatas, String partitionKey)
            throws EventHubException;

    CompletableFuture<Void> send(Iterable<EventData> eventDatas, String partitionKey);

    PartitionSender createPartitionSenderSync(String partitionId)
            throws EventHubException, IllegalArgumentException;

    CompletableFuture<PartitionSender> createPartitionSender(String partitionId)
                    throws EventHubException;

    PartitionReceiver createReceiverSync(String consumerGroupName, String partitionId, String startingOffset)
                            throws EventHubException;

    CompletableFuture<PartitionReceiver> createReceiver(String consumerGroupName, String partitionId, String startingOffset)
                                    throws EventHubException;

    PartitionReceiver createReceiverSync(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive)
                                            throws EventHubException;

    CompletableFuture<PartitionReceiver> createReceiver(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive)
                                                    throws EventHubException;

    PartitionReceiver createReceiverSync(String consumerGroupName, String partitionId, Instant dateTime)
                                                            throws EventHubException;

    CompletableFuture<PartitionReceiver> createReceiver(String consumerGroupName, String partitionId, Instant dateTime)
                                                                    throws EventHubException;

    PartitionReceiver createReceiverSync(String consumerGroupName, String partitionId, String startingOffset, ReceiverOptions receiverOptions)
                                                                            throws EventHubException;

    CompletableFuture<PartitionReceiver> createReceiver(String consumerGroupName, String partitionId, String startingOffset, ReceiverOptions receiverOptions)
                                                                                    throws EventHubException;

    PartitionReceiver createReceiverSync(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive, ReceiverOptions receiverOptions)
                                                                                            throws EventHubException;

    CompletableFuture<PartitionReceiver> createReceiver(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive, ReceiverOptions receiverOptions)
                                                                                                    throws EventHubException;

    PartitionReceiver createReceiverSync(String consumerGroupName, String partitionId, Instant dateTime, ReceiverOptions receiverOptions)
                                                                                                            throws EventHubException;

    CompletableFuture<PartitionReceiver> createReceiver(String consumerGroupName, String partitionId, Instant dateTime, ReceiverOptions receiverOptions)
                                                                                                                    throws EventHubException;

    PartitionReceiver createEpochReceiverSync(String consumerGroupName, String partitionId, String startingOffset, long epoch)
                                                                                                                            throws EventHubException;

    CompletableFuture<PartitionReceiver> createEpochReceiver(String consumerGroupName, String partitionId, String startingOffset, long epoch)
                                                                                                                                    throws EventHubException;

    PartitionReceiver createEpochReceiverSync(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive, long epoch)
                                                                                                                                            throws EventHubException;

    CompletableFuture<PartitionReceiver> createEpochReceiver(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive, long epoch)
                                                                                                                                                    throws EventHubException;

    PartitionReceiver createEpochReceiverSync(String consumerGroupName, String partitionId, Instant dateTime, long epoch)
                                                                                                                                                            throws EventHubException;

    CompletableFuture<PartitionReceiver> createEpochReceiver(String consumerGroupName, String partitionId, Instant dateTime, long epoch)
                                                                                                                                                                    throws EventHubException;

    PartitionReceiver createEpochReceiverSync(String consumerGroupName, String partitionId, String startingOffset, long epoch, ReceiverOptions receiverOptions)
                                                                                                                                                                            throws EventHubException;

    CompletableFuture<PartitionReceiver> createEpochReceiver(String consumerGroupName, String partitionId, String startingOffset, long epoch, ReceiverOptions receiverOptions)
                                                                                                                                                                                    throws EventHubException;

    PartitionReceiver createEpochReceiverSync(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive, long epoch, ReceiverOptions receiverOptions)
                                                                                                                                                                                            throws EventHubException;

    CompletableFuture<PartitionReceiver> createEpochReceiver(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive, long epoch, ReceiverOptions receiverOptions)
                                                                                                                                                                                                    throws EventHubException;

    PartitionReceiver createEpochReceiverSync(String consumerGroupName, String partitionId, Instant dateTime, long epoch, ReceiverOptions receiverOptions)
                                                                                                                                                                                                            throws EventHubException;

    CompletableFuture<PartitionReceiver> createEpochReceiver(String consumerGroupName, String partitionId, Instant dateTime, long epoch, ReceiverOptions receiverOptions)
                                                                                                                                                                                                                    throws EventHubException;

    CompletableFuture<Void> onClose();

    CompletableFuture<EventHubRuntimeInformation> getRuntimeInformation();

    CompletableFuture<EventHubPartitionRuntimeInformation> getPartitionRuntimeInformation(String partitionId);
}
