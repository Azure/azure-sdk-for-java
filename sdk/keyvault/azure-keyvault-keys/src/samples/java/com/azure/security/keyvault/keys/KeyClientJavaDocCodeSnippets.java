// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.EcKeyCreateOptions;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.KeyBase;
import com.azure.security.keyvault.keys.models.KeyCreateOptions;
import com.azure.security.keyvault.keys.models.RsaKeyCreateOptions;
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
            .endpoint("https://myvault.azure.net/")
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
        Key key = keyClient.createKey("keyName", KeyType.EC);
        System.out.printf("Key is created with name %s and id %s %n", key.name(), key.id());
        // END: com.azure.keyvault.keys.keyclient.createKey#string-keyType

        // BEGIN: com.azure.keyvault.keys.keyclient.createKey#keyOptions
        KeyCreateOptions keyCreateOptions = new KeyCreateOptions("keyName", KeyType.RSA)
            .notBefore(OffsetDateTime.now().plusDays(1))
            .expires(OffsetDateTime.now().plusYears(1));
        Key optionsKey = keyClient.createKey(keyCreateOptions);
        System.out.printf("Key is created with name %s and id %s \n", optionsKey.name(), optionsKey.id());
        // END: com.azure.keyvault.keys.keyclient.createKey#keyOptions

        // BEGIN: com.azure.keyvault.keys.keyclient.createRsaKey#keyOptions
        RsaKeyCreateOptions rsaKeyCreateOptions = new RsaKeyCreateOptions("keyName")
            .keySize(2048)
            .notBefore(OffsetDateTime.now().plusDays(1))
            .expires(OffsetDateTime.now().plusYears(1));
        Key rsaKey = keyClient.createRsaKey(rsaKeyCreateOptions);
        System.out.printf("Key is created with name %s and id %s \n", rsaKey.name(), rsaKey.id());
        // END: com.azure.keyvault.keys.keyclient.createRsaKey#keyOptions

        // BEGIN: com.azure.keyvault.keys.keyclient.createEcKey#keyOptions
        EcKeyCreateOptions ecKeyCreateOptions = new EcKeyCreateOptions("keyName")
            .curve(KeyCurveName.P_384)
            .notBefore(OffsetDateTime.now().plusDays(1))
            .expires(OffsetDateTime.now().plusYears(1));
        Key ecKey = keyClient.createEcKey(ecKeyCreateOptions);
        System.out.printf("Key is created with name %s and id %s \n", ecKey.name(), ecKey.id());
        // END: com.azure.keyvault.keys.keyclient.createEcKey#keyOptions


    }

    /**
     * Generates a code sample for using {@link KeyClient#deleteKey(String)}
     */
    public void deleteKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.deleteKey#string
        Key key = keyClient.getKey("keyName");
        DeletedKey deletedKey = keyClient.deleteKey("keyName");
        System.out.printf("Deleted Key's Recovery Id %s", deletedKey.recoveryId());
        // END: com.azure.keyvault.keys.keyclient.deleteKey#string
    }

    /**
     * Generates a code sample for using {@link KeyClient#getDeletedKey(String)}
     */
    public void getDeletedKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.getDeletedKey#string
        DeletedKey deletedKey = keyClient.getDeletedKey("keyName");
        System.out.printf("Deleted Key's Recovery Id %s", deletedKey.recoveryId());
        // END: com.azure.keyvault.keys.keyclient.getDeletedKey#string
    }

    /**
     * Generates a code sample for using {@link KeyClient#createKeyWithResponse(KeyCreateOptions, Context)}
     */
    public void createKeyWithResponses() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.createKeyWithResponse#keyCreateOptions-Context
        KeyCreateOptions keyCreateOptions = new KeyCreateOptions("keyName", KeyType.RSA)
            .notBefore(OffsetDateTime.now().plusDays(1))
            .expires(OffsetDateTime.now().plusYears(1));
        Key optionsKey = keyClient.createKeyWithResponse(keyCreateOptions, new Context(key1, value1)).value();
        System.out.printf("Key is created with name %s and id %s \n", optionsKey.name(), optionsKey.id());
        // END: com.azure.keyvault.keys.keyclient.createKeyWithResponse#keyCreateOptions-Context

        // BEGIN: com.azure.keyvault.keys.keyclient.createRsaKeyWithResponse#keyOptions-Context
        RsaKeyCreateOptions rsaKeyCreateOptions = new RsaKeyCreateOptions("keyName")
            .keySize(2048)
            .notBefore(OffsetDateTime.now().plusDays(1))
            .expires(OffsetDateTime.now().plusYears(1));
        Key rsaKey = keyClient.createRsaKeyWithResponse(rsaKeyCreateOptions, new Context(key1, value1)).value();
        System.out.printf("Key is created with name %s and id %s \n", rsaKey.name(), rsaKey.id());
        // END: com.azure.keyvault.keys.keyclient.createRsaKeyWithResponse#keyOptions-Context

        // BEGIN: com.azure.keyvault.keys.keyclient.createEcKeyWithResponse#keyOptions-Context
        EcKeyCreateOptions ecKeyCreateOptions = new EcKeyCreateOptions("keyName")
            .curve(KeyCurveName.P_384)
            .notBefore(OffsetDateTime.now().plusDays(1))
            .expires(OffsetDateTime.now().plusYears(1));
        Key ecKey = keyClient.createEcKeyWithResponse(ecKeyCreateOptions, new Context(key1, value1)).value();
        System.out.printf("Key is created with name %s and id %s \n", ecKey.name(), ecKey.id());
        // END: com.azure.keyvault.keys.keyclient.createEcKeyWithResponse#keyOptions-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#getKeyWithResponse(String, String, Context)}
     */
    public void getKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.getKeyWithResponse#string-string-Context
        String keyVersion = "6A385B124DEF4096AF1361A85B16C204";
        Key keyWithVersion = keyClient.getKeyWithResponse("keyName", keyVersion,
            new Context(key1, value1)).value();
        System.out.printf("Key is returned with name %s and id %s \n", keyWithVersion.name(), keyWithVersion.id());
        // END: com.azure.keyvault.keys.keyclient.getKeyWithResponse#string-string-Context

        // BEGIN: com.azure.keyvault.keys.keyclient.getKeyWithResponse#KeyBase-Context
        for (KeyBase key : keyClient.listKeys()) {
            Key keyResponse = keyClient.getKeyWithResponse(key, new Context(key1, value1)).value();
            System.out.printf("Received key with name %s and type %s", keyResponse.name(),
                keyResponse.keyMaterial().kty());
        }
        // END: com.azure.keyvault.keys.keyclient.getKeyWithResponse#KeyBase-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#getKey(String, String)}
     */
    public void getKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.getKey#string-string
        String keyVersion = "6A385B124DEF4096AF1361A85B16C204";
        Key keyWithVersion = keyClient.getKey("keyName", keyVersion);
        System.out.printf("Key is returned with name %s and id %s \n", keyWithVersion.name(), keyWithVersion.id());
        // END: com.azure.keyvault.keys.keyclient.getKey#string-string

        // BEGIN: com.azure.keyvault.keys.keyclient.getKey#string
        Key keyWithVersionValue = keyClient.getKey("keyName");
        System.out.printf("Key is returned with name %s and id %s \n", keyWithVersionValue.name(), keyWithVersionValue.id());
        // END: com.azure.keyvault.keys.keyclient.getKey#string

        // BEGIN: com.azure.keyvault.keys.keyclient.getKey#KeyBase
        for (KeyBase key : keyClient.listKeys()) {
            Key keyResponse = keyClient.getKey(key);
            System.out.printf("Received key with name %s and type %s", keyResponse.name(),
                keyResponse.keyMaterial().kty());
        }
        // END: com.azure.keyvault.keys.keyclient.getKey#KeyBase
    }

    /**
     * Generates a code sample for using {@link KeyClient#updateKeyWithResponse(KeyBase,  Context, KeyOperation...)}
     */
    public void updateKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.keyvault.keys.keyclient.updateKeyWithResponse#KeyBase-keyOperations-Context
        Key key = keyClient.getKey("keyName");
        key.expires(OffsetDateTime.now().plusDays(60));
        KeyBase updatedKeyBase = keyClient.updateKeyWithResponse(key,
            new Context(key1, value1), KeyOperation.ENCRYPT, KeyOperation.DECRYPT).value();
        Key updatedKey = keyClient.getKey(updatedKeyBase.name());
        System.out.printf("Key is updated with name %s and id %s \n", updatedKey.name(), updatedKey.id());
        // END: com.azure.keyvault.keys.keyclient.updateKeyWithResponse#KeyBase-keyOperations-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#updateKey(KeyBase, KeyOperation...)}
     */
    public void updateKeySnippets() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.keyvault.keys.keyclient.updateKey#KeyBase-keyOperations
        Key key = keyClient.getKey("keyName");
        key.expires(OffsetDateTime.now().plusDays(60));
        KeyBase updatedKeyBase = keyClient.updateKey(key, KeyOperation.ENCRYPT, KeyOperation.DECRYPT);
        Key updatedKey = keyClient.getKey(updatedKeyBase.name());
        System.out.printf("Key is updated with name %s and id %s \n", updatedKey.name(), updatedKey.id());
        // END: com.azure.keyvault.keys.keyclient.updateKey#KeyBase-keyOperations

        // BEGIN: com.azure.keyvault.keys.keyclient.updateKey#KeyBase
        Key updateKey = keyClient.getKey("keyName");
        key.expires(OffsetDateTime.now().plusDays(60));
        KeyBase updatedKeyBaseValue = keyClient.updateKey(updateKey);
        Key updatedKeyValue = keyClient.getKey(updatedKeyBaseValue.name());
        System.out.printf("Key is updated with name %s and id %s \n", updatedKeyValue.name(), updatedKeyValue.id());
        // END: com.azure.keyvault.keys.keyclient.updateKey#KeyBase
    }

    /**
     * Generates a code sample for using {@link KeyClient#deleteKeyWithResponse(String, Context)}
     */
    public void deleteKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.deleteKeyWithResponse#string-Context
        Key key = keyClient.getKey("keyName");
        DeletedKey deletedKey = keyClient.deleteKeyWithResponse("keyName", new Context(key1, value1)).value();
        System.out.printf("Deleted Key's Recovery Id %s", deletedKey.recoveryId());
        // END: com.azure.keyvault.keys.keyclient.deleteKeyWithResponse#string-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#getDeletedKeyWithResponse(String, Context)}
     */
    public void getDeleteKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.getDeletedKeyWithResponse#string-Context
        DeletedKey deletedKey = keyClient.getDeletedKeyWithResponse("keyName", new Context(key1, value1)).value();
        System.out.printf("Deleted Key with recovery Id %s \n", deletedKey.recoveryId());
        // END: com.azure.keyvault.keys.keyclient.getDeletedKeyWithResponse#string-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#purgeDeletedKey(String)}
     */
    public void purgeDeletedKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.purgeDeletedKey#string
        VoidResponse purgeResponse = keyClient.purgeDeletedKey("deletedKeyName");
        System.out.printf("Purge Status Code: %rsaPrivateExponent", purgeResponse.statusCode());
        // END: com.azure.keyvault.keys.keyclient.purgeDeletedKey#string

        // BEGIN: com.azure.keyvault.keys.keyclient.purgeDeletedKey#string-Context
        VoidResponse purgedResponse = keyClient.purgeDeletedKey("deletedKeyName", new Context(key2, value2));
        System.out.printf("Purge Status Code: %rsaPrivateExponent", purgedResponse.statusCode());
        // END: com.azure.keyvault.keys.keyclient.purgeDeletedKey#string-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#recoverDeletedKeyWithResponse(String, Context)}
     */
    public void recoverDeletedKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.recoverDeletedKeyWithResponse#string-Context
        Key recoveredKey =  keyClient.recoverDeletedKeyWithResponse("deletedKeyName",
            new Context(key2, value2)).value();
        System.out.printf("Recovered key with name %s", recoveredKey.name());
        // END: com.azure.keyvault.keys.keyclient.recoverDeletedKeyWithResponse#string-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#recoverDeletedKey(String)}
     */
    public void recoverDeletedKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.recoverDeletedKey#string
        Key recoveredKey =  keyClient.recoverDeletedKey("deletedKeyName");
        System.out.printf("Recovered key with name %s", recoveredKey.name());
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
        byte[] keyBackup = keyClient.backupKeyWithResponse("keyName", new Context(key2, value2)).value();
        System.out.printf("Key's Backup Byte array's length %s", keyBackup.length);
        // END: com.azure.keyvault.keys.keyclient.backupKeyWithResponse#string-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#restoreKey}
     */
    public void restoreKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.restoreKey#byte
        byte[] keyBackupByteArray = {};
        Key keyResponse = keyClient.restoreKey(keyBackupByteArray);
        System.out.printf("Restored Key with name %s and id %s \n", keyResponse.name(), keyResponse.id());
        // END: com.azure.keyvault.keys.keyclient.restoreKey#byte
    }

    /**
     * Generates a code sample for using {@link KeyClient#restoreKeyWithResponse(byte[], Context)}
     */
    public void restoreKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.restoreKeyWithResponse#byte-Context
        byte[] keyBackupByteArray = {};
        Response<Key> keyResponse = keyClient.restoreKeyWithResponse(keyBackupByteArray, new Context(key1, value1));
        System.out.printf("Restored Key with name %s and id %s \n",
            keyResponse.value().name(), keyResponse.value().id());
        // END: com.azure.keyvault.keys.keyclient.restoreKeyWithResponse#byte-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#listKeys}
     */
    public void listKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.listKeys
        for (KeyBase key : keyClient.listKeys()) {
            Key keyWithMaterial = keyClient.getKey(key);
            System.out.printf("Received key with name %s and type %s", keyWithMaterial.name(),
                keyWithMaterial.keyMaterial().kty());
        }
        // END: com.azure.keyvault.keys.keyclient.listKeys

        // BEGIN: com.azure.keyvault.keys.keyclient.listKeys#Context
        for (KeyBase key : keyClient.listKeys(new Context(key2, value2))) {
            Key keyWithMaterial = keyClient.getKey(key);
            System.out.printf("Received key with name %s and type %s", keyWithMaterial.name(),
                keyWithMaterial.keyMaterial().kty());
        }
        // END: com.azure.keyvault.keys.keyclient.listKeys#Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#listDeletedKeys}
     */
    public void listDeletedKeysSnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.listDeletedKeys
        for (DeletedKey deletedKey : keyClient.listDeletedKeys()) {
            System.out.printf("Deleted key's recovery Id %s", deletedKey.recoveryId());
        }
        // END: com.azure.keyvault.keys.keyclient.listDeletedKeys

        // BEGIN: com.azure.keyvault.keys.keyclient.listDeletedKeys#Context
        for (DeletedKey deletedKey : keyClient.listDeletedKeys(new Context(key2, value2))) {
            System.out.printf("Deleted key's recovery Id %s", deletedKey.recoveryId());
        }
        // END: com.azure.keyvault.keys.keyclient.listDeletedKeys#Context
    }

    /**
     * Generates code sample for using {@link KeyClient#listKeyVersions(String)}
     */
    public void listKeyVersions() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.listKeyVersions
        for (KeyBase key : keyClient.listKeyVersions("keyName")) {
            Key keyWithMaterial  = keyClient.getKey(key);
            System.out.printf("Received key's version with name %s, type %s and version %s", keyWithMaterial.name(),
                    keyWithMaterial.keyMaterial().kty(), keyWithMaterial.version());
        }
        // END: com.azure.keyvault.keys.keyclient.listKeyVersions

        // BEGIN: com.azure.keyvault.keys.keyclient.listKeyVersions#Context
        for (KeyBase key : keyClient.listKeyVersions("keyName", new Context(key2, value2))) {
            Key keyWithMaterial  = keyClient.getKey(key);
            System.out.printf("Received key's version with name %s, type %s and version %s", keyWithMaterial.name(),
                    keyWithMaterial.keyMaterial().kty(), keyWithMaterial.version());
        }
        // END: com.azure.keyvault.keys.keyclient.listKeyVersions#Context
    }

    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private TokenCredential getKeyVaultCredential() {
        return null;
    }
}
