// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import reactor.core.publisher.MonoSink;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An internal {@link EventData} used by {@link EventHubBufferedPartitionProducer} to signal flush request.
 */
final class FlushSignal extends EventData {
    private static final AtomicLong COUNTER = new AtomicLong(0);
    private final MonoSink<Void> sink;
    private final long id = COUNTER.getAndIncrement();

    /**
     * Creates a new instance of {@link FlushSignal}.
     *
     * @param sink the sink to signal when the flush operation has completed.
     */
    FlushSignal(MonoSink<Void> sink) {
        super("FLUSH_SIGNAL");
        this.sink = Objects.requireNonNull(sink, "'sink' cannot be null");
    }

    /**
     * Gets the id of this flush signal (for logging purpose).
     *
     * @return the id of this flush signal.
     */
    long getId() {
        return id;
    }

    /**
     * method to call when the flush operation requested by this signal has completed.
     */
    void flushed() {
        sink.success();
    }

    @Override
    public boolean equals(Object o) {
        // The 'FlushSignal' usage is internal and is not used for equality or hash, but override for checkstyle.
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return this.id == ((FlushSignal) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
