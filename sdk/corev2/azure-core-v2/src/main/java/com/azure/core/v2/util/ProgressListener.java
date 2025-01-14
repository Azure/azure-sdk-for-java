// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util;

/**
 * A {@link ProgressListener} is an interface that can be used to listen to the progress of the I/O transfers.
 * The {@link #handleProgress(long)} method will be called periodically with the total progress accumulated
 * at the given point of time.
 *
 * <p>
 * <strong>Code samples</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.util.ProgressReportingE2ESample -->
 * <!-- end com.azure.core.util.ProgressReportingE2ESample -->
 */
@FunctionalInterface
public interface ProgressListener {
    /**
     * The callback function invoked as progress is reported.
     *
     * <p>
     * The callback can be called concurrently from multiple threads if reporting spans across multiple
     * requests. The implementor must not perform thread blocking operations in the handler code.
     * </p>
     *
     * @param progress The total progress at the current point of time.
     */
    void handleProgress(long progress);
}
