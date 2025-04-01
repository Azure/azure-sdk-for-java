// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch.models;

import com.azure.core.util.logging.ClientLogger;

/**
 * Stores options that configure the operation of methods on Batch client parallel operations.
 */
public class BatchClientParallelOptions {

    private static final ClientLogger LOGGER = new ClientLogger(BatchClientParallelOptions.class);

    private Integer maxConcurrency;

    /**
     * Gets the maximum number of concurrent tasks enabled by this {@link BatchClientParallelOptions} instance.
     * If not set, it returns null, indicating no specific limit is set.
     * @return The maximum number of concurrent tasks or null if not set.
     */
    public Integer getMaxConcurrency() {
        return this.maxConcurrency;
    }

    /**
     * Sets the maximum number of concurrent tasks enabled by this {@link BatchClientParallelOptions} instance.
     * Pass null to make the maximum degree of parallelism optional.
     * @param maxConcurrency the maximum number of concurrent tasks or null.
     * @return The instance of {@link BatchClientParallelOptions}.
     * @throws IllegalArgumentException Exception thrown if maxConcurrency is less than 1 and not null.
     */
    public BatchClientParallelOptions setMaxConcurrency(Integer maxConcurrency) {
        if (maxConcurrency != null && maxConcurrency <= 0) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("maxConcurrency must be greater than 0 or null"));
        }
        this.maxConcurrency = maxConcurrency;
        return this;
    }

    /**
     * Initializes a new instance of the {@link BatchClientParallelOptions} class.
     */
    public BatchClientParallelOptions() {
    }

}
