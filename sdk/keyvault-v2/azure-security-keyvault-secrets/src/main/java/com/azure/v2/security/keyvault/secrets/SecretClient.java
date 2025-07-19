// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
import com.azure.v2.core.http.polling.PollingContext;
import com.azure.v2.security.keyvault.secrets.implementation.SecretClientImpl;
import com.azure.v2.security.keyvault.secrets.implementation.models.BackupSecretResult;
import com.azure.v2.security.keyvault.secrets.implementation.models.DeletedSecretBundle;
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
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.paging.PagedIterable;
import io.clientcore.core.http.paging.PagedResponse;
import io.clientcore.core.http.paging.PagingOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.time.Duration;
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
 * <!-- src_embed com.azure.v2.security.keyvault.secrets.SecretClient.instantiation -->
 * <pre>
 * SecretClient secretClient = new SecretClientBuilder&#40;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .endpoint&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.secrets.SecretClient.instantiation -->
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
 * <pre>
 * KeyVaultSecret secret = secretClient.setSecret&#40;&quot;secretName&quot;, &quot;secretValue&quot;&#41;;
 *
 * System.out.printf&#40;&quot;Set secret with name '%s' and value '%s'%n&quot;, secret.getName&#40;&#41;, secret.getValue&#40;&#41;&#41;;
 * </pre>
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
 * <pre>
 * KeyVaultSecret secret = secretClient.getSecret&#40;&quot;secretName&quot;&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved secret with name '%s' and value '%s'%n&quot;, secret.getName&#40;&#41;, secret.getValue&#40;&#41;&#41;;
 * </pre>
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
 * <pre>
 * Poller&lt;DeletedSecret, Void&gt; deleteSecretPoller = secretClient.beginDeleteSecret&#40;&quot;secretName&quot;&#41;;
 *
 * &#47;&#47; Deleted Secret is accessible as soon as polling begins.
 * PollResponse&lt;DeletedSecret&gt; deleteSecretPollResponse = deleteSecretPoller.poll&#40;&#41;;
 *
 * &#47;&#47; Deletion date only works for a soft-delete enabled key vault.
 * System.out.printf&#40;&quot;Deleted secret's recovery id: '%s'. Deleted date: '%s'.&quot;,
 *     deleteSecretPollResponse.getValue&#40;&#41;.getRecoveryId&#40;&#41;, deleteSecretPollResponse.getValue&#40;&#41;.getDeletedOn&#40;&#41;&#41;;
 *
 * &#47;&#47; Secret is being deleted on server.
 * deleteSecretPoller.waitForCompletion&#40;&#41;;
 * </pre>
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
     * <pre>
     * KeyVaultSecret secret = secretClient.setSecret&#40;&quot;secretName&quot;, &quot;secretValue&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Set secret with name '%s' and value '%s'%n&quot;, secret.getName&#40;&#41;, secret.getValue&#40;&#41;&#41;;
     * </pre>
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
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
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
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.setSecret#KeyVaultSecret -->
     * <pre>
     * KeyVaultSecret secretToSet = new KeyVaultSecret&#40;&quot;secretName&quot;, &quot;secretValue&quot;&#41;
     *     .setProperties&#40;new SecretProperties&#40;&#41;.setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;60&#41;&#41;&#41;;
     * KeyVaultSecret returnedSecret = secretClient.setSecret&#40;secretToSet&#41;;
     *
     * System.out.printf&#40;&quot;Set secret with name '%s' and value '%s'%n&quot;, returnedSecret.getName&#40;&#41;,
     *     returnedSecret.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.SecretClient.setSecret#KeyVaultSecret -->
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
        Objects.requireNonNull(secret, "'secret' cannot be null.");

        if (isNullOrEmpty(secret.getName())) {
            throw LOGGER.throwableAtError()
                .log("'secret.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        try (Response<SecretBundle> response = clientImpl.setSecretWithResponse(secret.getName(),
            prepareSecretSetParameters(secret), RequestContext.none())) {

            return createKeyVaultSecret(response.getValue());
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
     * <pre>
     * KeyVaultSecret secretToSet = new KeyVaultSecret&#40;&quot;secretName&quot;, &quot;secretValue&quot;&#41;
     *     .setProperties&#40;new SecretProperties&#40;&#41;.setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;60&#41;&#41;&#41;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultSecret&gt; response = secretClient.setSecretWithResponse&#40;secretToSet, requestContext&#41;;
     *     
     * System.out.printf&#40;&quot;Received response with status code %d and headers: %s%n&quot;, response.getStatusCode&#40;&#41;,
     *     response.getHeaders&#40;&#41;&#41;;
     *
     * KeyVaultSecret secret = response.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;The response contained the set secret with name '%s' and value '%s'%n&quot;, secret.getName&#40;&#41;,
     *     secret.getValue&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(secret, "'secret' cannot be null.");

        if (isNullOrEmpty(secret.getName())) {
            throw LOGGER.throwableAtError()
                .log("'secret.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(
            clientImpl.setSecretWithResponse(secret.getName(), prepareSecretSetParameters(secret), requestContext),
            SecretsModelsUtils::createKeyVaultSecret);
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
     * <pre>
     * KeyVaultSecret secret = secretClient.getSecret&#40;&quot;secretName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved secret with name '%s' and value '%s'%n&quot;, secret.getName&#40;&#41;, secret.getValue&#40;&#41;&#41;;
     * </pre>
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
     * <pre>
     * String secretVersion = &quot;6A385B124DEF4096AF1361A85B16C204&quot;;
     * KeyVaultSecret keyVaultSecret = secretClient.getSecret&#40;&quot;secretName&quot;, secretVersion&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved secret with name '%s' and value '%s'%n&quot;, keyVaultSecret.getName&#40;&#41;,
     *     keyVaultSecret.getValue&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return createKeyVaultSecret(clientImpl.getSecretWithResponse(name, version, RequestContext.none()).getValue());
    }

    /**
     * Gets the specified secret with specified version from the key vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific version of a secret in the key vault. Prints out details of the response returned by the
     * service and the requested secret.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.getSecretWithResponse#String-String-RequestContext -->
     * <pre>
     * String secretVersion = &quot;6A385B124DEF4096AF1361A85B16C204&quot;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultSecret&gt; response =
     *     secretClient.getSecretWithResponse&#40;&quot;secretName&quot;, secretVersion, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Received response with status code %d and headers: %s%n&quot;, response.getStatusCode&#40;&#41;,
     *     response.getHeaders&#40;&#41;&#41;;
     *
     * KeyVaultSecret keyVaultSecret = response.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;The response contained the secret with name '%s' and value '%s'%n&quot;,
     *     keyVaultSecret.getName&#40;&#41;, keyVaultSecret.getValue&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.getSecretWithResponse(name, version, requestContext),
            SecretsModelsUtils::createKeyVaultSecret);
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
     * <pre>
     * SecretProperties secretProperties = secretClient.getSecret&#40;&quot;secretName&quot;&#41;
     *     .getProperties&#40;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;60&#41;&#41;;
     *
     * &#47;&#47; Update secret with the new properties.
     * SecretProperties updatedSecretProperties = secretClient.updateSecretProperties&#40;secretProperties&#41;;
     *
     * &#47;&#47; Retrieve updated secret.
     * KeyVaultSecret updatedSecret = secretClient.getSecret&#40;updatedSecretProperties.getName&#40;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Updated secret with name '%s' and value '%s' to expire at: %s%n&quot;,
     *     updatedSecret.getName&#40;&#41;, updatedSecret.getValue&#40;&#41;, updatedSecret.getProperties&#40;&#41;.getExpiresOn&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(secretProperties, "'secretProperties' cannot be null.");

        if (isNullOrEmpty(secretProperties.getName())) {
            throw LOGGER.throwableAtError()
                .log("'secretProperties.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        try (Response<SecretBundle> response = clientImpl.updateSecretWithResponse(secretProperties.getName(),
            prepareUpdateSecretParameters(secretProperties), secretProperties.getVersion(), RequestContext.none())) {

            return createSecretProperties(response.getValue());
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
     * <pre>
     * SecretProperties secretProperties = secretClient.getSecret&#40;&quot;secretName&quot;&#41;
     *     .getProperties&#40;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;60&#41;&#41;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * &#47;&#47; Update secret with the new properties.
     * Response&lt;SecretProperties&gt; response =
     *     secretClient.updateSecretPropertiesWithResponse&#40;secretProperties, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Received response with status code %d and headers: %s%n&quot;, response.getStatusCode&#40;&#41;,
     *     response.getHeaders&#40;&#41;&#41;;
     *
     * SecretProperties updatedSecretProperties = response.getValue&#40;&#41;;
     *
     * &#47;&#47; Retrieve updated secret.
     * KeyVaultSecret updatedSecret = secretClient.getSecret&#40;updatedSecretProperties.getName&#40;&#41;&#41;;
     *
     * System.out.printf&#40;
     *     &quot;The response contained the updated secret with name '%s' and value '%s' set to expire at: %s%n&quot;,
     *     updatedSecret.getName&#40;&#41;, updatedSecret.getValue&#40;&#41;, updatedSecret.getProperties&#40;&#41;.getExpiresOn&#40;&#41;&#41;;
     * </pre>
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

        Objects.requireNonNull(secretProperties, "'secretProperties' cannot be null.");

        if (isNullOrEmpty(secretProperties.getName())) {
            throw LOGGER.throwableAtError()
                .log("'secretProperties.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(
            clientImpl.updateSecretWithResponse(secretProperties.getName(),
                prepareUpdateSecretParameters(secretProperties), secretProperties.getVersion(), requestContext),
            SecretsModelsUtils::createSecretProperties);
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
     * <pre>
     * Poller&lt;DeletedSecret, Void&gt; deleteSecretPoller = secretClient.beginDeleteSecret&#40;&quot;secretName&quot;&#41;;
     *
     * &#47;&#47; Deleted Secret is accessible as soon as polling begins.
     * PollResponse&lt;DeletedSecret&gt; deleteSecretPollResponse = deleteSecretPoller.poll&#40;&#41;;
     *
     * &#47;&#47; Deletion date only works for a soft-delete enabled key vault.
     * System.out.printf&#40;&quot;Deleted secret's recovery id: '%s'. Deleted date: '%s'.&quot;,
     *     deleteSecretPollResponse.getValue&#40;&#41;.getRecoveryId&#40;&#41;, deleteSecretPollResponse.getValue&#40;&#41;.getDeletedOn&#40;&#41;&#41;;
     *
     * &#47;&#47; Secret is being deleted on server.
     * deleteSecretPoller.waitForCompletion&#40;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.SecretClient.deleteSecret#String -->
     *
     * @param name The name of the secret to delete.
     * @return A poller object to poll with and retrieve the deleted secret.
     *
     * @throws HttpResponseException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<DeletedSecret, Void> beginDeleteSecret(String name) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return Poller.createPoller(Duration.ofSeconds(1), pollingContext -> deleteSecretActivationOperation(name),
            pollingContext -> deleteSecretPollOperation(name, pollingContext), (pollingContext, response) -> null,
            pollingContext -> null);
    }

    private PollResponse<DeletedSecret> deleteSecretActivationOperation(String name) {
        try (
            Response<DeletedSecretBundle> response = clientImpl.deleteSecretWithResponse(name, RequestContext.none())) {

            return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, createDeletedSecret(response.getValue()));
        }
    }

    private PollResponse<DeletedSecret> deleteSecretPollOperation(String name,
        PollingContext<DeletedSecret> pollingContext) {

        try {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                createDeletedSecret(clientImpl.getDeletedSecretWithResponse(name, RequestContext.none()).getValue()));
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
     * <pre>
     * DeletedSecret deletedSecret = secretClient.getDeletedSecret&#40;&quot;secretName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved deleted secret with recovery id: %s%n&quot;, deletedSecret.getRecoveryId&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return createDeletedSecret(clientImpl.getDeletedSecretWithResponse(name, RequestContext.none()).getValue());
    }

    /**
     * Gets a secret that has been deleted in a key vault <b>enabled for soft-delete</b>. This operation requires the
     * {@code secrets/list} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the deleted secret from a key vault enabled for soft-delete. Prints out details of the response returned
     * by the service and the deleted secret.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.getDeletedSecretWithResponse#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;DeletedSecret&gt; response = secretClient.getDeletedSecretWithResponse&#40;&quot;secretName&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Received response with status code %d and headers: %s%n&quot;, response.getStatusCode&#40;&#41;,
     *     response.getHeaders&#40;&#41;&#41;;
     *
     * DeletedSecret deletedSecret = response.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;The response contained the deleted secret with name '%s' recovery id '%s'%n&quot;,
     *     deletedSecret.getName&#40;&#41;, deletedSecret.getRecoveryId&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.getDeletedSecretWithResponse(name, requestContext),
            SecretsModelsUtils::createDeletedSecret);
    }

    /**
     * Permanently removes a deleted secret without the possibility of recovery. This operation can only be performed
     * on a key vault <b>enabled for soft-delete</b> and requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges a deleted secret from a key vault enabled for soft-delete.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecret#String -->
     * <pre>
     * secretClient.purgeDeletedSecret&#40;&quot;secretName&quot;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecret#String -->
     *
     * @param name The name of the secret to purge.
     *
     * @throws HttpResponseException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void purgeDeletedSecret(String name) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        clientImpl.purgeDeletedSecretWithResponse(name, RequestContext.none());
    }

    /**
     * Permanently removes a deleted secret without the possibility of recovery. This operation can only be performed
     * on a key vault <b>enabled for soft-delete</b> and requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges a deleted secret from a key vault enabled for soft-delete and prints out details of the response
     * returned by the service.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecretWithResponse#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;Void&gt; response = secretClient.purgeDeletedSecretWithResponse&#40;&quot;secretName&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Received response with status code %d and headers: %s%n&quot;, response.getStatusCode&#40;&#41;,
     *     response.getHeaders&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return clientImpl.purgeDeletedSecretWithResponse(name, requestContext);
    }

    /**
     * Recovers a deleted secret in the key vault to its latest version. Can only be performed on a key vault <b>enabled
     * for soft-delete</b>. This operation requires the {@code secrets/recover} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recovers a deleted secret from a key vault enabled for soft-delete and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.beginRecoverDeletedSecret#String -->
     * <pre>
     * Poller&lt;KeyVaultSecret, Void&gt; recoverSecretPoller = secretClient.beginRecoverDeletedSecret&#40;&quot;deletedSecretName&quot;&#41;;
     *
     * &#47;&#47; A secret to be recovered can be accessed as soon as polling is in progress.
     * PollResponse&lt;KeyVaultSecret&gt; recoveredSecretPollResponse = recoverSecretPoller.poll&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Recovered deleted secret with name '%s' and id '%s'%n&quot;,
     *     recoveredSecretPollResponse.getValue&#40;&#41;.getName&#40;&#41;, recoveredSecretPollResponse.getValue&#40;&#41;.getId&#40;&#41;&#41;;
     *
     * &#47;&#47; Wait for the secret to be recovered on the server.
     * recoverSecretPoller.waitForCompletion&#40;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.SecretClient.beginRecoverDeletedSecret#String -->
     *
     * @param name The name of the deleted secret to be recovered.
     * @return A poller object to poll with and retrieve the recovered secret.
     *
     * @throws HttpResponseException If a secret with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<KeyVaultSecret, Void> beginRecoverDeletedSecret(String name) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return Poller.createPoller(Duration.ofSeconds(1),
            pollingContext -> recoverDeletedSecretActivationOperation(name),
            pollingContext -> recoverDeletedSecretPollOperation(name, pollingContext),
            (pollingContext, response) -> null, pollingContext -> null);
    }

    private PollResponse<KeyVaultSecret> recoverDeletedSecretActivationOperation(String name) {
        try (Response<SecretBundle> response
            = clientImpl.recoverDeletedSecretWithResponse(name, RequestContext.none())) {
            return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                createKeyVaultSecret(response.getValue()));
        }
    }

    private PollResponse<KeyVaultSecret> recoverDeletedSecretPollOperation(String name,
        PollingContext<KeyVaultSecret> pollingContext) {

        try {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                createKeyVaultSecret(clientImpl.getSecretWithResponse(name, "", RequestContext.none()).getValue()));
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
     * <pre>
     * byte[] secretBackup = secretClient.backupSecret&#40;&quot;secretName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;The length of the resulting backup byte array is: %s%n&quot;, secretBackup.length&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        try (Response<BackupSecretResult> response = clientImpl.backupSecretWithResponse(name, RequestContext.none())) {
            return response.getValue().getValue();
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
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;byte[]&gt; response = secretClient.backupSecretWithResponse&#40;&quot;secretName&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Received response with status code %d and headers: %s%n&quot;, response.getStatusCode&#40;&#41;,
     *     response.getHeaders&#40;&#41;&#41;;
     *
     * byte[] secretBackup = response.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;The response contained a backup byte array with length: %s%n&quot;, secretBackup.length&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.backupSecretWithResponse(name, requestContext), BackupSecretResult::getValue);
    }

    /**
     * Restores a backed up secret and all its versions to a vault. All versions of the secret are restored to the
     * vault. This operation requires the {@code secrets/restore} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Restores a secret in the key vault from a backup and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.restoreSecret#byte -->
     * <pre>
     * KeyVaultSecret restoredSecret = secretClient.restoreSecretBackup&#40;secretBackupByteArray&#41;;
     *
     * System.out.printf&#40;&quot;Restored secret with name '%s' and value '%s'%n&quot;, restoredSecret.getName&#40;&#41;,
     *     restoredSecret.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.SecretClient.restoreSecret#byte -->
     *
     * @param backup The backup blob associated with the secret.
     * @return The restored secret.
     *
     * @throws HttpResponseException If the {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret restoreSecretBackup(byte[] backup) {
        try (Response<SecretBundle> response
            = clientImpl.restoreSecretWithResponse(new SecretRestoreParameters(backup), RequestContext.none())) {

            return createKeyVaultSecret(response.getValue());
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
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultSecret&gt; response =
     *     secretClient.restoreSecretBackupWithResponse&#40;secretBackupByteArray, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Received response with status code %d and headers: %s%n&quot;, response.getStatusCode&#40;&#41;,
     *     response.getHeaders&#40;&#41;&#41;;
     *
     * KeyVaultSecret restoredSecret = response.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;The response contained the restored secret with name '%s' and value '%s'%n&quot;,
     *     restoredSecret.getName&#40;&#41;, restoredSecret.getValue&#40;&#41;&#41;;
     * </pre>
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
        return mapResponse(clientImpl.restoreSecretWithResponse(new SecretRestoreParameters(backup), requestContext),
            SecretsModelsUtils::createKeyVaultSecret);
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
     * <pre>
     * secretClient.listPropertiesOfSecrets&#40;&#41;.forEach&#40;secretProperties -&gt; &#123;
     *     KeyVaultSecret secret = secretClient.getSecret&#40;secretProperties.getName&#40;&#41;, secretProperties.getVersion&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot;Retrieved secret with name '%s' and value '%s'%n&quot;, secret.getName&#40;&#41;, secret.getValue&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets -->
     *
     * <p><strong>Iterate through secrets by page</strong></p>
     * <p>Iterates through the secrets in the key vault by page and gets the value for each one's latest version by
     * looping though the properties objects and calling {@link SecretClient#getSecret(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets.iterableByPage -->
     * <pre>
     * secretClient.listPropertiesOfSecrets&#40;&#41;
     *     .iterableByPage&#40;&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Received response with status code %d and headers: %s%n&quot;,
     *             pagedResponse.getStatusCode&#40;&#41;, pagedResponse.getHeaders&#40;&#41;&#41;;
     *
     *         pagedResponse.getValue&#40;&#41;.forEach&#40;secretProperties -&gt; &#123;
     *             KeyVaultSecret secret =
     *                 secretClient.getSecret&#40;secretProperties.getName&#40;&#41;, secretProperties.getVersion&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot;Retrieved secret with name '%s' and value '%s'%n&quot;, secret.getName&#40;&#41;, secret.getValue&#40;&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
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
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * secretClient.listPropertiesOfSecrets&#40;requestContext&#41;.forEach&#40;secretProperties -&gt; &#123;
     *     KeyVaultSecret secret = secretClient.getSecret&#40;secretProperties.getName&#40;&#41;, secretProperties.getVersion&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot;Retrieved secret with name '%s' and value '%s'%n&quot;, secret.getName&#40;&#41;, secret.getValue&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets#RequestContext -->
     *
     * <p><strong>Iterate through secrets by page</strong></p>
     * <p>Iterates through the secrets in the key vault by page and gets the value for each one's latest version by
     * looping though the properties objects and calling {@link SecretClient#getSecret(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets.iterableByPage#RequestContext -->
     * <pre>
     * RequestContext reqContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * secretClient.listPropertiesOfSecrets&#40;reqContext&#41;
     *     .iterableByPage&#40;&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Received response with status code %d and headers: %s%n&quot;,
     *             pagedResponse.getStatusCode&#40;&#41;, pagedResponse.getHeaders&#40;&#41;&#41;;
     *
     *         pagedResponse.getValue&#40;&#41;.forEach&#40;secretProperties -&gt; &#123;
     *             KeyVaultSecret secret =
     *                 secretClient.getSecret&#40;secretProperties.getName&#40;&#41;, secretProperties.getVersion&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot;Retrieved secret with name '%s' and value '%s'%n&quot;, secret.getName&#40;&#41;,
     *                 secret.getValue&#40;&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets.iterableByPage#RequestContext -->
     *
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of properties objects of all the secrets in the vault. A properties object
     * contains all the information about the secret, except its value.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SecretProperties> listPropertiesOfSecrets(RequestContext requestContext) {
        return mapPages(pagingOptions -> clientImpl.getSecretsSinglePage(null, requestContext),
            (pagingOptions, nextLink) -> clientImpl.getSecretsNextSinglePage(nextLink, requestContext),
            SecretsModelsUtils::createSecretProperties);
    }

    /**
     * Lists all deleted secrets in the key vault currently available for recovery. This operation is applicable for key
     * vaults <b>enabled for soft-delete</b> and requires the {@code secrets/list} permission.
     *
     * <p><strong>Iterate through deleted secrets</strong></p>
     * <p>Lists the deleted secrets in a key vault enabled for soft-delete and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets -->
     * <pre>
     * secretClient.listDeletedSecrets&#40;&#41;.forEach&#40;deletedSecret -&gt; &#123;
     *     System.out.printf&#40;&quot;Retrieved deleted secret with recovery id: %s&quot;, deletedSecret.getRecoveryId&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets -->
     *
     * <p><strong>Iterate through deleted secrets by page</strong></p>
     * <p>Iterates through the deleted secrets in the key vault by page and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets.iterableByPage -->
     * <pre>
     * secretClient.listDeletedSecrets&#40;&#41;
     *     .iterableByPage&#40;&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Received response with status code %d and headers: %s%n&quot;,
     *             pagedResponse.getStatusCode&#40;&#41;, pagedResponse.getHeaders&#40;&#41;&#41;;
     *
     *         pagedResponse.getValue&#40;&#41;.forEach&#40;deletedSecret -&gt;
     *             System.out.printf&#40;&quot;Retrieved deleted secret with recovery id: %s%n&quot;,
     *                 deletedSecret.getRecoveryId&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
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
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * secretClient.listDeletedSecrets&#40;requestContext&#41;.forEach&#40;deletedSecret -&gt; &#123;
     *     System.out.printf&#40;&quot;Retrieved deleted secret with recovery id: %s%n&quot;, deletedSecret.getRecoveryId&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets#RequestContext -->
     *
     * <p><strong>Iterate through deleted secrets by page</strong></p>
     * <p>Iterates through the deleted secrets in the key vault by page and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets.iterableByPage#RequestContext -->
     * <pre>
     * RequestContext reqContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * secretClient.listDeletedSecrets&#40;reqContext&#41;
     *     .iterableByPage&#40;&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Received response with status code %d and headers: %s%n&quot;,
     *             pagedResponse.getStatusCode&#40;&#41;, pagedResponse.getHeaders&#40;&#41;&#41;;
     *
     *         pagedResponse.getValue&#40;&#41;.forEach&#40;deletedSecret -&gt;
     *             System.out.printf&#40;&quot;Retrieved deleted secret with recovery id: %s%n&quot;,
     *                 deletedSecret.getRecoveryId&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets.iterableByPage#RequestContext -->
     *
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} deleted secrets in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedSecret> listDeletedSecrets(RequestContext requestContext) {
        return mapPages(pagingOptions -> clientImpl.getDeletedSecretsSinglePage(null, requestContext),
            (pagingOptions, nextLink) -> clientImpl.getDeletedSecretsNextSinglePage(nextLink, requestContext),
            SecretsModelsUtils::createDeletedSecret);
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
     * <pre>
     * secretClient.listPropertiesOfSecretVersions&#40;&quot;secretName&quot;&#41;.forEach&#40;secretProperties -&gt; &#123;
     *     KeyVaultSecret secret = secretClient.getSecret&#40;secretProperties.getName&#40;&#41;, secretProperties.getVersion&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot;Retrieved secret version with name '%s' and value '%s'%n&quot;, secret.getName&#40;&#41;,
     *         secret.getValue&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions#String -->
     *
     * <p><strong>Iterate through secret versions by page</strong></p>
     * <p>The sample below iterates through the versions of a secret in the key vault by page and gets each one's value
     * by looping though the properties objects and calling {@link SecretClient#getSecret(String, String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions.iterableByPage#String -->
     * <pre>
     * secretClient.listPropertiesOfSecretVersions&#40;&quot;secretName&quot;&#41;
     *     .iterableByPage&#40;&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Received response with status code %d and headers: %s%n&quot;,
     *             pagedResponse.getStatusCode&#40;&#41;, pagedResponse.getHeaders&#40;&#41;&#41;;
     *
     *         pagedResponse.getValue&#40;&#41;.forEach&#40;secretProperties -&gt; &#123;
     *             KeyVaultSecret secret = secretClient.getSecret&#40;secretProperties.getName&#40;&#41;,
     *                 secretProperties.getVersion&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot;Retrieved secret version with name '%s' and value '%s'%n&quot;, secret.getName&#40;&#41;,
     *                 secret.getValue&#40;&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
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
     * <pre>
     * RequestContext reqContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * secretClient.listPropertiesOfSecretVersions&#40;&quot;secretName&quot;, reqContext&#41;
     *     .iterableByPage&#40;&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Received response with status code %d and headers: %s%n&quot;,
     *             pagedResponse.getStatusCode&#40;&#41;, pagedResponse.getHeaders&#40;&#41;&#41;;
     *
     *         pagedResponse.getValue&#40;&#41;.forEach&#40;secretProperties -&gt; &#123;
     *             KeyVaultSecret secret = secretClient.getSecret&#40;secretProperties.getName&#40;&#41;,
     *                 secretProperties.getVersion&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot;Retrieved secret version with name '%s' and value '%s'%n&quot;, secret.getName&#40;&#41;,
     *                 secret.getValue&#40;&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapPages(pagingOptions -> clientImpl.getSecretVersionsSinglePage(name, null, requestContext),
            (pagingOptions, nextLink) -> clientImpl.getSecretVersionsNextSinglePage(nextLink, requestContext),
            SecretsModelsUtils::createSecretProperties);
    }

    private static <T, S> Response<S> mapResponse(Response<T> response, Function<T, S> mapper) {
        if (response == null) {
            return null;
        }

        return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            mapper.apply(response.getValue()));
    }

    private static <T, S> PagedIterable<S> mapPages(Function<PagingOptions, PagedResponse<T>> firstPageRetriever,
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
