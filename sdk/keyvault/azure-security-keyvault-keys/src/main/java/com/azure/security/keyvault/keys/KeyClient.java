// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.ImportKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.RandomBytes;
import com.azure.security.keyvault.keys.models.ReleaseKeyOptions;
import com.azure.security.keyvault.keys.models.ReleaseKeyResult;

/**
 * The KeyClient provides synchronous methods to manage {@link KeyVaultKey keys} in the Azure Key Vault. The client supports
 * creating, retrieving, updating, deleting, purging, backing up, restoring and listing the {@link KeyVaultKey keys}. The client
 * also supports listing {@link DeletedKey deleted keys} for a soft-delete enabled Azure Key Vault.
 *
 * <p><strong>Samples to construct the sync client</strong></p>
 * {@codesnippet com.azure.security.keyvault.keys.keyclient.instantiation}
 *
 * @see KeyClientBuilder
 * @see PagedIterable
 */
@ServiceClient(builder = KeyClientBuilder.class, serviceInterfaces = KeyService.class)
public final class KeyClient {
    private final KeyAsyncClient client;

    /**
     * Creates a KeyClient that uses {@code pipeline} to service requests
     *
     * @param client The {@link KeyAsyncClient} that the client routes its request through.
     */
    KeyClient(KeyAsyncClient client) {
        this.client = client;
    }

    /**
     * Get the vault endpoint url
     * @return the vault endpoint url
     */
    public String getVaultUrl() {
        return client.getVaultUrl();
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link KeyType keyType} indicates the type of key to create. Possible values include:
     * {@link KeyType#EC EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM},
     * {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key. Prints out the details of the created key.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.createKey#string-keyType}
     *
     * @param name The name of the key being created.
     * @param keyType The type of key to create. For valid values, see {@link KeyType KeyType}.
     * @return The {@link KeyVaultKey created key}.
     * @throws ResourceModifiedException if {@code name} or {@code keyType} is null.
     * @throws HttpResponseException if {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createKey(String name, KeyType keyType) {
        return createKeyWithResponse(new CreateKeyOptions(name, keyType), Context.NONE).getValue();
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link CreateKeyOptions} is required. The {@link CreateKeyOptions#getExpiresOn() expires} and {@link
     * CreateKeyOptions#getNotBefore() notBefore} values are optional. The {@link CreateKeyOptions#isEnabled()} enabled} field
     * is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link CreateKeyOptions#getKeyType() keyType} indicates the type of key to create. Possible values
     * include: {@link KeyType#EC EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA},
     * {@link KeyType#RSA_HSM RSA-HSM}, {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new RSA key which activates in one day and expires in one year. Prints out the details of the
     * created key.</p> {@codesnippet com.azure.keyvault.keys.keyclient.createKey#keyOptions}
     *
     * @param createKeyOptions The key options object containing information about the key being created.
     * @return The {@link KeyVaultKey created key}.
     * @throws NullPointerException if {@code keyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code keyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createKey(CreateKeyOptions createKeyOptions) {
        return createKeyWithResponse(createKeyOptions, Context.NONE).getValue();
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link CreateKeyOptions} is required. The {@link CreateKeyOptions#getExpiresOn() expires} and {@link
     * CreateKeyOptions#getNotBefore() notBefore} values are optional. The {@link CreateKeyOptions#isEnabled() enabled} field
     * is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link CreateKeyOptions#getKeyType() keyType} indicates the type of key to create. Possible values
     * include: {@link KeyType#EC EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA},
     * {@link KeyType#RSA_HSM RSA-HSM}, {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new RSA key which activates in one day and expires in one year. Prints out the details of the
     * created key.</p> {@codesnippet com.azure.keyvault.keys.keyclient.createKeyWithResponse#keyCreateOptions-Context}
     *
     * @param createKeyOptions The key options object containing information about the key being created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey created key}.
     * @throws NullPointerException if {@code keyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code keyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createKeyWithResponse(CreateKeyOptions createKeyOptions, Context context) {
        return client.createKeyWithResponse(createKeyOptions, context).block();
    }

    /**
     * Creates a new Rsa key and stores it in the key vault. The create Rsa key operation can be used to create any Rsa
     * key type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It
     * requires the {@code keys/create} permission.
     *
     * <p>The {@link CreateRsaKeyOptions} is required. The {@link CreateRsaKeyOptions#getKeySize() keySize} can be
     * optionally specified. The {@link CreateRsaKeyOptions#getExpiresOn() expires} and {@link
     * CreateRsaKeyOptions#getNotBefore() notBefore} values are optional. The {@link CreateRsaKeyOptions#isEnabled() enabled}
     * field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link CreateRsaKeyOptions#getKeyType() keyType} indicates the type of key to create. Possible values
     * include: {@link KeyType#RSA RSA} and {@link KeyType#RSA_HSM RSA-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new RSA key with size 2048 which activates in one day and expires in one year. Prints out the
     * details of the created key.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.createRsaKey#keyOptions}
     *
     * @param createRsaKeyOptions The key options object containing information about the rsa key being created.
     * @return The {@link KeyVaultKey created key}.
     * @throws NullPointerException if {@code rsaKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code rsaKeyCreateOptions} is malformed.
     * @throws HttpResponseException if {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createRsaKey(CreateRsaKeyOptions createRsaKeyOptions) {
        return createRsaKeyWithResponse(createRsaKeyOptions, Context.NONE).getValue();
    }

    /**
     * Creates a new Rsa key and stores it in the key vault. The create Rsa key operation can be used to create any Rsa
     * key type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It
     * requires the {@code keys/create} permission.
     *
     * <p>The {@link CreateRsaKeyOptions} is required. The {@link CreateRsaKeyOptions#getKeySize() keySize} can be
     * optionally specified. The {@link CreateRsaKeyOptions#getExpiresOn() expires} and {@link
     * CreateRsaKeyOptions#getNotBefore() notBefore} values are optional. The {@link CreateRsaKeyOptions#isEnabled() enabled}
     * field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link CreateRsaKeyOptions#getKeyType() keyType} indicates the type of key to create. Possible values
     * include: {@link KeyType#RSA RSA} and {@link KeyType#RSA_HSM RSA-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new RSA key with size 2048 which activates in one day and expires in one year. Prints out the
     * details of the created key.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.createRsaKeyWithResponse#keyOptions-Context}
     *
     * @param createRsaKeyOptions The key options object containing information about the rsa key being created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey created key}.
     * @throws NullPointerException if {@code rsaKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code rsaKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createRsaKeyWithResponse(CreateRsaKeyOptions createRsaKeyOptions, Context context) {
        return client.createRsaKeyWithResponse(createRsaKeyOptions, context).block();
    }

    /**
     * Creates a new Ec key and  stores it in the key vault. The create Ec key operation can be used to create any Ec
     * key type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It
     * requires the {@code keys/create} permission.
     *
     * <p>The {@link CreateEcKeyOptions} parameter is required. The {@link CreateEcKeyOptions#getCurveName() key curve} can be
     * optionally specified. If not specified, default value of {@link KeyCurveName#P_256 P-256} is used by Azure Key
     * Vault. The {@link CreateEcKeyOptions#getExpiresOn() expires} and {@link CreateEcKeyOptions#getNotBefore() notBefore}
     * values are optional. The {@link CreateEcKeyOptions#isEnabled() enabled} field is set to true by Azure Key Vault, if
     * not specified.</p>
     *
     * <p>The {@link CreateEcKeyOptions#getKeyType() keyType} indicates the type of key to create. Possible values
     * include: {@link KeyType#EC EC} and {@link KeyType#EC_HSM EC-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key with P-384 web key curve. The key activates in one day and expires in one year. Prints
     * out the details of the created key.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.createEcKey#keyOptions}
     *
     * @param createEcKeyOptions The key options object containing information about the ec key being created.
     * @return The {@link KeyVaultKey created key}.
     * @throws NullPointerException if {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code ecKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createEcKey(CreateEcKeyOptions createEcKeyOptions) {
        return createEcKeyWithResponse(createEcKeyOptions, Context.NONE).getValue();
    }

    /**
     * Creates a new Ec key and  stores it in the key vault. The create Ec key operation can be used to create any Ec
     * key type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It
     * requires the {@code keys/create} permission.
     *
     * <p>The {@link CreateEcKeyOptions} parameter is required. The {@link CreateEcKeyOptions#getCurveName() key curve} can be
     * optionally specified. If not specified, default value of {@link KeyCurveName#P_256 P-256} is used by Azure Key
     * Vault. The {@link CreateEcKeyOptions#getExpiresOn() expires} and {@link CreateEcKeyOptions#getNotBefore() notBefore}
     * values are optional. The {@link CreateEcKeyOptions#isEnabled()} enabled} field is set to true by Azure Key Vault, if
     * not specified.</p>
     *
     * <p>The {@link CreateEcKeyOptions#getKeyType() keyType} indicates the type of key to create. Possible values
     * include:
     * {@link KeyType#EC EC} and {@link KeyType#EC_HSM EC-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key with P-384 web key curve. The key activates in one day and expires in one year. Prints
     * out the details of the newly created key.</p>
     *
     * {@codesnippet com.azure.keyvault.keys.keyclient.createEcKeyWithResponse#keyOptions-Context}
     *
     * @param createEcKeyOptions The key options object containing information about the ec key being created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey created key}.
     * @throws NullPointerException if {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code ecKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createEcKeyWithResponse(CreateEcKeyOptions createEcKeyOptions, Context context) {
        return client.createEcKeyWithResponse(createEcKeyOptions, context).block();
    }

    /**
     * Creates and stores a new symmetric key in Key Vault. If the named key already exists, Azure Key Vault creates
     * a new version of the key. This operation requires the keys/create permission.
     *
     * <p>The {@link CreateOctKeyOptions} parameter is required. The {@link CreateOctKeyOptions#getExpiresOn() expires}
     * and {@link CreateOctKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateOctKeyOptions#isEnabled() enabled} field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link CreateOctKeyOptions#getKeyType() keyType} indicates the type of key to create.
     * Possible values include: {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new symmetric key. The key activates in one day and expires in one year. Prints out the details of
     * the newly created key.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyClient.createOctKey#CreateOctKeyOptions}
     *
     * @param createOctKeyOptions The key options object containing information about the ec key being created.
     *
     * @return The {@link KeyVaultKey created key}.
     *
     * @throws NullPointerException If {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code ecKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createOctKey(CreateOctKeyOptions createOctKeyOptions) {
        return createOctKeyWithResponse(createOctKeyOptions, Context.NONE).getValue();
    }

    /**
     * Creates and stores a new symmetric key in Key Vault. If the named key already exists, Azure Key Vault creates a
     * new version of the key. This operation requires the keys/create permission.
     *
     * <p>The {@link CreateOctKeyOptions} parameter is required. The {@link CreateOctKeyOptions#getExpiresOn() expires}
     * and {@link CreateOctKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateOctKeyOptions#isEnabled() enabled} field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link CreateOctKeyOptions#getKeyType() keyType} indicates the type of key to create.
     * Possible values include: {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new symmetric key. The key activates in one day and expires in one year. Prints out the details of
     * the
     * created key.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyClient.createOctKey#CreateOctKeyOptions-Context}
     *
     * @param createOctKeyOptions The key options object containing information about the ec key being created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey created key}.
     *
     * @throws NullPointerException If {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code ecKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createOctKeyWithResponse(CreateOctKeyOptions createOctKeyOptions, Context context) {
        return client.createOctKeyWithResponse(createOctKeyOptions, context).block();
    }

    /**
     * Imports an externally created key and stores it in key vault. The import key operation may be used to import any
     * key type into the Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the
     * key. This operation requires the {@code keys/import} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new key into key vault. Prints out the details of the imported key.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.keyclient.importKey#string-jsonwebkey}
     *
     * @param name The name for the imported key.
     * @param keyMaterial The Json web key being imported.
     * @return The {@link KeyVaultKey imported key}.
     * @throws HttpResponseException if {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey importKey(String name, JsonWebKey keyMaterial) {
        return importKeyWithResponse(new ImportKeyOptions(name, keyMaterial), Context.NONE).getValue();
    }

    /**
     * Imports an externally created key and stores it in key vault. The import key operation may be used to import any
     * key type into the Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the
     * key. This operation requires the {@code keys/import} permission.
     *
     * <p>The {@code keyImportOptions} is required and its fields {@link ImportKeyOptions#getName() name} and {@link
     * ImportKeyOptions#getKey() key material} cannot be null. The {@link ImportKeyOptions#getExpiresOn() expires} and
     * {@link ImportKeyOptions#getNotBefore() notBefore} values in {@code keyImportOptions} are optional. If not specified,
     * no values are set for the fields. The {@link ImportKeyOptions#isEnabled() enabled} field is set to true and the
     * {@link ImportKeyOptions#isHardwareProtected() hsm} field is set to false by Azure Key Vault, if they are not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new key into key vault. Prints out the details of the imported key.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.keyclient.importKey#options}
     *
     * @param importKeyOptions The key import configuration object containing information about the json web key
     *     being imported.
     * @return The {@link KeyVaultKey imported key}.
     * @throws NullPointerException if {@code keyImportOptions} is {@code null}.
     * @throws HttpResponseException if {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey importKey(ImportKeyOptions importKeyOptions) {
        return importKeyWithResponse(importKeyOptions, Context.NONE).getValue();
    }

    /**
     * Imports an externally created key and stores it in key vault. The import key operation may be used to import any
     * key type into the Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the
     * key. This operation requires the {@code keys/import} permission.
     *
     * <p>The {@code keyImportOptions} is required and its fields {@link ImportKeyOptions#getName() name} and {@link
     * ImportKeyOptions#getKey() key material} cannot be null. The {@link ImportKeyOptions#getExpiresOn() expires} and
     * {@link ImportKeyOptions#getNotBefore() notBefore} values in {@code keyImportOptions} are optional. If not specified,
     * no values are set for the fields. The {@link ImportKeyOptions#isEnabled() enabled} field is set to true and the
     * {@link ImportKeyOptions#isHardwareProtected() hsm} field is set to false by Azure Key Vault, if they are not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new key into key vault. Prints out the details of the imported key.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.keyclient.importKeyWithResponse#options-response}
     *
     * @param importKeyOptions The key import configuration object containing information about the json web key
     *     being imported.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey imported key}.
     * @throws NullPointerException if {@code keyImportOptions} is {@code null}.
     * @throws HttpResponseException if {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> importKeyWithResponse(ImportKeyOptions importKeyOptions, Context context) {
        return client.importKeyWithResponse(importKeyOptions, context).block();
    }

    /**
     * Gets the public part of the specified key and key version. The get key operation is applicable to all key types
     * and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the key in the key vault. Prints out the details of the returned key.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.getKey#string-string}
     *
     * @param name The name of the key, cannot be null
     * @param version The version of the key to retrieve. If this is an empty String or null, this call is
     *     equivalent to calling {@link KeyClient#getKey(String)}, with the latest version being retrieved.
     * @return The requested {@link KeyVaultKey key}. The content of the key is null if
     * both {@code name} and {@code version} are null or empty.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault or
     * an empty/null {@code name} and a non null/empty {@code version} is provided.
     * @throws HttpResponseException if a valid {@code name} and a non null/empty {@code version} is specified.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey getKey(String name, String version) {
        return getKeyWithResponse(name, version, Context.NONE).getValue();
    }

    /**
     * Gets the public part of the specified key and key version. The get key operation is applicable to all key types
     * and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the key in the key vault. Prints out the details of the returned key.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.getKeyWithResponse#string-string-Context}
     *
     * @param name The name of the key, cannot be null
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @param version The version of the key to retrieve. If this is an empty String or null, this call is
     *     equivalent to calling {@link KeyClient#getKey(String)}, with the latest version being retrieved.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested {@link KeyVaultKey key}.
     * The content of the key is null if both {@code name} and {@code version} are null or empty.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault or
     * an empty/null {@code name} and a non null/empty {@code version} is provided.
     * @throws HttpResponseException if a valid {@code name} and a non null/empty {@code version} is specified.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> getKeyWithResponse(String name, String version, Context context) {
        return client.getKeyWithResponse(name, version, context).block();
    }

    /**
     * Get the public part of the latest version of the specified key from the key vault. The get key operation is
     * applicable to all key types and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the latest version of the key in the key vault. Prints out the details of the returned key.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.getKey#string}
     *
     * @param name The name of the key.
     * @return The requested {@link KeyVaultKey key}. The content of the key is null if {@code name} is null or empty.
     * @throws ResourceNotFoundException when a key with non null/empty {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException if a non null/empty and an invalid {@code name} is specified.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey getKey(String name) {
        return getKeyWithResponse(name, "", Context.NONE).getValue();
    }

    /**
     * Updates the attributes and key operations associated with the specified key, but not the cryptographic key
     * material of the specified key in the key vault. The update operation changes specified attributes of an existing
     * stored key and attributes that are not specified in the request are left unchanged. The cryptographic key
     * material of a key itself cannot be changed. This operation requires the {@code keys/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the latest version of the key, changes its expiry time and key operations and the updates the key in the
     * key vault.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.updateKeyProperties#KeyProperties-keyOperations}
     *
     * @param keyProperties The {@link KeyProperties key properties} object with updated properties.
     * @param keyOperations The updated key operations to associate with the key.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey updated key}.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @throws ResourceNotFoundException when a key with {@link KeyProperties#getName() name} and {@link KeyProperties#getVersion()
     *     version} doesn't exist in the key vault.
     * @throws HttpResponseException if {@link KeyProperties#getName() name} or {@link KeyProperties#getVersion() version} is empty
     *     string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey updateKeyProperties(KeyProperties keyProperties, KeyOperation... keyOperations) {
        return updateKeyPropertiesWithResponse(keyProperties, Context.NONE, keyOperations).getValue();
    }

    /**
     * Updates the attributes and key operations associated with the specified key, but not the cryptographic key
     * material of the specified key in the key vault. The update operation changes specified attributes of an existing
     * stored key and attributes that are not specified in the request are left unchanged. The cryptographic key
     * material of a key itself cannot be changed. This operation requires the {@code keys/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the latest version of the key, changes its expiry time and key operations and the updates the key in the
     * key vault.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.updateKeyPropertiesWithResponse#KeyProperties-keyOperations-Context}
     *
     * @param keyProperties The {@link KeyProperties key properties} object with updated properties.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @param keyOperations The updated key operations to associate with the key.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey updated key}.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @throws ResourceNotFoundException when a key with {@link KeyProperties#getName() name} and {@link KeyProperties#getVersion()
     *     version} doesn't exist in the key vault.
     * @throws HttpResponseException if {@link KeyProperties#getName() name} or {@link KeyProperties#getVersion() version} is empty
     *     string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> updateKeyPropertiesWithResponse(KeyProperties keyProperties, Context context, KeyOperation... keyOperations) {
        return client.updateKeyPropertiesWithResponse(keyProperties, context, keyOperations).block();
    }

    /**
     * Deletes a key of any type from the key vault. If soft-delete is enabled on the key vault then the key is placed
     * in the deleted state and requires to be purged for permanent deletion else the key is permanently deleted. The
     * delete operation applies to any key stored in Azure Key Vault but it cannot be applied to an individual version
     * of a key. This operation removes the cryptographic material associated with the key, which means the key is not
     * usable for Sign/Verify, Wrap/Unwrap or Encrypt/Decrypt operations. This operation requires the {@code
     * keys/delete} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the key from the keyvault. Prints out the recovery id of the deleted key returned in the
     * response.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.deleteKey#String}
     *
     * @param name The name of the key to be deleted.
     * @return A {@link SyncPoller} to poll on and retrieve {@link DeletedKey deleted key}
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DeletedKey, Void> beginDeleteKey(String name) {
        return client.beginDeleteKey(name).getSyncPoller();
    }

    /**
     * Gets the public part of a deleted key. The Get Deleted Key operation is applicable for soft-delete enabled
     * vaults. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the deleted key from the key vault enabled for soft-delete. Prints out the details of the deleted key
     * returned in the response.</p>
     * //Assuming key is deleted on a soft-delete enabled key vault.
     * {@codesnippet com.azure.keyvault.keys.keyclient.getDeletedKey#string}
     *
     * @param name The name of the deleted key.
     * @return The {@link DeletedKey deleted key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeletedKey getDeletedKey(String name) {
        return getDeletedKeyWithResponse(name, Context.NONE).getValue();
    }

    /**
     * Gets the public part of a deleted key. The Get Deleted Key operation is applicable for soft-delete enabled
     * vaults. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the deleted key from the key vault enabled for soft-delete. Prints out the details of the deleted key
     * returned in the response.</p>
     * //Assuming key is deleted on a soft-delete enabled key vault.
     * {@codesnippet com.azure.keyvault.keys.keyclient.getDeletedKeyWithResponse#string-Context}
     *
     * @param name The name of the deleted key.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DeletedKey deleted key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeletedKey> getDeletedKeyWithResponse(String name, Context context) {
        return client.getDeletedKeyWithResponse(name, context).block();
    }

    /**
     * Permanently deletes the specified key without the possibility of recovery. The Purge Deleted Key operation is
     * applicable for soft-delete enabled vaults. This operation requires the {@code keys/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted key from the key vault enabled for soft-delete. Prints out the status code from the server
     * response.</p>
     * //Assuming key is deleted on a soft-delete enabled key vault.
     * {@codesnippet com.azure.keyvault.keys.keyclient.purgeDeletedKey#string}
     *
     * @param name The name of the deleted key.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void purgeDeletedKey(String name) {
        purgeDeletedKeyWithResponse(name, Context.NONE);
    }

    /**
     * Permanently deletes the specified key without the possibility of recovery. The Purge Deleted Key operation is
     * applicable for soft-delete enabled vaults. This operation requires the {@code keys/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted key from the key vault enabled for soft-delete. Prints out the status code from the server
     * response.</p>
     * //Assuming key is deleted on a soft-delete enabled key vault.
     * {@codesnippet com.azure.keyvault.keys.keyclient.purgeDeletedKeyWithResponse#string-Context}
     *
     * @param name The name of the deleted key.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> purgeDeletedKeyWithResponse(String name, Context context) {
        return client.purgeDeletedKeyWithResponse(name, context).block();
    }

    /**
     * Recovers the deleted key in the key vault to its latest version and can only be performed on a soft-delete
     * enabled vault. An attempt to recover an non-deleted key will return an error. Consider this the inverse of the
     * delete operation on soft-delete enabled vaults. This operation requires the {@code keys/recover} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted key from the key vault enabled for soft-delete.</p>
     * //Assuming key is deleted on a soft-delete enabled key vault.
     * {@codesnippet com.azure.keyvault.keys.keyclient.recoverDeletedKey#String}
     *
     * @param name The name of the deleted key to be recovered.
     * @return A {@link SyncPoller} to poll on and retrieve {@link KeyVaultKey recovered key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultKey, Void> beginRecoverDeletedKey(String name) {
        return client.beginRecoverDeletedKey(name).getSyncPoller();
    }

    /**
     * Requests a backup of the specified key be downloaded to the client. The Key Backup operation exports a key from
     * Azure Key Vault in a protected form. Note that this operation does not return key material in a form that can be
     * used outside the Azure Key Vault system, the returned key material is either protected to a Azure Key Vault HSM
     * or to Azure Key Vault itself. The intent of this operation is to allow a client to generate a key in one Azure
     * Key Vault instance, backup the key, and then restore it into another Azure Key Vault instance. The backup
     * operation may be used to export, in protected form, any key type from Azure Key Vault. Individual versions of a
     * key cannot be backed up. Backup / Restore can be performed within geographical boundaries only; meaning that a
     * backup from one geographical area cannot be restored to another geographical area. For example, a backup from the
     * US geographical area cannot be restored in an EU geographical area. This operation requires the {@code
     * key/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the key from the key vault and prints out the length of the key's backup byte array returned in the
     * response</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.backupKey#string}
     *
     * @param name The name of the key.
     * @return The backed up key blob.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] backupKey(String name) {
        return backupKeyWithResponse(name, Context.NONE).getValue();
    }

    /**
     * Requests a backup of the specified key be downloaded to the client. The Key Backup operation exports a key from
     * Azure Key Vault in a protected form. Note that this operation does not return key material in a form that can be
     * used outside the Azure Key Vault system, the returned key material is either protected to a Azure Key Vault HSM
     * or to Azure Key Vault itself. The intent of this operation is to allow a client to generate a key in one Azure
     * Key Vault instance, backup the key, and then restore it into another Azure Key Vault instance. The backup
     * operation may be used to export, in protected form, any key type from Azure Key Vault. Individual versions of a
     * key cannot be backed up. Backup / Restore can be performed within geographical boundaries only; meaning that a
     * backup from one geographical area cannot be restored to another geographical area. For example, a backup from the
     * US geographical area cannot be restored in an EU geographical area. This operation requires the {@code
     * key/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the key from the key vault and prints out the length of the key's backup byte array returned in the
     * response</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.backupKeyWithResponse#string-Context}
     *
     * @param name The name of the key.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the backed up key blob.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<byte[]> backupKeyWithResponse(String name, Context context) {
        return client.backupKeyWithResponse(name, context).block();
    }

    /**
     * Restores a backed up key to a vault. Imports a previously backed up key into Azure Key Vault, restoring the key,
     * its key identifier, attributes and access control policies. The restore operation may be used to import a
     * previously backed up key. Individual versions of a key cannot be restored. The key is restored in its entirety
     * with the same key name as it had when it was backed up. If the key name is not available in the target Key Vault,
     * the restore operation will be rejected. While the key name is retained during restore, the final key identifier
     * will change if the key is restored to a different vault. Restore will restore all versions and preserve version
     * identifiers. The restore operation is subject to security constraints: The target Key Vault must be owned by the
     * same Microsoft Azure Subscription as the source Key Vault The user must have restore permission in the target Key
     * Vault. This operation requires the {@code keys/restore} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the key in the key vault from its backup. Prints out the details of the restored key returned in the
     * response.</p>
     * //Pass the Key Backup Byte array to the restore operation.
     * {@codesnippet com.azure.keyvault.keys.keyclient.restoreKeyBackup#byte}
     *
     * @param backup The backup blob associated with the key.
     * @return The {@link KeyVaultKey restored key}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey restoreKeyBackup(byte[] backup) {
        return restoreKeyBackupWithResponse(backup, Context.NONE).getValue();
    }

    /**
     * Restores a backed up key to a vault. Imports a previously backed up key into Azure Key Vault, restoring the key,
     * its key identifier, attributes and access control policies. The restore operation may be used to import a
     * previously backed up key. Individual versions of a key cannot be restored. The key is restored in its entirety
     * with the same key name as it had when it was backed up. If the key name is not available in the target Key Vault,
     * the restore operation will be rejected. While the key name is retained during restore, the final key identifier
     * will change if the key is restored to a different vault. Restore will restore all versions and preserve version
     * identifiers. The restore operation is subject to security constraints: The target Key Vault must be owned by the
     * same Microsoft Azure Subscription as the source Key Vault The user must have restore permission in the target Key
     * Vault. This operation requires the {@code keys/restore} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the key in the key vault from its backup. Prints out the details of the restored key returned in the
     * response.</p>
     * //Pass the Key Backup Byte array to the restore operation.
     * {@codesnippet com.azure.keyvault.keys.keyclient.restoreKeyBackupWithResponse#byte-Context}
     *
     * @param backup The backup blob associated with the key.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey restored key}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> restoreKeyBackupWithResponse(byte[] backup, Context context) {
        return client.restoreKeyBackupWithResponse(backup, context).block();
    }

    /**
     * List keys in the key vault. Retrieves a list of the keys in the Key Vault as JSON Web Key structures that contain
     * the public part of a stored key. The List operation is applicable to all key types and the individual key
     * response in the list is represented by {@link KeyProperties} as only the key identifier, attributes and tags are
     * provided in the response. The key material and individual key versions are not listed in the response. This
     * operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get full keys with key material from this information. Loop over the {@link KeyProperties key}
     * and call {@link KeyClient#getKey(String, String)}. This will return the {@link KeyVaultKey key} with key material
     * included of its latest version.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeys}
     *
     * <p><strong>Code Samples to iterate keys by page</strong></p>
     * <p>It is possible to get full keys with key material from this information. Iterate over all the {@link KeyProperties
     * key} by page and call {@link KeyClient#getKey(String, String)}. This will return the {@link KeyVaultKey key} with key
     * material included of its latest version.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeys.iterableByPage}
     *
     * @return {@link PagedIterable} of {@link KeyProperties key} of all the keys in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeys() {
        return listPropertiesOfKeys(Context.NONE);
    }

    /**
     * List keys in the key vault. Retrieves a list of the keys in the Key Vault as JSON Web Key structures that contain
     * the public part of a stored key. The List operation is applicable to all key types and the individual key
     * response in the list is represented by {@link KeyProperties} as only the key identifier, attributes and tags are
     * provided in the response. The key material and individual key versions are not listed in the response. This
     * operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get full keys with key material from this information. Loop over the {@link KeyProperties key}
     * and call {@link KeyClient#getKey(String, String)}. This will return the {@link KeyVaultKey key} with key material
     * included of its latest version.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeys#Context}
     *
     * <p><strong>Code Samples to iterate keys by page</strong></p>
     * <p>It is possible to get full keys with key material from this information. Iterate over all the {@link KeyProperties
     * key} by page and call {@link KeyClient#getKey(String, String)}. This will return the {@link KeyVaultKey key} with key
     * material included of its latest version.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeys.iterableByPage}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link PagedIterable} of {@link KeyProperties key} of all the keys in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeys(Context context) {
        return new PagedIterable<>(client.listPropertiesOfKeys(context));
    }

    /**
     * Lists {@link DeletedKey deleted keys} of the key vault. The deleted keys are retrieved as JSON Web Key structures
     * that contain the public part of a deleted key. The Get Deleted Keys operation is applicable for vaults enabled
     * for soft-delete. This operation requires the {@code keys/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted keys in the key vault and for each deleted key prints out its recovery id.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listDeletedKeys}
     *
     * <p><strong>Code Samples to iterate over deleted keys by page</strong></p>
     * <p>Iterate over the lists the deleted keys by each page in the key vault and for each deleted key prints out its
     * recovery id.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listDeletedKeys.iterableByPage}
     *
     * @return {@link PagedIterable} of all of the {@link DeletedKey deleted keys} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedKey> listDeletedKeys() {
        return listDeletedKeys(Context.NONE);
    }

    /**
     * Lists {@link DeletedKey deleted keys} of the key vault. The deleted keys are retrieved as JSON Web Key structures
     * that contain the public part of a deleted key. The Get Deleted Keys operation is applicable for vaults enabled
     * for soft-delete. This operation requires the {@code keys/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted keys in the key vault and for each deleted key prints out its recovery id.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listDeletedKeys#Context}
     *
     * <p><strong>Code Samples to iterate over deleted keys by page</strong></p>
     * <p>Iterate over the lists the deleted keys by each page in the key vault and for each deleted key prints out its
     * recovery id.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listDeletedKeys.iterableByPage}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link PagedIterable} of all of the {@link DeletedKey deleted keys} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedKey> listDeletedKeys(Context context) {
        return new PagedIterable<>(client.listDeletedKeys(context));
    }

    /**
     * List all versions of the specified key. The individual key response in the flux is represented by {@link KeyProperties}
     * as only the key identifier, attributes and tags are provided in the response. The key material values are
     * not provided in the response. This operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get full keys with key material for each version from this information. Loop over the
     * {@link KeyProperties key} and call {@link KeyClient#getKey(String, String)}. This will return the {@link KeyVaultKey keys}
     * with key material included of the specified versions.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeyVersions}
     *
     * <p><strong>Code Samples to iterate over key versions by page</strong></p>
     * <p>It is possible to get full keys with key material for each version from this information. Iterate over all
     * the {@link KeyProperties key} by page and call {@link KeyClient#getKey(String, String)}. This will return the {@link
     * KeyVaultKey keys} with key material included of the specified versions.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeyVersions.iterableByPage}
     *
     * @param name The name of the key.
     * @return {@link PagedIterable} of {@link KeyProperties key} of all the versions of the specified key in the vault. List
     *     is empty if key with {@code name} does not exist in key vault.
     * @throws ResourceNotFoundException when a given key {@code name} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeyVersions(String name) {
        return listPropertiesOfKeyVersions(name, Context.NONE);
    }

    /**
     * List all versions of the specified key. The individual key response in the flux is represented by {@link KeyProperties}
     * as only the key identifier, attributes and tags are provided in the response. The key material values are
     * not provided in the response. This operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get full keys with key material for each version from this information. Loop over the
     * {@link KeyProperties key} and call {@link KeyClient#getKey(String, String)}. This will return the {@link KeyVaultKey keys}
     * with key material included of the specified versions.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeyVersions}
     *
     * <p><strong>Code Samples to iterate over key versions by page</strong></p>
     * <p>It is possible to get full keys with key material for each version from this information. Iterate over all
     * the {@link KeyProperties key} by page and call {@link KeyClient#getKey(String, String)}. This will return the
     * {@link KeyVaultKey keys} with key material included of the specified versions.</p>
     *
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeyVersions.iterableByPage}
     *
     * @param name The name of the key.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link PagedIterable} of {@link KeyProperties key} of all the versions of the specified key in the vault. List
     *     is empty if key with {@code name} does not exist in key vault.
     * @throws ResourceNotFoundException when a given key {@code name} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeyVersions(String name, Context context) {
        return new PagedIterable<>(client.listPropertiesOfKeyVersions(name, context));
    }

    /**
     * Get the requested number of bytes containing random values from a managed HSM.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a number of bytes containing random values from a Managed HSM. Prints out the retrieved bytes in
     * base64Url format.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.getRandomBytes#int}
     *
     * @param count The requested number of random bytes.
     *
     * @return The requested number of bytes containing random values from a managed HSM.
     */
    public RandomBytes getRandomBytes(int count) {
        return client.getRandomBytes(count).block();
    }

    /**
     * Get the requested number of bytes containing random values from a managed HSM.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a number of bytes containing random values from a Managed HSM. Prints out the
     * {@link Response HTTP Response} details and the retrieved bytes in base64Url format.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.getRandomBytesWithResponse#int-Context}
     *
     * @param count The requested number of random bytes.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The {@link Response HTTP response} for this operation and the requested number of bytes containing
     * random values from a managed HSM.
     */
    public Response<RandomBytes> getRandomBytesWithResponse(int count, Context context) {
        return client.getRandomBytesWithResponse(count, context).block();
    }

    /**
     * Release the latest version of a key.
     *
     * <p>The key must be exportable. This operation requires the 'keys/release' permission.</p>
     *
     * @param name The name of the key to release.
     * @param target The attestation assertion for the target of the key release.
     *
     * @return The key release result containing the released key.
     *
     * @throws IllegalArgumentException If {@code name} or {@code target} are {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ReleaseKeyResult releaseKey(String name, String target) {
        return client.releaseKey(name, target).block();
    }

    /**
     * Releases a key.
     *
     * <p>The key must be exportable. This operation requires the 'keys/release' permission.</p>
     *
     * @param name The name of the key to release.
     * @param version The version of the key to retrieve. If this is empty or {@code null}, this call is equivalent to
     * calling {@link KeyAsyncClient#releaseKey(String, String)}, with the latest key version being released.
     * @param target The attestation assertion for the target of the key release.
     *
     * @return The key release result containing the released key.
     *
     * @throws IllegalArgumentException If {@code name} or {@code target} are {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ReleaseKeyResult releaseKey(String name, String version, String target) {
        return client.releaseKey(name, version, target).block();
    }

    /**
     * Releases a key.
     *
     * <p>The key must be exportable. This operation requires the 'keys/release' permission.</p>
     *
     * @param name The name of the key to release.
     * @param version Version of the key to release.This parameter is optional.
     * @param target The attestation assertion for the target of the key release.
     * @param options Additional options for releasing a key.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The {@link Response HTTP response} for this operation and the {@link ReleaseKeyResult} containing the
     * released key.
     *
     * @throws IllegalArgumentException If {@code name} or {@code target} are {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ReleaseKeyResult> releaseKeyWithResponse(String name, String version, String target,
                                                             ReleaseKeyOptions options, Context context) {
        return client.releaseKeyWithResponse(name, version, target, options, context).block();
    }
}
