/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.util;

import java.nio.ByteBuffer;

/**
 * Contains helper methods for dealing with Flowables.
 */
public class ByteBufferUtil {
    /**
     * Read the contents of the ByteBuffer into a byte[].
     * @param byteBuffer The ByteBuffer to read from.
     * @return The created byte[].
     */
    public static byte[] toByteArray(ByteBuffer byteBuffer) {
        byte[] byteArray = null;
        if (byteBuffer != null) {
            byteArray = new byte[byteBuffer.remaining()];
            byteBuffer.get(byteArray);
        }
        return byteArray;
    }
}
