// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys;

import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.v2.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.v2.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.v2.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.v2.security.keyvault.keys.models.DeletedKey;
import com.azure.v2.security.keyvault.keys.models.ImportKeyOptions;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import com.azure.v2.security.keyvault.keys.models.KeyCurveName;
import com.azure.v2.security.keyvault.keys.models.KeyExportEncryptionAlgorithm;
import com.azure.v2.security.keyvault.keys.models.KeyOperation;
import com.azure.v2.security.keyvault.keys.models.KeyProperties;
import com.azure.v2.security.keyvault.keys.models.KeyRotationLifetimeAction;
import com.azure.v2.security.keyvault.keys.models.KeyRotationPolicy;
import com.azure.v2.security.keyvault.keys.models.KeyRotationPolicyAction;
import com.azure.v2.security.keyvault.keys.models.KeyType;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKey;
import com.azure.v2.security.keyvault.keys.models.ReleaseKeyOptions;
import com.azure.v2.security.keyvault.keys.models.ReleaseKeyResult;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;

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
        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.instantiation
        KeyClient keyClient = new KeyClientBuilder()
            .endpoint("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.v2.security.keyvault.keys.KeyClient.instantiation
        return keyClient;
    }

    /**
     * Generates code sample for creating a {@link KeyClient} using a custom {@link HttpClient}.
     *
     * @return An instance of {@link KeyClient}.
     */
    public KeyClient createClientWithHttpClient() {
        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.instantiation.withHttpClient
        KeyClient keyClient = new KeyClientBuilder()
            .endpoint("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpInstrumentationOptions(new HttpInstrumentationOptions()
                .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS))
            .httpClient(HttpClient.getSharedInstance())
            .buildClient();
        // END: com.azure.v2.security.keyvault.keys.KeyClient.instantiation.withHttpClient

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

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.createKey#String-KeyType
        KeyVaultKey key = keyClient.createKey("keyName", KeyType.EC);
        System.out.printf("Created key with name: %s and id: %s%n", key.getName(), key.getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.createKey#String-KeyType

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.createKey#CreateKeyOptions
        CreateKeyOptions createKeyOptions = new CreateKeyOptions("keyName", KeyType.RSA)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey optionsKey = keyClient.createKey(createKeyOptions);

        System.out.printf("Created key with name: %s and id: %s%n", optionsKey.getName(), optionsKey.getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.createKey#CreateKeyOptions

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.createRsaKey#CreateRsaKeyOptions
        CreateRsaKeyOptions createRsaKeyOptions = new CreateRsaKeyOptions("keyName")
            .setKeySize(2048)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey rsaKey = keyClient.createRsaKey(createRsaKeyOptions);

        System.out.printf("Created key with name: %s and id: %s%n", rsaKey.getName(), rsaKey.getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.createRsaKey#CreateRsaKeyOptions

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.createEcKey#CreateOctKeyOptions
        CreateEcKeyOptions createEcKeyOptions = new CreateEcKeyOptions("keyName")
            .setCurveName(KeyCurveName.P_384)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey ecKey = keyClient.createEcKey(createEcKeyOptions);

        System.out.printf("Created key with name: %s and id: %s%n", ecKey.getName(), ecKey.getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.createEcKey#CreateOctKeyOptions

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.createOctKey#CreateOctKeyOptions
        CreateOctKeyOptions createOctKeyOptions = new CreateOctKeyOptions("keyName")
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        KeyVaultKey octKey = keyClient.createOctKey(createOctKeyOptions);

        System.out.printf("Created key with name: %s and id: %s%n", octKey.getName(), octKey.getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.createOctKey#CreateOctKeyOptions
    }

    /**
     * Generates code samples for using {@link KeyClient#importKey(String, JsonWebKey)},
     * {@link KeyClient#importKey(ImportKeyOptions)} and
     *{@link KeyClient#importKeyWithResponse(ImportKeyOptions, RequestContext)}.
     */
    public void importKey() {
        KeyClient keyClient = createClient();
        JsonWebKey jsonWebKeyToImport = new JsonWebKey();
        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.importKey#String-JsonWebKey
        KeyVaultKey key = keyClient.importKey("keyName", jsonWebKeyToImport);

        System.out.printf("Imported key with name: %s and id: %s%n", key.getName(), key.getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.importKey#String-JsonWebKey

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.importKey#ImportKeyOptions
        ImportKeyOptions options = new ImportKeyOptions("keyName", jsonWebKeyToImport)
            .setHardwareProtected(false);
        KeyVaultKey importedKey = keyClient.importKey(options);

        System.out.printf("Imported key with name: %s and id: %s%n", importedKey.getName(),
            importedKey.getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.importKey#ImportKeyOptions

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.importKeyWithResponse#ImportKeyOptions-RequestContext
        ImportKeyOptions importKeyOptions = new ImportKeyOptions("keyName", jsonWebKeyToImport)
            .setHardwareProtected(false);
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultKey> response = keyClient.importKeyWithResponse(importKeyOptions, requestContext);

        System.out.printf("Imported key with name: %s and id: %s%n", response.getValue().getName(),
            response.getValue().getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.importKeyWithResponse#ImportKeyOptions-RequestContext
    }

    /**
     * Generates a code sample for using {/@link KeyClient#beginDeleteKey(String)}.
     */
    public void beginDeleteKey() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.deleteKey#String
        Poller<DeletedKey, Void> deleteKeyPoller = keyClient.beginDeleteKey("keyName");
        PollResponse<DeletedKey> deleteKeyPollResponse = deleteKeyPoller.poll();

        // Deleted date only works for SoftDelete Enabled Key Vault.
        DeletedKey deletedKey = deleteKeyPollResponse.getValue();

        System.out.printf("Key delete date: %s%n", deletedKey.getDeletedOn());
        System.out.printf("Deleted key's recovery id: %s%n", deletedKey.getRecoveryId());

        // Key is being deleted on the server.
        deleteKeyPoller.waitForCompletion();
        // Key is deleted
        // END: com.azure.v2.security.keyvault.keys.KeyClient.deleteKey#String
    }

    /**
     * Generates a code sample for using {@link KeyClient#getDeletedKey(String)}.
     */
    public void getDeletedKey() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.getDeletedKey#String
        DeletedKey deletedKey = keyClient.getDeletedKey("keyName");

        System.out.printf("Deleted key's recovery id: %s%n", deletedKey.getRecoveryId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.getDeletedKey#String
    }

    /**
     * Generates a code sample for using {@link KeyClient#createKeyWithResponse(CreateKeyOptions, RequestContext)},
     * {@link KeyClient#createRsaKeyWithResponse(CreateRsaKeyOptions, RequestContext)},
     * {@link KeyClient#createEcKeyWithResponse(CreateEcKeyOptions, RequestContext)} and
     * {@link KeyClient#createOctKeyWithResponse(CreateOctKeyOptions, RequestContext)}.
     */
    public void createKeyWithResponse() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.createKeyWithResponse#CreateKeyOptions-RequestContext
        CreateKeyOptions createKeyOptions = new CreateKeyOptions("keyName", KeyType.RSA)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultKey> createKeyResponse = keyClient.createKeyWithResponse(createKeyOptions, requestContext);

        System.out.printf("Created key with name: %s and: id %s%n", createKeyResponse.getValue().getName(),
            createKeyResponse.getValue().getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.createKeyWithResponse#CreateKeyOptions-RequestContext

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.createRsaKeyWithResponse#CreateRsaKeyOptions-RequestContext
        CreateRsaKeyOptions createRsaKeyOptions = new CreateRsaKeyOptions("keyName")
            .setKeySize(2048)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        RequestContext reqContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultKey> createRsaKeyResponse =
            keyClient.createRsaKeyWithResponse(createRsaKeyOptions, reqContext);

        System.out.printf("Created key with name: %s and: id %s%n", createRsaKeyResponse.getValue().getName(),
            createRsaKeyResponse.getValue().getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.createRsaKeyWithResponse#CreateRsaKeyOptions-RequestContext

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.createEcKeyWithResponse#CreateEcKeyOptions-RequestContext
        CreateEcKeyOptions createEcKeyOptions = new CreateEcKeyOptions("keyName")
            .setCurveName(KeyCurveName.P_384)
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        Response<KeyVaultKey> createEcKeyResponse =
            keyClient.createEcKeyWithResponse(createEcKeyOptions, reqContext);

        System.out.printf("Created key with name: %s and: id %s%n", createEcKeyResponse.getValue().getName(),
            createEcKeyResponse.getValue().getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.createEcKeyWithResponse#CreateEcKeyOptions-RequestContext

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.createOctKey#CreateOctKeyOptions-RequestContext
        CreateOctKeyOptions createOctKeyOptions = new CreateOctKeyOptions("keyName")
            .setNotBefore(OffsetDateTime.now().plusDays(1))
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        Response<KeyVaultKey> createOctKeyResponse =
            keyClient.createOctKeyWithResponse(createOctKeyOptions, reqContext);

        System.out.printf("Created key with name: %s and: id %s%n", createOctKeyResponse.getValue().getName(),
            createOctKeyResponse.getValue().getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.createOctKey#CreateOctKeyOptions-RequestContext
    }

    /**
     * Generates a code sample for using {@link KeyClient#getKeyWithResponse(String, String, RequestContext)}.
     */
    public void getKeyWithResponse() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.getKeyWithResponse#String-String-RequestContext
        String keyVersion = "<key-version>";
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultKey> getKeyResponse =
            keyClient.getKeyWithResponse("keyName", keyVersion, requestContext);

        System.out.printf("Retrieved key with name: %s and: id %s%n", getKeyResponse.getValue().getName(),
            getKeyResponse.getValue().getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.getKeyWithResponse#String-String-RequestContext
    }

    /**
     * Generates a code sample for using {@link KeyClient#getKey(String)} and
     * {@link KeyClient#getKey(String, String)}.
     */
    public void getKey() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.getKey#String
        KeyVaultKey keyWithVersionValue = keyClient.getKey("keyName");

        System.out.printf("Retrieved key with name: %s and: id %s%n", keyWithVersionValue.getName(),
            keyWithVersionValue.getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.getKey#String

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.getKey#String-String
        String keyVersion = "<key-version>";
        KeyVaultKey keyWithVersion = keyClient.getKey("keyName", keyVersion);

        System.out.printf("Retrieved key with name: %s and: id %s%n", keyWithVersion.getName(),
            keyWithVersion.getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.getKey#String-String
    }

    /**
     * Generates a code sample for using
     * {@link KeyClient#updateKeyPropertiesWithResponse(KeyProperties, List, RequestContext)}.
     */
    public void updateKeyPropertiesWithResponse() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.updateKeyPropertiesWithResponse#KeyProperties-RequestContext-KeyOperation
        KeyVaultKey key = keyClient.getKey("keyName");

        key.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(60));

        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultKey> updateKeyResponse = keyClient.updateKeyPropertiesWithResponse(key.getProperties(),
            Arrays.asList(KeyOperation.ENCRYPT, KeyOperation.DECRYPT), requestContext);

        System.out.printf("Updated key with name: %s and id: %s%n", updateKeyResponse.getValue().getName(),
            updateKeyResponse.getValue().getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.updateKeyPropertiesWithResponse#KeyProperties-RequestContext-KeyOperation
    }

    /**
     * Generates a code sample for using {@link KeyClient#updateKeyProperties(KeyProperties, List)}.
     */
    public void updateKeyProperties() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.updateKeyProperties#KeyProperties-KeyOperation
        KeyVaultKey key = keyClient.getKey("keyName");

        key.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(60));

        KeyVaultKey updatedKey = keyClient.updateKeyProperties(key.getProperties(),
            Arrays.asList(KeyOperation.ENCRYPT, KeyOperation.DECRYPT));

        System.out.printf("Key is updated with name %s and id %s %n", updatedKey.getName(), updatedKey.getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.updateKeyProperties#KeyProperties-KeyOperation
    }

    /**
     * Generates a code sample for using {@link KeyClient#getDeletedKeyWithResponse(String, RequestContext)}.
     */
    public void getDeletedKeyWithResponse() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.getDeletedKeyWithResponse#String-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<DeletedKey> deletedKeyResponse = keyClient.getDeletedKeyWithResponse("keyName", requestContext);

        System.out.printf("Deleted key with recovery id: %s%n", deletedKeyResponse.getValue().getRecoveryId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.getDeletedKeyWithResponse#String-RequestContext
    }

    /**
     * Generates a code sample for using {@link KeyClient#purgeDeletedKey(String)}.
     */
    public void purgeDeletedKey() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.purgeDeletedKey#String
        keyClient.purgeDeletedKey("deletedKeyName");
        // END: com.azure.v2.security.keyvault.keys.KeyClient.purgeDeletedKey#String
    }

    /**
     * Generates a code sample for using {@link KeyClient#purgeDeletedKeyWithResponse(String, RequestContext)}.
     */
    public void purgeDeletedKeyWithResponse() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.purgeDeletedKeyWithResponse#String-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<Void> purgeDeletedKeyResponse =
            keyClient.purgeDeletedKeyWithResponse("deletedKeyName", requestContext);

        System.out.printf("Purge response status code: %d%n", purgeDeletedKeyResponse.getStatusCode());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.purgeDeletedKeyWithResponse#String-RequestContext
    }

    /**
     * Generates a code sample for using {/@link KeyClient#beginRecoverDeletedKey(String)}.
     */
    public void beginRecoverDeletedKey() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.recoverDeletedKey#String
        Poller<KeyVaultKey, Void> recoverKeyPoller = keyClient.beginRecoverDeletedKey("deletedKeyName");

        PollResponse<KeyVaultKey> recoverKeyPollResponse = recoverKeyPoller.poll();

        KeyVaultKey recoveredKey = recoverKeyPollResponse.getValue();
        System.out.printf("Recovered key name: %s%n", recoveredKey.getName());
        System.out.printf("Recovered key id: %s%n", recoveredKey.getId());

        // Key is being recovered on the server.
        recoverKeyPoller.waitForCompletion();
        // Key is recovered
        // END: com.azure.v2.security.keyvault.keys.KeyClient.recoverDeletedKey#String
    }

    /**
     * Generates a code sample for using {@link KeyClient#backupKey(String)}.
     */
    public void backupKey() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.backupKey#String
        byte[] keyBackup = keyClient.backupKey("keyName");

        System.out.printf("Key backup byte array length: %s%n", keyBackup.length);
        // END: com.azure.v2.security.keyvault.keys.KeyClient.backupKey#String
    }

    /**
     * Generates a code sample for using {@link KeyClient#backupKeyWithResponse(String, RequestContext)}.
     */
    public void backupKeyWithResponse() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.backupKeyWithResponse#String-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<byte[]> backupKeyResponse = keyClient.backupKeyWithResponse("keyName", requestContext);

        System.out.printf("Key backup byte array length: %s%n", backupKeyResponse.getValue().length);
        // END: com.azure.v2.security.keyvault.keys.KeyClient.backupKeyWithResponse#String-RequestContext
    }

    /**
     * Generates a code sample for using {@link KeyClient#restoreKeyBackup}.
     */
    public void restoreKeyBackup() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.restoreKeyBackup#byte
        byte[] keyBackupByteArray = {};
        KeyVaultKey keyResponse = keyClient.restoreKeyBackup(keyBackupByteArray);
        System.out.printf("Restored key with name: %s and: id %s%n", keyResponse.getName(), keyResponse.getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.restoreKeyBackup#byte
    }

    /**
     * Generates a code sample for using {@link KeyClient#restoreKeyBackupWithResponse(byte[], RequestContext)}.
     */
    public void restoreKeyBackupWithResponse() {
        KeyClient keyClient = createClient();
        byte[] keyBackupByteArray = {};
        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.restoreKeyBackupWithResponse#byte-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultKey> keyResponse = keyClient.restoreKeyBackupWithResponse(keyBackupByteArray, requestContext);

        System.out.printf("Restored key with name: %s and: id %s%n",
            keyResponse.getValue().getName(), keyResponse.getValue().getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.restoreKeyBackupWithResponse#byte-RequestContext
    }

    /**
     * Generates a code sample for using {@link KeyClient#listPropertiesOfKeys()} and
     * {@link KeyClient#listPropertiesOfKeys(RequestContext)}.
     */
    public void listPropertiesOfKeys() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys
        keyClient.listPropertiesOfKeys().forEach(keyProperties -> {
            KeyVaultKey key = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());

            System.out.printf("Retrieved key with name: %s and type: %s%n", key.getName(), key.getKeyType());
        });
        // END: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage
        keyClient.listPropertiesOfKeys().iterableByPage().forEach(pagedResponse -> {
            System.out.printf("Got response details. Url: %s. Status code: %d.%n",
                pagedResponse.getRequest().getUri(), pagedResponse.getStatusCode());

            pagedResponse.getValue().forEach(keyProperties -> {
                KeyVaultKey key = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());

                System.out.printf("Retrieved key with name: %s and type: %s%n", key.getName(), key.getKeyType());
            });
        });
        // END: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys#RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        keyClient.listPropertiesOfKeys(requestContext).forEach(keyProperties -> {
            KeyVaultKey key = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());

            System.out.printf("Retrieved key with name: %s and type: %s%n", key.getName(),
                key.getKeyType());
        });
        // END: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys#RequestContext

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage#RequestContext
        RequestContext reqContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        keyClient.listPropertiesOfKeys(reqContext).iterableByPage().forEach(pagedResponse -> {
            System.out.printf("Got response details. Url: %s. Status code: %d.%n",
                pagedResponse.getRequest().getUri(), pagedResponse.getStatusCode());

            pagedResponse.getValue().forEach(keyProperties -> {
                KeyVaultKey key = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());

                System.out.printf("Retrieved key with name: %s and type: %s%n", key.getName(),
                    key.getKeyType());
            });
        });
        // END: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage#RequestContext
    }

    /**
     * Generates a code sample for using {@link KeyClient#listDeletedKeys()} and
     * {@link KeyClient#listDeletedKeys(RequestContext)}.
     */
    public void listDeletedKeys() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys
        keyClient.listDeletedKeys().forEach(deletedKey -> {
            System.out.printf("Deleted key's recovery id:%s%n", deletedKey.getRecoveryId());
        });
        // END: com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage
        keyClient.listDeletedKeys().iterableByPage().forEach(pagedResponse -> {
            System.out.printf("Got response details. Url: %s. Status code: %d.%n",
                pagedResponse.getRequest().getUri(), pagedResponse.getStatusCode());

            pagedResponse.getValue().forEach(deletedKey ->
                System.out.printf("Deleted key's recovery id:%s%n", deletedKey.getRecoveryId()));
        });
        // END: com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys#RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        keyClient.listDeletedKeys(requestContext).forEach(deletedKey -> {
            System.out.printf("Deleted key's recovery id:%s%n", deletedKey.getRecoveryId());
        });
        // END: com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys#RequestContext

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage#RequestContext
        RequestContext reqContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();
        
        keyClient.listDeletedKeys(reqContext).iterableByPage().forEach(pagedResponse -> {
            System.out.printf("Got response details. Url: %s. Status code: %d.%n",
                pagedResponse.getRequest().getUri(), pagedResponse.getStatusCode());

            pagedResponse.getValue().forEach(deletedKey ->
                System.out.printf("Deleted key's recovery id:%s%n", deletedKey.getRecoveryId()));
        });
        // END: com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage#RequestContext
    }

    /**
     * Generates code sample for using {@link KeyClient#listPropertiesOfKeyVersions(String)} and
     * {@link KeyClient#listPropertiesOfKeyVersions(String, RequestContext)}.
     */
    public void listPropertiesOfKeyVersions() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String
        keyClient.listPropertiesOfKeyVersions("keyName").forEach(keyProperties -> {
            KeyVaultKey key = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());

            System.out.printf("Retrieved key version: %s with name: %s and type: %s%n",
                key.getProperties().getVersion(), key.getName(), key.getKeyType());
        });
        // END: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage#String
        keyClient.listPropertiesOfKeyVersions("keyName").iterableByPage().forEach(pagedResponse -> {
            System.out.printf("Got response details. Url: %s. Status code: %d.%n",
                pagedResponse.getRequest().getUri(), pagedResponse.getStatusCode());

            pagedResponse.getValue().forEach(keyProperties ->
                System.out.printf("Key name: %s. Key version: %s.%n", keyProperties.getName(),
                    keyProperties.getVersion()));
        });
        // END: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage#String

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        for (KeyProperties keyProperties : keyClient.listPropertiesOfKeyVersions("keyName", requestContext)) {
            KeyVaultKey key = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());

            System.out.printf("Retrieved key version: %s with name: %s and type: %s%n",
                key.getProperties().getVersion(), key.getName(), key.getKeyType());
        }
        // END: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String-RequestContext

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage#String-RequestContext
        RequestContext reqContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        keyClient.listPropertiesOfKeyVersions("keyName", reqContext).iterableByPage().forEach(pagedResponse -> {
            System.out.printf("Got response details. Url: %s. Status code: %d.%n",
                pagedResponse.getRequest().getUri(), pagedResponse.getStatusCode());

            pagedResponse.getValue().forEach(keyProperties ->
                System.out.printf("Key name: %s. Key version: %s.%n", keyProperties.getName(),
                    keyProperties.getVersion()));
        });
        // END: com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage#String-RequestContext
    }

    /**
     * Generates code samples for using {@link KeyClient#getRandomBytes(int)} and
     * {@link KeyClient#getRandomBytesWithResponse(int, RequestContext)}.
     */
    public void getRandomBytes() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.getRandomBytes#int
        int amount = 16;
        byte[] randomBytes = keyClient.getRandomBytes(amount);

        System.out.printf("Retrieved %d random bytes: %s%n", amount, Arrays.toString(randomBytes));
        // END: com.azure.v2.security.keyvault.keys.KeyClient.getRandomBytes#int

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.getRandomBytesWithResponse#int-RequestContext
        int amountOfBytes = 16;
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<byte[]> response = keyClient.getRandomBytesWithResponse(amountOfBytes, requestContext);

        System.out.printf("Response received successfully with status code: %d. Retrieved %d random bytes: %s%n",
            response.getStatusCode(), amountOfBytes, Arrays.toString(response.getValue()));
        // END: com.azure.v2.security.keyvault.keys.KeyClient.getRandomBytesWithResponse#int-RequestContext
    }

    /**
     * Generates code samples for using {@link KeyClient#releaseKey(String, String)},
     * {@link KeyClient#releaseKey(String, String, String)} and
     * {@link KeyClient#releaseKeyWithResponse(String, String, String, ReleaseKeyOptions, RequestContext)}.
     */
    public void releaseKey() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.releaseKey#String-String
        String targetAttestationToken = "someAttestationToken";
        ReleaseKeyResult releaseKeyResult = keyClient.releaseKey("keyName", targetAttestationToken);

        System.out.printf("Signed object containing released key: %s%n", releaseKeyResult);
        // END: com.azure.v2.security.keyvault.keys.KeyClient.releaseKey#String-String

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.releaseKey#String-String-String
        String myKeyVersion = "<key-version>";
        String myTargetAttestationToken = "someAttestationToken";
        ReleaseKeyResult releaseKeyVersionResult =
            keyClient.releaseKey("keyName", myKeyVersion, myTargetAttestationToken);

        System.out.printf("Signed object containing released key: %s%n", releaseKeyVersionResult);
        // END: com.azure.v2.security.keyvault.keys.KeyClient.releaseKey#String-String-String

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.releaseKeyWithResponse#String-String-String-ReleaseKeyOptions-RequestContext
        String releaseKeyVersion = "<key-version>";
        String someTargetAttestationToken = "someAttestationToken";
        ReleaseKeyOptions releaseKeyOptions = new ReleaseKeyOptions()
            .setAlgorithm(KeyExportEncryptionAlgorithm.RSA_AES_KEY_WRAP_256)
            .setNonce("someNonce");
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<ReleaseKeyResult> releaseKeyResultResponse = keyClient.releaseKeyWithResponse("keyName",
            releaseKeyVersion, someTargetAttestationToken, releaseKeyOptions, requestContext);

        System.out.printf("Response received successfully with status code: %d. Signed object containing"
                + "released key: %s%n", releaseKeyResultResponse.getStatusCode(),
            releaseKeyResultResponse.getValue().getValue());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.releaseKeyWithResponse#String-String-String-ReleaseKeyOptions-RequestContext
    }

    /**
     * Generates code samples for using {@link KeyClient#rotateKey(String)} and
     * {@link KeyClient#rotateKeyWithResponse(String, RequestContext)}.
     */
    public void rotateKey() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String
        KeyVaultKey key = keyClient.rotateKey("keyName");

        System.out.printf("Rotated key with name: %s and version:%s%n", key.getName(),
            key.getProperties().getVersion());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();
        Response<KeyVaultKey> keyResponse = keyClient.rotateKeyWithResponse("keyName", requestContext);

        System.out.printf("Response received successfully with status code: %d. Rotated key with name: %s and"
                + "version: %s%n", keyResponse.getStatusCode(), keyResponse.getValue().getName(),
            keyResponse.getValue().getProperties().getVersion());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String-RequestContext
    }

    /**
     * Generates code samples for using {@link KeyClient#getKeyRotationPolicy(String)} and
     * {@link KeyClient#getKeyRotationPolicyWithResponse(String, RequestContext)}.
     */
    public void getKeyRotationPolicy() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.getKeyRotationPolicy#String
        KeyRotationPolicy keyRotationPolicy = keyClient.getKeyRotationPolicy("keyName");

        System.out.printf("Retrieved key rotation policy with id: %s%n", keyRotationPolicy.getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.getKeyRotationPolicy#String

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.getKeyRotationPolicyWithResponse#String-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyRotationPolicy> keyRotationPolicyResponse =
            keyClient.getKeyRotationPolicyWithResponse("keyName", requestContext);

        System.out.printf("Response received successfully with status code: %d. Retrieved key rotation policy"
            + "with id: %s%n", keyRotationPolicyResponse.getStatusCode(), keyRotationPolicyResponse.getValue().getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.getKeyRotationPolicyWithResponse#String-RequestContext
    }

    /**
     * Generates code samples for using {@link KeyClient#updateKeyRotationPolicy(String, KeyRotationPolicy)}
     * and {@link KeyClient#updateKeyRotationPolicyWithResponse(String, KeyRotationPolicy, RequestContext)}.
     */
    public void updateKeyRotationPolicy() {
        KeyClient keyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.updateKeyRotationPolicy#String-KeyRotationPolicy
        List<KeyRotationLifetimeAction> lifetimeActions = new ArrayList<>();
        KeyRotationLifetimeAction rotateLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.ROTATE)
            .setTimeAfterCreate("P90D");
        KeyRotationLifetimeAction notifyLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.NOTIFY)
            .setTimeBeforeExpiry("P45D");

        lifetimeActions.add(rotateLifetimeAction);
        lifetimeActions.add(notifyLifetimeAction);

        KeyRotationPolicy keyRotationPolicy = new KeyRotationPolicy()
            .setLifetimeActions(lifetimeActions)
            .setExpiresIn("P6M");

        KeyRotationPolicy updatedPolicy =
            keyClient.updateKeyRotationPolicy("keyName", keyRotationPolicy);

        System.out.printf("Updated key rotation policy with id: %s%n", updatedPolicy.getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.updateKeyRotationPolicy#String-KeyRotationPolicy

        // BEGIN: com.azure.v2.security.keyvault.keys.KeyClient.updateKeyRotationPolicyWithResponse#String-KeyRotationPolicy-RequestContext
        List<KeyRotationLifetimeAction> myLifetimeActions = new ArrayList<>();
        KeyRotationLifetimeAction myRotateLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.ROTATE)
            .setTimeAfterCreate("P90D");
        KeyRotationLifetimeAction myNotifyLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.NOTIFY)
            .setTimeBeforeExpiry("P45D");

        myLifetimeActions.add(myRotateLifetimeAction);
        myLifetimeActions.add(myNotifyLifetimeAction);

        KeyRotationPolicy myKeyRotationPolicy = new KeyRotationPolicy()
            .setLifetimeActions(myLifetimeActions)
            .setExpiresIn("P6M");
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyRotationPolicy> keyRotationPolicyResponse =
            keyClient.updateKeyRotationPolicyWithResponse("keyName", myKeyRotationPolicy, requestContext);

        System.out.printf("Response received successfully with status code: %d. Updated key rotation policy"
            + "with id: %s%n", keyRotationPolicyResponse.getStatusCode(), keyRotationPolicyResponse.getValue().getId());
        // END: com.azure.v2.security.keyvault.keys.KeyClient.updateKeyRotationPolicyWithResponse#String-KeyRotationPolicy-RequestContext
    }
}
