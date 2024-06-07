// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class AppInsightsByteBufferPool {

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

    void offer(List<ByteBuffer> byteBuffers) {
        // TODO(trask) batch offer?
        for (ByteBuffer byteBuffer : byteBuffers) {
            queue.offer(byteBuffer);
        }
    }
}
