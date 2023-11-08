// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

class AppInsightsByteBufferPool {

    private static final int BYTE_BUFFER_SIZE = 65536;
    private static final int MAX_RETAINED = 10;

    private final Queue<ByteBuffer> queue = new ArrayBlockingQueue<>(MAX_RETAINED);

    ByteBuffer remove() {
        ByteBuffer byteBuffer = queue.poll();
        if (byteBuffer != null) {
            byteBuffer.clear();
            return byteBuffer;
        }
        return ByteBuffer.allocate(BYTE_BUFFER_SIZE);
    }

    @SuppressFBWarnings(
        value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
        justification =
            "this is just best effort returning byte buffers to the pool,"
                + " so it's ok if the offer doesn't succeed,"
                + " so there's no need to check the return value")
    void offer(List<ByteBuffer> byteBuffers) {
        // TODO(trask) batch offer?
        for (ByteBuffer byteBuffer : byteBuffers) {
            queue.offer(byteBuffer);
        }
    }
}
