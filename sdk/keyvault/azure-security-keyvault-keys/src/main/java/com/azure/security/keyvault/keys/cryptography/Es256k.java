// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

class Es256k extends Ecdsa {
    public static final String ALGORITHM_NAME = "ES256K";

    @Override
    public int getDigestLength() {
        return 32;
    }

    @Override
    public int getCoordLength() {
        return 32;
    }
}
