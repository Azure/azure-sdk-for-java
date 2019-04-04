// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography.algorithms;

public class Aes256CbcHmacSha512 extends AesCbcHmacSha2 {

    public static final String ALGORITHM_NAME = "A256CBC-HS512";

    public Aes256CbcHmacSha512() {
        super(ALGORITHM_NAME);
    }
}
