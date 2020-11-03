// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.exception.AzureException;

/**
 * An exception thrown when an operation is interrupted and can be continued later on.
 */
public class DataLakeAclChangeFailedException extends AzureException {

    private final String continuationToken;

    /**
     * Initializes a new instance of DataLakeAclChangeFailedException with a specified error message, and a reference
     * to the inner exception that is the cause of this exception.
     *
     * @param message The message that describes the error.
     * @param e The exception thrown.
     * @param continuationToken The continuation token returned from the previous successful response.
     */
    public DataLakeAclChangeFailedException(String message, Exception e, String continuationToken) {
        super(message, e);
        this.continuationToken = continuationToken;
    }

    /**
     * Initializes a new instance of DataLakeAclChangeFailedException with a specified error message, HTTP status code,
     * error code, and a reference to the inner exception that is the cause of this exception.
     *
     * @param message The message that describes the error.
     * @param e The exception thrown from the failed request.
     * @param continuationToken The continuation token returned from the previous successful response.
     */
    public DataLakeAclChangeFailedException(String message, DataLakeStorageException e, String continuationToken) {
        super(message, e);
        this.continuationToken = continuationToken;
    }

    /**
     * @return the continuation token to resume a datalake recursive acl function.
     */
    public String getContinuationToken() {
        return continuationToken;
    }
}
