// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.models;

/**
 * Exception thrown when parsing Arrow ListBlobs payloads fails or encounters unsupported content.
 */
public final class BlobListArrowParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a message.
     *
     * @param message parse failure details.
     */
    public BlobListArrowParseException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and cause.
     *
     * @param message parse failure details.
     * @param cause originating cause.
     */
    public BlobListArrowParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
