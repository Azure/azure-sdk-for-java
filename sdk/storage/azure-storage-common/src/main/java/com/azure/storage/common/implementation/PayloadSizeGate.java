package com.azure.storage.common.implementation;

import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

final class PayloadSizeGate {
    private final long threshold;
    private long size = 0;
    private Queue<ByteBuffer> byteBuffers = new LinkedList<>();

    public PayloadSizeGate(long threshold) {
        this.threshold = threshold;
    }

    Flux<ByteBuffer> write(ByteBuffer buf) {
        if (isThresholdBroken()) {
            return Flux.just(buf);
        } else {
            size += buf.remaining();
            byteBuffers.add(buf);
            if (isThresholdBroken()) {
                Flux<ByteBuffer> result = dequeuingFlux(byteBuffers);
                byteBuffers = null;
                return result;
            } else {
                return Flux.empty();
            }
        }
    }

    Flux<ByteBuffer> flush() {
        if (byteBuffers != null) {
            Flux<ByteBuffer> result = dequeuingFlux(byteBuffers);
            byteBuffers = null;
            return result;
        } else {
            return Flux.empty();
        }
    }

    long size() {
        return size;
    }

    boolean isThresholdBroken() {
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
