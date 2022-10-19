// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

import org.springframework.core.NestedRuntimeException;

/**
 * The Azure Storage specific {@link NestedRuntimeException}.
 */
public final class StorageRuntimeException extends NestedRuntimeException {

    /**
     * Creates a new instance of {@link StorageRuntimeException}.
     *
     * @param msg the error message
     */
    public StorageRuntimeException(String msg) {
        super(msg);
    }

    /**
     * Creates a new instance of {@link StorageRuntimeException}.
     *
     * @param msg the error message
     * @param cause the cause
     */
    public StorageRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
