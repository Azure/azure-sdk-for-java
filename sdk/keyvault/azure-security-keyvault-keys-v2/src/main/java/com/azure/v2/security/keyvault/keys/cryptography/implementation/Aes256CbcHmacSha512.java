// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

class Aes256CbcHmacSha512 extends AesCbcHmacSha2 {
    public static final String ALGORITHM_NAME = "A256CBC-HS512";

    Aes256CbcHmacSha512() {
        super(ALGORITHM_NAME);
    }
}
