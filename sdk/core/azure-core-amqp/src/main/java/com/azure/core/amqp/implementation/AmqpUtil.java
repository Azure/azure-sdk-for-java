// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import java.time.Instant;

/**
 * A reusable utility class.
 */
public class AmqpUtil {

    private static final long EPOCHINDOTNETTICKS = 621355968000000000L;

    /**
     * .Net ticks are measured from 01/01/0001, java instants are measured from 01/01/1970
     * @param dotNetTicks measure.
     * @return {@link Instant} representing dotNetTicks.
     */
    public static Instant convertDotNetTicksToInstant(long dotNetTicks) {
        long ticksFromEpoch = dotNetTicks - EPOCHINDOTNETTICKS;
        long millisecondsFromEpoch = ticksFromEpoch / 10000;
        long fractionTicks = ticksFromEpoch % 10000;
        return Instant.ofEpochMilli(millisecondsFromEpoch).plusNanos(fractionTicks * 100);
    }
}
