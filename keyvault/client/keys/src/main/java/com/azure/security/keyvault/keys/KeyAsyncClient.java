// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.exception.HttpRequestException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.RestProxy;
import com.azure.core.implementation.annotation.ServiceClient;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.implementation.DeletedKeyPage;
import com.azure.security.keyvault.keys.implementation.KeyBasePage;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.EcKeyCreateOptions;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.KeyBase;
import com.azure.security.keyvault.keys.models.KeyCreateOptions;
import com.azure.security.keyvault.keys.models.KeyImportOptions;
import com.azure.security.keyvault.keys.models.RsaKeyCreateOptions;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;
import com.azure.security.keyvault.keys.models.webkey.KeyCurveName;
import com.azure.security.keyvault.keys.models.webkey.KeyOperation;
import com.azure.security.keyvault.keys.models.webkey.KeyType;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The KeyAsyncClient provides asynchronous methods to manage {@link Key keys} in the Azure Key Vault. The client
 * supports creating, retrieving, updating, deleting, purging, backing up, restoring and listing the {@link Key keys}. The client
 * also supports listing {@link DeletedKey deleted keys} for a soft-delete enabled Azure Key Vault.
 *
 * <p><strong>Samples to construct the client</strong></p>
 * <pre>
 * KeyAsyncClient.builder()
 *   .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
 *   .credential(new DefaultAzureCredential())
 *   .build()
 * </pre>
 *
 * @see KeyAsyncClientBuilder
 */
@ServiceClient(builder = KeyAsyncClientBuilder.class, isAsync = true, serviceInterfaces = KeyService.class)
public final class KeyAsyncClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    static final String KEY_VAULT_SCOPE = "https://vault.azure.net/.default";

    private String endpoint;
    private final KeyService service;
    private final ClientLogger logger = new ClientLogger(KeyAsyncClient.class);


    /**
     * Creates a KeyAsyncClient that uses {@code pipeline} to service requests
     *
     * @param endpoint URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    KeyAsyncClient(URL endpoint, HttpPipeline pipeline) {
        Objects.requireNonNull(endpoint, KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.endpoint = endpoint.toString();
        this.service = RestProxy.create(KeyService.class, pipeline);
    }

    /**
     * Creates a builder that can configure options for the KeyAsyncClient before creating an instance of it.
     * @return A new builder to create a KeyAsyncClient from.
     */
    public static KeyAsyncClientBuilder builder() {
        return new KeyAsyncClientBuilder();
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the {@code keys/create} permission.
     *
     * <p>The {@link KeyType keyType} indicates the type of key to create. Possible values include: {@link KeyType#EC EC},
     * {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM} and {@link KeyType#OCT OCT}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key. Subscribes to the call asynchronously and prints out the newly created key details when a response has been received.</p>
     * <pre>
     * keyAsyncClient.createKey("keyName", KeyType.EC).subscribe(keyResponse -&gt;
     *   System.out.printf("Key is created with name %s and id %s \n", keyResponse.value().name(), keyResponse.value().id()));
     * </pre>
     *
     * @param name The name of the key being created.
     * @param keyType The type of key to create. For valid values, see {@link KeyType KeyType}.
     * @throws ResourceModifiedException if {@code name} or {@code keyType} is null.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link Key created key}.
     */
    public Mono<Response<Key>> createKey(String name, KeyType keyType) {
        KeyRequestParameters parameters = new KeyRequestParameters().kty(keyType);
        return service.createKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Creating key - {}",  name))
                .doOnSuccess(response -> logger.logAsInfo("Created key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to create key - {}", name, error));
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the {@code keys/create} permission.
     *
     * <p>The {@link KeyCreateOptions} is required. The {@link KeyCreateOptions#expires() expires} and {@link KeyCreateOptions#notBefore() notBefore} values
     * are optional. The {@link KeyCreateOptions#enabled() enabled} field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link KeyCreateOptions#keyType() keyType} indicates the type of key to create. Possible values include: {@link KeyType#EC EC},
     * {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM} and {@link KeyType#OCT OCT}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new Rsa key which activates in one day and expires in one year. Subscribes to the call asynchronously
     * and prints out the newly created key details when a response has been received.</p>
     * <pre>
     * KeyCreateOptions keyCreateOptions = new KeyCreateOptions("keyName", KeyType.RSA)
     *    .notBefore(OffsetDateTime.now().plusDays(1))
     *    .expires(OffsetDateTime.now().plusYears(1));
     *
     * keyAsyncClient.createKey(keyCreateOptions).subscribe(keyResponse -&gt;
     *   System.out.printf("Key is created with name %s and id %s \n", keyResponse.value().name(), keyResponse.value().id()));
     * </pre>
     *
     * @param keyCreateOptions The key configuration object containing information about the key being created.
     * @throws NullPointerException if {@code keyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code keyCreateOptions} is malformed.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link Key created key}.
     */
    public Mono<Response<Key>> createKey(KeyCreateOptions keyCreateOptions) {
        Objects.requireNonNull(keyCreateOptions, "The key options parameter cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
                .kty(keyCreateOptions.keyType())
                .keyOps(keyCreateOptions.keyOperations())
                .keyAttributes(new KeyRequestAttributes(keyCreateOptions));
        return service.createKey(endpoint, keyCreateOptions.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Creating key - {}",  keyCreateOptions.name()))
                .doOnSuccess(response -> logger.logAsInfo("Created key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to create key - {}", keyCreateOptions.name(), error));
    }

    /**
     * Creates a new Rsa key and stores it in the key vault. The create Rsa key operation can be used to create any Rsa key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the {@code keys/create} permission.
     *
     * <p>The {@link RsaKeyCreateOptions} is required. The {@link RsaKeyCreateOptions#keySize() keySize} can be optionally specified. The {@link RsaKeyCreateOptions#expires() expires}
     * and {@link RsaKeyCreateOptions#notBefore() notBefore} values are optional. The {@link RsaKeyCreateOptions#enabled() enabled} field
     * is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link RsaKeyCreateOptions#keyType() keyType} indicates the type of key to create. Possible values include: {@link KeyType#RSA RSA} and
     * {@link KeyType#RSA_HSM RSA-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new RSA key with size 2048 which activates in one day and expires in one year. Subscribes to the call asynchronously
     * and prints out the newly created key details when a response has been received.</p>
     * <pre>
     * RsaKeyCreateOptions rsaKeyCreateOptions = new RsaKeyCreateOptions("keyName", KeyType.RSA)
     *    .keySize(2048)
     *    .notBefore(OffsetDateTime.now().plusDays(1))
     *    .expires(OffsetDateTime.now().plusYears(1));
     *
     * keyAsyncClient.createRsaKey(rsaKeyCreateOptions).subscribe(keyResponse -&gt;
     *   System.out.printf("RSA Key is created with name %s and id %s \n", keyResponse.value().name(), keyResponse.value().id()));
     * </pre>
     *
     * @param rsaKeyCreateOptions The key configuration object containing information about the rsa key being created.
     * @throws NullPointerException if {@code rsaKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code rsaKeyCreateOptions} is malformed.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link Key created key}.
     */
    public Mono<Response<Key>> createRsaKey(RsaKeyCreateOptions rsaKeyCreateOptions) {
        Objects.requireNonNull(rsaKeyCreateOptions, "The Rsa key options parameter cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
            .kty(rsaKeyCreateOptions.keyType())
            .keySize(rsaKeyCreateOptions.keySize())
            .keyOps(rsaKeyCreateOptions.keyOperations())
            .keyAttributes(new KeyRequestAttributes(rsaKeyCreateOptions));
        return service.createKey(endpoint, rsaKeyCreateOptions.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Creating Rsa key - {}",  rsaKeyCreateOptions.name()))
                .doOnSuccess(response -> logger.logAsInfo("Created Rsa key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to create Rsa key - {}", rsaKeyCreateOptions.name(), error));
    }

    /**
     * Creates a new Ec key and stores it in the key vault. The create Ec key operation can be used to create any Ec key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the {@code keys/create} permission.
     *
     * <p>The {@link EcKeyCreateOptions} parameter is required. The {@link EcKeyCreateOptions#curve() key curve} can be optionally specified. If not specified,
     * default value of {@link KeyCurveName#P_256 P-256} is used by Azure Key Vault. The {@link EcKeyCreateOptions#expires() expires} and {@link EcKeyCreateOptions#notBefore() notBefore} values
     * are optional. The {@link EcKeyCreateOptions#enabled() enabled} field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link EcKeyCreateOptions#keyType() keyType} indicates the type of key to create. Possible values include: {@link KeyType#EC EC} and
     * {@link KeyType#EC_HSM EC-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key with P-384 web key curve. The key activates in one day and expires in one year. Subscribes to the call asynchronously
     * and prints out the newly created ec key details when a response has been received.</p>
     * <pre>
     * EcKeyCreateOptions ecKeyCreateOptions = new EcKeyCreateOptions("keyName", KeyType.EC)
     *    .curve(KeyCurveName.P_384)
     *    .notBefore(OffsetDateTime.now().plusDays(1))
     *    .expires(OffsetDateTime.now().plusYears(1));
     *
     * keyAsyncClient.createEcKey(ecKeyCreateOptions).subscribe(keyResponse -&gt;
     *   System.out.printf("EC Key is created with name %s and id %s \n", keyResponse.value().name(), keyResponse.value().id()));
     * </pre>
     *
     * @param ecKeyCreateOptions The key options object containing information about the ec key being created.
     * @throws NullPointerException if {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code ecKeyCreateOptions} is malformed.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link Key created key}.
     */
    public Mono<Response<Key>> createEcKey(EcKeyCreateOptions ecKeyCreateOptions) {
        Objects.requireNonNull(ecKeyCreateOptions, "The Ec key options options cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
            .kty(ecKeyCreateOptions.keyType())
            .curve(ecKeyCreateOptions.curve())
            .keyOps(ecKeyCreateOptions.keyOperations())
            .keyAttributes(new KeyRequestAttributes(ecKeyCreateOptions));
        return service.createKey(endpoint, ecKeyCreateOptions.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Creating Ec key - {}",  ecKeyCreateOptions.name()))
                .doOnSuccess(response -> logger.logAsInfo("Created Ec key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to create Ec key - {}", ecKeyCreateOptions.name(), error));
    }

    /**
     * Imports an externally created key and stores it in key vault. The import key operation may be used to import any key type
     * into the Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key. This operation requires the {@code keys/import} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new key into key vault. Subscribes to the call asynchronously and prints out the newly imported key details
     * when a response has been received.</p>
     * <pre>
     * keyAsyncClient.importKey("keyName", jsonWebKeyToImport).subscribe(keyResponse -&gt;
     *   System.out.printf("Key is imported with name %s and id %s \n", keyResponse.value().name(), keyResponse.value().id()));
     * </pre>
     *
     * @param name The name for the imported key.
     * @param keyMaterial The Json web key being imported.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link Key imported key}.
     */
    public Mono<Response<Key>> importKey(String name, JsonWebKey keyMaterial) {
        KeyImportRequestParameters parameters = new KeyImportRequestParameters().key(keyMaterial);
        return service.importKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Importing key - {}",  name))
                .doOnSuccess(response -> logger.logAsInfo("Imported key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to import key - {}", name, error));
    }

    /**
     * Imports an externally created key and stores it in key vault. The import key operation may be used to import any key type
     * into the Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key. This operation requires the {@code keys/import} permission.
     *
     * <p>The {@code keyImportOptions} is required and its fields {@link KeyImportOptions#name() name} and {@link KeyImportOptions#keyMaterial() key material} cannot
     * be null. The {@link KeyImportOptions#expires() expires} and {@link KeyImportOptions#notBefore() notBefore} values in {@code keyImportOptions}
     * are optional. If not specified, no values are set for the fields. The {@link KeyImportOptions#enabled() enabled} field is set to true and
     * the {@link KeyImportOptions#hsm() hsm} field is set to false by Azure Key Vault, if they are not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new key into key vault. Subscribes to the call asynchronously and prints out the newly imported key details
     * when a response has been received.</p>
     * <pre>
     * KeyImportOptions keyImportOptions = new KeyImportOptions("keyName", jsonWebKeyToImport)
     *   .hsm(true)
     *   .expires(OffsetDateTime.now().plusDays(60));
     *
     * keyAsyncClient.importKey(keyImportOptions).subscribe(keyResponse -&gt;
     *   System.out.printf("Key is imported with name %s and id %s \n", keyResponse.value().name(), keyResponse.value().id()));
     * </pre>
     *
     * @param keyImportOptions The key import configuration object containing information about the json web key being imported.
     * @throws NullPointerException if {@code keyImportOptions} is {@code null}.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link Key imported key}.
     */
    public Mono<Response<Key>> importKey(KeyImportOptions keyImportOptions) {
        Objects.requireNonNull(keyImportOptions, "The key import configuration parameter cannot be null.");
        KeyImportRequestParameters parameters = new KeyImportRequestParameters()
                .key(keyImportOptions.keyMaterial())
                .hsm(keyImportOptions.hsm())
                .keyAttributes(new KeyRequestAttributes(keyImportOptions));
        return service.importKey(endpoint, keyImportOptions.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Importing key - {}",  keyImportOptions.name()))
                .doOnSuccess(response -> logger.logAsInfo("Imported key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to import key - {}", keyImportOptions.name(), error));
    }

    /**
     * Gets the public part of the specified key and key version. The get key operation is applicable to all key types and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the key in the key vault. Subscribes to the call asynchronously and prints out the
     * returned key details when a response has been received.</p>
     * <pre>
     * String keyVersion = "6A385B124DEF4096AF1361A85B16C204";
     * keyAsyncClient.getKey("keyName", keyVersion).subscribe(keyResponse -&gt;
     *   System.out.printf("Key returned with name %s, id %s and version %s", keyResponse.value().name(),
     *   keyResponse.value().id(), keyResponse.value().version()));
     * </pre>
     *
     * @param name The name of the key, cannot be null
     * @param version The version of the key to retrieve. If this is an empty String or null, this call is equivalent to calling {@link KeyAsyncClient#getKey(String)}, with the latest version being retrieved.
     * @throws ResourceNotFoundException when a key with {@code name} and {@code version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} or {@code version} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the requested {@link Key key}.
     */
    public Mono<Response<Key>> getKey(String name, String version) {
        String keyVersion = "";
        if (version != null) {
            keyVersion = version;
        }
        return service.getKey(endpoint, name, keyVersion, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Retrieving key - {}",  name))
                .doOnSuccess(response -> logger.logAsInfo("Retrieved key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to get key - {}", name, error));
    }

    /**
     * Get the public part of the latest version of the specified key from the key vault. The get key operation is applicable to
     * all key types and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the key in the key vault. Subscribes to the call asynchronously and prints out the
     * returned key details when a response has been received.</p>
     * <pre>
     * keyAsyncClient.getKey("keyName").subscribe(keyResponse -&gt;
     *   System.out.printf("Key with name %s, id %s \n", keyResponse.value().name(),
     *   keyResponse.value().id()));
     * </pre>
     *
     * @param name The name of the key.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the requested {@link Key key}.
     */
    public Mono<Response<Key>> getKey(String name) {
        return getKey(name, "")
                .doOnRequest(ignored -> logger.logAsInfo("Retrieving key - {}",  name))
                .doOnSuccess(response -> logger.logAsInfo("Retrieved key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to get key - {}", name, error));
    }


    /**
     * Get public part of the key which represents {@link KeyBase keyBase} from the key vault. The get key operation is applicable
     * to all key types and it requires the {@code keys/get} permission.
     *
     * <p>The list operations {@link KeyAsyncClient#listKeys()} and {@link KeyAsyncClient#listKeyVersions(String)} return
     * the {@link Flux} containing {@link KeyBase base key} as output excluding the key material of the key.
     * This operation can then be used to get the full key with its key material from {@code keyBase}.</p>
     * <pre>
     * keyAsyncClient.listKeys().subscribe(keyBase -&gt;
     *     client.getKey(keyBase).subscribe(keyResponse -&gt;
     *       System.out.printf("Key with name %s and value %s \n", keyResponse.value().name(), keyResponse.value().id())));
     * </pre>
     *
     * @param keyBase The {@link KeyBase base key} holding attributes of the key being requested.
     * @throws ResourceNotFoundException when a key with {@link KeyBase#name() name} and {@link KeyBase#version() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link KeyBase#name()}  name} or {@link KeyBase#version() version} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the requested {@link Key key}.
     */
    public Mono<Response<Key>> getKey(KeyBase keyBase) {
        Objects.requireNonNull(keyBase, "The Key Base parameter cannot be null.");
        String keyVersion = "";
        if (keyBase.version() != null) {
            keyVersion = keyBase.version();
        }
        return service.getKey(endpoint, keyBase.name(), keyVersion, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Retrieving key - {}",  keyBase.name()))
                .doOnSuccess(response -> logger.logAsInfo("Retrieved key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to get key - {}", keyBase.name(), error));
    }

    /**
     * Updates the attributes associated with the specified key, but not the cryptographic key material of the specified key in the key vault. The update
     * operation changes specified attributes of an existing stored key and attributes that are not specified in the request are left unchanged.
     * The cryptographic key material of a key itself cannot be changed. This operation requires the {@code keys/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the key, changes its notBefore time and then updates it in the Azure Key Vault. Subscribes to the call asynchronously and prints out the
     * returned key details when a response has been received.</p>
     * <pre>
     * keyAsyncClient.getKey("keyName").subscribe(keyResponse -&gt; {
     *     Key key = keyResponse.value();
     *     //Update the not before time of the key.
     *     key.notBefore(OffsetDateTime.now().plusDays(50));
     *     keyAsyncClient.updateKey(key).subscribe(updatedKeyResponse -&gt;
     *         System.out.printf("Key's updated not before time %s \n", updatedKeyResponse.value().notBefore().toString()));
     *   });
     * </pre>
     *
     * @param key The {@link KeyBase base key} object with updated properties.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @throws ResourceNotFoundException when a key with {@link KeyBase#name() name} and {@link KeyBase#version() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link KeyBase#name() name} or {@link KeyBase#version() version} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link KeyBase updated key}.
     */
    public Mono<Response<Key>> updateKey(KeyBase key) {
        Objects.requireNonNull(key, "The key input parameter cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
                .tags(key.tags())
                .keyAttributes(new KeyRequestAttributes(key));

        return service.updateKey(endpoint, key.name(), key.version(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Updating key - {}",  key.name()))
                .doOnSuccess(response -> logger.logAsInfo("Updated key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to update key - {}", key.name(), error));
    }

    /**
     * Updates the attributes and key operations associated with the specified key, but not the cryptographic key material of the specified key in the key vault. The update
     * operation changes specified attributes of an existing stored key and attributes that are not specified in the request are left unchanged.
     * The cryptographic key material of a key itself cannot be changed. This operation requires the {@code keys/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the key, changes its notBefore time and then updates it in the Azure Key Vault. Subscribes to the call asynchronously and prints out the
     * returned key details when a response has been received.</p>
     * <pre>
     * keyAsyncClient.getKey("keyName").subscribe(keyResponse -&gt; {
     *     Key key = keyResponse.value();
     *     //Update the not before time of the key and associate Encrypt and Decrypt operations with it.
     *     key.notBefore(OffsetDateTime.now().plusDays(50));
     *     keyAsyncClient.updateKey(key, KeyOperation.ENCRYPT, KeyOperation.DECRYPT).subscribe(updatedKeyResponse -&gt;
     *         System.out.printf("Key's updated not before time %s \n", updatedKeyResponse.value().notBefore().toString()));
     *   });
     * </pre>
     *
     * @param key The {@link KeyBase base key} object with updated properties.
     * @param keyOperations The updated key operations to associate with the key.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @throws ResourceNotFoundException when a key with {@link KeyBase#name() name} and {@link KeyBase#version() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link KeyBase#name() name} or {@link KeyBase#version() version} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link KeyBase updated key}.
     */
    public Mono<Response<Key>> updateKey(KeyBase key, KeyOperation... keyOperations) {
        Objects.requireNonNull(key, "The key input parameter cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
                .tags(key.tags())
                .keyOps(Arrays.asList(keyOperations))
                .keyAttributes(new KeyRequestAttributes(key));

        return service.updateKey(endpoint, key.name(), key.version(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Updating key - {}",  key.name()))
                .doOnSuccess(response -> logger.logAsInfo("Updated key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to update key - {}", key.name(), error));
    }

    /**
     * Deletes a key of any type from the key vault. If soft-delete is enabled on the key vault then the key is placed in the deleted state
     * and requires to be purged for permanent deletion else the key is permanently deleted. The delete operation applies to any key stored
     * in Azure Key Vault but it cannot be applied to an individual version of a key. This operation removes the cryptographic material
     * associated with the key, which means the key is not usable for Sign/Verify, Wrap/Unwrap or Encrypt/Decrypt operations. This operation
     * requires the {@code keys/delete} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the key in the Azure Key Vault. Subscribes to the call asynchronously and prints out the
     * deleted key details when a response has been received.</p>
     * <pre>
     * keyAsyncClient.deleteKey("keyName").subscribe(deletedKeyResponse -&gt;
     *   System.out.printf("Deleted Key's Recovery Id %s \n", deletedKeyResponse.value().recoveryId()));
     * </pre>
     *
     * @param name The name of the key to be deleted.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link DeletedKey deleted key}.
     */
    public Mono<Response<DeletedKey>> deleteKey(String name) {
        return service.deleteKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Deleting key - {}",  name))
                .doOnSuccess(response -> logger.logAsInfo("Deleted key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to delete key - {}", name, error));
    }

    /**
     * Gets the public part of a deleted key. The Get Deleted Key operation is applicable for soft-delete enabled vaults. This operation
     * requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Gets the deleted key from the key vault enabled for soft-delete. Subscribes to the call asynchronously and prints out the
     * deleted key details when a response has been received.</p>
     * <pre>
     * //Assuming key is deleted on a soft-delete enabled vault.
     * keyAsyncClient.getDeletedKey("keyName").subscribe(deletedKeyResponse -&gt;
     *   System.out.printf("Deleted Key with recovery Id %s \n", deletedKeyResponse.value().recoveryId()));
     * </pre>
     *
     * @param name The name of the deleted key.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link DeletedKey deleted key}.
     */
    public Mono<Response<DeletedKey>> getDeletedKey(String name) {
        return service.getDeletedKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Retrieving deleted key - {}",  name))
                .doOnSuccess(response -> logger.logAsInfo("Retrieved deleted key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to get key - {}", name, error));
    }

    /**
     * Permanently deletes the specified key without the possibility of recovery. The Purge Deleted Key operation is applicable for
     * soft-delete enabled vaults. This operation requires the {@code keys/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted key from the key vault enabled for soft-delete. Subscribes to the call asynchronously and prints out the
     * status code from the server response when a response has been received.</p>
     * <pre>
     * //Assuming key is deleted on a soft-delete enabled vault.
     * keyAsyncClient.purgeDeletedKey("deletedKeyName").subscribe(purgeResponse -&gt;
     *   System.out.printf("Purge Status response %rsaPrivateExponent \n", purgeResponse.statusCode()));
     * </pre>
     *
     * @param name The name of the deleted key.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link VoidResponse}.
     */
    public Mono<VoidResponse> purgeDeletedKey(String name) {
        return service.purgeDeletedKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Purging deleted key - {}",  name))
                .doOnSuccess(response -> logger.logAsInfo("Purged deleted key - {}", name))
                .doOnError(error -> logger.logAsWarning("Failed to purge deleted key - {}", name, error));
    }

    /**
     * Recovers the deleted key in the key vault to its latest version and can only be performed on a soft-delete enabled vault. An attempt
     * to recover an non-deleted key will return an error. Consider this the inverse of the delete operation on soft-delete enabled vaults.
     * This operation requires the {@code keys/recover} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted key from the key vault enabled for soft-delete. Subscribes to the call asynchronously and prints out the
     * recovered key details when a response has been received.</p>
     * <pre>
     * //Assuming key is deleted on a soft-delete enabled vault.
     * keyAsyncClient.recoverDeletedKey("deletedKeyName").subscribe(recoveredKeyResponse -&gt;
     *   System.out.printf("Recovered Key with name %s \n", recoveredKeyResponse.value().name()));
     * </pre>
     *
     * @param name The name of the deleted key to be recovered.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link Key recovered key}.
     */
    public Mono<Response<Key>> recoverDeletedKey(String name) {
        return service.recoverDeletedKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Recovering deleted key - {}",  name))
                .doOnSuccess(response -> logger.logAsInfo("Recovered deleted key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to recover deleted key - {}", name, error));
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
     * <p>Backs up the key from the key vault. Subscribes to the call asynchronously and prints out the
     * length of the key's backup byte array returned in the response.</p>
     * <pre>
     * keyAsyncClient.backupKey("keyName").subscribe(keyBackupResponse -&gt;
     *   System.out.printf("Key's Backup Byte array's length %s \n", keyBackupResponse.value().length));
     * </pre>
     *
     * @param name The name of the key.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the backed up key blob.
     */
    public Mono<Response<byte[]>> backupKey(String name) {
        return service.backupKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Backing up key - {}",  name))
                .doOnSuccess(response -> logger.logAsInfo("Backed up key - {}", name))
                .doOnError(error -> logger.logAsWarning("Failed to backup key - {}", name, error))
                .flatMap(base64URLResponse ->  Mono.just(new SimpleResponse<byte[]>(base64URLResponse.request(),
                base64URLResponse.statusCode(), base64URLResponse.headers(), base64URLResponse.value().value())));
    }

    /**
     * Restores a backed up key to a vault. Imports a previously backed up key into Azure Key Vault, restoring the key, its key identifier,
     * attributes and access control policies. The restore operation may be used to import a previously backed up key. The individual versions of a
     * key cannot be restored. The key is restored in its entirety with the same key name as it had when it was backed up. If the key name is not
     * available in the target Key Vault, the restore operation will be rejected. While the key name is retained during restore, the final key identifier
     * will change if the key is restored to a different vault. Restore will restore all versions and preserve version identifiers. The restore operation is subject
     * to security constraints: The target Key Vault must be owned by the same Microsoft Azure Subscription as the source Key Vault The user must have restore permission in
     * the target Key Vault. This operation requires the {@code keys/restore} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the key in the key vault from its backup. Subscribes to the call asynchronously and prints out the restored key
     * details when a response has been received.</p>
     * <pre>
     * //Pass the Key Backup Byte array to the restore operation.
     * keyAsyncClient.restoreKey(keyBackupByteArray).subscribe(keyResponse -&gt;
     *   System.out.printf("Restored Key with name %s and id %s \n", keyResponse.value().name(), keyResponse.value().id()));
     * </pre>
     *
     * @param backup The backup blob associated with the key.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link Key restored key}.
     */
    public Mono<Response<Key>> restoreKey(byte[] backup) {
        KeyRestoreRequestParameters parameters = new KeyRestoreRequestParameters().keyBackup(backup);
        return service.restoreKey(endpoint, API_VERSION, parameters, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Attempting to restore key"))
                .doOnSuccess(response -> logger.logAsInfo("Restored Key - {}", response.value().name()))
                .doOnError(error -> logger.logAsWarning("Failed to restore key - {}", error));
    }

    /**
     * List keys in the key vault. Retrieves a list of the keys in the Key Vault as JSON Web Key structures that contain the public
     * part of a stored key. The List operation is applicable to all key types and the individual key response in the flux is represented by {@link KeyBase} as only the base key identifier,
     * attributes and tags are provided in the response. The key material and individual key versions are not listed in the response. This operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get full keys with key material from this information. Convert the {@link Flux} containing {@link KeyBase base key} to
     * {@link Flux} containing {@link Key key} using {@link KeyAsyncClient#getKey(KeyBase baseKey)} within {@link Flux#flatMap(Function)}.</p>
     * <pre>
     * Flux&lt;Key&gt; keys = keyAsyncClient.listKeys()
     *   .flatMap(keyAsyncClient::getKey)
     *   .map(Response::value);
     * </pre>
     *
     * @return A {@link Flux} containing {@link KeyBase key} of all the keys in the vault.
     */
    public Flux<KeyBase> listKeys() {
        return service.getKeys(endpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Listing keys"))
                .doOnSuccess(response -> logger.logAsInfo("Listed keys"))
                .doOnError(error -> logger.logAsWarning("Failed to list keys", error))
                .flatMapMany(r -> extractAndFetchKeys(r, Context.NONE));
    }

    /**
     * Lists {@link DeletedKey deleted keys} of the key vault. The deleted keys are retrieved as JSON Web Key structures
     * that contain the public part of a deleted key. The Get Deleted Keys operation is applicable for vaults enabled
     * for soft-delete. This operation requires the {@code keys/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted keys in the key vault. Subscribes to the call asynchronously and prints out the
     * recovery id of each deleted key when a response has been received.</p>
     * <pre>
     * keyAsyncClient.listDeletedKeys().subscribe(deletedKey -&gt;
     *   System.out.printf("Deleted key's recovery Id %s \n", deletedKey.recoveryId()));
     * </pre>
     *
     * @return A {@link Flux} containing all of the {@link DeletedKey deleted keys} in the vault.
     */
    public Flux<DeletedKey> listDeletedKeys() {
        return service.getDeletedKeys(endpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Listing deleted keys"))
                .doOnSuccess(response -> logger.logAsInfo("Listed deleted keys"))
                .doOnError(error -> logger.logAsWarning("Failed to list deleted keys", error))
                .flatMapMany(r -> extractAndFetchDeletedKeys(r, Context.NONE));
    }

    /**
     * List all versions of the specified key. The individual key response in the flux is represented by {@link KeyBase}
     * as only the base key identifier, attributes and tags are provided in the response. The key material values are
     * not provided in the response. This operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get the keys with key material of all the versions from this information. Convert the {@link Flux}
     * containing {@link KeyBase base key} to {@link Flux} containing {@link Key key} using
     * {@link KeyAsyncClient#getKey(KeyBase baseKey)} within {@link Flux#flatMap(Function)}.</p>
     * <pre>
     * Flux&lt;Key&gt; keys = keyAsyncClient.listKeyVersions("keyName")
     *   .flatMap(keyAsyncClient::getKey)
     *   .map(Response::value);
     * </pre>
     *
     * @param name The name of the key.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     * @return A {@link Flux} containing {@link KeyBase key} of all the versions of the specified key in the vault. Flux is empty if key with {@code name} does not exist in key vault.
     */
    public Flux<KeyBase> listKeyVersions(String name) {
        return service.getKeyVersions(endpoint, name, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .doOnRequest(ignored -> logger.logAsInfo("Listing key versions - {}", name))
                .doOnSuccess(response -> logger.logAsInfo("Listed key versions - {}", name))
                .doOnError(error -> logger.logAsWarning(String.format("Failed to list key versions - {}", name), error))
                .flatMapMany(r -> extractAndFetchKeys(r, Context.NONE));
    }

    /**
     * Gets attributes of all the keys given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyAsyncClient#listKeys()}.
     *
     * @param nextPageLink The {@link KeyBasePage#nextLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link KeyBase key} from the next page of results.
     */
    private Flux<KeyBase> listKeysNext(String nextPageLink, Context context) {
        return service.getKeys(endpoint, nextPageLink, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(r -> extractAndFetchKeys(r, context));
    }

    private Publisher<KeyBase> extractAndFetchKeys(PagedResponse<KeyBase> page, Context context) {
        return ImplUtils.extractAndFetch(page, context, this::listKeysNext);
    }

    /**
     * Gets attributes of all the keys given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyAsyncClient#listDeletedKeys()}.
     *
     * @param nextPageLink The {@link DeletedKeyPage#nextLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link KeyBase key} from the next page of results.
     */
    private Flux<DeletedKey> listDeletedKeysNext(String nextPageLink, Context context) {
        return service.getDeletedKeys(endpoint, nextPageLink, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(r -> extractAndFetchDeletedKeys(r, context));
    }

    private Publisher<DeletedKey> extractAndFetchDeletedKeys(PagedResponse<DeletedKey> page, Context context) {
        return ImplUtils.extractAndFetch(page, context, this::listDeletedKeysNext);
    }
}

