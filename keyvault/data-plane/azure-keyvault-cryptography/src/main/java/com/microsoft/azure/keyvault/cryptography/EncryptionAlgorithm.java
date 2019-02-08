// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography;

/**
 * Abstract base class for all encryption algorithms.
 *
 */
public abstract class EncryptionAlgorithm extends Algorithm {

    /**
     * Constructor.
     * @param name The name of the algorithm.
     */
    protected EncryptionAlgorithm(String name) {
        super(name);
    }

}
