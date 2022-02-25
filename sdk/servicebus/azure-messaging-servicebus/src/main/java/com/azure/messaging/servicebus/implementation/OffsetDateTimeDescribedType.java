// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.EPOCH_TICKS;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.OFFSETDATETIME_SYMBOL;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.TICK_PER_SECOND;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.TIME_LENGTH_DELTA;

/**
 * OffsetDateTime described type.
 */
public class OffsetDateTimeDescribedType extends ServiceBusDescribedType {
    /**
     * Set descriptor and described to describe data in described type.
     *
     * @param offsetDateTime set as described in DescribedType.
     */
    public OffsetDateTimeDescribedType(OffsetDateTime offsetDateTime) {
        super(OFFSETDATETIME_SYMBOL, convertToTickTime(offsetDateTime));
    }

    /**
     * .net SDK send time by using tick, i.e. TimeSpan.ticks(), DateTimeOffset.UtcTicks.
     * these method return tick value which represent a date like      2022-02-23T16:40:27.7665521+08:00
     * but in java, we can get nanoseconds which represent a date like 2022-02-23T16:40:27.766552100+08:00
     * we need to trim/append tick value to align with .net SDK for send/receive tick value here.
     * Tick value is start from 12:00:00 midnight on January 1, 0001 , need to add EPOCH_TICKS to epoch second.
     * @return described type value.
     */
    private static Long convertToTickTime(OffsetDateTime offsetDateTime) {
        int nano = offsetDateTime.toInstant().atOffset(ZoneOffset.UTC).getNano();
        long seconds = offsetDateTime.toInstant().atOffset(ZoneOffset.UTC).toEpochSecond();
        return EPOCH_TICKS + seconds * TICK_PER_SECOND + nano / TIME_LENGTH_DELTA;
    }

    @Override
    public int size() {
        return OFFSETDATETIME_SYMBOL.length() + Long.BYTES;
    }
}
