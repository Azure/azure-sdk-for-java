// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

/**
 * Represents Mock Error Context class for {@link MockEventProcessor}
 */
public class MockErrorContext {
    private final Throwable throwable;
    private final int partition;

    /**
     * Creates an instance of the Mock Error Context
     *
     * @param partition the target partition
     * @param throwable the error
     */
    public MockErrorContext(int partition, Throwable throwable) {
        this.throwable = throwable;
        this.partition = partition;
    }

    /**
     * Get the error
     *
     * @return the throwable error
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Get the target partition
     *
     * @return the target partition
     */
    public int getPartition() {
        return partition;
    }
}
