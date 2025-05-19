// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * Abstract base class for all Algorithm objects.
 *
 */
abstract class Algorithm {

    private final String name;

    Algorithm(String name) {
        if (isNullOrEmpty(name) || name.trim().isEmpty()) {
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
