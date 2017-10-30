/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2;

import com.microsoft.rest.v2.RestException;

/**
 * The current state of polling for the result of a long running operation.
 * @param <T> The type of value that will be returned from the long running operation.
 */
public class OperationStatus<T> {
    private final PollStrategy pollStrategy;
    private final T result;
    private final RestException error;
    private final String status;

    /**
     * Create a new OperationStatus with the provided PollStrategy.
     * @param pollStrategy The polling strategy that the OperationStatus will use to check the
     *                     progress of a long running operation.
     */
    OperationStatus(PollStrategy pollStrategy) {
        this.pollStrategy = pollStrategy;
        this.result = null;
        this.error = null;
        this.status = pollStrategy.status();
    }

    /**
     * Create a new OperationStatus with the provided result.
     * @param result The final result of a long running operation.
     */
    OperationStatus(T result, String provisioningState) {
        this.pollStrategy = null;
        this.result = result;
        this.error = null;
        this.status = provisioningState;
    }

    OperationStatus(RestException error, String provisioningState) {
        this.pollStrategy = null;
        this.result = null;
        this.error = error;
        this.status = provisioningState;
    }

    /**
     * @return Whether or not the long running operation is done.
     */
    public boolean isDone() {
        return pollStrategy == null;
    }

    /**
     * @return the current status of the long running operation.
     */
    public String status() {
        return status;
    }

    /**
     * If the long running operation is done, get the result of the operation. If the operation is
     * not done or if the operation failed, then return null.
     * @return The result of the operation, or null if the operation isn't done yet or if it failed.
     */
    public T result() {
        return result;
    }

    /**
     * If the long running operation failed, get the error that occurred. If the operation is not
     * done or did not fail, then return null.
     * @return The error of the operation, or null if the operation isn't done or didn't fail.
     */
    public RestException error() {
        return error;
    }
}
