// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys;

import com.azure.core.credentials.TokenCredential;
import com.azure.keyvault.keys.models.Key;
import com.azure.keyvault.keys.models.webkey.KeyType;

/**
 * This class contains code samples for generating javadocs through doclets
 */
public class KeyClientJavaDocCodeSnippets {

    /**
     * Generates code sample for creating a {@link KeyClient}
     * @return An instance of {@link KeyClient}
     */
    public KeyClient createClient() {
        TokenCredential keyVaultCredential = getKeyVaultCredential();
        // BEGIN: com.azure.keyvault.keys.keyclient.instantiation
        KeyClient keyClient = KeyClient.builder()
            .endpoint("https://myvault.azure.net/")
            .credential(keyVaultCredential)
            .build();
        // END: com.azure.keyvault.keys.keyclient.instantiation
        return keyClient;
    }

    /**
     * Generates a code sample for using {@link KeyClient#createKey(String, KeyType)}
     */
    public void createKey() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.createKey#string-keyType
        Key retKey = keyClient.createKey("keyName", KeyType.EC).value();
        System.out.println("Key is created with name " + retKey.name() + " and id " + retKey.id());
        // END: com.azure.keyvault.keys.keyclient.createKey#string-keyType
    }

    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private TokenCredential getKeyVaultCredential() {
        return null;
    }
}
