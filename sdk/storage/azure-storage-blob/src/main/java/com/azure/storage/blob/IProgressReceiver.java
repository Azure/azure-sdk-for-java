// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import reactor.core.publisher.Flux;

/**
 * An {@code IProgressReceiver} is an object that can be used to report progress on network transfers. When specified on
 * transfer operations, the {@code reportProgress} method will be called periodically with the total number of bytes
 * transferred. The user may configure this method to report progress in wha tever format desired. It is recommended
 * that this type be used in conjunction with {@link ProgressReporter#addProgressReporting(Flux, IProgressReceiver)}.
 */
interface IProgressReceiver {

    /**
     * The callback function invoked as progress is reported.
     *
     * @param bytesTransferred The total number of bytes transferred during this transaction.
     */
    void reportProgress(long bytesTransferred);
}
