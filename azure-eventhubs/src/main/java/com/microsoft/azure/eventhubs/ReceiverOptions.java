/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import com.microsoft.azure.servicebus.ClientConstants;

/**
 * Represents various optional behaviors which can be turned on or off during the creation of a {@link PartitionReceiver}.
 */
public final class ReceiverOptions {

    private boolean receiverRuntimeMetricEnabled;
    private String identifier;

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
     * after the first {@link PartitionReceiver#receive(int)} call, {@link PartitionReceiver#getRuntimeInformation()} is populated.
     * <p>
     * Enabling this knob will add 3 additional properties to all {@link EventData}'s received on the {@link EventHubClient#createReceiver}.
     *
     * @param value the {@link boolean} to indicate, whether, the runtime metric of the receiver should be enabled
     */
    public void setReceiverRuntimeMetricEnabled(boolean value) {

        this.receiverRuntimeMetricEnabled = value;
    }

    public String getIdentifier() {

        return this.identifier;
    }

    public void setIdentifier(final String value) {

        ReceiverOptions.validateReceiverIdentifier(value);
        this.identifier = value;
    }

    private static void validateReceiverIdentifier(final String receiverName) {

        if (receiverName != null &&
                receiverName.length() > ClientConstants.MAX_RECEIVER_NAME_LENGTH) {
            throw new IllegalArgumentException("receiverIdentifier length cannot exceed 64");
        }
    }
}
