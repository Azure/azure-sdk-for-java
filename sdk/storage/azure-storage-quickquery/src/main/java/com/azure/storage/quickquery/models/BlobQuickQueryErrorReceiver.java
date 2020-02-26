// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery.models;

/**
 * A {@code QuickQueryErrorReceiver} is an object that can be used to report errors on network transfers. When
 * specified on transfer operations, the {@code reportError} method will be whenever non-fatal errors are parsed
 * in the response. The user may configure this method to report errors in whatever format desired.
 */
public interface BlobQuickQueryErrorReceiver {

    /**
     * The callback function invoked as non-fatal errors are reported.
     *
     * @param nonFatalError {@link BlobQuickQueryError}.
     */
    void reportError(BlobQuickQueryError nonFatalError);
}
