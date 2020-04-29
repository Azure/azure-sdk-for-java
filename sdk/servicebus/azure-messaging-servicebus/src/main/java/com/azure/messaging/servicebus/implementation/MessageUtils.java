// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ExceptionUtil;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Contains helper methods for message conversions and reading status codes.
 */
final class MessageUtils {
    static final UUID ZERO_LOCK_TOKEN = new UUID(0L, 0L);
    static final int LOCK_TOKEN_SIZE = 16;

    private static final long EPOCH_IN_DOT_NET_TICKS = 621355968000000000L;
    private static final int GUID_SIZE = 16;

    private MessageUtils() {
    }

    /**
     * Converts a .NET GUID to its Java UUID representation.
     *
     * @param dotNetBytes .NET GUID to convert.
     * @return the equivalent UUID.
     */
    static UUID convertDotNetBytesToUUID(byte[] dotNetBytes) {
        // First 4 bytes are in reverse order, 5th and 6th bytes are in reverse order,
        // 7th and 8th bytes are also in reverse order
        if (dotNetBytes == null || dotNetBytes.length != GUID_SIZE) {
            return ZERO_LOCK_TOKEN;
        }

        final byte[] reOrderedBytes = reorderBytes(dotNetBytes);

        final ByteBuffer buffer = ByteBuffer.wrap(reOrderedBytes);
        final long mostSignificantBits = buffer.getLong();
        final long leastSignificantBits = buffer.getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    /**
     * Converts a Java UUID to its byte[] representation.
     *
     * @param uuid UUID to convert to .NET bytes.
     * @return The .NET byte representation.
     */
    static byte[] convertUUIDToDotNetBytes(UUID uuid) {
        if (uuid == null || uuid.equals(ZERO_LOCK_TOKEN)) {
            return new byte[GUID_SIZE];
        }

        ByteBuffer buffer = ByteBuffer.allocate(GUID_SIZE);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        byte[] javaBytes = buffer.array();

        return reorderBytes(javaBytes);
    }

    /**
     * Gets the {@link Instant} representation of .NET epoch ticks. .NET ticks are measured from 0001/01/01. Java
     * {@link Instant} is measured from 1970/01/01.
     *
     * @param dotNetTicks long measured from 01/01/0001
     * @return The instant represented by the ticks.
     */
    static Instant convertDotNetTicksToInstant(long dotNetTicks) {
        long ticksFromEpoch = dotNetTicks - EPOCH_IN_DOT_NET_TICKS;
        long millisecondsFromEpoch = Double.valueOf(ticksFromEpoch * 0.0001).longValue();
        long fractionTicks = ticksFromEpoch % 10000;

        return Instant.ofEpochMilli(millisecondsFromEpoch).plusNanos(fractionTicks * 100);
    }

    // Pass little less than client timeout to the server so client doesn't time out before server times out
    static Duration adjustServerTimeout(Duration clientTimeout) {
        return clientTimeout.minusMillis(1000);
    }

    /**
     * Creates an exception given the error condition and context.
     *
     * @param errorCondition Error condition for the AMQP exception.
     * @param errorContext AMQP context it occurred in.
     * @return Corresponding {@link Throwable} for the error condition.
     */
    static Throwable toException(ErrorCondition errorCondition, AmqpErrorContext errorContext) {
        final Symbol condition = errorCondition.getCondition();
        final String description = errorCondition.getDescription();

        try {
            return ExceptionUtil.toException(condition.toString(), description, errorContext);
        } catch (IllegalArgumentException ignored) {
            final ServiceBusErrorCondition error = ServiceBusErrorCondition.fromString(condition.toString());

            return toException(error, description, errorContext);
        }
    }

    static Throwable toException(ServiceBusErrorCondition errorCondition, String description,
        AmqpErrorContext errorContext) {

        final boolean isTransient = errorCondition.isTransient();
        final String message = String.format("condition[%s]: %s", errorCondition.toString(), description);
        return new AmqpException(isTransient, message, errorContext);
    }

    private static byte[] reorderBytes(byte[] javaBytes) {
        byte[] reorderedBytes = new byte[GUID_SIZE];
        for (int i = 0; i < GUID_SIZE; i++) {
            int indexInReorderedBytes;
            switch (i) {
                case 0:
                    indexInReorderedBytes = 3;
                    break;
                case 1:
                    indexInReorderedBytes = 2;
                    break;
                case 2:
                    indexInReorderedBytes = 1;
                    break;
                case 3:
                    indexInReorderedBytes = 0;
                    break;
                case 4:
                    indexInReorderedBytes = 5;
                    break;
                case 5:
                    indexInReorderedBytes = 4;
                    break;
                case 6:
                    indexInReorderedBytes = 7;
                    break;
                case 7:
                    indexInReorderedBytes = 6;
                    break;
                default:
                    indexInReorderedBytes = i;
            }

            reorderedBytes[indexInReorderedBytes] = javaBytes[i];
        }

        return reorderedBytes;
    }
}
