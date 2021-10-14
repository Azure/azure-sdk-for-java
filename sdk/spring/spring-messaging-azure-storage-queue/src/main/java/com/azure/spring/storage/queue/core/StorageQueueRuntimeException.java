// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.storage.queue.core;

import org.springframework.core.NestedRuntimeException;

/**
 * Thrown when exception happens during storage queue client.
 */
public class StorageQueueRuntimeException extends NestedRuntimeException {

    public StorageQueueRuntimeException(String msg) {
        super(msg);
    }

    public StorageQueueRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
