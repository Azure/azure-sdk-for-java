// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

abstract class LocalSignatureAlgorithm extends Algorithm {

    protected LocalSignatureAlgorithm(String name) {
        super(name);
    }

}
