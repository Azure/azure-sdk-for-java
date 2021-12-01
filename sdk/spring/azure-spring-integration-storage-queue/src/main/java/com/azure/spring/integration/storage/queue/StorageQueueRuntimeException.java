// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.storage.queue;

import org.springframework.core.NestedRuntimeException;

/**
 * Thrown when exception happens during storage queue client.
 */
public class StorageQueueRuntimeException extends NestedRuntimeException {

    /**
     *
     * @param msg The message.
     */
    public StorageQueueRuntimeException(String msg) {
        super(msg);
    }

    /**
     *
     * @param msg The message.
     * @param cause The cause of this exception.
     */
    public StorageQueueRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
