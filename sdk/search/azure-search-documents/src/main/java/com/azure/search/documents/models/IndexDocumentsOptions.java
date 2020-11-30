// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;

/**
 * Options for document index operations.
 */
@Fluent
public class IndexDocumentsOptions {
    private boolean throwOnAnyError = true;

    /**
     * Returns whether an exception will be thrown if any operation in the batch fails.
     * <p>
     * Default value is {@code true}.
     *
     * @return Flag indicating if an exception will be thrown if any operation in the batch fails.
     */
    public boolean throwOnAnyError() {
        return throwOnAnyError;
    }

    /**
     * Sets whether an exception is thrown if any operation in a batch fails.
     * <p>
     * Default value is {@code true}.
     *
     * @param throwOnAnyError Flag indicating whether to throw on batch operation failure.
     * @return The updated IndexDocumentsOptions object.
     */
    public IndexDocumentsOptions setThrowOnAnyError(boolean throwOnAnyError) {
        this.throwOnAnyError = throwOnAnyError;
        return this;
    }
}
