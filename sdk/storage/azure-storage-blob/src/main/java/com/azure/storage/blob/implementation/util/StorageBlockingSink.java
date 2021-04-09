// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
    private static final int WRITE_RETRY_IN_SECONDS = 3;

    /**
     * Create a new StorageBlockingSink.
     */
    public StorageBlockingSink() {
        /*
        We use a LinkedBlockingQueue to effectively block the thread that calls tryEmitNext if there is
        backpressure from downstream. Its capacity is 1 to keep the buffer as small as possible, as downstream
        implementations do their own buffering.
        */
        Queue<ByteBuffer> writeLimitQueue = new LinkedBlockingQueue<>(1);
        this.writeSink = Sinks.many().unicast().onBackpressureBuffer(writeLimitQueue);
    }

    /**
     * Try to emit an element.
     *
     * @param buffer {@link ByteBuffer} to emit.
     * @return an optional IOException if an unrecoverable error was thrown.
     */
    public IOException tryEmitNext(ByteBuffer buffer) {
        /*
        tryEmitNext returns a Sinks.EmitResult that indicates different success/error cases we can
        potentially handle (in case we want to retry transmission).
        */
        boolean shouldRetry = false;
        do {
            final Sinks.EmitResult writeResult = this.writeSink.tryEmitNext(buffer);

            switch (writeResult) {
                case OK: // Success
                    return null;
                case FAIL_OVERFLOW: { // When queue overflows. This indicates there is backpressure.
                    shouldRetry = true;
                    try {
                        TimeUnit.SECONDS.sleep(WRITE_RETRY_IN_SECONDS);
                    } catch (InterruptedException e) {
                        /*
                        Just throw the error and do not populate the error field since a customer can recover from
                        this error
                        */
                        throw logger.logExceptionAsError(new RuntimeException(e));
                    }
                    break;
                }
                case FAIL_TERMINATED: // Flux already emitted completion signal. We implicitly save customer from hitting this state by calling checkStreamState before write.
                case FAIL_CANCELLED: // Flux got a cancellation signal.
                case FAIL_NON_SERIALIZED: // Concurrent calls to tryEmitNext.
                case FAIL_ZERO_SUBSCRIBER: // No one ever subscribed to Flux. This should not happen since we manage the subscribe process in the constructor.
                default: // This case shouldnt get hit - Would it be better to do nothing in this case?
                    return new IOException("Faulted stream due to underlying sink write failure, "
                        + "result:" + writeResult, null);
            }
        } while (shouldRetry);
        return null;
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
