// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys;

import com.azure.core.ServiceClient;
import com.azure.core.exception.HttpRequestException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.RestProxy;
import com.azure.keyvault.keys.implementation.DeletedKeyPage;
import com.azure.keyvault.keys.implementation.KeyBasePage;
import com.azure.keyvault.keys.models.Key;
import com.azure.keyvault.keys.models.KeyCreateOptions;
import com.azure.keyvault.keys.models.DeletedKey;
import com.azure.keyvault.keys.models.EcKeyCreateOptions;
import com.azure.keyvault.keys.models.KeyBase;
import com.azure.keyvault.keys.models.KeyImportOptions;
import com.azure.keyvault.keys.models.RsaKeyCreateOptions;
import com.azure.keyvault.webkey.JsonWebKey;
import com.azure.keyvault.webkey.JsonWebKeyCurveName;
import com.azure.keyvault.webkey.JsonWebKeyType;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * The KeyClient provides synchronous methods to manage {@link Key keys} in the Azure Key Vault. The client
 * supports creating, retrieving, updating, deleting, purging, backing up, restoring and listing the {@link Key keys}. The client
 * also supports listing {@link DeletedKey deleted keys} for a soft-delete enabled Azure Key Vault.
 *
 * <p><strong>Samples to construct the client</strong></p>
 * <pre>
 * KeyClient.builder()
 *   .endpoint("https://myvault.vault.azure.net/")
 *   .credentials(keyVaultCredentials)
 *   .build()
 * </pre>
 *
 * @see KeyClientBuilder
 */
public final class KeyClient extends ServiceClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private String endpoint;
    private final KeyService service;

    /**
     * Creates a KeyClient that uses {@code pipeline} to service requests
     *
     * @param endpoint URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    KeyClient(URL endpoint, HttpPipeline pipeline) {
        super(pipeline);
        Objects.requireNonNull(endpoint, KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.endpoint = endpoint.toString();
        this.service = RestProxy.create(KeyService.class, this);
    }

    /**
     * Creates a builder that can configure options for the KeyClient before creating an instance of it.
     * @return A new builder to create a KeyClient from.
     */
    static KeyClientBuilder builder() {
        return new KeyClientBuilder();
    }


    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the {@code keys/create} permission.
     *
     * <p>The {@link JsonWebKeyType keyType} indicates the type of key to create. Possible values include: {@link JsonWebKeyType#EC EC},
     * {@link JsonWebKeyType#EC_HSM EC-HSM}, {@link JsonWebKeyType#RSA RSA}, {@link JsonWebKeyType#RSA_HSM RSA-HSM} and {@link JsonWebKeyType#OCT OCT}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key. Prints out the details of the created key.</p>
     * <pre>
     * Key retKey = keyClient.createKey("keyName", JsonWebKeyType.EC).value();
     * System.out.printf("Key is created with name %s and id %s \n", retKey.name(), retKey.id());
     * </pre>
     *
     * @param name The name of the key being created.
     * @param keyType The type of key to create. For valid values, see {@link JsonWebKeyType JsonWebKeyType}.
     * @throws ResourceModifiedException if {@code name} or {@code keyType} is null.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link Key created key}.
     */
    public Response<Key> createKey(String name, JsonWebKeyType keyType) {
        KeyRequestParameters parameters = new KeyRequestParameters().kty(keyType);
        return service.createKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the {@code keys/create} permission.
     *
     * <p>The {@link KeyCreateOptions} is required. The {@link KeyCreateOptions#expires() expires} and {@link KeyCreateOptions#notBefore() notBefore} values
     * are optional. If not specified, no values are set for the fields. The {@link KeyCreateOptions#enabled() enabled} field
     * is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link KeyCreateOptions#keyType() keyType} indicates the type of key to create. Possible values include: {@link JsonWebKeyType#EC EC},
     * {@link JsonWebKeyType#EC_HSM EC-HSM}, {@link JsonWebKeyType#RSA RSA}, {@link JsonWebKeyType#RSA_HSM RSA-HSM} and {@link JsonWebKeyType#OCT OCT}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new RSA key which activates in one day and expires in one year. Prints out the details of the created key.</p>
     * <pre>
     * KeyCreateOptions KeyCreateOptions = new KeyCreateOptions("keyName", JsonWebKeyType.RSA)
     *    .notBefore(OffsetDateTime.now().plusDays(1))
     *    .expires(OffsetDateTime.now().plusYears(1));
     *
     * Key key = keyClient.createKey(keyCreateOptions).value();
     * System.out.printf("Key is created with name %s and id %s \n", key.name(), key.id());
     * </pre>
     *
     * @param keyCreateOptions The key options object containing information about the key being created.
     * @throws NullPointerException if {@code keyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code keyCreateOptions} is malformed.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link Key created key}.
     */
    public Response<Key> createKey(KeyCreateOptions keyCreateOptions) {
        Objects.requireNonNull(keyCreateOptions, "The key options parameter cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
                .kty(keyCreateOptions.keyType())
                .keyOps(keyCreateOptions.keyOperations())
                .keyAttributes(new KeyRequestAttributes(keyCreateOptions));
        return service.createKey(endpoint, keyCreateOptions.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Creates a new Rsa key and stores it in the key vault. The create Rsa key operation can be used to create any Rsa key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the {@code keys/create} permission.
     *
     * <p>The {@link RsaKeyCreateOptions} is required. The {@link RsaKeyCreateOptions#keySize() keySize} can be optionally specified. The {@link RsaKeyCreateOptions#expires() expires}
     * and {@link RsaKeyCreateOptions#notBefore() notBefore} values are optional. If not specified, no values are set for the fields. The {@link RsaKeyCreateOptions#enabled() enabled} field
     * is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link RsaKeyCreateOptions#keyType() keyType} indicates the type of key to create. Possible values include: {@link JsonWebKeyType#RSA RSA} and
     * {@link JsonWebKeyType#RSA_HSM RSA-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new RSA key with size 2048 which activates in one day and expires in one year. Prints out the details of the created key.</p>
     * <pre>
     * RsaKeyCreateOptions rsaKeyCreateOptions = new RsaKeyCreateOptions("keyName", JsonWebKeyType.RSA)
     *    .keySize(2048)
     *    .notBefore(OffsetDateTime.now().plusDays(1))
     *    .expires(OffsetDateTime.now().plusYears(1));
     *
     * Key rsaKey = keyClient.createRSAKey(rsaKeyCreateOptions).value();
     * System.out.printf("Key is created with name %s and id %s \n", rsaKey.name(), rsaKey.id());
     * </pre>
     *
     * @param rsaKeyCreateOptions The key options object containing information about the rsa key being created.
     * @throws NullPointerException if {@code rsaKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code rsaKeyCreateOptions} is malformed.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link Key created key}.
     */
    public Response<Key> createRsaKey(RsaKeyCreateOptions rsaKeyCreateOptions) {
        Objects.requireNonNull(rsaKeyCreateOptions, "The Rsa key options parameter cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
            .kty(rsaKeyCreateOptions.keyType())
            .keySize(rsaKeyCreateOptions.keySize())
            .keyOps(rsaKeyCreateOptions.keyOperations())
            .keyAttributes(new KeyRequestAttributes(rsaKeyCreateOptions));
        return service.createKey(endpoint, rsaKeyCreateOptions.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Creates a new Ec key and  stores it in the key vault. The create Ec key operation can be used to create any Ec key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the {@code keys/create} permission.
     *
     * <p>The {@link EcKeyCreateOptions} parameter is required. The {@link EcKeyCreateOptions#curve() key curve} can be optionally specified. If not specified,
     * default value of {@link JsonWebKeyCurveName#P_256 P-256} is used by Azure Key Vault. The {@link EcKeyCreateOptions#expires() expires} and {@link EcKeyCreateOptions#notBefore() notBefore} values
     * are optional. If not specified, no values are set for the fields. The {@link EcKeyCreateOptions#enabled() enabled} field is
     * set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link EcKeyCreateOptions#keyType() keyType} indicates the type of key to create. Possible values include: {@link JsonWebKeyType#EC EC} and
     * {@link JsonWebKeyType#EC_HSM EC-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key with P-384 web key curve. The key activates in one day and expires in one year. Prints out
     * the details of the created key.</p>
     * <pre>
     * EcKeyCreateOptions ecKeyCreateOptions = new EcKeyCreateOptions("keyName", JsonWebKeyType.EC)
     *    .curve(JsonWebKeyCurveName.P_384)
     *    .notBefore(OffsetDateTime.now().plusDays(1))
     *    .expires(OffsetDateTime.now().plusYears(1));
     *
     * Key ecKey = keyClient.createECKey(ecKeyCreateOptions).value();
     * System.out.printf("Key is created with name %s and id %s \n", ecKey.name(), ecKey.id());
     * </pre>
     *
     * @param ecKeyCreateOptions The key options object containing information about the ec key being created.
     * @throws NullPointerException if {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code ecKeyCreateOptions} is malformed.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link Key created key}.
     */
    public Response<Key> createEcKey(EcKeyCreateOptions ecKeyCreateOptions) {
        Objects.requireNonNull(ecKeyCreateOptions, "The Ec key options parameter cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
            .kty(ecKeyCreateOptions.keyType())
            .curve(ecKeyCreateOptions.curve())
            .keyOps(ecKeyCreateOptions.keyOperations())
            .keyAttributes(new KeyRequestAttributes(ecKeyCreateOptions));
        return service.createKey(endpoint, ecKeyCreateOptions.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Imports an externally created key and stores it in key vault. The import key operation may be used to import any key type
     * into the Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key. This operation requires the {@code keys/import} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new key into key vault. Prints out the details of the imported key.</p>
     * <pre>
     * Key importedKey = keyClient.importKey("keyName", jsonWebKeyToImport).value();
     * System.out.printf("Key is imported with name %s and id %s \n", importedKey.name(), importedKey.id());
     * </pre>
     *
     * @param name The name for the imported key.
     * @param keyMaterial The Json web key being imported.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link Key imported key}.
     */
    public Response<Key> importKey(String name, JsonWebKey keyMaterial) {
        KeyImportRequestParameters parameters = new KeyImportRequestParameters().key(keyMaterial);
        return service.importKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Imports an externally created key and stores it in key vault. The import key operation may be used to import any key type
     * into the Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key. This operation requires the {@code keys/import} permission.
     *
     * <p>The {@code keyImportOptions} is required and its fields {@link KeyImportOptions#name() name} and {@link KeyImportOptions#keyMaterial() key material} cannot
     * be null. The {@link KeyImportOptions#expires() expires} and {@link KeyImportOptions#notBefore() notBefore} values in {@code keyImportOptions} are optional. If not
     * specified, no values are set for the fields. The {@link KeyImportOptions#enabled() enabled} field is set to true and the {@link KeyImportOptions#hsm() hsm} field is
     * set to false by Azure Key Vault, if they are not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new key into key vault. Prints out the details of the imported key.</p>
     * <pre>
     * KeyImportOptions keyImportOptions = new KeyImportOptions("keyName", jsonWebKeyToImport)
     *   .hsm(true)
     *   .expires(OffsetDateTime.now().plusDays(60));
     *
     * Key importedKey = keyClient.importKey(keyImportOptions).value();
     * System.out.printf("Key is imported with name %s and id %s \n", importedKey.name(), importedKey.id());
     * </pre>
     *
     * @param keyImportOptions The key import configuration object containing information about the json web key being imported.
     * @throws NullPointerException if {@code keyImportOptions} is {@code null}.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link Key imported key}.
     */
    public Response<Key> importKey(KeyImportOptions keyImportOptions) {
        Objects.requireNonNull(keyImportOptions, "The key import configuration parameter cannot be null.");
        KeyImportRequestParameters parameters = new KeyImportRequestParameters()
                .key(keyImportOptions.keyMaterial())
                .hsm(keyImportOptions.hsm())
                .keyAttributes(new KeyRequestAttributes(keyImportOptions));
        return service.importKey(endpoint, keyImportOptions.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Gets the public part of the specified key and key version. The get key operation is applicable to all key types and
     * If the requested key is symmetric, then no key material is released in the response. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the key in the key vault. Prints out the details of the returned key.</p>
     * <pre>
     * String keyVersion = "6A385B124DEF4096AF1361A85B16C204";
     * Key keyWithVersion = keyClient.getKey("keyName", keyVersion).value();
     * System.out.printf("Key is returned with name %s and id %s \n", keyWithVersion.name(), keyWithVersion.id());
     * </pre>
     *
     * @param name The name of the key, cannot be null
     * @param version The version of the key to retrieve. If this is an empty String or null, this call is equivalent to calling {@link KeyClient#getKey(String)}, with the latest version being retrieved.
     * @throws ResourceNotFoundException when a key with {@code name} and {@code version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} or {@code version} are empty strings.
     * @return A {@link Response} whose {@link Response#value() value} contains the requested {@link Key key}.
     */
    public Response<Key> getKey(String name, String version) {
        String keyVersion = "";
        if (version != null) {
            keyVersion = version;
        }
        return service.getKey(endpoint, name, keyVersion, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Get the public part of the latest version of the specified key from the key vault. The get key operation is applicable to all key types and
     * If the requested key is symmetric, then no key material is released in the response. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the latest version of the key in the key vault. Prints out the details of the returned key.</p>
     * <pre>
     * Key key = keyClient.getKey("keyName").value();
     * System.out.printf("Key is returned with name %s and id %s \n", key.name(), key.value().id());
     * </pre>
     *
     * @param name The name of the key.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the requested {@link Key key}.
     */
    public Response<Key> getKey(String name) {
        return getKey(name, "");
    }


    /**
     * Get public part of the key which represents {@link KeyBase keyBase} from the key vault. If {@link KeyBase#version() version} is not set
     * then the latest version of the key is returned. The get key operation is applicable to all key types and If the requested key is symmetric,
     * then no key material is released in the response. This operation requires the {@code keys/get} permission.
     *
     * <p>The list operations {@link KeyClient#listKeys()} and {@link KeyClient#listKeyVersions(String)} return
     * the {@link List} containing {@link KeyBase base key} as output excluding the key material of the key.
     * This operation can then be used to get the full key with its key material from {@code keyBase}. </p>
     * <pre>
     * keyClient.listKeys().stream().map(keyClient::getKey).forEach(keyResponse -&gt;
     *   System.out.printf("Key is returned with name %s and id %s \n", keyResponse.value().name(), keyResponse.value().id()));
     * </pre>
     *
     * @param keyBase The {@link KeyBase base key} holding attributes of the key being requested.
     * @throws ResourceNotFoundException when a key with {@link KeyBase#name() name} and {@link KeyBase#version() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link KeyBase#name()}  name} or {@link KeyBase#version() version} are empty strings.
     * @return A {@link Response} whose {@link Response#value() value} contains the requested {@link Key key}.
     */
    public Response<Key> getKey(KeyBase keyBase) {
        Objects.requireNonNull(keyBase, "The Key Base parameter cannot be null.");
        String keyVersion = "";
        return service.getKey(endpoint, keyBase.name(), keyVersion, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Updates the attributes associated with the specified key, but not the cryptographic key material of the specified key in the key vault. The update
     * operation changes specified attributes of an existing stored key and attributes that are not specified in the request are left unchanged.
     * The cryptographic key material of a key itself cannot be changed. This operation requires the {@code keys/set} permission.
     *
     * <p>The {@code key} is required and its fields {@link KeyBase#name() name} and {@link KeyBase#version() version} cannot be null.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the latest version of the key, changes its expiry time and the updates the key in the key vault.</p>
     * <pre>
     * Key key = keyClient.getKey("keyName").value();
     * key.expires(OffsetDateTime.now().plusDays(60));
     * KeyBase updatedKeyBase = keyClient.updateKey(key).value();
     * Key updatedKey = keyClient.getKey(updatedKeyBase.name()).value();
     * </pre>
     *
     * @param key The {@link KeyBase base key} object with updated properties.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @throws ResourceNotFoundException when a key with {@link KeyBase#name() name} and {@link KeyBase#version() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link KeyBase#name() name} or {@link KeyBase#version() version} are empty strings.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link KeyBase updated key}.
     */
    public Response<Key> updateKey(KeyBase key) {
        Objects.requireNonNull(key, "The key input parameter cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
            .tags(key.tags())
            .keyOps(key.keyOperations())
            .keyAttributes(new KeyRequestAttributes(key));

        return service.updateKey(endpoint, key.name(), key.version(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Deletes a key of any type from the key vault. If soft-delete is enabled on the key vault then the key is placed in the deleted state
     * and requires to be purged for permanent deletion else the key is permanently deleted. The delete operation applies to any key stored
     * in Azure Key Vault but it cannot be applied to an individual version of a key. This operation removes the cryptographic material
     * associated with the key, which means the key is not usable for Sign/Verify, Wrap/Unwrap or Encrypt/Decrypt operations. This operation
     * requires the {@code keys/delete} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the key from the keyvault. Prints out the recovery id of the deleted key returned in the response.</p>
     * <pre>
     * DeletedKey deletedKey = keyClient.deleteKey("keyName").value();
     * System.out.printf("Deleted Key's Recovery Id %s", deletedKey.recoveryId()));
     * </pre>
     *
     * @param name The name of the key to be deleted.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link DeletedKey deleted key}.
     */
    public Response<DeletedKey> deleteKey(String name) {
        return service.deleteKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Gets the public part of a deleted key. The Get Deleted Key operation is applicable for soft-delete enabled vaults. This operation
     * requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the deleted key from the key vault enabled for soft-delete. Prints out the details of the deleted key
     * returned in the response.</p>
     * <pre>
     * //Assuming key is deleted on a soft-delete enabled key vault.
     * DeletedKey deletedKey = keyClient.getDeletedKey("keyName").value();
     * System.out.printf("Deleted Key with recovery Id %s \n", deletedKey.recoveryId());
     * </pre>
     *
     * @param name The name of the deleted key.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link DeletedKey deleted key}.
     */
    public Response<DeletedKey> getDeletedKey(String name) {
        return service.getDeletedKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Permanently deletes the specified key without the possibility of recovery. The Purge Deleted Key operation is applicable for
     * soft-delete enabled vaults. This operation requires the {@code keys/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted key from the key vault enabled for soft-delete. Prints out the status code from the server response.</p>
     * <pre>
     * //Assuming key is deleted on a soft-delete enabled key vault.
     * VoidResponse purgeResponse = keyClient.purgeDeletedKey("deletedKeyName");
     * System.out.printf("Purge Status Code: %rsaPrivateExponent", purgeResponse.statusCode());
     * </pre>
     *
     * @param name The name of the deleted key.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     * @return A {@link VoidResponse}.
     */
    public VoidResponse purgeDeletedKey(String name) {
        return service.purgeDeletedKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Recovers the deleted key in the key vault to its latest version and can only be performed on a soft-delete enabled vault. An attempt
     * to recover an non-deleted key will return an error. Consider this the inverse of the delete operation on soft-delete enabled vaults.
     * This operation requires the {@code keys/recover} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted key from the key vault enabled for soft-delete.</p>
     * <pre>
     * //Assuming key is deleted on a soft-delete enabled key vault.
     * Key recoveredKey =  keyClient.recoverDeletedKey("deletedKeyName").value();
     * System.out.printf("Recovered key with name %s", recoveredKey.name());
     * </pre>
     *
     * @param name The name of the deleted key to be recovered.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link Key recovered key}.
     */
    public Response<Key> recoverDeletedKey(String name) {
        return service.recoverDeletedKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Requests a backup of the specified key be downloaded to the client. The Key Backup operation exports a key from Azure Key
     * Vault in a protected form. Note that this operation does not return key material in a form that can be used outside the Azure Key
     * Vault system, the returned key material is either protected to a Azure Key Vault HSM or to Azure Key Vault itself. The intent
     * of this operation is to allow a client to generate a key in one Azure Key Vault instance, backup the key, and then restore it
     * into another Azure Key Vault instance. The backup operation may be used to export, in protected form, any key type from Azure
     * Key Vault. Individual versions of a key cannot be backed up. Backup / Restore can be performed within geographical boundaries only;
     * meaning that a backup from one geographical area cannot be restored to another geographical area. For example, a backup
     * from the US geographical area cannot be restored in an EU geographical area. This operation requires the {@code key/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the key from the key vault and prints out the length of the key's backup byte array returned in the response</p>
     * <pre>
     * byte[] keyBackup = keyClient.backupKey("keyName").value();
     * System.out.printf("Key's Backup Byte array's length %s", keyBackup.length);
     * </pre>
     *
     * @param name The name of the key.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the backed up key blob.
     */
    public Response<byte[]> backupKey(String name) {
        return service.backupKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
            .flatMap(base64URLResponse ->  Mono.just(new SimpleResponse<byte[]>(base64URLResponse.request(),
                base64URLResponse.statusCode(), base64URLResponse.headers(), base64URLResponse.value().value()))).block();
    }

    /**
     * Restores a backed up key to a vault. Imports a previously backed up key into Azure Key Vault, restoring the key, its key identifier,
     * attributes and access control policies. The restore operation may be used to import a previously backed up key. Individual versions of a
     * key cannot be restored. The key is restored in its entirety with the same key name as it had when it was backed up. If the key name is not
     * available in the target Key Vault, the restore operation will be rejected. While the key name is retained during restore, the final key identifier
     * will change if the key is restored to a different vault. Restore will restore all versions and preserve version identifiers. The restore operation is subject
     * to security constraints: The target Key Vault must be owned by the same Microsoft Azure Subscription as the source Key Vault The user must have restore permission in
     * the target Key Vault. This operation requires the {@code keys/restore} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the key in the key vault from its backup. Prints out the details of the restored key returned in the response.</p>
     * <pre>
     * //Pass the Key Backup Byte array to the restore operation.
     * KeyClient.restoreKey(keyBackupByteArray).subscribe(keyResponse -&gt;
     *   System.out.printf("Restored Key with name %s and id %s \n", keyResponse.value().name(), keyResponse.value().id()));
     * </pre>
     *
     * @param backup The backup blob associated with the key.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link Key restored key}.
     */
    public Response<Key> restoreKey(byte[] backup) {
        KeyRestoreRequestParameters parameters = new KeyRestoreRequestParameters().keyBackup(backup);
        return service.restoreKey(endpoint, API_VERSION, parameters, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * List keys in the key vault. Retrieves a list of the keys in the Key Vault as JSON Web Key structures that contain the public
     * part of a stored key. The List operation is applicable to all key types and the individual key response in the list is represented by {@link KeyBase} as only the base key identifier,
     * attributes and tags are provided in the response. The key material and individual key versions are not listed in the response. This operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get full keys with values from this information. Loop over the {@link KeyBase key} and
     * call {@link KeyClient#getKey(KeyBase baseKey)} . This will return the {@link Key key} with value included of its latest version.</p>
     * <pre>
     * keyClient.listKeys().stream().map(keyClient::getKey).forEach(keyResponse -&gt;
     *   System.out.printf("Received key with name %s and id %s", keyResponse.value().name(), keyResponse.value().id()));
     * </pre>
     *
     * @return A {@link List} containing {@link KeyBase key} of all the keys in the vault.
     */
    public List<KeyBase> listKeys() {
        return service.getKeys(endpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchKeys).collectList().block();
    }

    /**
     * Lists {@link DeletedKey deleted keys} of the key vault. The deleted keys are retrieved as JSON Web Key structures
     * that contain the public part of a deleted key. The Get Deleted Keys operation is applicable for vaults enabled
     * for soft-delete. This operation requires the {@code keys/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted keys in the key vault and for each deleted key prints out its recovery id.</p>
     * <pre>
     * keyClient.listDeletedKeys().stream().forEach(deletedKey -&gt;
     *   System.out.printf("Deleted key's recovery Id %s", deletedKey.recoveryId()));
     * </pre>
     *
     * @return A {@link List} containing all of the {@link DeletedKey deleted keys} in the vault.
     */
    public List<DeletedKey> listDeletedKeys() {
        return service.getDeletedKeys(endpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchDeletedKeys).collectList().block();
    }

    /**
     * List all versions of the specified key. The individual key response in the flux is represented by {@link KeyBase}
     * as only the base key identifier, attributes and tags are provided in the response. The key material values are
     * not provided in the response. This operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get full keys with values for each version from this information. Loop over the {@link KeyBase key} and
     * call {@link KeyClient#getKey(KeyBase baseKey)} . This will return the {@link Key keys} with values included of the specified versions.</p>
     * <pre>
     * keyClient.listKeyVersions("keyName").stream().map(keyClient::getKey).forEach(keyResponse -&gt;
     *   System.out.printf("Received key's version with name %s and id %s", keyResponse.value().name(), keyResponse.value().id()));
     * </pre>
     *
     * @param name The name of the key.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     * @return A {@link List} containing {@link KeyBase key} of all the versions of the specified key in the vault. List is empty if key with {@code name} does not exist in key vault.
     */
    public List<KeyBase> listKeyVersions(String name) {
        return service.getKeyVersions(endpoint, name, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchKeys).collectList().block();
    }

    /**
     * Gets attributes of all the keys given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyClient#listKeys()}.
     *
     * @param nextPageLink The {@link KeyBasePage#nextLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link KeyBase key} from the next page of results.
     */
    private Flux<KeyBase> listKeysNext(String nextPageLink) {
        return service.getKeys(endpoint, nextPageLink, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchKeys);
    }

    private Publisher<KeyBase> extractAndFetchKeys(PagedResponse<KeyBase> page) {
        return extractAndFetch(page, this::listKeysNext);
    }

    /**
     * Gets attributes of all the keys given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyClient#listDeletedKeys()}.
     *
     * @param nextPageLink The {@link DeletedKeyPage#nextLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link KeyBase key} from the next page of results.
     */
    private Flux<DeletedKey> listDeletedKeysNext(String nextPageLink) {
        return service.getDeletedKeys(endpoint, nextPageLink, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchDeletedKeys);
    }

    private Publisher<DeletedKey> extractAndFetchDeletedKeys(PagedResponse<DeletedKey> page) {
        return extractAndFetch(page, this::listDeletedKeysNext);
    }

    //TODO: Extract this in azure-core ImplUtils and use from there
    private <T> Publisher<T> extractAndFetch(PagedResponse<T> page, Function<String, Publisher<T>> content) {
        String nextPageLink = page.nextLink();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(content.apply(nextPageLink));
    }
}
