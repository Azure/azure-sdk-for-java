// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is a lightweight wrapper around Sinks.Many to implement a Sink backed by a LinkedBlockingQueue, effectively
 * implementing a BlockingSink.
 */
public class StorageBlockingSink {
    private final ClientLogger logger = new ClientLogger(StorageBlockingSink.class);

    /*
    Note: Though this is used only by BlockBlobOutputStream, the decision was made to abstract this type out to make
    testing easier.
    */
    private final Sinks.Many<ByteBuffer> writeSink;
    private final LinkedBlockingQueue<ByteBuffer> writeLimitQueue;

    /* Overrided implementation of LinkedBlockingQueue to effectively implement a true BlockingSink. */
    private static class ProducerBlockingQueue<T> extends LinkedBlockingQueue<T> {
        private final ClientLogger logger = new ClientLogger(ProducerBlockingQueue.class);

        ProducerBlockingQueue(int queueSize) {
            super(queueSize);
        }

        /* The Reactor implementation of Sinks(UnicastProcessor).tryEmitNext (see usage below) makes a call to
        queue.offer(). The desired functionality of tryEmitNext is for it to block on backpressure until there is
        space in the queue. Since this code is called from user thread on BlobOutputStream.write and blocking on that
        API until data can be written is ok, this is the simplest way to get desired behavior. */
        @Override
        public boolean offer(T o) {
            try {
                super.put(o);
                return true;
            } catch (InterruptedException e) {
                throw logger.logExceptionAsError(new RuntimeException(e));
            }
        }
    }

    /**
     * Create a new StorageBlockingSink.
     */
    public StorageBlockingSink() {
        /*
        We use a LinkedBlockingQueue to effectively block the thread that calls tryEmitNext if there is
        backpressure from downstream. Its capacity is 1 to keep the buffer as small as possible, as downstream
        implementations do their own buffering.
        */
        this.writeLimitQueue = new ProducerBlockingQueue<>(1);
        this.writeSink = Sinks.many().unicast().onBackpressureBuffer(writeLimitQueue);
    }

    /**
     * Try to emit an element.
     *
     * @param buffer {@link ByteBuffer} to emit.
     * @throws IllegalStateException if an unrecoverable error was thrown.
     */
    public void tryEmitNext(ByteBuffer buffer) throws IllegalStateException {
        /*
        tryEmitNext returns a Sinks.EmitResult that indicates different success/error cases we can
        potentially handle (in case we want to retry transmission).
        */
        final Sinks.EmitResult writeResult = this.writeSink.tryEmitNext(buffer);
        switch (writeResult) {
            case OK: // Success
                return;
            case FAIL_OVERFLOW: /* When queue overflows. This indicates there is backpressure.
            NOTE: If the FAIL_OVERFLOW case ever gets hit, it indicates there is a mismatch between the way Reactor
            implements tryEmitNext and the way ProducerBlockingQueue is designed. */
            case FAIL_TERMINATED: /* Flux already emitted completion signal. We implicitly save customer from
            hitting this state by calling checkStreamState before write. */
            case FAIL_CANCELLED: /* Flux got a cancellation signal. */
            case FAIL_NON_SERIALIZED: /* Concurrent calls to tryEmitNext. */
            case FAIL_ZERO_SUBSCRIBER: /* No one ever subscribed to Flux. This should not happen since we manage the
            subscribe process in the constructor. */
            default: // This case should never get hit
                throw logger.logExceptionAsError(new IllegalStateException("Faulted stream due to underlying sink "
                    + "write failure, result:" + writeResult));
        }
    }

    /**
     * Try to emit a complete signal, otherwise throw.
     */
    public void tryEmitCompleteOrThrow() {
        this.writeSink.tryEmitComplete().orThrow();
    }

    /**
     * @return The Flux that represents the Sink.
     */
    public Flux<ByteBuffer> asFlux() {
        return this.writeSink.asFlux();
    }

}
