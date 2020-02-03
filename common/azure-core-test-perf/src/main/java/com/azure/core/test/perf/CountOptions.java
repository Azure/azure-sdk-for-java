// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.perf;

import com.beust.jcommander.Parameter;

/**
 * Represents the count option for performance tests.
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
