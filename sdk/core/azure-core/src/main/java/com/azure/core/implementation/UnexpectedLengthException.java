// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

/**
 * This exception class represents an error when the specified input length doesn't match the data length.
 */
public final class UnexpectedLengthException extends IllegalStateException {
    private final long bytesRead;
    private final long bytesExpected;

    /**
     * Constructor of the UnexpectedLengthException.
     * @param message The message for the exception.
     * @param bytesRead The number of bytes read from resource.
     * @param bytesExpected The number of bytes expected from the receiver.
     */
    public UnexpectedLengthException(String message, long bytesRead, long bytesExpected) {
        super(message);
        this.bytesRead = bytesRead;
        this.bytesExpected = bytesExpected;
    }

    /**
     * @return the number of bytes read from the input
     */
    public long bytesRead() {
        return this.bytesRead;
    }

    /**
     * @return the number of bytes that were expected to be read from the input
     */
    public long bytesExpected() {
        return this.bytesExpected;
    }
}
