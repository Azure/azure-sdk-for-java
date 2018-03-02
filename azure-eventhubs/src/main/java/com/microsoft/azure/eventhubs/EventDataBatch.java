/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

/**
 * Helper for creating a batch/collection of EventData objects to be used while Sending to EventHubs
 */
public interface EventDataBatch {

    /**
     * Get the number of events present in this {@link EventDataBatch}
     *
     * @return the EventDataBatch size
     */
    int getSize();

    /**
     * Add's {@link EventData} to {@link EventDataBatch}, if permitted by the batch's size limit.
     * This method is not thread-safe.
     *
     * @param eventData The {@link EventData} to add.
     * @return A boolean value indicating if the {@link EventData} addition to this batch/collection was successful or not.
     * @throws PayloadSizeExceededException when a single {@link EventData} instance exceeds maximum allowed size of the batch
     */
    boolean tryAdd(final EventData eventData) throws PayloadSizeExceededException;
}
