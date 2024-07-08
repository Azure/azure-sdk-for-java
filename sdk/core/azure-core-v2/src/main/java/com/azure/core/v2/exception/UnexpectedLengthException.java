// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.exception;

/**
 * <p>The {@code UnexpectedLengthException} represents an exception thrown when the specified input length doesn't
 * match the actual data length.</p>
 *
 * <p>This exception is typically thrown when the number of bytes read from an input source does not match the
 * expected number of bytes. This could occur when reading data from a file or a network connection.</p>
 *
 * <p>This class also provides methods to get the number of bytes read from the input and the number of bytes that were
 * expected to be read from the input.</p>
 *
 * @see com.azure.core.exception
 * @see java.lang.IllegalStateException
 */
public final class UnexpectedLengthException extends IllegalStateException {
    /**
     * Number of bytes read from the input.
     */
    private final long bytesRead;

    /**
     * Number of bytes that were expected to be read from the input.
     */
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
     * Gets the number of bytes read from the input.
     *
     * @return the number of bytes read from the input
     */
    public long getBytesRead() {
        return this.bytesRead;
    }

    /**
     * Gets the number of bytes that were expected to be read from the input.
     *
     * @return the number of bytes that were expected to be read from the input
     */
    public long getBytesExpected() {
        return this.bytesExpected;
    }
}
