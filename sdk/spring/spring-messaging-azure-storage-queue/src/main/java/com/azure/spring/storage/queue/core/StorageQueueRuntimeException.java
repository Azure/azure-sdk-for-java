// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.storage.queue.core;

import org.springframework.core.NestedRuntimeException;

/**
 * Thrown when exception happens during storage queue client.
 */
public final class StorageQueueRuntimeException extends NestedRuntimeException {

    /**
     * Construct {@code StorageQueueRuntimeException} with the specified detail message.
     * @param msg the exception information.
     */
    public StorageQueueRuntimeException(String msg) {
        super(msg);
    }

    /**
     * Construct {@code StorageQueueRuntimeException} with the specified detail message and nested exception.
     * @param msg the specified detail message.
     * @param cause the nested exception.
     */
    public StorageQueueRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
