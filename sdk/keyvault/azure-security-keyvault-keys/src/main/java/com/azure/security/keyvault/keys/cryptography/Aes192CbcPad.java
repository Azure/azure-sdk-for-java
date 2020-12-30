// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

class Aes192CbcPad extends AesCbcPad {

    public static final String ALGORITHM_NAME = "A192CBCPAD";
    private static final int KEY_SIZE = 192;

    Aes192CbcPad() {
        super(ALGORITHM_NAME, KEY_SIZE);
    }
}
