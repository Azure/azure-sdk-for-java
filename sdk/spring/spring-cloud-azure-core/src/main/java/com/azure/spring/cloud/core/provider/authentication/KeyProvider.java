// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.provider.authentication;

/**
 * Interface to be implemented by classes that wish to provide the key.
 */
public interface KeyProvider {

    /**
     * Get the key
     * @return the key
     */
    String getKey();

}
