// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

/**
 * An {@code ErrorReceiver} is a class that can be used to report errors on transfers. When specified on transfer
 * operations, the {@code reportError} method will be called whenever errors are encountered. The user may configure
 * this method to report errors in whatever format desired.
 */
public interface ErrorReceiver<T> {

    /**
     * The callback function invoked as errors are reported.
     *
     * @param error The error to report.
     */
    void reportError(T error);

}
