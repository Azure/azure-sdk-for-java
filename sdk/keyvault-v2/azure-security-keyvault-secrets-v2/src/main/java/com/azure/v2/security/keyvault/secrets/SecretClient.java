// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.PollingContext;
import com.azure.v2.security.keyvault.secrets.implementation.SecretClientImpl;
import com.azure.v2.security.keyvault.secrets.implementation.models.BackupSecretResult;
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
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.paging.PagedIterable;
import io.clientcore.core.http.paging.PagedResponse;
import io.clientcore.core.http.paging.PagingOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.v2.security.keyvault.secrets.implementation.models.SecretsModelsUtils.createDeletedSecret;
import static com.azure.v2.security.keyvault.secrets.implementation.models.SecretsModelsUtils.createKeyVaultSecret;
import static com.azure.v2.security.keyvault.secrets.implementation.models.SecretsModelsUtils.createSecretAttributes;
import static com.azure.v2.security.keyvault.secrets.implementation.models.SecretsModelsUtils.createSecretProperties;
import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * This class provides methods to manage secrets in Azure Key Vault. The client supports creating, retrieving, updating,
 * deleting, purging, backing up, restoring, and listing the secrets. The client also supports listing deleted secrets
 * for a key vault enabled for soft-delete.
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
 * <p><strong>Sample: Construct Secret Client</strong></p>
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
 * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.setSecret#String-String -->
 * <!-- end com.azure.v2.security.keyvault.SecretClient.setSecret#String-String -->
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
 * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.getSecret#String -->
 * <!-- end com.azure.v2.security.keyvault.SecretClient.getSecret#String -->
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

    private final SecretClientImpl clientImpl;

    /**
     * Creates an instance of {@link SecretClient} that sends requests to the given endpoint.
     *
     * @param clientImpl The implementation client.
     */
    SecretClient(SecretClientImpl clientImpl) {
        this.clientImpl = clientImpl;
    }

    /**
     * Adds a secret to the key vault if it does not exist. If a secret with the provided name already exists, a new
     * version of the secret is created. This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new secret in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.setSecret#String-String -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.setSecret#String-String -->
     *
     * @param name The name of the secret. It is required and cannot be {@code null}.
     * @param value The value of the secret. It is required and cannot be {@code null}.
     * @return The newly created secret.
     *
     * @throws HttpResponseException If either of the provided {@code name} or {@code value} is invalid.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret setSecret(String name, String value) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'name' cannot be null or empty."));
        }

        return setSecret(new KeyVaultSecret(name, value));
    }

    /**
     * Adds a secret to the key vault if it does not exist. If the named secret exists, a new version of the secret is
     * created. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@code secret} parameter and its {@link KeyVaultSecret#getName() name} value are required. The
     * {@link SecretProperties#getExpiresOn() expires}, {@link SecretProperties#getContentType() contentType}, and
     * {@link SecretProperties#getNotBefore() notBefore} values in the provided {@link KeyVaultSecret secret object}
     * are optional. If not specified, {@link SecretProperties#isEnabled() enabled} is set to true by key vault.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new secret in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.setSecret#secret -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.setSecret#secret -->
     *
     * @param secret The {@link KeyVaultSecret secret object} containing information about the secret and its
     * properties. It is required and cannot be {@code null}.
     * @return The newly created secret.
     *
     * @throws HttpResponseException If the provided {@code secret} is malformed.
     * @throws IllegalArgumentException If {@link KeyVaultSecret#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If the provided {@code secret} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret setSecret(KeyVaultSecret secret) {
        try {
            Objects.requireNonNull(secret, "'secret' cannot be null.");

            if (isNullOrEmpty(secret.getName())) {
                throw new IllegalArgumentException("'secret.getName()' cannot be null or empty.");
            }

            return createKeyVaultSecret(clientImpl.setSecret(secret.getName(), prepareSecretSetParameters(secret)));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Adds a secret to the key vault if it does not exist. If the named secret exists, a new version of the secret is
     * created. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@code secret} parameter and its {@link KeyVaultSecret#getName() name} value are required. The
     * {@link SecretProperties#getExpiresOn() expires}, {@link SecretProperties#getContentType() contentType}, and
     * {@link SecretProperties#getNotBefore() notBefore} values in the provided {@link KeyVaultSecret secret object}
     * are optional. If not specified, {@link SecretProperties#isEnabled() enabled} is set to true by key vault.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new secret in the key vault. Prints out details of the response returned by the service and the
     * newly created secret.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.setSecretWithResponse#secret-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.setSecretWithResponse#secret-RequestContext -->
     *
     * @param secret The {@link KeyVaultSecret secret object} containing information about the secret and its
     * properties. It is required and cannot be {@code null}.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the newly created secret.
     *
     * @throws HttpResponseException If the provided {@code secret} is malformed.
     * @throws IllegalArgumentException If {@link KeyVaultSecret#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If the provided {@code secret} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSecret> setSecretWithResponse(KeyVaultSecret secret, RequestContext requestContext) {
        try {
            Objects.requireNonNull(secret, "'secret' cannot be null.");

            if (isNullOrEmpty(secret.getName())) {
                throw new IllegalArgumentException("'secret.getName()' cannot be null or empty.");
            }

            return mapResponse(clientImpl.setSecretWithResponse(secret.getName(),
                prepareSecretSetParameters(secret), requestContext), SecretsModelsUtils::createKeyVaultSecret);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
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
     * <p>Gets the latest version of a secret in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.getSecret#String -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.getSecret#String -->
     *
     * @param name The name of the secret.
     * @return The requested secret.
     *
     * @throws HttpResponseException If a secret with the given {@code name} doesn't exist in the key vault.
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
     * <p>Gets a specific version of a secret in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.getSecret#String-String -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.getSecret#String-String -->
     *
     * @param name The name of the secret, cannot be {@code null}.
     * @param version The version of the secret to retrieve. If this is an empty string or {@code null}, this call is
     * equivalent to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @return The requested secret.
     *
     * @throws HttpResponseException If a secret with the given {@code name} and {@code version} doesn't exist in the
     * key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret getSecret(String name, String version) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return createKeyVaultSecret(clientImpl.getSecret(name, version));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets the specified secret with specified version from the key vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific version of a secret in the key vault. Prints out details of the response returned by the
     * service and the requested secret.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.getSecretWithResponse#String-String-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.getSecretWithResponse#String-String-RequestContext -->
     *
     * @param name The name of the secret, cannot be {@code null}
     * @param version The version of the secret to retrieve. If this is an empty string or {@code null}, this call is
     * equivalent to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the requested secret.
     *
     * @throws HttpResponseException If a secret with the given {@code name} and {@code version} doesn't exist in the
     * vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSecret> getSecretWithResponse(String name, String version, RequestContext requestContext) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return mapResponse(clientImpl.getSecretWithResponse(name, version, requestContext),
                SecretsModelsUtils::createKeyVaultSecret);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates the attributes associated with the secret. The value of the secret in the key vault cannot be changed.
     * Only attributes populated in {@code secretProperties} are changed. Attributes not specified in the request are
     * not changed. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@code secretProperties} parameter and its {@link SecretProperties#getName() name} value are required.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the latest version of a secret and updates its expiry time in the key vault, then prints out the updated
     * secret's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.updateSecretProperties#secretProperties -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.updateSecretProperties#secretProperties -->
     *
     * @param secretProperties An object containing the secret properties to update. It is required and cannot be
     * {@code null}.
     * @return The updated secret properties.
     *
     * @throws HttpResponseException If a secret with the given {@link SecretProperties#getName() name} and
     * {@link SecretProperties#getVersion() version} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@link SecretProperties#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code secretProperties} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SecretProperties updateSecretProperties(SecretProperties secretProperties) {
        try {
            Objects.requireNonNull(secretProperties, "'secretProperties' cannot be null.");

            if (isNullOrEmpty(secretProperties.getName())) {
                throw new IllegalArgumentException("'secretProperties.getName()' cannot be null or empty.");
            }

            return createSecretProperties(
                clientImpl.updateSecret(secretProperties.getName(), secretProperties.getVersion(),
                    prepareUpdateSecretParameters(secretProperties)));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates the attributes associated with the secret. The value of the secret in the key vault cannot be changed.
     * Only attributes populated in {@code secretProperties} are changed. Attributes not specified in the request are
     * not changed. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@code secretProperties} parameter and its {@link SecretProperties#getName() name} value are required.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the latest version of a secret and updates its expiry time in the key vault. Prints out details of the
     * response returned by the service and the updated secret.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.updateSecretPropertiesWithResponse#secretProperties-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.updateSecretPropertiesWithResponse#secretProperties-RequestContext -->
     *
     * @param secretProperties An object containing the secret properties to update. It is required and cannot be
     * {@code null}.
     * @param requestContext Additional {@link RequestContext options} that are passed through the HTTP pipeline during
     * the service call.
     * @return A response object whose {@link Response#getValue() value} contains the updated secret properties.
     *
     * @throws HttpResponseException If a secret with the given {@link SecretProperties#getName() name} and
     * {@link SecretProperties#getVersion() version} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@link SecretProperties#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code secretProperties} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SecretProperties> updateSecretPropertiesWithResponse(SecretProperties secretProperties,
        RequestContext requestContext) {

        try {
            Objects.requireNonNull(secretProperties, "'secretProperties' cannot be null.");

            if (isNullOrEmpty(secretProperties.getName())) {
                throw new IllegalArgumentException("'secretProperties.getName()' cannot be null or empty.");
            }

            return mapResponse(
                clientImpl.updateSecretWithResponse(secretProperties.getName(), secretProperties.getVersion(),
                    prepareUpdateSecretParameters(secretProperties), requestContext),
                SecretsModelsUtils::createSecretProperties);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
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
     * deleted state and requires to be purged for permanent deletion. Otherwise, the secret is permanently deleted.
     * All versions of a secret are deleted. This cannot be applied to individual versions of a secret. This operation
     * requires the {@code secrets/delete} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes the secret from a key vault enabled for soft-delete and prints out its recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.deleteSecret#String -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.deleteSecret#String -->
     *
     * @param name The name of the secret to delete.
     * @return A poller object to poll with and retrieve the deleted secret.
     *
     * @throws HttpResponseException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    // TODO (vcolin7): Uncomment when creating a Poller is supported in azure-core-v2.
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<DeletedSecret, Void> beginDeleteSecret(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return Poller.createPoller(Duration.ofSeconds(1),
                pollingContext -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    createDeletedSecret(clientImpl.deleteSecret(name))),
                pollingContext -> deletePollOperation(name, pollingContext),
                (pollingContext, response) -> null,
                pollingContext -> null);
        } catch (HttpResponseException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }*/

    private PollResponse<DeletedSecret> deletePollOperation(String name, PollingContext<DeletedSecret> pollingContext) {
        try {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, createDeletedSecret(
                clientImpl.getDeletedSecret(name)));
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
        } catch (RuntimeException e) {
            // This means either vault has soft-delete disabled or permission is not granted for the get deleted
            // key operation. In both cases deletion operation was successful when activation operation
            // succeeded before reaching here.
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                pollingContext.getLatestResponse().getValue());
        }
    }

    /**
     * Gets a secret that has been deleted in a key vault <b>enabled for soft-delete</b>. This operation requires the
     * {@code secrets/list} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a deleted secret from a key vault enabled for soft-delete and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.getDeletedSecret#String -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.getDeletedSecret#String -->
     *
     * @param name The name of the deleted secret.
     * @return The deleted secret.
     *
     * @throws HttpResponseException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeletedSecret getDeletedSecret(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return createDeletedSecret(clientImpl.getDeletedSecret(name));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets a secret that has been deleted in a key vault <b>enabled for soft-delete</b>. This operation requires the
     * {@code secrets/list} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the deleted secret from a key vault enabled for soft-delete. Prints out details of the response returned
     * by the service and the deleted secret.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.getDeletedSecretWithResponse#String-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.getDeletedSecretWithResponse#String-RequestContext -->
     *
     * @param name The name of the deleted secret.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the deleted secret.
     *
     * @throws HttpResponseException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeletedSecret> getDeletedSecretWithResponse(String name, RequestContext requestContext) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return mapResponse(clientImpl.getDeletedSecretWithResponse(name, requestContext),
                SecretsModelsUtils::createDeletedSecret);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Permanently removes a deleted secret without the possibility of recovery. This operation can only be performed
     * on a key vault <b>enabled for soft-delete</b> and requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges a deleted secret from a key vault enabled for soft-delete.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecret#String -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecret#String -->
     *
     * @param name The name of the secret to purge.
     *
     * @throws HttpResponseException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void purgeDeletedSecret(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            clientImpl.purgeDeletedSecret(name);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Permanently removes a deleted secret without the possibility of recovery. This operation can only be performed
     * on a key vault <b>enabled for soft-delete</b> and requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges a deleted secret from a key vault enabled for soft-delete and prints out details of the response
     * returned by the service.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecretWithResponse#String-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecretWithResponse#String-RequestContext -->
     *
     * @param name The name of the secret to purge.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object containing the status code and headers related to the operation.
     *
     * @throws HttpResponseException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> purgeDeletedSecretWithResponse(String name, RequestContext requestContext) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return clientImpl.purgeDeletedSecretWithResponse(name, requestContext);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Recovers a deleted secret in the key vault to its latest version. Can only be performed on a key vault <b>enabled
     * for soft-delete</b>. This operation requires the {@code secrets/recover} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recovers a deleted secret from a key vault enabled for soft-delete and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.recoverDeletedSecret#String -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.recoverDeletedSecret#String -->
     *
     * @param name The name of the deleted secret to be recovered.
     * @return A poller object to poll with and retrieve the recovered secret.
     *
     * @throws HttpResponseException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    // TODO (vcolin7): Uncomment when creating a Poller is supported in azure-core-v2.
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<KeyVaultSecret, Void> beginRecoverDeletedSecret(String name) {
        if (isNullOrEmpty(name)) {
            throw new IllegalArgumentException("'name' cannot be null or empty.");
        }

        try {
            return Poller.createPoller(Duration.ofSeconds(1),
                pollingContext -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    createKeyVaultSecret(clientImpl.recoverDeletedSecret(name))),
                pollingContext -> recoverPollOperation(name, pollingContext),
                (pollingContext, response) -> null,
                pollingContext -> null);
        } catch (HttpResponseException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }*/

    private PollResponse<KeyVaultSecret> recoverPollOperation(String name,
        PollingContext<KeyVaultSecret> pollingContext) {

        try {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                createKeyVaultSecret(clientImpl.getSecret(name, "")));
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
        } catch (RuntimeException e) {
            // This means permission is not granted for the get deleted key operation. In both cases the
            // deletion operation was successful when activation operation succeeded before reaching here.
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                pollingContext.getLatestResponse().getValue());
        }
    }

    /**
     * Requests a backup of the secret be downloaded. All versions of the secret will be downloaded. This operation
     * requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Backs up a secret from the key vault and prints out the length of the secret's backup blob.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.backupSecret#String -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.backupSecret#String -->
     *
     * @param name The name of the secret to back up.
     * @return A byte array containing the backed up secret blob.
     *
     * @throws HttpResponseException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] backupSecret(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return clientImpl.backupSecret(name).getValue();
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Requests a backup of the secret be downloaded. All versions of the secret will be downloaded. This operation
     * requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Backs up a secret from the key vault. Prints out details of the response returned by the service and the
     * length of the secret's backup blob.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.backupSecretWithResponse#String-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.backupSecretWithResponse#String-RequestContext -->
     *
     * @param name The name of the secret to back up.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the backed up secret blob.
     *
     * @throws HttpResponseException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<byte[]> backupSecretWithResponse(String name, RequestContext requestContext) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return mapResponse(clientImpl.backupSecretWithResponse(name, requestContext), BackupSecretResult::getValue);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Restores a backed up secret and all its versions to a vault. All versions of the secret are restored to the
     * vault. This operation requires the {@code secrets/restore} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Restores a secret in the key vault from a backup and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.restoreSecret#byte -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.restoreSecret#byte -->
     *
     * @param backup The backup blob associated with the secret.
     * @return The restored secret.
     *
     * @throws HttpResponseException If the {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret restoreSecretBackup(byte[] backup) {
        try {
            return createKeyVaultSecret(clientImpl.restoreSecret(new SecretRestoreParameters(backup)));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Restores a backed up secret and all its versions to a vault. All versions of the secret are restored to the
     * vault. This operation requires the {@code secrets/restore} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Restores a secret in the key vault from a backup. Prints our details of the response returned by the service
     * and the restored secret.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.restoreSecretWithResponse#byte-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.restoreSecretWithResponse#byte-RequestContext -->
     *
     * @param backup The backup blob associated with the secret.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the restored secret.
     *
     * @throws HttpResponseException If the {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSecret> restoreSecretBackupWithResponse(byte[] backup, RequestContext requestContext) {
        try {
            return mapResponse(clientImpl.restoreSecretWithResponse(new SecretRestoreParameters(backup),
                requestContext), SecretsModelsUtils::createKeyVaultSecret);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Lists all secrets in the key vault. Each secret is represented by a properties object containing the secret
     * identifier and attributes. The secret values and their versions are not listed in the response. This operation
     * requires the {@code secrets/list} permission.
     *
     * <p><strong>Iterate through secrets</strong></p>
     * <p>Lists the secrets in the key vault and gets the value for each one's latest version by looping though the
     * properties objects and calling {@link SecretClient#getSecret(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets -->
     *
     * <p><strong>Iterate through secrets by page</strong></p>
     * <p>Iterates through the secrets in the key vault by page and gets the value for each one's latest version by
     * looping though the properties objects and calling {@link SecretClient#getSecret(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets.iterableByPage -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets.iterableByPage -->
     *
     * @return A {@link PagedIterable} of properties objects of all the secrets in the vault. A properties object
     * contains all the information about the secret, except its value.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SecretProperties> listPropertiesOfSecrets() {
        return listPropertiesOfSecrets(RequestContext.none());
    }

    /**
     * Lists all secrets in the key vault. Each secret is represented by a properties object containing the secret
     * identifier and attributes. The secret values and their versions are not listed in the response. This operation
     * requires the {@code secrets/list} permission.
     *
     * <p><strong>Iterate through secrets</strong></p>
     * <p>Lists the secrets in the key vault and gets the value for each one's latest version by looping though the
     * properties objects and calling {@link SecretClient#getSecret(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets#RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets#RequestContext -->
     *
     * <p><strong>Iterate through secrets by page</strong></p>
     * <p>Iterates through the secrets in the key vault by page and gets the value for each one's latest version by
     * looping though the properties objects and calling {@link SecretClient#getSecret(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets.iterableByPage#RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets.iterableByPage#RequestContext -->
     *
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of properties objects of all the secrets in the vault. A properties object
     * contains all the information about the secret, except its value.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SecretProperties> listPropertiesOfSecrets(RequestContext requestContext) {
        try {
            return mapPages(pagingOptions -> clientImpl.getSecretsSinglePage(null, requestContext),
                (pagingOptions, nextLink) -> clientImpl.getSecretsNextSinglePage(nextLink,
                    requestContext.toBuilder().build()),
                SecretsModelsUtils::createSecretProperties);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Lists all deleted secrets in the key vault currently available for recovery. This operation is applicable for key
     * vaults <b>enabled for soft-delete</b> and requires the {@code secrets/list} permission.
     *
     * <p><strong>Iterate through deleted secrets</strong></p>
     * <p>Lists the deleted secrets in a key vault enabled for soft-delete and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets -->
     *
     * <p><strong>Iterate through deleted secrets by page</strong></p>
     * <p>Iterates through the deleted secrets in the key vault by page and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets.iterableByPage -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets.iterableByPage -->
     *
     * @return A {@link PagedIterable} of deleted secrets in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedSecret> listDeletedSecrets() {
        return listDeletedSecrets(RequestContext.none());
    }

    /**
     * Lists all deleted secrets in the key vault currently available for recovery. This operation is applicable for key
     * vaults <b>enabled for soft-delete</b> and requires the {@code secrets/list} permission.
     *
     * <p><strong>Iterate through deleted secrets</strong></p>
     * <p>Lists the deleted secrets in a key vault enabled for soft-delete and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets#RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets#RequestContext -->
     *
     * <p><strong>Iterate through deleted secrets by page</strong></p>
     * <p>Iterates through the deleted secrets in the key vault by page and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets.iterableByPage#RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets.iterableByPage#RequestContext -->
     *
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} deleted secrets in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedSecret> listDeletedSecrets(RequestContext requestContext) {
        try {
            Stream<PagedResponse<DeletedSecret>> stream = clientImpl.getDeletedSecrets(null, requestContext)
                .streamByPage()
                .map(pagedResponse -> mapPagedResponse(pagedResponse, SecretsModelsUtils::createDeletedSecret));

            return mapPages(pagingOptions -> clientImpl.getDeletedSecretsSinglePage(null, requestContext),
                (pagingOptions, nextLink) -> clientImpl.getDeletedSecretsNextSinglePage(nextLink,
                    requestContext.toBuilder().build()), SecretsModelsUtils::createDeletedSecret);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Lists all versions of the specified secret in the key vault. Each version is represented by a properties object
     * containing the secret identifier and attributes. The secret values are not listed in the response. This operation
     * requires the {@code secrets/list} permission.
     *
     * <p><strong>Iterate through secret versions</strong></p>
     * <p>Lists the versions of a secret in the key vault and gets each one's value by looping though the properties
     * objects and calling {@link SecretClient#getSecret(String, String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions#String -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions#String -->
     *
     * <p><strong>Iterate through secret versions by page</strong></p>
     * <p>The sample below iterates through the versions of a secret in the key vault by page and gets each one's value
     * by looping though the properties objects and calling {@link SecretClient#getSecret(String, String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions.iterableByPage#String -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions.iterableByPage#String -->
     *
     * @param name The name of the secret.
     * @return {@link PagedIterable} of properties objects of all the versions of the specified secret in the vault. A
     * properties object contains all the information about the secret, except its value. The {@link PagedIterable} will
     * be empty if no secret with the given {@code name} exists in key vault.
     *
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SecretProperties> listPropertiesOfSecretVersions(String name) {
        return listPropertiesOfSecretVersions(name, RequestContext.none());
    }

    /**
     * Lists all versions of the specified secret in the key vault. Each version is represented by a properties object
     * containing the secret identifier and attributes. The secret values are not listed in the response. This operation
     * requires the {@code secrets/list} permission.
     *
     * <p><strong>Iterate through secret versions</strong></p>
     * <p>Lists the versions of a secret in the key vault and gets each one's value by looping though the properties
     * objects and calling {@link SecretClient#getSecret(String, String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions#String-RequestContext-->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions#String-RequestContext -->
     *
     * <p><strong>Iterate through secret versions by page</strong></p>
     * <p>The sample below iterates through the versions of a secret in the key vault by page and gets each one's value
     * by looping though the properties objects and calling {@link SecretClient#getSecret(String, String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions.iterableByPage#String-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions.iterableByPage#String-RequestContext -->
     *
     * @param name The name of the secret.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return {@link PagedIterable} of properties objects of all the versions of the specified secret in the vault. A
     * properties object contains all the information about the secret, except its value. The {@link PagedIterable} will
     * be empty if no secret with the given {@code name} exists in key vault.
     *
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SecretProperties> listPropertiesOfSecretVersions(String name, RequestContext requestContext) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return mapPages(pagingOptions -> clientImpl.getSecretVersionsSinglePage(name, null, requestContext),
                (pagingOptions, nextLink) -> clientImpl.getSecretVersionsNextSinglePage(nextLink,
                    requestContext.toBuilder().build()), SecretsModelsUtils::createSecretProperties);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    private static <T, S> Response<S> mapResponse(Response<T> response, Function<T, S> mapper) {
        if (response == null) {
            return null;
        }

        return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            mapper.apply(response.getValue()));
    }

    private static  <T, S> PagedIterable<S> mapPages(Function<PagingOptions, PagedResponse<T>> firstPageRetriever,
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
