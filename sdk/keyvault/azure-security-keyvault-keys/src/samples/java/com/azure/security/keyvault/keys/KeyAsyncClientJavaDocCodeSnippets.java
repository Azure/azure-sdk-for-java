// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.ImportKeyOptions;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyExportEncryptionAlgorithm;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyRotationLifetimeAction;
import com.azure.security.keyvault.keys.models.KeyRotationPolicyAction;
import com.azure.security.keyvault.keys.models.KeyRotationPolicyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.ReleaseKeyOptions;
import reactor.util.context.Context;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyAsyncClient}.
 */
public final class KeyAsyncClientJavaDocCodeSnippets {
    /**
     * Generates code sample for creating a {@link KeyAsyncClient}.
     *
     * @return An instance of {@link KeyAsyncClient}.
     */
    public KeyAsyncClient createAsyncClient() {
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.instantiation
        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
            .vaultUrl("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.instantiation
        return keyAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link KeyAsyncClient} using a custom {@link HttpClient}.
     *
     * @return An instance of {@link KeyAsyncClient}.
     */
    public KeyAsyncClient createAsyncClientWithHttpClient() {
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.instantiation.withHttpClient
        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
            .vaultUrl("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .httpClient(HttpClient.createDefault())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.instantiation.withHttpClient
        return keyAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link KeyAsyncClient}
     * @return An instance of {@link KeyAsyncClient}
     */
    public KeyAsyncClient createAsyncClientWithPipeline() {
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.instantiation.withHttpPipeline
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new KeyVaultCredentialPolicy(new DefaultAzureCredentialBuilder().build()), new RetryPolicy())
            .build();
        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
            .pipeline(pipeline)
            .vaultUrl("https://myvault.azure.net/")
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.instantiation.withHttpPipeline
        return keyAsyncClient;
    }

    /**
     * Generates code samples for using {@link KeyAsyncClient#createKey(String, KeyType)},
     * {@link KeyAsyncClient#createKey(CreateKeyOptions)},
     * {@link KeyAsyncClient#createRsaKey(CreateRsaKeyOptions)},
     * {@link KeyAsyncClient#createEcKey(CreateEcKeyOptions)} and
     * {@link KeyAsyncClient#createOctKey(CreateOctKeyOptions)}.
     */
    public void createKey() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.createKey#String-KeyType
        keyAsyncClient.createKey("keyName", KeyType.EC)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(key ->
                System.out.printf("Created key with name: %s and id: %s %n", key.getName(),
                    key.getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.createKey#String-KeyType

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.createKey#CreateKeyOptions
        CreateKeyOptions createKeyOptions = new CreateKeyOptions("keyName", KeyType.RSA)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));

        keyAsyncClient.createKey(createKeyOptions)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(key ->
                System.out.printf("Created key with name: %s and id: %s %n", key.getName(),
                    key.getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.createKey#CreateKeyOptions

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.createRsaKey#CreateRsaKeyOptions
        CreateRsaKeyOptions createRsaKeyOptions = new CreateRsaKeyOptions("keyName")
            .setKeySize(2048)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));

        keyAsyncClient.createRsaKey(createRsaKeyOptions)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(rsaKey ->
                System.out.printf("Created key with name: %s and id: %s %n", rsaKey.getName(),
                    rsaKey.getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.createRsaKey#CreateRsaKeyOptions

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.createEcKey#CreateEcKeyOptions
        CreateEcKeyOptions createEcKeyOptions = new CreateEcKeyOptions("keyName")
            .setCurveName(KeyCurveName.P_384)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));

        keyAsyncClient.createEcKey(createEcKeyOptions)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(ecKey ->
                System.out.printf("Created key with name: %s and id: %s %n", ecKey.getName(),
                    ecKey.getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.createEcKey#CreateEcKeyOptions

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.createOctKey#CreateOctKeyOptions
        CreateOctKeyOptions createOctKeyOptions = new CreateOctKeyOptions("keyName")
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));

        keyAsyncClient.createOctKey(createOctKeyOptions)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(octKey ->
                System.out.printf("Created key with name: %s and id: %s %n", octKey.getName(),
                    octKey.getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.createOctKey#CreateOctKeyOptions
    }

    /**
     * Generates code samples for using {@link KeyAsyncClient#importKey(String, JsonWebKey)},
     * {@link KeyAsyncClient#importKey(ImportKeyOptions)} and
     * {@link KeyAsyncClient#importKeyWithResponse(ImportKeyOptions)}.
     */
    public void importKey() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        JsonWebKey jsonWebKeyToImport = new JsonWebKey();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.importKey#String-JsonWebKey
        keyAsyncClient.importKey("keyName", jsonWebKeyToImport)
            .subscribe(keyVaultKey ->
                System.out.printf("Imported key with name: %s and id: %s%n", keyVaultKey.getName(),
                    keyVaultKey.getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.importKey#String-JsonWebKey

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.importKey#ImportKeyOptions
        ImportKeyOptions options = new ImportKeyOptions("keyName", jsonWebKeyToImport)
            .setHardwareProtected(false);

        keyAsyncClient.importKey(options).subscribe(keyVaultKey ->
            System.out.printf("Imported key with name: %s and id: %s%n", keyVaultKey.getName(), keyVaultKey.getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.importKey#ImportKeyOptions

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.importKeyWithResponse#ImportKeyOptions
        ImportKeyOptions importKeyOptions = new ImportKeyOptions("keyName", jsonWebKeyToImport)
            .setHardwareProtected(false);

        keyAsyncClient.importKeyWithResponse(importKeyOptions).subscribe(response ->
            System.out.printf("Imported key with name: %s and id: %s%n", response.getValue().getName(),
                response.getValue().getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.importKeyWithResponse#ImportKeyOptions
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#beginDeleteKey(String)}.
     */
    public void beginDeleteKey() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.deleteKey#String
        keyAsyncClient.beginDeleteKey("keyName")
            .subscribe(pollResponse -> {
                System.out.printf("Deletion status: %s%n", pollResponse.getStatus());
                System.out.printf("Key name: %s%n", pollResponse.getValue().getName());
                System.out.printf("Key delete date: %s%n", pollResponse.getValue().getDeletedOn());
            });
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.deleteKey#String
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#getDeletedKey(String)}.
     */
    public void getDeletedKey() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.getDeletedKey#String
        keyAsyncClient.getDeletedKey("keyName")
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(deletedKey ->
                System.out.printf("Deleted key's recovery id:%s%n", deletedKey.getRecoveryId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.getDeletedKey#String
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#createKeyWithResponse(CreateKeyOptions)},
     * {@link KeyAsyncClient#createRsaKeyWithResponse(CreateRsaKeyOptions)},
     * {@link KeyAsyncClient#createEcKeyWithResponse(CreateEcKeyOptions)} and
     * {@link KeyAsyncClient#createOctKeyWithResponse(CreateOctKeyOptions)}.
     */
    public void createKeyWithResponse() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.createKeyWithResponse#CreateKeyOptions
        CreateKeyOptions createKeyOptions = new CreateKeyOptions("keyName", KeyType.RSA)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));

        keyAsyncClient.createKeyWithResponse(createKeyOptions)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(createKeyResponse ->
                System.out.printf("Created key with name: %s and: id %s%n", createKeyResponse.getValue().getName(),
                    createKeyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.createKeyWithResponse#CreateKeyOptions

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.createRsaKeyWithResponse#CreateRsaKeyOptions
        CreateRsaKeyOptions createRsaKeyOptions = new CreateRsaKeyOptions("keyName")
            .setKeySize(2048)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));

        keyAsyncClient.createRsaKeyWithResponse(createRsaKeyOptions)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(createRsaKeyResponse ->
                System.out.printf("Created key with name: %s and: id %s%n", createRsaKeyResponse.getValue().getName(),
                    createRsaKeyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.createRsaKeyWithResponse#CreateRsaKeyOptions

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.createEcKeyWithResponse#CreateEcKeyOptions
        CreateEcKeyOptions createEcKeyOptions = new CreateEcKeyOptions("keyName")
            .setCurveName(KeyCurveName.P_384)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));

        keyAsyncClient.createEcKeyWithResponse(createEcKeyOptions)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(createEcKeyResponse ->
                System.out.printf("Created key with name: %s and: id %s%n", createEcKeyResponse.getValue().getName(),
                    createEcKeyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.createEcKeyWithResponse#CreateEcKeyOptions

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.createOctKeyWithResponse#CreateOctKeyOptions
        CreateOctKeyOptions createOctKeyOptions = new CreateOctKeyOptions("keyName")
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));

        keyAsyncClient.createOctKeyWithResponse(createOctKeyOptions)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(createOctKeyResponse ->
                System.out.printf("Created key with name: %s and: id %s%n", createOctKeyResponse.getValue().getName(),
                    createOctKeyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.createOctKeyWithResponse#CreateOctKeyOptions
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#getKeyWithResponse(String, String)}.
     */
    public void getKeyWithResponse() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.getKeyWithResponse#String-String
        String keyVersion = "6A385B124DEF4096AF1361A85B16C204";

        keyAsyncClient.getKeyWithResponse("keyName", keyVersion)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(getKeyResponse ->
                System.out.printf("Created key with name: %s and: id %s%n",
                    getKeyResponse.getValue().getName(), getKeyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.getKeyWithResponse#String-String
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#getKey(String, String)}.
     */
    public void getKey() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.getKey#String
        keyAsyncClient.getKey("keyName")
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(key ->
                System.out.printf("Created key with name: %s and: id %s%n", key.getName(),
                    key.getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.getKey#String

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.getKey#String-String
        String keyVersion = "6A385B124DEF4096AF1361A85B16C204";

        keyAsyncClient.getKey("keyName", keyVersion)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(key ->
                System.out.printf("Created key with name: %s and: id %s%n", key.getName(),
                    key.getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.getKey#String-String
    }

    /**
     * Generates a code sample for using
     * {@link KeyAsyncClient#updateKeyPropertiesWithResponse(KeyProperties, KeyOperation...)}.
     */
    public void updateKeyPropertiesWithResponse() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.updateKeyPropertiesWithResponse#KeyProperties-KeyOperation
        keyAsyncClient.getKey("keyName")
            .subscribe(getKeyResponse -> {
                //Update the not before time of the key.
                getKeyResponse.getProperties().setNotBefore(OffsetDateTime.now().plusDays(50));
                keyAsyncClient.updateKeyPropertiesWithResponse(getKeyResponse.getProperties(), KeyOperation.ENCRYPT,
                        KeyOperation.DECRYPT)
                    .contextWrite(Context.of("key1", "value1", "key2", "value2"))
                    .subscribe(updateKeyResponse ->
                        System.out.printf("Updated key's \"not before time\": %s%n",
                            updateKeyResponse.getValue().getProperties().getNotBefore().toString()));
            });
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.updateKeyPropertiesWithResponse#KeyProperties-KeyOperation
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#updateKeyProperties(KeyProperties, KeyOperation...)}.
     */
    public void updateKeyProperties() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.updateKeyProperties#KeyProperties-KeyOperation
        keyAsyncClient.getKey("keyName")
            .subscribe(key -> {
                //Update the not before time of the key.
                key.getProperties().setNotBefore(OffsetDateTime.now().plusDays(50));
                keyAsyncClient.updateKeyProperties(key.getProperties(), KeyOperation.ENCRYPT,
                        KeyOperation.DECRYPT)
                    .contextWrite(Context.of("key1", "value1", "key2", "value2"))
                    .subscribe(updatedKey ->
                        System.out.printf("Updated key's \"not before time\": %s%n",
                            updatedKey.getProperties().getNotBefore().toString()));
            });
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.updateKeyProperties#KeyProperties-KeyOperation
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#getDeletedKeyWithResponse(String)}.
     */
    public void getDeletedKeyWithResponse() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.getDeletedKeyWithResponse#String
        keyAsyncClient.getDeletedKeyWithResponse("keyName")
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(getDeletedKeyResponse ->
                System.out.printf("Deleted key's recovery id: %s%n", getDeletedKeyResponse.getValue().getRecoveryId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.getDeletedKeyWithResponse#String
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#purgeDeletedKey(String)}.
     */
    public void purgeDeletedKey() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.purgeDeletedKey#String
        keyAsyncClient.purgeDeletedKey("deletedKeyName")
            .subscribe(ignored ->
                System.out.println("Successfully purged deleted key"));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.purgeDeletedKey#String
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#purgeDeletedKeyWithResponse(String)}.
     */
    public void purgeDeletedKeyWithResponse() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.purgeDeletedKeyWithResponse#String
        keyAsyncClient.purgeDeletedKeyWithResponse("deletedKeyName")
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(purgeDeletedKeyResponse ->
                System.out.printf("Purge response status code: %d%n", purgeDeletedKeyResponse.getStatusCode()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.purgeDeletedKeyWithResponse#String
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#beginRecoverDeletedKey(String)}.
     */
    public void beginRecoverDeletedKey() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.recoverDeletedKey#String
        keyAsyncClient.beginRecoverDeletedKey("deletedKeyName")
            .subscribe(pollResponse -> {
                System.out.printf("Recovery status: %s%n", pollResponse.getStatus());
                System.out.printf("Key name: %s%n", pollResponse.getValue().getName());
                System.out.printf("Key type: %s%n", pollResponse.getValue().getKeyType());
            });
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.recoverDeletedKey#String
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#backupKey(String)}.
     */
    public void backupKey() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.backupKey#String
        keyAsyncClient.backupKey("keyName")
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(bytes ->
                System.out.printf("Key backup byte array length: %s%n", bytes.length));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.backupKey#String
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#backupKeyWithResponse(String)}.
     */
    public void backupKeyWithResponse() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.backupKeyWithResponse#String
        keyAsyncClient.backupKeyWithResponse("keyName")
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(backupKeyResponse ->
                System.out.printf("Key backup byte array length: %s%n", backupKeyResponse.getValue().length));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.backupKeyWithResponse#String
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#restoreKeyBackup}.
     */
    public void restoreKeyBackup() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        byte[] keyBackupByteArray = {};
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.restoreKeyBackup#byte
        keyAsyncClient.restoreKeyBackup(keyBackupByteArray)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(restoreKeyResponse ->
                System.out.printf("Restored key with name: %s and: id %s%n", restoreKeyResponse.getName(),
                    restoreKeyResponse.getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.restoreKeyBackup#byte
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#restoreKeyBackupWithResponse(byte[])}.
     */
    public void restoreKeyBackupWithResponse() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        byte[] keyBackupByteArray = {};
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.restoreKeyBackupWithResponse#byte
        keyAsyncClient.restoreKeyBackupWithResponse(keyBackupByteArray)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(restoreKeyBackupResponse ->
                System.out.printf("Restored key with name: %s and: id %s%n",
                    restoreKeyBackupResponse.getValue().getName(), restoreKeyBackupResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.restoreKeyBackupWithResponse#byte
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#listPropertiesOfKeys}.
     */
    public void listPropertiesOfKeys() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.listPropertiesOfKeys
        keyAsyncClient.listPropertiesOfKeys()
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .flatMap(keyProperties -> keyAsyncClient.getKey(keyProperties.getName(), keyProperties.getVersion()))
            .subscribe(key -> System.out.printf("Retrieved key with name: %s and type: %s%n",
                key.getName(),
                key.getKeyType()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.listPropertiesOfKeys
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#listDeletedKeys()}.
     */
    public void listDeletedKeys() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.listDeletedKeys
        keyAsyncClient.listDeletedKeys()
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(deletedKey ->
                System.out.printf("Deleted key's recovery id:%s%n", deletedKey.getRecoveryId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.listDeletedKeys
    }

    /**
     * Generates code sample for using {@link KeyAsyncClient#listPropertiesOfKeyVersions(String)}.
     */
    public void listPropertiesOfKeyVersions() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.listKeyVersions
        keyAsyncClient.listPropertiesOfKeyVersions("keyName")
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .flatMap(keyProperties -> keyAsyncClient.getKey(keyProperties.getName(), keyProperties.getVersion()))
            .subscribe(key ->
                System.out.printf("Retrieved key version: %s with name: %s and type: %s%n",
                    key.getProperties().getVersion(), key.getName(), key.getKeyType()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.listKeyVersions
    }

    /**
     * Generates code samples for using {@link KeyAsyncClient#getRandomBytes(int)} and
     * {@link KeyAsyncClient#getRandomBytesWithResponse(int)}.
     */
    public void getRandomBytes() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.getRandomBytes#int
        int amount = 16;
        keyAsyncClient.getRandomBytes(amount)
            .subscribe(randomBytes ->
                System.out.printf("Retrieved %d random bytes: %s%n", amount, Arrays.toString(randomBytes.getBytes())));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.getRandomBytes#int

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.getRandomBytesWithResponse#int
        int amountOfBytes = 16;
        keyAsyncClient.getRandomBytesWithResponse(amountOfBytes).subscribe(response ->
            System.out.printf("Response received successfully with status code: %d. Retrieved %d random bytes: %s%n",
                response.getStatusCode(), amountOfBytes, Arrays.toString(response.getValue().getBytes())));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.getRandomBytesWithResponse#int
    }

    /**
     * Generates code samples for using {@link KeyAsyncClient#releaseKey(String, String)},
     * {@link KeyAsyncClient#releaseKey(String, String, String)} and
     * {@link KeyAsyncClient#releaseKeyWithResponse(String, String, String, ReleaseKeyOptions)}.
     */
    public void releaseKey() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.releaseKey#String-String
        String target = "someAttestationToken";

        keyAsyncClient.releaseKey("keyName", target)
            .subscribe(releaseKeyResult ->
                System.out.printf("Signed object containing released key: %s%n", releaseKeyResult.getValue()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.releaseKey#String-String

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.releaseKey#String-String-String
        String myKeyVersion = "6A385B124DEF4096AF1361A85B16C204";
        String myTarget = "someAttestationToken";

        keyAsyncClient.releaseKey("keyName", myKeyVersion, myTarget)
            .subscribe(releaseKeyResult ->
                System.out.printf("Signed object containing released key: %s%n", releaseKeyResult.getValue()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.releaseKey#String-String-String

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.releaseKeyWithResponse#String-String-String-ReleaseKeyOptions
        String releaseKeyVersion = "6A385B124DEF4096AF1361A85B16C204";
        String releaseTarget = "someAttestationToken";
        ReleaseKeyOptions releaseKeyOptions = new ReleaseKeyOptions()
            .setAlgorithm(KeyExportEncryptionAlgorithm.RSA_AES_KEY_WRAP_256)
            .setNonce("someNonce");

        keyAsyncClient.releaseKeyWithResponse("keyName", releaseKeyVersion, releaseTarget, releaseKeyOptions)
            .subscribe(releaseKeyResponse ->
                System.out.printf("Response received successfully with status code: %d. Signed object containing"
                        + "released key: %s%n", releaseKeyResponse.getStatusCode(),
                    releaseKeyResponse.getValue().getValue()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.releaseKeyWithResponse#String-String-String-ReleaseKeyOptions
    }

    /**
     * Generates code samples for using {@link KeyAsyncClient#rotateKey(String)} and
     * {@link KeyAsyncClient#rotateKeyWithResponse(String)}.
     */
    public void rotateKey() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.rotateKey#String
        keyAsyncClient.rotateKey("keyName")
            .subscribe(key ->
                System.out.printf("Rotated key with name: %s and version:%s%n", key.getName(),
                    key.getProperties().getVersion()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.rotateKey#String

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.rotateKeyWithResponse#String
        keyAsyncClient.rotateKeyWithResponse("keyName")
            .subscribe(rotateKeyResponse ->
                System.out.printf("Response received successfully with status code: %d. Rotated key with name: %s and"
                        + "version: %s%n", rotateKeyResponse.getStatusCode(), rotateKeyResponse.getValue().getName(),
                    rotateKeyResponse.getValue().getProperties().getVersion()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.rotateKeyWithResponse#String
    }

    /**
     * Generates code samples for using {@link KeyAsyncClient#getKeyRotationPolicy(String)} and
     * {@link KeyAsyncClient#getKeyRotationPolicyWithResponse(String)}.
     */
    public void getKeyRotationPolicy() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.getKeyRotationPolicy#String
        keyAsyncClient.getKeyRotationPolicy("keyName")
            .subscribe(keyRotationPolicy ->
                System.out.printf("Retrieved key rotation policy with id: %s%n", keyRotationPolicy.getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.getKeyRotationPolicy#String

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.getKeyRotationPolicyWithResponse#String
        keyAsyncClient.getKeyRotationPolicyWithResponse("keyName")
            .subscribe(getKeyRotationPolicyResponse ->
                System.out.printf("Response received successfully with status code: %d. Retrieved key rotation policy"
                    + "with id: %s%n", getKeyRotationPolicyResponse.getStatusCode(),
                    getKeyRotationPolicyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.getKeyRotationPolicyWithResponse#String
    }

    /**
     * Generates code samples for using
     * {@link KeyAsyncClient#updateKeyRotationPolicy(String, KeyRotationPolicyProperties)} and
     * {@link KeyAsyncClient#updateKeyRotationPolicyWithResponse(String, KeyRotationPolicyProperties)}.
     */
    public void updateKeyRotationPolicy() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.updateKeyRotationPolicy#String-KeyRotationPolicyProperties
        List<KeyRotationLifetimeAction> lifetimeActions = new ArrayList<>();
        KeyRotationLifetimeAction rotateLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.ROTATE)
            .setTimeAfterCreate("P90D")
            .setTimeBeforeExpiry("P45D");
        KeyRotationLifetimeAction notifyLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.NOTIFY)
            .setTimeAfterCreate("P90D")
            .setTimeBeforeExpiry("P45D");

        lifetimeActions.add(rotateLifetimeAction);
        lifetimeActions.add(notifyLifetimeAction);

        KeyRotationPolicyProperties policyProperties = new KeyRotationPolicyProperties()
            .setLifetimeActions(lifetimeActions)
            .setExpiryTime("P6M");

        keyAsyncClient.updateKeyRotationPolicy("keyName", policyProperties)
            .subscribe(keyRotationPolicy ->
                System.out.printf("Updated key rotation policy with id: %s%n", keyRotationPolicy.getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.updateKeyRotationPolicy#String-KeyRotationPolicyProperties

        // BEGIN: com.azure.security.keyvault.keys.KeyAsyncClient.updateKeyRotationPolicyWithResponse#String-KeyRotationPolicyProperties
        List<KeyRotationLifetimeAction> myLifetimeActions = new ArrayList<>();
        KeyRotationLifetimeAction myRotateLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.ROTATE)
            .setTimeAfterCreate("P90D")
            .setTimeBeforeExpiry("P45D");
        KeyRotationLifetimeAction myNotifyLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.NOTIFY)
            .setTimeAfterCreate("P90D")
            .setTimeBeforeExpiry("P45D");

        myLifetimeActions.add(myRotateLifetimeAction);
        myLifetimeActions.add(myNotifyLifetimeAction);

        KeyRotationPolicyProperties myPolicyProperties = new KeyRotationPolicyProperties()
            .setLifetimeActions(myLifetimeActions)
            .setExpiryTime("P6M");

        keyAsyncClient.updateKeyRotationPolicyWithResponse("keyName", myPolicyProperties)
            .subscribe(updateKeyRotationPolicyResponse ->
                System.out.printf("Response received successfully with status code: %d. Updated key rotation policy"
                    + "with id: %s%n", updateKeyRotationPolicyResponse.getStatusCode(),
                    updateKeyRotationPolicyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.KeyAsyncClient.updateKeyRotationPolicyWithResponse#String-KeyRotationPolicyProperties
    }
}
