/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography;

/**
 * Abstract base class for all Algorithm objects.
 *
 */
public abstract class Algorithm {

    private final String _name;

    /**
     * Constructor.
     * 
     * @param name The name of the algorithm.
     */
    protected Algorithm(String name) {
        if (Strings.isNullOrWhiteSpace(name)) {
            throw new IllegalArgumentException("name");
        }

        _name = name;
    }

    /**
     * Gets the name of the algorithm.
     * 
     * @return The name of the algorithm.
     */
    public String getName() {
        return _name;
    }
}
