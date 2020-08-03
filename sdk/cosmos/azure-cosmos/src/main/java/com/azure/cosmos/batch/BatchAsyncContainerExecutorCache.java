// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.batch.BatchRequestResponseConstant.MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES;
import static com.azure.cosmos.batch.BatchRequestResponseConstant.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;

/**
 * Cache to create and share Executor instances across the client's lifetime.
 */
public final class BatchAsyncContainerExecutorCache implements AutoCloseable {

    private final ConcurrentHashMap<String, BatchAsyncContainerExecutor> executors = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean();

    public BatchAsyncContainerExecutor getExecutorForContainer(final CosmosAsyncContainer container) {

        checkState(!this.closed.get(), "cache closed");

        checkNotNull(container, "expected non-null container");

        final boolean allowBulkExecution = true; //clientContext.getConnectionPolicy().getAllowBulkExecution();
        checkState(allowBulkExecution, "expected client connection policy to allow bulk execution");

        return this.executors.computeIfAbsent(BridgeInternal.getLink(container), k ->
            new BatchAsyncContainerExecutor(
                container,
                MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST,
                MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES)
        );
    }

    /**
     * Closes this {@link BatchAsyncContainerExecutorCache} instance.
     *
     * @throws IOException if the close fails due to an input/output error.
     */
    public void close() throws IOException {
        if (this.closed.compareAndSet(false, true)) {
            for (BatchAsyncContainerExecutor executor : this.executors.values()) {
                executor.close();
            }
            this.executors.clear();
        }
    }
}
