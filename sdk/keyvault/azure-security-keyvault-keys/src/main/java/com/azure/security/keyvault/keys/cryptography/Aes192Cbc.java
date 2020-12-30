// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

class Aes192Cbc extends AesCbc {

    public static final String ALGORITHM_NAME = "A192CBC";
    private static final int KEY_SIZE = 192;

    Aes192Cbc() {
        super(ALGORITHM_NAME, KEY_SIZE);
    }
}
