// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage;

import com.azure.perf.test.core.PerfStressOptions;

/**
 * Common utilities used by all Storage performance tests.
 */
public final class StoragePerfUtils {
    /**
     * The default buffer size used when performing a download test.
     */
    public static final int DEFAULT_DOWNLOAD_BUFFER_SIZE = 4 * 1024 * 1024;

    /**
     * Gets the dynamic download buffer size based on the size of the blob or file being used in the performance tests,
     * or more generally the value of {@link PerfStressOptions#getSize()}.
     * <p>
     * This will return the minimum ({@link Math#min(long, long)}) of the {@link #DEFAULT_DOWNLOAD_BUFFER_SIZE} and the
     * blob or file size. The aim is to reduce the size of buffers being used in the performance test to make
     * allocations by the SDK clearer when capturing heap dumps or profiles.
     *
     * @param testingSize The size of the blob or file being used in the performance test.
     * @return The dynamic download buffer size.
     */
    public static int getDynamicDownloadBufferSize(long testingSize) {
        return (int) Math.min(testingSize, DEFAULT_DOWNLOAD_BUFFER_SIZE);
    }
}
