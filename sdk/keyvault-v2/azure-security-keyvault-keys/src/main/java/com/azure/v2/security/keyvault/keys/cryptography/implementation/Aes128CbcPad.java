// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

class Aes128CbcPad extends AesCbcPad {
    public static final String ALGORITHM_NAME = "A128CBCPAD";
    private static final int KEY_SIZE = 128;

    Aes128CbcPad() {
        super(ALGORITHM_NAME, KEY_SIZE);
    }
}
