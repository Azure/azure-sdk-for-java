// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.beust.jcommander.Parameter;

/**
 * Represents the count option for performance tests.
 * For example, can be used to indicate the number of resources to create prior to running the test.
 */
public class CountOptions extends PerfStressOptions {
    @Parameter(names = { "-c", "--count" }, description = "Number of items")
    private int count = 10;

    /**
     * Get the configured count for performance test.
     * @return The count.
     */
    public int getCount() {
        return count;
    }
}
