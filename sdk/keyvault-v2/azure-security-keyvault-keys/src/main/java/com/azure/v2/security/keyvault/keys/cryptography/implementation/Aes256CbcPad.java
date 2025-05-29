// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

class Aes256CbcPad extends AesCbcPad {
    public static final String ALGORITHM_NAME = "A256CBCPAD";
    private static final int KEY_SIZE = 256;

    Aes256CbcPad() {
        super(ALGORITHM_NAME, KEY_SIZE);
    }
}
