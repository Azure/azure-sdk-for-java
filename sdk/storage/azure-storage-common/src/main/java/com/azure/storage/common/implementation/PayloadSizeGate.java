// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class provides ability to measure if incoming Flux of ByteBuffers is larger than a threshold.
 * This answers question if volume of data in bytes is larger than threshold.
 *
 * The {@link #write(ByteBuffer)} operation buffers incoming ByteBuffers until threshold is crossed.
 * After that it's pass-through as fact that data volume exceeds threshold is already determined.
 *
 * RESERVED FOR INTERNAL USE.
 */
final class PayloadSizeGate {
    private final long threshold;
    private long size = 0;
    private Queue<ByteBuffer> byteBuffers = new LinkedList<>();

    /**
     * Creates a new instance of PayloadSizeGate
     * @param threshold Number of bytes up to which data is buffered.
     */
    PayloadSizeGate(long threshold) {
        this.threshold = threshold;
    }

    /**
     * Keeps buffering buffers until threshold is breached.
     * Then it acts as pass-through.
     * @param buf Incoming data.
     * @return Buffered data or incoming data depending on threshold condition.
     */
    Flux<ByteBuffer> write(ByteBuffer buf) {
        if (isThresholdBreached()) {
            size += buf.remaining();
            return Flux.just(buf);
        } else {
            size += buf.remaining();
            byteBuffers.add(buf);
            if (isThresholdBreached()) {
                Flux<ByteBuffer> result = dequeuingFlux(byteBuffers);
                byteBuffers = null;
                return result;
            } else {
                return Flux.empty();
            }
        }
    }

    /**
     * Flushes the gate. If threshold has not been broken then invoking this method pushes any lingering data forward.
     * @return Buffered data if threshold has not been broken. Otherwise empty.
     */
    Flux<ByteBuffer> flush() {
        if (byteBuffers != null) {
            // We return Flux from iterable in this case to support retries on single upload.
            Flux<ByteBuffer> result = Flux.fromIterable(byteBuffers);
            byteBuffers = null;
            return result;
        } else {
            return Flux.empty();
        }
    }

    /**
     * @return Size of data observed by the gate.
     */
    long size() {
        return size;
    }

    /**
     * @return A flag indicating if observed data has breached the threshold.
     */
    boolean isThresholdBreached() {
        return size > threshold;
    }

    private static Flux<ByteBuffer> dequeuingFlux(Queue<ByteBuffer> queue) {
        // Generate is used as opposed to Flux.fromIterable as it allows the buffers to be garbage collected sooner.
        return Flux.generate(sink -> {
            ByteBuffer buffer = queue.poll();
            if (buffer != null) {
                sink.next(buffer);
            } else {
                sink.complete();
            }
        });
    }
}
