// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import java.time.Duration;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DURATION_SYMBOL;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.TIME_LENGTH_DELTA;

/**
 * Duration described type.
 */
public class DurationDescribedType extends ServiceBusDescribedType {
    /**
     * Set described to describe data in described type.
     *
     * @param duration set as described in DescribedType.
     */
    public DurationDescribedType(Duration duration) {
        super(DURATION_SYMBOL, convertToTickTime(duration));
    }

    /**
     * Convert nanosecond to ticks, align with dotnet SDK.
     * @return described type value.
     */
    private static Long convertToTickTime(Duration described) {
        return described.toNanos() / TIME_LENGTH_DELTA;
    }

    @Override
    public int size() {
        return DURATION_SYMBOL.length() + Long.BYTES;
    }
}
