// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

class Aes128Gcm extends AesGcm {

    public static final String ALGORITHM_NAME = "A128GCM";
    private static final int KEY_SIZE = 128;

    Aes128Gcm() {
        super(ALGORITHM_NAME, KEY_SIZE);
    }
}
