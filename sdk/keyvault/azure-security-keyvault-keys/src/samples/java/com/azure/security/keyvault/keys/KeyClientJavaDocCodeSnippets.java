// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.ImportKeyOptions;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.RandomBytes;

import java.time.OffsetDateTime;
import java.util.Arrays;

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
            .vaultUrl("https://myvault.azure.net/")
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
            .setCurveName(KeyCurveName.P_384)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey ecKey = keyClient.createEcKey(createEcKeyOptions);
        System.out.printf("Key is created with name %s and id %s %n", ecKey.getName(), ecKey.getId());
        // END: com.azure.keyvault.keys.keyclient.createEcKey#keyOptions

        // BEGIN: com.azure.security.keyvault.keys.async.keyClient.createOctKey#CreateOctKeyOptions
        CreateOctKeyOptions createOctKeyOptions = new CreateOctKeyOptions("keyName")
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey octKey = keyClient.createOctKey(createOctKeyOptions);

        System.out.printf("Key is created with name %s and id %s %n", octKey.getName(), octKey.getId());
        // END: com.azure.security.keyvault.keys.async.keyClient.createOctKey#CreateOctKeyOptions
    }

    /**
     * Generates a code sample for using {@link KeyClient#importKey(String, JsonWebKey)}
     */
    public void importKeySnippets() {
        KeyClient keyClient = createClient();
        JsonWebKey jsonWebKeyToImport = new JsonWebKey();
        // BEGIN: com.azure.security.keyvault.keys.keyclient.importKey#string-jsonwebkey
        KeyVaultKey importedKey = keyClient.importKey("keyName", jsonWebKeyToImport);
        System.out.printf("Key is imported with name %s and id %s \n", importedKey.getName(), importedKey.getId());
        // END: com.azure.security.keyvault.keys.keyclient.importKey#string-jsonwebkey

        // BEGIN: com.azure.security.keyvault.keys.keyclient.importKey#options
        ImportKeyOptions options = new ImportKeyOptions("keyName", jsonWebKeyToImport)
            .setHardwareProtected(false);

        KeyVaultKey importedKeyResponse = keyClient.importKey(options);
        System.out.printf("Key is imported with name %s and id %s \n", importedKeyResponse.getName(),
            importedKeyResponse.getId());
        // END: com.azure.security.keyvault.keys.keyclient.importKey#options

        // BEGIN: com.azure.security.keyvault.keys.keyclient.importKeyWithResponse#options-response
        ImportKeyOptions importKeyOptions = new ImportKeyOptions("keyName", jsonWebKeyToImport)
            .setHardwareProtected(false);

        KeyVaultKey importedKeyResp = keyClient.importKeyWithResponse(importKeyOptions, new Context(key1, value1))
            .getValue();
        System.out.printf("Key is imported with name %s and id %s \n", importedKeyResp.getName(),
            importedKeyResp.getId());
        // END: com.azure.security.keyvault.keys.keyclient.importKeyWithResponse#options-response
    }

    /**
     * Generates a code sample for using {@link KeyClient#beginDeleteKey(String)}.
     */
    public void deleteKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.deleteKey#String
        SyncPoller<DeletedKey, Void> deleteKeyPoller = keyClient.beginDeleteKey("keyName");

        PollResponse<DeletedKey> deleteKeyPollResponse = deleteKeyPoller.poll();

        // Deleted date only works for SoftDelete Enabled Key Vault.
        DeletedKey key = deleteKeyPollResponse.getValue();
        System.out.println("Deleted Date  %s" + key.getDeletedOn().toString());
        System.out.printf("Deleted Key's Recovery Id %s", key.getRecoveryId());

        // Key is being deleted on server.
        deleteKeyPoller.waitForCompletion();
        // Key is deleted
        // END: com.azure.keyvault.keys.keyclient.deleteKey#String
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
            .setCurveName(KeyCurveName.P_384)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey ecKey = keyClient.createEcKeyWithResponse(createEcKeyOptions, new Context(key1, value1)).getValue();
        System.out.printf("Key is created with name %s and id %s %n", ecKey.getName(), ecKey.getId());
        // END: com.azure.keyvault.keys.keyclient.createEcKeyWithResponse#keyOptions-Context

        // BEGIN: com.azure.security.keyvault.keys.async.keyClient.createOctKey#CreateOctKeyOptions-Context
        CreateOctKeyOptions createOctKeyOptions = new CreateOctKeyOptions("keyName")
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey octKey =
            keyClient.createOctKeyWithResponse(createOctKeyOptions, new Context(key1, value1)).getValue();

        System.out.printf("Key is created with name %s and id %s %n", octKey.getName(), octKey.getId());
        // END: com.azure.security.keyvault.keys.async.keyClient.createOctKey#CreateOctKeyOptions-Context
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
        KeyVaultKey updatedKey = keyClient.updateKeyProperties(key.getProperties(), KeyOperation.ENCRYPT,
            KeyOperation.DECRYPT);
        System.out.printf("Key is updated with name %s and id %s %n", updatedKey.getName(), updatedKey.getId());
        // END: com.azure.keyvault.keys.keyclient.updateKeyProperties#KeyProperties-keyOperations
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
     * Generates a code sample for using {@link KeyClient#beginRecoverDeletedKey(String)}.
     */
    public void recoverDeletedKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.recoverDeletedKey#String
        SyncPoller<KeyVaultKey, Void> recoverKeyPoller = keyClient.beginRecoverDeletedKey("deletedKeyName");

        PollResponse<KeyVaultKey> recoverKeyPollResponse = recoverKeyPoller.poll();

        KeyVaultKey recoveredKey = recoverKeyPollResponse.getValue();
        System.out.println("Recovered Key Name %s" + recoveredKey.getName());
        System.out.printf("Recovered Key's Id %s", recoveredKey.getId());

        // Key is being recovered on server.
        recoverKeyPoller.waitForCompletion();
        // Key is recovered
        // END: com.azure.keyvault.keys.keyclient.recoverDeletedKey#String
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
        // BEGIN: com.azure.keyvault.keys.keyclient.restoreKeyBackup#byte
        byte[] keyBackupByteArray = {};
        KeyVaultKey keyResponse = keyClient.restoreKeyBackup(keyBackupByteArray);
        System.out.printf("Restored Key with name %s and id %s %n", keyResponse.getName(), keyResponse.getId());
        // END: com.azure.keyvault.keys.keyclient.restoreKeyBackup#byte
    }

    /**
     * Generates a code sample for using {@link KeyClient#restoreKeyBackupWithResponse(byte[], Context)}
     */
    public void restoreKeyWithResponseSnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.restoreKeyBackupWithResponse#byte-Context
        byte[] keyBackupByteArray = {};
        Response<KeyVaultKey> keyResponse = keyClient.restoreKeyBackupWithResponse(keyBackupByteArray,
            new Context(key1, value1));
        System.out.printf("Restored Key with name %s and id %s %n",
            keyResponse.getValue().getName(), keyResponse.getValue().getId());
        // END: com.azure.keyvault.keys.keyclient.restoreKeyBackupWithResponse#byte-Context
    }

    /**
     * Generates a code sample for using {@link KeyClient#listPropertiesOfKeys}
     */
    public void listKeySnippets() {
        KeyClient keyClient = createClient();
        // BEGIN: com.azure.keyvault.keys.keyclient.listKeys
        for (KeyProperties key : keyClient.listPropertiesOfKeys()) {
            KeyVaultKey keyWithMaterial = keyClient.getKey(key.getName(), key.getVersion());
            System.out.printf("Received key with name %s and type %s", keyWithMaterial.getName(),
                keyWithMaterial.getKeyType());
        }
        // END: com.azure.keyvault.keys.keyclient.listKeys

        // BEGIN: com.azure.keyvault.keys.keyclient.listKeys#Context
        for (KeyProperties key : keyClient.listPropertiesOfKeys(new Context(key2, value2))) {
            KeyVaultKey keyWithMaterial = keyClient.getKey(key.getName(), key.getVersion());
            System.out.printf("Received key with name %s and type %s", keyWithMaterial.getName(),
                keyWithMaterial.getKeyType());
        }
        // END: com.azure.keyvault.keys.keyclient.listKeys#Context

        // BEGIN: com.azure.keyvault.keys.keyclient.listKeys.iterableByPage
        keyClient.listPropertiesOfKeys().iterableByPage().forEach(resp -> {
            System.out.printf("Got response headers . Url: %s, Status code: %d %n",
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                KeyVaultKey keyWithMaterial = keyClient.getKey(value.getName(), value.getVersion());
                System.out.printf("Received key with name %s and type %s %n", keyWithMaterial.getName(),
                    keyWithMaterial.getKeyType());
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
            KeyVaultKey keyWithMaterial  = keyClient.getKey(key.getName(), key.getVersion());
            System.out.printf("Received key's version with name %s, type %s and version %s",
                keyWithMaterial.getName(),
                    keyWithMaterial.getKeyType(), keyWithMaterial.getProperties().getVersion());
        }
        // END: com.azure.keyvault.keys.keyclient.listKeyVersions

        // BEGIN: com.azure.keyvault.keys.keyclient.listKeyVersions#Context
        for (KeyProperties key : keyClient.listPropertiesOfKeyVersions("keyName", new Context(key2, value2))) {
            KeyVaultKey keyWithMaterial  = keyClient.getKey(key.getName(), key.getVersion());
            System.out.printf("Received key's version with name %s, type %s and version %s",
                keyWithMaterial.getName(),
                    keyWithMaterial.getKeyType(), keyWithMaterial.getProperties().getVersion());
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
     * Implementation not provided for this method
     * @return {@code null}
     */
    private TokenCredential getKeyVaultCredential() {
        return null;
    }
}
