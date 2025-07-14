// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

class Es256 extends Ecdsa {
    public static final String ALGORITHM_NAME = "ES256";

    @Override
    public int getDigestLength() {
        return 32;
    }

    @Override
    public int getCoordLength() {
        return 32;
    }
}
