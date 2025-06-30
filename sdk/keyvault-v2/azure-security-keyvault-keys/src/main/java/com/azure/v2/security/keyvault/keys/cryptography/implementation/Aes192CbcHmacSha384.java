// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

class Aes192CbcHmacSha384 extends AesCbcHmacSha2 {
    public static final String ALGORITHM_NAME = "A192CBC-HS384";

    Aes192CbcHmacSha384() {
        super(ALGORITHM_NAME);
    }
}
