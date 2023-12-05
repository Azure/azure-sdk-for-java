// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

class Aes192Cbc extends AesCbc {

    private static final int KEY_SIZE = 192;
    public static final String ALGORITHM_NAME = "A192CBC";

    Aes192Cbc() {
        super(ALGORITHM_NAME, KEY_SIZE);
    }
}
