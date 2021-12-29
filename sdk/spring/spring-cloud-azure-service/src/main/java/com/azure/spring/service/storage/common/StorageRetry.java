// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.common;

import com.azure.spring.core.aware.RetryAware;

/**
 *  Interface to be implemented by classes that wish to describe storage sdks related retry operations.
 */
public interface StorageRetry extends RetryAware.HttpRetry {

    /**
     * Get the secondary host for retry.
     * @return the secondary host.
     */
    String getSecondaryHost();
}
