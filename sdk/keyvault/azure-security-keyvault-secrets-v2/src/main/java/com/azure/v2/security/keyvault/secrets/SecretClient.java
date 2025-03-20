// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.security.keyvault.secrets.implementation.SecretClientImpl;
import com.azure.v2.security.keyvault.secrets.implementation.models.BackupSecretResult;
import com.azure.v2.security.keyvault.secrets.implementation.models.DeletedSecretBundle;
import com.azure.v2.security.keyvault.secrets.implementation.models.KeyVaultErrorException;
import com.azure.v2.security.keyvault.secrets.implementation.models.SecretBundle;
import com.azure.v2.security.keyvault.secrets.implementation.models.SecretRestoreParameters;
import com.azure.v2.security.keyvault.secrets.implementation.models.SecretSetParameters;
import com.azure.v2.security.keyvault.secrets.implementation.models.SecretUpdateParameters;
import com.azure.v2.security.keyvault.secrets.implementation.models.SecretsModelsUtils;
import com.azure.v2.security.keyvault.secrets.models.DeletedSecret;
import com.azure.v2.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.v2.security.keyvault.secrets.models.SecretProperties;
import io.clientcore.core.annotations.ReturnType;
import io.clientcore.core.annotations.ServiceClient;
import io.clientcore.core.annotations.ServiceMethod;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.paging.PagedIterable;
import io.clientcore.core.http.paging.PagedResponse;
import io.clientcore.core.http.paging.PagingOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.Context;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.v2.security.keyvault.secrets.implementation.models.SecretsModelsUtils.createDeletedSecret;
import static com.azure.v2.security.keyvault.secrets.implementation.models.SecretsModelsUtils.createKeyVaultSecret;
import static com.azure.v2.security.keyvault.secrets.implementation.models.SecretsModelsUtils.createSecretAttributes;
import static com.azure.v2.security.keyvault.secrets.implementation.models.SecretsModelsUtils.createSecretProperties;
import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * This class provides methods to manage secrets in Azure Key Vault. The client supports creating, retrieving, updating,
 * deleting, purging, backing up, restoring, and listing the secrets. The client also supports listing deleted secrets
 * for a soft-delete enabled key vault.
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link SecretClient} class, a Key Vault endpoint and a {@link TokenCredential credential} object.</p>
 *
 * <p>The examples shown in this document use a credential object named {@code DefaultAzureCredential} for
 * authentication, which is appropriate for most scenarios, including local development and production environments.
 * Additionally, we recommend using a
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments. You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity documentation"</a>.</p>
 *
 * <p><strong>Sample: Construct SecretClient</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link SecretClient}, using a {@link SecretClientBuilder}
 * to configure it.</p>
 *
 * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.instantiation -->
 * <!-- end com.azure.v2.security.keyvault.SecretClient.instantiation -->
 * <br/>
 * <hr/>
 *
 * <h2>Create a Secret</h2>
 * The {@link SecretClient} can be used to create a secret in the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to create and store a secret in the key vault, using the
 * {@link SecretClient#setSecret(String, String)} API.</p>
 *
 * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.setSecret#string-string -->
 * <!-- end com.azure.v2.security.keyvault.SecretClient.setSecret#string-string -->
 * <br/>
 * <hr/>
 *
 * <h2>Get a Secret</h2>
 * The {@link SecretClient} can be used to retrieve a secret from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to retrieve a previously stored secret from the key vault, using the
 * {@link SecretClient#getSecret(String)} API.</p>
 *
 * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.getSecret#string -->
 * <!-- end com.azure.v2.security.keyvault.SecretClient.getSecret#string -->
 * <br/>
 * <hr/>
 *
 * <h2>Delete a Secret</h2>
 * The {@link SecretClient} can be used to delete a secret from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to delete a secret from the key vault, using the
 * {/@link SecretClient#beginDeleteSecret(String)} API.</p>
 *
 * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.deleteSecret#String -->
 * <!-- end com.azure.v2.security.keyvault.SecretClient.deleteSecret#String -->
 *
 * @see com.azure.v2.security.keyvault.secrets
 * @see SecretClientBuilder
 */
@ServiceClient(builder = SecretClientBuilder.class, serviceInterfaces = SecretClientImpl.SecretClientService.class)
public final class SecretClient {
    private static final ClientLogger LOGGER = new ClientLogger(SecretClient.class);

    private final SecretClientImpl implClient;
    private final String endpoint;

    /**
     * Gets the vault endpoint to which service requests are sent to.
     *
     * @return The vault endpoint.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Creates an instance of {@link SecretClient} that sends requests to the given endpoint.
     *
     * @param clientImpl The implementation client.
     * @param endpoint The vault endpoint.
     */
    SecretClient(SecretClientImpl clientImpl, String endpoint) {
        this.implClient = clientImpl;
        this.endpoint = endpoint;
    }

    /**
     * Adds a secret to the key vault if it does not exist. If a secret with the provided name already exists, a new
     * version of the secret is created. This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new secret in the key vault. Prints out the details of the newly created secret returned in the
     * response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.setSecret#string-string -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.setSecret#string-string -->
     *
     * @param name The name of the secret. It is required and cannot be {@code null}.
     * @param value The value of the secret. It is required and cannot be {@code null}.
     * @return The newly created secret.
     *
     * @throws KeyVaultErrorException If either of the provided {@code name} or {@code value} is invalid.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret setSecret(String name, String value) {
        try (Response<SecretBundle> response = implClient.setSecretWithResponse(name, new SecretSetParameters(value),
            RequestOptions.none())) {

            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return createKeyVaultSecret(response.getValue());
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Adds a secret to the key vault if it does not exist. If the named secret exists, a new version of the secret is
     * created. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@link SecretProperties#getExpiresOn() expires}, {@link SecretProperties#getContentType() contentType},
     * and {@link SecretProperties#getNotBefore() notBefore} values in the provided {@link KeyVaultSecret secret object}
     * are optional. If not specified, {@link SecretProperties#isEnabled() enabled} is set to true by key vault.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new secret in the key vault. Prints out the details of the newly created secret returned in the
     * response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.setSecret#secret -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.setSecret#secret -->
     *
     * @param secret The {@link KeyVaultSecret secret object} containing information about the secret and its
     * properties. The properties {@link KeyVaultSecret#getName() name} and {@link KeyVaultSecret#getValue() value}
     * cannot be {@code null}.
     * @return The newly created secret.
     *
     * @throws KeyVaultErrorException If the provided {@link KeyVaultSecret secret object} is malformed or if either of
     * {@link KeyVaultSecret#getName()} or {@link KeyVaultSecret#getValue()} is invalid.
     * @throws IllegalArgumentException If {@link KeyVaultSecret#getName()} is {@code null} or an empty stirng.
     * @throws NullPointerException if the provided {@link KeyVaultSecret secret object} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret setSecret(KeyVaultSecret secret) {
        try (Response<SecretBundle> response = implClient.setSecretWithResponse(secret.getName(),
            prepareSecretSetParameters(secret), RequestOptions.none())) {

            Objects.requireNonNull(secret, "'secret' cannot be null.");

            if (isNullOrEmpty(secret.getName())) {
                throw new IllegalArgumentException("'secret.getName()' cannot be null or empty.");
            }

            return createKeyVaultSecret(response.getValue());
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Adds a secret to the key vault if it does not exist. If the named secret exists, a new version of the secret is
     * created. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@link SecretProperties#getExpiresOn() expires}, {@link SecretProperties#getContentType() contentType},
     * and {@link SecretProperties#getNotBefore() notBefore} values in the provided {@link KeyVaultSecret secret object}
     * are optional. If not specified, {@link SecretProperties#isEnabled() enabled} is set to true by key vault.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new secret in the key vault. Prints out the details of the newly created secret returned in the
     * response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.setSecretWithResponse#secret-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.setSecretWithResponse#secret-RequestOptions -->
     *
     * @param secret The {@link KeyVaultSecret secret object} containing information about the secret and its
     * properties. The properties {@link KeyVaultSecret#getName() name} and {@link KeyVaultSecret#getValue() value}
     * cannot be {@code null}.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the newly created secret.
     *
     * @throws KeyVaultErrorException If the provided {@link KeyVaultSecret secret object} is malformed or if either of
     * {@link KeyVaultSecret#getName()} or {@link KeyVaultSecret#getValue()} is invalid.
     * @throws IllegalArgumentException If {@link KeyVaultSecret#getName()} is {@code null} or an empty stirng.
     * @throws NullPointerException if the provided {@link KeyVaultSecret secret object} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSecret> setSecretWithResponse(KeyVaultSecret secret, RequestOptions requestOptions) {
        try (Response<SecretBundle> response = implClient.setSecretWithResponse(secret.getName(),
            prepareSecretSetParameters(secret), requestOptions)) {

            Objects.requireNonNull(secret, "'secret' cannot be null.");

            if (isNullOrEmpty(secret.getName())) {
                throw new IllegalArgumentException("'secret.getName()' cannot be null or empty.");
            }

            return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                createKeyVaultSecret(response.getValue()));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    private static SecretSetParameters prepareSecretSetParameters(KeyVaultSecret secret) {
        SecretSetParameters secretSetParameters = new SecretSetParameters(secret.getValue());
        SecretProperties secretProperties = secret.getProperties();

        if (secretProperties != null) {
            secretSetParameters.setTags(secretProperties.getTags())
                .setContentType(secretProperties.getContentType())
                .setSecretAttributes(createSecretAttributes(secretProperties));
        }

        return secretSetParameters;
    }

    /**
     * Gets the latest version of the specified secret from the key vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the latest version of the secret in the key vault. Prints out the details of the secret returned in the
     * response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.getSecret#string -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.getSecret#string -->
     *
     * @param name The name of the secret.
     * @return The requested secret.
     *
     * @throws KeyVaultErrorException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret getSecret(String name) {
        return getSecret(name, "");
    }

    /**
     * Gets the specified secret with specified version from the key vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific version of the secret in the key vault. Prints out the details of the secret returned in the
     * response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.getSecret#string-string -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.getSecret#string-string -->
     *
     * @param name The name of the secret, cannot be {@code null}.
     * @param version The version of the secret to retrieve. If this is an empty string or {@code null}, this call is
     * equivalent to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @return The requested secret.
     *
     * @throws KeyVaultErrorException If a secret with the given {@code name} and {@code version} doesn't exist in the
     * key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret getSecret(String name, String version) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return createKeyVaultSecret(
                implClient.getSecretWithResponse(name, version, RequestOptions.none()).getValue());
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets the specified secret with specified version from the key vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific version of the secret in the key vault. Prints out the details of the secret returned in the
     * response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.getSecretWithResponse#string-string-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.getSecretWithResponse#string-string-RequestOptions -->
     *
     * @param name The name of the secret, cannot be {@code null}
     * @param version The version of the secret to retrieve. If this is an empty string or {@code null}, this call is
     * equivalent to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the requested secret.
     *
     * @throws KeyVaultErrorException If a secret with the given {@code name} and {@code version} doesn't exist in the
     * vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSecret> getSecretWithResponse(String name, String version, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            Response<SecretBundle> response = implClient.getSecretWithResponse(name, version, requestOptions);

            return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                createKeyVaultSecret(response.getValue()));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates the attributes associated with the secret. The value of the secret in the key vault cannot be changed.
     * Only attributes populated in {@code secretProperties} are changed. Attributes not specified in the request are
     * not changed. This operation requires the {@code secrets/set} permission.
     *
     * <p>The secret properties {@link SecretProperties#getName() name} and
     * {@link SecretProperties#getVersion() version} cannot be {@code null}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the latest version of the secret, changes its expiry time, and the updates the secret in the key
     * vault.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.updateSecretProperties#secretProperties -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.updateSecretProperties#secretProperties -->
     *
     * @param secretProperties An object containing the secret properties to update.
     * @return The updated secret properties.
     *
     * @throws KeyVaultErrorException If a secret with the given {@link SecretProperties#getName()} and
     * {@link SecretProperties#getVersion()} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@link SecretProperties#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code secretProperties} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SecretProperties updateSecretProperties(SecretProperties secretProperties) {
        try (Response<SecretBundle> response = implClient.updateSecretWithResponse(secretProperties.getName(),
            secretProperties.getVersion(), prepareUpdateSecretParameters(secretProperties), RequestOptions.none())) {

            Objects.requireNonNull(secretProperties, "'secretProperties' cannot be null.");

            if (isNullOrEmpty(secretProperties.getName())) {
                throw new IllegalArgumentException("'secretProperties.getName()' cannot be null or empty.");
            }

            return createSecretProperties(response.getValue());
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Updates the attributes associated with the secret. The value of the secret in the key vault cannot be changed.
     * Only attributes populated in {@code secretProperties} are changed. Attributes not specified in the request are
     * not changed. This operation requires the {@code secrets/set} permission.
     *
     * <p>The secret properties {@link SecretProperties#getName() name} and
     * {@link SecretProperties#getVersion() version} cannot be {@code null}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the latest version of the secret, changes its expiry time, and the updates the secret in the key vault.
     * </p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.updateSecretPropertiesWithResponse#secretProperties-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.updateSecretPropertiesWithResponse#secretProperties-RequestOptions -->
     *
     * @param secretProperties An object containing the secret properties to update.
     * @param requestOptions Additional {@link RequestOptions options} that are passed through the HTTP pipeline during
     * the service call.
     * @return A response object whose {@link Response#getValue() value} contains the updated secret properties.
     *
     * @throws KeyVaultErrorException If a secret with the given {@link SecretProperties#getName()} and
     * {@link SecretProperties#getVersion()} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@link SecretProperties#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code secretProperties} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SecretProperties> updateSecretPropertiesWithResponse(SecretProperties secretProperties,
        RequestOptions requestOptions) {

        try (Response<SecretBundle> response = implClient.updateSecretWithResponse(secretProperties.getName(),
            secretProperties.getVersion(), prepareUpdateSecretParameters(secretProperties), requestOptions)) {

            Objects.requireNonNull(secretProperties, "'secretProperties' cannot be null.");

            if (isNullOrEmpty(secretProperties.getName())) {
                throw new IllegalArgumentException("'secretProperties.getName()' cannot be null or empty.");
            }

            return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                createSecretProperties(response.getValue()));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    private static SecretUpdateParameters prepareUpdateSecretParameters(SecretProperties secretProperties) {
        SecretUpdateParameters secretUpdateParameters = new SecretUpdateParameters();

        if (secretProperties != null) {
            secretUpdateParameters.setTags(secretProperties.getTags())
                .setContentType(secretProperties.getContentType())
                .setSecretAttributes(createSecretAttributes(secretProperties));
        }

        return secretUpdateParameters;
    }

    /**
     * Deletes a secret from the key vault. If soft-delete is enabled on the key vault then the secret is placed in the
     * deleted state and for permanent deletion, needs to be purged. Otherwise, the secret is permanently deleted.
     * All versions of a secret are deleted. This cannot be applied to individual versions of a secret. This operation
     * requires the {@code secrets/delete} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes the secret from a key vault <b>enabled for soft-delete</b>. Prints out the recovery id of the deleted
     * secret returned in the response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.deleteSecret#String -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.deleteSecret#String -->
     *
     * @param name The name of the secret to delete.
     * @return A poller object to poll with and retrieve the deleted secret.
     *
     * @throws KeyVaultErrorException If a secret with the given {@code name} doesn't exist in the key vault or if the
     * provided {@code name} is an empty string.
     */
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<DeletedSecret, Void> beginDeleteSecret(String name) {
        return Poller.createPoller(Duration.ofSeconds(1), deleteActivationOperation(name), deletePollOperation(name),
            (context, response) -> null, context -> null);
    }

    private Function<PollingContext<DeletedSecret>, PollResponse<DeletedSecret>> deleteActivationOperation(
        String name) {

        return pollingContext -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
            createDeletedSecret(implClient.deleteSecretWithResponse(name, RequestOptions.none()).getValue()));
    }

    private Function<PollingContext<DeletedSecret>, PollResponse<DeletedSecret>> deletePollOperation(String name) {
        return pollingContext -> {
            try {
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, createDeletedSecret(
                    implClient.getDeletedSecretWithResponse(name, RequestOptions.none()).getValue()));
            } catch (HttpResponseException e) {
                if (e.getResponse().getStatusCode() == 404) {
                    return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                        pollingContext.getLatestResponse().getValue());
                } else {
                    // This means either vault has soft-delete disabled or permission is not granted for the get deleted
                    // key operation. In both cases deletion operation was successful when activation operation
                    // succeeded before reaching here.
                    return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                        pollingContext.getLatestResponse().getValue());
                }
            } catch (Exception e) {
                // This means either vault has soft-delete disabled or permission is not granted for the get deleted
                // key operation. In both cases deletion operation was successful when activation operation
                // succeeded before reaching here.
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue());
            }
        };
    }*/

    /**
     * Gets a secret that has been deleted for a soft-delete enabled key vault. This operation requires the
     * {@code secrets/list} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the deleted secret from the key vault <b>enabled for soft-delete</b>. Prints out the details of the
     * deleted secret returned in the response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.getDeletedSecret#string -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.getDeletedSecret#string -->
     *
     * @param name The name of the deleted secret.
     * @return The deleted secret.
     *
     * @throws KeyVaultErrorException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeletedSecret getDeletedSecret(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return createDeletedSecret(implClient.getDeletedSecretWithResponse(name, RequestOptions.none()).getValue());
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets a secret that has been deleted for a soft-delete enabled key vault. This operation requires the
     * {@code secrets/list} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the deleted secret from the key vault <b>enabled for soft-delete</b>. Prints out the details of the
     * deleted secret returned in the response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.getDeletedSecretWithResponse#string-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.getDeletedSecretWithResponse#string-RequestOptions -->
     *
     * @param name The name of the deleted secret.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the deleted secret.
     *
     * @throws KeyVaultErrorException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeletedSecret> getDeletedSecretWithResponse(String name, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            Response<DeletedSecretBundle> response = implClient.getDeletedSecretWithResponse(name, requestOptions);

            return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                createDeletedSecret(response.getValue()));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Permanently removes a deleted secret, without the possibility of recovery. This operation can only be performed
     * on a <b>soft-delete enabled</b> key vault. This operation requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges the deleted secret from the key vault <b>enabled for soft-delete</b>. Prints out the status code from
     * the server response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecret#string -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecret#string -->
     *
     * @param name The name of the secret to purge.
     *
     * @throws KeyVaultErrorException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void purgeDeletedSecret(String name) {
        // Using try-with-resources to ensure the response is closed.
        try (Response<Void> response = purgeDeletedSecretWithResponse(name, RequestOptions.none())) {
            // Ignored
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Permanently removes a deleted secret, without the possibility of recovery. This operation can only be performed
     * on a <b>soft-delete enabled</b> key vault. This operation requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges the deleted secret from the key vault <b>enabled for soft-delete</b>. Prints out the status code from
     * the server response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecretWithResponse#string-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecretWithResponse#string-RequestOptions -->
     *
     * @param name The name of the secret to purge.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object containing the status code and headers related to the operation.
     *
     * @throws KeyVaultErrorException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> purgeDeletedSecretWithResponse(String name, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return implClient.purgeDeletedSecretWithResponse(name, requestOptions);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Recovers a deleted secret in the key vault to its latest version. Can only be performed on a <b>soft-delete
     * enabled</b> key vault. This operation requires the {@code secrets/recover} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recovers the deleted secret from a key vault enabled for <b>soft-delete</b>. Prints out the details of the
     * recovered secret returned in the response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.recoverDeletedSecret#String -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.recoverDeletedSecret#String -->
     *
     * @param name The name of the deleted secret to be recovered.
     * @return A poller object to poll with and retrieve the recovered secret.
     *
     * @throws KeyVaultErrorException If a secret with the given {@code name} doesn't exist in the key vault or if the
     * provided {@code name} is an empty string.
     */
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<KeyVaultSecret, Void> beginRecoverDeletedSecret(String name) {
        return Poller.createPoller(Duration.ofSeconds(1), recoverActivationOperation(name), recoverPollOperation(name),
            (context, response) -> null, context -> null);
    }

    private Function<PollingContext<KeyVaultSecret>, PollResponse<KeyVaultSecret>> recoverActivationOperation(
        String name) {

        return pollingContext -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
            createKeyVaultSecret(implClient.recoverDeletedSecretWithResponse(name, RequestOptions.none()).getValue()));
    }*/

    /*private Function<PollingContext<KeyVaultSecret>, PollResponse<KeyVaultSecret>> recoverPollOperation(String name) {
        return pollingContext -> {
            try {
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    createKeyVaultSecret(implClient.getSecretWithResponse(name, "", RequestOptions.none()).getValue()));
            } catch (HttpResponseException e) {
                if (e.getResponse().getStatusCode() == 404) {
                    return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                        pollingContext.getLatestResponse().getValue());
                } else {
                    // This means permission is not granted for the get deleted key operation. In both cases the
                    // deletion operation was successful when activation operation succeeded before reaching here.
                    return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                        pollingContext.getLatestResponse().getValue());
                }
            } catch (Exception e) {
                // This means permission is not granted for the get deleted key operation. In both cases the
                // deletion operation was successful when activation operation succeeded before reaching here.
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue());
            }
        };
    }*/

    /**
     * Requests a backup of the secret be downloaded. All versions of the secret will be downloaded. This operation
     * requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Backs up the secret from the key vault and prints out the length of the secret's backup byte array returned in
     * the response</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.backupSecret#string -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.backupSecret#string -->
     *
     * @param name The name of the secret to back up.
     * @return A byte array containing the backed up secret blob.
     *
     * @throws KeyVaultErrorException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] backupSecret(String name) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'name' cannot be null or empty."));
        }

        // Using try-with-resources to ensure the response is closed.
        try (Response<byte[]> response = backupSecretWithResponse(name, RequestOptions.none())) {
            return response.getValue();
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Requests a backup of the secret be downloaded. All versions of the secret will be downloaded. This operation
     * requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Backs up the secret from the key vault and prints out the length of the secret's backup byte array returned in
     * the response</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.backupSecretWithResponse#string-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.backupSecretWithResponse#string-RequestOptions -->
     *
     * @param name The name of the secret to back up.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the backed up secret blob.
     *
     * @throws KeyVaultErrorException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<byte[]> backupSecretWithResponse(String name, RequestOptions requestOptions) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'name' cannot be null or empty."));
        }

        // Using try-with-resources to ensure the response is closed.
        try (Response<BackupSecretResult> response = implClient.backupSecretWithResponse(name, requestOptions)) {
            return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                response.getValue().getValue());
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Restores a backed up secret and all its versions to a vault. This operation requires the {@code secrets/restore}
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Restores a secret in the key vault from its backup byte array. Prints out the details of the restored secret
     * returned in the response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.restoreSecret#byte -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.restoreSecret#byte -->
     *
     * @param backup The backup blob associated with the secret.
     * @return The restored secret.
     *
     * @throws KeyVaultErrorException If the {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret restoreSecretBackup(byte[] backup) {
        try (Response<SecretBundle> response = implClient.restoreSecretWithResponse(new SecretRestoreParameters(backup),
            RequestOptions.none())) {

            return createKeyVaultSecret(response.getValue());
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Restores a backed up secret and all its versions to a vault. This operation requires the {@code secrets/restore}
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Restores a secret in the key vault from its backup byte array. Prints out the details of the restored secret
     * returned in the response.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.restoreSecretWithResponse#byte-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.restoreSecretWithResponse#byte-RequestOptions -->
     *
     * @param backup The backup blob associated with the secret.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the restored secret.
     *
     * @throws KeyVaultErrorException If the {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSecret> restoreSecretBackupWithResponse(byte[] backup, RequestOptions requestOptions) {
        try (Response<SecretBundle> response = implClient.restoreSecretWithResponse(new SecretRestoreParameters(backup),
            requestOptions)) {

            return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                createKeyVaultSecret(response.getValue()));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Lists secrets in the key vault. Each secret returned only has its identifier and attributes populated. The secret
     * values and their versions are not listed in the response. This operation requires the {@code secrets/list}
     * permission.
     *
     * <p><strong>Iterate through secrets and fetch their latest value</strong></p>
     * <p>The snippet below loops over each {@link SecretProperties secret properties object} and calls
     * {@link #getSecret(String, String)}. This gets the corresponding {@link KeyVaultSecret secret object} and the
     * value of its latest version.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listSecrets -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listSecrets -->
     *
     * <p><strong>Iterate over secrets by page</strong></p>
     * <p>The snippet below loops over each {@link SecretProperties secret properties object} by page and calls
     * {@link #getSecret(String, String)}. This gets the corresponding {@link KeyVaultSecret secret object} and the
     * value of its latest version.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listSecrets.iterableByPage -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listSecrets.iterableByPage -->
     *
     * @return A {@link PagedIterable} of properties objects of all the secrets in the vault. A properties object
     * contains all the information about the secret, except its value.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SecretProperties> listPropertiesOfSecrets() {
        return listPropertiesOfSecrets(RequestOptions.none());
    }

    /**
     * Lists secrets in the key vault. Each secret returned only has its identifier and attributes populated. The secret
     * values and their versions are not listed in the response. This operation requires the {@code secrets/list}
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>The snippet below loops over each {@link SecretProperties secret properties object} and calls
     * {@link #getSecret(String, String)}. This gets the corresponding {@link KeyVaultSecret secret object} and the
     * value of its latest version.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listSecrets#Context -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listSecrets#Context -->
     *
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of properties objects of all the secrets in the vault. A properties object
     * contains all the information about the secret, except its value.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SecretProperties> listPropertiesOfSecrets(RequestOptions requestOptions) {
        try {
            RequestOptions requestOptionsForNextPage = new RequestOptions();

            requestOptionsForNextPage.setContext(requestOptions != null && requestOptions.getContext() != null
                ? requestOptions.getContext()
                : Context.none());

            return mapPage((pagingOptions) -> implClient.getSecretsSinglePage(null, requestOptions),
                (pagingOptions, nextLink) -> implClient.getSecretsNextSinglePage(nextLink, requestOptionsForNextPage),
                SecretsModelsUtils::createSecretProperties);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Lists deleted secrets of the key vault if it is <b>soft-delete enabled</b>. This operation requires the
     * {@code secrets/list} permission.
     *
     * <p><strong>Iterate through deleted secrets</strong></p>
     * <p>Lists the deleted secrets in the key vault and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets -->
     *
     * <p><strong>Iterate through deleted secrets by page</strong></p>
     * <p>Lists the deleted secrets by page in the key vault and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets.iterableByPage -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets.iterableByPage -->
     *
     * @return A {@link PagedIterable} of deleted secrets in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedSecret> listDeletedSecrets() {
        return listDeletedSecrets(RequestOptions.none());
    }

    /**
     * Lists deleted secrets of the key vault if it is <b>soft-delete enabled</b>. This operation requires the
     * {@code secrets/list} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Lists the deleted secrets in the key vault and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets#Context -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets#Context -->
     *
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} deleted secrets in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedSecret> listDeletedSecrets(RequestOptions requestOptions) {
        try {
            RequestOptions requestOptionsForNextPage = new RequestOptions();

            requestOptionsForNextPage.setContext(requestOptions != null && requestOptions.getContext() != null
                ? requestOptions.getContext()
                : Context.none());

            return mapPage((pagingOptions) -> implClient.getDeletedSecretsSinglePage(null, requestOptions),
                (pagingOptions, nextLink) -> implClient.getDeletedSecretsNextSinglePage(nextLink,
                    requestOptionsForNextPage), SecretsModelsUtils::createDeletedSecret);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Lists all versions of the specified secret. Each returned {@link SecretProperties secret properties object} only
     * has its identifier and attributes populated. The secret values and secret versions are not listed in the
     * response. This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>The sample below fetches all versions of a given secret. For each secret version retrieved, it makes a call to
     * {@link #getSecret(String, String)} to get the version's value, and then prints it out.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listSecretVersions#string -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listSecretVersions#string -->
     *
     * @param name The name of the secret.
     * @return {@link PagedIterable} of {@link SecretProperties secret properties objects} of all the versions of the
     * specified secret in the vault. The list is empty if a secret with the given {@code name} does not exist in key
     * vault.
     *
     * @throws KeyVaultErrorException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SecretProperties> listPropertiesOfSecretVersions(String name) {
        return listPropertiesOfSecretVersions(name, RequestOptions.none());
    }

    /**
     * Lists all versions of the specified secret. Each returned {@link SecretProperties secret properties object} only
     * has its identifier and attributes populated. The secret values and secret versions are not listed in the
     * response. This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Iterate through secret versions</strong></p>
     * <p>The sample below fetches all versions of a given secret. For each secret version retrieved, it makes a call to
     * {@link #getSecret(String, String)} to get the version's value, and then prints it out.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listSecretVersions#string-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listSecretVersions#string-RequestOptions -->
     *
     * <p><strong>Iterate through secret versions by page</strong></p>
     * <p>The sample below iterates over all versions of a given secret by page. For each secret version retrieved, it
     * makes a call to {@link #getSecret(String, String)} to get the version's value, and then prints it out.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listSecretVersions#string-RequestOptions-iterableByPage -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listSecretVersions#string-RequestOptions-iterableByPage -->
     *
     * @param name The name of the secret.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return {@link PagedIterable} of {@link SecretProperties secret properties objects} of all the versions of the
     * specified secret in the vault. The list is empty if a secret with the given {@code name} does not exist in key
     * vault.
     *
     * @throws KeyVaultErrorException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SecretProperties> listPropertiesOfSecretVersions(String name, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            RequestOptions requestOptionsForNextPage = new RequestOptions();

            requestOptionsForNextPage.setContext(requestOptions != null && requestOptions.getContext() != null
                ? requestOptions.getContext()
                : Context.none());

            return mapPage((pagingOptions) -> implClient.getSecretVersionsSinglePage(name, null, requestOptions),
                (pagingOptions, nextLink) -> implClient.getSecretVersionsNextSinglePage(nextLink,
                    requestOptionsForNextPage), SecretsModelsUtils::createSecretProperties);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    public <T, S> PagedIterable<S> mapPage(Function<PagingOptions, PagedResponse<T>> firstPageRetriever,
        BiFunction<PagingOptions, String, PagedResponse<T>> nextPageRetriever, Function<T, S> mapper) {

        return new PagedIterable<>(pageSize -> mapPagedResponse(firstPageRetriever.apply(pageSize), mapper),
            (continuationToken, pageSize) -> mapPagedResponse(nextPageRetriever.apply(continuationToken, pageSize),
                mapper));
    }

    private static <T, S> PagedResponse<S> mapPagedResponse(PagedResponse<T> pagedResponse, Function<T, S> mapper) {
        if (pagedResponse == null) {
            return null;
        }

        return new PagedResponse<>(pagedResponse.getRequest(), pagedResponse.getStatusCode(),
            pagedResponse.getHeaders(),
            pagedResponse.getValue()
                .stream()
                .map(mapper)
                .collect(Collectors.toCollection(() -> new ArrayList<>(pagedResponse.getValue().size()))),
            pagedResponse.getContinuationToken(), null, null, null, null);
    }
}
