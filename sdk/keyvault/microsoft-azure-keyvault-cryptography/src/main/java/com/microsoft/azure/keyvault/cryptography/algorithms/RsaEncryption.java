// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography.algorithms;

import com.microsoft.azure.keyvault.cryptography.AsymmetricEncryptionAlgorithm;

public abstract class RsaEncryption extends AsymmetricEncryptionAlgorithm {

    protected RsaEncryption(String name) {
        super(name);
    }

}
