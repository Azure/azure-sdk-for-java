/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography.algorithms;

import com.microsoft.azure.keyvault.cryptography.AsymmetricEncryptionAlgorithm;

public abstract class RsaEncryption extends AsymmetricEncryptionAlgorithm {

    protected RsaEncryption(String name) {
        super(name);
    }

}
