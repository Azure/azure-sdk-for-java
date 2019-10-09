// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.models.RecordedData;
import com.azure.core.test.policy.RecordNetworkCallPolicy;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.EcKeyCreateOptions;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyCreateOptions;
import com.azure.security.keyvault.keys.models.RsaKeyCreateOptions;
import com.azure.security.keyvault.keys.models.webkey.KeyCurveName;
import com.azure.security.keyvault.keys.models.webkey.KeyOperation;
import com.azure.security.keyvault.keys.models.webkey.KeyType;
import reactor.util.context.Context;

import java.time.OffsetDateTime;

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
        RecordedData networkData = new RecordedData();
        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
            .endpoint("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .addPolicy(new RecordNetworkCallPolicy(networkData))
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
            .endpoint("https://myvault.azure.net/")
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
        RecordedData networkData = new RecordedData();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RecordNetworkCallPolicy(networkData)).build();
        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
            .pipeline(pipeline)
            .endpoint("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
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
        KeyCreateOptions keyCreateOptions = new KeyCreateOptions("keyName", KeyType.RSA)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpires(OffsetDateTime.now().plusYears(1));
        keyAsyncClient.createKey(keyCreateOptions)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getName(),
                    keyResponse.getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.createKey#keyCreateOptions

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.createRsaKey#RsaKeyCreateOptions
        RsaKeyCreateOptions rsaKeyCreateOptions = new RsaKeyCreateOptions("keyName")
            .setKeySize(2048)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpires(OffsetDateTime.now().plusYears(1));
        keyAsyncClient.createRsaKey(rsaKeyCreateOptions)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getName(),
                    keyResponse.getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.createRsaKey#RsaKeyCreateOptions

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.createEcKey#EcKeyCreateOptions
        EcKeyCreateOptions ecKeyCreateOptions = new EcKeyCreateOptions("keyName")
            .setCurve(KeyCurveName.P_384)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpires(OffsetDateTime.now().plusYears(1));
        keyAsyncClient.createEcKey(ecKeyCreateOptions)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getName(),
                    keyResponse.getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.createEcKey#EcKeyCreateOptions
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#deleteKey(String)}
     */
    public void deleteKeySnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.deleteKey#string
        keyAsyncClient.deleteKey("keyName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Deleted Key's Recovery Id %s", keyResponse.getRecoveryId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.deleteKey#string
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
     * Generates a code sample for using {@link KeyAsyncClient#createKeyWithResponse(KeyCreateOptions)}
     */
    public void createKeyWithResponses() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.createKeyWithResponse#keyCreateOptions
        KeyCreateOptions keyCreateOptions = new KeyCreateOptions("keyName", KeyType.RSA)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpires(OffsetDateTime.now().plusYears(1));
        keyAsyncClient.createKeyWithResponse(keyCreateOptions)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getValue().getName(),
                    keyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.createKeyWithResponse#keyCreateOptions

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.createRsaKeyWithResponse#RsaKeyCreateOptions
        RsaKeyCreateOptions rsaKeyCreateOptions = new RsaKeyCreateOptions("keyName")
            .setKeySize(2048)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpires(OffsetDateTime.now().plusYears(1));
        keyAsyncClient.createRsaKeyWithResponse(rsaKeyCreateOptions)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getValue().getName(),
                    keyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.createRsaKeyWithResponse#RsaKeyCreateOptions

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.createEcKeyWithResponse#EcKeyCreateOptions
        EcKeyCreateOptions ecKeyCreateOptions = new EcKeyCreateOptions("keyName")
            .setCurve(KeyCurveName.P_384)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpires(OffsetDateTime.now().plusYears(1));
        keyAsyncClient.createEcKeyWithResponse(ecKeyCreateOptions)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and id %s %n", keyResponse.getValue().getName(),
                    keyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.createEcKeyWithResponse#EcKeyCreateOptions
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

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.getKeyWithResponse#KeyProperties
        keyAsyncClient.listKeys().subscribe(keyProperties ->
            keyAsyncClient.getKeyWithResponse(keyProperties)
                .subscriberContext(Context.of(key1, value1, key2, value2))
                .subscribe(keyResponse ->
                System.out.printf("Key with name %s and value %s %n", keyResponse.getValue().getName(),
                    keyResponse.getValue().getId())));
        // END: com.azure.security.keyvault.keys.async.keyclient.getKeyWithResponse#KeyProperties
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

        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.getKey#KeyProperties
        keyAsyncClient.listKeys().subscribe(keyProperties ->
            keyAsyncClient.getKey(keyProperties)
                .subscriberContext(Context.of(key1, value1, key2, value2))
                .subscribe(keyResponse ->
                    System.out.printf("Key with name %s and value %s %n", keyResponse.getName(), keyResponse.getId())));
        // END: com.azure.security.keyvault.keys.async.keyclient.getKey#KeyProperties
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
     * Generates a code sample for using {@link KeyAsyncClient#deleteKeyWithResponse(String)}
     */
    public void deleteKeyWithResponseSnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.deleteKeyWithResponse#string
        keyAsyncClient.deleteKeyWithResponse("keyName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedKeyResponse ->
                System.out.printf("Deleted Key's Recovery Id %s", deletedKeyResponse.getValue().getRecoveryId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.deleteKeyWithResponse#string
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
     * Generates a code sample for using {@link KeyAsyncClient#recoverDeletedKeyWithResponse(String)}
     */
    public void recoverDeletedKeyWithResponseSnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.recoverDeletedKeyWithResponse#string
        keyAsyncClient.recoverDeletedKeyWithResponse("deletedKeyName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(recoveredKeyResponse ->
                System.out.printf("Recovered Key with name %s %n", recoveredKeyResponse.getValue().getName()));
        // END: com.azure.security.keyvault.keys.async.keyclient.recoverDeletedKeyWithResponse#string
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#recoverDeletedKey(String)}
     */
    public void recoverDeletedKeySnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.recoverDeletedKey#string
        keyAsyncClient.recoverDeletedKey("deletedKeyName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(recoveredKeyResponse ->
                System.out.printf("Recovered Key with name %s %n", recoveredKeyResponse.getName()));
        // END: com.azure.security.keyvault.keys.async.keyclient.recoverDeletedKey#string
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
     * Generates a code sample for using {@link KeyAsyncClient#restoreKey}
     */
    public void restoreKeySnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.restoreKey#byte
        byte[] keyBackupByteArray = {};
        keyAsyncClient.restoreKey(keyBackupByteArray)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse  ->
                System.out.printf("Restored Key with name %s and id %s %n", keyResponse.getName(),
                    keyResponse.getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.restoreKey#byte
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#restoreKeyWithResponse(byte[])}
     */
    public void restoreKeyWithResponseSnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.restoreKeyWithResponse#byte
        byte[] keyBackupByteArray = {};
        keyAsyncClient.restoreKeyWithResponse(keyBackupByteArray)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse  ->
                System.out.printf("Restored Key with name %s and id %s %n", keyResponse.getValue().getName(),
                    keyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.async.keyclient.restoreKeyWithResponse#byte
    }

    /**
     * Generates a code sample for using {@link KeyAsyncClient#listKeys}
     */
    public void listKeySnippets() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.listKeys
        keyAsyncClient.listKeys()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyProperties -> keyAsyncClient.getKey(keyProperties)
                .subscribe(keyResponse -> System.out.printf("Received key with name %s and type %s",
                    keyResponse.getName(),
                     keyResponse.getKeyMaterial().getKty())));
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
     * Generates code sample for using {@link KeyAsyncClient#listKeyVersions(String)}
     */
    public void listKeyVersions() {
        KeyAsyncClient keyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.async.keyclient.listKeyVersions
        keyAsyncClient.listKeyVersions("keyName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(keyProperties -> keyAsyncClient.getKey(keyProperties)
                .subscribe(keyResponse ->
                    System.out.printf("Received key's version with name %s, type %s and version %s",
                        keyResponse.getName(),
                        keyResponse.getKeyMaterial().getKty(), keyResponse.getProperties().getVersion())));
        // END: com.azure.security.keyvault.keys.async.keyclient.listKeyVersions
    }

    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private TokenCredential getKeyVaultCredential() {
        return null;
    }
}
