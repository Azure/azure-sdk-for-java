// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

/**
 * A {@code ProgressReceiver} is an object that can be used to report progress on network transfers. When specified on
 * transfer operations, the {@code reportProgress} method will be called periodically with the total number of bytes
 * transferred. The user may configure this method to report progress in whatever format desired.
 * Note that any method accepting a {@link ParallelDownloadOptions} will use
 * the {@code ProgressReceiver} specified there and will handle the logic to coordinate the reporting between parallel
 * operations.
 */
@FunctionalInterface
public interface ProgressReceiver {

    /**
     * The callback function invoked as progress is reported.
     *
     * @param bytesTransferred The total number of bytes transferred during this transaction.
     */
    void reportProgress(long bytesTransferred);
}
