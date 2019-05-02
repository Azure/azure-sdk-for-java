// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.exception;

/**
 * An exception thrown when a Swagger interface defines a method with an invalid return
 * type.
 */
public class InvalidReturnTypeException extends RuntimeException {
    /**
     * Create a new InvalidReturnTypeException with the provided message.
     * @param message The message for this exception.
     */
    public InvalidReturnTypeException(String message) {
        super(message);
    }
}
