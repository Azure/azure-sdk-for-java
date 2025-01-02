// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.keyvault.keys;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.models.KeyVaultKey;

/**
 * A sample to demonstrate getting a key from Azure Key Vault using GraalVM.
 */
public final class KeyVaultKeysSample {

    private static final String AZURE_KEY_VAULT_URL = System.getenv("AZURE_KEY_VAULT_URL");

    /**
     * The method to run the Key Vault Keys sample.
     */
    public static void runSample() {
        System.out.println("\n================================================================");
        System.out.println(" Starting Key Vault Keys Sample");
        System.out.println("================================================================");

        if (AZURE_KEY_VAULT_URL == null || AZURE_KEY_VAULT_URL.isEmpty()) {
            System.err.println("AZURE_KEY_VAULT_URL environment variable is not set - exiting");
        }

        KeyClient keyClient = new KeyClientBuilder().vaultUrl(AZURE_KEY_VAULT_URL)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        System.out.println("Getting key from Key Vault");
        KeyVaultKey key = keyClient.getKey("testkey");
        System.out.println(key.getName() + " " + key.getId());

        System.out.println("\n================================================================");
        System.out.println(" Key Vault Keys Sample Complete");
        System.out.println("================================================================");
    }

    private KeyVaultKeysSample() {
    }
}
