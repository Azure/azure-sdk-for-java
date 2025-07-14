// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

/*
 * Abstract base class for all encryption implementation.
 */
abstract class LocalEncryptionAlgorithm extends Algorithm {
    protected LocalEncryptionAlgorithm(String name) {
        super(name);
    }
}
