// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

/**
 * A util class for idempotent producer partition publishing.
 */
public class PartitionPublishingUtils {
    /**
     * Increase an int value. If the increased value is over {@link Integer#MAX_VALUE}, restart from 0.
     * @param value The number to be incremented.
     * @param delta The number is to be incremented by delta.
     * @return The incremented value.
     */
    public static int incrementSequenceNumber(int value, int delta) {
        if (Integer.MAX_VALUE - delta >= value) {
            return value + delta;
        } else {
            return delta - (Integer.MAX_VALUE - value) - 1;
        }
    }

    /**
     * Increase an int value by 1. If the increased value is over {@link Integer#MAX_VALUE}, restart from 0.
     * @param value The number to be incremented.
     * @return The incremented value.
     */
    public static int incrementSequenceNumber(int value) {
        return incrementSequenceNumber(value, 1);
    }
}
