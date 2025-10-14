// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.paging.PageRetriever;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.security.keyvault.secrets.implementation.SecretClientImpl;
import com.azure.security.keyvault.secrets.implementation.models.BackupSecretResult;
import com.azure.security.keyvault.secrets.implementation.models.DeletedSecretBundle;
import com.azure.security.keyvault.secrets.implementation.models.DeletedSecretItem;
import com.azure.security.keyvault.secrets.implementation.models.SecretBundle;
import com.azure.security.keyvault.secrets.implementation.models.SecretItem;
import com.azure.security.keyvault.secrets.implementation.models.SecretRestoreParameters;
import com.azure.security.keyvault.secrets.implementation.models.SecretSetParameters;
import com.azure.security.keyvault.secrets.implementation.models.SecretsModelsUtils;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.security.keyvault.secrets.SecretClient.prepareSecretSetParameters;
import static com.azure.security.keyvault.secrets.SecretClient.prepareUpdateSecretParameters;
import static com.azure.security.keyvault.secrets.implementation.models.SecretsModelsUtils.createDeletedSecret;
import static com.azure.security.keyvault.secrets.implementation.models.SecretsModelsUtils.createKeyVaultSecret;
import static com.azure.security.keyvault.secrets.implementation.models.SecretsModelsUtils.createSecretProperties;

/**
 * The SecretAsyncClient provides asynchronous methods to manage {@link KeyVaultSecret secrets} in the Azure Key Vault.
 * The client supports creating, retrieving, updating, deleting, purging, backing up, restoring, and listing the
 * {@link KeyVaultSecret secrets}. The client also supports listing {@link DeletedSecret deleted secrets} for a
 * soft-delete enabled key vault.
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link com.azure.security.keyvault.secrets.SecretAsyncClient} class, a vault url and a credential object.</p>
 *
 * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication,
 * which is appropriate for most scenarios, including local development and production environments. Additionally,
 * we recommend using a
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">
 * managed identity</a> for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">
 * Azure Identity documentation"</a>.</p>
 *
 * <p><strong>Sample: Construct Asynchronous Secret Client</strong></p>
 *
 * <!-- src_embed com.azure.security.keyvault.secrets.SecretAsyncClient.instantiation -->
 * <pre>
 * SecretAsyncClient secretAsyncClient = new SecretClientBuilder&#40;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.secrets.SecretAsyncClient.instantiation -->
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Create a Secret</h2>
 * The {@link SecretAsyncClient} can be used to create a secret in the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to create and store a secret in the key vault, using the
 * {@link SecretAsyncClient#setSecret(String, String)} API.</p>
 *
 * <!-- src_embed com.azure.keyvault.secrets.SecretClient.setSecret#string-string -->
 * <pre>
 * secretAsyncClient.setSecret&#40;&quot;secretName&quot;, &quot;secretValue&quot;&#41;
 *     .subscribe&#40;secretResponse -&gt;
 *         System.out.printf&#40;&quot;Secret is created with name %s and value %s%n&quot;,
 *             secretResponse.getName&#40;&#41;, secretResponse.getValue&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.keyvault.secrets.SecretClient.setSecret#string-string -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link SecretClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get a Secret</h2>
 * The {@link SecretAsyncClient} can be used to retrieve a secret from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously retrieve a previously stored secret from the
 * key vault, using the {@link SecretAsyncClient#getSecret(String)} API.</p>
 *
 * <!-- src_embed com.azure.keyvault.secrets.SecretClient.getSecret#string -->
 * <pre>
 * secretAsyncClient.getSecret&#40;&quot;secretName&quot;&#41;
 *     .subscribe&#40;secretWithVersion -&gt;
 *         System.out.printf&#40;&quot;Secret is returned with name %s and value %s %n&quot;,
 *             secretWithVersion.getName&#40;&#41;, secretWithVersion.getValue&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.keyvault.secrets.SecretClient.getSecret#string -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link SecretClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Delete a Secret</h2>
 * The {@link SecretAsyncClient} can be used to delete a secret from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to delete a secret from the key vault, using the
 * {@link SecretAsyncClient#beginDeleteSecret(String)} API.</p>
 *
 * <!-- src_embed com.azure.keyvault.secrets.SecretClient.deleteSecret#String -->
 * <pre>
 * secretAsyncClient.beginDeleteSecret&#40;&quot;secretName&quot;&#41;
 *     .subscribe&#40;pollResponse -&gt; &#123;
 *         System.out.println&#40;&quot;Delete Status: &quot; + pollResponse.getStatus&#40;&#41;.toString&#40;&#41;&#41;;
 *         System.out.println&#40;&quot;Deleted Secret Name: &quot; + pollResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;;
 *         System.out.println&#40;&quot;Deleted Secret Value: &quot; + pollResponse.getValue&#40;&#41;.getValue&#40;&#41;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.keyvault.secrets.SecretClient.deleteSecret#String -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link SecretClient}.</p>
 *
 * @see SecretClientBuilder
 * @see PollerFlux
 * @see PagedFlux
 */
@ServiceClient(
    builder = SecretClientBuilder.class,
    isAsync = true,
    serviceInterfaces = SecretClientImpl.SecretClientService.class)
public final class SecretAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(SecretAsyncClient.class);

    static final RequestOptions EMPTY_OPTIONS = new RequestOptions();

    private final SecretClientImpl implClient;
    private final String vaultUrl;

    /**
     * Creates a SecretAsyncClient to service requests
     *
     * @param implClient The implementation client.
     * @param vaultUrl The vault url.
     */
    SecretAsyncClient(SecretClientImpl implClient, String vaultUrl) {
        this.implClient = implClient;
        this.vaultUrl = vaultUrl;
    }

    /**
     * Gets the vault endpoint url to which service requests are sent to.
     * @return the vault endpoint url.
     */
    public String getVaultUrl() {
        return vaultUrl;
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
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.setSecret#secret -->
     * <pre>
     * SecretProperties properties = new SecretProperties&#40;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;60&#41;&#41;;
     * KeyVaultSecret newSecret = new KeyVaultSecret&#40;&quot;secretName&quot;, &quot;secretValue&quot;&#41;
     *     .setProperties&#40;properties&#41;;
     *
     * secretAsyncClient.setSecret&#40;newSecret&#41;
     *     .subscribe&#40;secretResponse -&gt;
     *         System.out.printf&#40;&quot;Secret is created with name %s and value %s %n&quot;,
     *             secretResponse.getName&#40;&#41;, secretResponse.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.setSecret#secret -->
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties
     * {@link KeyVaultSecret#getName() secret.name} and {@link KeyVaultSecret#getValue() secret.value} cannot be null.
     * @return A {@link Mono} containing the {@link KeyVaultSecret created secret}.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceModifiedException if {@code secret} is malformed.
     * @throws HttpResponseException if {@link KeyVaultSecret#getName() name} or {@link KeyVaultSecret#getValue() value}
     * is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSecret> setSecret(KeyVaultSecret secret) {
        try {
            return implClient
                .setSecretWithResponseAsync(secret.getName(), BinaryData.fromObject(prepareSecretSetParameters(secret)),
                    EMPTY_OPTIONS)
                .onErrorMap(HttpResponseException.class, SecretAsyncClient::mapSetSecretException)
                .map(response -> createKeyVaultSecret(response.getValue().toObject(SecretBundle.class)));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Adds a secret to the key vault if it does not exist. If the named secret exists, a new version of the secret is
     * created. This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Creates a new secret in the key vault. Subscribes to the call asynchronously and prints out
     * the newly created secret details when a response is received.</p>
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.setSecret#string-string -->
     * <pre>
     * secretAsyncClient.setSecret&#40;&quot;secretName&quot;, &quot;secretValue&quot;&#41;
     *     .subscribe&#40;secretResponse -&gt;
     *         System.out.printf&#40;&quot;Secret is created with name %s and value %s%n&quot;,
     *             secretResponse.getName&#40;&#41;, secretResponse.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.setSecret#string-string -->
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
            return implClient
                .setSecretWithResponseAsync(name, BinaryData.fromObject(new SecretSetParameters(value)), EMPTY_OPTIONS)
                .onErrorMap(HttpResponseException.class, SecretAsyncClient::mapSetSecretException)
                .map(response -> createKeyVaultSecret(response.getValue().toObject(SecretBundle.class)));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.setSecretWithResponse#secret -->
     * <pre>
     * KeyVaultSecret newSecret = new KeyVaultSecret&#40;&quot;secretName&quot;, &quot;secretValue&quot;&#41;.
     *     setProperties&#40;new SecretProperties&#40;&#41;.setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;60&#41;&#41;&#41;;
     * secretAsyncClient.setSecretWithResponse&#40;newSecret&#41;
     *     .subscribe&#40;secretResponse -&gt;
     *         System.out.printf&#40;&quot;Secret is created with name %s and value %s %n&quot;,
     *             secretResponse.getValue&#40;&#41;.getName&#40;&#41;, secretResponse.getValue&#40;&#41;.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.setSecretWithResponse#secret -->
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties
     * {@link KeyVaultSecret#getName() secret.name} and {@link KeyVaultSecret#getValue() secret.value} cannot be null.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultSecret created secret}.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceModifiedException if {@code secret} is malformed.
     * @throws HttpResponseException if {@link KeyVaultSecret#getName() name} or {@link KeyVaultSecret#getValue() value}
     * is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultSecret>> setSecretWithResponse(KeyVaultSecret secret) {
        try {
            return implClient
                .setSecretWithResponseAsync(secret.getName(), BinaryData.fromObject(prepareSecretSetParameters(secret)),
                    EMPTY_OPTIONS)
                .onErrorMap(HttpResponseException.class, SecretAsyncClient::mapSetSecretException)
                .map(response -> new SimpleResponse<>(response,
                    createKeyVaultSecret(response.getValue().toObject(SecretBundle.class))));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    // For some reason, the service does not return a 409 when a secret with the same name exists. Instead, it returns
    // a 400.
    static HttpResponseException mapSetSecretException(HttpResponseException e) {
        return (e.getResponse().getStatusCode() == 400)
            ? new ResourceModifiedException(e.getMessage(), e.getResponse(), e.getValue())
            : e;
    }

    /**
     * Gets the latest version of the specified secret from the key vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets latest version of the secret in the key vault. Subscribes to the call asynchronously and prints out the
     * returned secret details when a response is received.</p>
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.getSecret#string -->
     * <pre>
     * secretAsyncClient.getSecret&#40;&quot;secretName&quot;&#41;
     *     .subscribe&#40;secretWithVersion -&gt;
     *         System.out.printf&#40;&quot;Secret is returned with name %s and value %s %n&quot;,
     *             secretWithVersion.getName&#40;&#41;, secretWithVersion.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.getSecret#string -->
     *
     * @param name The name of the secret.
     * @return A {@link Mono} containing the requested {@link KeyVaultSecret secret}.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or empty.
     * @throws ResourceNotFoundException When a secret with the given {@code name} doesn't exist in the vault.
     * @throws HttpResponseException If the server reports an error when executing the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSecret> getSecret(String name) {
        return getSecret(name, "");
    }

    /**
     * Gets the specified secret with specified version from the key vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets a specific version of the secret in the key vault. Subscribes to the call
     * asynchronously and prints out the returned secret details when a response is received.</p>
     *
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.getSecret#string-string -->
     * <pre>
     * String secretVersion = &quot;6A385B124DEF4096AF1361A85B16C204&quot;;
     * secretAsyncClient.getSecret&#40;&quot;secretName&quot;, secretVersion&#41;
     *     &#47;&#47; Passing a Context is optional and useful if you want a set of data to flow through the request.
     *     &#47;&#47; Otherwise, the line below can be removed.
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;secretWithVersion -&gt;
     *         System.out.printf&#40;&quot;Secret is returned with name %s and value %s %n&quot;,
     *             secretWithVersion.getName&#40;&#41;, secretWithVersion.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.getSecret#string-string -->
     *
     * @param name The name of the secret, cannot be null.
     * @param version The version of the secret to retrieve. If this is an empty string or null, this
     * call is equivalent to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * requested {@link KeyVaultSecret secret}.
     * @throws ResourceNotFoundException When a secret with the given {@code name} and {@code version} doesn't exist in
     * the vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or empty.
     * @throws HttpResponseException If the server reports an error when executing the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSecret> getSecret(String name, String version) {
        if (CoreUtils.isNullOrEmpty(name)) {
            return monoError(LOGGER, new IllegalArgumentException("'name' cannot be null or empty."));
        }

        try {
            return implClient.getSecretWithResponseAsync(name, version, EMPTY_OPTIONS)
                .onErrorMap(HttpResponseException.class, SecretAsyncClient::mapGetSecretException)
                .map(response -> createKeyVaultSecret(response.getValue().toObject(SecretBundle.class)));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Gets the specified secret with specified version from the key vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets a specific version of the secret in the key vault. Subscribes to the call asynchronously and prints out
     * the returned secret details when a response is received.</p>
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.getSecretWithResponse#string-string -->
     * <pre>
     * String secretVersion = &quot;6A385B124DEF4096AF1361A85B16C204&quot;;
     * secretAsyncClient.getSecretWithResponse&#40;&quot;secretName&quot;, secretVersion&#41;
     *     &#47;&#47; Passing a Context is optional and useful if you want a set of data to flow through the request.
     *     &#47;&#47; Otherwise, the line below can be removed.
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;secretWithVersion -&gt;
     *         System.out.printf&#40;&quot;Secret is returned with name %s and value %s %n&quot;,
     *             secretWithVersion.getValue&#40;&#41;.getName&#40;&#41;, secretWithVersion.getValue&#40;&#41;.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.getSecretWithResponse#string-string -->
     *
     * @param name The name of the secret, cannot be null.
     * @param version The version of the secret to retrieve. If this is an empty string or null, this call is equivalent
     * to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * requested {@link KeyVaultSecret secret}.
     * @throws ResourceNotFoundException When a secret with the given {@code name} and {@code version} doesn't exist in
     * the vault.
     * @throws IllegalArgumentException If {@code name} is either {@code null} or empty.
     * @throws HttpResponseException If the server reports an error when executing the request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultSecret>> getSecretWithResponse(String name, String version) {
        if (CoreUtils.isNullOrEmpty(name)) {
            return monoError(LOGGER, new IllegalArgumentException("'name' cannot be null or empty."));
        }

        try {
            return implClient.getSecretWithResponseAsync(name, version, EMPTY_OPTIONS)
                .onErrorMap(HttpResponseException.class, SecretAsyncClient::mapGetSecretException)
                .map(response -> new SimpleResponse<>(response,
                    createKeyVaultSecret(response.getValue().toObject(SecretBundle.class))));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    // For some reason, the service does not return a 409 when a secret with the same name exists. Instead, it returns
    // a 403.
    static HttpResponseException mapGetSecretException(HttpResponseException e) {
        if (e.getResponse().getStatusCode() == 403) {
            return new ResourceModifiedException(e.getMessage(), e.getResponse(), e.getValue());
        } else {
            return e;
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
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.updateSecretProperties#secretProperties -->
     * <pre>
     * secretAsyncClient.getSecret&#40;&quot;secretName&quot;&#41;
     *     .subscribe&#40;secretResponseValue -&gt; &#123;
     *         SecretProperties secretProperties = secretResponseValue.getProperties&#40;&#41;;
     *         &#47;&#47;Update the not before time of the secret.
     *         secretProperties.setNotBefore&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;50&#41;&#41;;
     *         secretAsyncClient.updateSecretProperties&#40;secretProperties&#41;
     *             .subscribe&#40;secretResponse -&gt;
     *                 System.out.printf&#40;&quot;Secret's updated not before time %s %n&quot;,
     *                     secretResponse.getNotBefore&#40;&#41;.toString&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.updateSecretProperties#secretProperties -->
     *
     * @param secretProperties The {@link SecretProperties secret properties} object with updated properties.
     * @return A {@link Mono} containing the {@link SecretProperties updated secret}.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceNotFoundException when a secret with {@link SecretProperties#getName() name} and
     * {@link SecretProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpResponseException if {@link SecretProperties#getName() name} or
     * {@link SecretProperties#getVersion() version} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SecretProperties> updateSecretProperties(SecretProperties secretProperties) {
        try {
            return implClient
                .updateSecretWithResponseAsync(secretProperties.getName(), secretProperties.getVersion(),
                    BinaryData.fromObject(prepareUpdateSecretParameters(secretProperties)), EMPTY_OPTIONS)
                .map(response -> createSecretProperties(response.getValue().toObject(SecretBundle.class)));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.updateSecretPropertiesWithResponse#secretProperties -->
     * <pre>
     * secretAsyncClient.getSecret&#40;&quot;secretName&quot;&#41;
     *     .subscribe&#40;secretResponseValue -&gt; &#123;
     *         SecretProperties secretProperties = secretResponseValue.getProperties&#40;&#41;;
     *         &#47;&#47;Update the not before time of the secret.
     *         secretProperties.setNotBefore&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;50&#41;&#41;;
     *         secretAsyncClient.updateSecretPropertiesWithResponse&#40;secretProperties&#41;
     *             .subscribe&#40;secretResponse -&gt;
     *                 System.out.printf&#40;&quot;Secret's updated not before time %s %n&quot;,
     *                     secretResponse.getValue&#40;&#41;.getNotBefore&#40;&#41;.toString&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.updateSecretPropertiesWithResponse#secretProperties -->
     *
     * <p>The {@code secret} is required and its fields {@link SecretProperties#getName() name} and
     * {@link SecretProperties#getVersion() version} cannot be null.</p>
     *
     * @param secretProperties The {@link SecretProperties secret properties} object with updated properties.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link SecretProperties updated secret}.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceNotFoundException when a secret with {@link SecretProperties#getName() name} and
     * {@link SecretProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpResponseException if {@link SecretProperties#getName() name} or
     * {@link SecretProperties#getVersion() version} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SecretProperties>> updateSecretPropertiesWithResponse(SecretProperties secretProperties) {
        try {
            return implClient
                .updateSecretWithResponseAsync(secretProperties.getName(), secretProperties.getVersion(),
                    BinaryData.fromObject(prepareUpdateSecretParameters(secretProperties)), EMPTY_OPTIONS)
                .map(response -> new SimpleResponse<>(response,
                    createSecretProperties(response.getValue().toObject(SecretBundle.class))));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
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
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.deleteSecret#String -->
     * <pre>
     * secretAsyncClient.beginDeleteSecret&#40;&quot;secretName&quot;&#41;
     *     .subscribe&#40;pollResponse -&gt; &#123;
     *         System.out.println&#40;&quot;Delete Status: &quot; + pollResponse.getStatus&#40;&#41;.toString&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;Deleted Secret Name: &quot; + pollResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;Deleted Secret Value: &quot; + pollResponse.getValue&#40;&#41;.getValue&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.deleteSecret#String -->
     *
     * @param name The name of the secret to be deleted.
     * @return A {@link PollerFlux} to poll on and retrieve {@link DeletedSecret deleted secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DeletedSecret, Void> beginDeleteSecret(String name) {
        return new PollerFlux<>(Duration.ofSeconds(1), deleteActivationOperation(name), deletePollOperation(name),
            (context, firstResponse) -> Mono.empty(), context -> Mono.empty());
    }

    private Function<PollingContext<DeletedSecret>, Mono<DeletedSecret>> deleteActivationOperation(String name) {
        return pollingContext -> {
            try {
                return implClient.deleteSecretWithResponseAsync(name, EMPTY_OPTIONS)
                    .map(response -> createDeletedSecret(response.getValue().toObject(DeletedSecretBundle.class)));
            } catch (RuntimeException e) {
                return monoError(LOGGER, e);
            }
        };
    }

    private Function<PollingContext<DeletedSecret>, Mono<PollResponse<DeletedSecret>>>
        deletePollOperation(String name) {

        return pollingContext -> implClient.getDeletedSecretWithResponseAsync(name, EMPTY_OPTIONS)
            .map(response -> new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                createDeletedSecret(response.getValue().toObject(DeletedSecretBundle.class))))
            .onErrorResume(HttpResponseException.class, exception -> {
                if (exception.getResponse().getStatusCode() == 404) {
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                        pollingContext.getLatestResponse().getValue()));
                } else {
                    // This means either vault has soft-delete disabled or permission is not granted for the get deleted
                    // key operation. In both cases deletion operation was successful when activation operation
                    // succeeded before reaching here.
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                        pollingContext.getLatestResponse().getValue()));
                }
            })
            // This means either vault has soft-delete disabled or permission is not granted for the get deleted key
            // operation. In both cases deletion operation was successful when activation operation succeeded before
            // reaching here.
            .onErrorReturn(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                pollingContext.getLatestResponse().getValue()));
    }

    /**
     * Gets a secret that has been deleted for a soft-delete enabled key vault. This operation requires the
     * {@code secrets/list} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets the deleted secret from the key vault <b>enabled for soft-delete</b>. Subscribes to the call
     * asynchronously and prints out the deleted secret details when a response is received.</p>
     *
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.getDeletedSecret#string -->
     * <pre>
     * secretAsyncClient.getDeletedSecret&#40;&quot;secretName&quot;&#41;
     *     .subscribe&#40;deletedSecretResponse -&gt;
     *         System.out.printf&#40;&quot;Deleted Secret's Recovery Id %s %n&quot;, deletedSecretResponse.getRecoveryId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.getDeletedSecret#string -->
     *
     * @param name The name of the deleted secret.
     * @return A {@link Mono} containing the {@link DeletedSecret deleted secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeletedSecret> getDeletedSecret(String name) {
        try {
            return implClient.getDeletedSecretWithResponseAsync(name, EMPTY_OPTIONS)
                .map(response -> createDeletedSecret(response.getValue().toObject(DeletedSecretBundle.class)));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.getDeletedSecretWithResponse#string -->
     * <pre>
     * secretAsyncClient.getDeletedSecretWithResponse&#40;&quot;secretName&quot;&#41;
     *     .subscribe&#40;deletedSecretResponse -&gt;
     *         System.out.printf&#40;&quot;Deleted Secret's Recovery Id %s %n&quot;,
     *             deletedSecretResponse.getValue&#40;&#41;.getRecoveryId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.getDeletedSecretWithResponse#string -->
     *
     * @param name The name of the deleted secret.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DeletedSecret deleted secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeletedSecret>> getDeletedSecretWithResponse(String name) {
        try {
            return implClient.getDeletedSecretWithResponseAsync(name, EMPTY_OPTIONS)
                .map(response -> new SimpleResponse<>(response,
                    createDeletedSecret(response.getValue().toObject(DeletedSecretBundle.class))));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Permanently removes a deleted secret, without the possibility of recovery. This operation can only be performed
     * on a <b>soft-delete enabled</b>. This operation requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Purges the deleted secret from the key vault enabled for <b>soft-delete</b>. Subscribes to the call
     * asynchronously and prints out the status code from the server response when a response is received.</p>
     *
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.purgeDeletedSecret#string -->
     * <pre>
     * secretAsyncClient.purgeDeletedSecret&#40;&quot;deletedSecretName&quot;&#41;
     *     .doOnSuccess&#40;purgeResponse -&gt;
     *         System.out.println&#40;&quot;Successfully Purged deleted Secret&quot;&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.purgeDeletedSecret#string -->
     *
     * @param name The name of the secret.
     * @return An empty {@link Mono}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> purgeDeletedSecret(String name) {
        try {
            return implClient.purgeDeletedSecretWithResponseAsync(name, EMPTY_OPTIONS)
                .flatMap(response -> Mono.empty());
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.purgeDeletedSecretWithResponse#string -->
     * <pre>
     * secretAsyncClient.purgeDeletedSecretWithResponse&#40;&quot;deletedSecretName&quot;&#41;
     *     .subscribe&#40;purgeResponse -&gt;
     *         System.out.printf&#40;&quot;Purge Status response %d %n&quot;, purgeResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.purgeDeletedSecretWithResponse#string -->
     *
     * @param name The name of the secret.
     * @return A {@link Mono} containing a Response containing status code and HTTP headers.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> purgeDeletedSecretWithResponse(String name) {
        try {
            return implClient.purgeDeletedSecretWithResponseAsync(name, EMPTY_OPTIONS);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Recovers the deleted secret in the key vault to its latest version. Can only be performed on a <b>soft-delete
     * enabled</b> vault. This operation requires the {@code secrets/recover} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recovers the deleted secret from the key vault enabled for <b>soft-delete</b>. Subscribes to the call
     * asynchronously and prints out the recovered secret details when a response is received.</p>
     *
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.recoverDeletedSecret#String -->
     * <pre>
     * secretAsyncClient.beginRecoverDeletedSecret&#40;&quot;deletedSecretName&quot;&#41;
     *     .subscribe&#40;pollResponse -&gt; &#123;
     *         System.out.println&#40;&quot;Recovery Status: &quot; + pollResponse.getStatus&#40;&#41;.toString&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;Recovered Secret Name: &quot; + pollResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;Recovered Secret Value: &quot; + pollResponse.getValue&#40;&#41;.getValue&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.recoverDeletedSecret#String -->
     *
     * @param name The name of the deleted secret to be recovered.
     * @return A {@link PollerFlux} to poll on and retrieve the {@link KeyVaultSecret recovered secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultSecret, Void> beginRecoverDeletedSecret(String name) {
        return new PollerFlux<>(Duration.ofSeconds(1), recoverActivationOperation(name), recoverPollOperation(name),
            (context, firstResponse) -> Mono.empty(), context -> Mono.empty());
    }

    private Function<PollingContext<KeyVaultSecret>, Mono<KeyVaultSecret>> recoverActivationOperation(String name) {
        return pollingContext -> {
            try {
                return implClient.recoverDeletedSecretWithResponseAsync(name, EMPTY_OPTIONS)
                    .map(response -> createKeyVaultSecret(response.getValue().toObject(SecretBundle.class)));
            } catch (RuntimeException e) {
                return monoError(LOGGER, e);
            }
        };
    }

    private Function<PollingContext<KeyVaultSecret>, Mono<PollResponse<KeyVaultSecret>>>
        recoverPollOperation(String name) {

        return pollingContext -> implClient.getSecretWithResponseAsync(name, "", EMPTY_OPTIONS)
            .map(response -> new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                createKeyVaultSecret(response.getValue().toObject(SecretBundle.class))))
            .onErrorResume(HttpResponseException.class, exception -> {
                if (exception.getResponse().getStatusCode() == 404) {
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                        pollingContext.getLatestResponse().getValue()));
                } else {
                    // This means permission is not granted for the get deleted key operation. In both cases the
                    // deletion operation was successful when activation operation succeeded before reaching here.
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                        pollingContext.getLatestResponse().getValue()));
                }
            })
            // This means permission is not granted for the get deleted key operation. In both cases the deletion
            // operation was successful when activation operation succeeded before reaching here.
            .onErrorReturn(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                pollingContext.getLatestResponse().getValue()));
    }

    /**
     * Requests a backup of the secret be downloaded to the client. All versions of the secret will be downloaded. This
     * operation requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Backs up the secret from the key vault. Subscribes to the call asynchronously and prints out
     * the length of the secret's backup byte array returned in the response.</p>
     *
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.backupSecret#string -->
     * <pre>
     * secretAsyncClient.backupSecret&#40;&quot;secretName&quot;&#41;
     *     .subscribe&#40;secretBackupResponse -&gt;
     *         System.out.printf&#40;&quot;Secret's Backup Byte array's length %s%n&quot;, secretBackupResponse.length&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.backupSecret#string -->
     *
     * @param name The name of the secret.
     * @return A {@link Mono} containing the backed up secret blob.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<byte[]> backupSecret(String name) {
        try {
            return implClient.backupSecretWithResponseAsync(name, EMPTY_OPTIONS)
                .map(response -> response.getValue().toObject(BackupSecretResult.class))
                .mapNotNull(BackupSecretResult::getValue);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.backupSecretWithResponse#string -->
     * <pre>
     * secretAsyncClient.backupSecretWithResponse&#40;&quot;secretName&quot;&#41;
     *     .subscribe&#40;secretBackupResponse -&gt;
     *         System.out.printf&#40;&quot;Secret's Backup Byte array's length %s%n&quot;, secretBackupResponse.getValue&#40;&#41;.length&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.backupSecretWithResponse#string -->
     *
     * @param name The name of the secret.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * backed up secret blob.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<byte[]>> backupSecretWithResponse(String name) {
        try {
            return implClient.backupSecretWithResponseAsync(name, EMPTY_OPTIONS)
                .map(response -> new SimpleResponse<>(response, response.getValue().toBytes()));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.restoreSecret#byte -->
     * <pre>
     * &#47;&#47; Pass the secret backup byte array to the restore operation.
     * byte[] secretBackupByteArray = &#123;&#125;;
     * secretAsyncClient.restoreSecretBackup&#40;secretBackupByteArray&#41;
     *     .subscribe&#40;secretResponse -&gt; System.out.printf&#40;&quot;Restored Secret with name %s and value %s %n&quot;,
     *         secretResponse.getName&#40;&#41;, secretResponse.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.restoreSecret#byte -->
     *
     * @param backup The backup blob associated with the secret.
     * @return A {@link Mono} containing the {@link KeyVaultSecret restored secret}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSecret> restoreSecretBackup(byte[] backup) {
        try {
            return implClient
                .restoreSecretWithResponseAsync(BinaryData.fromObject(new SecretRestoreParameters(backup)),
                    EMPTY_OPTIONS)
                .onErrorMap(HttpResponseException.class, SecretAsyncClient::mapRestoreSecretException)
                .map(response -> createKeyVaultSecret(response.getValue().toObject(SecretBundle.class)));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.restoreSecretWithResponse#byte -->
     * <pre>
     * &#47;&#47; Pass the secret backup byte array to the restore operation.
     * byte[] secretBackupByteArray = &#123;&#125;;
     * secretAsyncClient.restoreSecretBackupWithResponse&#40;secretBackupByteArray&#41;
     *     .subscribe&#40;secretResponse -&gt; System.out.printf&#40;&quot;Restored Secret with name %s and value %s %n&quot;,
     *         secretResponse.getValue&#40;&#41;.getName&#40;&#41;, secretResponse.getValue&#40;&#41;.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.restoreSecretWithResponse#byte -->
     *
     * @param backup The backup blob associated with the secret.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultSecret restored secret}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultSecret>> restoreSecretBackupWithResponse(byte[] backup) {
        try {
            return implClient
                .restoreSecretWithResponseAsync(BinaryData.fromObject(new SecretRestoreParameters(backup)),
                    EMPTY_OPTIONS)
                .onErrorMap(HttpResponseException.class, SecretAsyncClient::mapRestoreSecretException)
                .map(response -> new SimpleResponse<>(response,
                    createKeyVaultSecret(response.getValue().toObject(SecretBundle.class))));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    // For some reason, the service does not return a 409 but a 400 in this case.
    static HttpResponseException mapRestoreSecretException(HttpResponseException e) {
        return (e.getResponse().getStatusCode() == 400)
            ? new ResourceModifiedException(e.getMessage(), e.getResponse(), e.getValue())
            : e;
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
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.listSecrets -->
     * <pre>
     * secretAsyncClient.listPropertiesOfSecrets&#40;&#41;
     *     .flatMap&#40;secretProperties -&gt; &#123;
     *         String name = secretProperties.getName&#40;&#41;;
     *         String version = secretProperties.getVersion&#40;&#41;;
     *
     *         System.out.printf&#40;&quot;Getting secret name: '%s', version: %s%n&quot;, name, version&#41;;
     *         return secretAsyncClient.getSecret&#40;name, version&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;secretResponse -&gt; System.out.printf&#40;&quot;Received secret with name %s and type %s&quot;,
     *         secretResponse.getName&#40;&#41;, secretResponse.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.listSecrets -->
     *
     * @return A {@link PagedFlux} containing {@link SecretProperties properties} of all the secrets in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SecretProperties> listPropertiesOfSecrets() {
        try {
            return mapPages(implClient.getSecretsAsync(EMPTY_OPTIONS),
                binaryData -> SecretsModelsUtils.createSecretProperties(binaryData.toObject(SecretItem.class)));
        } catch (RuntimeException e) {
            return pagedFluxError(LOGGER, e);
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
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.listDeletedSecrets -->
     * <pre>
     * secretAsyncClient.listDeletedSecrets&#40;&#41;
     *     .subscribe&#40;deletedSecretResponse -&gt; System.out.printf&#40;&quot;Deleted Secret's Recovery Id %s %n&quot;,
     *         deletedSecretResponse.getRecoveryId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.listDeletedSecrets -->
     *
     * @return A {@link Flux} containing all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DeletedSecret> listDeletedSecrets() {
        try {
            return mapPages(implClient.getDeletedSecretsAsync(EMPTY_OPTIONS),
                binaryData -> SecretsModelsUtils.createDeletedSecret(binaryData.toObject(DeletedSecretItem.class)));
        } catch (RuntimeException e) {
            return pagedFluxError(LOGGER, e);
        }
    }

    /**
     * Lists all versions of the specified secret. Each {@link SecretProperties secret} returned only has its identifier
     * and attributes populated. The secret values and secret versions are not listed in the response.
     * This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>The sample below fetches the all the versions of the given secret. For each version retrieved, makes a call
     * to {@link #getSecret(String, String) getSecret(String, String)} to get the version's value, and then prints it
     * out.</p>
     *
     * <!-- src_embed com.azure.keyvault.secrets.SecretClient.listSecretVersions#string -->
     * <pre>
     * secretAsyncClient.listPropertiesOfSecretVersions&#40;&quot;secretName&quot;&#41;
     *     .flatMap&#40;secretProperties -&gt; &#123;
     *         System.out.println&#40;&quot;Get secret value for version: &quot; + secretProperties.getVersion&#40;&#41;&#41;;
     *         return secretAsyncClient.getSecret&#40;secretProperties.getName&#40;&#41;, secretProperties.getVersion&#40;&#41;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;secret -&gt; System.out.printf&#40;&quot;Received secret with name %s and type %s%n&quot;,
     *         secret.getName&#40;&#41;, secret.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.keyvault.secrets.SecretClient.listSecretVersions#string -->
     *
     * @param name The name of the secret.
     * @return A {@link PagedFlux} containing {@link SecretProperties properties} of all the versions of the specified
     * secret in the vault. Flux is empty if secret with {@code name} does not exist in key vault
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SecretProperties> listPropertiesOfSecretVersions(String name) {
        try {
            return mapPages(implClient.getSecretVersionsAsync(name, EMPTY_OPTIONS),
                binaryData -> SecretsModelsUtils.createSecretProperties(binaryData.toObject(SecretItem.class)));
        } catch (RuntimeException e) {
            return pagedFluxError(LOGGER, e);
        }
    }

    private static <R, T> PagedFlux<R> mapPages(PagedFlux<T> pagedFlux, Function<T, R> mapper) {
        final Function<PagedResponse<T>, PagedResponse<R>> responseMapper
            = inputResponse -> new PagedResponseBase<Void, R>(inputResponse.getRequest(), inputResponse.getStatusCode(),
                inputResponse.getHeaders(), inputResponse.getValue().stream().map(mapper).collect(Collectors.toList()),
                inputResponse.getContinuationToken(), null);

        final Supplier<PageRetriever<String, PagedResponse<R>>> provider = () -> (continuationToken, pageSize) -> {
            Flux<PagedResponse<T>> flux
                = (continuationToken == null) ? pagedFlux.byPage() : pagedFlux.byPage(continuationToken);

            return flux.map(responseMapper);
        };

        return PagedFlux.create(provider);
    }
}
