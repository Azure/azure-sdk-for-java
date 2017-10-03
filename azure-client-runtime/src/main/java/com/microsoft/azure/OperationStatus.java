/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

/**
 * The current state of polling for the result of a long running operation.
 * @param <T> The type of value that will be returned from the long running operation.
 */
public class OperationStatus<T> {
    private PollStrategy pollStrategy;
    private T result;

    /**
     * Create a new OperationStatus with the provided PollStrategy.
     * @param pollStrategy The polling strategy that the OperationStatus will use to check the
     *                     progress of a long running operation.
     */
    OperationStatus(PollStrategy pollStrategy) {
        this.pollStrategy = pollStrategy;
    }

    /**
     * Create a new OperationStatus with the provided result.
     * @param result The final result of a long running operation.
     */
    OperationStatus(T result) {
        this.result = result;
    }

    /**
     * Get whether or not the long running operation is done.
     * @return Whether or not the long running operation is done.
     */
    public boolean isDone() {
        return pollStrategy == null;
    }

    /**
     * If the long running operation is done, get the result of the operation. If the operation is
     * not done, then return null.
     * @return The result of the operation, or null if the operation isn't done yet.
     */
    public T result() {
        return result;
    }
}
