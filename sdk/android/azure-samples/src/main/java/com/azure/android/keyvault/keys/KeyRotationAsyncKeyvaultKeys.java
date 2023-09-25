// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.keyvault.keys;

import android.util.Log;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.KeyRotationLifetimeAction;
import com.azure.security.keyvault.keys.models.KeyRotationPolicy;
import com.azure.security.keyvault.keys.models.KeyRotationPolicyAction;

import java.util.ArrayList;
import java.util.List;

/*
 * This sample demonstrates how to set key rotation policies and manually rotate keys in Key Vault to create a new key
 * version.
 */
public class KeyRotationAsyncKeyvaultKeys {
    /**
     * Authenticates with the key vault and shows set key rotation policies and manually rotate keys in Key Vault to
     * create a new key version.
     *
     * @throws IllegalArgumentException when an invalid key vault endpoint is passed.
     */

    private static final String TAG = "KeyRotationAsync";

    public static void main(String endpoint, ClientSecretCredential clientSecretCredential) throws InterruptedException {
        /* Instantiate a KeyAsyncClient that will be used to call the service. Notice that the client is using default
        Azure credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault. See the README
        (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md)
        for links and instructions. */

        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
            .vaultUrl(endpoint)
            .credential(clientSecretCredential)
            .buildAsyncClient();

        // Let's create an RSA key.
        String keyName = "MyKey";
        keyAsyncClient.createRsaKey(new CreateRsaKeyOptions(keyName).setKeySize(2048))
            .subscribe(originalKey ->
                Log.i(TAG, String.format("Key created with name: %s, and type: %s%n", originalKey.getName(),
                    originalKey.getKeyType())));

        // give time for key to be created
        Thread.sleep(10000);

        // You can configure its key rotation policy to allow Azure Key Vault to do it automatically under certain
        // conditions. Properties such as timeAfterCreate and timeBeforeExpiry should be defined as an ISO 8601
        // duration. For example, 90 days would be "P90D", 3 months would be "P3M" and 1 year and 10 days would be
        // "P1Y10D". See https://wikipedia.org/wiki/ISO_8601#Durations for more information.
        List<KeyRotationLifetimeAction> keyRotationLifetimeActionList = new ArrayList<>();
        KeyRotationLifetimeAction rotateLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.ROTATE)
            .setTimeAfterCreate("P90D"); // Rotate the key after 90 days of its creation.
        Log.i(TAG, "set key to rotate after 90 days");
        keyRotationLifetimeActionList.add(rotateLifetimeAction);

        KeyRotationPolicy keyRotationPolicy = new KeyRotationPolicy()
            .setLifetimeActions(keyRotationLifetimeActionList)
            .setExpiresIn("P6M"); // Make any new versions of the key expire 6 months after creation.
        Log.i(TAG, "set future keys to rotate after 6 months");

        Thread.sleep(2000);

        // An object containing the details of the recently updated key rotation policy will be returned by the update
        // method.
        keyAsyncClient.updateKeyRotationPolicy(keyName, keyRotationPolicy)
            .subscribe(updatedPolicy ->
                    Log.i(TAG, String.format("Updated key rotation policy with id: %s%n", updatedPolicy.getId())));

        // You can also manually rotate a key by calling the following method.
        keyAsyncClient.rotateKey(keyName)
            .subscribe(manuallyRotatedKey ->
                    Log.i(TAG, String.format("Rotated key with name: %s%n", manuallyRotatedKey.getName())));
    }
}
