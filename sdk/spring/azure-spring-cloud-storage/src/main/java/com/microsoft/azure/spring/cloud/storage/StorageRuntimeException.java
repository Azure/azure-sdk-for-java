/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.storage;

import org.springframework.core.NestedRuntimeException;

/**
 * The Azure Storage specific {@link NestedRuntimeException}.
 *
 * @author Warren Zhu
 */
public class StorageRuntimeException extends NestedRuntimeException {

    public StorageRuntimeException(String msg) {
        super(msg);
    }

    public StorageRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
