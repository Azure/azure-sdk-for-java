// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.aware.authentication;

/**
 * Interface to be implemented by classes that wish to be aware of the key.
 */
public interface KeyAware {

    /**
     * Get the key
     * @return the key
     */
    String getKey();

}
