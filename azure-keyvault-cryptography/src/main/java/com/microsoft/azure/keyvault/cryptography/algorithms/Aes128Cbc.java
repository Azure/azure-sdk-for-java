/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography.algorithms;

public class Aes128Cbc extends AesCbc {

    private static final int KEY_SIZE = 128;
    public static final String ALGORITHM_NAME = "A128CBC";

    public Aes128Cbc() {
        super(ALGORITHM_NAME, KEY_SIZE);
    }
}
