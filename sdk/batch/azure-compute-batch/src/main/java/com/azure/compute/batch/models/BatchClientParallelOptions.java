// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch.models;

import com.azure.core.util.logging.ClientLogger;

/**
 * Stores options that configure the operation of methods on Batch client parallel operations.
 */
public class BatchClientParallelOptions {

    private static final ClientLogger LOGGER = new ClientLogger(BatchClientParallelOptions.class);

    private int maxDegreeOfParallelism;

    /**
     * Gets the maximum number of concurrent tasks enabled by this {@link BatchClientParallelOptions} instance.
     *
     * The default value is 1.
     * @return The maximum number of concurrent tasks.
     */
    public int maxDegreeOfParallelism() {
        return this.maxDegreeOfParallelism;
    }

    /**
     * Sets the maximum number of concurrent tasks enabled by this {@link BatchClientParallelOptions} instance.
     *
     * @param maxDegreeOfParallelism the maximum number of concurrent tasks.
     * @return The instance of {@link BatchClientParallelOptions}.
     * @throws IllegalArgumentException Exception thrown if maxDegreeOfParallelism is not a valid number.
     */
    public BatchClientParallelOptions setMaxDegreeOfParallelism(int maxDegreeOfParallelism) {
        if (maxDegreeOfParallelism > 0) {
            this.maxDegreeOfParallelism = maxDegreeOfParallelism;
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("maxDegreeOfParallelism"));
        }
        return this;
    }

    /**
     * Initializes a new instance of the {@link BatchClientParallelOptions} class with default values.
     */
    public BatchClientParallelOptions() {
        this.maxDegreeOfParallelism = 1;
    }

    /**
     * Initializes a new instance of the {@link BatchClientParallelOptions} class.
     *
     * @param maxDegreeOfParallelism the maximum number of concurrent tasks.
     */
    public BatchClientParallelOptions(int maxDegreeOfParallelism) {
        this.maxDegreeOfParallelism = maxDegreeOfParallelism;
    }

}
