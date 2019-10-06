// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.exception.HttpRequestException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.implementation.RestProxy;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.EcKeyCreateOptions;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.KeyCreateOptions;
import com.azure.security.keyvault.keys.models.KeyImportOptions;
import com.azure.security.keyvault.keys.models.RsaKeyCreateOptions;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;
import com.azure.security.keyvault.keys.models.webkey.KeyCurveName;
import com.azure.security.keyvault.keys.models.webkey.KeyOperation;
import com.azure.security.keyvault.keys.models.webkey.KeyType;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.azure.core.implementation.util.FluxUtil.withContext;

/**
 * The KeyAsyncClient provides asynchronous methods to manage {@link Key keys} in the Azure Key Vault. The client
 * supports creating, retrieving, updating, deleting, purging, backing up, restoring and listing the {@link Key keys}.
 * The client also supports listing {@link DeletedKey deleted keys} for a soft-delete enabled Azure Key Vault.
 *
 * <p><strong>Samples to construct the async client</strong></p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.instantiation}
 *
 * @see KeyClientBuilder
 * @see PagedFlux
 */
@ServiceClient(builder = KeyClientBuilder.class, isAsync = true, serviceInterfaces = KeyService.class)
public final class KeyAsyncClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    static final String KEY_VAULT_SCOPE = "https://vault.azure.net/.default";

    private final String endpoint;
    private final KeyService service;
    private final ClientLogger logger = new ClientLogger(KeyAsyncClient.class);


    /**
     * Creates a KeyAsyncClient that uses {@code pipeline} to service requests
     *
     * @param endpoint URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    KeyAsyncClient(URL endpoint, HttpPipeline pipeline) {
        Objects.requireNonNull(endpoint,
            KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.endpoint = endpoint.toString();
        this.service = RestProxy.create(KeyService.class, pipeline);
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link KeyType keyType} indicates the type of key to create. Possible values include: {@link KeyType#EC
     * EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM} and
     * {@link KeyType#OCT OCT}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key. Subscribes to the call asynchronously and prints out the newly created key details when
     * a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.createKey#string-keyType}
     *
     * @param name The name of the key being created.
     * @param keyType The type of key to create. For valid values, see {@link KeyType KeyType}.
     * @return A {@link Mono} containing the {@link Key created key}.
     * @throws ResourceModifiedException if {@code name} or {@code keyType} is null.
     * @throws HttpRequestException if {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Key> createKey(String name, KeyType keyType) {
        return withContext(context -> createKeyWithResponse(name, keyType, context)).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link KeyType keyType} indicates the type of key to create. Possible values include: {@link KeyType#EC
     * EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM} and
     * {@link KeyType#OCT OCT}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key. Subscribes to the call asynchronously and prints out the newly created key details when
     * a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.createKeyWithResponse#keyCreateOptions}
     *
     * @param keyCreateOptions The key configuration object containing information about the key being created.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link Key
     *     created key}.
     * @throws ResourceModifiedException if {@code name} or {@code keyType} is null.
     * @throws HttpRequestException if {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Key>> createKeyWithResponse(KeyCreateOptions keyCreateOptions) {
        return withContext(context -> createKeyWithResponse(keyCreateOptions, context));
    }

    Mono<Response<Key>> createKeyWithResponse(String name, KeyType keyType, Context context) {
        KeyRequestParameters parameters = new KeyRequestParameters().setKty(keyType);
        return service.createKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
            context)
            .doOnRequest(ignored -> logger.info("Creating key - {}", name))
            .doOnSuccess(response -> logger.info("Created key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create key - {}", name, error));
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link KeyCreateOptions} is required. The {@link KeyCreateOptions#getExpires() expires} and {@link
     * KeyCreateOptions#notBefore() notBefore} values are optional. The {@link KeyCreateOptions#setEnabled(Boolean) enabled}
     * field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link KeyCreateOptions#keyType() keyType} indicates the type of key to create. Possible values include:
     * {@link KeyType#EC EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM}
     * and {@link KeyType#OCT OCT}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new Rsa key which activates in one day and expires in one year. Subscribes to the call
     * asynchronously and prints out the newly created key details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.createKey#keyCreateOptions}
     *
     * @param keyCreateOptions The key configuration object containing information about the key being created.
     * @return A {@link Mono} containing the {@link Key created key}.
     * @throws NullPointerException if {@code keyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code keyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Key> createKey(KeyCreateOptions keyCreateOptions) {
        return createKeyWithResponse(keyCreateOptions).flatMap(FluxUtil::toMono);
    }

    Mono<Response<Key>> createKeyWithResponse(KeyCreateOptions keyCreateOptions, Context context) {
        Objects.requireNonNull(keyCreateOptions, "The key create options parameter cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
            .setKty(keyCreateOptions.keyType())
            .setKeyOps(keyCreateOptions.keyOperations())
            .setKeyAttributes(new KeyRequestAttributes(keyCreateOptions));
        return service.createKey(endpoint, keyCreateOptions.getName(), API_VERSION, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Creating key - {}", keyCreateOptions.getName()))
            .doOnSuccess(response -> logger.info("Created key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create key - {}", keyCreateOptions.getName(), error));
    }

    /**
     * Creates a new Rsa key and stores it in the key vault. The create Rsa key operation can be used to create any Rsa
     * key type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It
     * requires the {@code keys/create} permission.
     *
     * <p>The {@link RsaKeyCreateOptions} is required. The {@link RsaKeyCreateOptions#getKeySize() keySize} can be
     * optionally specified. The {@link RsaKeyCreateOptions#getExpires() expires} and
     * {@link RsaKeyCreateOptions#notBefore() notBefore} values are optional. The
     * {@link RsaKeyCreateOptions#setEnabled(Boolean) enabled} field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link RsaKeyCreateOptions#keyType() keyType} indicates the type of key to create. Possible values
     * include: {@link KeyType#RSA RSA} and {@link KeyType#RSA_HSM RSA-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new RSA key with size 2048 which activates in one day and expires in one year. Subscribes to the
     * call asynchronously and prints out the newly created key details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.createRsaKey#RsaKeyCreateOptions}
     *
     * @param rsaKeyCreateOptions The key configuration object containing information about the rsa key being
     *     created.
     * @return A {@link Mono} containing the {@link Key created key}.
     * @throws NullPointerException if {@code rsaKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code rsaKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Key> createRsaKey(RsaKeyCreateOptions rsaKeyCreateOptions) {
        return createRsaKeyWithResponse(rsaKeyCreateOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new Rsa key and stores it in the key vault. The create Rsa key operation can be used to create any Rsa
     * key type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It
     * requires the {@code keys/create} permission.
     *
     * <p>The {@link RsaKeyCreateOptions} is required. The {@link RsaKeyCreateOptions#getKeySize() keySize} can be
     * optionally specified. The {@link RsaKeyCreateOptions#getExpires() expires} and
     * {@link RsaKeyCreateOptions#notBefore() notBefore} values are optional. The {@link
     * RsaKeyCreateOptions#setEnabled(Boolean) enabled} field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link RsaKeyCreateOptions#keyType() keyType} indicates the type of key to create. Possible values
     * include: {@link KeyType#RSA RSA} and {@link KeyType#RSA_HSM RSA-HSM}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.createRsaKeyWithResponse#RsaKeyCreateOptions}
     *
     * @param rsaKeyCreateOptions The key configuration object containing information about the rsa key being
     *     created.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link Key
     *     created key}.
     * @throws NullPointerException if {@code rsaKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code rsaKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Key>> createRsaKeyWithResponse(RsaKeyCreateOptions rsaKeyCreateOptions) {
        return withContext(context -> createRsaKeyWithResponse(rsaKeyCreateOptions, context));
    }

    Mono<Response<Key>> createRsaKeyWithResponse(RsaKeyCreateOptions rsaKeyCreateOptions, Context context) {
        Objects.requireNonNull(rsaKeyCreateOptions, "The Rsa key options parameter cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
            .setKty(rsaKeyCreateOptions.keyType())
            .setKeySize(rsaKeyCreateOptions.getKeySize())
            .setKeyOps(rsaKeyCreateOptions.keyOperations())
            .setKeyAttributes(new KeyRequestAttributes(rsaKeyCreateOptions));
        return service.createKey(endpoint, rsaKeyCreateOptions.getName(), API_VERSION, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Creating Rsa key - {}", rsaKeyCreateOptions.getName()))
            .doOnSuccess(response -> logger.info("Created Rsa key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create Rsa key - {}", rsaKeyCreateOptions.getName(), error));
    }

    /**
     * Creates a new Ec key and stores it in the key vault. The create Ec key operation can be used to create any Ec key
     * type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires
     * the {@code keys/create} permission.
     *
     * <p>The {@link EcKeyCreateOptions} parameter is required. The {@link EcKeyCreateOptions#getCurve() key curve} can be
     * optionally specified. If not specified, default value of {@link KeyCurveName#P_256 P-256} is used by Azure Key
     * Vault. The {@link EcKeyCreateOptions#getExpires() expires} and {@link EcKeyCreateOptions#notBefore() notBefore}
     * values are optional. The {@link EcKeyCreateOptions#setEnabled(Boolean) enabled} field is set to true by Azure Key Vault,
     * if not specified.</p>
     *
     * <p>The {@link EcKeyCreateOptions#keyType() keyType} indicates the type of key to create. Possible values include:
     * {@link KeyType#EC EC} and {@link KeyType#EC_HSM EC-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key with P-384 web key curve. The key activates in one day and expires in one year.
     * Subscribes to the call asynchronously and prints out the newly created ec key details when a response has been
     * received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.createEcKey#EcKeyCreateOptions}
     *
     * @param ecKeyCreateOptions The key options object containing information about the ec key being created.
     * @return A {@link Mono} containing the {@link Key created key}.
     * @throws NullPointerException if {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code ecKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Key> createEcKey(EcKeyCreateOptions ecKeyCreateOptions) {
        return createEcKeyWithResponse(ecKeyCreateOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new Ec key and stores it in the key vault. The create Ec key operation can be used to create any Ec key
     * type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires
     * the {@code keys/create} permission.
     *
     * <p>The {@link EcKeyCreateOptions} parameter is required. The {@link EcKeyCreateOptions#getCurve() key curve} can be
     * optionally specified. If not specified, default value of {@link KeyCurveName#P_256 P-256} is used by Azure Key
     * Vault. The {@link EcKeyCreateOptions#getExpires() expires} and {@link EcKeyCreateOptions#notBefore() notBefore}
     * values are optional. The {@link EcKeyCreateOptions#setEnabled(Boolean) enabled} field is set to true by Azure Key Vault, if
     * not specified.</p>
     *
     * <p>The {@link EcKeyCreateOptions#keyType() keyType} indicates the type of key to create. Possible values include:
     * {@link KeyType#EC EC} and {@link KeyType#EC_HSM EC-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key with P-384 web key curve. The key activates in one day and expires in one year.
     * Subscribes to the call asynchronously and prints out the newly created ec key details when a response has been
     * received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.createEcKeyWithResponse#EcKeyCreateOptions}
     *
     * @param ecKeyCreateOptions The key options object containing information about the ec key being created.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link Key
     *     created key}.
     * @throws NullPointerException if {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code ecKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Key>> createEcKeyWithResponse(EcKeyCreateOptions ecKeyCreateOptions) {
        return withContext(context -> createEcKeyWithResponse(ecKeyCreateOptions, context));
    }

    Mono<Response<Key>> createEcKeyWithResponse(EcKeyCreateOptions ecKeyCreateOptions, Context context) {
        Objects.requireNonNull(ecKeyCreateOptions, "The Ec key options options cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
            .setKty(ecKeyCreateOptions.keyType())
            .setCurve(ecKeyCreateOptions.getCurve())
            .setKeyOps(ecKeyCreateOptions.keyOperations())
            .setKeyAttributes(new KeyRequestAttributes(ecKeyCreateOptions));
        return service.createKey(endpoint, ecKeyCreateOptions.getName(), API_VERSION, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Creating Ec key - {}", ecKeyCreateOptions.getName()))
            .doOnSuccess(response -> logger.info("Created Ec key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create Ec key - {}", ecKeyCreateOptions.getName(), error));
    }

    /**
     * Imports an externally created key and stores it in key vault. The import key operation may be used to import any
     * key type into the Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the
     * key. This operation requires the {@code keys/import} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new key into key vault. Subscribes to the call asynchronously and prints out the newly imported key
     * details when a response has been received.</p>
     *
     * <pre>
     * keyAsyncClient.importKey("keyName", jsonWebKeyToImport).subscribe(keyResponse -&gt;
     *   System.out.printf("Key is imported with name %s and id %s \n", keyResponse.value().getName(), keyResponse.value
     *   ().getId()));
     * </pre>
     *
     * @param name The name for the imported key.
     * @param keyMaterial The Json web key being imported.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link Key
     *     imported key}.
     * @throws HttpRequestException if {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Key> importKey(String name, JsonWebKey keyMaterial) {
        return withContext(context -> importKeyWithResponse(name, keyMaterial, context)).flatMap(FluxUtil::toMono);
    }

    Mono<Response<Key>> importKeyWithResponse(String name, JsonWebKey keyMaterial, Context context) {
        KeyImportRequestParameters parameters = new KeyImportRequestParameters().setKey(keyMaterial);
        return service.importKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
            context)
            .doOnRequest(ignored -> logger.info("Importing key - {}", name))
            .doOnSuccess(response -> logger.info("Imported key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to import key - {}", name, error));
    }

    /**
     * Imports an externally created key and stores it in key vault. The import key operation may be used to import any
     * key type into the Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the
     * key. This operation requires the {@code keys/import} permission.
     *
     * <p>The {@code keyImportOptions} is required and its fields {@link KeyImportOptions#getName() name} and {@link
     * KeyImportOptions#getKeyMaterial() key material} cannot be null. The {@link KeyImportOptions#getExpires() expires} and
     * {@link KeyImportOptions#getNotBefore() notBefore} values in {@code keyImportOptions} are optional. If not specified,
     * no values are set for the fields. The {@link KeyImportOptions#getEnabled() enabled} field is set to true and the
     * {@link KeyImportOptions#isHsm() hsm} field is set to false by Azure Key Vault, if they are not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new key into key vault. Subscribes to the call asynchronously and prints out the newly imported key
     * details when a response has been received.</p>
     * <pre>
     * KeyImportOptions keyImportOptions = new KeyImportOptions("keyName", jsonWebKeyToImport)
     *   .hsm(true)
     *   .setExpires(OffsetDateTime.now().plusDays(60));
     *
     * keyAsyncClient.importKey(keyImportOptions).subscribe(keyResponse -&gt;
     *   System.out.printf("Key is imported with name %s and id %s \n", keyResponse.value().getName(),
     *   keyResponse.value().getId()));
     * </pre>
     *
     * @param keyImportOptions The key import configuration object containing information about the json web key
     *     being imported.
     * @return A {@link Mono} containing the {@link Key imported key}.
     * @throws NullPointerException if {@code keyImportOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Key> importKey(KeyImportOptions keyImportOptions) {
        return importKeyWithResponse(keyImportOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Imports an externally created key and stores it in key vault. The import key operation may be used to import any
     * key type into the Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the
     * key. This operation requires the {@code keys/import} permission.
     *
     * <p>The {@code keyImportOptions} is required and its fields {@link KeyImportOptions#getName() name} and {@link
     * KeyImportOptions#getKeyMaterial() key material} cannot be null. The {@link KeyImportOptions#getExpires() expires} and
     * {@link KeyImportOptions#getNotBefore() notBefore} values in {@code keyImportOptions} are optional. If not specified,
     * no values are set for the fields. The {@link KeyImportOptions#getEnabled() enabled}
     * field is set to true and the {@link KeyImportOptions#isHsm() hsm} field is set to false by Azure Key Vault, if they
     * are not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new key into key vault. Subscribes to the call asynchronously and prints out the newly imported key
     * details when a response has been received.</p>
     *
     * <pre>
     * KeyImportOptions keyImportOptions = new KeyImportOptions("keyName", jsonWebKeyToImport)
     *   .hsm(true)
     *   .setExpires(OffsetDateTime.now().plusDays(60));
     *
     * keyAsyncClient.importKey(keyImportOptions).subscribe(keyResponse -&gt;
     *   System.out.printf("Key is imported with name %s and id %s \n", keyResponse.value().getName(),
     *   keyResponse.value().getId()));
     * </pre>
     *
     * @param keyImportOptions The key import configuration object containing information about the json web key
     *     being imported.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link Key
     *     imported key}.
     * @throws NullPointerException if {@code keyImportOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Key>> importKeyWithResponse(KeyImportOptions keyImportOptions) {
        return withContext(context -> importKeyWithResponse(keyImportOptions, context));
    }

    Mono<Response<Key>> importKeyWithResponse(KeyImportOptions keyImportOptions, Context context) {
        Objects.requireNonNull(keyImportOptions, "The key import configuration parameter cannot be null.");
        KeyImportRequestParameters parameters = new KeyImportRequestParameters()
            .setKey(keyImportOptions.getKeyMaterial())
            .setHsm(keyImportOptions.isHsm())
            .setKeyAttributes(new KeyRequestAttributes(keyImportOptions));
        return service.importKey(endpoint, keyImportOptions.getName(), API_VERSION, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Importing key - {}", keyImportOptions.getName()))
            .doOnSuccess(response -> logger.info("Imported key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to import key - {}", keyImportOptions.getName(), error));
    }

    /**
     * Gets the public part of the specified key and key version. The get key operation is applicable to all key types
     * and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the key in the key vault. Subscribes to the call asynchronously and prints out the
     * returned key details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.getKey#string-string}
     *
     * @param name The name of the key, cannot be null
     * @param version The version of the key to retrieve. If this is an empty String or null, this call is
     *     equivalent to calling {@link KeyAsyncClient#getKey(String)}, with the latest version being retrieved.
     * @return A {@link Mono} containing the requested {@link Key key}.
     * @throws ResourceNotFoundException when a key with {@code name} and {@code version} doesn't exist in the key
     *     vault.
     * @throws HttpRequestException if {@code name} or {@code version} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Key> getKey(String name, String version) {
        return getKeyWithResponse(name, version).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets the public part of the specified key and key version. The get key operation is applicable to all key types
     * and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the key in the key vault. Subscribes to the call asynchronously and prints out the
     * returned key details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.getKeyWithResponse#string-string}
     *
     * @param name The name of the key, cannot be null
     * @param version The version of the key to retrieve. If this is an empty String or null, this call is
     *     equivalent to calling {@link KeyAsyncClient#getKey(String)}, with the latest version being retrieved.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested
     *     {@link Key key}.
     * @throws ResourceNotFoundException when a key with {@code name} and {@code version} doesn't exist in the key
     *     vault.
     * @throws HttpRequestException if {@code name} or {@code version} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Key>> getKeyWithResponse(String name, String version) {
        return withContext(context -> getKeyWithResponse(name, version == null ? "" : version, context));
    }

    Mono<Response<Key>> getKeyWithResponse(String name, String version, Context context) {
        return service.getKey(endpoint, name, version, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Retrieving key - {}", name))
            .doOnSuccess(response -> logger.info("Retrieved key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to get key - {}", name, error));
    }

    /**
     * Get the public part of the latest version of the specified key from the key vault. The get key operation is
     * applicable to all key types and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the key in the key vault. Subscribes to the call asynchronously and prints out the
     * returned key details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.getKey#string}
     *
     * @param name The name of the key.
     * @return A {@link Mono} containing the requested {@link Key key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Key> getKey(String name) {
        return getKeyWithResponse(name, "").flatMap(FluxUtil::toMono);
    }

    /**
     * Get public part of the key which represents {@link KeyProperties keyProperties} from the key vault. The get key operation is
     * applicable to all key types and it requires the {@code keys/get} permission.
     *
     * <p>The list operations {@link KeyAsyncClient#listKeys()} and {@link KeyAsyncClient#listKeyVersions(String)}
     * return the {@link Flux} containing {@link KeyProperties base key} as output excluding the key material of the key.
     * This operation can then be used to get the full key with its key material from {@code keyProperties}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.getKey#KeyProperties}
     *
     * @param keyProperties The {@link KeyProperties base key} holding attributes of the key being requested.
     * @return A {@link Mono} containing the requested {@link Key key}.
     * @throws ResourceNotFoundException when a key with {@link KeyProperties#getName() name} and {@link KeyProperties#getVersion()
     *     version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link KeyProperties#getName()}  name} or {@link KeyProperties#getVersion() version} is empty
     *     string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Key> getKey(KeyProperties keyProperties) {
        return getKeyWithResponse(keyProperties).flatMap(FluxUtil::toMono);
    }

    /**
     * Get public part of the key which represents {@link KeyProperties keyProperties} from the key vault. The get key operation is
     * applicable to all key types and it requires the {@code keys/get} permission.
     *
     * <p>The list operations {@link KeyAsyncClient#listKeys()} and {@link KeyAsyncClient#listKeyVersions(String)}
     * return the {@link Flux} containing {@link KeyProperties base key} as output excluding the key material of the key.
     * This operation can then be used to get the full key with its key material from {@code keyProperties}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.getKeyWithResponse#KeyProperties}
     *
     * @param keyProperties The {@link KeyProperties base key} holding attributes of the key being requested.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested
     *     {@link Key key}.
     * @throws ResourceNotFoundException when a key with {@link KeyProperties#getName() name} and {@link KeyProperties#getVersion()
     *     version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link KeyProperties#getName()}  name} or {@link KeyProperties#getVersion() version} is empty
     *     string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Key>> getKeyWithResponse(KeyProperties keyProperties) {
        Objects.requireNonNull(keyProperties, "The Key Base parameter cannot be null.");
        return withContext(context -> getKeyWithResponse(keyProperties.getName(), keyProperties.getVersion() == null ? ""
            : keyProperties.getVersion(), context));
    }

    /**
     * Updates the attributes associated with the specified key, but not the cryptographic key material of the specified
     * key in the key vault. The update operation changes specified attributes of an existing stored key and attributes
     * that are not specified in the request are left unchanged. The cryptographic key material of a key itself cannot
     * be changed. This operation requires the {@code keys/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the key, changes its notBefore time and then updates it in the Azure Key Vault.
     * Subscribes to the call asynchronously and prints out the
     * returned key details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.updateKeyProperties#KeyProperties}
     *
     * @param key The {@link KeyProperties base key} object with updated properties.
     * @return A {@link Mono} containing the {@link KeyProperties updated key}.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @throws ResourceNotFoundException when a key with {@link KeyProperties#getName() name} and {@link KeyProperties#getVersion()
     *     version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link KeyProperties#getName() name} or {@link KeyProperties#getVersion() version} is empty
     *     string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Key> updateKeyProperties(KeyProperties key) {
        return withContext(context -> updateKeyPropertiesWithResponse(key, context).flatMap(FluxUtil::toMono));
    }

    Mono<Response<Key>> updateKeyPropertiesWithResponse(KeyProperties key, Context context) {
        Objects.requireNonNull(key, "The key input parameter cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
            .setTags(key.getTags())
            .setKeyAttributes(new KeyRequestAttributes(key));
        return service.updateKey(endpoint, key.getName(), key.getVersion(), API_VERSION, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Updating key - {}", key.getName()))
            .doOnSuccess(response -> logger.info("Updated key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to update key - {}", key.getName(), error));
    }

    /**
     * Updates the attributes and key operations associated with the specified key, but not the cryptographic key
     * material of the specified key in the key vault. The update operation changes specified attributes of an existing
     * stored key and attributes that are not specified in the request are left unchanged. The cryptographic key
     * material of a key itself cannot be changed. This operation requires the {@code keys/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the key, changes its notBefore time and then updates it in the Azure Key Vault.
     * Subscribes to the call asynchronously and prints out the returned key details when a response has been received.
     * </p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.updateKeyPropertiesWithResponse#KeyProperties-keyOperations}
     *
     * @param key The {@link KeyProperties base key} object with updated properties.
     * @param keyOperations The updated key operations to associate with the key.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link
     *     KeyProperties updated key}.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @throws ResourceNotFoundException when a key with {@link KeyProperties#getName() name} and {@link KeyProperties#getVersion()
     *     version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link KeyProperties#getName() name} or {@link KeyProperties#getVersion() version} is empty
     *     string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Key>> updateKeyPropertiesWithResponse(KeyProperties key, KeyOperation... keyOperations) {
        return withContext(context -> updateKeyPropertiesWithResponse(key, context, keyOperations));
    }

    /**
     * Updates the attributes and key operations associated with the specified key, but not the cryptographic key
     * material of the specified key in the key vault. The update operation changes specified attributes of an existing
     * stored key and attributes that are not specified in the request are left unchanged. The cryptographic key
     * material of a key itself cannot be changed. This operation requires the {@code keys/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the key, changes its notBefore time and then updates it in the Azure Key Vault.
     * Subscribes to the call asynchronously and prints out the returned key details when a response has been received.
     * </p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.updateKeyProperties#KeyProperties-keyOperations}
     *
     * @param key The {@link KeyProperties base key} object with updated properties.
     * @param keyOperations The updated key operations to associate with the key.
     * @return A {@link Mono} containing the {@link KeyProperties updated key}.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @throws ResourceNotFoundException when a key with {@link KeyProperties#getName() name} and {@link KeyProperties#getVersion()
     *     version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link KeyProperties#getName() name} or {@link KeyProperties#getVersion() version} is empty
     *     string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Key> updateKeyProperties(KeyProperties key, KeyOperation... keyOperations) {
        return updateKeyPropertiesWithResponse(key, keyOperations).flatMap(FluxUtil::toMono);
    }

    Mono<Response<Key>> updateKeyPropertiesWithResponse(KeyProperties key, Context context, KeyOperation... keyOperations) {
        Objects.requireNonNull(key, "The key input parameter cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
            .setTags(key.getTags())
            .setKeyOps(Arrays.asList(keyOperations))
            .setKeyAttributes(new KeyRequestAttributes(key));
        return service.updateKey(endpoint, key.getName(), key.getVersion(), API_VERSION, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Updating key - {}", key.getName()))
            .doOnSuccess(response -> logger.info("Updated key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to update key - {}", key.getName(), error));
    }

    /**
     * Deletes a key of any type from the key vault. If soft-delete is enabled on the key vault then the key is placed
     * in the deleted state and requires to be purged for permanent deletion else the key is permanently deleted. The
     * delete operation applies to any key stored in Azure Key Vault but it cannot be applied to an individual version
     * of a key. This operation removes the cryptographic material associated with the key, which means the key is not
     * usable for Sign/Verify, Wrap/Unwrap or Encrypt/Decrypt operations. This operation requires the
     * {@code keys/delete} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the key in the Azure Key Vault. Subscribes to the call asynchronously and prints out the deleted key
     * details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.deleteKey#string}
     *
     * @param name The name of the key to be deleted.
     * @return A {@link Mono} containing the {@link DeletedKey deleted key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeletedKey> deleteKey(String name) {
        return deleteKeyWithResponse(name).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a key of any type from the key vault. If soft-delete is enabled on the key vault then the key is placed
     * in the deleted state and requires to be purged for permanent deletion else the key is permanently deleted. The
     * delete operation applies to any key stored in Azure Key Vault but it cannot be applied to an individual version
     * of a key. This operation removes the cryptographic material associated with the key, which means the key is not
     * usable for Sign/Verify, Wrap/Unwrap or Encrypt/Decrypt operations. This operation requires the
     * {@code keys/delete} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the key in the Azure Key Vault. Subscribes to the call asynchronously and prints out the deleted key
     * details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.deleteKeyWithResponse#string}
     *
     * @param name The name of the key to be deleted.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link
     *     DeletedKey deleted key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeletedKey>> deleteKeyWithResponse(String name) {
        return withContext(context -> deleteKeyWithResponse(name, context));
    }

    Mono<Response<DeletedKey>> deleteKeyWithResponse(String name, Context context) {
        return service.deleteKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Deleting key - {}", name))
            .doOnSuccess(response -> logger.info("Deleted key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to delete key - {}", name, error));
    }

    /**
     * Gets the public part of a deleted key. The Get Deleted Key operation is applicable for soft-delete enabled
     * vaults. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Gets the deleted key from the key vault enabled for soft-delete. Subscribes to the call asynchronously and
     * prints out the deleted key details when a response has been received.</p>
     * //Assuming key is deleted on a soft-delete enabled vault.
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.getDeletedKey#string}
     *
     * @param name The name of the deleted key.
     * @return A {@link Mono} containing the {@link DeletedKey deleted key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeletedKey> getDeletedKey(String name) {
        return getDeletedKeyWithResponse(name).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets the public part of a deleted key. The Get Deleted Key operation is applicable for soft-delete enabled
     * vaults. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Gets the deleted key from the key vault enabled for soft-delete. Subscribes to the call asynchronously and
     * prints out the deleted key details when a response has been received.</p>
     *
     * //Assuming key is deleted on a soft-delete enabled vault.
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.getDeletedKeyWithResponse#string}
     *
     * @param name The name of the deleted key.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link
     *     DeletedKey deleted key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeletedKey>> getDeletedKeyWithResponse(String name) {
        return withContext(context -> getDeletedKeyWithResponse(name, context));
    }

    Mono<Response<DeletedKey>> getDeletedKeyWithResponse(String name, Context context) {
        return service.getDeletedKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Retrieving deleted key - {}", name))
            .doOnSuccess(response -> logger.info("Retrieved deleted key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to get key - {}", name, error));
    }

    /**
     * Permanently deletes the specified key without the possibility of recovery. The Purge Deleted Key operation is
     * applicable for soft-delete enabled vaults. This operation requires the {@code keys/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted key from the key vault enabled for soft-delete. Subscribes to the call asynchronously and
     * prints out the status code from the server response when a response has been received.</p>
     *
     * //Assuming key is deleted on a soft-delete enabled vault.
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.purgeDeletedKey#string}
     *
     * @param name The name of the deleted key.
     * @return An empty {@link Mono}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> purgeDeletedKey(String name) {
        return purgeDeletedKeyWithResponse(name).flatMap(FluxUtil::toMono);
    }

    /**
     * Permanently deletes the specified key without the possibility of recovery. The Purge Deleted Key operation is
     * applicable for soft-delete enabled vaults. This operation requires the {@code keys/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted key from the key vault enabled for soft-delete. Subscribes to the call asynchronously and
     * prints out the status code from the server response when a response has been received.</p>
     *
     * //Assuming key is deleted on a soft-delete enabled vault.
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.purgeDeletedKeyWithResponse#string}
     *
     * @param name The name of the deleted key.
     * @return A {@link Mono} containing a Response containing status code and HTTP headers.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> purgeDeletedKeyWithResponse(String name) {
        return withContext(context -> purgeDeletedKeyWithResponse(name, context));
    }

    Mono<Response<Void>> purgeDeletedKeyWithResponse(String name, Context context) {
        return service.purgeDeletedKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Purging deleted key - {}", name))
            .doOnSuccess(response -> logger.info("Purged deleted key - {}", name))
            .doOnError(error -> logger.warning("Failed to purge deleted key - {}", name, error));
    }

    /**
     * Recovers the deleted key in the key vault to its latest version and can only be performed on a soft-delete
     * enabled vault. An attempt to recover an non-deleted key will return an error. Consider this the inverse of the
     * delete operation on soft-delete enabled vaults. This operation requires the {@code keys/recover} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted key from the key vault enabled for soft-delete. Subscribes to the call asynchronously and
     * prints out the recovered key details when a response has been received.</p>
     * //Assuming key is deleted on a soft-delete enabled vault.
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.recoverDeletedKey#string}
     *
     * @param name The name of the deleted key to be recovered.
     * @return A {@link Mono} containing the {@link Key recovered key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Key> recoverDeletedKey(String name) {
        return recoverDeletedKeyWithResponse(name).flatMap(FluxUtil::toMono);
    }

    /**
     * Recovers the deleted key in the key vault to its latest version and can only be performed on a soft-delete
     * enabled vault. An attempt to recover an non-deleted key will return an error. Consider this the inverse of the
     * delete operation on soft-delete enabled vaults. This operation requires the {@code keys/recover} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted key from the key vault enabled for soft-delete. Subscribes to the call asynchronously and
     * prints out the recovered key details when a response has been received.</p>
     * //Assuming key is deleted on a soft-delete enabled vault.
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.recoverDeletedKeyWithResponse#string}
     *
     * @param name The name of the deleted key to be recovered.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link Key
     *     recovered key}.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Key>> recoverDeletedKeyWithResponse(String name) {
        return withContext(context -> recoverDeletedKeyWithResponse(name, context));
    }

    Mono<Response<Key>> recoverDeletedKeyWithResponse(String name, Context context) {
        return service.recoverDeletedKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context)
            .doOnRequest(ignored -> logger.info("Recovering deleted key - {}", name))
            .doOnSuccess(response -> logger.info("Recovered deleted key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to recover deleted key - {}", name, error));
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
     * <p>Backs up the key from the key vault. Subscribes to the call asynchronously and prints out the length of the
     * key's backup byte array returned in the response.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.backupKey#string}
     *
     * @param name The name of the key.
     * @return A {@link Mono} containing the backed up key blob.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<byte[]> backupKey(String name) {
        return backupKeyWithResponse(name).flatMap(FluxUtil::toMono);
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
     * <p>Backs up the key from the key vault. Subscribes to the call asynchronously and prints out the length of the
     * key's backup byte array returned in the response.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.backupKeyWithResponse#string}
     *
     * @param name The name of the key.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the backed up
     *     key blob.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<byte[]>> backupKeyWithResponse(String name) {
        return withContext(context -> backupKeyWithResponse(name, context));
    }

    Mono<Response<byte[]>> backupKeyWithResponse(String name, Context context) {
        return service.backupKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Backing up key - {}", name))
            .doOnSuccess(response -> logger.info("Backed up key - {}", name))
            .doOnError(error -> logger.warning("Failed to backup key - {}", name, error))
            .flatMap(base64URLResponse -> Mono.just(new SimpleResponse<byte[]>(base64URLResponse.getRequest(),
                base64URLResponse.getStatusCode(), base64URLResponse.getHeaders(), base64URLResponse.getValue().getValue())));
    }

    /**
     * Restores a backed up key to a vault. Imports a previously backed up key into Azure Key Vault, restoring the key,
     * its key identifier, attributes and access control policies. The restore operation may be used to import a
     * previously backed up key. The individual versions of a key cannot be restored. The key is restored in its
     * entirety with the same key name as it had when it was backed up. If the key name is not available in the target
     * Key Vault, the restore operation will be rejected. While the key name is retained during restore, the final key
     * identifier will change if the key is restored to a different vault. Restore will restore all versions and
     * preserve version identifiers. The restore operation is subject to security constraints: The target Key Vault must
     * be owned by the same Microsoft Azure Subscription as the source Key Vault The user must have restore permission
     * in the target Key Vault. This operation requires the {@code keys/restore} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the key in the key vault from its backup. Subscribes to the call asynchronously and prints out the
     * restored key details when a response has been received.</p>
     * //Pass the Key Backup Byte array to the restore operation.
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.restoreKey#byte}
     *
     * @param backup The backup blob associated with the key.
     * @return A {@link Mono} containing the {@link Key restored key}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Key> restoreKey(byte[] backup) {
        return restoreKeyWithResponse(backup).flatMap(FluxUtil::toMono);
    }

    /**
     * Restores a backed up key to a vault. Imports a previously backed up key into Azure Key Vault, restoring the key,
     * its key identifier, attributes and access control policies. The restore operation may be used to import a
     * previously backed up key. The individual versions of a key cannot be restored. The key is restored in its
     * entirety with the same key name as it had when it was backed up. If the key name is not available in the target
     * Key Vault, the restore operation will be rejected. While the key name is retained during restore, the final key
     * identifier will change if the key is restored to a different vault. Restore will restore all versions and
     * preserve version identifiers. The restore operation is subject to security constraints: The target Key Vault must
     * be owned by the same Microsoft Azure Subscription as the source Key Vault The user must have restore permission
     * in the target Key Vault. This operation requires the {@code keys/restore} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the key in the key vault from its backup. Subscribes to the call asynchronously and prints out the
     * restored key details when a response has been received.</p>
     * //Pass the Key Backup Byte array to the restore operation.
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.restoreKeyWithResponse#byte}
     *
     * @param backup The backup blob associated with the key.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link Key
     *     restored key}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Key>> restoreKeyWithResponse(byte[] backup) {
        return withContext(context -> restoreKeyWithResponse(backup, context));
    }

    Mono<Response<Key>> restoreKeyWithResponse(byte[] backup, Context context) {
        KeyRestoreRequestParameters parameters = new KeyRestoreRequestParameters().setKeyBackup(backup);
        return service.restoreKey(endpoint, API_VERSION, parameters, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context)
            .doOnRequest(ignored -> logger.info("Attempting to restore key"))
            .doOnSuccess(response -> logger.info("Restored Key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to restore key - {}", error));
    }

    /**
     * List keys in the key vault. Retrieves a list of the keys in the Key Vault as JSON Web Key structures that contain
     * the public part of a stored key. The List operation is applicable to all key types and the individual key
     * response in the flux is represented by {@link KeyProperties} as only the base key identifier, attributes and tags are
     * provided in the response. The key material and individual key versions are not listed in the response. This
     * operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get full keys with key material from this information. Convert the {@link Flux} containing
     * {@link KeyProperties base key} to {@link Flux} containing {@link Key key} using
     * {@link KeyAsyncClient#getKey(KeyProperties baseKey)} within {@link Flux#flatMap(Function)}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.listKeys}
     *
     * @return A {@link PagedFlux} containing {@link KeyProperties key} of all the keys in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<KeyProperties> listKeys() {
        return new PagedFlux<>(
            () -> withContext(context -> listKeysFirstPage(context)),
            continuationToken -> withContext(context -> listKeysNextPage(continuationToken, context)));
    }

    PagedFlux<KeyProperties> listKeys(Context context) {
        return new PagedFlux<>(
            () -> listKeysFirstPage(context),
            continuationToken -> listKeysNextPage(continuationToken, context));
    }

    /*
     * Gets attributes of all the keys given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyAsyncClient#listKeys()}.
     *
     * @param continuationToken The {@link PagedResponse#nextLink()} from a previous, successful call to one of the
     * listKeys operations.
     * @return A {@link Mono} of {@link PagedResponse<KeyProperties>} from the next page of results.
     */
    private Mono<PagedResponse<KeyProperties>> listKeysNextPage(String continuationToken, Context context) {
        return service.getKeys(endpoint, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Listing next keys page - Page {} ", continuationToken))
            .doOnSuccess(response -> logger.info("Listed next keys page - Page {} ", continuationToken))
            .doOnError(error -> logger.warning("Failed to list next keys page - Page {} ", continuationToken, error));
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code
     * DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<KeyProperties>> listKeysFirstPage(Context context) {
        return service.getKeys(endpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Listing keys"))
            .doOnSuccess(response -> logger.info("Listed keys"))
            .doOnError(error -> logger.warning("Failed to list keys", error));
    }

    /**
     * Lists {@link DeletedKey deleted keys} of the key vault. The deleted keys are retrieved as JSON Web Key structures
     * that contain the public part of a deleted key. The Get Deleted Keys operation is applicable for vaults enabled
     * for soft-delete. This operation requires the {@code keys/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted keys in the key vault. Subscribes to the call asynchronously and prints out the recovery id
     * of each deleted key when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.listDeletedKeys}
     *
     * @return A {@link PagedFlux} containing all of the {@link DeletedKey deleted keys} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DeletedKey> listDeletedKeys() {
        return new PagedFlux<>(
            () -> withContext(context -> listDeletedKeysFirstPage(context)),
            continuationToken -> withContext(context -> listDeletedKeysNextPage(continuationToken, context)));
    }

    PagedFlux<DeletedKey> listDeletedKeys(Context context) {
        return new PagedFlux<>(
            () -> listDeletedKeysFirstPage(context),
            continuationToken -> listDeletedKeysNextPage(continuationToken, context));
    }

    /*
     * Gets attributes of all the keys given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyAsyncClient#listDeletedKeys()}.
     *
     * @param continuationToken The {@link PagedResponse#nextLink()} from a previous, successful call to one of the
     * list operations.
     * @return A {@link Mono} of {@link PagedResponse<DeletedKey>} from the next page of results.
     */
    private Mono<PagedResponse<DeletedKey>> listDeletedKeysNextPage(String continuationToken, Context context) {
        return service.getDeletedKeys(endpoint, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Listing next deleted keys page - Page {} ", continuationToken))
            .doOnSuccess(response -> logger.info("Listed next deleted keys page - Page {} ", continuationToken))
            .doOnError(error -> logger.warning("Failed to list next deleted keys page - Page {} ", continuationToken,
                error));
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code
     * DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<DeletedKey>> listDeletedKeysFirstPage(Context context) {
        return service.getDeletedKeys(endpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Listing deleted keys"))
            .doOnSuccess(response -> logger.info("Listed deleted keys"))
            .doOnError(error -> logger.warning("Failed to list deleted keys", error));
    }

    /**
     * List all versions of the specified key. The individual key response in the flux is represented by {@link KeyProperties}
     * as only the base key identifier, attributes and tags are provided in the response. The key material values are
     * not provided in the response. This operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get the keys with key material of all the versions from this information. Convert the {@link
     * Flux} containing {@link KeyProperties base key} to {@link Flux} containing {@link Key key} using
     * {@link KeyAsyncClient#getKey(KeyProperties baseKey)} within {@link Flux#flatMap(Function)}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.listKeyVersions}
     *
     * @param name The name of the key.
     * @return A {@link PagedFlux} containing {@link KeyProperties key} of all the versions of the specified key in the vault.
     *     Flux is empty if key with {@code name} does not exist in key vault.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<KeyProperties> listKeyVersions(String name) {
        return new PagedFlux<>(
            () -> withContext(context -> listKeyVersionsFirstPage(name, context)),
            continuationToken -> withContext(context -> listKeyVersionsNextPage(continuationToken, context)));
    }

    PagedFlux<KeyProperties> listKeyVersions(String name, Context context) {
        return new PagedFlux<>(
            () -> listKeyVersionsFirstPage(name, context),
            continuationToken -> listKeyVersionsNextPage(continuationToken, context));
    }

    private Mono<PagedResponse<KeyProperties>> listKeyVersionsFirstPage(String name, Context context) {
        return service.getKeyVersions(endpoint, name, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Listing key versions - {}", name))
            .doOnSuccess(response -> logger.info("Listed key versions - {}", name))
            .doOnError(error -> logger.warning(String.format("Failed to list key versions - {}", name), error));
    }

    /*
     * Gets attributes of all the keys given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyAsyncClient#listKeyVersions()}.
     *
     * @param continuationToken The {@link PagedResponse#nextLink()} from a previous, successful call to one of the
     * listKeys operations.
     * @return A {@link Mono} of {@link PagedResponse<KeyProperties>} from the next page of results.
     */
    private Mono<PagedResponse<KeyProperties>> listKeyVersionsNextPage(String continuationToken, Context context) {
        return service.getKeys(endpoint, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Listing next key versions page - Page {} ", continuationToken))
            .doOnSuccess(response -> logger.info("Listed next key versions page - Page {} ", continuationToken))
            .doOnError(error -> logger.warning("Failed to list next key versions page - Page {} ", continuationToken,
                error));
    }
}

