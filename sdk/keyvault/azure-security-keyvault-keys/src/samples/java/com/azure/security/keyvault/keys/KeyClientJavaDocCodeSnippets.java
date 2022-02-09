// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.ImportKeyOptions;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyExportEncryptionAlgorithm;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyRotationLifetimeAction;
import com.azure.security.keyvault.keys.models.KeyRotationPolicy;
import com.azure.security.keyvault.keys.models.KeyRotationPolicyAction;
import com.azure.security.keyvault.keys.models.KeyRotationPolicyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.RandomBytes;
import com.azure.security.keyvault.keys.models.ReleaseKeyOptions;
import com.azure.security.keyvault.keys.models.ReleaseKeyResult;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyClient}.
 */
public final class KeyClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link KeyClient}.
     *
     * @return An instance of {@link KeyClient}.
     */
    public KeyClient createClient() {
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.instantiation
        KeyClient keyClient = new KeyClientBuilder()
            .vaultUrl("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.security.keyvault.keys.KeyClient.instantiation
        return keyClient;
    }

    /**
     * Generates a code sample for using {@link KeyClient#createKey(String, KeyType)},
     * {@link KeyClient#createRsaKey(CreateRsaKeyOptions)},
     * {@link KeyClient#createEcKey(CreateEcKeyOptions)} and
     * {@link KeyClient#createOctKey(CreateOctKeyOptions)}.
     */
    public void createKey() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.createKey#String-KeyType
        KeyVaultKey key = keyClient.createKey("keyName", KeyType.EC);

        System.out.printf("Created key with name: %s and id: %s%n", key.getName(), key.getId());
        // END: com.azure.security.keyvault.keys.KeyClient.createKey#String-KeyType

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.createKey#CreateKeyOptions
        CreateKeyOptions createKeyOptions = new CreateKeyOptions("keyName", KeyType.RSA)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey optionsKey = keyClient.createKey(createKeyOptions);

        System.out.printf("Created key with name: %s and id: %s%n", optionsKey.getName(), optionsKey.getId());
        // END: com.azure.security.keyvault.keys.KeyClient.createKey#CreateKeyOptions

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.createRsaKey#CreateRsaKeyOptions
        CreateRsaKeyOptions createRsaKeyOptions = new CreateRsaKeyOptions("keyName")
            .setKeySize(2048)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey rsaKey = keyClient.createRsaKey(createRsaKeyOptions);

        System.out.printf("Created key with name: %s and id: %s%n", rsaKey.getName(), rsaKey.getId());
        // END: com.azure.security.keyvault.keys.KeyClient.createRsaKey#CreateRsaKeyOptions

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.createEcKey#CreateOctKeyOptions
        CreateEcKeyOptions createEcKeyOptions = new CreateEcKeyOptions("keyName")
            .setCurveName(KeyCurveName.P_384)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey ecKey = keyClient.createEcKey(createEcKeyOptions);

        System.out.printf("Created key with name: %s and id: %s%n", ecKey.getName(), ecKey.getId());
        // END: com.azure.security.keyvault.keys.KeyClient.createEcKey#CreateOctKeyOptions

        // BEGIN: com.azure.security.keyvault.keys.async.KeyClient.createOctKey#CreateOctKeyOptions
        CreateOctKeyOptions createOctKeyOptions = new CreateOctKeyOptions("keyName")
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey octKey = keyClient.createOctKey(createOctKeyOptions);

        System.out.printf("Created key with name: %s and id: %s%n", octKey.getName(), octKey.getId());
        // END: com.azure.security.keyvault.keys.async.KeyClient.createOctKey#CreateOctKeyOptions
    }

    /**
     * Generates code samples for using {@link KeyAsyncClient#importKey(String, JsonWebKey)},
     * {@link KeyAsyncClient#importKey(ImportKeyOptions)} and
     * {@link KeyAsyncClient#importKeyWithResponse(ImportKeyOptions)}.
     */
    public void importKey() {
        KeyClient keyClient = createClient();
        JsonWebKey jsonWebKeyToImport = new JsonWebKey();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.importKey#String-JsonWebKey
        KeyVaultKey key = keyClient.importKey("keyName", jsonWebKeyToImport);

        System.out.printf("Imported key with name: %s and id: %s%n", key.getName(), key.getId());
        // END: com.azure.security.keyvault.keys.KeyClient.importKey#String-JsonWebKey

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.importKey#ImportKeyOptions
        ImportKeyOptions options = new ImportKeyOptions("keyName", jsonWebKeyToImport)
            .setHardwareProtected(false);
        KeyVaultKey importedKey = keyClient.importKey(options);

        System.out.printf("Imported key with name: %s and id: %s%n", importedKey.getName(),
            importedKey.getId());
        // END: com.azure.security.keyvault.keys.KeyClient.importKey#ImportKeyOptions

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.importKeyWithResponse#ImportKeyOptions-Context
        ImportKeyOptions importKeyOptions = new ImportKeyOptions("keyName", jsonWebKeyToImport)
            .setHardwareProtected(false);
        Response<KeyVaultKey> response =
            keyClient.importKeyWithResponse(importKeyOptions, new Context("key1", "value1"));

        System.out.printf("Imported key with name: %s and id: %s%n", response.getValue().getName(),
            response.getValue().getId());
        // END: com.azure.security.keyvault.keys.KeyClient.importKeyWithResponse#ImportKeyOptions-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#beginDeleteKey(String)}.
     */
    public void beginDeleteKey() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.deleteKey#String
        SyncPoller<DeletedKey, Void> deleteKeyPoller = keyClient.beginDeleteKey("keyName");
        PollResponse<DeletedKey> deleteKeyPollResponse = deleteKeyPoller.poll();

        // Deleted date only works for SoftDelete Enabled Key Vault.
        DeletedKey deletedKey = deleteKeyPollResponse.getValue();

        System.out.printf("Key delete date: %s%n" + deletedKey.getDeletedOn());
        System.out.printf("Deleted key's recovery id: %s%n", deletedKey.getRecoveryId());

        // Key is being deleted on server.
        deleteKeyPoller.waitForCompletion();
        // Key is deleted
        // END: com.azure.security.keyvault.keys.KeyClient.deleteKey#String
    }

    /**
     * Generates a code sample for using {@link KeyClient#getDeletedKey(String)}.
     */
    public void getDeletedKey() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.getDeletedKey#String
        DeletedKey deletedKey = keyClient.getDeletedKey("keyName");

        System.out.printf("Deleted key's recovery id: %s%n", deletedKey.getRecoveryId());
        // END: com.azure.security.keyvault.keys.KeyClient.getDeletedKey#String
    }

    /**
     * Generates a code sample for using {@link KeyClient#createKeyWithResponse(CreateKeyOptions, Context)},
     * {@link KeyClient#createRsaKeyWithResponse(CreateRsaKeyOptions, Context)},
     * {@link KeyClient#createEcKeyWithResponse(CreateEcKeyOptions, Context)} and
     * {@link KeyClient#createOctKeyWithResponse(CreateOctKeyOptions, Context)}.
     */
    public void createKeyWithResponse() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.createKeyWithResponse#CreateKeyOptions-Context
        CreateKeyOptions createKeyOptions = new CreateKeyOptions("keyName", KeyType.RSA)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        Response<KeyVaultKey> createKeyResponse =
            keyClient.createKeyWithResponse(createKeyOptions, new Context("key1", "value1"));

        System.out.printf("Created key with name: %s and: id %s%n", createKeyResponse.getValue().getName(),
            createKeyResponse.getValue().getId());
        // END: com.azure.security.keyvault.keys.KeyClient.createKeyWithResponse#CreateKeyOptions-Context

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.createRsaKeyWithResponse#CreateRsaKeyOptions-Context
        CreateRsaKeyOptions createRsaKeyOptions = new CreateRsaKeyOptions("keyName")
            .setKeySize(2048)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        Response<KeyVaultKey> createRsaKeyResponse =
            keyClient.createRsaKeyWithResponse(createRsaKeyOptions, new Context("key1", "value1"));

        System.out.printf("Created key with name: %s and: id %s%n", createRsaKeyResponse.getValue().getName(),
            createRsaKeyResponse.getValue().getId());
        // END: com.azure.security.keyvault.keys.KeyClient.createRsaKeyWithResponse#CreateRsaKeyOptions-Context

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.createEcKeyWithResponse#CreateEcKeyOptions-Context
        CreateEcKeyOptions createEcKeyOptions = new CreateEcKeyOptions("keyName")
            .setCurveName(KeyCurveName.P_384)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        Response<KeyVaultKey> createEcKeyResponse =
            keyClient.createEcKeyWithResponse(createEcKeyOptions, new Context("key1", "value1"));

        System.out.printf("Created key with name: %s and: id %s%n", createEcKeyResponse.getValue().getName(),
            createEcKeyResponse.getValue().getId());
        // END: com.azure.security.keyvault.keys.KeyClient.createEcKeyWithResponse#CreateEcKeyOptions-Context

        // BEGIN: com.azure.security.keyvault.keys.async.KeyClient.createOctKey#CreateOctKeyOptions-Context
        CreateOctKeyOptions createOctKeyOptions = new CreateOctKeyOptions("keyName")
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        Response<KeyVaultKey> createOctKeyResponse =
            keyClient.createOctKeyWithResponse(createOctKeyOptions, new Context("key1", "value1"));

        System.out.printf("Created key with name: %s and: id %s%n", createOctKeyResponse.getValue().getName(),
            createOctKeyResponse.getValue().getId());
        // END: com.azure.security.keyvault.keys.async.KeyClient.createOctKey#CreateOctKeyOptions-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#getKeyWithResponse(String, String, Context)}.
     */
    public void getKeyWithResponse() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.getKeyWithResponse#String-String-Context
        String keyVersion = "6A385B124DEF4096AF1361A85B16C204";
        Response<KeyVaultKey> getKeyResponse =
            keyClient.getKeyWithResponse("keyName", keyVersion, new Context("key1", "value1"));

        System.out.printf("Retrieved key with name: %s and: id %s%n", getKeyResponse.getValue().getName(),
            getKeyResponse.getValue().getId());
        // END: com.azure.security.keyvault.keys.KeyClient.getKeyWithResponse#String-String-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#getKey(String)} and
     * {@link KeyClient#getKey(String, String)}.
     */
    public void getKey() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.getKey#String
        KeyVaultKey keyWithVersionValue = keyClient.getKey("keyName");

        System.out.printf("Retrieved key with name: %s and: id %s%n", keyWithVersionValue.getName(),
            keyWithVersionValue.getId());
        // END: com.azure.security.keyvault.keys.KeyClient.getKey#String

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.getKey#String-String
        String keyVersion = "6A385B124DEF4096AF1361A85B16C204";
        KeyVaultKey keyWithVersion = keyClient.getKey("keyName", keyVersion);

        System.out.printf("Retrieved key with name: %s and: id %s%n", keyWithVersion.getName(),
            keyWithVersion.getId());
        // END: com.azure.security.keyvault.keys.KeyClient.getKey#String-String
    }

    /**
     * Generates a code sample for using
     * {@link KeyClient#updateKeyPropertiesWithResponse(KeyProperties, Context, KeyOperation...)}.
     */
    public void updateKeyPropertiesWithResponse() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.updateKeyPropertiesWithResponse#KeyProperties-Context-KeyOperation
        KeyVaultKey key = keyClient.getKey("keyName");

        key.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(60));

        Response<KeyVaultKey> updateKeyResponse =
            keyClient.updateKeyPropertiesWithResponse(key.getProperties(), new Context("key1", "value1"),
                KeyOperation.ENCRYPT, KeyOperation.DECRYPT);

        System.out.printf("Updated key with name: %s and id: %s%n", updateKeyResponse.getValue().getName(),
            updateKeyResponse.getValue().getId());
        // END: com.azure.security.keyvault.keys.KeyClient.updateKeyPropertiesWithResponse#KeyProperties-Context-KeyOperation
    }

    /**
     * Generates a code sample for using {@link KeyClient#updateKeyProperties(KeyProperties, KeyOperation...)}.
     */
    public void updateKeyProperties() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.updateKeyProperties#KeyProperties-KeyOperation
        KeyVaultKey key = keyClient.getKey("keyName");

        key.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(60));

        KeyVaultKey updatedKey = keyClient.updateKeyProperties(key.getProperties(), KeyOperation.ENCRYPT,
            KeyOperation.DECRYPT);

        System.out.printf("Key is updated with name %s and id %s %n", updatedKey.getName(), updatedKey.getId());
        // END: com.azure.security.keyvault.keys.KeyClient.updateKeyProperties#KeyProperties-KeyOperation
    }

    /**
     * Generates a code sample for using {@link KeyClient#getDeletedKeyWithResponse(String, Context)}.
     */
    public void getDeletedKeyWithResponse() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.getDeletedKeyWithResponse#String-Context
        Response<DeletedKey> deletedKeyResponse =
            keyClient.getDeletedKeyWithResponse("keyName", new Context("key1", "value1"));

        System.out.printf("Deleted key with recovery id: %s%n", deletedKeyResponse.getValue().getRecoveryId());
        // END: com.azure.security.keyvault.keys.KeyClient.getDeletedKeyWithResponse#String-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#purgeDeletedKey(String)}.
     */
    public void purgeDeletedKey() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.purgeDeletedKey#String
        keyClient.purgeDeletedKey("deletedKeyName");
        // END: com.azure.security.keyvault.keys.KeyClient.purgeDeletedKey#String
    }

    /**
     * Generates a code sample for using {@link KeyClient#purgeDeletedKeyWithResponse(String, Context)}.
     */
    public void purgeDeletedKeyWithResponse() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.purgeDeletedKeyWithResponse#String-Context
        Response<Void> purgeDeletedKeyResponse = keyClient.purgeDeletedKeyWithResponse("deletedKeyName",
            new Context("key1", "value1"));

        System.out.printf("Purge response status code: %d%n", purgeDeletedKeyResponse.getStatusCode());
        // END: com.azure.security.keyvault.keys.KeyClient.purgeDeletedKeyWithResponse#String-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#beginRecoverDeletedKey(String)}.
     */
    public void beginRecoverDeletedKey() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.recoverDeletedKey#String
        SyncPoller<KeyVaultKey, Void> recoverKeyPoller = keyClient.beginRecoverDeletedKey("deletedKeyName");

        PollResponse<KeyVaultKey> recoverKeyPollResponse = recoverKeyPoller.poll();

        KeyVaultKey recoveredKey = recoverKeyPollResponse.getValue();
        System.out.printf("Recovered key name: %s%n", recoveredKey.getName());
        System.out.printf("Recovered key id: %s%n", recoveredKey.getId());

        // Key is being recovered on server.
        recoverKeyPoller.waitForCompletion();
        // Key is recovered
        // END: com.azure.security.keyvault.keys.KeyClient.recoverDeletedKey#String
    }

    /**
     * Generates a code sample for using {@link KeyClient#backupKey(String)}.
     */
    public void backupKey() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.backupKey#String
        byte[] keyBackup = keyClient.backupKey("keyName");

        System.out.printf("Key backup byte array length: %s%n", keyBackup.length);
        // END: com.azure.security.keyvault.keys.KeyClient.backupKey#String
    }

    /**
     * Generates a code sample for using {@link KeyClient#backupKeyWithResponse(String, Context)}.
     */
    public void backupKeyWithResponse() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.backupKeyWithResponse#String-Context
        Response<byte[]> backupKeyResponse = keyClient.backupKeyWithResponse("keyName", new Context("key1", "value1"));

        System.out.printf("Key backup byte array length: %s%n", backupKeyResponse.getValue().length);
        // END: com.azure.security.keyvault.keys.KeyClient.backupKeyWithResponse#String-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#restoreKeyBackup}.
     */
    public void restoreKeyBackup() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.restoreKeyBackup#byte
        byte[] keyBackupByteArray = {};
        KeyVaultKey keyResponse = keyClient.restoreKeyBackup(keyBackupByteArray);
        System.out.printf("Restored key with name: %s and: id %s%n", keyResponse.getName(), keyResponse.getId());
        // END: com.azure.security.keyvault.keys.KeyClient.restoreKeyBackup#byte
    }

    /**
     * Generates a code sample for using {@link KeyClient#restoreKeyBackupWithResponse(byte[], Context)}.
     */
    public void restoreKeyBackupWithResponse() {
        KeyClient keyClient = createClient();
        byte[] keyBackupByteArray = {};
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.restoreKeyBackupWithResponse#byte-Context
        Response<KeyVaultKey> keyResponse = keyClient.restoreKeyBackupWithResponse(keyBackupByteArray,
            new Context("key1", "value1"));

        System.out.printf("Restored key with name: %s and: id %s%n",
            keyResponse.getValue().getName(), keyResponse.getValue().getId());
        // END: com.azure.security.keyvault.keys.KeyClient.restoreKeyBackupWithResponse#byte-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#listPropertiesOfKeys()} and
     * {@link KeyClient#listPropertiesOfKeys(Context)}.
     */
    public void listPropertiesOfKeys() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeys
        for (KeyProperties keyProperties : keyClient.listPropertiesOfKeys()) {
            KeyVaultKey key = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());

            System.out.printf("Retrieved key with name: %s and type: %s%n", key.getName(), key.getKeyType());
        }
        // END: com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeys

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeys#Context
        for (KeyProperties keyProperties : keyClient.listPropertiesOfKeys(new Context("key1", "value1"))) {
            KeyVaultKey key = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());

            System.out.printf("Retrieved key with name: %s and type: %s%n", key.getName(),
                key.getKeyType());
        }
        // END: com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeys#Context

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage
        keyClient.listPropertiesOfKeys().iterableByPage().forEach(pagedResponse -> {
            System.out.printf("Got response details. Url: %s. Status code: %d.%n",
                pagedResponse.getRequest().getUrl(), pagedResponse.getStatusCode());
            pagedResponse.getElements().forEach(keyProperties -> {
                KeyVaultKey key = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());

                System.out.printf("Retrieved key with name: %s and type: %s%n", key.getName(),
                    key.getKeyType());
            });
        });
        // END: com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage
    }

    /**
     * Generates a code sample for using {@link KeyClient#listDeletedKeys()} and
     * {@link KeyClient#listDeletedKeys(Context)}.
     */
    public void listDeletedKeys() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.listDeletedKeys
        for (DeletedKey deletedKey : keyClient.listDeletedKeys()) {
            System.out.printf("Deleted key's recovery id:%s%n", deletedKey.getRecoveryId());
        }
        // END: com.azure.security.keyvault.keys.KeyClient.listDeletedKeys

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.listDeletedKeys#Context
        for (DeletedKey deletedKey : keyClient.listDeletedKeys(new Context("key1", "value1"))) {
            System.out.printf("Deleted key's recovery id:%s%n", deletedKey.getRecoveryId());
        }
        // END: com.azure.security.keyvault.keys.KeyClient.listDeletedKeys#Context

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage
        keyClient.listDeletedKeys().iterableByPage().forEach(pagedResponse -> {
            System.out.printf("Got response details. Url: %s. Status code: %d.%n",
                pagedResponse.getRequest().getUrl(), pagedResponse.getStatusCode());
            pagedResponse.getElements().forEach(deletedKey ->
                System.out.printf("Deleted key's recovery id:%s%n", deletedKey.getRecoveryId()));
        });
        // END: com.azure.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage
    }

    /**
     * Generates code sample for using {@link KeyClient#listPropertiesOfKeyVersions(String)} and
     * {@link KeyClient#listPropertiesOfKeyVersions(String, Context)}.
     */
    public void listPropertiesOfKeyVersions() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String
        for (KeyProperties keyProperties : keyClient.listPropertiesOfKeyVersions("keyName")) {
            KeyVaultKey key = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());

            System.out.printf("Retrieved key version: %s with name: %s and type: %s%n",
                key.getProperties().getVersion(), key.getName(), key.getKeyType());
        }
        // END: com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String-Context
        for (KeyProperties keyProperties : keyClient.listPropertiesOfKeyVersions("keyName", new Context("key1", "value1"))) {
            KeyVaultKey key = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());

            System.out.printf("Retrieved key version: %s with name: %s and type: %s%n",
                key.getProperties().getVersion(), key.getName(), key.getKeyType());
        }
        // END: com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String-Context

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage
        keyClient.listPropertiesOfKeyVersions("keyName").iterableByPage().forEach(pagedResponse -> {
            System.out.printf("Got response details. Url: %s. Status code: %d.%n",
                pagedResponse.getRequest().getUrl(), pagedResponse.getStatusCode());
            pagedResponse.getElements().forEach(keyProperties ->
                System.out.printf("Key name: %s. Key version: %s.%n", keyProperties.getName(),
                    keyProperties.getVersion()));
        });
        // END: com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage
    }

    /**
     * Generates code samples for using {@link KeyClient#getRandomBytes(int)} and
     * {@link KeyClient#getRandomBytesWithResponse(int, Context)}.
     */
    public void getRandomBytes() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.getRandomBytes#int
        int amount = 16;
        RandomBytes randomBytes = keyClient.getRandomBytes(amount);

        System.out.printf("Retrieved %d random bytes: %s%n", amount, Arrays.toString(randomBytes.getBytes()));
        // END: com.azure.security.keyvault.keys.KeyClient.getRandomBytes#int

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.getRandomBytesWithResponse#int-Context
        int amountOfBytes = 16;
        Response<RandomBytes> response =
            keyClient.getRandomBytesWithResponse(amountOfBytes, new Context("key1", "value1"));

        System.out.printf("Response received successfully with status code: %d. Retrieved %d random bytes: %s%n",
            response.getStatusCode(), amountOfBytes, Arrays.toString(response.getValue().getBytes()));
        // END: com.azure.security.keyvault.keys.KeyClient.getRandomBytesWithResponse#int-Context
    }

    /**
     * Generates code samples for using {@link KeyClient#releaseKey(String, String)},
     * {@link KeyClient#releaseKey(String, String, String)} and
     * {@link KeyClient#releaseKeyWithResponse(String, String, String, ReleaseKeyOptions, Context)}.
     */
    public void releaseKey() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.releaseKey#String-String
        String target = "someAttestationToken";
        ReleaseKeyResult releaseKeyResult = keyClient.releaseKey("keyName", target);

        System.out.printf("Signed object containing released key: %s%n", releaseKeyResult);
        // END: com.azure.security.keyvault.keys.KeyClient.releaseKey#String-String

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.releaseKey#String-String-String
        String myKeyVersion = "6A385B124DEF4096AF1361A85B16C204";
        String myTarget = "someAttestationToken";
        ReleaseKeyResult releaseKeyVersionResult = keyClient.releaseKey("keyName", myKeyVersion, myTarget);

        System.out.printf("Signed object containing released key: %s%n", releaseKeyVersionResult);
        // END: com.azure.security.keyvault.keys.KeyClient.releaseKey#String-String-String

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.releaseKeyWithResponse#String-String-String-ReleaseKeyOptions-Context
        String releaseKeyVersion = "6A385B124DEF4096AF1361A85B16C204";
        String releaseTarget = "someAttestationToken";
        ReleaseKeyOptions releaseKeyOptions = new ReleaseKeyOptions()
            .setAlgorithm(KeyExportEncryptionAlgorithm.RSA_AES_KEY_WRAP_256)
            .setNonce("someNonce");

        Response<ReleaseKeyResult> releaseKeyResultResponse =
            keyClient.releaseKeyWithResponse("keyName", releaseKeyVersion, releaseTarget, releaseKeyOptions,
                new Context("key1", "value1"));

        System.out.printf("Response received successfully with status code: %d. Signed object containing"
                + "released key: %s%n", releaseKeyResultResponse.getStatusCode(),
            releaseKeyResultResponse.getValue().getValue());
        // END: com.azure.security.keyvault.keys.KeyClient.releaseKeyWithResponse#String-String-String-ReleaseKeyOptions-Context
    }

    /**
     * Generates code samples for using {@link KeyClient#rotateKey(String)} and
     * {@link KeyClient#rotateKeyWithResponse(String, Context)}.
     */
    public void rotateKey() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String
        KeyVaultKey key = keyClient.rotateKey("keyName");

        System.out.printf("Rotated key with name: %s and version:%s%n", key.getName(),
            key.getProperties().getVersion());
        // END: com.azure.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String-Context
        Response<KeyVaultKey> keyResponse = keyClient.rotateKeyWithResponse("keyName", new Context("key1", "value1"));

        System.out.printf("Response received successfully with status code: %d. Rotated key with name: %s and"
                + "version: %s%n", keyResponse.getStatusCode(), keyResponse.getValue().getName(),
            keyResponse.getValue().getProperties().getVersion());
        // END: com.azure.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String-Context
    }

    /**
     * Generates code samples for using {@link KeyClient#getKeyRotationPolicy(String)} and
     * {@link KeyClient#getKeyRotationPolicyWithResponse(String, Context)}.
     */
    public void getKeyRotationPolicy() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.getKeyRotationPolicy#String
        KeyRotationPolicy keyRotationPolicy = keyClient.getKeyRotationPolicy("keyName");

        System.out.printf("Retrieved key rotation policy with id: %s%n", keyRotationPolicy.getId());
        // END: com.azure.security.keyvault.keys.KeyClient.getKeyRotationPolicy#String

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.getKeyRotationPolicyWithResponse#String-Context
        Response<KeyRotationPolicy> keyRotationPolicyResponse =
            keyClient.getKeyRotationPolicyWithResponse("keyName", new Context("key1", "value1"));

        System.out.printf("Response received successfully with status code: %d. Retrieved key rotation policy"
            + "with id: %s%n", keyRotationPolicyResponse.getStatusCode(), keyRotationPolicyResponse.getValue().getId());
        // END: com.azure.security.keyvault.keys.KeyClient.getKeyRotationPolicyWithResponse#String-Context
    }

    /**
     * Generates code samples for using {@link KeyClient#updateKeyRotationPolicy(String, KeyRotationPolicyProperties)}
     * and {@link KeyClient#updateKeyRotationPolicyWithResponse(String, KeyRotationPolicyProperties, Context)}.
     */
    public void updateKeyRotationPolicy() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyClient.updateKeyRotationPolicy#String-KeyRotationPolicyProperties
        List<KeyRotationLifetimeAction> lifetimeActions = new ArrayList<>();
        KeyRotationLifetimeAction rotateLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.ROTATE)
            .setTimeAfterCreate("P90D");
        KeyRotationLifetimeAction notifyLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.NOTIFY)
            .setTimeBeforeExpiry("P45D");

        lifetimeActions.add(rotateLifetimeAction);
        lifetimeActions.add(notifyLifetimeAction);

        KeyRotationPolicyProperties policyProperties = new KeyRotationPolicyProperties()
            .setLifetimeActions(lifetimeActions)
            .setExpiryTime("P6M");

        KeyRotationPolicy keyRotationPolicy =
            keyClient.updateKeyRotationPolicy("keyName", policyProperties);

        System.out.printf("Updated key rotation policy with id: %s%n", keyRotationPolicy.getId());
        // END: com.azure.security.keyvault.keys.KeyClient.updateKeyRotationPolicy#String-KeyRotationPolicyProperties

        // BEGIN: com.azure.security.keyvault.keys.KeyClient.updateKeyRotationPolicyWithResponse#String-KeyRotationPolicyProperties-Context
        List<KeyRotationLifetimeAction> myLifetimeActions = new ArrayList<>();
        KeyRotationLifetimeAction myRotateLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.ROTATE)
            .setTimeAfterCreate("P90D");
        KeyRotationLifetimeAction myNotifyLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.NOTIFY)
            .setTimeBeforeExpiry("P45D");

        myLifetimeActions.add(myRotateLifetimeAction);
        myLifetimeActions.add(myNotifyLifetimeAction);

        KeyRotationPolicyProperties myPolicyProperties = new KeyRotationPolicyProperties()
            .setLifetimeActions(myLifetimeActions)
            .setExpiryTime("P6M");

        Response<KeyRotationPolicy> keyRotationPolicyResponse = keyClient.updateKeyRotationPolicyWithResponse(
            "keyName", myPolicyProperties, new Context("key1", "value1"));

        System.out.printf("Response received successfully with status code: %d. Updated key rotation policy"
            + "with id: %s%n", keyRotationPolicyResponse.getStatusCode(), keyRotationPolicyResponse.getValue().getId());
        // END: com.azure.security.keyvault.keys.KeyClient.updateKeyRotationPolicyWithResponse#String-KeyRotationPolicyProperties-Context
    }
}
