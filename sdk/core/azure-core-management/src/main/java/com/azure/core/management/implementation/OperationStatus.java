// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.swagger.OperationDescription;

/**
 * The current state of polling for the result of a long running operation.
 * @param <T> The type of value that will be returned from the long running operation.
 */
public class OperationStatus<T> {
    private final PollStrategy pollStrategy;
    private final HttpRequest originalHttpRequest;
    private final T result;
    private final HttpResponseException error;
    private final String status;

    /**
     * Create a new OperationStatus with the provided PollStrategy.
     * @param pollStrategy The polling strategy that the OperationStatus will use to check the
     *                     progress of a long running operation.
     */
    OperationStatus(PollStrategy pollStrategy, HttpRequest originalHttpRequest) {
        this.originalHttpRequest = originalHttpRequest;
        this.pollStrategy = pollStrategy;
        this.result = null;
        this.error = null;
        this.status = pollStrategy.getStatus();
    }

    /**
     * Create a new OperationStatus with the provided result.
     * @param result The final result of a long running operation.
     */
    OperationStatus(T result, String provisioningState) {
        this.pollStrategy = null;
        this.originalHttpRequest = null;
        this.result = result;
        this.error = null;
        this.status = provisioningState;
    }

    OperationStatus(HttpResponseException error, String provisioningState) {
        this.pollStrategy = null;
        this.originalHttpRequest = null;
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
    public String getStatus() {
        return status;
    }

    /**
     * If the long running operation is done, get the result of the operation. If the operation is
     * not done or if the operation failed, then return null.
     * @return The result of the operation, or null if the operation isn't done yet or if it failed.
     */
    public T getResult() {
        return result;
    }

    /**
     * If the long running operation failed, get the error that occurred. If the operation is not
     * done or did not fail, then return null.
     * @return The error of the operation, or null if the operation isn't done or didn't fail.
     */
    public HttpResponseException getError() {
        return error;
    }

    /**
     * Builds an object that can be used to resume the polling of the operation.
     * @return The OperationDescription.
     */
    public OperationDescription buildDescription() {
        if (this.isDone()) {
            return null;
        }

        return new OperationDescription(
                this.pollStrategy.getMethodParser().getFullyQualifiedMethodName(),
                this.pollStrategy.getStrategyData(),
                this.originalHttpRequest);
    }

}
