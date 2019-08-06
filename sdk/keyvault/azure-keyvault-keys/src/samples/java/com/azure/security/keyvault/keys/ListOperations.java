// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.EcKeyCreateOptions;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.KeyBase;
import com.azure.security.keyvault.keys.models.RsaKeyCreateOptions;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to list keys and versions of a given key in the key vault.
 */
public class ListOperations {
    /**
     * Authenticates with the key vault and shows how to list keys and list versions of a specific key in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     */
    public static void main(String[] args) throws IllegalArgumentException {

        // Instantiate a keyClient that will be used to call the service. Notice that the keyClient is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        KeyClient keyClient = new KeyClientBuilder()
                .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        // Let's create Ec and Rsa keys valid for 1 year. if the key
        // already exists in the key vault, then a new version of the key is created.
        keyClient.createRsaKey(new RsaKeyCreateOptions("CloudRsaKey")
                .expires(OffsetDateTime.now().plusYears(1))
                .keySize(2048));

        keyClient.createEcKey(new EcKeyCreateOptions("CloudEcKey")
                .expires(OffsetDateTime.now().plusYears(1)));

        // You need to check te type of keys already exist in your key vault. Let's list the keys and print their types.
        // List operations don't return the keys with key material information. So, for each returned key we call getKey to get the key with its key material information.
        for (KeyBase key : keyClient.listKeys()) {
            Key keyWithMaterial = keyClient.getKey(key);
            System.out.printf("Received key with name %s and type %s", keyWithMaterial.name(), keyWithMaterial.keyMaterial().kty());
        }

        // We need the Cloud Rsa key with bigger key size, so you want to update the key in key vault to ensure it has the required size.
        // Calling createRsaKey on an existing key creates a new version of the key in the key vault with the new specified size.
        keyClient.createRsaKey(new RsaKeyCreateOptions("CloudRsaKey")
                .expires(OffsetDateTime.now().plusYears(1))
                .keySize(4096));

        // You need to check all the different versions Cloud Rsa key had previously. Lets print all the versions of this key.
        for (KeyBase key : keyClient.listKeyVersions("CloudRsaKey")) {
            Key keyWithMaterial  = keyClient.getKey(key);
            System.out.printf("Received key's version with name %s, type %s and version %s", keyWithMaterial.name(), keyWithMaterial.keyMaterial().kty(), keyWithMaterial.version());
        }
    }
}
