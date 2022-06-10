// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.common;

import com.azure.spring.cloud.core.provider.RetryOptionsProvider;

import java.time.Duration;

/**
 *  Interface to be implemented by classes that wish to describe storage sdks related retry operations.
 */
public interface StorageRetry extends RetryOptionsProvider.RetryOptions {

    /**
     * Get the secondary host for retry.
     * @return the secondary host.
     */
    String getSecondaryHost();

    /**
     * Amount of time to wait until a timeout.
     * @return the timeout.
     */
    Duration getTryTimeout();


}
