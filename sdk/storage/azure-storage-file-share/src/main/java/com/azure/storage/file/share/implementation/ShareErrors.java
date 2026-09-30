// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation;

import com.azure.core.util.logging.ClientLogger;

/**
 * Shared checks for operations that require a particular resource addressing mode.
 */
public final class ShareErrors {
    private static final ClientLogger LOGGER = new ClientLogger(ShareErrors.class);

    private ShareErrors() {
    }

    /**
     * Validates a file ID accepted by a client factory or builder.
     *
     * @param fileId The ID to validate.
     * @throws IllegalArgumentException If the ID is null or blank.
     */
    public static void validateFileId(String fileId) {
        if (fileId == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'fileId' cannot be null."));
        }
        if (fileId.trim().isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'fileId' cannot be empty."));
        }
    }

    /**
     * Throws if the client does not address its resource by file ID.
     *
     * @param fileId The client's file ID.
     * @param operationName The operation name to report on failure.
     * @throws IllegalStateException If the client is not file-ID-addressed.
     */
    public static void assertFileIdAddressed(String fileId, String operationName) {
        if (fileId == null || fileId.isEmpty()) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException(operationName + " requires a file-ID-addressed client."));
        }
    }

    /**
     * Throws if the client addresses its resource by file ID.
     *
     * @param fileId The client's file ID.
     * @param operationName The operation name to report on failure.
     * @throws IllegalStateException If the client is file-ID-addressed.
     */
    public static void assertNotFileIdAddressed(String fileId, String operationName) {
        if (fileId != null && !fileId.isEmpty()) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException(operationName + " is not supported for a file-ID-addressed client."));
        }
    }
}
