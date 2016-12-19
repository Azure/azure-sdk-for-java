/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.interceptor;

import com.microsoft.azure.batch.BatchClientBehavior;

/**
 * Stores options that configure the operation of methods on Batch client parallel operations.
 */
public class BatchClientParallelOptions extends BatchClientBehavior {

    private int maxDegreeOfParallelism;

    /**
     * Gets the maximum number of concurrent tasks enabled by this {@link BatchClientParallelOptions} instance.
     *
     * The default value is 1.
     * @return The maximum number of concurrent tasks
     */
    public int maxDegreeOfParallelism() {
        return this.maxDegreeOfParallelism;
    }

    /**
     * Sets the maximum number of concurrent tasks enabled by this {@link BatchClientParallelOptions} instance.
     *
     * @param maxDegreeOfParallelism the maximum number of concurrent tasks
     * @return The instance of {@link BatchClientParallelOptions}
     */
    public BatchClientParallelOptions withMaxDegreeOfParallelism(int maxDegreeOfParallelism) {
        if (maxDegreeOfParallelism > 0) {
            this.maxDegreeOfParallelism = maxDegreeOfParallelism;
        }
        else {
            throw new IllegalArgumentException("maxDegreeOfParallelism");
        }
        return this;
    }

    /**
     * Initializes a new instance of the {@link BatchClientParallelOptions} class with default value.
     */
    public BatchClientParallelOptions() {
        this.maxDegreeOfParallelism = 1;
    }

    /**
     * Initializes a new instance of the {@link BatchClientParallelOptions} class.
     *
     * @param maxDegreeOfParallelism the maximum number of concurrent tasks
     */
    public BatchClientParallelOptions(int maxDegreeOfParallelism) {
        this.maxDegreeOfParallelism = maxDegreeOfParallelism;
    }

}
