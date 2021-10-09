// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.aware.credential;

import com.azure.spring.core.properties.credential.NamedKeyProperties;

/**
 * Interface to be implemented by classes that wish to be aware of the named key.
 */
public interface NamedKeyAware {

    void setNamedKey(NamedKeyProperties namedKey);

    NamedKeyProperties getNamedKey();
}
