// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import com.microsoft.azure.eventhubs.impl.ExceptionUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

/**
 * This sender class is a logical representation of sending events to a specific EventHub partition. Do not use this class
 * if you do not care about sending events to specific partitions. Instead, use {@link EventHubClient#send} method.
 *
 * @see EventHubClient#createPartitionSender(String)
 * @see EventHubClient#create(String, ScheduledExecutorService)
 */
public interface PartitionSender {

    /**
     * The partition id that will receive events from this sender.
     *
     * @return the partition id the PartitionSender is connected to.
     */
    String getPartitionId();

    /**
     * Creates an Empty Collection of {@link EventData}.
     * The same partitionKey must be used while sending these events using {@link PartitionSender#send(EventDataBatch)}.
     *
     * @param options see {@link BatchOptions} for more usage details
     * @return the empty {@link EventDataBatch}, after negotiating maximum message size with EventHubs service
     */
    EventDataBatch createBatch(BatchOptions options);

    /**
     * Creates an Empty Collection of {@link EventData}.
     * The same partitionKey must be used while sending these events using {@link PartitionSender#send(EventDataBatch)}.
     *
     * @return the empty {@link EventDataBatch}, after negotiating maximum message size with EventHubs service
     */
    default EventDataBatch createBatch() {
        return this.createBatch(new BatchOptions());
    }

    /**
     * Synchronous version of {@link #send(EventData)} Api.
     *
     * @param data the {@link EventData} to be sent.
     * @throws PayloadSizeExceededException if the total size of the {@link EventData} exceeds a pre-defined limit set by the service. Default is 256k bytes.
     * @throws EventHubException            if Service Bus service encountered problems during the operation.
     */
    default void sendSync(final EventData data) throws EventHubException {
        ExceptionUtil.syncVoid(() -> this.send(data).get());
    }

    /**
     * Send {@link EventData} to a specific EventHub partition. The target partition is pre-determined when this PartitionSender was created.
     * This send pattern emphasize data correlation over general availability and latency.
     * <p>
     * There are 3 ways to send to EventHubs, each exposed as a method (along with its sendBatch overload):
     * <pre>
     * i.   {@link EventHubClient#send(EventData)}, {@link EventHubClient#send(Iterable)}, {@link EventHubClient#send(EventDataBatch)}
     * ii.  {@link EventHubClient#send(EventData, String)} or {@link EventHubClient#send(Iterable, String)}
     * iii. {@link PartitionSender#send(EventData)}, {@link PartitionSender#send(Iterable)}, or {@link PartitionSender#send(EventDataBatch)}
     * </pre>
     * <p>
     * Use this type of Send, if:
     * <pre>
     * i. The client wants to take direct control of distribution of data across partitions. In this case client is responsible for making sure there is at least one sender per event hub partition.
     * ii. User cannot use partition key as a mean to direct events to specific partition, yet there is a need for data correlation with partitioning scheme.
     * </pre>
     *
     * @param data the {@link EventData} to be sent.
     * @return a CompletableFuture that can be completed when the send operations is done..
     */
    CompletableFuture<Void> send(EventData data);

    /**
     * Synchronous version of {@link #send(Iterable)} .
     *
     * @param eventDatas batch of events to send to EventHub
     * @throws EventHubException if Service Bus service encountered problems during the operation.
     */
    default void sendSync(final Iterable<EventData> eventDatas) throws EventHubException {
        ExceptionUtil.syncVoid(() -> this.send(eventDatas).get());
    }

    /**
     * Send {@link EventData} to a specific EventHub partition. The targeted partition is pre-determined when this PartitionSender was created.
     * <p>
     * There are 3 ways to send to EventHubs, to understand this particular type of Send refer to the overload {@link #send(EventData)}, which is the same type of Send and is used to send single {@link EventData}.
     * <p>
     * Sending a batch of {@link EventData}'s is useful in the following cases:
     * <pre>
     * i.   Efficient send - sending a batch of {@link EventData} maximizes the overall throughput by optimally using the number of sessions created to EventHubs' service.
     * ii.  Send multiple {@link EventData}'s in a Transaction. To achieve ACID properties, the Gateway Service will forward all {@link EventData}'s in the batch to a single EventHubs' partition.
     * </pre>
     * <p>
     * Sample code (sample uses sync version of the api but concept are identical):
     * <pre>
     * Gson gson = new GsonBuilder().create();
     * EventHubClient client = EventHubClient.createSync("__connection__");
     * PartitionSender senderToPartitionOne = client.createPartitionSenderSync("1");
     *
     * while (true)
     * {
     *     LinkedList{@literal<}EventData{@literal>} events = new LinkedList{@literal<}EventData{@literal>}();
     *     for (int count = 1; count {@literal<} 11; count++)
     *     {
     *         PayloadEvent payload = new PayloadEvent(count);
     *         byte[] payloadBytes = gson.toJson(payload).getBytes(Charset.defaultCharset());
     *         EventData sendEvent = EventData.create(payloadBytes);
     *         sendEvent.getProperties().put("from", "javaClient");
     *         events.add(sendEvent);
     *     }
     *
     *     senderToPartitionOne.sendSync(events);
     *     System.out.println(String.format("Sent Batch... Size: %s", events.size()));
     * }
     * </pre>
     *
     * @param eventDatas batch of events to send to EventHub
     * @return a CompletableFuture that can be completed when the send operations is done..
     */
    CompletableFuture<Void> send(Iterable<EventData> eventDatas);

    /**
     * Synchronous version of {@link #send(EventDataBatch)}
     *
     * @param eventDatas EventDataBatch to send to EventHub
     * @throws EventHubException if Service Bus service encountered problems during the operation.
     */
    default void sendSync(final EventDataBatch eventDatas) throws EventHubException {
        ExceptionUtil.syncVoid(() -> this.send(eventDatas).get());
    }

    /**
     * Send {@link EventDataBatch} to a specific EventHub partition. The targeted partition is pre-determined when this PartitionSender was created.
     * A partitionKey cannot be set when using EventDataBatch with a PartitionSender.
     * <p>
     * There are 3 ways to send to EventHubs, to understand this particular type of Send refer to the overload {@link #send(EventData)}, which is the same type of Send and is used to send single {@link EventData}.
     * <p>
     * Sending a batch of {@link EventData}'s is useful in the following cases:
     * <pre>
     * i.   Efficient send - sending a batch of {@link EventData} maximizes the overall throughput by optimally using the number of sessions created to EventHubs' service.
     * ii.  Send multiple {@link EventData}'s in a Transaction. To achieve ACID properties, the Gateway Service will forward all {@link EventData}'s in the batch to a single EventHubs' partition.
     * </pre>
     *
     * @param eventDatas EventDataBatch to send to EventHub
     * @return a CompletableFuture that can be completed when the send operation is done..
     * @see #send(Iterable)
     * @see EventDataBatch
     */
    CompletableFuture<Void> send(EventDataBatch eventDatas);

    CompletableFuture<Void> close();

    void closeSync() throws EventHubException;
}
