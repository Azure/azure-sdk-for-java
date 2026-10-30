// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyVaultKey;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;

@SuppressWarnings("unused")
public class ReadmeSamples {
    private static final byte[] SEED;

    static {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte[] seed;
        try {
            dos.writeLong(0x1234567L);
            dos.flush();
            seed = bos.toByteArray();
        } catch (IOException ex) {
            seed = new byte[0];
        }

        SEED = seed;
    }

    private final KeyClient keyClient = new KeyClientBuilder()
        .vaultUrl("<your-key-vault-url>")
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();
    private final KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
        .vaultUrl("<your-key-vault-url>")
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildAsyncClient();

    private final CryptographyClient cryptoClient = new CryptographyClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .keyIdentifier("<your-key-id-from-key-vault")
        .buildClient();
    private final CryptographyAsyncClient cryptoAsyncClient = new CryptographyClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .keyIdentifier("<your-key-id-from-key-vault")
        .buildAsyncClient();

    public void createKeyClient() {
        // BEGIN: readme-sample-createKeyClient
        KeyClient keyClient = new KeyClientBuilder()
            .vaultUrl("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-createKeyClient
    }

    public void createCryptographyClient() {
        // BEGIN: readme-sample-createCryptographyClient
        // Create client with key identifier from Key Vault.
        CryptographyClient cryptoClient = new CryptographyClientBuilder()
            .keyIdentifier("<your-key-id-from-key-vault>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-createCryptographyClient
    }

    public void createKey() {
        // BEGIN: readme-sample-createKey
        KeyVaultKey rsaKey = keyClient.createRsaKey(new CreateRsaKeyOptions("CloudRsaKey")
            .setExpiresOn(OffsetDateTime.now().plusYears(1))
            .setKeySize(2048));
        System.out.printf("Key created with name \"%s\" and id %s%n", rsaKey.getName(), rsaKey.getId());

        KeyVaultKey ecKey = keyClient.createEcKey(new CreateEcKeyOptions("CloudEcKey")
            .setCurveName(KeyCurveName.P_256)
            .setExpiresOn(OffsetDateTime.now().plusYears(1)));
        System.out.printf("Key created with name \"%s\" and id %s%n", ecKey.getName(), ecKey.getId());
        // END: readme-sample-createKey
    }

    public void retrieveKey() {
        // BEGIN: readme-sample-retrieveKey
        KeyVaultKey key = keyClient.getKey("<key-name>");
        System.out.printf("A key was returned with name \"%s\" and id %s%n", key.getName(), key.getId());
        // END: readme-sample-retrieveKey
    }

    public void updateKey() {
        // BEGIN: readme-sample-updateKey
        // Get the key to update.
        KeyVaultKey key = keyClient.getKey("<key-name>");
        // Update the expiry time of the key.
        key.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(30));
        KeyVaultKey updatedKey = keyClient.updateKeyProperties(key.getProperties());
        System.out.printf("Key's updated expiry time: %s%n", updatedKey.getProperties().getExpiresOn());
        // END: readme-sample-updateKey
    }

    public void deleteKey() {
        // BEGIN: readme-sample-deleteKey
        SyncPoller<DeletedKey, Void> deletedKeyPoller = keyClient.beginDeleteKey("<key-name>");

        PollResponse<DeletedKey> deletedKeyPollResponse = deletedKeyPoller.poll();

        // Deleted key is accessible as soon as polling begins.
        DeletedKey deletedKey = deletedKeyPollResponse.getValue();
        // Deletion date only works for a soft-delete enabled key vault.
        System.out.printf("Deletion date: %s%n", deletedKey.getDeletedOn());

        // The key is being deleted on the server.
        deletedKeyPoller.waitForCompletion();
        // END: readme-sample-deleteKey
    }

    public void listKeys() {
        // BEGIN: readme-sample-listKeys
        // List operations don't return the keys with key material information. So, for each returned key we call getKey to
        // get the key with its key material information.
        for (KeyProperties keyProperties : keyClient.listPropertiesOfKeys()) {
            KeyVaultKey keyWithMaterial = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());
            System.out.printf("Received key with name \"%s\" and type \"%s\"%n", keyWithMaterial.getName(),
                keyWithMaterial.getKey().getKeyType());
        }
        // END: readme-sample-listKeys
    }

    public void encrypt() {
        // BEGIN: readme-sample-encrypt
        byte[] plaintext = new byte[100];
        new SecureRandom(SEED).nextBytes(plaintext);

        // Let's encrypt a simple plain text of size 100 bytes.
        EncryptResult encryptionResult = cryptoClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext);
        System.out.printf("Returned ciphertext size is %d bytes with algorithm \"%s\"%n",
            encryptionResult.getCipherText().length, encryptionResult.getAlgorithm());
        // END: readme-sample-encrypt
    }

    public void decrypt() {
        // BEGIN: readme-sample-decrypt
        byte[] plaintext = new byte[100];
        new SecureRandom(SEED).nextBytes(plaintext);
        EncryptResult encryptionResult = cryptoClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext);

        //Let's decrypt the encrypted result.
        DecryptResult decryptionResult = cryptoClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptionResult.getCipherText());
        System.out.printf("Returned plaintext size is %d bytes%n", decryptionResult.getPlainText().length);
        // END: readme-sample-decrypt
    }

    public void createKeyAsync() {
        // BEGIN: readme-sample-createKeyAsync
        keyAsyncClient.createRsaKey(new CreateRsaKeyOptions("CloudRsaKey")
                .setExpiresOn(OffsetDateTime.now().plusYears(1))
                .setKeySize(2048))
            .subscribe(key ->
                System.out.printf("Key created with name \"%s\" and id %s%n", key.getName(), key.getId()));

        keyAsyncClient.createEcKey(new CreateEcKeyOptions("CloudEcKey")
                .setExpiresOn(OffsetDateTime.now().plusYears(1)))
            .subscribe(key ->
                System.out.printf("Key created with name \"%s\" and id %s%n", key.getName(), key.getId()));
        // END: readme-sample-createKeyAsync
    }

    public void retrieveKeyAsync() {
        // BEGIN: readme-sample-retrieveKeyAsync
        keyAsyncClient.getKey("<key-name>")
            .subscribe(key ->
                System.out.printf("Key was returned with name \"%s\" and id %s%n", key.getName(), key.getId()));
        // END: readme-sample-retrieveKeyAsync
    }

    public void updateKeyAsync() {
        // BEGIN: readme-sample-updateKeyAsync
        keyAsyncClient.getKey("<key-name>")
            .flatMap(key -> {
                // Update the expiry time of the key.
                key.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(50));
                return keyAsyncClient.updateKeyProperties(key.getProperties());
            }).subscribe(updatedKey ->
                System.out.printf("Key's updated expiry time: %s%n", updatedKey.getProperties().getExpiresOn()));
        // END: readme-sample-updateKeyAsync
    }

    public void deleteKeyAsync() {
        // BEGIN: readme-sample-deleteKeyAsync
        keyAsyncClient.beginDeleteKey("<key-name>")
            .subscribe(pollResponse -> {
                System.out.printf("Deletion status: %s%n", pollResponse.getStatus());
                System.out.printf("Deleted key name: %s%n", pollResponse.getValue().getName());
                System.out.printf("Key deletion date: %s%n", pollResponse.getValue().getDeletedOn());
            });
        // END: readme-sample-deleteKeyAsync
    }

    public void listKeysAsync() {
        // BEGIN: readme-sample-listKeysAsync
        // The List Keys operation returns keys without their value, so for each key returned we call `getKey` to get its value
        // as well.
        keyAsyncClient.listPropertiesOfKeys()
            .flatMap(keyProperties -> keyAsyncClient.getKey(keyProperties.getName(), keyProperties.getVersion()))
            .subscribe(key ->
                System.out.printf("Received key with name \"%s\" and type \"%s\"", key.getName(), key.getKeyType()));
        // END: readme-sample-listKeysAsync
    }

    public void encryptAsync() {
        // BEGIN: readme-sample-encryptAsync
        byte[] plaintext = new byte[100];
        new SecureRandom(SEED).nextBytes(plaintext);

        // Let's encrypt a simple plain text of size 100 bytes.
        cryptoAsyncClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext)
            .subscribe(encryptionResult -> System.out.printf("Returned ciphertext size is %d bytes with algorithm \"%s\"%n",
                encryptionResult.getCipherText().length, encryptionResult.getAlgorithm()));
        // END: readme-sample-encryptAsync
    }

    public void decryptAsync() {
        // BEGIN: readme-sample-decryptAsync
        byte[] plaintext = new byte[100];
        new SecureRandom(SEED).nextBytes(plaintext);

        // Let's encrypt a simple plain text of size 100 bytes.
        cryptoAsyncClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext)
            .flatMap(encryptionResult -> {
                System.out.printf("Returned ciphertext size is %d bytes with algorithm \"%s\"%n",
                    encryptionResult.getCipherText().length, encryptionResult.getAlgorithm());
                //Let's decrypt the encrypted response.
                return cryptoAsyncClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptionResult.getCipherText());
            }).subscribe(decryptionResult ->
                System.out.printf("Returned plaintext size is %d bytes%n", decryptionResult.getPlainText().length));
        // END: readme-sample-decryptAsync
    }

    public void troubleshooting() {
        // BEGIN: readme-sample-troubleshooting
        try {
            keyClient.getKey("<deleted-key-name>");
        } catch (ResourceNotFoundException e) {
            System.out.println(e.getMessage());
        }
        // END: readme-sample-troubleshooting
    }
}
