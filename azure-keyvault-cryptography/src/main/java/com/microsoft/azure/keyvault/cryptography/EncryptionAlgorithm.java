/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

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
