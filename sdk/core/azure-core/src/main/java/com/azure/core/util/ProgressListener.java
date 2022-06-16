// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

/**
 * A {@link ProgressListener} is an interface that can be used to report progress on network transfers.
 * The {@link #reportProgress(long)} method will be called periodically with the total number of bytes
 * transferred.
 */
public interface ProgressListener {
    /**
     * The callback function invoked as progress is reported.
     *
     * <p>
     * The callback can be called concurrently from multiple threads if reporting spans across multiple
     * requests.
     * </p>
     *
     * @param bytesTransferred The total number of bytes transferred during this transaction.
     */
    void reportProgress(long bytesTransferred);
}
