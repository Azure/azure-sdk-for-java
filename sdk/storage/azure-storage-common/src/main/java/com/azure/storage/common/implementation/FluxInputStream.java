// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An InputStream interface that subscribes to a Flux allows data to be read.
 */
public class FluxInputStream extends InputStream {

    private ClientLogger logger = new ClientLogger(FluxInputStream.class);

    // The data to subscribe to.
    private Flux<ByteBuffer> data;

    // Subscription to request more data from as needed
    private Subscription subscription;

    private ByteArrayInputStream buffer;

    private boolean subscribed;
    private boolean fluxComplete;
    private boolean waitingForData;

    /* The following lock and condition variable is to synchronize access between the reader and the
        reactor thread asynchronously reading data from the Flux. If no data is available, the reader
        acquires the lock and waits on the dataAvailable condition variable. Once data is available
        (or an error or completion event occurs) the reactor thread acquires the lock and signals that
        data is available. */
    private final Lock lock;
    private final Condition dataAvailable;

    private IOException lastError;

    /**
     * Creates a new FluxInputStream
     *
     * @param data The data to subscribe to and read from.
     */
    public FluxInputStream(Flux<ByteBuffer> data) {
        this.subscribed = false;
        this.fluxComplete = false;
        this.waitingForData = false;
        this.data = data;
        this.lock = new ReentrantLock();
        this.dataAvailable = lock.newCondition();
    }

    @Override
    public int read() throws IOException {
        byte[] ret = new byte[1];
        int count = read(ret, 0, 1);
        return count == -1 ? -1 : ret[0];
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        validateParameters(b, off, len);

        /* If len is 0, then no bytes are read and 0 is returned. */
        if (len == 0) {
            return 0;
        }
        /* Attempt to read at least one byte. If no byte is available because the stream is at end of file,
           the value -1 is returned; otherwise, at least one byte is read and stored into b. */

        /* Not subscribed? subscribe and block for data */
        if (!subscribed) {
            blockForData();
        }
        /* Now, we have subscribed. */
        /* At this point, buffer should not be null. If it is, that indicates either an error or completion event
           was emitted by the Flux. */
        if (this.buffer == null) { // Only executed on first subscription.
            if (this.lastError != null) {
                throw logger.logThrowableAsError(this.lastError);
            }
            if (this.fluxComplete) {
                return -1;
            }
            throw logger.logExceptionAsError(new IllegalStateException("An unexpected error occurred. No data was "
                + "read from the stream but the stream did not indicate completion."));
        }

        /* Now we are guaranteed that buffer is SOMETHING. */
        /* No data is available in the buffer.  */
        if (this.buffer.available() == 0) {
            /* If the flux completed, there is no more data available to be read from the stream. Return -1. */
            if (this.fluxComplete) {
                return -1;
            }
            /* Block current thread until data is available. */
            blockForData();
        }

        /* Data available in buffer, read the buffer. */
        if (this.buffer.available() > 0) {
            return this.buffer.read(b, off, len);
        }

        /* If the flux completed, there is no more data available to be read from the stream. Return -1. */
        if (this.fluxComplete) {
            return -1;
        } else {
            throw logger.logExceptionAsError(new IllegalStateException("An unexpected error occurred. No data was "
                + "read from the stream but the stream did not indicate completion."));
        }
    }

    @Override
    public void close() throws IOException {
        subscription.cancel();
        if (this.buffer != null) {
            this.buffer.close();
        }
        super.close();
        if (this.lastError != null) {
            throw logger.logThrowableAsError(this.lastError);
        }
    }

    /**
     * Request more data and wait on data to become available.
     */
    private void blockForData() {
        lock.lock();
        try {
            waitingForData = true;
            if (!subscribed) {
                subscribeToData();
            } else {
                subscription.request(1);
            }
            // Block current thread until data is available.
            while (waitingForData) {
                if (fluxComplete) {
                    break;
                } else {
                    try {
                        dataAvailable.await();
                    } catch (InterruptedException e) {
                        throw logger.logExceptionAsError(new RuntimeException(e));
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Subscribes to the data with a special subscriber.
     */
    private void subscribeToData() {
        this.data
            .filter(Buffer::hasRemaining) /* Filter to make sure only non empty byte buffers are emitted. */
            .onBackpressureBuffer()
            .subscribe(
                // ByteBuffer consumer
                byteBuffer -> {
                    this.buffer = new ByteArrayInputStream(FluxUtil.byteBufferToArray(byteBuffer));
                    lock.lock();
                    try {
                        this.waitingForData = false;
                        // Signal the consumer when data is available.
                        dataAvailable.signal();
                    } finally {
                        lock.unlock();
                    }
                },
                // Error consumer
                throwable -> {
                    // Signal the consumer in case an error occurs (indicates we completed without data).
                    if (throwable instanceof IOException) {
                        this.lastError = (IOException) throwable;
                    } else {
                        this.lastError = new IOException(throwable);
                    }
                    signalOnCompleteOrError();
                },
                // Complete consumer
                // Signal the consumer in case we completed without data.
                this::signalOnCompleteOrError,
                // Subscription consumer
                subscription -> {
                    this.subscription = subscription;
                    this.subscribed = true;
                    this.subscription.request(1);
                }
            );
    }

    /**
     * Signals to the subscriber when the flux completes without data (onCompletion or onError)
     */
    private void signalOnCompleteOrError() {
        this.fluxComplete = true;
        lock.lock();
        try {
            this.waitingForData = false;
            dataAvailable.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Validates parameters according to {@link InputStream#read(byte[], int, int)} spec.
     *
     * @param b the buffer into which the data is read.
     * @param off the start offset in array b at which the data is written.
     * @param len the maximum number of bytes to read.
     */
    private void validateParameters(byte[] b, int off, int len) {
        if (b == null) {
            throw logger.logExceptionAsError(new NullPointerException("'b' cannot be null"));
        }
        if (off < 0) {
            throw logger.logExceptionAsError(new IndexOutOfBoundsException("'off' cannot be less than 0"));
        }
        if (len < 0) {
            throw logger.logExceptionAsError(new IndexOutOfBoundsException("'len' cannot be less than 0"));
        }
        if (len > (b.length - off)) {
            throw logger.logExceptionAsError(new IndexOutOfBoundsException("'len' cannot be greater than 'b'.length - 'off'"));
        }
    }
}
