/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

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

    void offer(List<ByteBuffer> byteBuffers) {
        // TODO(trask) batch offer?
        for (ByteBuffer byteBuffer : byteBuffers) {
            queue.offer(byteBuffer);
        }
    }
}
