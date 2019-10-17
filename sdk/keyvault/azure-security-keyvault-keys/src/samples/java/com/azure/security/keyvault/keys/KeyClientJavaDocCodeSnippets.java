// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.*;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.webkey.KeyCurveName;
import com.azure.security.keyvault.keys.models.webkey.KeyOperation;
import com.azure.security.keyvault.keys.models.webkey.KeyType;

import java.time.OffsetDateTime;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyClient}
 */
public final class KeyClientJavaDocCodeSnippets {

    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Generates code sample for creating a {@link KeyClient}
     * @return An instance of {@link KeyClient}
     */
    public KeyClient createClient() {
        // BEGIN: com.azure.security.keyvault.keys.keyclient.instantiation
        KeyClient keyClient = new KeyClientBuilder()
            .vaultEndpoint("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.security.keyvault.keys.keyclient.instantiation
        return keyClient;
    }

    /**
     * Generates a code sample for using {@link KeyClient#createKey(String, KeyType)}
     */
    public void createKey() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.createKey#string-keyType
        KeyVaultKey key = keyClient.createKey("keyName", KeyType.EC);
        System.out.printf("Key is created with name %s and id %s %n", key.getName(), key.getId());
        // END: com.azure.keyvault.keys.keyclient.createKey#string-keyType

        // BEGIN: com.azure.keyvault.keys.keyclient.createKey#keyOptions
        CreateKeyOptions createKeyOptions = new CreateKeyOptions("keyName", KeyType.RSA)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey optionsKey = keyClient.createKey(createKeyOptions);
        System.out.printf("Key is created with name %s and id %s %n", optionsKey.getName(), optionsKey.getId());
        // END: com.azure.keyvault.keys.keyclient.createKey#keyOptions

        // BEGIN: com.azure.keyvault.keys.keyclient.createRsaKey#keyOptions
        CreateRsaKeyOptions createRsaKeyOptions = new CreateRsaKeyOptions("keyName")
            .setKeySize(2048)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey rsaKey = keyClient.createRsaKey(createRsaKeyOptions);
        System.out.printf("Key is created with name %s and id %s %n", rsaKey.getName(), rsaKey.getId());
        // END: com.azure.keyvault.keys.keyclient.createRsaKey#keyOptions

        // BEGIN: com.azure.keyvault.keys.keyclient.createEcKey#keyOptions
        CreateEcKeyOptions createEcKeyOptions = new CreateEcKeyOptions("keyName")
            .setCurve(KeyCurveName.P_384)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey ecKey = keyClient.createEcKey(createEcKeyOptions);
        System.out.printf("Key is created with name %s and id %s %n", ecKey.getName(), ecKey.getId());
        // END: com.azure.keyvault.keys.keyclient.createEcKey#keyOptions


    }

    /**
     * Generates a code sample for using {@link KeyClient#deleteKey(String)}
     */
    public void deleteKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.deleteKey#string
        KeyVaultKey key = keyClient.getKey("keyName");
        DeletedKey deletedKey = keyClient.deleteKey("keyName");
        System.out.printf("Deleted Key's Recovery Id %s", deletedKey.getRecoveryId());
        // END: com.azure.keyvault.keys.keyclient.deleteKey#string
    }

    /**
     * Generates a code sample for using {@link KeyClient#getDeletedKey(String)}
     */
    public void getDeletedKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.getDeletedKey#string
        DeletedKey deletedKey = keyClient.getDeletedKey("keyName");
        System.out.printf("Deleted Key's Recovery Id %s", deletedKey.getRecoveryId());
        // END: com.azure.keyvault.keys.keyclient.getDeletedKey#string
    }

    /**
     * Generates a code sample for using {@link KeyClient#createKeyWithResponse(CreateKeyOptions, Context)}
     */
    public void createKeyWithResponses() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.createKeyWithResponse#keyCreateOptions-Context
        CreateKeyOptions createKeyOptions = new CreateKeyOptions("keyName", KeyType.RSA)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey optionsKey = keyClient.createKeyWithResponse(createKeyOptions, new Context(key1, value1)).getValue();
        System.out.printf("Key is created with name %s and id %s %n", optionsKey.getName(), optionsKey.getId());
        // END: com.azure.keyvault.keys.keyclient.createKeyWithResponse#keyCreateOptions-Context

        // BEGIN: com.azure.keyvault.keys.keyclient.createRsaKeyWithResponse#keyOptions-Context
        CreateRsaKeyOptions createRsaKeyOptions = new CreateRsaKeyOptions("keyName")
            .setKeySize(2048)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey rsaKey = keyClient.createRsaKeyWithResponse(createRsaKeyOptions, new Context(key1, value1)).getValue();
        System.out.printf("Key is created with name %s and id %s %n", rsaKey.getName(), rsaKey.getId());
        // END: com.azure.keyvault.keys.keyclient.createRsaKeyWithResponse#keyOptions-Context

        // BEGIN: com.azure.keyvault.keys.keyclient.createEcKeyWithResponse#keyOptions-Context
        CreateEcKeyOptions createEcKeyOptions = new CreateEcKeyOptions("keyName")
            .setCurve(KeyCurveName.P_384)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey ecKey = keyClient.createEcKeyWithResponse(createEcKeyOptions, new Context(key1, value1)).getValue();
        System.out.printf("Key is created with name %s and id %s %n", ecKey.getName(), ecKey.getId());
        // END: com.azure.keyvault.keys.keyclient.createEcKeyWithResponse#keyOptions-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#getKeyWithResponse(String, String, Context)}
     */
    public void getKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.getKeyWithResponse#string-string-Context
        String keyVersion = "6A385B124DEF4096AF1361A85B16C204";
        KeyVaultKey keyWithVersion = keyClient.getKeyWithResponse("keyName", keyVersion,
            new Context(key1, value1)).getValue();
        System.out.printf("Key is returned with name %s and id %s %n", keyWithVersion.getName(),
            keyWithVersion.getId());
        // END: com.azure.keyvault.keys.keyclient.getKeyWithResponse#string-string-Context

        // BEGIN: com.azure.keyvault.keys.keyclient.getKeyWithResponse#KeyProperties-Context
        for (KeyProperties key : keyClient.listPropertiesOfKeys()) {
            KeyVaultKey keyResponse = keyClient.getKeyWithResponse(key, new Context(key1, value1)).getValue();
            System.out.printf("Received key with name %s and type %s", keyResponse.getName(),
                keyResponse.getKey().getKeyType());
        }
        // END: com.azure.keyvault.keys.keyclient.getKeyWithResponse#KeyProperties-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#getKey(String, String)}
     */
    public void getKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.getKey#string-string
        String keyVersion = "6A385B124DEF4096AF1361A85B16C204";
        KeyVaultKey keyWithVersion = keyClient.getKey("keyName", keyVersion);
        System.out.printf("Key is returned with name %s and id %s %n", keyWithVersion.getName(),
            keyWithVersion.getId());
        // END: com.azure.keyvault.keys.keyclient.getKey#string-string

        // BEGIN: com.azure.keyvault.keys.keyclient.getKey#string
        KeyVaultKey keyWithVersionValue = keyClient.getKey("keyName");
        System.out.printf("Key is returned with name %s and id %s %n", keyWithVersionValue.getName(),
            keyWithVersionValue.getId());
        // END: com.azure.keyvault.keys.keyclient.getKey#string

        // BEGIN: com.azure.keyvault.keys.keyclient.getKey#KeyProperties
        for (KeyProperties key : keyClient.listPropertiesOfKeys()) {
            KeyVaultKey keyResponse = keyClient.getKey(key);
            System.out.printf("Received key with name %s and type %s", keyResponse.getName(),
                keyResponse.getKey().getKeyType());
        }
        // END: com.azure.keyvault.keys.keyclient.getKey#KeyProperties
    }

    /**
     * Generates a code sample for using {@link KeyClient#updateKeyPropertiesWithResponse(KeyProperties,  Context, KeyOperation...)}
     */
    public void updateKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.keyvault.keys.keyclient.updateKeyPropertiesWithResponse#KeyProperties-keyOperations-Context
        KeyVaultKey key = keyClient.getKey("keyName");
        key.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(60));
        KeyVaultKey updatedKey = keyClient.updateKeyPropertiesWithResponse(key.getProperties(),
            new Context(key1, value1), KeyOperation.ENCRYPT, KeyOperation.DECRYPT).getValue();
        System.out.printf("Key is updated with name %s and id %s %n", updatedKey.getName(), updatedKey.getId());
        // END: com.azure.keyvault.keys.keyclient.updateKeyPropertiesWithResponse#KeyProperties-keyOperations-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#updateKeyProperties(KeyProperties, KeyOperation...)}
     */
    public void updateKeySnippets() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.keyvault.keys.keyclient.updateKeyProperties#KeyProperties-keyOperations
        KeyVaultKey key = keyClient.getKey("keyName");
        key.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(60));
        KeyVaultKey updatedKey = keyClient.updateKeyProperties(key.getProperties(), KeyOperation.ENCRYPT, KeyOperation.DECRYPT);
        System.out.printf("Key is updated with name %s and id %s %n", updatedKey.getName(), updatedKey.getId());
        // END: com.azure.keyvault.keys.keyclient.updateKeyProperties#KeyProperties-keyOperations
    }

    /**
     * Generates a code sample for using {@link KeyClient#deleteKeyWithResponse(String, Context)}
     */
    public void deleteKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.deleteKeyWithResponse#string-Context
        KeyVaultKey key = keyClient.getKey("keyName");
        DeletedKey deletedKey = keyClient.deleteKeyWithResponse("keyName", new Context(key1, value1))
            .getValue();
        System.out.printf("Deleted Key's Recovery Id %s", deletedKey.getRecoveryId());
        // END: com.azure.keyvault.keys.keyclient.deleteKeyWithResponse#string-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#getDeletedKeyWithResponse(String, Context)}
     */
    public void getDeleteKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.getDeletedKeyWithResponse#string-Context
        DeletedKey deletedKey = keyClient.getDeletedKeyWithResponse("keyName", new Context(key1, value1))
            .getValue();
        System.out.printf("Deleted Key with recovery Id %s %n", deletedKey.getRecoveryId());
        // END: com.azure.keyvault.keys.keyclient.getDeletedKeyWithResponse#string-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#purgeDeletedKey(String)}
     */
    public void purgeDeletedKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.purgeDeletedKey#string
        keyClient.purgeDeletedKey("deletedKeyName");
        // END: com.azure.keyvault.keys.keyclient.purgeDeletedKey#string
    }

    /**
     * Generates a code sample for using {@link KeyClient#purgeDeletedKeyWithResponse(String, Context)}
     */
    public void purgeDeletedKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.keyvault.keys.keyclient.purgeDeletedKeyWithResponse#string-Context
        Response<Void> purgedResponse = keyClient.purgeDeletedKeyWithResponse("deletedKeyName",
            new Context(key2, value2));
        System.out.printf("Purge Status Code: %d %n", purgedResponse.getStatusCode());
        // END: com.azure.keyvault.keys.keyclient.purgeDeletedKeyWithResponse#string-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#recoverDeletedKeyWithResponse(String, Context)}
     */
    public void recoverDeletedKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.recoverDeletedKeyWithResponse#string-Context
        KeyVaultKey recoveredKey =  keyClient.recoverDeletedKeyWithResponse("deletedKeyName",
            new Context(key2, value2)).getValue();
        System.out.printf("Recovered key with name %s", recoveredKey.getName());
        // END: com.azure.keyvault.keys.keyclient.recoverDeletedKeyWithResponse#string-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#recoverDeletedKey(String)}
     */
    public void recoverDeletedKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.recoverDeletedKey#string
        KeyVaultKey recoveredKey =  keyClient.recoverDeletedKey("deletedKeyName");
        System.out.printf("Recovered key with name %s", recoveredKey.getName());
        // END: com.azure.keyvault.keys.keyclient.recoverDeletedKey#string
    }

    /**
     * Generates a code sample for using {@link KeyClient#backupKey(String)}
     */
    public void backupKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.backupKey#string
        byte[] keyBackup = keyClient.backupKey("keyName");
        System.out.printf("Key's Backup Byte array's length %s", keyBackup.length);
        // END: com.azure.keyvault.keys.keyclient.backupKey#string
    }

    /**
     * Generates a code sample for using {@link KeyClient#backupKeyWithResponse(String, Context)}
     */
    public void backupKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.backupKeyWithResponse#string-Context
        byte[] keyBackup = keyClient.backupKeyWithResponse("keyName", new Context(key2, value2)).getValue();
        System.out.printf("Key's Backup Byte array's length %s", keyBackup.length);
        // END: com.azure.keyvault.keys.keyclient.backupKeyWithResponse#string-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#restoreKeyBackup}
     */
    public void restoreKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.restoreKey#byte
        byte[] keyBackupByteArray = {};
        KeyVaultKey keyResponse = keyClient.restoreKeyBackup(keyBackupByteArray);
        System.out.printf("Restored Key with name %s and id %s %n", keyResponse.getName(), keyResponse.getId());
        // END: com.azure.keyvault.keys.keyclient.restoreKey#byte
    }

    /**
     * Generates a code sample for using {@link KeyClient#restoreKeyBackupWithResponse(byte[], Context)}
     */
    public void restoreKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.restoreKeyWithResponse#byte-Context
        byte[] keyBackupByteArray = {};
        Response<KeyVaultKey> keyResponse = keyClient.restoreKeyBackupWithResponse(keyBackupByteArray, new Context(key1, value1));
        System.out.printf("Restored Key with name %s and id %s %n",
            keyResponse.getValue().getName(), keyResponse.getValue().getId());
        // END: com.azure.keyvault.keys.keyclient.restoreKeyWithResponse#byte-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#listPropertiesOfKeys}
     */
    public void listKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.listKeys
        for (KeyProperties key : keyClient.listPropertiesOfKeys()) {
            KeyVaultKey keyWithMaterial = keyClient.getKey(key);
            System.out.printf("Received key with name %s and type %s", keyWithMaterial.getName(),
                keyWithMaterial.getKey().getKeyType());
        }
        // END: com.azure.keyvault.keys.keyclient.listKeys

        // BEGIN: com.azure.keyvault.keys.keyclient.listKeys#Context
        for (KeyProperties key : keyClient.listPropertiesOfKeys(new Context(key2, value2))) {
            KeyVaultKey keyWithMaterial = keyClient.getKey(key);
            System.out.printf("Received key with name %s and type %s", keyWithMaterial.getName(),
                keyWithMaterial.getKey().getKeyType());
        }
        // END: com.azure.keyvault.keys.keyclient.listKeys#Context

        // BEGIN: com.azure.keyvault.keys.keyclient.listKeys.iterableByPage
        keyClient.listPropertiesOfKeys().iterableByPage().forEach(resp -> {
            System.out.printf("Got response headers . Url: %s, Status code: %d %n",
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                KeyVaultKey keyWithMaterial = keyClient.getKey(value);
                System.out.printf("Received key with name %s and type %s %n", keyWithMaterial.getName(),
                    keyWithMaterial.getKey().getKeyType());
            });
        });
        // END: com.azure.keyvault.keys.keyclient.listKeys.iterableByPage
    }

    /**
     * Generates a code sample for using {@link KeyClient#listDeletedKeys}
     */
    public void listDeletedKeysSnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.listDeletedKeys
        for (DeletedKey deletedKey : keyClient.listDeletedKeys()) {
            System.out.printf("Deleted key's recovery Id %s", deletedKey.getRecoveryId());
        }
        // END: com.azure.keyvault.keys.keyclient.listDeletedKeys

        // BEGIN: com.azure.keyvault.keys.keyclient.listDeletedKeys#Context
        for (DeletedKey deletedKey : keyClient.listDeletedKeys(new Context(key2, value2))) {
            System.out.printf("Deleted key's recovery Id %s", deletedKey.getRecoveryId());
        }
        // END: com.azure.keyvault.keys.keyclient.listDeletedKeys#Context

        // BEGIN: com.azure.keyvault.keys.keyclient.listDeletedKeys.iterableByPage
        keyClient.listDeletedKeys().iterableByPage().forEach(resp -> {
            System.out.printf("Got response headers . Url: %s, Status code: %d %n",
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                System.out.printf("Deleted key's recovery Id %s %n", value.getRecoveryId());
            });
        });
        // END: com.azure.keyvault.keys.keyclient.listDeletedKeys.iterableByPage
    }

    /**
     * Generates code sample for using {@link KeyClient#listPropertiesOfKeyVersions(String)}
     */
    public void listKeyVersions() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.listKeyVersions
        for (KeyProperties key : keyClient.listPropertiesOfKeyVersions("keyName")) {
            KeyVaultKey keyWithMaterial  = keyClient.getKey(key);
            System.out.printf("Received key's version with name %s, type %s and version %s",
                keyWithMaterial.getName(),
                    keyWithMaterial.getKey().getKeyType(), keyWithMaterial.getProperties().getVersion());
        }
        // END: com.azure.keyvault.keys.keyclient.listKeyVersions

        // BEGIN: com.azure.keyvault.keys.keyclient.listKeyVersions#Context
        for (KeyProperties key : keyClient.listPropertiesOfKeyVersions("keyName", new Context(key2, value2))) {
            KeyVaultKey keyWithMaterial  = keyClient.getKey(key);
            System.out.printf("Received key's version with name %s, type %s and version %s",
                keyWithMaterial.getName(),
                    keyWithMaterial.getKey().getKeyType(), keyWithMaterial.getProperties().getVersion());
        }
        // END: com.azure.keyvault.keys.keyclient.listKeyVersions#Context

        // BEGIN: com.azure.keyvault.keys.keyclient.listKeyVersions.iterableByPage
        keyClient.listPropertiesOfKeyVersions("keyName").iterableByPage().forEach(resp -> {
            System.out.printf("Got response headers . Url: %s, Status code: %d %n",
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                System.out.printf("Key name: %s, Key version: %s %n", value.getName(), value.getVersion());
            });
        });
        // END: com.azure.keyvault.keys.keyclient.listKeyVersions.iterableByPage
    }

    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private TokenCredential getKeyVaultCredential() {
        return null;
    }
}
