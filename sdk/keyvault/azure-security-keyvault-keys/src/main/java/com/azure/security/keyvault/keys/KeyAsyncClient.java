// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.ImportKeyOptions;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * The KeyAsyncClient provides asynchronous methods to manage {@link KeyVaultKey keys} in the Azure Key Vault. The client
 * supports creating, retrieving, updating, deleting, purging, backing up, restoring and listing the {@link KeyVaultKey keys}.
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
    private final String apiVersion;
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    static final String KEY_VAULT_SCOPE = "https://vault.azure.net/.default";
    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";

    private static final Duration DEFAULT_POLL_DURATION = Duration.ofSeconds(1);

    private final String vaultUrl;
    private final KeyService service;
    private final ClientLogger logger = new ClientLogger(KeyAsyncClient.class);


    /**
     * Creates a KeyAsyncClient that uses {@code pipeline} to service requests
     *
     * @param vaultUrl URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link KeyServiceVersion} of the service to be used when making requests.
     */
    KeyAsyncClient(URL vaultUrl, HttpPipeline pipeline, KeyServiceVersion version) {
        Objects.requireNonNull(vaultUrl,
            KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.vaultUrl = vaultUrl.toString();
        this.service = RestProxy.create(KeyService.class, pipeline);
        apiVersion = version.getVersion();
    }

    /**
     * Get the vault endpoint url to which service requests are sent to.
     * @return the vault endpoint url
     */
    public String getVaultUrl() {
        return vaultUrl;
    }

    Duration getPollDuration() {
        return DEFAULT_POLL_DURATION;
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
     * @return A {@link Mono} containing the {@link KeyVaultKey created key}.
     * @throws ResourceModifiedException if {@code name} or {@code keyType} is null.
     * @throws HttpResponseException if {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> createKey(String name, KeyType keyType) {
        try {
            return withContext(context -> createKeyWithResponse(name, keyType, context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @param createKeyOptions The key configuration object containing information about the key being created.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey
     *     created key}.
     * @throws ResourceModifiedException if {@code name} or {@code keyType} is null.
     * @throws HttpResponseException if {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> createKeyWithResponse(CreateKeyOptions createKeyOptions) {
        try {
            return withContext(context -> createKeyWithResponse(createKeyOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> createKeyWithResponse(String name, KeyType keyType, Context context) {
        KeyRequestParameters parameters = new KeyRequestParameters().setKty(keyType);
        return service.createKey(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Creating key - {}", name))
            .doOnSuccess(response -> logger.info("Created key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create key - {}", name, error));
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link CreateKeyOptions} is required. The {@link CreateKeyOptions#getExpiresOn() expires} and {@link
     * CreateKeyOptions#getNotBefore() notBefore} values are optional. The {@link CreateKeyOptions#isEnabled() enabled}
     * field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link CreateKeyOptions#getKeyType() keyType} indicates the type of key to create. Possible values include:
     * {@link KeyType#EC EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM}
     * and {@link KeyType#OCT OCT}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new Rsa key which activates in one day and expires in one year. Subscribes to the call
     * asynchronously and prints out the newly created key details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.createKey#keyCreateOptions}
     *
     * @param createKeyOptions The key configuration object containing information about the key being created.
     * @return A {@link Mono} containing the {@link KeyVaultKey created key}.
     * @throws NullPointerException if {@code keyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code keyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> createKey(CreateKeyOptions createKeyOptions) {
        try {
            return createKeyWithResponse(createKeyOptions).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> createKeyWithResponse(CreateKeyOptions createKeyOptions, Context context) {
        Objects.requireNonNull(createKeyOptions, "The key create options parameter cannot be null.");
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = new KeyRequestParameters()
            .setKty(createKeyOptions.getKeyType())
            .setKeyOps(createKeyOptions.getKeyOperations())
            .setKeyAttributes(new KeyRequestAttributes(createKeyOptions));
        return service.createKey(vaultUrl, createKeyOptions.getName(), apiVersion, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Creating key - {}", createKeyOptions.getName()))
            .doOnSuccess(response -> logger.info("Created key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create key - {}", createKeyOptions.getName(), error));
    }

    /**
     * Creates a new Rsa key and stores it in the key vault. The create Rsa key operation can be used to create any Rsa
     * key type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It
     * requires the {@code keys/create} permission.
     *
     * <p>The {@link CreateRsaKeyOptions} is required. The {@link CreateRsaKeyOptions#getKeySize() keySize} can be
     * optionally specified. The {@link CreateRsaKeyOptions#getExpiresOn() expires} and
     * {@link CreateRsaKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateRsaKeyOptions#isEnabled() enabled} field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link CreateRsaKeyOptions#getKeyType() keyType} indicates the type of key to create. Possible values
     * include: {@link KeyType#RSA RSA} and {@link KeyType#RSA_HSM RSA-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new RSA key with size 2048 which activates in one day and expires in one year. Subscribes to the
     * call asynchronously and prints out the newly created key details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.createRsaKey#RsaKeyCreateOptions}
     *
     * @param createRsaKeyOptions The key configuration object containing information about the rsa key being
     *     created.
     * @return A {@link Mono} containing the {@link KeyVaultKey created key}.
     * @throws NullPointerException if {@code rsaKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code rsaKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> createRsaKey(CreateRsaKeyOptions createRsaKeyOptions) {
        try {
            return createRsaKeyWithResponse(createRsaKeyOptions).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new Rsa key and stores it in the key vault. The create Rsa key operation can be used to create any Rsa
     * key type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It
     * requires the {@code keys/create} permission.
     *
     * <p>The {@link CreateRsaKeyOptions} is required. The {@link CreateRsaKeyOptions#getKeySize() keySize} can be
     * optionally specified. The {@link CreateRsaKeyOptions#getExpiresOn() expires} and
     * {@link CreateRsaKeyOptions#getNotBefore() notBefore} values are optional. The {@link
     * CreateRsaKeyOptions#isEnabled() enabled} field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p>The {@link CreateRsaKeyOptions#getKeyType() keyType} indicates the type of key to create. Possible values
     * include: {@link KeyType#RSA RSA} and {@link KeyType#RSA_HSM RSA-HSM}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.createRsaKeyWithResponse#RsaKeyCreateOptions}
     *
     * @param createRsaKeyOptions The key configuration object containing information about the rsa key being
     *     created.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey
     *     created key}.
     * @throws NullPointerException if {@code rsaKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code rsaKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> createRsaKeyWithResponse(CreateRsaKeyOptions createRsaKeyOptions) {
        try {
            return withContext(context -> createRsaKeyWithResponse(createRsaKeyOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> createRsaKeyWithResponse(CreateRsaKeyOptions createRsaKeyOptions, Context context) {
        Objects.requireNonNull(createRsaKeyOptions, "The Rsa key options parameter cannot be null.");
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = new KeyRequestParameters()
            .setKty(createRsaKeyOptions.getKeyType())
            .setKeySize(createRsaKeyOptions.getKeySize())
            .setKeyOps(createRsaKeyOptions.getKeyOperations())
            .setKeyAttributes(new KeyRequestAttributes(createRsaKeyOptions));
        return service.createKey(vaultUrl, createRsaKeyOptions.getName(), apiVersion, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Creating Rsa key - {}", createRsaKeyOptions.getName()))
            .doOnSuccess(response -> logger.info("Created Rsa key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create Rsa key - {}", createRsaKeyOptions.getName(), error));
    }

    /**
     * Creates a new Ec key and stores it in the key vault. The create Ec key operation can be used to create any Ec key
     * type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires
     * the {@code keys/create} permission.
     *
     * <p>The {@link CreateEcKeyOptions} parameter is required. The {@link CreateEcKeyOptions#getCurveName() key curve} can be
     * optionally specified. If not specified, default value of {@link KeyCurveName#P_256 P-256} is used by Azure Key
     * Vault. The {@link CreateEcKeyOptions#getExpiresOn() expires} and {@link CreateEcKeyOptions#getNotBefore() notBefore}
     * values are optional. The {@link CreateEcKeyOptions#isEnabled() enabled} field is set to true by Azure Key Vault,
     * if not specified.</p>
     *
     * <p>The {@link CreateEcKeyOptions#getKeyType() keyType} indicates the type of key to create. Possible values include:
     * {@link KeyType#EC EC} and {@link KeyType#EC_HSM EC-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key with P-384 web key curve. The key activates in one day and expires in one year.
     * Subscribes to the call asynchronously and prints out the newly created ec key details when a response has been
     * received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.createEcKey#EcKeyCreateOptions}
     *
     * @param createEcKeyOptions The key options object containing information about the ec key being created.
     * @return A {@link Mono} containing the {@link KeyVaultKey created key}.
     * @throws NullPointerException if {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code ecKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> createEcKey(CreateEcKeyOptions createEcKeyOptions) {
        try {
            return createEcKeyWithResponse(createEcKeyOptions).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new Ec key and stores it in the key vault. The create Ec key operation can be used to create any Ec key
     * type in key vault. If the named key already exists, Azure Key Vault creates a new version of the key. It requires
     * the {@code keys/create} permission.
     *
     * <p>The {@link CreateEcKeyOptions} parameter is required. The {@link CreateEcKeyOptions#getCurveName() key curve} can be
     * optionally specified. If not specified, default value of {@link KeyCurveName#P_256 P-256} is used by Azure Key
     * Vault. The {@link CreateEcKeyOptions#getExpiresOn() expires} and {@link CreateEcKeyOptions#getNotBefore() notBefore}
     * values are optional. The {@link CreateEcKeyOptions#isEnabled() enabled} field is set to true by Azure Key Vault, if
     * not specified.</p>
     *
     * <p>The {@link CreateEcKeyOptions#getKeyType() keyType} indicates the type of key to create. Possible values include:
     * {@link KeyType#EC EC} and {@link KeyType#EC_HSM EC-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new EC key with P-384 web key curve. The key activates in one day and expires in one year.
     * Subscribes to the call asynchronously and prints out the newly created ec key details when a response has been
     * received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.createEcKeyWithResponse#EcKeyCreateOptions}
     *
     * @param createEcKeyOptions The key options object containing information about the ec key being created.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey
     *     created key}.
     * @throws NullPointerException if {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException if {@code ecKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> createEcKeyWithResponse(CreateEcKeyOptions createEcKeyOptions) {
        try {
            return withContext(context -> createEcKeyWithResponse(createEcKeyOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> createEcKeyWithResponse(CreateEcKeyOptions createEcKeyOptions, Context context) {
        Objects.requireNonNull(createEcKeyOptions, "The Ec key options cannot be null.");
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = new KeyRequestParameters()
            .setKty(createEcKeyOptions.getKeyType())
            .setCurve(createEcKeyOptions.getCurveName())
            .setKeyOps(createEcKeyOptions.getKeyOperations())
            .setKeyAttributes(new KeyRequestAttributes(createEcKeyOptions));
        return service.createKey(vaultUrl, createEcKeyOptions.getName(), apiVersion, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Creating Ec key - {}", createEcKeyOptions.getName()))
            .doOnSuccess(response -> logger.info("Created Ec key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create Ec key - {}", createEcKeyOptions.getName(), error));
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
     * {@codesnippet com.azure.security.keyvault.keys.keyasyncclient.importKey#string-jsonwebkey}
     *
     * @param name The name for the imported key.
     * @param keyMaterial The Json web key being imported.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey
     *     imported key}.
     * @throws HttpResponseException if {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> importKey(String name, JsonWebKey keyMaterial) {
        try {
            return withContext(context -> importKeyWithResponse(name, keyMaterial, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> importKeyWithResponse(String name, JsonWebKey keyMaterial, Context context) {
        KeyImportRequestParameters parameters = new KeyImportRequestParameters().setKey(keyMaterial);
        return service.importKey(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Importing key - {}", name))
            .doOnSuccess(response -> logger.info("Imported key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to import key - {}", name, error));
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
     * <p>Imports a new key into key vault. Subscribes to the call asynchronously and prints out the newly imported key
     * details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.keyasyncclient.importKey#options}
     *
     * @param importKeyOptions The key import configuration object containing information about the json web key
     *     being imported.
     * @return A {@link Mono} containing the {@link KeyVaultKey imported key}.
     * @throws NullPointerException if {@code keyImportOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> importKey(ImportKeyOptions importKeyOptions) {
        try {
            return importKeyWithResponse(importKeyOptions).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Imports an externally created key and stores it in key vault. The import key operation may be used to import any
     * key type into the Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the
     * key. This operation requires the {@code keys/import} permission.
     *
     * <p>The {@code keyImportOptions} is required and its fields {@link ImportKeyOptions#getName() name} and {@link
     * ImportKeyOptions#getKey() key material} cannot be null. The {@link ImportKeyOptions#getExpiresOn() expires} and
     * {@link ImportKeyOptions#getNotBefore() notBefore} values in {@code keyImportOptions} are optional. If not specified,
     * no values are set for the fields. The {@link ImportKeyOptions#isEnabled() enabled}
     * field is set to true and the {@link ImportKeyOptions#isHardwareProtected() hsm} field is set to false by Azure Key Vault, if they
     * are not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new key into key vault. Subscribes to the call asynchronously and prints out the newly imported key
     * details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.keyasyncclient.importKeyWithResponse#options-response}
     *
     * @param importKeyOptions The key import configuration object containing information about the json web key
     *     being imported.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey
     *     imported key}.
     * @throws NullPointerException if {@code keyImportOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> importKeyWithResponse(ImportKeyOptions importKeyOptions) {
        try {
            return withContext(context -> importKeyWithResponse(importKeyOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> importKeyWithResponse(ImportKeyOptions importKeyOptions, Context context) {
        Objects.requireNonNull(importKeyOptions, "The key import configuration parameter cannot be null.");
        context = context == null ? Context.NONE : context;
        KeyImportRequestParameters parameters = new KeyImportRequestParameters()
            .setKey(importKeyOptions.getKey())
            .setHsm(importKeyOptions.isHardwareProtected())
            .setKeyAttributes(new KeyRequestAttributes(importKeyOptions));
        return service.importKey(vaultUrl, importKeyOptions.getName(), apiVersion, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Importing key - {}", importKeyOptions.getName()))
            .doOnSuccess(response -> logger.info("Imported key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to import key - {}", importKeyOptions.getName(), error));
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
     * @return A {@link Mono} containing the requested {@link KeyVaultKey key}.
     * The content of the key is null if both {@code name} and {@code version} are null or empty.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault or
     * an empty/null {@code name} and a non null/empty {@code version} is provided.
     * @throws HttpResponseException if a valid {@code name} and a non null/empty {@code version} is specified.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> getKey(String name, String version) {
        try {
            return getKeyWithResponse(name, version).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     *     {@link KeyVaultKey key}. The content of the key is null if both {@code name} and {@code version}
     *     are null or empty.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault or
     * an empty/null {@code name} and a non null/empty {@code version} is provided.
     * @throws HttpResponseException if a valid {@code name} and a non null/empty {@code version} is specified.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> getKeyWithResponse(String name, String version) {
        try {
            return withContext(context -> getKeyWithResponse(name, version == null ? "" : version, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> getKeyWithResponse(String name, String version, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getKey(vaultUrl, name, version, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
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
     * @return A {@link Mono} containing the requested {@link KeyVaultKey key}. The content of the key is null
     * if {@code name} is null or empty.
     * @throws ResourceNotFoundException when a key with non null/empty {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException if a non null/empty and an invalid {@code name} is specified.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> getKey(String name) {
        try {
            return getKeyWithResponse(name, "").flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @param keyProperties The {@link KeyProperties key properties} object with updated properties.
     * @param keyOperations The updated key operations to associate with the key.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link
     *     KeyVaultKey updated key}.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @throws ResourceNotFoundException when a key with {@link KeyProperties#getName() name} and {@link KeyProperties#getVersion()
     *     version} doesn't exist in the key vault.
     * @throws HttpResponseException if {@link KeyProperties#getName() name} or {@link KeyProperties#getVersion() version} is empty
     *     string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> updateKeyPropertiesWithResponse(KeyProperties keyProperties, KeyOperation... keyOperations) {
        try {
            return withContext(context -> updateKeyPropertiesWithResponse(keyProperties, context, keyOperations));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @param keyProperties The {@link KeyProperties key properties} object with updated properties.
     * @param keyOperations The updated key operations to associate with the key.
     * @return A {@link Mono} containing the {@link KeyVaultKey updated key}.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @throws ResourceNotFoundException when a key with {@link KeyProperties#getName() name} and {@link KeyProperties#getVersion()
     *     version} doesn't exist in the key vault.
     * @throws HttpResponseException if {@link KeyProperties#getName() name} or {@link KeyProperties#getVersion() version} is empty
     *     string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> updateKeyProperties(KeyProperties keyProperties, KeyOperation... keyOperations) {
        try {
            return updateKeyPropertiesWithResponse(keyProperties, keyOperations).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> updateKeyPropertiesWithResponse(KeyProperties keyProperties, Context context, KeyOperation... keyOperations) {
        Objects.requireNonNull(keyProperties, "The key properties input parameter cannot be null.");
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = new KeyRequestParameters()
            .setTags(keyProperties.getTags())
            .setKeyAttributes(new KeyRequestAttributes(keyProperties));
        if (keyOperations.length > 0) {
            parameters.setKeyOps(Arrays.asList(keyOperations));
        }
        return service.updateKey(vaultUrl, keyProperties.getName(), keyProperties.getVersion(), apiVersion, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Updating key - {}", keyProperties.getName()))
            .doOnSuccess(response -> logger.info("Updated key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to update key - {}", keyProperties.getName(), error));
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
     * @return A {@link PollerFlux} to poll on the {@link DeletedKey deleted key} status.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<DeletedKey, Void> beginDeleteKey(String name) {
        return new PollerFlux<>(getPollDuration(),
            activationOperation(name),
            createPollOperation(name),
            (context, firstResponse) -> Mono.empty(),
            (context) -> Mono.empty());
    }

    private Function<PollingContext<DeletedKey>, Mono<DeletedKey>> activationOperation(String name) {
        return (pollingContext) -> withContext(context -> deleteKeyWithResponse(name, context))
            .flatMap(deletedKeyResponse -> Mono.just(deletedKeyResponse.getValue()));
    }

    /*
    Polling operation to poll on create delete key operation status.
    */
    private Function<PollingContext<DeletedKey>, Mono<PollResponse<DeletedKey>>> createPollOperation(String keyName) {
        return pollingContext ->
            withContext(context -> service.getDeletedKeyPoller(vaultUrl, keyName, apiVersion,
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                .flatMap(deletedKeyResponse -> {
                    if (deletedKeyResponse.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                        return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                                pollingContext.getLatestResponse().getValue())));
                    }
                    return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, deletedKeyResponse.getValue())));
                })
                // This means either vault has soft-delete disabled or permission is not granted for the get deleted key operation.
                // In both cases deletion operation was successful when activation operation succeeded before reaching here.
                .onErrorReturn(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollingContext.getLatestResponse().getValue()));
    }

    Mono<Response<DeletedKey>> deleteKeyWithResponse(String name, Context context) {
        return service.deleteKey(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
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
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeletedKey> getDeletedKey(String name) {
        try {
            return getDeletedKeyWithResponse(name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeletedKey>> getDeletedKeyWithResponse(String name) {
        try {
            return withContext(context -> getDeletedKeyWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DeletedKey>> getDeletedKeyWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getDeletedKey(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
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
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> purgeDeletedKey(String name) {
        try {
            return purgeDeletedKeyWithResponse(name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> purgeDeletedKeyWithResponse(String name) {
        try {
            return withContext(context -> purgeDeletedKeyWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> purgeDeletedKeyWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        return service.purgeDeletedKey(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
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
     * @return A {@link PollerFlux} to poll on the {@link KeyVaultKey recovered key} status.
     * @throws ResourceNotFoundException when a key with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<KeyVaultKey, Void> beginRecoverDeletedKey(String name) {
        return new PollerFlux<>(getPollDuration(),
            recoverActivationOperation(name),
            createRecoverPollOperation(name),
            (context, firstResponse) -> Mono.empty(),
            context -> Mono.empty());
    }

    private Function<PollingContext<KeyVaultKey>, Mono<KeyVaultKey>> recoverActivationOperation(String name) {
        return (pollingContext) -> withContext(context -> recoverDeletedKeyWithResponse(name, context))
            .flatMap(keyResponse -> Mono.just(keyResponse.getValue()));
    }

    /*
    Polling operation to poll on create delete key operation status.
    */
    private Function<PollingContext<KeyVaultKey>, Mono<PollResponse<KeyVaultKey>>> createRecoverPollOperation(String keyName) {
        return pollingContext ->
            withContext(context -> service.getKeyPoller(vaultUrl, keyName, "", apiVersion,
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                .flatMap(keyResponse -> {
                    if (keyResponse.getStatusCode() == 404) {
                        return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                                pollingContext.getLatestResponse().getValue())));
                    }
                    return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            keyResponse.getValue())));
                })
                // This means permission is not granted for the get deleted key operation.
                // In both cases deletion operation was successful when activation operation succeeded before reaching here.
                .onErrorReturn(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                        pollingContext.getLatestResponse().getValue()));
    }

    Mono<Response<KeyVaultKey>> recoverDeletedKeyWithResponse(String name, Context context) {
        return service.recoverDeletedKey(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
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
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<byte[]> backupKey(String name) {
        try {
            return backupKeyWithResponse(name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @throws HttpResponseException when a key with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<byte[]>> backupKeyWithResponse(String name) {
        try {
            return withContext(context -> backupKeyWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<byte[]>> backupKeyWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        return service.backupKey(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
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
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.restoreKeyBackup#byte}
     *
     * @param backup The backup blob associated with the key.
     * @return A {@link Mono} containing the {@link KeyVaultKey restored key}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> restoreKeyBackup(byte[] backup) {
        try {
            return restoreKeyBackupWithResponse(backup).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.restoreKeyBackupWithResponse#byte}
     *
     * @param backup The backup blob associated with the key.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey
     *     restored key}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> restoreKeyBackupWithResponse(byte[] backup) {
        try {
            return withContext(context -> restoreKeyBackupWithResponse(backup, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> restoreKeyBackupWithResponse(byte[] backup, Context context) {
        context = context == null ? Context.NONE : context;
        KeyRestoreRequestParameters parameters = new KeyRestoreRequestParameters().setKeyBackup(backup);
        return service.restoreKey(vaultUrl, apiVersion, parameters, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Attempting to restore key"))
            .doOnSuccess(response -> logger.info("Restored Key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to restore key - {}", error));
    }

    /**
     * List keys in the key vault. Retrieves a list of the keys in the Key Vault as JSON Web Key structures that contain
     * the public part of a stored key. The List operation is applicable to all key types and the individual key
     * response in the flux is represented by {@link KeyProperties} as only the key identifier, attributes and tags are
     * provided in the response. The key material and individual key versions are not listed in the response. This
     * operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get full keys with key material from this information. Convert the {@link Flux} containing
     * {@link KeyProperties key properties} to {@link Flux} containing {@link KeyVaultKey key} using
     * {@link KeyAsyncClient#getKey(String, String)} within {@link Flux#flatMap(Function)}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.listKeys}
     *
     * @return A {@link PagedFlux} containing {@link KeyProperties key} of all the keys in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<KeyProperties> listPropertiesOfKeys() {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listKeysFirstPage(context)),
                continuationToken -> withContext(context -> listKeysNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<KeyProperties> listPropertiesOfKeys(Context context) {
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
        try {
            return service.getKeys(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing next keys page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.info("Listed next keys page - Page {} ", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next keys page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code
     * DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<KeyProperties>> listKeysFirstPage(Context context) {
        try {
            return service.getKeys(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing keys"))
                .doOnSuccess(response -> logger.info("Listed keys"))
                .doOnError(error -> logger.warning("Failed to list keys", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listDeletedKeysFirstPage(context)),
                continuationToken -> withContext(context -> listDeletedKeysNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
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
        try {
            return service.getDeletedKeys(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing next deleted keys page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.info("Listed next deleted keys page - Page {} ", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next deleted keys page - Page {} ", continuationToken,
                    error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code
     * DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<DeletedKey>> listDeletedKeysFirstPage(Context context) {
        try {
            return service.getDeletedKeys(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing deleted keys"))
                .doOnSuccess(response -> logger.info("Listed deleted keys"))
                .doOnError(error -> logger.warning("Failed to list deleted keys", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * List all versions of the specified key. The individual key response in the flux is represented by {@link KeyProperties}
     * as only the key identifier, attributes and tags are provided in the response. The key material values are
     * not provided in the response. This operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get the keys with key material of all the versions from this information. Convert the {@link
     * Flux} containing {@link KeyProperties key properties} to {@link Flux} containing {@link KeyVaultKey key} using
     * {@link KeyAsyncClient#getKey(String, String)} within {@link Flux#flatMap(Function)}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.async.keyclient.listKeyVersions}
     *
     * @param name The name of the key.
     * @return A {@link PagedFlux} containing {@link KeyProperties key} of all the versions of the specified key in the vault.
     *     Flux is empty if key with {@code name} does not exist in key vault.
     * @throws ResourceNotFoundException when a given key {@code name} is null or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<KeyProperties> listPropertiesOfKeyVersions(String name) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listKeyVersionsFirstPage(name, context)),
                continuationToken -> withContext(context -> listKeyVersionsNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<KeyProperties> listPropertiesOfKeyVersions(String name, Context context) {
        return new PagedFlux<>(
            () -> listKeyVersionsFirstPage(name, context),
            continuationToken -> listKeyVersionsNextPage(continuationToken, context));
    }

    private Mono<PagedResponse<KeyProperties>> listKeyVersionsFirstPage(String name, Context context) {
        try {
            return service.getKeyVersions(vaultUrl, name, DEFAULT_MAX_PAGE_RESULTS, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing key versions - {}", name))
                .doOnSuccess(response -> logger.info("Listed key versions - {}", name))
                .doOnError(error -> logger.warning(String.format("Failed to list key versions - %s", name), error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return service.getKeys(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing next key versions page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.info("Listed next key versions page - Page {} ", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next key versions page - Page {} ", continuationToken,
                    error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}

