/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;


/**
 * The response body contains the status of the specified asynchronous
 * operation, indicating whether it has succeeded, is inprogress, or has
 * failed. Note that this status is distinct from the HTTP status code
 * returned for the Get Operation Status operation itself. If the
 * asynchronous operation succeeded, the response body includes the HTTP
 * status code for the successful request. If the asynchronous operation
 * failed, the response body includes the HTTP status code for the failed
 * request and error information regarding the failure.
 */
public class AzureAsyncOperationResult {
    /**
     * Status of the AzureAsuncOperation. Possible values include:
     * 'InProgress', 'Succeeded', 'Failed'.
     */
    private String status;

    /**
     * The error property.
     */
    private Error error;

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public String status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the AzureAsyncOperationResult object itself.
     */
    public AzureAsyncOperationResult withStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * Get the error value.
     *
     * @return the error value
     */
    public Error error() {
        return this.error;
    }

    /**
     * Set the error value.
     *
     * @param error the error value to set
     * @return the AzureAsyncOperationResult object itself.
     */
    public AzureAsyncOperationResult withError(Error error) {
        this.error = error;
        return this;
    }

}
