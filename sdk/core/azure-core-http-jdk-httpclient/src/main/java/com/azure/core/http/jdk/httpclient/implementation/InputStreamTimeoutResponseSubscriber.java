// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static final ClientLogger LOGGER = new ClientLogger(InputStreamTimeoutResponseSubscriber.class);

    static final int MAX_BUFFERS_IN_QUEUE = 1;  // lock-step with the producer

    // An immutable ByteBuffer sentinel to mark that the last byte was received.
    private static final ByteBuffer LAST_BUFFER = ByteBuffer.wrap(new byte[0]);
    private static final List<ByteBuffer> LAST_LIST = List.of(LAST_BUFFER);

    // A queue of yet unprocessed ByteBuffers received from the flow API.
    private final BlockingQueue<List<ByteBuffer>> buffers;
    private volatile Flow.Subscription subscription;
    private volatile boolean closed;
    private volatile Throwable failed;
    private volatile Iterator<ByteBuffer> currentListItr;
    private volatile ByteBuffer currentBuffer;
    private final AtomicBoolean subscribed = new AtomicBoolean();

    private final long readTimeout;

    /**
     * Creates a response body subscriber that emits the response body as a {@link InputStream} while tracking a timeout
     * for each value emitted by the subscription.
     *
     * @param readTimeout The timeout for reading each value emitted by the subscription.
     */
    public InputStreamTimeoutResponseSubscriber(long readTimeout) {
        this.buffers = new ArrayBlockingQueue<>(2);
        this.readTimeout = readTimeout;
    }

    @Override
    public CompletionStage<InputStream> getBody() {
        // Returns the stream immediately, before the
        // response body is received.
        // This makes it possible for sendAsync().get().body()
        // to complete before the response body is received.
        return CompletableFuture.completedStage(this);
    }

    // Returns the current byte buffer to read from.
    // If the current buffer has no remaining data, this method will take the
    // next buffer from the buffers queue, possibly blocking until
    // a new buffer is made available through the Flow API, or the
    // end of the flow has been reached.
    private ByteBuffer current() throws IOException {
        while (currentBuffer == null || !currentBuffer.hasRemaining()) {
            // Check whether the stream is closed or exhausted
            if (closed || failed != null) {
                throw new IOException("closed", failed);
            }
            if (currentBuffer == LAST_BUFFER)
                break;

            try {
                if (currentListItr == null || !currentListItr.hasNext()) {
                    // Take a new list of buffers from the queue, blocking
                    // if none is available yet...

                    LOGGER.verbose("Taking list of Buffers");
                    List<ByteBuffer> lb = buffers.take();
                    currentListItr = lb.iterator();
                    LOGGER.verbose("List of Buffers Taken");

                    // Check whether an exception was encountered upstream
                    if (closed || failed != null)
                        throw new IOException("closed", failed);

                    // Check whether we're done.
                    if (lb == LAST_LIST) {
                        currentListItr = null;
                        currentBuffer = LAST_BUFFER;
                        break;
                    }

                    // Request another upstream item ( list of buffers )
                    Flow.Subscription s = subscription;
                    if (s != null) {
                        LOGGER.verbose("Increased demand by 1");
                        s.request(1);
                    }
                    assert currentListItr != null;
                    if (lb.isEmpty())
                        continue;
                }
                assert currentListItr != null;
                assert currentListItr.hasNext();
                LOGGER.verbose("Next Buffer");
                currentBuffer = currentListItr.next();
            } catch (InterruptedException ex) {
                try {
                    close();
                } catch (IOException ignored) {
                }
                Thread.currentThread().interrupt();
                throw new IOException(ex);
            }
        }
        assert currentBuffer == LAST_BUFFER || currentBuffer.hasRemaining();
        return currentBuffer;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, bytes.length);
        if (len == 0) {
            return 0;
        }
        // get the buffer to read from, possibly blocking if
        // none is available
        ByteBuffer buffer;
        if ((buffer = current()) == LAST_BUFFER)
            return -1;

        // don't attempt to read more than what is available
        // in the current buffer.
        int read = Math.min(buffer.remaining(), len);
        assert read > 0 && read <= buffer.remaining();

        // buffer.get() will do the boundary check for us.
        buffer.get(bytes, off, read);
        return read;
    }

    @Override
    public int read() throws IOException {
        ByteBuffer buffer;
        if ((buffer = current()) == LAST_BUFFER)
            return -1;
        return buffer.get() & 0xFF;
    }

    @Override
    public int available() throws IOException {
        // best effort: returns the number of remaining bytes in
        // the current buffer if any, or 1 if the current buffer
        // is null or empty but the queue or current buffer list
        // are not empty. Returns 0 otherwise.
        if (closed)
            return 0;
        int available = 0;
        ByteBuffer current = currentBuffer;
        if (current == LAST_BUFFER)
            return 0;
        if (current != null)
            available = current.remaining();
        if (available != 0)
            return available;
        Iterator<?> iterator = currentListItr;
        if (iterator != null && iterator.hasNext())
            return 1;
        if (buffers.isEmpty())
            return 0;
        return 1;
    }

    @Override
    public void onSubscribe(Flow.Subscription s) {
        Objects.requireNonNull(s);
        LOGGER.verbose("onSubscribe called");
        try {
            if (!subscribed.compareAndSet(false, true)) {
                LOGGER.verbose("Already subscribed: canceling");
                s.cancel();
            } else {
                // check whether the stream is already closed.
                // if so, we should cancel the subscription
                // immediately.
                boolean closed;
                synchronized (this) {
                    closed = this.closed;
                    if (!closed) {
                        this.subscription = s;
                        // should contain at least 2, unless closed or failed.
                        assert buffers.remainingCapacity() > 1 || failed != null
                            : "buffers capacity: " + buffers.remainingCapacity() + ", closed: " + closed
                                + ", terminated: " + buffers.contains(LAST_LIST) + ", failed: " + failed;
                    }
                }
                if (closed) {
                    LOGGER.verbose("Already closed: canceling");
                    s.cancel();
                    return;
                }
                LOGGER.verbose("onSubscribe: requesting " + Math.max(1, buffers.remainingCapacity() - 1));
                s.request(Math.max(1, buffers.remainingCapacity() - 1));
            }
        } catch (Throwable t) {
            failed = t;
            LOGGER.verbose("onSubscribe failed", t);
            try {
                close();
            } catch (IOException x) {
                // OK
            } finally {
                onError(t);
            }
        }
    }

    @Override
    public void onNext(List<ByteBuffer> t) {
        Objects.requireNonNull(t);
        try {
            LOGGER.verbose("next item received");
            if (!buffers.offer(t)) {
                throw new IllegalStateException("queue is full");
            }
            LOGGER.verbose("item offered");
        } catch (Throwable ex) {
            failed = ex;
            try {
                close();
            } catch (IOException ex1) {
                // OK
            } finally {
                onError(ex);
            }
        }
    }

    @Override
    public void onError(Throwable thrwbl) {
        LOGGER.verbose("onError called: " + thrwbl);
        subscription = null;
        failed = Objects.requireNonNull(thrwbl);
        // The client process that reads the input stream might
        // be blocked in queue.take().
        // Tries to offer LAST_LIST to the queue. If the queue is
        // full we don't care if we can't insert this buffer, as
        // the client can't be blocked in queue.take() in that case.
        // Adding LAST_LIST to the queue is harmless, as the client
        // should find failed != null before handling LAST_LIST.
        buffers.offer(LAST_LIST);
    }

    @Override
    public void onComplete() {
        LOGGER.verbose("onComplete called");
        subscription = null;
        onNext(LAST_LIST);
    }

    @Override
    public void close() throws IOException {
        Flow.Subscription s;
        synchronized (this) {
            if (closed)
                return;
            closed = true;
            s = subscription;
            subscription = null;
        }
        LOGGER.verbose("close called");
        // s will be null if already completed
        try {
            if (s != null) {
                s.cancel();
            }
        } finally {
            buffers.offer(LAST_LIST);
            super.close();
        }
    }
}
