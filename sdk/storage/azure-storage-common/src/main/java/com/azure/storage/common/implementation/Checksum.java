// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import java.nio.ByteBuffer;

/**
 * Wrapper class for checksum calculation.
 */
public interface Checksum {
    void update(byte[] b, int off, int len);

    default void update(ByteBuffer buffer) {
        int pos = buffer.position();
        int limit = buffer.limit();
        assert (pos <= limit);
        int rem = limit - pos;
        if (rem <= 0) {
            return;
        }
        if (buffer.hasArray()) {
            update(buffer.array(), pos + buffer.arrayOffset(), rem);
        } else {
            byte[] b = new byte[Math.min(buffer.remaining(), 4096)];
            while (buffer.hasRemaining()) {
                int length = Math.min(buffer.remaining(), b.length);
                buffer.get(b, 0, length);
                update(b, 0, length);
            }
        }
        buffer.position(limit);
    }

    byte[] getValue();

    void reset();
}
