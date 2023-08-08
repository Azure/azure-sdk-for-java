// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.provider.authentication;

import com.azure.spring.cloud.core.properties.authentication.NamedKeyProperties;

/**
 * Interface to be implemented by classes that wish to provide the named key.
 */
public interface NamedKeyProvider {

    /**
     * Get the named key properties
     * @return the named key properties
     */
    NamedKeyProperties getNamedKey();
}
