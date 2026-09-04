// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;

import java.io.Closeable;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * An {@link IterableStream} that provides deterministic cleanup for resource-backed iteration.
 *
 * <p>This stream takes ownership of the resource associated with the supplied iterable. After construction, manage the
 * resource's lifetime through this stream rather than closing the resource directly.</p>
 *
 * <p>Iteration behavior, including whether multiple iterators are supported, is determined by the supplied iterable.
 * Use this stream in a try-with-resources statement to ensure that the owned resource is released whether iteration
 * completes normally or ends early. Closing the stream is safe to do more than once.</p>
 *
 * <p><strong>Code sample</strong></p>
 *
 * <!-- src_embed com.azure.core.util.closeableIterableStream.iterate -->
 * <pre>
 * BufferedReader responseBody = getResponseBody&#40;&#41;;
 * Iterable&lt;String&gt; eventData = parseEventData&#40;responseBody&#41;;
 *
 * try &#40;CloseableIterableStream&lt;String&gt; events = new CloseableIterableStream&lt;&gt;&#40;eventData, responseBody&#41;&#41; &#123;
 *     for &#40;String event : events&#41; &#123;
 *         System.out.printf&#40;&quot;Event data: %s%n&quot;, event&#41;;
 *     &#125;
 * &#125;
 * </pre>
 * <!-- end com.azure.core.util.closeableIterableStream.iterate -->
 *
 * @param <T> The type of values in this stream.
 */
public final class CloseableIterableStream<T> extends IterableStream<T> implements Closeable {
    private static final ClientLogger LOGGER = new ClientLogger(CloseableIterableStream.class);

    private final Closeable ownedResource;
    private boolean closed;

    /**
     * Creates an iterable stream that owns the resource associated with its iteration.
     *
     * @param iterable The iterable that reads values from, or is otherwise associated with, the owned resource.
     * @param ownedResource The resource whose ownership is transferred to this stream and which is released when this
     * stream is closed.
     * @throws NullPointerException If {@code iterable} or {@code ownedResource} is {@code null}.
     */
    public CloseableIterableStream(Iterable<T> iterable, Closeable ownedResource) {
        super(iterable);
        this.ownedResource = Objects.requireNonNull(ownedResource, "'ownedResource' cannot be null.");
    }

    /**
     * Gets a Java stream of values that closes this iterable stream when the returned stream is closed.
     *
     * <p>Closing the returned Java stream also closes this iterable stream. If this iterable stream isn't managed with
     * try-with-resources, the returned Java stream must be explicitly closed to release the owned resource. Terminal
     * operations don't automatically close it.</p>
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
            ownedResource.close();
        } catch (Exception exception) {
            throw LOGGER
                .logExceptionAsError(new IllegalStateException("Failed to close the iterable stream.", exception));
        }
    }
}
