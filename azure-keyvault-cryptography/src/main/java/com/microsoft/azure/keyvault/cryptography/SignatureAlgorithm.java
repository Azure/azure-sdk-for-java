/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography;

public abstract class SignatureAlgorithm extends Algorithm {

    protected SignatureAlgorithm(String name) {
        super(name);
    }

}
