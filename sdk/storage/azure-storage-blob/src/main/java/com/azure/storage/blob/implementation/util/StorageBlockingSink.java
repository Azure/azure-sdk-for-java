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
public final class StorageBlockingSink {
    private final ClientLogger logger = new ClientLogger(StorageBlockingSink.class);

    /*
    Note: Though this is used only by BlockBlobOutputStream, the decision was made to abstract this type out to make
    testing easier.
    */
    private final Sinks.Many<ByteBuffer> writeSink;
    private final LinkedBlockingQueue<ByteBuffer> writeLimitQueue;

    /* Overrided implementation of LinkedBlockingQueue to effectively implement a true BlockingSink. */
    private static final class ProducerBlockingQueue<ByteBuffer> extends LinkedBlockingQueue<ByteBuffer> {
        private final transient ClientLogger logger;
        private static final long serialVersionUID = 1;

        ProducerBlockingQueue(int queueSize, ClientLogger logger) {
            super(queueSize);
            this.logger = logger;
        }

        /* The Reactor implementation of Sinks(UnicastProcessor).tryEmitNext (see usage below) makes a call to
        queue.offer(). The desired functionality of tryEmitNext is for it to block on backpressure until there is
        space in the queue. Since this code is called from user thread on BlobOutputStream.write and blocking on that
        API until data can be written is ok, this is the simplest way to get desired behavior. */
        @Override
        public boolean offer(ByteBuffer o) {
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
        We use a ProducerBlockingQueue to effectively block the thread that calls tryEmitNext if there is
        backpressure from downstream. Its capacity is 1 to keep the buffer as small as possible, as downstream
        implementations do their own buffering.
        */
        this.writeLimitQueue = new ProducerBlockingQueue<>(1, this.logger);
        this.writeSink = Sinks.many().unicast().onBackpressureBuffer(writeLimitQueue);
    }

    /**
     * Try to emit an element.
     *
     * @param buffer {@link ByteBuffer} to emit.
     */
    public void emitNext(ByteBuffer buffer) {
        try {
            this.writeSink.tryEmitNext(buffer).orThrow();
            /* Here are different cases that tryEmitNext can return.
             * OK: Success
             * FAIL_OVERFLOW: When the writeLimitQueue overflows. This indicates there is backpressure. NOTE: If this
             * ever gets hit, it indicates that there is a mismatch between the way Reactor implements tryEmitNext
             * and they way ProducerBlockingQueue is designed.
             * FAIL_TERMINATED: The Flux already emitted a completion signal. We implicitly save ourselves from hitting
             * this case by calling checkStreamState before write in BlockBlobOutputStream.
             * FAIL_CANCELLED: The Flux received a cancellation signal. This can happen due to timeouts.
             * FAIL_NON_SERIALIZED: Concurrent calls to tryEmitNext. This is invalid for OutputStreams anyway.
             * FAIL_ZERO_SUBSCRIBER: The Flux was never subscribed to. We implicitly save ourselves from hitting this
             * case since we manage the subscribe process in the constructor of BlockBlobOutputStream
             */
        } catch (Exception e) {
            throw logger.logExceptionAsError(new IllegalStateException("Faulted stream due to underlying sink "
                + "write failure", e));
        }
    }

    /**
     * Try to emit a complete signal, otherwise throw.
     */
    public void emitCompleteOrThrow() {
        this.writeSink.tryEmitComplete().orThrow();
    }

    /**
     * @return The Flux that represents the Sink.
     */
    public Flux<ByteBuffer> asFlux() {
        return this.writeSink.asFlux();
    }

}
