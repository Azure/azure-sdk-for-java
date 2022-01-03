// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.KeyRotationLifetimeAction;
import com.azure.security.keyvault.keys.models.KeyRotationPolicyAction;
import com.azure.security.keyvault.keys.models.KeyRotationPolicyProperties;

import java.util.ArrayList;
import java.util.List;

/*
 * This sample demonstrates how to set key rotation policies and manually rotate keys in Key Vault to create a new key
 * version.
 */
public class KeyRotationAsync {
    /**
     * Authenticates with the key vault and shows set key rotation policies and manually rotate keys in Key Vault to
     * create a new key version.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when an invalid key vault endpoint is passed.
     */
    public static void main(String[] args) {
        // Instantiate a KeyAsyncClient that will be used to call the service. Notice that the KeyAsyncClient is using
        // default Azure credentials. To make default credentials work, ensure that environment variables
        // 'AZURE_CLIENT_ID', 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // Let's create an RSA key.
        String keyName = "MyKey";
        keyAsyncClient.createRsaKey(new CreateRsaKeyOptions(keyName).setKeySize(2048))
            .subscribe(originalKey ->
                System.out.printf("Key created with name: %s, and type: %s%n", originalKey.getName(),
                    originalKey.getKeyType()));

        // You can configure its key rotation policy to allow Azure Key Vault to do it automatically under certain
        // conditions.
        List<KeyRotationLifetimeAction> keyRotationLifetimeActionList = new ArrayList<>();
        KeyRotationLifetimeAction rotateLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.ROTATE)
            .setTimeAfterCreate("P90D"); // Rotate the key after 90 days of its creation.
        keyRotationLifetimeActionList.add(rotateLifetimeAction);
        KeyRotationPolicyProperties keyRotationPolicyProperties = new KeyRotationPolicyProperties()
            .setLifetimeActions(keyRotationLifetimeActionList)
            .setExpiryTime("P6M"); // Make any new versions of the key expire 6 months after creation.

        // An object containing the details of the recently updated key rotation policy will be returned by the update
        // method.
        keyAsyncClient.updateKeyRotationPolicy(keyName, keyRotationPolicyProperties)
            .subscribe(keyRotationPolicy ->
                System.out.printf("Updated key rotation policy with id: %s%n", keyRotationPolicy.getId()));

        // You can also manually rotate a key by calling the following method.
        keyAsyncClient.rotateKey(keyName)
            .subscribe(manuallyRotatedKey ->
                System.out.printf("Rotated key with name: %s%n", manuallyRotatedKey.getName()));
    }
}
