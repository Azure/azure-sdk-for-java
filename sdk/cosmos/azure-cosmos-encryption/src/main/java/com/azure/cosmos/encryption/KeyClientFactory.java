// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;

/**
 * Factory Class for Accessing KeyClient methods.
 */
public class KeyClientFactory {
    public KeyAsyncClient getKeyClient(KeyVaultKeyUriProperties keyVaultKeyUriProperties,
                                       TokenCredential tokenCredential) {
        return new KeyClientBuilder()
            .credential(tokenCredential)
            .vaultUrl(keyVaultKeyUriProperties.getKeyVaultUri().toString())
            .buildAsyncClient();
    }
}
