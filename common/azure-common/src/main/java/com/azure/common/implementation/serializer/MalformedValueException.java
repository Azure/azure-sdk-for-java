/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.implementation.serializer;

/**
 * An exception thrown while parsing an invalid input during serialization or deserialization.
 */
public class MalformedValueException extends RuntimeException {
    /**
     * Create a MalformedValueException instance.
     *
     * @param message the exception message
     */
    public MalformedValueException(String message) {
        super(message);
    }

    /**
     * Create a MalformedValueException instance.
     *
     * @param message the exception message
     * @param cause the actual cause
     */
    public MalformedValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
