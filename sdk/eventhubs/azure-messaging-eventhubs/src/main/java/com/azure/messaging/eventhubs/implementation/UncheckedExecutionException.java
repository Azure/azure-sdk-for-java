// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import java.util.concurrent.ExecutionException;

/**
 * Adding an unchecked {@link InterruptedException}.
 */
public class UncheckedExecutionException extends RuntimeException {
    /**
     * Creates a new instance.
     *
     * @param error Exception that occurred.
     */
    public UncheckedExecutionException(InterruptedException error) {
        super("Unable to create a new batch because thread was interrupted.", error);
    }

    /**
     * Creates a new instance of the error.
     *
     * @param error Error that occurred.
     */
    public UncheckedExecutionException(ExecutionException error) {
        super("Unable to create a new batch because task was aborted.", error);
    }
}
