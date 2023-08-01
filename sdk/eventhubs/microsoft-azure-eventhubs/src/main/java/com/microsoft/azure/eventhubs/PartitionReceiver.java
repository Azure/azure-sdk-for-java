// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import com.microsoft.azure.eventhubs.impl.ExceptionUtil;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

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
public interface PartitionReceiver {

    int MINIMUM_PREFETCH_COUNT = 1;
    int DEFAULT_PREFETCH_COUNT = 500;
    int MAXIMUM_PREFETCH_COUNT = 8000;

    long NULL_EPOCH = 0;

    /**
     * Get EventHubs partition identifier.
     *
     * @return The identifier representing the partition from which this receiver is fetching data
     */
    String getPartitionId();

    Duration getReceiveTimeout();

    void setReceiveTimeout(Duration value);

    /**
     * Determine the current state of the receiver.
     *
     * @return false if the receiver is closing or has been closed, true if the receiver is open and ready to use.
     */
    boolean getIsOpen();

    /**
     * Get the epoch value that this receiver is currently using for partition ownership.
     * <p>
     * A value of 0 means this receiver is not an epoch-based receiver.
     *
     * @return the epoch value that this receiver is currently using for partition ownership.
     */
    long getEpoch();

    /**
     * Gets the temporal {@link ReceiverRuntimeInformation} for this EventHub partition.
     * In general, this information is a representation of, where this {@link PartitionReceiver}'s end of stream is,
     * at the time {@link ReceiverRuntimeInformation#getRetrievalTime()}.
     * <p> This value will not be populated, unless the knob {@link ReceiverOptions#setReceiverRuntimeMetricEnabled(boolean)} is set.
     * This value will be refreshed every time an {@link EventData} is consumed from {@link PartitionReceiver}.
     * For ex: if no events have been consumed, then this value is not populated.
     *
     * @return receiver runtime information
     */
    ReceiverRuntimeInformation getRuntimeInformation();

    /**
     * Get the {@link EventPosition} that corresponds to an {@link EventData} which was returned last by the receiver.
     * <p> This value will not be populated, unless the knob {@link ReceiverOptions#setReceiverRuntimeMetricEnabled(boolean)} is set.
     * Note that EventPosition object is initialized using SequenceNumber and other parameters are not set and get will return null.
     *
     * @return the EventPosition object.
     */
    EventPosition getEventPosition();

    /**
     * Synchronous version of {@link #receive}.
     *
     * @param maxEventCount maximum number of {@link EventData}'s that this call should return
     * @return Batch of {@link EventData}'s from the partition on which this receiver is created. Returns 'null' if no {@link EventData} is present.
     * @throws EventHubException if ServiceBus client encountered any unrecoverable/non-transient problems during {@link #receive}
     */
    default Iterable<EventData> receiveSync(final int maxEventCount) throws EventHubException {
        return ExceptionUtil.sync(() -> this.receive(maxEventCount).get());
    }

    /**
     * Receive a batch of {@link EventData}'s from an EventHub partition
     * <p>
     * Sample code (sample uses sync version of the api but concept are identical):
     * <pre>
     * EventHubClient client = EventHubClient.createSync("__connection__");
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
    CompletableFuture<Iterable<EventData>> receive(int maxEventCount);

    /**
     * Register a receive handler that will be called when an event is available. A
     * {@link PartitionReceiveHandler} is a handler that allows user to specify a callback
     * for event processing and error handling in a receive pump model.
     *
     * @param receiveHandler An implementation of {@link PartitionReceiveHandler}. Setting this handler to <code>null</code> will stop the receive pump.
     * @return A completableFuture which sets receiveHandler
     */
    CompletableFuture<Void> setReceiveHandler(PartitionReceiveHandler receiveHandler);

    /**
     * Register a receive handler that will be called when an event is available. A
     * {@link PartitionReceiveHandler} is a handler that allows user to specify a callback
     * for event processing and error handling in a receive pump model.
     *
     * @param receiveHandler     An implementation of {@link PartitionReceiveHandler}
     * @param invokeWhenNoEvents flag to indicate whether the {@link PartitionReceiveHandler#onReceive(Iterable)} should be invoked when the receive call times out
     * @return A completableFuture which sets receiveHandler
     */
    CompletableFuture<Void> setReceiveHandler(PartitionReceiveHandler receiveHandler, boolean invokeWhenNoEvents);

    CompletableFuture<Void> close();

    void closeSync() throws EventHubException;
}
