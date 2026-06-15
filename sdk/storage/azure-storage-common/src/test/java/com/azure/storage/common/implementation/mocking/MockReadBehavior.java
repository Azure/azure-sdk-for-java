// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.implementation.mocking;

import com.azure.storage.common.implementation.StorageSeekableByteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.BiFunction;

public class MockReadBehavior implements StorageSeekableByteChannel.ReadBehavior {
    private final long resourceLength;
    private final BiFunction<ByteBuffer, Long, Integer> readFunction;
    private final boolean hasConsistencyLock;

    public MockReadBehavior() {
        this(0, (buffer, offset) -> 0);
    }

    public MockReadBehavior(long resourceLength, BiFunction<ByteBuffer, Long, Integer> readFunction) {
        this(resourceLength, readFunction, true);
    }

    public MockReadBehavior(long resourceLength, BiFunction<ByteBuffer, Long, Integer> readFunction,
        boolean hasConsistencyLock) {
        this.resourceLength = resourceLength;
        this.readFunction = readFunction;
        this.hasConsistencyLock = hasConsistencyLock;
    }

    @Override
    public int read(ByteBuffer dst, long sourceOffset) throws IOException {
        return readFunction.apply(dst, sourceOffset);
    }

    @Override
    public final long getResourceLength() {
        return resourceLength;
    }

    @Override
    public boolean hasConsistencyLock() {
        return hasConsistencyLock;
    }
}
