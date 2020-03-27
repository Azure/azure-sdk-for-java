package com.azure.storage.blob;

import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

final class BufferAggregator {
    private final long limit;
    private long size = 0;
    private Queue<ByteBuffer> buffers = new LinkedList<>();

    public BufferAggregator(long limit) {
        this.limit = limit;
    }

    public long remaining() {
        return limit - size;
    }

    public long size() {
        return this.size;
    }

    public void add(ByteBuffer byteBuffer) {
        buffers.add(byteBuffer);
        size += byteBuffer.remaining();
    }

    public void reset() {
        this.size = 0;
        this.buffers = new LinkedList<>();
    }

    public Flux<ByteBuffer> getBuffers(){
        return dequeuingFlux(this.buffers);
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
