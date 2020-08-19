// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.storage.queue;

import org.springframework.core.NestedRuntimeException;

public class StorageQueueRuntimeException extends NestedRuntimeException {

    public StorageQueueRuntimeException(String msg) {
        super(msg);
    }

    public StorageQueueRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
