// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.credential.TokenCredential;
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
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import reactor.util.context.Context;

import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyAsyncClient}
 */
public final class KeyAsyncClientJavaDocCodeSnippets {

    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Generates code sample for creating a {@link KeyAsyncClient}
     * @return An instance of {@link KeyAsyncClient}
     */
    public KeyAsyncClient createAsyncClientWithHttpClient() {
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.withhttpclient.instantiation
        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
            .vaultUrl("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .httpClient(HttpClient.createDefault())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.async.keyclient.withhttpclient.instantiation
        return keyAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link KeyAsyncClient}
     * @return An instance of {@link KeyAsyncClient}
     */
    public KeyAsyncClient createAsyncClient() {
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.instantiation
        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
            .vaultUrl("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.async.keyclient.instantiation
        return keyAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link KeyAsyncClient}
     * @return An instance of {@link KeyAsyncClient}
     */
    public KeyAsyncClient createAsyncClientWithPipeline() {
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new KeyVaultCredentialPolicy(new DefaultAzureCredentialBuilder().build()), new RetryPolicy())
            .build();
        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
            .pipeline(pipeline)
            .vaultUrl("https://myvault.azure.net/")
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.async.keyclient.pipeline.instantiation
        return keyAsyncClient;
    }


    /**
     * Generates a code sample for using {@link KeyAsyncClient#createKey(String, KeyType)}
     */
    public void createKey() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.createKey#string-keyType
        keyAsyncClient.createKey("keyName", KeyType.EC)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getName(),
                    keyResponse.getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.createKey#string-keyType

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.createKey#keyCreateOptions
        CreateKeyOptions createKeyOptions = new CreateKeyOptions("keyName", KeyType.RSA)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        keyAsyncClient.createKey(createKeyOptions)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getName(),
                    keyResponse.getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.createKey#keyCreateOptions

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.createRsaKey#RsaKeyCreateOptions
        CreateRsaKeyOptions createRsaKeyOptions = new CreateRsaKeyOptions("keyName")
            .setKeySize(2048)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        keyAsyncClient.createRsaKey(createRsaKeyOptions)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getName(),
                    keyResponse.getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.createRsaKey#RsaKeyCreateOptions

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.createEcKey#EcKeyCreateOptions
        CreateEcKeyOptions createEcKeyOptions = new CreateEcKeyOptions("keyName")
            .setCurveName(KeyCurveName.P_384)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        keyAsyncClient.createEcKey(createEcKeyOptions)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getName(),
                    keyResponse.getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.createEcKey#EcKeyCreateOptions

        // BEGIN: com.azure.security.keyvault.keys.async.keyAsyncClient.createOctKey#CreateOctKeyOptions
        CreateOctKeyOptions createOctKeyOptions = new CreateOctKeyOptions("keyName")
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        keyAsyncClient.createOctKey(createOctKeyOptions)
            .contextWrite(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getName(),
                    keyResponse.getId()));
        // END: com.azure.security.keyvault.keys.async.keyAsyncClient.createOctKey#CreateOctKeyOptions
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#beginDeleteKey(String)}.
     */
    public void deleteKeySnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.deleteKey#String
        keyAsyncClient.beginDeleteKey("keyName")
            .subscribe(pollResponse -> {
                System.out.println("Delete Status: " + pollResponse.getStatus().toString());
                System.out.println("Delete Key Name: " + pollResponse.getValue().getName());
                System.out.println("Key Delete Date: " + pollResponse.getValue().getDeletedOn().toString());
            });
        // END: com.azure.security.keyvault.keys.async.keyclient.deleteKey#String
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#importKey(String, JsonWebKey)}
     */
    public void importKeySnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        JsonWebKey jsonWebKeyToImport = new JsonWebKey();
        // BEGIN: com.azure.security.keyvault.keys.keyasyncclient.importKey#string-jsonwebkey
        keyAsyncClient.importKey("keyName", jsonWebKeyToImport).subscribe(keyResponse ->
            System.out.printf("Key is imported with name %s and id %s \n", keyResponse.getName(), keyResponse.getId()));
        // END: com.azure.security.keyvault.keys.keyasyncclient.importKey#string-jsonwebkey

        // BEGIN: com.azure.security.keyvault.keys.keyasyncclient.importKey#options
        ImportKeyOptions options = new ImportKeyOptions("keyName", jsonWebKeyToImport)
            .setHardwareProtected(false);
        keyAsyncClient.importKey(options).subscribe(keyResponse ->
            System.out.printf("Key is imported with name %s and id %s \n", keyResponse.getName(), keyResponse.getId()));
        // END: com.azure.security.keyvault.keys.keyasyncclient.importKey#options

        // BEGIN: com.azure.security.keyvault.keys.keyasyncclient.importKeyWithResponse#options-response
        ImportKeyOptions importKeyOptions = new ImportKeyOptions("keyName", jsonWebKeyToImport)
            .setHardwareProtected(false);
        keyAsyncClient.importKeyWithResponse(importKeyOptions).subscribe(keyResponse ->
            System.out.printf("Key is imported with name %s and id %s \n", keyResponse.getValue().getName(),
                keyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.keyasyncclient.importKeyWithResponse#options-response
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#getDeletedKey(String)}
     */
    public void getDeletedKeySnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.getDeletedKey#string
        keyAsyncClient.getDeletedKey("keyName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Deleted Key's Recovery Id %s", keyResponse.getRecoveryId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.getDeletedKey#string
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#createKeyWithResponse(CreateKeyOptions)}
     */
    public void createKeyWithResponses() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.createKeyWithResponse#keyCreateOptions
        CreateKeyOptions createKeyOptions = new CreateKeyOptions("keyName", KeyType.RSA)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        keyAsyncClient.createKeyWithResponse(createKeyOptions)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getValue().getName(),
                    keyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.createKeyWithResponse#keyCreateOptions

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.createRsaKeyWithResponse#RsaKeyCreateOptions
        CreateRsaKeyOptions createRsaKeyOptions = new CreateRsaKeyOptions("keyName")
            .setKeySize(2048)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        keyAsyncClient.createRsaKeyWithResponse(createRsaKeyOptions)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getValue().getName(),
                    keyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.createRsaKeyWithResponse#RsaKeyCreateOptions

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.createEcKeyWithResponse#EcKeyCreateOptions
        CreateEcKeyOptions createEcKeyOptions = new CreateEcKeyOptions("keyName")
            .setCurveName(KeyCurveName.P_384)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        keyAsyncClient.createEcKeyWithResponse(createEcKeyOptions)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getValue().getName(),
                    keyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.createEcKeyWithResponse#EcKeyCreateOptions

        // BEGIN: com.azure.security.keyvault.keys.async.keyAsyncClient.createOctKeyWithResponse#CreateOctKeyOptions
        CreateOctKeyOptions createOctKeyOptions = new CreateOctKeyOptions("keyName")
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        keyAsyncClient.createOctKeyWithResponse(createOctKeyOptions)
            .contextWrite(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getValue().getName(),
                    keyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.async.keyAsyncClient.createOctKeyWithResponse#CreateOctKeyOptions
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#getKeyWithResponse(String, String)}
     */
    public void getKeyWithResponseSnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.getKeyWithResponse#string-string
        String keyVersion = "6A385B124DEF4096AF1361A85B16C204";
        keyAsyncClient.getKeyWithResponse("keyName", keyVersion)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n",
                    keyResponse.getValue().getName(), keyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.getKeyWithResponse#string-string
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#getKey(String, String)}
     */
    public void getKeySnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.getKey#string-string
        String keyVersion = "6A385B124DEF4096AF1361A85B16C204";
        keyAsyncClient.getKey("keyName", keyVersion)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getName(),
                    keyResponse.getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.getKey#string-string

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.getKey#string
        keyAsyncClient.getKey("keyName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getName(),
                    keyResponse.getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.getKey#string
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#updateKeyPropertiesWithResponse(KeyProperties, KeyOperation...)}
     */
    public void updateKeyWithResponseSnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.updateKeyPropertiesWithResponse#KeyProperties-keyOperations
        keyAsyncClient.getKey("keyName")
            .subscribe(keyResponse  -> {
                //Update the not before time of the key.
                keyResponse.getProperties().setNotBefore(OffsetDateTime.now().plusDays(50));
                keyAsyncClient.updateKeyPropertiesWithResponse(keyResponse.getProperties(), KeyOperation.ENCRYPT,
                    KeyOperation.DECRYPT)
                    .subscriberContext(Context.of(key1, value1, key2, value2))
                    .subscribe(updatedKeyResponse  ->
                        System.out.printf("Key's updated not before time %s %n",
                            updatedKeyResponse.getValue().getProperties().getNotBefore().toString()));
            });
        // END: com.azure.security.keyvault.keys.async.keyclient.updateKeyPropertiesWithResponse#KeyProperties-keyOperations
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#updateKeyProperties(KeyProperties, KeyOperation...)}
     */
    public void updateKeySnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.updateKeyProperties#KeyProperties-keyOperations
        keyAsyncClient.getKey("keyName")
             .subscribe(keyResponse  -> {
                 //Update the not before time of the key.
                 keyResponse.getProperties().setNotBefore(OffsetDateTime.now().plusDays(50));
                 keyAsyncClient.updateKeyProperties(keyResponse.getProperties(), KeyOperation.ENCRYPT,
                     KeyOperation.DECRYPT)
                     .subscriberContext(Context.of(key1, value1, key2, value2))
                     .subscribe(updatedKeyResponse  ->
                         System.out.printf("Key's updated not before time %s %n",
                             updatedKeyResponse.getProperties().getNotBefore().toString()));
             });
        // END: com.azure.security.keyvault.keys.async.keyclient.updateKeyProperties#KeyProperties-keyOperations

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.updateKeyProperties#KeyProperties
        keyAsyncClient.getKey("keyName")
            .subscribe(keyResponse  -> {
                //Update the not before time of the key.
                keyResponse.getProperties().setNotBefore(OffsetDateTime.now().plusDays(50));
                keyAsyncClient.updateKeyProperties(keyResponse.getProperties())
                    .subscriberContext(Context.of(key1, value1, key2, value2))
                    .subscribe(updatedKeyResponse  ->
                        System.out.printf("Key's updated not before time %s %n",
                            updatedKeyResponse.getProperties().getNotBefore().toString()));
            });
        // END: com.azure.security.keyvault.keys.async.keyclient.updateKeyProperties#KeyProperties
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#getDeletedKeyWithResponse(String)}
     */
    public void getDeleteKeyWithResponseSnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.getDeletedKeyWithResponse#string
        keyAsyncClient.getDeletedKeyWithResponse("keyName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedKeyResponse ->
                System.out.printf("Deleted Key's Recovery Id %s", deletedKeyResponse.getValue().getRecoveryId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.getDeletedKeyWithResponse#string
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#purgeDeletedKey(String)}
     */
    public void purgeDeletedKeySnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.purgeDeletedKey#string
        keyAsyncClient.purgeDeletedKey("deletedKeyName")
            .subscribe(purgeResponse ->
                System.out.println("Successfully Purged deleted Key"));
        // END: com.azure.security.keyvault.keys.async.keyclient.purgeDeletedKey#string
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#purgeDeletedKeyWithResponse(String)}
     */
    public void purgeDeletedKeyWithResponseSnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.purgeDeletedKeyWithResponse#string
        keyAsyncClient.purgeDeletedKeyWithResponse("deletedKeyName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(purgeResponse ->
                System.out.printf("Purge Status response %d %n", purgeResponse.getStatusCode()));
        // END: com.azure.security.keyvault.keys.async.keyclient.purgeDeletedKeyWithResponse#string
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#beginRecoverDeletedKey(String)}.
     */
    public void recoverDeletedKeySnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.recoverDeletedKey#String
        keyAsyncClient.beginRecoverDeletedKey("deletedKeyName")
            .subscribe(pollResponse -> {
                System.out.println("Recovery Status: " + pollResponse.getStatus().toString());
                System.out.println("Recover Key Name: " + pollResponse.getValue().getName());
                System.out.println("Recover Key Type: " + pollResponse.getValue().getKeyType());
            });
        // END: com.azure.security.keyvault.keys.async.keyclient.recoverDeletedKey#String
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#backupKey(String)}
     */
    public void backupKeySnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.backupKey#string
        keyAsyncClient.backupKey("keyName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyBackupResponse ->
                System.out.printf("Key's Backup Byte array's length %s %n", keyBackupResponse.length));
        // END: com.azure.security.keyvault.keys.async.keyclient.backupKey#string
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#backupKeyWithResponse(String)}
     */
    public void backupKeyWithResponseSnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.backupKeyWithResponse#string
        keyAsyncClient.backupKeyWithResponse("keyName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyBackupResponse ->
                System.out.printf("Key's Backup Byte array's length %s %n", keyBackupResponse.getValue().length));
        // END: com.azure.security.keyvault.keys.async.keyclient.backupKeyWithResponse#string
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#restoreKeyBackup}
     */
    public void restoreKeySnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.restoreKeyBackup#byte
        byte[] keyBackupByteArray = {};
        keyAsyncClient.restoreKeyBackup(keyBackupByteArray)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse  ->
                System.out.printf("Restored Key with name %s and id %s %n", keyResponse.getName(),
                    keyResponse.getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.restoreKeyBackup#byte
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#restoreKeyBackupWithResponse(byte[])}
     */
    public void restoreKeyWithResponseSnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.restoreKeyBackupWithResponse#byte
        byte[] keyBackupByteArray = {};
        keyAsyncClient.restoreKeyBackupWithResponse(keyBackupByteArray)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse  ->
                System.out.printf("Restored Key with name %s and id %s %n", keyResponse.getValue().getName(),
                    keyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.restoreKeyBackupWithResponse#byte
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#listPropertiesOfKeys}
     */
    public void listKeySnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.listKeys
        keyAsyncClient.listPropertiesOfKeys()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyProperties -> keyAsyncClient.getKey(keyProperties.getName(), keyProperties.getVersion())
                .subscribe(keyResponse -> System.out.printf("Received key with name %s and type %s",
                    keyResponse.getName(),
                     keyResponse.getKeyType())));
        // END: com.azure.security.keyvault.keys.async.keyclient.listKeys
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#listDeletedKeys}
     */
    public void listDeletedKeysSnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.listDeletedKeys
        keyAsyncClient.listDeletedKeys()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedKey  -> System.out.printf("Deleted key's recovery Id %s", deletedKey.getRecoveryId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.listDeletedKeys
    }

    /**
     * Generates code sample for using {@link KeyAsyncClient#listPropertiesOfKeyVersions(String)}
     */
    public void listKeyVersions() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.listKeyVersions
        keyAsyncClient.listPropertiesOfKeyVersions("keyName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyProperties -> keyAsyncClient.getKey(keyProperties.getName(), keyProperties.getVersion())
                .subscribe(keyResponse ->
                    System.out.printf("Received key's version with name %s, type %s and version %s",
                        keyResponse.getName(),
                        keyResponse.getKeyType(), keyResponse.getProperties().getVersion())));
        // END: com.azure.security.keyvault.keys.async.keyclient.listKeyVersions
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
     * Implementation not provided for this method
     * @return {@code null}
     */
    private TokenCredential getKeyVaultCredential() {
        return null;
    }
}
