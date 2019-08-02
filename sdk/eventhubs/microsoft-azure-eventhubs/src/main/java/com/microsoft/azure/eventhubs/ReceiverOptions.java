// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import com.microsoft.azure.eventhubs.impl.ClientConstants;

import java.util.Locale;

/**
 * Represents various optional behaviors which can be turned on or off during the creation of a {@link PartitionReceiver}.
 */
public final class ReceiverOptions {

    private boolean receiverRuntimeMetricEnabled;
    private String identifier;
    private int prefetchCount;

    public ReceiverOptions() {
        this.prefetchCount = PartitionReceiver.DEFAULT_PREFETCH_COUNT;
    }

    private static void validateReceiverIdentifier(final String receiverName) {

        if (receiverName != null
                && receiverName.length() > ClientConstants.MAX_RECEIVER_NAME_LENGTH) {
            throw new IllegalArgumentException("receiverIdentifier length cannot exceed 64");
        }
    }

    /**
     * Knob to enable/disable runtime metric of the receiver. If this is set to true and is passed to {@link EventHubClient#createReceiver},
     * after the first {@link PartitionReceiver#receive(int)} call, {@link PartitionReceiver#getRuntimeInformation()} is populated.
     * <p>
     * Enabling this knob will add 3 additional properties to all {@link EventData}'s received on the {@link EventHubClient#createReceiver}.
     *
     * @return the {@link boolean} indicating, whether, the runtime metric of the receiver was enabled
     */
    public boolean getReceiverRuntimeMetricEnabled() {

        return this.receiverRuntimeMetricEnabled;
    }

    /**
     * Knob to enable/disable runtime metric of the receiver. If this is set to true and is passed to {@link EventHubClient#createReceiver},
     * after the first {@link PartitionReceiver#receive(int)} call, {@link PartitionReceiver#getRuntimeInformation()} and
     * {@link PartitionReceiver#getEventPosition()} will be populated.
     * <p>
     * This knob facilitates for an optimization where the Consumer of Event Hub has the end of stream details at the disposal,
     * without making any additional {@link EventHubClient#getPartitionRuntimeInformation(String)} call to Event Hubs service.
     * To achieve this, behind the scenes, along with the actual {@link EventData}, that the Event Hubs {@link PartitionReceiver}
     * delivers, it includes extra information about the Event Hubs partitions end of stream details on every event.
     * In summary, enabling this knob will
     * help users to save an extra call to Event Hubs service to fetch Event Hubs partition information and as a result, will add that information as
     * header to each {@link EventData} received by the client.
     *
     * @param value the {@link boolean} to indicate, whether, the runtime metric of the receiver should be enabled
     */
    public void setReceiverRuntimeMetricEnabled(boolean value) {

        this.receiverRuntimeMetricEnabled = value;
    }

    /**
     * Gets the identifier of the {@link PartitionReceiver}
     *
     * @return identifier of the {@link PartitionReceiver}; null if nothing was set
     */
    public String getIdentifier() {

        return this.identifier;
    }

    /**
     * Set an identifier to {@link PartitionReceiver}.
     * <p>
     * This identifier will be used by EventHubs service when reporting any errors across receivers, and is caused by this receiver.
     * For example, when receiver quota limit is hit, while a user is trying to create New receiver,
     * EventHubs service will throw {@link QuotaExceededException} and will include this identifier.
     * So, its very critical to choose a value, which can uniquely identify the whereabouts of {@link PartitionReceiver}.
     * <p>
     *
     * @param value string to identify {@link PartitionReceiver}
     */
    public void setIdentifier(final String value) {

        ReceiverOptions.validateReceiverIdentifier(value);
        this.identifier = value;
    }

    /**
     * Get Prefetch Count.
     *
     * @return the upper limit of events this receiver will actively receive regardless of whether a receive operation is pending.
     * @see #setPrefetchCount
     */
    public int getPrefetchCount() {
        return this.prefetchCount;
    }

    /**
     * Set the number of events that can be pre-fetched and cached at the {@link PartitionReceiver}.
     * <p>By default the value is 500
     *
     * @param prefetchCount the number of events to pre-fetch. value must be between 1 and 2000.
     * @throws EventHubException if setting prefetchCount encounters error
     */
    public void setPrefetchCount(final int prefetchCount) throws EventHubException {
        if (prefetchCount < PartitionReceiver.MINIMUM_PREFETCH_COUNT) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "PrefetchCount has to be above %s", PartitionReceiver.MINIMUM_PREFETCH_COUNT));
        }

        if (prefetchCount > PartitionReceiver.MAXIMUM_PREFETCH_COUNT) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "PrefetchCount has to be below %s", PartitionReceiver.MAXIMUM_PREFETCH_COUNT));
        }

        this.prefetchCount = prefetchCount;
    }
}
