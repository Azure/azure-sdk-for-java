// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import io.reactivex.Flowable;

/**
 * An {@code IProgressReceiver} is an object that can be used to report progress on network transfers. When specified on
 * transfer operations, the {@code reportProgress} method will be called periodically with the total number of bytes
 * transferred. The user may configure this method to report progress in whatever format desired. It is recommended
 * that this type be used in conjunction with
 * {@link ProgressReporter#addProgressReporting(Flowable, IProgressReceiver)}.
 */
public interface IProgressReceiver {

    /**
     * The callback function invoked as progress is reported.
     *
     * @param bytesTransferred
     *      The total number of bytes transferred during this transaction.
     */
    void reportProgress(long bytesTransferred);
}
