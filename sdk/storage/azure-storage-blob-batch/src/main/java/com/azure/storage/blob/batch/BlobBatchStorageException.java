// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.storage.blob.models.BlobStorageException;

/**
 * This exception class is an aggregate for {@link BlobStorageException BlobStorageExceptions}. This will contain all
 * exceptions from a single batch operation.
 */
public final class BlobBatchStorageException extends HttpResponseException {
    private final Iterable<BlobStorageException> exceptions;

    BlobBatchStorageException(String message, HttpResponse response, Iterable<BlobStorageException> exceptions) {
        super(message, response);

        this.exceptions = exceptions;
    }

    /**
     * Gets all the exceptions thrown in a single batch request.
     *
     * @return All the exceptions thrown in a single batch request.
     */
    public Iterable<BlobStorageException> getBatchExceptions() {
        return exceptions;
    }
}
