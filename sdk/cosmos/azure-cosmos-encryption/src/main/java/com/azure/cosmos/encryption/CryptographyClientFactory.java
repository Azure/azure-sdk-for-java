// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;

/**
 * Factory Class for Accessing CryptographyClient methods.
 */
class CryptographyClientFactory {
    public CryptographyAsyncClient getCryptographyClient(KeyVaultKeyUriProperties keyVaultKeyUriProperties,
                                                         TokenCredential tokenCredential) {
        return new CryptographyClientBuilder()
            .credential(tokenCredential)
            .keyIdentifier(keyVaultKeyUriProperties.getKeyUri().toString())
            .buildAsyncClient();
    }
}
