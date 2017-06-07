/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import com.microsoft.azure.servicebus.ServiceBusException;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public interface IEventHubClient {
    void sendSync(EventData data)
            throws ServiceBusException;

    CompletableFuture<Void> send(EventData data);

    void sendSync(Iterable<EventData> eventDatas)
            throws ServiceBusException;

    CompletableFuture<Void> send(Iterable<EventData> eventDatas);

    void sendSync(EventData eventData, String partitionKey)
            throws ServiceBusException;

    CompletableFuture<Void> send(EventData eventData, String partitionKey);

    void sendSync(Iterable<EventData> eventDatas, String partitionKey)
            throws ServiceBusException;

    CompletableFuture<Void> send(Iterable<EventData> eventDatas, String partitionKey);

    PartitionSender createPartitionSenderSync(String partitionId)
            throws ServiceBusException, IllegalArgumentException;

    CompletableFuture<PartitionSender> createPartitionSender(String partitionId)
                    throws ServiceBusException;

    PartitionReceiver createReceiverSync(String consumerGroupName, String partitionId, String startingOffset)
                            throws ServiceBusException;

    CompletableFuture<PartitionReceiver> createReceiver(String consumerGroupName, String partitionId, String startingOffset)
                                    throws ServiceBusException;

    PartitionReceiver createReceiverSync(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive)
                                            throws ServiceBusException;

    CompletableFuture<PartitionReceiver> createReceiver(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive)
                                                    throws ServiceBusException;

    PartitionReceiver createReceiverSync(String consumerGroupName, String partitionId, Instant dateTime)
                                                            throws ServiceBusException;

    CompletableFuture<PartitionReceiver> createReceiver(String consumerGroupName, String partitionId, Instant dateTime)
                                                                    throws ServiceBusException;

    PartitionReceiver createReceiverSync(String consumerGroupName, String partitionId, String startingOffset, ReceiverOptions receiverOptions)
                                                                            throws ServiceBusException;

    CompletableFuture<PartitionReceiver> createReceiver(String consumerGroupName, String partitionId, String startingOffset, ReceiverOptions receiverOptions)
                                                                                    throws ServiceBusException;

    PartitionReceiver createReceiverSync(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive, ReceiverOptions receiverOptions)
                                                                                            throws ServiceBusException;

    CompletableFuture<PartitionReceiver> createReceiver(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive, ReceiverOptions receiverOptions)
                                                                                                    throws ServiceBusException;

    PartitionReceiver createReceiverSync(String consumerGroupName, String partitionId, Instant dateTime, ReceiverOptions receiverOptions)
                                                                                                            throws ServiceBusException;

    CompletableFuture<PartitionReceiver> createReceiver(String consumerGroupName, String partitionId, Instant dateTime, ReceiverOptions receiverOptions)
                                                                                                                    throws ServiceBusException;

    PartitionReceiver createEpochReceiverSync(String consumerGroupName, String partitionId, String startingOffset, long epoch)
                                                                                                                            throws ServiceBusException;

    CompletableFuture<PartitionReceiver> createEpochReceiver(String consumerGroupName, String partitionId, String startingOffset, long epoch)
                                                                                                                                    throws ServiceBusException;

    PartitionReceiver createEpochReceiverSync(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive, long epoch)
                                                                                                                                            throws ServiceBusException;

    CompletableFuture<PartitionReceiver> createEpochReceiver(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive, long epoch)
                                                                                                                                                    throws ServiceBusException;

    PartitionReceiver createEpochReceiverSync(String consumerGroupName, String partitionId, Instant dateTime, long epoch)
                                                                                                                                                            throws ServiceBusException;

    CompletableFuture<PartitionReceiver> createEpochReceiver(String consumerGroupName, String partitionId, Instant dateTime, long epoch)
                                                                                                                                                                    throws ServiceBusException;

    PartitionReceiver createEpochReceiverSync(String consumerGroupName, String partitionId, String startingOffset, long epoch, ReceiverOptions receiverOptions)
                                                                                                                                                                            throws ServiceBusException;

    CompletableFuture<PartitionReceiver> createEpochReceiver(String consumerGroupName, String partitionId, String startingOffset, long epoch, ReceiverOptions receiverOptions)
                                                                                                                                                                                    throws ServiceBusException;

    PartitionReceiver createEpochReceiverSync(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive, long epoch, ReceiverOptions receiverOptions)
                                                                                                                                                                                            throws ServiceBusException;

    CompletableFuture<PartitionReceiver> createEpochReceiver(String consumerGroupName, String partitionId, String startingOffset, boolean offsetInclusive, long epoch, ReceiverOptions receiverOptions)
                                                                                                                                                                                                    throws ServiceBusException;

    PartitionReceiver createEpochReceiverSync(String consumerGroupName, String partitionId, Instant dateTime, long epoch, ReceiverOptions receiverOptions)
                                                                                                                                                                                                            throws ServiceBusException;

    CompletableFuture<PartitionReceiver> createEpochReceiver(String consumerGroupName, String partitionId, Instant dateTime, long epoch, ReceiverOptions receiverOptions)
                                                                                                                                                                                                                    throws ServiceBusException;

    CompletableFuture<Void> onClose();

    CompletableFuture<EventHubRuntimeInformation> getRuntimeInformation();

    CompletableFuture<EventHubPartitionRuntimeInformation> getPartitionRuntimeInformation(String partitionId);
}
