// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography.algorithms;

public class Aes192CbcHmacSha384 extends AesCbcHmacSha2 {

    public static final String ALGORITHM_NAME = "A192CBC-HS384";

    public Aes192CbcHmacSha384() {
        super(ALGORITHM_NAME);
    }
}
