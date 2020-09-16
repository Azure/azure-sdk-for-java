// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Page;
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
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * The SecretAsyncClient provides asynchronous methods to manage {@link KeyVaultSecret secrets} in the Azure Key Vault. The
 * client supports creating, retrieving, updating, deleting, purging, backing up, restoring, and listing the {@link
 * KeyVaultSecret secrets}. The client also supports listing {@link DeletedSecret deleted secrets} for a soft-delete enabled
 * Azure Key Vault.
 *
 * <p><strong>Construct the async client</strong></p>
 * {@codesnippet com.azure.security.keyvault.secrets.async.secretclient.construct}
 *
 * @see SecretClientBuilder
 * @see PagedFlux
 */
@ServiceClient(builder = SecretClientBuilder.class, isAsync = true, serviceInterfaces = SecretService.class)
public final class SecretAsyncClient {
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
    private final SecretService service;
    private final ClientLogger logger = new ClientLogger(SecretAsyncClient.class);

    /**
     * Creates a SecretAsyncClient that uses {@code pipeline} to service requests
     *
     * @param vaultUrl URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link SecretServiceVersion} of the service to be used when making requests.
     */
    SecretAsyncClient(URL vaultUrl, HttpPipeline pipeline, SecretServiceVersion version) {
        Objects.requireNonNull(vaultUrl,
            KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.vaultUrl = vaultUrl.toString();
        this.service = RestProxy.create(SecretService.class, pipeline);
        apiVersion = version.getVersion();
    }

    /**
     * Gets the vault endpoint url to which service requests are sent to.
     * @return the vault endpoint url.
     */
    public String getVaultUrl() {
        return vaultUrl;
    }

    Duration getPollDuration() {
        return DEFAULT_POLL_DURATION;
    }

    /**
     * Adds a secret to the key vault if it does not exist. If the named secret exists, a new version of the secret is
     * created. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@link SecretProperties#getExpiresOn() expires}, {@link SecretProperties#getContentType() contentType},
     * and {@link SecretProperties#getNotBefore() notBefore} values in {@code secret} are optional.
     * If not specified, {@link SecretProperties#isEnabled() enabled} is set to true by key vault.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <p>Creates a new secret which activates in one day and expires in one year. Subscribes to the call asynchronously
     * and prints out the newly created secret details when a response is received.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.setSecret#secret}
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties
     *     {@link KeyVaultSecret#getName() secret.name} and {@link KeyVaultSecret#getValue() secret.value} cannot be
     *     null.
     * @return A {@link Mono} containing the {@link KeyVaultSecret created secret}.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceModifiedException if {@code secret} is malformed.
     * @throws HttpResponseException if {@link KeyVaultSecret#getName()  name} or {@link KeyVaultSecret#getValue() value}
     *      is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSecret> setSecret(KeyVaultSecret secret) {
        try {
            return setSecretWithResponse(secret).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Adds a secret to the key vault if it does not exist. If the named secret exists, a new version of the secret is
     * created. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@link SecretProperties#getExpiresOn() expires}, {@link SecretProperties#getContentType() contentType},
     * and {@link SecretProperties#getNotBefore() notBefore} values in {@code secret} are optional.
     * If not specified, {@link SecretProperties#isEnabled() enabled} is set to true by key vault.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <p>Creates a new secret which activates in one day and expires in one year. Subscribes to the call asynchronously
     * and prints out the newly created secret details when a response is received.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.setSecretWithResponse#secret}
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties
     *     {@link KeyVaultSecret#getName() secret.name} and {@link KeyVaultSecret#getValue() secret.value} cannot be
     *     null.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link
     *     KeyVaultSecret created secret}.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceModifiedException if {@code secret} is malformed.
     * @throws HttpResponseException if {@link KeyVaultSecret#getName() name} or {@link KeyVaultSecret#getValue() value}
     *      is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultSecret>> setSecretWithResponse(KeyVaultSecret secret) {
        try {
            return withContext(context -> setSecretWithResponse(secret, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultSecret>> setSecretWithResponse(KeyVaultSecret secret, Context context) {
        Objects.requireNonNull(secret, "The Secret input parameter cannot be null.");
        context = context == null ? Context.NONE : context;
        SecretRequestParameters parameters = new SecretRequestParameters()
            .setValue(secret.getValue())
            .setTags(secret.getProperties().getTags())
            .setContentType(secret.getProperties().getContentType())
            .setSecretAttributes(new SecretRequestAttributes(secret.getProperties()));

        return service.setSecret(vaultUrl, secret.getName(), apiVersion, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Setting secret - {}", secret.getName()))
            .doOnSuccess(response -> logger.info("Set secret - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to set secret - {}", secret.getName(), error));
    }

    /**
     * Adds a secret to the key vault if it does not exist. If the named secret exists, a new version of the secret is
     * created. This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Creates a new secret in the key vault. Subscribes to the call asynchronously and prints out
     * the newly created secret details when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.setSecret#string-string}
     *
     * @param name The name of the secret. It is required and cannot be null.
     * @param value The value of the secret. It is required and cannot be null.
     * @return A {@link Mono} containing the {@link KeyVaultSecret created secret}.
     * @throws ResourceModifiedException if invalid {@code name} or {@code value} are specified.
     * @throws HttpResponseException if {@code name} or {@code value} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSecret> setSecret(String name, String value) {
        try {
            return withContext(context -> setSecretWithResponse(name, value, context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultSecret>> setSecretWithResponse(String name, String value, Context context) {
        SecretRequestParameters parameters = new SecretRequestParameters().setValue(value);
        return service.setSecret(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Setting secret - {}", name))
            .doOnSuccess(response -> logger.info("Set secret - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to set secret - {}", name, error));
    }

    /**
     * Gets the specified secret with specified version from the key vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets a specific version of the secret in the key vault. Subscribes to the call
     * asynchronously and prints out the returned secret details when a response is received.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.getSecret#string-string}
     *
     * @param name The name of the secret, cannot be null.
     * @param version The version of the secret to retrieve. If this is an empty string or null, this
     *     call is equivalent to calling {@link #getSecret(String)}, with the latest version being
     *     retrieved.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value}
     *     contains the requested {@link KeyVaultSecret secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} and {@code version} doesn't
     *     exist in the key vault.
     * @throws HttpResponseException if {@code name}  name} or {@code version} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSecret> getSecret(String name, String version) {
        try {
            return getSecretWithResponse(name, version).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the specified secret with specified version from the key vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets a specific version of the secret in the key vault. Subscribes to the call asynchronously and prints out
     * the returned secret details when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.getSecretWithResponse#string-string}
     *
     * @param name The name of the secret, cannot be null.
     * @param version The version of the secret to retrieve. If this is an empty string or null, this call is equivalent
     *     to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     *     requested {@link KeyVaultSecret secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} and {@code version} doesn't exist in the key
     *     vault.
     * @throws HttpResponseException if {@code name}  name} or {@code version} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultSecret>> getSecretWithResponse(String name, String version) {
        try {
            return withContext(context -> getSecretWithResponse(name, version, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultSecret>> getSecretWithResponse(String name, String version, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getSecret(vaultUrl, name, version == null ? "" : version, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignoredValue -> logger.info("Retrieving secret - {}", name))
            .doOnSuccess(response -> logger.info("Retrieved secret - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to get secret - {}", name, error));
    }

    /**
     * Gets the latest version of the specified secret from the key vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets latest version of the secret in the key vault. Subscribes to the call asynchronously and prints out the
     * returned secret details when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.getSecret#string}
     *
     * @param name The name of the secret.
     * @return A {@link Mono} containing the requested {@link KeyVaultSecret secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSecret> getSecret(String name) {
        try {
            return getSecretWithResponse(name, "").flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates the attributes associated with the secret. The value of the secret in the key vault cannot be changed.
     * Only attributes populated in {@code secretProperties} are changed. Attributes not specified in the request are
     * not changed. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@code secret} is required and its fields {@link SecretProperties#getName() name} and
     * {@link SecretProperties#getVersion() version} cannot be null.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets latest version of the secret, changes its {@link SecretProperties#setNotBefore(OffsetDateTime) notBefore}
     * time, and then updates it in the Azure Key Vault. Subscribes to the call asynchronously and prints out the
     * returned secret details when a response is received.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.updateSecretProperties#secretProperties}
     *
     * @param secretProperties The {@link SecretProperties secret properties} object with updated properties.
     * @return A {@link Mono} containing the {@link SecretProperties updated secret}.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceNotFoundException when a secret with {@link SecretProperties#getName() name} and {@link
     *     SecretProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpResponseException if {@link SecretProperties#getName() name} or
     *     {@link SecretProperties#getVersion() version} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SecretProperties> updateSecretProperties(SecretProperties secretProperties) {
        try {
            return updateSecretPropertiesWithResponse(secretProperties).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates the attributes associated with the secret. The value of the secret in the key vault cannot be changed.
     * Only attributes populated in {@code secretProperties} are changed. Attributes not specified in the request are
     * not changed. This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets latest version of the secret, changes its {@link SecretProperties#setNotBefore(OffsetDateTime) notBefore}
     * time, and then updates it in the Azure Key Vault. Subscribes to the call asynchronously and prints out the
     * returned secret details when a response is received.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.updateSecretPropertiesWithResponse#secretProperties}
     *
     * <p>The {@code secret} is required and its fields {@link SecretProperties#getName() name} and
     * {@link SecretProperties#getVersion() version} cannot be null.</p>
     *
     * @param secretProperties The {@link SecretProperties secret properties} object with updated properties.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link
     *     SecretProperties updated secret}.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceNotFoundException when a secret with {@link SecretProperties#getName() name} and {@link
     *     SecretProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpResponseException if {@link SecretProperties#getName() name} or
     *     {@link SecretProperties#getVersion() version} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SecretProperties>> updateSecretPropertiesWithResponse(SecretProperties secretProperties) {
        try {
            return withContext(context -> updateSecretPropertiesWithResponse(secretProperties, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<SecretProperties>> updateSecretPropertiesWithResponse(SecretProperties secretProperties, Context context) {
        Objects.requireNonNull(secretProperties, "The secret properties input parameter cannot be null.");
        context = context == null ? Context.NONE : context;
        SecretRequestParameters parameters = new SecretRequestParameters()
            .setTags(secretProperties.getTags())
            .setContentType(secretProperties.getContentType())
            .setSecretAttributes(new SecretRequestAttributes(secretProperties));

        return service.updateSecret(vaultUrl, secretProperties.getName(), secretProperties.getVersion(), apiVersion, ACCEPT_LANGUAGE,
            parameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Updating secret - {}", secretProperties.getName()))
            .doOnSuccess(response -> logger.info("Updated secret - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to update secret - {}", secretProperties.getName(), error));
    }

    /**
     * Deletes a secret from the key vault. If soft-delete is enabled on the key vault then the secret is placed in the
     * deleted state and for permanent deletion, needs to be purged. Otherwise, the secret is permanently deleted.
     * All versions of a secret are deleted. This cannot be applied to individual versions of a secret.
     * This operation requires the {@code secrets/delete} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Deletes the secret in the Azure Key Vault. Subscribes to the call asynchronously and prints out the deleted
     * secret details when a response is received.</p>
     * {@codesnippet com.azure.keyvault.secrets.secretclient.deleteSecret#string}
     *
     * @param name The name of the secret to be deleted.
     * @return A {@link PollerFlux} to poll on and retrieve {@link DeletedSecret deleted secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<DeletedSecret, Void> beginDeleteSecret(String name) {
        return new PollerFlux<>(getPollDuration(),
            activationOperation(name),
            createPollOperation(name),
            (pollingContext, firstResponse) -> Mono.empty(),
            (pollingContext) -> Mono.empty());
    }

    private Function<PollingContext<DeletedSecret>, Mono<DeletedSecret>> activationOperation(String name) {
        return (pollingContext) -> withContext(context -> deleteSecretWithResponse(name, context)).flatMap(deletedSecretResponse -> Mono.just(deletedSecretResponse.getValue()));
    }

    /*
    Polling operation to poll on create delete key operation status.
    */
    private Function<PollingContext<DeletedSecret>, Mono<PollResponse<DeletedSecret>>> createPollOperation(String keyName) {
        return pollingContext ->
            withContext(context -> service.getDeletedSecretPoller(vaultUrl, keyName, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                .flatMap(deletedSecretResponse -> {
                    if (deletedSecretResponse.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                        return Mono.defer(() -> Mono.just(new PollResponse<DeletedSecret>(LongRunningOperationStatus.IN_PROGRESS,
                                pollingContext.getLatestResponse().getValue())));
                    }
                    return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            deletedSecretResponse.getValue())));
                })
                // This means either vault has soft-delete disabled or permission is not granted for the get deleted key operation.
                // In both cases deletion operation was successful when activation operation succeeded before reaching here.
                .onErrorReturn(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue()));
    }

    Mono<Response<DeletedSecret>> deleteSecretWithResponse(String name, Context context) {
        return service.deleteSecret(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Deleting secret - {}", name))
            .doOnSuccess(response -> logger.info("Deleted secret - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to delete secret - {}", name, error));
    }

    /**
     * Gets a secret that has been deleted for a soft-delete enabled key vault. This operation requires the
     * {@code secrets/list} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets the deleted secret from the key vault <b>enabled for soft-delete</b>. Subscribes to the call
     * asynchronously and prints out the deleted secret details when a response is received.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.getDeletedSecret#string}
     *
     * @param name The name of the deleted secret.
     * @return A {@link Mono} containing the {@link DeletedSecret deleted secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeletedSecret> getDeletedSecret(String name) {
        try {
            return getDeletedSecretWithResponse(name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a secret that has been deleted for a soft-delete enabled key vault. This operation requires the
     * {@code secrets/list} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets the deleted secret from the key vault <b>enabled for soft-delete</b>. Subscribes to the call
     * asynchronously and prints out the deleted secret details when a response is received.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.getDeletedSecretWithResponse#string}
     *
     * @param name The name of the deleted secret.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     *     {@link DeletedSecret deleted secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeletedSecret>> getDeletedSecretWithResponse(String name) {
        try {
            return withContext(context -> getDeletedSecretWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DeletedSecret>> getDeletedSecretWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getDeletedSecret(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Retrieving deleted secret - {}", name))
            .doOnSuccess(response -> logger.info("Retrieved deleted secret - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to retrieve deleted secret - {}", name, error));
    }

    /**
     * Permanently removes a deleted secret, without the possibility of recovery. This operation can only be performed
     * on a <b>soft-delete enabled</b>. This operation requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Purges the deleted secret from the key vault enabled for <b>soft-delete</b>. Subscribes to the call
     * asynchronously and prints out the status code from the server response when a response is received.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.purgeDeletedSecret#string}
     *
     * @param name The name of the secret.
     * @return An empty {@link Mono}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> purgeDeletedSecret(String name) {
        try {
            return purgeDeletedSecretWithResponse(name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Permanently removes a deleted secret, without the possibility of recovery. This operation can only be enabled on
     * a soft-delete enabled vault. This operation requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Purges the deleted secret from the key vault enabled for soft-delete. Subscribes to the call
     * asynchronously and prints out the status code from the server response when a response is received.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.purgeDeletedSecretWithResponse#string}
     *
     * @param name The name of the secret.
     * @return A {@link Mono} containing a Response containing status code and HTTP headers.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> purgeDeletedSecretWithResponse(String name) {
        try {
            return withContext(context -> purgeDeletedSecretWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> purgeDeletedSecretWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        return service.purgeDeletedSecret(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Purging deleted secret - {}", name))
            .doOnSuccess(response -> logger.info("Purged deleted secret - {}", name))
            .doOnError(error -> logger.warning("Failed to purge deleted secret - {}", name, error));
    }

    /**
     * Recovers the deleted secret in the key vault to its latest version. Can only be performed on a <b>soft-delete
     * enabled</b> vault. This operation requires the {@code secrets/recover} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recovers the deleted secret from the key vault enabled for <b>soft-delete</b>. Subscribes to the call
     * asynchronously and prints out the recovered secret details when a response is received.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.recoverDeletedSecret#string}
     *
     * @param name The name of the deleted secret to be recovered.
     * @return A {@link PollerFlux} to poll on and retrieve the {@link KeyVaultSecret recovered secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<KeyVaultSecret, Void> beginRecoverDeletedSecret(String name) {
        return new PollerFlux<>(getPollDuration(),
            recoverActivationOperation(name),
            createRecoverPollOperation(name),
            (pollerContext, firstResponse) -> Mono.empty(),
            (pollingContext) -> Mono.empty());
    }

    private Function<PollingContext<KeyVaultSecret>, Mono<KeyVaultSecret>> recoverActivationOperation(String name) {
        return (pollingContext) -> withContext(context -> recoverDeletedSecretWithResponse(name,
            context)).flatMap(keyResponse -> Mono.just(keyResponse.getValue()));
    }

    /*
    Polling operation to poll on create delete key operation status.
    */
    private Function<PollingContext<KeyVaultSecret>, Mono<PollResponse<KeyVaultSecret>>> createRecoverPollOperation(String secretName) {
        return pollingContext ->
            withContext(context -> service.getSecretPoller(vaultUrl, secretName, "", apiVersion,
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                .flatMap(secretResponse -> {
                    PollResponse<KeyVaultSecret> prePollResponse = pollingContext.getLatestResponse();
                    if (secretResponse.getStatusCode() == 404) {
                        return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, prePollResponse.getValue())));
                    }
                    return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, secretResponse.getValue())));
                })
                // This means permission is not granted for the get deleted key operation.
                // In both cases deletion operation was successful when activation operation succeeded before reaching here.
                .onErrorReturn(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollingContext.getLatestResponse().getValue()));
    }

    Mono<Response<KeyVaultSecret>> recoverDeletedSecretWithResponse(String name, Context context) {
        return service.recoverDeletedSecret(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Recovering deleted secret - {}", name))
            .doOnSuccess(response -> logger.info("Recovered deleted secret - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to recover deleted secret - {}", name, error));
    }

    /**
     * Requests a backup of the secret be downloaded to the client. All versions of the secret will be downloaded. This
     * operation requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Backs up the secret from the key vault. Subscribes to the call asynchronously and prints out
     * the length of the secret's backup byte array returned in the response.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.backupSecret#string}
     *
     * @param name The name of the secret.
     * @return A {@link Mono} containing the backed up secret blob.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<byte[]> backupSecret(String name) {
        try {
            return backupSecretWithResponse(name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Requests a backup of the secret be downloaded to the client. All versions of the secret will be downloaded. This
     * operation requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Backs up the secret from the key vault. Subscribes to the call asynchronously and prints out
     * the length of the secret's backup byte array returned in the response.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.backupSecretWithResponse#string}
     *
     * @param name The name of the secret.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value}
     *     contains the backed up secret blob.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key
     *     vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<byte[]>> backupSecretWithResponse(String name) {
        try {
            return withContext(context -> backupSecretWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<byte[]>> backupSecretWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        return service.backupSecret(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Backing up secret - {}", name))
            .doOnSuccess(response -> logger.info("Backed up secret - {}", name))
            .doOnError(error -> logger.warning("Failed to back up secret - {}", name, error))
            .flatMap(base64URLResponse -> Mono.just(new SimpleResponse<byte[]>(base64URLResponse.getRequest(),
                base64URLResponse.getStatusCode(), base64URLResponse.getHeaders(), base64URLResponse.getValue().getValue())));
    }

    /**
     * Restores a backed up secret, and all its versions, to a vault. This operation requires the
     * {@code secrets/restore} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Restores the secret in the key vault from its backup. Subscribes to the call asynchronously
     * and prints out the restored secret details when a response is received.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.restoreSecret#byte}
     *
     * @param backup The backup blob associated with the secret.
     * @return A {@link Mono} containing the {@link KeyVaultSecret restored secret}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSecret> restoreSecretBackup(byte[] backup) {
        try {
            return restoreSecretBackupWithResponse(backup).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Restores a backed up secret, and all its versions, to a vault. This operation requires the
     * {@code secrets/restore} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Restores the secret in the key vault from its backup. Subscribes to the call asynchronously
     * and prints out the restored secret details when a response is received.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.restoreSecretWithResponse#byte}
     *
     * @param backup The backup blob associated with the secret.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value}
     *     contains the {@link KeyVaultSecret restored secret}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultSecret>> restoreSecretBackupWithResponse(byte[] backup) {
        try {
            return withContext(context -> restoreSecretBackupWithResponse(backup, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultSecret>> restoreSecretBackupWithResponse(byte[] backup, Context context) {
        context = context == null ? Context.NONE : context;
        SecretRestoreRequestParameters parameters = new SecretRestoreRequestParameters().setSecretBackup(backup);
        return service.restoreSecret(vaultUrl, apiVersion, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Attempting to restore secret"))
            .doOnSuccess(response -> logger.info("Restored secret - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to restore secret", error));
    }

    /**
     * Lists secrets in the key vault. Each {@link SecretProperties secret} returned only has its identifier and
     * attributes populated. The secret values and their versions are not listed in the response.
     * This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>The sample below fetches the all the secret properties in the vault. For each secret retrieved, makes a call
     * to {@link #getSecret(String, String) getSecret(String, String)} to get its value, and then prints it out.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.listSecrets}
     *
     * @return A {@link PagedFlux} containing {@link SecretProperties properties} of all the secrets in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SecretProperties> listPropertiesOfSecrets() {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listSecretsFirstPage(context)),
                continuationToken -> withContext(context -> listSecretsNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<SecretProperties> listPropertiesOfSecrets(Context context) {
        return new PagedFlux<>(
            () -> listSecretsFirstPage(context),
            continuationToken -> listSecretsNextPage(continuationToken, context));
    }

    /*
     * Gets attributes of all the secrets given by the {@code nextPageLink} that was retrieved from a call to
     * {@link SecretAsyncClient#listSecrets()}.
     *
     * @param continuationToken The {@link PagedResponse#nextLink()} from a previous, successful call to one of the
     * list operations.
     * @return A {@link Mono} of {@link PagedResponse<SecretProperties>} from the next page of results.
     */
    private Mono<PagedResponse<SecretProperties>> listSecretsNextPage(String continuationToken, Context context) {
        try {
            return service.getSecrets(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignoredValue -> logger.info("Retrieving the next secrets page - Page {}", continuationToken))
                .doOnSuccess(response -> logger.info("Retrieved the next secrets page - Page {}", continuationToken))
                .doOnError(error -> logger.warning("Failed to retrieve the next secrets page - Page {}",
                    continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code
     * DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<SecretProperties>> listSecretsFirstPage(Context context) {
        try {
            return service.getSecrets(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing secrets"))
                .doOnSuccess(response -> logger.info("Listed secrets"))
                .doOnError(error -> logger.warning("Failed to list secrets", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists {@link DeletedSecret deleted secrets} of the key vault if it has enabled soft-delete. This operation
     * requires the {@code secrets/list} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Lists the deleted secrets in the key vault. Subscribes to the call asynchronously and prints out the
     * recovery id of each deleted secret when a response is received.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.listDeletedSecrets}
     *
     * @return A {@link Flux} containing all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DeletedSecret> listDeletedSecrets() {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listDeletedSecretsFirstPage(context)),
                continuationToken -> withContext(context -> listDeletedSecretsNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
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
     * @param continuationToken The {@link Page#getContinuationToken()} from a previous, successful call to one of the
     *     list operations.
     * @return A {@link Mono} of {@link PagedResponse} that contains {@link DeletedSecret} from the next page of
     * results.
     */
    private Mono<PagedResponse<DeletedSecret>> listDeletedSecretsNextPage(String continuationToken, Context context) {
        try {
            return service.getDeletedSecrets(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignoredValue -> logger.info("Retrieving the next deleted secrets page - Page {}",
                    continuationToken))
                .doOnSuccess(response -> logger.info("Retrieved the next deleted secrets page - Page {}",
                    continuationToken))
                .doOnError(error -> logger.warning("Failed to retrieve the next deleted secrets page - Page {}",
                    continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code
     * DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<DeletedSecret>> listDeletedSecretsFirstPage(Context context) {
        try {
            return service.getDeletedSecrets(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing deleted secrets"))
                .doOnSuccess(response -> logger.info("Listed deleted secrets"))
                .doOnError(error -> logger.warning("Failed to list deleted secrets", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all versions of the specified secret. Each {@link SecretProperties secret} returned only has its identifier
     * and attributes populated. The secret values and secret versions are not listed in the response.
     * This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>The sample below fetches the all the versions of the given secret. For each version retrieved, makes a call
     * to {@link #getSecret(String, String) getSecret(String, String)} to get the version's value, and then prints it out.</p>
     *
     * {@codesnippet com.azure.keyvault.secrets.secretclient.listSecretVersions#string}
     *
     * @param name The name of the secret.
     * @return A {@link PagedFlux} containing {@link SecretProperties properties} of all the versions of the specified
     *     secret in the vault. Flux is empty if secret with {@code name} does not exist in key vault
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SecretProperties> listPropertiesOfSecretVersions(String name) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listSecretVersionsFirstPage(name, context)),
                continuationToken -> withContext(context -> listSecretVersionsNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<SecretProperties> listPropertiesOfSecretVersions(String name, Context context) {
        return new PagedFlux<>(
            () -> listSecretVersionsFirstPage(name, context),
            continuationToken -> listSecretVersionsNextPage(continuationToken, context));
    }

    /*
     * Gets attributes of all the secrets versions given by the {@code nextPageLink} that was retrieved from a call to
     * {@link SecretAsyncClient#listSecretVersions()}.
     *
     * @param continuationToken The {@link PagedResponse#nextLink()} from a previous, successful call to one of the
     * list operations.
     *
     * @return A {@link Mono} of {@link PagedResponse<SecretProperties>} from the next page of results.
     */
    private Mono<PagedResponse<SecretProperties>> listSecretVersionsNextPage(String continuationToken, Context context) {
        try {
            return service.getSecrets(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignoredValue -> logger.info("Retrieving the next secrets versions page - Page {}",
                    continuationToken))
                .doOnSuccess(response -> logger.info("Retrieved the next secrets versions page - Page {}",
                    continuationToken))
                .doOnError(error -> logger.warning("Failed to retrieve the next secrets versions page - Page {}",
                    continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code
     * DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<SecretProperties>> listSecretVersionsFirstPage(String name, Context context) {
        try {
            return service.getSecretVersions(vaultUrl, name, DEFAULT_MAX_PAGE_RESULTS, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing secret versions - {}", name))
                .doOnSuccess(response -> logger.info("Listed secret versions - {}", name))
                .doOnError(error -> logger.warning(String.format("Failed to list secret versions - {}", name), error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
