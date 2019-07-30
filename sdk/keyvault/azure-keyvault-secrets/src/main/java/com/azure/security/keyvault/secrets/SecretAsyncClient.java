// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.RestProxy;
import com.azure.core.implementation.annotation.ReturnType;
import com.azure.core.implementation.annotation.ServiceClient;
import com.azure.core.implementation.annotation.ServiceMethod;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.Secret;
import com.azure.security.keyvault.secrets.models.SecretBase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Objects;
import java.util.function.Function;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.HttpRequestException;

import static com.azure.core.implementation.util.FluxUtil.withContext;

/**
 * The SecretAsyncClient provides asynchronous methods to manage {@link Secret secrets} in the Azure Key Vault. The client
 * supports creating, retrieving, updating, deleting, purging, backing up, restoring and listing the {@link Secret secrets}. The client
 * also supports listing {@link DeletedSecret deleted secrets} for a soft-delete enabled Azure Key Vault.
 *
 * <p><strong>Samples to construct the async client</strong></p>
 * {@codesnippet com.azure.security.keyvault.secrets.async.secretclient.construct}
 *
 * @see SecretClientBuilder
 * @see PagedFlux
 */
@ServiceClient(builder = SecretClientBuilder.class, isAsync = true, serviceInterfaces = SecretService.class)
public final class SecretAsyncClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    static final String KEY_VAULT_SCOPE = "https://vault.azure.net/.default";

    private final String endpoint;
    private final SecretService service;
    private final ClientLogger logger = new ClientLogger(SecretAsyncClient.class);

    /**
     * Creates a SecretAsyncClient that uses {@code pipeline} to service requests
     *
     * @param endpoint URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    SecretAsyncClient(URL endpoint, HttpPipeline pipeline) {
        Objects.requireNonNull(endpoint, KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.endpoint = endpoint.toString();
        this.service = RestProxy.create(SecretService.class, pipeline);
    }

    /**
     * The set operation adds a secret to the key vault. If the named secret already exists, Azure Key Vault creates
     * a new version of that secret. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@link Secret} is required. The {@link Secret#expires() expires}, {@link Secret#contentType() contentType} and
     * {@link Secret#notBefore() notBefore} values in {@code secret} are optional. The {@link Secret#enabled() enabled} field is
     * set to true by key vault, if not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new secret which activates in 1 day and expires in 1 year in the Azure Key Vault. Subscribes to the call asynchronously and
     * prints out the newly created secret details when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.setSecret#secret}
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties secret.name and secret.value must be non null.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceModifiedException if {@code secret} is malformed.
     * @throws HttpRequestException if {@link Secret#name()  name} or {@link Secret#value() value} is empty string.
     * @return A {@link Mono} containing the {@link Secret created secret}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Secret> setSecret(Secret secret) {
        return setSecretWithResponse(secret).flatMap(FluxUtil::toMono);
    }

    /**
     * The set operation adds a secret to the key vault. If the named secret already exists, Azure Key Vault creates
     * a new version of that secret. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@link Secret} is required. The {@link Secret#expires() expires}, {@link Secret#contentType() contentType} and
     * {@link Secret#notBefore() notBefore} values in {@code secret} are optional. The {@link Secret#enabled() enabled} field is
     * set to true by key vault, if not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new secret which activates in 1 day and expires in 1 year in the Azure Key Vault. Subscribes to the call asynchronously and
     * prints out the newly created secret details when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.setSecretWithResponse#secret}
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties secret.name and secret.value must be non null.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceModifiedException if {@code secret} is malformed.
     * @throws HttpRequestException if {@link Secret#name()  name} or {@link Secret#value() value} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link Secret created secret}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Secret>> setSecretWithResponse(Secret secret) {
        return withContext(context -> setSecretWithResponse(secret, context));
    }

    Mono<Response<Secret>> setSecretWithResponse(Secret secret, Context context) {
        Objects.requireNonNull(secret, "The Secret input parameter cannot be null.");
        SecretRequestParameters parameters = new SecretRequestParameters()
            .value(secret.value())
            .tags(secret.tags())
            .contentType(secret.contentType())
            .secretAttributes(new SecretRequestAttributes(secret));

        return service.setSecret(endpoint, secret.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Setting secret - {}", secret.name()))
                .doOnSuccess(response -> logger.info("Set secret - {}", response.value().name()))
                .doOnError(error -> logger.warning("Failed to set secret - {}", secret.name(), error));
    }

    /**
     * The set operation adds a secret to the key vault. If the named secret already exists, Azure Key
     * Vault creates a new version of that secret. This operation requires the {@code secrets/set}
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new secret in the key vault. Subscribes to the call asynchronously and prints out
     * the newly created secret details when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.setSecret#string-string}
     *
     * @param name The name of the secret. It is required and cannot be null.
     * @param value The value of the secret. It is required and cannot be null.
     * @return A {@link Mono} containing the {@link Secret created secret}.
     * @throws ResourceModifiedException if invalid {@code name} or {@code value} are specified.
     * @throws HttpRequestException if {@code name} or {@code value} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Secret> setSecret(String name, String value) {
        return withContext(context -> setSecretWithResponse(name, value, context)).flatMap(FluxUtil::toMono);
    }

    Mono<Response<Secret>> setSecretWithResponse(String name, String value, Context context) {
        SecretRequestParameters parameters = new SecretRequestParameters().value(value);
        return service.setSecret(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Setting secret - {}", name))
            .doOnSuccess(response -> logger.info("Set secret - {}", response.value().name()))
            .doOnError(error -> logger.warning("Failed to set secret - {}", name, error));
    }

    /**
     * Get the specified secret with specified version from the key vault. The get operation is
     * applicable to any secret stored in Azure Key Vault. This operation requires the {@code
     * secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the secret in the key vault. Subscribes to the call
     * asynchronously and prints out the
     * returned secret details when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.getSecret#string-string}
     *
     * @param name The name of the secret, cannot be null
     * @param version The version of the secret to retrieve. If this is an empty String or null, this
     * call is equivalent to calling {@link #getSecret(String)}, with the latest version being
     * retrieved.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value}
     * contains the requested {@link Secret secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} and {@code version} doesn't
     * exist in the key vault.
     * @throws HttpRequestException if {@code name}  name} or {@code version} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Secret> getSecret(String name, String version) {
        return getSecretWithResponse(name, version).flatMap(FluxUtil::toMono);
    }

    /**
     * Get the specified secret with specified version from the key vault. The get operation is
     * applicable to any secret stored in Azure Key Vault. This operation requires the {@code
     * secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the secret in the key vault. Subscribes to the call
     * asynchronously and prints out the
     * returned secret details when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.getSecretWithResponse#string-string}
     *
     * @param name The name of the secret, cannot be null
     * @param version The version of the secret to retrieve. If this is an empty String or null, this
     * call is equivalent to calling {@link #getSecret(String)}, with the latest version being
     * retrieved.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value}
     * contains the requested {@link Secret secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} and {@code version} doesn't
     * exist in the key vault.
     * @throws HttpRequestException if {@code name}  name} or {@code version} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Secret>> getSecretWithResponse(String name, String version) {
        return withContext(context -> getSecretWithResponse(name, version, context));
    }

    Mono<Response<Secret>> getSecretWithResponse(String name, String version, Context context) {
        return service.getSecret(endpoint, name, version == null ? "" : version, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignoredValue -> logger.info("Retrieving secret - {}", name))
            .doOnSuccess(response -> logger.info("Retrieved secret - {}", response.value().name()))
            .doOnError(error -> logger.warning("Failed to get secret - {}", name, error));
    }

    /**
     * Get the secret which represents {@link SecretBase secretBase} from the key vault. The get
     * operation is applicable to any secret stored in Azure Key Vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p>The list operations {@link SecretAsyncClient#listSecrets()} and {@link
     * SecretAsyncClient#listSecretVersions(String)} return
     * the {@link Flux} containing {@link SecretBase base secret} as output. This operation can then be used to get
     * the full secret with its value from {@code secretBase}. </p>
     * <p><strong>Code Samples</strong></p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.getSecret#secretBase}
     *
     * @param secretBase The {@link SecretBase base secret} secret base holding attributes of the
     * secret being requested.
     * @return A {@link Mono} containing the requested {@link Secret secret}.
     * @throws ResourceNotFoundException when a secret with {@link SecretBase#name() name} and {@link
     * SecretBase#version() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link SecretBase#name()}  name} or {@link SecretBase#version()
     * version} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Secret> getSecret(SecretBase secretBase) {
        return getSecretWithResponse(secretBase).flatMap(FluxUtil::toMono);
    }

    /**
     * Get the secret which represents {@link SecretBase secretBase} from the key vault. The get
     * operation is applicable to any secret stored in Azure Key Vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p>The list operations {@link SecretAsyncClient#listSecrets()} and {@link
     * SecretAsyncClient#listSecretVersions(String)} return
     * the {@link Flux} containing {@link SecretBase base secret} as output. This operation can then be used to get
     * the full secret with its value from {@code secretBase}. </p>
     * <p><strong>Code Samples</strong></p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.getSecretWithResponse#secretBase}
     *
     * @param secretBase The {@link SecretBase base secret} secret base holding attributes of the
     * secret being requested.
     * @return A {@link Response} whose {@link Response#value() value} contains the requested {@link
     * Secret secret}.
     * @throws ResourceNotFoundException when a secret with {@link SecretBase#name() name} and {@link
     * SecretBase#version() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link SecretBase#name()}  name} or {@link SecretBase#version()
     * version} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Secret>> getSecretWithResponse(SecretBase secretBase) {
        return withContext(context ->  getSecretWithResponse(secretBase, context));
    }

    Mono<Response<Secret>> getSecretWithResponse(SecretBase secretBase, Context context) {
        Objects.requireNonNull(secretBase, "The Secret Base parameter cannot be null.");
        return getSecretWithResponse(secretBase.name(), secretBase.version() == null ? "" : secretBase.version(), context);
    }

    /**
     * Get the latest version of the specified secret from the key vault. The get operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the {@code secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the secret in the key vault. Subscribes to the call asynchronously and prints out the
     * returned secret details when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.getSecret#string}
     *
     * @param name The name of the secret.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing the requested {@link Secret secret}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Secret> getSecret(String name) {
        return getSecretWithResponse(name, "").flatMap(FluxUtil::toMono);
    }

    /**
     * Updates the attributes associated with the specified secret, but not the value of the specified secret in the key vault. The update
     * operation changes specified attributes of an existing stored secret and attributes that are not specified in the request are left unchanged.
     * The value of a secret itself cannot be changed. This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the secret, changes its notBefore time and then updates it in the Azure Key Vault. Subscribes to the call asynchronously and prints out the
     * returned secret details when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.updateSecret#secretBase}
     *
     * <p>The {@code secret} is required and its fields {@link SecretBase#name() name} and {@link SecretBase#version() version} cannot be null.</p>
     *
     * @param secret The {@link SecretBase base secret} object with updated properties.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceNotFoundException when a secret with {@link SecretBase#name() name} and {@link SecretBase#version() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link SecretBase#name()}  name} or {@link SecretBase#version() version} is empty string.
     * @return A {@link Mono} containing the {@link SecretBase updated secret}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SecretBase> updateSecret(SecretBase secret) {
        return updateSecretWithResponse(secret).flatMap(FluxUtil::toMono);
    }

    /**
     * Updates the attributes associated with the specified secret, but not the value of the specified secret in the key vault. The update
     * operation changes specified attributes of an existing stored secret and attributes that are not specified in the request are left unchanged.
     * The value of a secret itself cannot be changed. This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the secret, changes its notBefore time and then updates it in the Azure Key Vault. Subscribes to the call asynchronously and prints out the
     * returned secret details when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.updateSecretWithResponse#secretBase}
     *
     * <p>The {@code secret} is required and its fields {@link SecretBase#name() name} and {@link SecretBase#version() version} cannot be null.</p>
     *
     * @param secret The {@link SecretBase base secret} object with updated properties.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceNotFoundException when a secret with {@link SecretBase#name() name} and {@link SecretBase#version() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link SecretBase#name()}  name} or {@link SecretBase#version() version} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link SecretBase updated secret}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SecretBase>> updateSecretWithResponse(SecretBase secret) {
        return withContext(context -> updateSecretWithResponse(secret, context));
    }

    Mono<Response<SecretBase>> updateSecretWithResponse(SecretBase secret, Context context) {
        Objects.requireNonNull(secret, "The secret input parameter cannot be null.");
        SecretRequestParameters parameters = new SecretRequestParameters()
                .tags(secret.tags())
                .contentType(secret.contentType())
                .secretAttributes(new SecretRequestAttributes(secret));

        return service.updateSecret(endpoint, secret.name(), secret.version(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Updating secret - {}", secret.name()))
                .doOnSuccess(response -> logger.info("Updated secret - {}", response.value().name()))
                .doOnError(error -> logger.warning("Failed to update secret - {}", secret.name(), error));
    }

    /**
     * Deletes a secret from the key vault. If soft-delete is enabled on the key vault then the secret is placed in the deleted state
     * and requires to be purged for permanent deletion else the secret is permanently deleted. The delete operation applies to any secret stored in Azure Key Vault but
     * it cannot be applied to an individual version of a secret. This operation requires the {@code secrets/delete} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the secret in the Azure Key Vault. Subscribes to the call asynchronously and prints out the
     * deleted secret details when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.deleteSecret#string}
     *
     * @param name The name of the secret to be deleted.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link Mono} containing the {@link DeletedSecret deleted secret}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeletedSecret> deleteSecret(String name) {
        return deleteSecretWithResponse(name).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a secret from the key vault. If soft-delete is enabled on the key vault then the secret is placed in the deleted state
     * and requires to be purged for permanent deletion else the secret is permanently deleted. The delete operation applies to any secret stored in Azure Key Vault but
     * it cannot be applied to an individual version of a secret. This operation requires the {@code secrets/delete} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the secret in the Azure Key Vault. Subscribes to the call asynchronously and prints out the
     * deleted secret details when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.deleteSecretWithResponse#string}
     *
     * @param name The name of the secret to be deleted.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link DeletedSecret deleted secret}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeletedSecret>> deleteSecretWithResponse(String name) {
        return withContext(context -> deleteSecretWithResponse(name, context));
    }

    Mono<Response<DeletedSecret>> deleteSecretWithResponse(String name, Context context) {
        return service.deleteSecret(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Deleting secret - {}", name))
                .doOnSuccess(response -> logger.info("Deleted secret - {}", response.value().name()))
                .doOnError(error -> logger.warning("Failed to delete secret - {}", name, error));
    }

    /**
     * The get deleted secret operation returns the secrets that have been deleted for a vault enabled
     * for soft-delete. This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Gets the deleted secret from the key vault enabled for soft-delete. Subscribes to the call
     * asynchronously and prints out the
     * deleted secret details when a response is received.</p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled vault.
     * {@codesnippet com.azure.keyvault.secrets.secretclient.getDeletedSecret#string}
     *
     * @param name The name of the deleted secret.
     * @return A {@link Mono} containing the {@link DeletedSecret deleted secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key
     * vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeletedSecret> getDeletedSecret(String name) {
        return getDeletedSecretWithResponse(name).flatMap(FluxUtil::toMono);
    }

    /**
     * The get deleted secret operation returns the secrets that have been deleted for a vault enabled
     * for soft-delete. This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Gets the deleted secret from the key vault enabled for soft-delete. Subscribes to the call
     * asynchronously and prints out the
     * deleted secret details when a response is received.</p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled vault.
     * {@codesnippet com.azure.keyvault.secrets.secretclient.getDeletedSecretWithResponse#string}
     *
     * @param name The name of the deleted secret.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value}
     * contains the {@link DeletedSecret deleted secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key
     * vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeletedSecret>> getDeletedSecretWithResponse(String name) {
        return withContext(context -> getDeletedSecretWithResponse(name, context));
    }

    Mono<Response<DeletedSecret>> getDeletedSecretWithResponse(String name, Context context) {
        return service.getDeletedSecret(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Retrieving deleted secret - {}", name))
                .doOnSuccess(response -> logger.info("Retrieved deleted secret - {}", response.value().name()))
                .doOnError(error -> logger.warning("Failed to retrieve deleted secret - {}", name, error));
    }

    /**
     * The purge deleted secret operation removes the secret permanently, without the possibility of
     * recovery. This operation can only be enabled on a soft-delete enabled vault. This operation
     * requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted secret from the key vault enabled for soft-delete. Subscribes to the call
     * asynchronously and prints out the
     * status code from the server response when a response is received.</p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled vault.
     * {@codesnippet com.azure.keyvault.secrets.secretclient.purgeDeletedSecret#string}
     *
     * @param name The name of the secret.
     * @return A {@link Mono} containing a {@link VoidResponse}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key
     * vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<VoidResponse> purgeDeletedSecret(String name) {
        return withContext(context -> purgeDeletedSecret(name, context));
    }

    Mono<VoidResponse> purgeDeletedSecret(String name, Context context) {
        return service.purgeDeletedSecret(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Purging deleted secret - {}", name))
                .doOnSuccess(response -> logger.info("Purged deleted secret - {}", name))
                .doOnError(error -> logger.warning("Failed to purge deleted secret - {}", name, error));
    }

    /**
     * Recovers the deleted secret in the key vault to its latest version and can only be performed on a soft-delete enabled vault.
     * This operation requires the {@code secrets/recover} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted secret from the key vault enabled for soft-delete. Subscribes to the call asynchronously and prints out the
     * recovered secret details when a response is received.</p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled vault.
     * {@codesnippet com.azure.keyvault.secrets.secretclient.recoverDeletedSecret#string}
     *
     * @param name The name of the deleted secret to be recovered.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link Mono} containing the {@link Secret recovered secret}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Secret> recoverDeletedSecret(String name) {
        return recoverDeletedSecretWithResponse(name).flatMap(FluxUtil::toMono);
    }

    /**
     * Recovers the deleted secret in the key vault to its latest version and can only be performed on a soft-delete enabled vault.
     * This operation requires the {@code secrets/recover} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted secret from the key vault enabled for soft-delete. Subscribes to the call asynchronously and prints out the
     * recovered secret details when a response is received.</p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled vault.
     * {@codesnippet com.azure.keyvault.secrets.secretclient.recoverDeletedSecretWithResponse#string}
     *
     * @param name The name of the deleted secret to be recovered.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link Secret recovered secret}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Secret>> recoverDeletedSecretWithResponse(String name) {
        return withContext(context -> recoverDeletedSecretWithResponse(name, context));
    }

    Mono<Response<Secret>> recoverDeletedSecretWithResponse(String name, Context context) {
        return service.recoverDeletedSecret(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Recovering deleted secret - {}", name))
                .doOnSuccess(response -> logger.info("Recovered deleted secret - {}", response.value().name()))
                .doOnError(error -> logger.warning("Failed to recover deleted secret - {}", name, error));
    }

    /**
     * Requests a backup of the specified secret be downloaded to the client. All versions of the
     * secret will be downloaded. This operation requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the secret from the key vault. Subscribes to the call asynchronously and prints out
     * the
     * length of the secret's backup byte array returned in the response.</p>
     * <pre>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.backupSecret#string}
     *
     * @param name The name of the secret.
     * @return A {@link Mono} containing the backed up secret blob.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key
     * vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<byte[]> backupSecret(String name) {
        return backupSecretWithResponse(name).flatMap(FluxUtil::toMono);
    }

    /**
     * Requests a backup of the specified secret be downloaded to the client. All versions of the
     * secret will be downloaded. This operation requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the secret from the key vault. Subscribes to the call asynchronously and prints out
     * the
     * length of the secret's backup byte array returned in the response.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.backupSecretWithResponse#string}
     *
     * @param name The name of the secret.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value}
     * contains the backed up secret blob.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key
     * vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<byte[]>> backupSecretWithResponse(String name) {
        return withContext(context -> backupSecretWithResponse(name, context));
    }

    Mono<Response<byte[]>> backupSecretWithResponse(String name, Context context) {
        return service.backupSecret(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Backing up secret - {}", name))
                .doOnSuccess(response -> logger.info("Backed up secret - {}", name))
                .doOnError(error -> logger.warning("Failed to back up secret - {}", name, error))
                .flatMap(base64URLResponse ->  Mono.just(new SimpleResponse<byte[]>(base64URLResponse.request(),
                base64URLResponse.statusCode(), base64URLResponse.headers(), base64URLResponse.value().value())));
    }

    /**
     * Restores a backed up secret, and all its versions, to a vault. This operation requires the
     * {@code secrets/restore} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the secret in the key vault from its backup. Subscribes to the call asynchronously
     * and prints out the
     * restored secret details when a response is received.</p>
     * <pre>
     * //Pass the Secret Backup Byte array to the restore operation.
     * {@codesnippet com.azure.keyvault.secrets.secretclient.restoreSecret#byte}
     *
     * @param backup The backup blob associated with the secret.
     * @return A {@link Mono} containing the {@link Secret restored secret}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Secret> restoreSecret(byte[] backup) {
        return restoreSecretWithResponse(backup).flatMap(FluxUtil::toMono);
    }

    /**
     * Restores a backed up secret, and all its versions, to a vault. This operation requires the
     * {@code secrets/restore} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the secret in the key vault from its backup. Subscribes to the call asynchronously
     * and prints out the
     * restored secret details when a response is received.</p>
     * <pre>
     * //Pass the Secret Backup Byte array to the restore operation.
     * {@codesnippet com.azure.keyvault.secrets.secretclient.restoreSecretWithResponse#byte}
     *
     * @param backup The backup blob associated with the secret.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value}
     * contains the {@link Secret restored secret}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Secret>> restoreSecretWithResponse(byte[] backup) {
        return withContext(context -> restoreSecretWithResponse(backup, context));
    }

    Mono<Response<Secret>> restoreSecretWithResponse(byte[] backup, Context context) {
        SecretRestoreRequestParameters parameters = new SecretRestoreRequestParameters().secretBackup(backup);
        return service.restoreSecret(endpoint, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Attempting to restore secret"))
                .doOnSuccess(response -> logger.info("Restored secret - {}", response.value().name()))
                .doOnError(error -> logger.warning("Failed to restore secret", error));
    }

    /**
     * List secrets in the key vault. The list Secrets operation is applicable to the entire vault. The individual secret response
     * in the flux is represented by {@link SecretBase} as only the base secret identifier and its attributes are
     * provided in the response. The secret values and individual secret versions are not listed in the response. This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>It is possible to get full Secrets with values from this information. Convert the {@link Flux} containing {@link SecretBase base secret} to
     * {@link Flux} containing {@link Secret secret} using {@link SecretAsyncClient#getSecret(SecretBase baseSecret)} within {@link Flux#flatMap(Function)}.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.listSecrets}
     *
     * @return A {@link PagedFlux} containing {@link SecretBase secret} of all the secrets in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SecretBase> listSecrets() {
        return new PagedFlux<>(
            () -> withContext(context -> listSecretsFirstPage(context)),
            continuationToken -> withContext(context -> listSecretsNextPage(continuationToken, context)));
    }

    PagedFlux<SecretBase> listSecrets(Context context) {
        return new PagedFlux<>(
            () -> listSecretsFirstPage(context),
            continuationToken -> listSecretsNextPage(continuationToken, context));
    }

    /*
     * Gets attributes of all the secrets given by the {@code nextPageLink} that was retrieved from a call to
     * {@link SecretAsyncClient#listSecrets()}.
     *
     * @param continuationToken The {@link PagedResponse#nextLink()} from a previous, successful call to one of the list operations.
     * @return A {@link Mono} of {@link PagedResponse<SecretBase>} from the next page of results.
     */
    private Mono<PagedResponse<SecretBase>> listSecretsNextPage(String continuationToken, Context context) {
        return service.getSecrets(endpoint, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignoredValue -> logger.info("Retrieving the next secrets page - Page {}", continuationToken))
            .doOnSuccess(response -> logger.info("Retrieved the next secrets page - Page {}", continuationToken))
            .doOnError(error -> logger.warning("Failed to retrieve the next secrets page - Page {}", continuationToken, error));
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<SecretBase>> listSecretsFirstPage(Context context) {
        return service.getSecrets(endpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Listing secrets"))
            .doOnSuccess(response -> logger.info("Listed secrets"))
            .doOnError(error -> logger.warning("Failed to list secrets", error));
    }

    /**
     * Lists {@link DeletedSecret deleted secrets} of the key vault. The get deleted secrets operation returns the secrets that
     * have been deleted for a vault enabled for soft-delete. This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted secrets in the key vault. Subscribes to the call asynchronously and prints out the
     * recovery id of each deleted secret when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.listDeletedSecrets}
     *
     * @return A {@link Flux} containing all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DeletedSecret> listDeletedSecrets() {
        return new PagedFlux<>(
            () -> withContext(context -> listDeletedSecretsFirstPage(context)),
            continuationToken -> withContext(context -> listDeletedSecretsNextPage(continuationToken, context)));
    }

    PagedFlux<DeletedSecret> listDeletedSecrets(Context context) {
        return new PagedFlux<>(
            () -> listDeletedSecretsFirstPage(context),
            continuationToken -> listDeletedSecretsNextPage(continuationToken, context));
    }


    /**
     * Gets attributes of all the secrets given by the {@code nextPageLink} that was retrieved from a call to
     * {@link SecretAsyncClient#listDeletedSecrets()}.
     *
     * @param continuationToken The {@link PagedResponse#nextLink()} from a previous, successful call to one of the list operations.
     * @return A {@link Mono} of {@link PagedResponse<DeletedSecret>} from the next page of results.
     */
    private Mono<PagedResponse<DeletedSecret>> listDeletedSecretsNextPage(String continuationToken, Context context) {
        return service.getDeletedSecrets(endpoint, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignoredValue -> logger.info("Retrieving the next deleted secrets page - Page {}", continuationToken))
            .doOnSuccess(response -> logger.info("Retrieved the next deleted secrets page - Page {}", continuationToken))
            .doOnError(error -> logger.warning("Failed to retrieve the next deleted secrets page - Page {}", continuationToken, error));
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<DeletedSecret>> listDeletedSecretsFirstPage(Context context) {
        return service.getDeletedSecrets(endpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Listing deleted secrets"))
            .doOnSuccess(response -> logger.info("Listed deleted secrets"))
            .doOnError(error -> logger.warning("Failed to list deleted secrets", error));
    }

    /**
     * List all versions of the specified secret. The individual secret response in the flux is represented by {@link SecretBase}
     * as only the base secret identifier and its attributes are provided in the response. The secret values are
     * not provided in the response. This operation requires the {@code secrets/list} permission.
     *
     * <p>It is possible to get the Secret with value of all the versions from this information. Convert the {@link Flux}
     * containing {@link SecretBase base secret} to {@link Flux} containing {@link Secret secret} using
     * {@link SecretAsyncClient#getSecret(SecretBase baseSecret)} within {@link Flux#flatMap(Function)}.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.listSecretVersions#string}
     *
     * @param name The name of the secret.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link PagedFlux} containing {@link SecretBase secret} of all the versions of the specified secret in the vault. Flux is empty if secret with {@code name} does not exist in key vault
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SecretBase> listSecretVersions(String name) {
        return new PagedFlux<>(
            () -> withContext(context -> listSecretVersionsFirstPage(name, context)),
            continuationToken -> withContext(context -> listSecretVersionsNextPage(continuationToken, context)));
    }

    PagedFlux<SecretBase> listSecretVersions(String name, Context context) {
        return new PagedFlux<>(
            () -> listSecretVersionsFirstPage(name, context),
            continuationToken -> listSecretVersionsNextPage(continuationToken, context));
    }

    /*
     * Gets attributes of all the secrets versions given by the {@code nextPageLink} that was retrieved from a call to
     * {@link SecretAsyncClient#listSecretVersions()}.
     *
     * @param continuationToken The {@link PagedResponse#nextLink()} from a previous, successful call to one of the list operations.
     *
     * @return A {@link Mono} of {@link PagedResponse<SecretBase>} from the next page of results.
     */
    private Mono<PagedResponse<SecretBase>> listSecretVersionsNextPage(String continuationToken, Context context) {
        return service.getSecrets(endpoint, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignoredValue -> logger.info("Retrieving the next secrets versions page - Page {}", continuationToken))
            .doOnSuccess(response -> logger.info("Retrieved the next secrets versions page - Page {}", continuationToken))
            .doOnError(error -> logger.warning("Failed to retrieve the next secrets versions page - Page {}", continuationToken, error));
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<SecretBase>> listSecretVersionsFirstPage(String name, Context context) {
        return service.getSecretVersions(endpoint, name, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Listing secret versions - {}", name))
            .doOnSuccess(response -> logger.info("Listed secret versions - {}", name))
            .doOnError(error -> logger.warning(String.format("Failed to list secret versions - {}", name), error));
    }
}
