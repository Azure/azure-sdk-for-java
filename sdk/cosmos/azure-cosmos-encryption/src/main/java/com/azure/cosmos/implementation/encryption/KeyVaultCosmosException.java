// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.CosmosException;

import java.util.HashMap;

public class KeyVaultCosmosException extends CosmosException {
    public KeyVaultCosmosException(int statusCode,
                                   String message,
                                   Throwable cause) {
        super(statusCode, message, new HashMap<>(), cause);
    }
}
