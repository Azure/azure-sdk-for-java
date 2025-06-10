// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import io.clientcore.core.instrumentation.logging.ClientLogger;

import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * Abstract base class for all Algorithm objects.
 *
 */
abstract class Algorithm {
    private static final ClientLogger LOGGER = new ClientLogger(Algorithm.class);

    private final String name;

    Algorithm(String name) {
        if (isNullOrEmpty(name) || name.trim().isEmpty()) {
            throw LOGGER.throwableAtError()
                .addKeyValue("name", name)
                .log("Algorithm name cannot be null or empty.", IllegalArgumentException::new);
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
