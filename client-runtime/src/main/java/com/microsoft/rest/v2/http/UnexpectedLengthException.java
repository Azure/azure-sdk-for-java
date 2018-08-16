/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

/**
 * Indicates that a stream emitted an unexpected number of bytes.
 */
public class UnexpectedLengthException extends IllegalStateException {
    private final long bytesRead;
    private final long bytesExpected;

    /**
     * Creates an UnexpectedLengthException.
     * @param message the message
     * @param bytesRead the number of bytes actually read in the stream
     * @param bytesExpected the number of bytes expected to be read in the stream
     */
    public UnexpectedLengthException(String message, long bytesRead, long bytesExpected) {
        super(message);
        this.bytesRead = bytesRead;
        this.bytesExpected = bytesExpected;
    }

    /**
     * @return the number of bytes actually read in the stream
     */
    public long bytesRead() {
        return bytesRead;
    }

    /**
     * @return the number of bytes expected to be read in the stream
     */
    public long bytesExpected() {
        return bytesExpected;
    }
}
