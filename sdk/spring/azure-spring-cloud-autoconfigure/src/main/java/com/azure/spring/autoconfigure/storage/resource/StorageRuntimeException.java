// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage.resource;

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
