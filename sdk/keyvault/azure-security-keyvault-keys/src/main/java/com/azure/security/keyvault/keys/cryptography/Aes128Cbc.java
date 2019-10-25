// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

class Aes128Cbc extends AesCbc {

    private static final int KEY_SIZE = 128;
    public static final String ALGORITHM_NAME = "A128CBC";

    Aes128Cbc() {
        super(ALGORITHM_NAME, KEY_SIZE);
    }
}
