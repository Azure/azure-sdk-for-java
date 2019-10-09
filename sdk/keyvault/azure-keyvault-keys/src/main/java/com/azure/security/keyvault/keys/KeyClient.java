// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.exception.HttpRequestException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.EcKeyCreateOptions;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyCreateOptions;
import com.azure.security.keyvault.keys.models.KeyImportOptions;
import com.azure.security.keyvault.keys.models.RsaKeyCreateOptions;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;
import com.azure.security.keyvault.keys.models.webkey.KeyCurveName;
import com.azure.security.keyvault.keys.models.webkey.KeyOperation;
import com.azure.security.keyvault.keys.models.webkey.KeyType;

import java.util.List;
import java.util.Objects;

/**
 * The KeyClient provides synchronous methods to manage {@link Key keys} in the Azure Key Vault. The client supports
 * creating, retrieving, updating, deleting, purging, backing up, restoring and listing the {@link Key keys}. The client
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
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link KeyType keyType} indicates the type of key to create. Possible values include: {@link KeyType#EC
     * EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM} and {@link
     * KeyType#OCT OCT}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key. Prints out the details of the created key.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.createKey#string-keyType}
     *
     * @param name The name of the key being created.
     * @param keyType The type of key to create. For valid values, see {@link KeyType KeyType}.
     * @return The {@link Key created key}.
     * @throws ResourceModifiedException if {@code name} or {@code keyType} is null.
     * @throws HttpRequestException if {@code name} is empty string.
     */
    public Key createKey(String name, KeyType keyType) {
        return createKeyWithResponse(new KeyCreateOptions(name, keyType), Context.NONE).getValue();
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link KeyCreateOptions} is required. The {@link KeyCreateOptions#getExpires() expires} and {@link
     * KeyCreateOptions#getNotBefore() notBefore} values are optional. The {@link KeyCreateOptions#isEnabled()} enabled} field
     * is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link KeyCreateOptions#getKeyType() keyType} indicates the type of key to create. Possible values include:
     * {@link KeyType#EC EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM}
     * and {@link KeyType#OCT OCT}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new RSA key which activates in one day and expires in one year. Prints out the details of the
     * created key.</p> {@codesnippet com.azure.keyvault.keys.keyclient.createKey#keyOptions}
     *
     * @param keyCreateOptions The key options object containing information about the key being created.
     * @return The {@link Key created key}.
     * @throws NullPointerException if {@code keyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code keyCreateOptions} is malformed.
     */
    public Key createKey(KeyCreateOptions keyCreateOptions) {
        return createKeyWithResponse(keyCreateOptions, Context.NONE).getValue();
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link KeyCreateOptions} is required. The {@link KeyCreateOptions#getExpires() expires} and {@link
     * KeyCreateOptions#getNotBefore() notBefore} values are optional. The {@link KeyCreateOptions#isEnabled() enabled} field
     * is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link KeyCreateOptions#getKeyType() keyType} indicates the type of key to create. Possible values include:
     * {@link KeyType#EC EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM}
     * and {@link KeyType#OCT OCT}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new RSA key which activates in one day and expires in one year. Prints out the details of the
     * created key.</p> {@codesnippet com.azure.keyvault.keys.keyclient.createKeyWithResponse#keyCreateOptions-Context}
     *
     * @param keyCreateOptions The key options object containing information about the key being created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link Key created key}.
     * @throws NullPointerException if {@code keyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code keyCreateOptions} is malformed.
     */
    public Response<Key> createKeyWithResponse(KeyCreateOptions keyCreateOptions, Context context) {
        return client.createKeyWithResponse(keyCreateOptions, context).block();
    }

    /**
     * Creates a new Rsa key and stores it in the key vault. The create Rsa key operation can be used to create any Rsa
     * key type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It
     * requires the {@code keys/create} permission.
     *
     * <p>The {@link RsaKeyCreateOptions} is required. The {@link RsaKeyCreateOptions#getKeySize() keySize} can be
     * optionally specified. The {@link RsaKeyCreateOptions#getExpires() expires} and {@link
     * RsaKeyCreateOptions#getNotBefore() notBefore} values are optional. The {@link RsaKeyCreateOptions#isEnabled() enabled}
     * field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link RsaKeyCreateOptions#getKeyType() keyType} indicates the type of key to create. Possible values
     * include: {@link KeyType#RSA RSA} and {@link KeyType#RSA_HSM RSA-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new RSA key with size 2048 which activates in one day and expires in one year. Prints out the
     * details of the created key.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.createRsaKey#keyOptions}
     *
     * @param rsaKeyCreateOptions The key options object containing information about the rsa key being created.
     * @return The {@link Key created key}.
     * @throws NullPointerException if {@code rsaKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code rsaKeyCreateOptions} is malformed.
     * @throws HttpRequestException if {@code name} is empty string.
     */
    public Key createRsaKey(RsaKeyCreateOptions rsaKeyCreateOptions) {
        return createRsaKeyWithResponse(rsaKeyCreateOptions, Context.NONE).getValue();
    }

    /**
     * Creates a new Rsa key and stores it in the key vault. The create Rsa key operation can be used to create any Rsa
     * key type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It
     * requires the {@code keys/create} permission.
     *
     * <p>The {@link RsaKeyCreateOptions} is required. The {@link RsaKeyCreateOptions#getKeySize() keySize} can be
     * optionally specified. The {@link RsaKeyCreateOptions#getExpires() expires} and {@link
     * RsaKeyCreateOptions#getNotBefore() notBefore} values are optional. The {@link RsaKeyCreateOptions#isEnabled() enabled}
     * field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link RsaKeyCreateOptions#getKeyType() keyType} indicates the type of key to create. Possible values
     * include: {@link KeyType#RSA RSA} and {@link KeyType#RSA_HSM RSA-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new RSA key with size 2048 which activates in one day and expires in one year. Prints out the
     * details of the created key.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.createRsaKeyWithResponse#keyOptions-Context}
     *
     * @param rsaKeyCreateOptions The key options object containing information about the rsa key being created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link Key created key}.
     * @throws NullPointerException if {@code rsaKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code rsaKeyCreateOptions} is malformed.
     */
    public Response<Key> createRsaKeyWithResponse(RsaKeyCreateOptions rsaKeyCreateOptions, Context context) {
        return client.createRsaKeyWithResponse(rsaKeyCreateOptions, context).block();
    }

    /**
     * Creates a new Ec key and  stores it in the key vault. The create Ec key operation can be used to create any Ec
     * key type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It
     * requires the {@code keys/create} permission.
     *
     * <p>The {@link EcKeyCreateOptions} parameter is required. The {@link EcKeyCreateOptions#getCurve() key curve} can be
     * optionally specified. If not specified, default value of {@link KeyCurveName#P_256 P-256} is used by Azure Key
     * Vault. The {@link EcKeyCreateOptions#getExpires() expires} and {@link EcKeyCreateOptions#getNotBefore() notBefore}
     * values are optional. The {@link EcKeyCreateOptions#isEnabled() enabled} field is set to true by Azure Key Vault, if
     * not specified.</p>
     *
     * <p>The {@link EcKeyCreateOptions#getKeyType() keyType} indicates the type of key to create. Possible values
     * include: {@link KeyType#EC EC} and {@link KeyType#EC_HSM EC-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key with P-384 web key curve. The key activates in one day and expires in one year. Prints
     * out the details of the created key.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.createEcKey#keyOptions}
     *
     * @param ecKeyCreateOptions The key options object containing information about the ec key being created.
     * @return The {@link Key created key}.
     * @throws NullPointerException if {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code ecKeyCreateOptions} is malformed.
     */
    public Key createEcKey(EcKeyCreateOptions ecKeyCreateOptions) {
        return createEcKeyWithResponse(ecKeyCreateOptions, Context.NONE).getValue();
    }

    /**
     * Creates a new Ec key and  stores it in the key vault. The create Ec key operation can be used to create any Ec
     * key type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It
     * requires the {@code keys/create} permission.
     *
     * <p>The {@link EcKeyCreateOptions} parameter is required. The {@link EcKeyCreateOptions#getCurve() key curve} can be
     * optionally specified. If not specified, default value of {@link KeyCurveName#P_256 P-256} is used by Azure Key
     * Vault. The {@link EcKeyCreateOptions#getExpires() expires} and {@link EcKeyCreateOptions#getNotBefore() notBefore}
     * values are optional. The {@link EcKeyCreateOptions#isEnabled()} enabled} field is set to true by Azure Key Vault, if
     * not specified.</p>
     *
     * <p>The {@link EcKeyCreateOptions#getKeyType() keyType} indicates the type of key to create. Possible values
     * include:
     * {@link KeyType#EC EC} and {@link KeyType#EC_HSM EC-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key with P-384 web key curve. The key activates in one day and expires in one year. Prints
     * out the details of the created key.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.createEcKeyWithResponse#keyOptions-Context}
     *
     * @param ecKeyCreateOptions The key options object containing information about the ec key being created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link Key created key}.
     * @throws NullPointerException if {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code ecKeyCreateOptions} is malformed.
     */
    public Response<Key> createEcKeyWithResponse(EcKeyCreateOptions ecKeyCreateOptions, Context context) {
        return client.createEcKeyWithResponse(ecKeyCreateOptions, context).block();
    }

    /**
     * Imports an externally created key and stores it in key vault. The import key operation may be used to import any
     * key type into the Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the
     * key. This operation requires the {@code keys/import} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new key into key vault. Prints out the details of the imported key.</p>
     * <pre>
     * Key importedKey = keyClient.importKey("keyName", jsonWebKeyToImport);
     * System.out.printf("Key is imported with name %s and id %s \n", importedKey.getName(), importedKey.getId());
     * </pre>
     *
     * @param name The name for the imported key.
     * @param keyMaterial The Json web key being imported.
     * @return The {@link Key imported key}.
     * @throws HttpRequestException if {@code name} is empty string.
     */
    public Key importKey(String name, JsonWebKey keyMaterial) {
        return importKeyWithResponse(new KeyImportOptions(name, keyMaterial), Context.NONE).getValue();
    }

    /**
     * Imports an externally created key and stores it in key vault. The import key operation may be used to import any
     * key type into the Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the
     * key. This operation requires the {@code keys/import} permission.
     *
     * <p>The {@code keyImportOptions} is required and its fields {@link KeyImportOptions#getName() name} and {@link
     * KeyImportOptions#getKeyMaterial() key material} cannot be null. The {@link KeyImportOptions#getExpires() expires} and
     * {@link KeyImportOptions#getNotBefore() notBefore} values in {@code keyImportOptions} are optional. If not specified,
     * no values are set for the fields. The {@link KeyImportOptions#isEnabled() enabled} field is set to true and the
     * {@link KeyImportOptions#isHsm() hsm} field is set to false by Azure Key Vault, if they are not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new key into key vault. Prints out the details of the imported key.</p>
     * <pre>
     * KeyImportOptions keyImportOptions = new KeyImportOptions("keyName", jsonWebKeyToImport)
     *   .hsm(true)
     *   .setExpires(OffsetDateTime.now().plusDays(60));
     *
     * Key importedKey = keyClient.importKey(keyImportOptions);
     * System.out.printf("Key is imported with name %s and id %s \n", importedKey.getName(), importedKey.getId());
     * </pre>
     *
     * @param keyImportOptions The key import configuration object containing information about the json web key
     *     being imported.
     * @return The {@link Key imported key}.
     * @throws NullPointerException if {@code keyImportOptions} is {@code null}.
     * @throws HttpRequestException if {@code name} is empty string.
     */
    public Key importKey(KeyImportOptions keyImportOptions) {
        return importKeyWithResponse(keyImportOptions, Context.NONE).getValue();
    }

    /**
     * Imports an externally created key and stores it in key vault. The import key operation may be used to import any
     * key type into the Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the
     * key. This operation requires the {@code keys/import} permission.
     *
     * <p>The {@code keyImportOptions} is required and its fields {@link KeyImportOptions#getName() name} and {@link
     * KeyImportOptions#getKeyMaterial() key material} cannot be null. The {@link KeyImportOptions#getExpires() expires} and
     * {@link KeyImportOptions#getNotBefore() notBefore} values in {@code keyImportOptions} are optional. If not specified,
     * no values are set for the fields. The {@link KeyImportOptions#isEnabled() enabled} field is set to true and the
     * {@link KeyImportOptions#isHsm() hsm} field is set to false by Azure Key Vault, if they are not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new key into key vault. Prints out the details of the imported key.</p>
     * <pre>
     * KeyImportOptions keyImportOptions = new KeyImportOptions("keyName", jsonWebKeyToImport)
     *   .hsm(true)
     *   .setExpires(OffsetDateTime.now().plusDays(60));
     *
     * Key importedKey = keyClient.importKey(keyImportOptions, new Context(key1, value1)).value();
     * System.out.printf("Key is imported with name %s and id %s \n", importedKey.getName(), importedKey.getId());
     * </pre>
     *
     * @param keyImportOptions The key import configuration object containing information about the json web key
     *     being imported.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link Key imported key}.
     * @throws NullPointerException if {@code keyImportOptions} is {@code null}.
     * @throws HttpRequestException if {@code name} is empty string.
     */
    public Response<Key> importKeyWithResponse(KeyImportOptions keyImportOptions, Context context) {
        return client.importKeyWithResponse(keyImportOptions, context).block();
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
     * @return The requested {@link Key key}.
     * @throws ResourceNotFoundException when a key with {@code name} and {@code version} doesn't exist in the key
     *     vault.
     * @throws HttpRequestException if {@code name} or {@code version} is empty string.
     */
    public Key getKey(String name, String version) {
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
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested {@link Key key}.
     * @throws ResourceNotFoundException when a key with {@code name} and {@code version} doesn't exist in the key
     *     vault.
     * @throws HttpRequestException if {@code name} or {@code version} is empty string.
     */
    public Response<Key> getKeyWithResponse(String name, String version, Context context) {
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
     * @return The requested {@link Key key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     */
    public Key getKey(String name) {
        return getKeyWithResponse(name, "", Context.NONE).getValue();
    }

    /**
     * Get public part of the key which represents {@link KeyProperties keyProperties} from the key vault. The get key operation is
     * applicable to all key types and it requires the {@code keys/get} permission.
     *
     * <p>The list operations {@link KeyClient#listKeys()} and {@link KeyClient#listKeyVersions(String)} return
     * the {@link List} containing {@link KeyProperties key properties} as output excluding the key material of the key. This
     * operation can then be used to get the full key with its key material from {@code keyProperties}. </p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.getKey#KeyProperties}
     *
     * @param keyProperties The {@link KeyProperties key properties} holding attributes of the key being requested.
     * @return The requested {@link Key key}.
     * @throws ResourceNotFoundException when a key with {@link KeyProperties#getName() name} and {@link KeyProperties#getVersion()
     *     version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link KeyProperties#getName()}  name} or {@link KeyProperties#getVersion() version} is empty
     *     string.
     */
    public Key getKey(KeyProperties keyProperties) {
        return getKeyWithResponse(keyProperties, Context.NONE).getValue();
    }

    /**
     * Get public part of the key which represents {@link KeyProperties keyProperties} from the key vault. The get key operation is
     * applicable to all key types and it requires the {@code keys/get} permission.
     *
     * <p>The list operations {@link KeyClient#listKeys()} and {@link KeyClient#listKeyVersions(String)} return
     * the {@link List} containing {@link KeyProperties key properties} as output excluding the key material of the key. This
     * operation can then be used to get the full key with its key material from {@code keyProperties}. </p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.getKeyWithResponse#KeyProperties-Context}
     *
     * @param keyProperties The {@link KeyProperties key properties} holding attributes of the key being requested.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested {@link Key key}.
     * @throws ResourceNotFoundException when a key with {@link KeyProperties#getName() name} and {@link KeyProperties#getVersion()
     *     version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link KeyProperties#getName() name} or {@link KeyProperties#getVersion() version} is empty
     *     string.
     */
    public Response<Key> getKeyWithResponse(KeyProperties keyProperties, Context context) {
        Objects.requireNonNull(keyProperties, "The Key properties parameter cannot be null.");
        return client
            .getKeyWithResponse(keyProperties.getName(), keyProperties.getVersion() == null ? "" : keyProperties.getVersion(), context).block();
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
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link Key updated key}.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @throws ResourceNotFoundException when a key with {@link KeyProperties#getName() name} and {@link KeyProperties#getVersion()
     *     version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link KeyProperties#getName() name} or {@link KeyProperties#getVersion() version} is empty
     *     string.
     */
    public Key updateKeyProperties(KeyProperties keyProperties, KeyOperation... keyOperations) {
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
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link Key updated key}.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @throws ResourceNotFoundException when a key with {@link KeyProperties#getName() name} and {@link KeyProperties#getVersion()
     *     version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link KeyProperties#getName() name} or {@link KeyProperties#getVersion() version} is empty
     *     string.
     */
    public Response<Key> updateKeyPropertiesWithResponse(KeyProperties keyProperties, Context context, KeyOperation... keyOperations) {
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
     * {@codesnippet com.azure.keyvault.keys.keyclient.deleteKey#string}
     *
     * @param name The name of the key to be deleted.
     * @return The {@link DeletedKey deleted key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    public DeletedKey deleteKey(String name) {
        return deleteKeyWithResponse(name, Context.NONE).getValue();
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
     * {@codesnippet com.azure.keyvault.keys.keyclient.deleteKeyWithResponse#string-Context}
     *
     * @param name The name of the key to be deleted.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DeletedKey deleted key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    public Response<DeletedKey> deleteKeyWithResponse(String name, Context context) {
        return client.deleteKeyWithResponse(name, context).block();
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
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
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
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
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
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
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
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
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
     * {@codesnippet com.azure.keyvault.keys.keyclient.recoverDeletedKey#string}
     *
     * @param name The name of the deleted key to be recovered.
     * @return The {@link Key recovered key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    public Key recoverDeletedKey(String name) {
        return recoverDeletedKeyWithResponse(name, Context.NONE).getValue();
    }

    /**
     * Recovers the deleted key in the key vault to its latest version and can only be performed on a soft-delete
     * enabled vault. An attempt to recover an non-deleted key will return an error. Consider this the inverse of the
     * delete operation on soft-delete enabled vaults. This operation requires the {@code keys/recover} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted key from the key vault enabled for soft-delete.</p>
     * //Assuming key is deleted on a soft-delete enabled key vault.
     * {@codesnippet com.azure.keyvault.keys.keyclient.recoverDeletedKeyWithResponse#string-Context}
     *
     * @param name The name of the deleted key to be recovered.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link Key recovered key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    public Response<Key> recoverDeletedKeyWithResponse(String name, Context context) {
        return client.recoverDeletedKeyWithResponse(name, context).block();
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
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
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
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
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
     * {@codesnippet com.azure.keyvault.keys.keyclient.restoreKey#byte}
     *
     * @param backup The backup blob associated with the key.
     * @return The {@link Key restored key}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    public Key restoreKey(byte[] backup) {
        return restoreKeyWithResponse(backup, Context.NONE).getValue();
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
     * {@codesnippet com.azure.keyvault.keys.keyclient.restoreKeyWithResponse#byte-Context}
     *
     * @param backup The backup blob associated with the key.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link Key restored key}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    public Response<Key> restoreKeyWithResponse(byte[] backup, Context context) {
        return client.restoreKeyWithResponse(backup, context).block();
    }

    /**
     * List keys in the key vault. Retrieves a list of the keys in the Key Vault as JSON Web Key structures that contain
     * the public part of a stored key. The List operation is applicable to all key types and the individual key
     * response in the list is represented by {@link KeyProperties} as only the key identifier, attributes and tags are
     * provided in the response. The key material and individual key versions are not listed in the response. This
     * operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get full keys with key material from this information. Loop over the {@link KeyProperties key}
     * and call {@link KeyClient#getKey(KeyProperties key properties)}. This will return the {@link Key key} with key material
     * included of its latest version.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeys}
     *
     * <p><strong>Code Samples to iterate keys by page</strong></p>
     * <p>It is possible to get full keys with key material from this information. Iterate over all the {@link KeyProperties
     * key} by page and call {@link KeyClient#getKey(KeyProperties key properties)}. This will return the {@link Key key} with key
     * material included of its latest version.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeys.iterableByPage}
     *
     * @return {@link PagedIterable} of {@link KeyProperties key} of all the keys in the vault.
     */
    public PagedIterable<KeyProperties> listKeys() {
        return listKeys(Context.NONE);
    }

    /**
     * List keys in the key vault. Retrieves a list of the keys in the Key Vault as JSON Web Key structures that contain
     * the public part of a stored key. The List operation is applicable to all key types and the individual key
     * response in the list is represented by {@link KeyProperties} as only the key identifier, attributes and tags are
     * provided in the response. The key material and individual key versions are not listed in the response. This
     * operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get full keys with key material from this information. Loop over the {@link KeyProperties key}
     * and call {@link KeyClient#getKey(KeyProperties key properties)}. This will return the {@link Key key} with key material
     * included of its latest version.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeys#Context}
     *
     * <p><strong>Code Samples to iterate keys by page</strong></p>
     * <p>It is possible to get full keys with key material from this information. Iterate over all the {@link KeyProperties
     * key} by page and call {@link KeyClient#getKey(KeyProperties key properties)}. This will return the {@link Key key} with key
     * material included of its latest version.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeys.iterableByPage}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link PagedIterable} of {@link KeyProperties key} of all the keys in the vault.
     */
    public PagedIterable<KeyProperties> listKeys(Context context) {
        return new PagedIterable<>(client.listKeys(context));
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
    public PagedIterable<DeletedKey> listDeletedKeys(Context context) {
        return new PagedIterable<>(client.listDeletedKeys(context));
    }

    /**
     * List all versions of the specified key. The individual key response in the flux is represented by {@link KeyProperties}
     * as only the key identifier, attributes and tags are provided in the response. The key material values are
     * not provided in the response. This operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get full keys with key material for each version from this information. Loop over the
     * {@link KeyProperties key} and call {@link KeyClient#getKey(KeyProperties key properties)}. This will return the {@link Key keys}
     * with key material included of the specified versions.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeyVersions}
     *
     * <p><strong>Code Samples to iterate over key versions by page</strong></p>
     * <p>It is possible to get full keys with key material for each version from this information. Iterate over all
     * the {@link KeyProperties key} by page and call {@link KeyClient#getKey(KeyProperties key properties)}. This will return the {@link
     * Key keys} with key material included of the specified versions.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeyVersions.iterableByPage}
     *
     * @param name The name of the key.
     * @return {@link PagedIterable} of {@link KeyProperties key} of all the versions of the specified key in the vault. List
     *     is empty if key with {@code name} does not exist in key vault.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    public PagedIterable<KeyProperties> listKeyVersions(String name) {
        return listKeyVersions(name, Context.NONE);
    }

    /**
     * List all versions of the specified key. The individual key response in the flux is represented by {@link KeyProperties}
     * as only the key identifier, attributes and tags are provided in the response. The key material values are
     * not provided in the response. This operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get full keys with key material for each version from this information. Loop over the
     * {@link KeyProperties key} and call {@link KeyClient#getKey(KeyProperties key properties)}. This will return the {@link Key keys}
     * with key material included of the specified versions.</p>
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeyVersions}
     *
     * <p><strong>Code Samples to iterate over key versions by page</strong></p>
     * <p>It is possible to get full keys with key material for each version from this information. Iterate over all
     * the {@link KeyProperties key} by page and call {@link KeyClient#getKey(KeyProperties key properties)}. This will return the
     * {@link Key keys} with key material included of the specified versions.</p>
     *
     * {@codesnippet com.azure.keyvault.keys.keyclient.listKeyVersions.iterableByPage}
     *
     * @param name The name of the key.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link PagedIterable} of {@link KeyProperties key} of all the versions of the specified key in the vault. List
     *     is empty if key with {@code name} does not exist in key vault.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    public PagedIterable<KeyProperties> listKeyVersions(String name, Context context) {
        return new PagedIterable<>(client.listKeyVersions(name, context));
    }
}
