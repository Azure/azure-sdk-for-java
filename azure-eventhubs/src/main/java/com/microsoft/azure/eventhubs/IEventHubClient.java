/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.util.concurrent.CompletableFuture;

public interface IEventHubClient {

    void sendSync(EventData data) throws EventHubException;

    CompletableFuture<Void> send(EventData data);

    void sendSync(Iterable<EventData> eventDatas) throws EventHubException;

    CompletableFuture<Void> send(Iterable<EventData> eventDatas);

    void sendSync(EventDataBatch eventDatas) throws EventHubException;

    CompletableFuture<Void> send(EventDataBatch eventDatas);

    void sendSync(EventData eventData, String partitionKey) throws EventHubException;

    CompletableFuture<Void> send(EventData eventData, String partitionKey);

    void sendSync(Iterable<EventData> eventDatas, String partitionKey) throws EventHubException;

    CompletableFuture<Void> send(Iterable<EventData> eventDatas, String partitionKey);

    PartitionSender createPartitionSenderSync(String partitionId) throws EventHubException, IllegalArgumentException;

    CompletableFuture<PartitionSender> createPartitionSender(String partitionId) throws EventHubException;

    PartitionReceiver createReceiverSync(final String consumerGroupName, final String partitionId, final EventPosition eventPosition)
            throws EventHubException;

    CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final EventPosition eventPosition)
            throws EventHubException;

    PartitionReceiver createReceiverSync(final String consumerGroupName, final String partitionId, final EventPosition eventPosition, final ReceiverOptions receiverOptions)
            throws EventHubException;

    CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final EventPosition eventPosition, final ReceiverOptions receiverOptions)
            throws EventHubException;

    PartitionReceiver createEpochReceiverSync(final String consumerGroupName, final String partitionId, final EventPosition eventPosition, final long epoch)
            throws EventHubException;

    CompletableFuture<PartitionReceiver> createEpochReceiver(final String consumerGroupName, final String partitionId, final EventPosition eventPosition, final long epoch)
            throws EventHubException;

    PartitionReceiver createEpochReceiverSync(final String consumerGroupName, final String partitionId, final EventPosition eventPosition, final long epoch, final ReceiverOptions receiverOptions)
            throws EventHubException;

    CompletableFuture<PartitionReceiver> createEpochReceiver(final String consumerGroupName, final String partitionId, final EventPosition eventPosition, final long epoch, final ReceiverOptions receiverOptions)
            throws EventHubException;

    CompletableFuture<Void> onClose();

    CompletableFuture<EventHubRuntimeInformation> getRuntimeInformation();

    CompletableFuture<EventHubPartitionRuntimeInformation> getPartitionRuntimeInformation(String partitionId);
}
