// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;

/**
 * Options for document index operations.
 */
@Fluent
public class IndexDocumentsOptions {
    private boolean throwOnAnyError = false;

    /**
     * Returns whether an exception will be thrown if any operation in the batch fails.
     *
     * @return Flag indicating if an exception will be thrown if any operation in the batch fails.
     */
    public boolean throwOnAnyError() {
        return throwOnAnyError;
    }

    /**
     * Sets whether an exception is thrown if any operation in a batch fails.
     *
     * @param throwOnAnyError Flag indicating whether to throw on batch operation failure.
     * @return The updated IndexDocumentsOptions object.
     */
    public IndexDocumentsOptions setThrowOnAnyError(boolean throwOnAnyError) {
        this.throwOnAnyError = throwOnAnyError;
        return this;
    }
}
