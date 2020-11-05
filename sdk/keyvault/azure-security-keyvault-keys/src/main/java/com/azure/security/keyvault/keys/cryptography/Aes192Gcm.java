// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

class Aes192Gcm extends AesGcm {
    private static final int KEY_SIZE = 192;
    public static final String ALGORITHM_NAME = "A192GCM";

    Aes192Gcm() {
        super(ALGORITHM_NAME, KEY_SIZE);
    }
}
