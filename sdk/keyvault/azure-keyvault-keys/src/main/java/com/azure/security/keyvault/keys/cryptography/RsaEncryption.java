// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

abstract class RsaEncryption extends AsymmetricEncryptionAlgorithm {

    protected RsaEncryption(String name) {
        super(name);
    }

}
