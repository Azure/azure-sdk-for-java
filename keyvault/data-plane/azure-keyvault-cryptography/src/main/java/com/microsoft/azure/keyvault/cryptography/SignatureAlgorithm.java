// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography;

public abstract class SignatureAlgorithm extends Algorithm {

    protected SignatureAlgorithm(String name) {
        super(name);
    }

}
