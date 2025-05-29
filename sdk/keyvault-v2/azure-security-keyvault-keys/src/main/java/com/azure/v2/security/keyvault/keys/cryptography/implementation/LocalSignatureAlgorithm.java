// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

abstract class LocalSignatureAlgorithm extends Algorithm {
    protected LocalSignatureAlgorithm(String name) {
        super(name);
    }
}
