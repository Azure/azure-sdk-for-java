// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography.algorithms;

public class Es512 extends Ecdsa {
    public static final String ALGORITHM_NAME = "ES512";

    @Override
    public int getDigestLength() {
        return 64;
    }

    @Override
    public int getCoordLength() {
        return 66;
    }
}
