// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import com.azure.core.util.ServiceVersion;

/**
 * {@link ServiceVersion} enum that doesn't have a {@code getLatest} static method.
 */
public enum ServiceVersionWithoutGetLatest implements ServiceVersion {
    BAD_SERVICE_VERSION;

    @Override
    public String getVersion() {
        return BAD_SERVICE_VERSION.toString();
    }
}
