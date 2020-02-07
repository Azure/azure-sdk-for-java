// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.beust.jcommander.Parameter;

/**
 * Represents the size option for performance test.
 */
public class SizeOptions extends PerfStressOptions {
    @Parameter(names = { "-s", "--size" }, description = "Size of payload (in bytes)")
    private long size = 10 * 1024;

    /**
     * Get the configured size option for performance test.
      * @return The size.
     */
    public long getSize() {
        return size;
    }
}
