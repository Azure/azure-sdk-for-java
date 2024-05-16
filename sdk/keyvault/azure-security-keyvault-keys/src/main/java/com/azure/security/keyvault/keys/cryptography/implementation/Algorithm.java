// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.implementation;

import com.azure.core.util.CoreUtils;

/**
 * Abstract base class for all Algorithm objects.
 *
 */
abstract class Algorithm {

    private final String name;

    Algorithm(String name) {
        if (CoreUtils.isNullOrEmpty(name) || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name");
        }

        this.name = name;
    }

    /*
     * Gets the name of the algorithm.
     *
     * @return The name of the algorithm.
     */
    public String getName() {
        return name;
    }
}
