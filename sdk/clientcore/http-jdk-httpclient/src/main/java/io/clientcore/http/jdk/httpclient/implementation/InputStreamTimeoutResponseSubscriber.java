// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.jdk.httpclient.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.Semaphore;

/**
 * Implementation of {@link HttpResponse.BodySubscriber} that emits the response body as a {@link InputStream} while
 * tracking a timeout for each value emitted by the subscription.
 * <p>
 * This differs from {@link HttpResponse.BodySubscribers#ofPublisher()} in that it tracks a timeout for each value
 * emitted by the subscription. This is needed to offer better reliability when reading the response in cases where the
 * server is either slow to respond or the connection has dropped without a signal from the server.
 */
public final class InputStreamTimeoutResponseSubscriber extends InputStream
    implements HttpResponse.BodySubscriber<InputStream> {
    // Sentinel values to indicate completion.
    private static final ByteBuffer LAST_BUFFER = ByteBuffer.wrap(new byte[0]);
    private static final List<ByteBuffer> LAST_LIST = List.of(LAST_BUFFER);

    // A queue of yet unprocessed ByteBuffers received from the flow API.
    private final BlockingQueue<List<ByteBuffer>> buffers;
    private volatile Flow.Subscription subscription;
    private volatile boolean subscribed;
    private volatile boolean closed;
    private volatile Throwable failed;
    private volatile Iterator<ByteBuffer> currentListItr;
    private volatile ByteBuffer currentBuffer;

    private final Semaphore semaphore = new Semaphore(1);

    private final long readTimeout;
    private TimerTask currentTimeout;

    /**
     * Creates a response body subscriber that emits the response body as a {@link InputStream} while tracking a timeout
     * for each value emitted by the subscription.
     *
     * @param readTimeout The timeout for reading each value emitted by the subscription.
     */
    public InputStreamTimeoutResponseSubscriber(long readTimeout) {
        // Use a queue size of 2 to allow for the in-process list of buffers and the sentinel value.
        // onComplete happens after onNext which will result in two items in the queue.
        // All other states are invalid if there are more than two items in the queue.
        this.buffers = new ArrayBlockingQueue<>(2);
        this.readTimeout = readTimeout;
    }

    @Override
    public CompletionStage<InputStream> getBody() {
        // Complete the future immediately as consumption of the network body is lazy and will be done through the
        // InputStream APIs.
        return CompletableFuture.completedStage(this);
    }

    private ByteBuffer current() throws IOException {
        while (currentBuffer == null || !currentBuffer.hasRemaining()) {
            // Validate state before attempting to get a new buffer.
            validateState();

            if (currentBuffer == LAST_BUFFER) {
                break;
            }

            try {
                if (currentListItr == null || !currentListItr.hasNext()) {
                    // Use 'take' over 'poll' to block until an item is available.
                    // This is important to ensure that the timeout is correctly scheduled.
                    List<ByteBuffer> lb = buffers.take();
                    currentListItr = lb.iterator();

                    // Validate state again as an error may have happened while waiting for the buffer.
                    validateState();

                    // Check whether we're done.
                    if (lb == LAST_LIST) {
                        currentListItr = null;
                        currentBuffer = LAST_BUFFER;
                        break;
                    }

                    // Request another upstream item ( list of buffers )
                    Flow.Subscription s = subscription;
                    if (s != null) {
                        currentTimeout = createTimeout();
                        s.request(1);
                    }

                    if (lb.isEmpty()) {
                        continue;
                    }
                }

                currentBuffer = currentListItr.next();
            } catch (InterruptedException ex) {
                close();
                Thread.currentThread().interrupt();
                throw new IOException(ex);
            }
        }

        return currentBuffer;
    }

    private void validateState() throws IOException {
        // Stream is closed, it's invalid to attempt to read after closure.
        if (closed) {
            // If the failure was a timeout, throw a HttpTimeoutException instead.
            if (failed instanceof HttpTimeoutException) {
                throw (HttpTimeoutException) failed;
            } else {
                throw new IOException("closed", failed);
            }
        }

        // Stream has failed, propagate the exception.
        if (failed != null) {
            // If the failure was a timeout, throw a HttpTimeoutException instead.
            if (failed instanceof HttpTimeoutException) {
                throw (HttpTimeoutException) failed;
            } else {
                throw new IOException(failed);
            }
        }
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, bytes.length);

        // Nothing to be read, return.
        if (len == 0) {
            return 0;
        }

        ByteBuffer buffer = current();
        // If this is the sentinel value, we're done.
        if (buffer == LAST_BUFFER) {
            return -1;
        }

        // Either read what was requested or what is left in the buffer.
        int read = Math.min(buffer.remaining(), len);
        buffer.get(bytes, off, read);

        return read;
    }

    @Override
    public int read() throws IOException {
        ByteBuffer buffer = current();
        if (buffer == LAST_BUFFER) {
            // If this is the sentinel value, we're done.
            return -1;
        }

        return buffer.get() & 0xFF;
    }

    @Override
    public int available() {
        // best effort: returns the number of remaining bytes in
        // the current buffer if any, or 1 if the current buffer
        // is null or empty but the queue or current buffer list
        // are not empty. Returns 0 otherwise.
        if (closed) {
            return 0;
        }

        int available = 0;
        ByteBuffer current = currentBuffer;
        if (current == LAST_BUFFER) {
            return 0;
        }

        if (current != null) {
            available = current.remaining();
        }

        if (available != 0) {
            return available;
        }

        Iterator<?> iterator = currentListItr;
        if (iterator != null && iterator.hasNext()) {
            return 1;
        }

        if (buffers.isEmpty()) {
            return 0;
        }

        return 1;
    }

    @Override
    public void onSubscribe(Flow.Subscription s) {
        if (subscribed) {
            // Only one subscription is valid.
            s.cancel();
            return;
        }

        // Check for the stream being closed before the subscription.
        boolean closed = this.closed;
        if (closed) {
            // Stream was closed before subscription, cancel the subscription.
            s.cancel();
            return;
        }

        subscription = s;
        subscribed = true;
        currentTimeout = createTimeout();
        s.request(1);
    }

    @Override
    public void onNext(List<ByteBuffer> t) {
        // Cancel the timeout as the next element has been received.
        currentTimeout.cancel();
        Objects.requireNonNull(t);
        if (!buffers.offer(t)) {
            IllegalStateException ex = new IllegalStateException("queue is full");
            failed = ex;
            close();
            onError(ex);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        // Cancel the timeout as we're in an error state.
        currentTimeout.cancel();
        subscription = null;

        // If we've already received a failure, add the new error as a suppressed exception.
        if (failed != null) {
            failed.addSuppressed(throwable);
        } else {
            failed = Objects.requireNonNull(throwable);
        }

        // Offer to the queue the sentinel value. If the stream was waiting on the queue this will unblock it and allow
        // the stream to propagate the exception.
        // If the queue is full, this will drop this offer, but that's fine as we're already in an error state.
        buffers.offer(LAST_LIST);
    }

    @Override
    public void onComplete() {
        // Cancel the timeout as we're done.
        currentTimeout.cancel();
        subscription = null;

        // Offer to the queue the sentinel value. If the stream was waiting on the queue this will unblock it and allow
        // the stream to propagate the completion.
        onNext(LAST_LIST);
    }

    @Override
    public void close() {
        Flow.Subscription s;
        semaphore.acquireUninterruptibly();
        try {
            if (closed) {
                return;
            }

            closed = true;
            s = subscription;
            subscription = null;
        } finally {
            semaphore.release();
        }

        // s will be null if already completed
        try {
            if (s != null) {
                s.cancel();
            }
        } finally {
            buffers.offer(LAST_LIST);
        }
    }

    private TimerTask createTimeout() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Set the failed exception before cancelling. Cancelling the subscription causes an error to be emitted
                // about the subscription being cancelled which we don't want to propagate as we are explicitly doing
                // it.
                failed = new HttpTimeoutException("Timeout reading response body.");
                subscription.cancel();
                close();
            }
        };

        JdkHttpUtils.scheduleTimeoutTask(task, readTimeout);
        return task;
    }
}
