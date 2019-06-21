// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys;

import com.azure.keyvault.keys.models.EcKeyCreateOptions;
import com.azure.keyvault.keys.models.RsaKeyCreateOptions;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to asynchronously list keys and versions of a given key in the key vault.
 */
public class ListOperationsAsync {
    /**
     * Authenticates with the key vault and shows how to asynchronously list keys and list versions of a specific key in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     */
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, InterruptedException {

        // Instantiate an async key client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        KeyAsyncClient keyAsyncClient = KeyAsyncClient.builder()
                .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
                //.credential(AzureCredential.DEFAULT)
                .build();

        // Let's create Ec and Rsa keys valid for 1 year. if the key
        // already exists in the key vault, then a new version of the key is created.
        keyAsyncClient.createEcKey(new EcKeyCreateOptions("CloudEcKey")
                .expires(OffsetDateTime.now().plusYears(1)))
                .subscribe(keyResponse ->
                        System.out.printf("Key is created with name %s and type %s \n", keyResponse.value().name(), keyResponse.value().keyMaterial().kty()));

        Thread.sleep(2000);

        keyAsyncClient.createRsaKey(new RsaKeyCreateOptions("CloudRsaKey")
                .expires(OffsetDateTime.now().plusYears(1)))
                .subscribe(keyResponse ->
                        System.out.printf("Key is created with name %s and type %s \n", keyResponse.value().name(), keyResponse.value().keyMaterial().kty()));

        Thread.sleep(2000);

        // You need to check te type of keys already exist in your key vault. Let's list the keys and print their types.
        // List operations don't return the keys with key material information. So, for each returned key we call getKey to get the key with its key material information.
        keyAsyncClient.listKeys()
          .subscribe(keyBase ->
            keyAsyncClient.getKey(keyBase).subscribe(keyResponse ->
                  System.out.printf("Received key with name %s and type %s \n", keyResponse.value().name(), keyResponse.value().keyMaterial().kty())));

        Thread.sleep(15000);

        // We need the Cloud Rsa key with bigger key size, so you want to update the key in key vault to ensure it has the required size.
        // Calling createRsaKey on an existing key creates a new version of the key in the key vault with the new specified size.
        keyAsyncClient.createRsaKey(new RsaKeyCreateOptions("CloudRsaKey")
          .keySize(4096)
          .expires(OffsetDateTime.now().plusYears(1))).subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and type %s \n", keyResponse.value().name(), keyResponse.value().keyMaterial().kty()));

        Thread.sleep(2000);

        // You need to check all the different versions Cloud Rsa key had previously. Lets print all the versions of this key.
        keyAsyncClient.listKeyVersions("BankAccountPassword").subscribe(keyBase ->
            keyAsyncClient.getKey(keyBase).subscribe(keyResponse ->
                System.out.printf("Received key's version with name %s, value %s and version %s \n", keyResponse.value().name(), keyResponse.value().keyMaterial().kty())));

        Thread.sleep(15000);
    }
}
