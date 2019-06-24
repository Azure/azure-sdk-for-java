// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys;

import com.azure.core.credentials.TokenCredential;
import com.azure.keyvault.keys.models.Key;
import com.azure.keyvault.keys.models.KeyBase;
import com.azure.keyvault.keys.models.webkey.KeyType;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyClient}
 */
public final class KeyClientJavaDocCodeSnippets {

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
        System.out.printf("Key is created with name %s and id %s %n", retKey.name(), retKey.id());
        // END: com.azure.keyvault.keys.keyclient.createKey#string-keyType
    }

    /**
     * Generates code sample for using {@link KeyClient#listKeyVersions(String)}
     */
    public void listKeyVersions() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.listKeyVersions
        for (KeyBase key : keyClient.listKeyVersions("keyName")) {
            Key keyWithMaterial  = keyClient.getKey(key).value();
            System.out.printf("Received key's version with name %s, type %s and version %s", keyWithMaterial.name(), keyWithMaterial.keyMaterial().kty(), keyWithMaterial.version());
        }
        // END: com.azure.keyvault.keys.keyclient.listKeyVersions
    }

    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private TokenCredential getKeyVaultCredential() {
        return null;
    }
}
