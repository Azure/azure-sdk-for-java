// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

/**
 * The handler to invoke after receiving {@link EventData}s from Microsoft Azure EventHubs. Use any implementation of this abstract class to specify
 * user action when using PartitionReceiver's setReceiveHandler().
 *
 * @see PartitionReceiver#setReceiveHandler
 */
public interface PartitionReceiveHandler {

    /**
     * Maximum number of {@link EventData} to supply while invoking {@link #onReceive(Iterable)}
     * <p>Ensure that the value should be less than or equal to the value of {@link ReceiverOptions#getPrefetchCount()}
     *
     * @return value indicating the maximum number of {@link EventData} to supply while invoking {@link #onReceive(Iterable)}
     */
    int getMaxEventCount();

    /**
     * user should implement this method to specify the action to be performed on the received events.
     *
     * @param events the list of fetched events from the corresponding PartitionReceiver.
     * @see PartitionReceiver#receive
     */
    void onReceive(Iterable<EventData> events);

    /**
     * Implement this method to Listen to errors which lead to Closure of the {@link PartitionReceiveHandler} pump.
     *
     * @param error fatal error encountered while running the {@link PartitionReceiveHandler} pump
     */
    void onError(Throwable error);
}
