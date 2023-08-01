// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

class Aes128CbcHmacSha256 extends AesCbcHmacSha2 {

    static final String ALGORITHM_NAME = "A128CBC-HS256";

    Aes128CbcHmacSha256() {
        super(ALGORITHM_NAME);
    }
}
