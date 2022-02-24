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
     * @param described  real value in the described type.
     */
    public DurationDescribedType(Object described) {
        super(DURATION_SYMBOL, convertToTickTime(described));
    }

    /**
     * .net SDK send time by using tick, i.e. TimeSpan.ticks(), DateTimeOffset.UtcTicks.
     * these method return tick value which represent a date like      2022-02-23T16:40:27.7665521+08:00
     * but in java, we can get nanoseconds which represent a date like 2022-02-23T16:40:27.766552100+08:00
     * we need to trim/append tick value to align with .net SDK for send/receive tick value here.
     * @return described type value.
     */
    private static Long convertToTickTime(Object described) {
        return ((Duration) described).toNanos() / TIME_LENGTH_DELTA;
    }

    @Override
    public int size() {
        return DURATION_SYMBOL.length() + Long.BYTES;
    }
}
