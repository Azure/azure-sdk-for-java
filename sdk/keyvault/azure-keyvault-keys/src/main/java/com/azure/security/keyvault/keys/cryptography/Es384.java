// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

class Es384 extends Ecdsa {
    public static final String ALGORITHM_NAME = "ES384";

    @Override
    public int getDigestLength() {
        return 48;
    }
    
    @Override
    public int getCoordLength() {
        return 48;
    }
}
