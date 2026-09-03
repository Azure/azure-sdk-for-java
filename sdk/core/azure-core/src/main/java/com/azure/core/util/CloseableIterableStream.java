// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;

import java.io.Closeable;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * An {@link IterableStream} that owns a closeable resource.
 *
 * <p>This stream must be closed after use. Use it in a try-with-resources statement to ensure the owned resource is
 * released whether iteration completes normally or ends early. Closing the stream is safe to do more than once.</p>
 *
 * <p><strong>Code sample</strong></p>
 *
 * <!-- src_embed com.azure.core.util.closeableIterableStream.iterate -->
 * <pre>
 * try &#40;CloseableIterableStream&lt;ServerSentEvent&lt;String&gt;&gt; events = response&#41; &#123;
 *     for &#40;ServerSentEvent&lt;String&gt; event : events&#41; &#123;
 *         System.out.printf&#40;&quot;Event '%s': %s%n&quot;, event.getEvent&#40;&#41;, event.getData&#40;&#41;&#41;;
 *     &#125;
 * &#125;
 * </pre>
 * <!-- end com.azure.core.util.closeableIterableStream.iterate -->
 *
 * @param <T> The type of values in this stream.
 */
public final class CloseableIterableStream<T> extends IterableStream<T> implements Closeable {
    private static final ClientLogger LOGGER = new ClientLogger(CloseableIterableStream.class);

    private final Closeable closeable;
    private boolean closed;

    /**
     * Creates an instance with the given iterable and closeable resource.
     *
     * @param iterable The values to iterate over.
     * @param closeable The resource to release when this stream is closed.
     * @throws NullPointerException If {@code iterable} or {@code closeable} is {@code null}.
     */
    public CloseableIterableStream(Iterable<T> iterable, Closeable closeable) {
        super(iterable);
        this.closeable = Objects.requireNonNull(closeable, "'closeable' cannot be null.");
    }

    /**
     * Gets a Java stream of values that closes this iterable stream when the returned stream is closed.
     *
     * @return A Java stream of values.
     */
    @Override
    public Stream<T> stream() {
        return super.stream().onClose(this::close);
    }

    /**
     * Releases the resource owned by this stream.
     *
     * <p>This method is idempotent.</p>
     */
    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }

        closed = true;
        try {
            closeable.close();
        } catch (Exception exception) {
            throw LOGGER
                .logExceptionAsError(new IllegalStateException("Failed to close the iterable stream.", exception));
        }
    }
}
