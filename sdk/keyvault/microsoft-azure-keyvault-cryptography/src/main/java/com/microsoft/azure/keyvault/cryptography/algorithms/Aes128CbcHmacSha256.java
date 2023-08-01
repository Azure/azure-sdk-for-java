// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography.algorithms;

public class Aes128CbcHmacSha256 extends AesCbcHmacSha2 {

    public static final String ALGORITHM_NAME = "A128CBC-HS256";

    public Aes128CbcHmacSha256() {
        super(ALGORITHM_NAME);
    }
}
