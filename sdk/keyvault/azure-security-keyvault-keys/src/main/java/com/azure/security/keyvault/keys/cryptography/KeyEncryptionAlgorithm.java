// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

/**
 * Abstract base class for all encryption implementation.
 *
 */
abstract class KeyEncryptionAlgorithm extends Algorithm {

    /**
     * Constructor.
     * @param name The name of the algorithm.
     */
    protected KeyEncryptionAlgorithm(String name) {
        super(name);
    }

}
