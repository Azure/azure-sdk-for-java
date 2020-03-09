// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Contains helper methods for message conversions.
 */
public final class MessageUtils {
    public static final UUID ZERO_LOCK_TOKEN = new UUID(0L, 0L);
    private static final int LOCK_TOKEN_SIZE = 16;
    private static final int GUID_SIZE = 16;

    private MessageUtils() {
    }

    /**
     * Converts a .NET GUID to its Java UUID representation.
     *
     * @param dotNetBytes .NET GUID to convert.
     *
     * @return the equivalent UUID.
     */
    public static UUID convertDotNetBytesToUUID(byte[] dotNetBytes) {
        // First 4 bytes are in reverse order, 5th and 6th bytes are in reverse order,
        // 7th and 8th bytes are also in reverse order
        if (dotNetBytes == null || dotNetBytes.length != GUID_SIZE) {
            return ZERO_LOCK_TOKEN;
        }

        byte[] reOrderedBytes = new byte[GUID_SIZE];
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

            reOrderedBytes[indexInReorderedBytes] = dotNetBytes[i];
        }

        ByteBuffer buffer = ByteBuffer.wrap(reOrderedBytes);
        long mostSignificantBits = buffer.getLong();
        long leastSignificantBits = buffer.getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
