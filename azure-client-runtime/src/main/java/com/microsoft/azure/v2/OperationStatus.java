/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2;

/**
 * The status of a long running operation. This generally is created from the result of polling for
 * whether a long running operation is done or not.
 * @param <T>
 */
public final class OperationStatus<T> {
    private final boolean isDone;
    private final T result;

    private OperationStatus(boolean isDone, T result) {
        this.isDone = isDone;
        this.result = result;
    }

    /**
     * Get whether or not the long running operation is done.
     * @return Whether or not the long running operation is done.
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * If the long running operation is done, get the result of the operation. If the operation is
     * not done, then return null.
     * @return The result of the operation, or null if the operation isn't done yet.
     */
    public T result() {
        return result;
    }

    /**
     * Get an OperationStatus that represents a long running operation that is still in progress.
     * @param <T> The type of result that the long running operation will return.
     * @return An OperationStatus that represents a long running operation that is still in
     * progress.
     */
    public static <T> OperationStatus<T> inProgress() {
        return new OperationStatus<>(false, null);
    }

    /**
     * Get an OperationStatus that represents a long running operation that has completed.
     * @param result The result of the long running operation.
     * @param <T> The type of result that the long running operation will return.
     * @return An OperationStatus that represents a long running operation that has completed.
     */
    public static <T> OperationStatus<T> completed(T result) {
        return new OperationStatus<>(true, result);
    }
}
