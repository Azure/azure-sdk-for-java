/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

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
