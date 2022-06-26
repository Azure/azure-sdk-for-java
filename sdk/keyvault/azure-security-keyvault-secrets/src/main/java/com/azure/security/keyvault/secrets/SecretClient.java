// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.secrets.implementation.SecretRequestAttributes;
import com.azure.security.keyvault.secrets.implementation.SecretRequestParameters;
import com.azure.security.keyvault.secrets.implementation.SecretService;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.net.URL;
import java.time.Duration;
import java.util.Objects;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * The SecretClient provides synchronous methods to manage {@link KeyVaultSecret secrets} in the Azure Key Vault. The client
 * supports creating, retrieving, updating, deleting, purging, backing up, restoring, and listing the {@link KeyVaultSecret
 * secrets}. The client also supports listing {@link DeletedSecret deleted secrets} for a soft-delete enabled Azure Key
 * Vault.
 *
 * <p><strong>Construct the sync client</strong></p>
 * <!-- src_embed com.azure.security.keyvault.secretclient.sync.construct -->
 * <pre>
 * SecretClient secretClient = new SecretClientBuilder&#40;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .vaultUrl&#40;&quot;https:&#47;&#47;myvault.vault.azure.net&#47;&quot;&#41;
 *     .httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.secretclient.sync.construct -->
 *
 * @see SecretClientBuilder
 * @see PagedIterable
 */
@ServiceClient(builder = SecretClientBuilder.class, serviceInterfaces = SecretService.class)
public final class SecretClient {
    private final String apiVersion;
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";

    private static final Duration DEFAULT_POLLING_INTERVAL = Duration.ofSeconds(1);
    private final SecretAsyncClient client;
    private final String vaultUrl;
    private final SecretServiceSync service;
    private final ClientLogger logger = new ClientLogger(SecretClient.class);
    private final HttpPipeline pipeline;

    /**
     * Gets the vault endpoint url to which service requests are sent to.
     * @return the vault endpoint url.
     */
    public String getVaultUrl() {
        return client.getVaultUrl();
    }

    /**
     * Creates a SecretClient that uses {@code pipeline} to service requests
     *
     * @param client The {@link SecretAsyncClient} that the client routes its request through.
     */
    SecretClient(SecretAsyncClient client) {
        this.client = client;
        this.vaultUrl = null;
        this.service = null;
        this.pipeline = null;
        this.apiVersion = null;
    }

    SecretClient(URL vaultUrl, HttpPipeline pipeline, SecretServiceVersion version, SecretAsyncClient client) {
        Objects.requireNonNull(vaultUrl,
            KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.client = client;
        this.vaultUrl = vaultUrl.toString();
        this.service = RestProxy.create(SecretServiceSync.class, pipeline);
        this.pipeline = pipeline;
        apiVersion = version.getVersion();
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
     * <p>Creates a new secret in the key vault. Prints out the details of the newly created secret returned in the
     * response.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.setSecret#secret -->
     * <pre>
     * KeyVaultSecret newSecret = new KeyVaultSecret&#40;&quot;secretName&quot;, &quot;secretValue&quot;&#41;
     *     .setProperties&#40;new SecretProperties&#40;&#41;.setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;60&#41;&#41;&#41;;
     * KeyVaultSecret returnedSecret = secretClient.setSecret&#40;newSecret&#41;;
     * System.out.printf&#40;&quot;Secret is created with name %s and value %s%n&quot;, returnedSecret.getName&#40;&#41;,
     *     returnedSecret.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.setSecret#secret -->
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties
     *     {@link KeyVaultSecret#getName() secret.name} and {@link KeyVaultSecret#getValue() secret.value} cannot be
     *     null.
     * @return The {@link KeyVaultSecret created secret}.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceModifiedException if {@code secret} is malformed.
     * @throws HttpResponseException if {@link KeyVaultSecret#getName() name} or {@link KeyVaultSecret#getValue() value}
     *     is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret setSecret(KeyVaultSecret secret) {
        return setSecretWithResponse(secret, Context.NONE).getValue();
    }

    /**
     * Adds a secret to the key vault if it does not exist. If the named secret exists, a new version of the secret is
     * created. This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Creates a new secret in the key vault. Prints out the details of the newly created secret returned in the
     * response.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.setSecret#string-string -->
     * <pre>
     * KeyVaultSecret secret = secretClient.setSecret&#40;&quot;secretName&quot;, &quot;secretValue&quot;&#41;;
     * System.out.printf&#40;&quot;Secret is created with name %s and value %s%n&quot;, secret.getName&#40;&#41;, secret.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.setSecret#string-string -->
     *
     * @param name The name of the secret. It is required and cannot be null.
     * @param value The value of the secret. It is required and cannot be null.
     * @return The {@link KeyVaultSecret created secret}.
     * @throws ResourceModifiedException if invalid {@code name} or {@code value} is specified.
     * @throws HttpResponseException if {@code name} or {@code value} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret setSecret(String name, String value) {
        return setSecretWithResponse(new KeyVaultSecret(name, value), Context.NONE).getValue();
    }

    /**
     * Adds a secret to the key vault if it does not exist. If the named secret exists, a new version of the secret is
     * created. This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Creates a new secret in the key vault. Prints out the details of the newly created secret returned in the
     * response.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.setSecretWithResponse#secret-Context -->
     * <pre>
     * KeyVaultSecret newSecret = new KeyVaultSecret&#40;&quot;secretName&quot;, &quot;secretValue&quot;&#41;
     *          .setProperties&#40;new SecretProperties&#40;&#41;.setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;60&#41;&#41;&#41;;
     * KeyVaultSecret secret = secretClient.setSecretWithResponse&#40;newSecret, new Context&#40;key1, value1&#41;&#41;.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Secret is created with name %s and value %s%n&quot;, secret.getName&#40;&#41;, secret.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.setSecretWithResponse#secret-Context -->
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties
     *     secret.name and secret.value must be non null.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultSecret created secret}.
     * @throws ResourceModifiedException if invalid {@code name} or {@code value} is specified.
     * @throws HttpResponseException if {@code name} or {@code value} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSecret> setSecretWithResponse(KeyVaultSecret secret, Context context) {
        SecretRequestParameters parameters = new SecretRequestParameters()
            .setValue(secret.getValue())
            .setTags(secret.getProperties().getTags())
            .setContentType(secret.getProperties().getContentType())
            .setSecretAttributes(new SecretRequestAttributes(secret.getProperties()));

        return service.setSecret(vaultUrl, secret.getName(), apiVersion, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    /**
     * Gets the specified secret with specified version from the key vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets a specific version of the secret in the key vault. Prints out the details of the returned secret.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.getSecret#string-string -->
     * <pre>
     * String secretVersion = &quot;6A385B124DEF4096AF1361A85B16C204&quot;;
     * KeyVaultSecret secretWithVersion = secretClient.getSecret&#40;&quot;secretName&quot;, secretVersion&#41;;
     * System.out.printf&#40;&quot;Secret is returned with name %s and value %s%n&quot;,
     *     secretWithVersion.getName&#40;&#41;, secretWithVersion.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.getSecret#string-string -->
     *
     * @param name The name of the secret, cannot be null.
     * @param version The version of the secret to retrieve. If this is an empty string or null, this call is
     *     equivalent to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @return The requested {@link KeyVaultSecret secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} and {@code version} doesn't exist in the
     *     key vault.
     * @throws HttpResponseException if {@code name} or {@code version} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret getSecret(String name, String version) {
        return getSecretWithResponse(name, version, Context.NONE).getValue();
    }

    /**
     * Gets the latest version of the specified secret from the key vault.
     * This operation requires the {@code secrets/get} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets the latest version of the secret in the key vault. Prints out the details of the returned secret.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.getSecret#string -->
     * <pre>
     * KeyVaultSecret secretWithoutVersion = secretClient.getSecret&#40;&quot;secretName&quot;, secretVersion&#41;;
     * System.out.printf&#40;&quot;Secret is returned with name %s and value %s%n&quot;,
     *     secretWithoutVersion.getName&#40;&#41;, secretWithoutVersion.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.getSecret#string -->
     *
     * @param name The name of the secret.
     * @return The requested {@link KeyVaultSecret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException if {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret getSecret(String name) {
        return getSecretWithResponse(name, "", Context.NONE).getValue();
    }

    /**
     * Gets the specified secret with specified version from the key vault. This operation requires the
     * {@code secrets/get} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets a specific version of the secret in the key vault. Prints out the details of the returned secret.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.getSecretWithResponse#string-string-Context -->
     * <pre>
     * String secretVersion = &quot;6A385B124DEF4096AF1361A85B16C204&quot;;
     * KeyVaultSecret secretWithVersion = secretClient.getSecretWithResponse&#40;&quot;secretName&quot;, secretVersion,
     *     new Context&#40;key2, value2&#41;&#41;.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Secret is returned with name %s and value %s%n&quot;,
     *     secretWithVersion.getName&#40;&#41;, secretWithVersion.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.getSecretWithResponse#string-string-Context -->
     *
     * @param name The name of the secret, cannot be null
     * @param version The version of the secret to retrieve. If this is an empty string or null, this call is equivalent
     *     to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested {@link KeyVaultSecret}.
     * @throws ResourceNotFoundException when a secret with {@code name} and {@code version} doesn't exist in the key
     *     vault.
     * @throws HttpResponseException if {@code name} or {@code version} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSecret> getSecretWithResponse(String name, String version, Context context) {
        return client.getSecretWithResponse(name, version, context).block();
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
     * <p>Gets the latest version of the secret, changes its expiry time, and the updates the secret in the key vault.
     * </p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.updateSecretPropertiesWithResponse#secretProperties-Context -->
     * <pre>
     * SecretProperties secretProperties = secretClient.getSecret&#40;&quot;secretName&quot;&#41;.getProperties&#40;&#41;;
     * secretProperties.setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;60&#41;&#41;;
     * SecretProperties updatedSecretBase = secretClient.updateSecretPropertiesWithResponse&#40;secretProperties,
     *     new Context&#40;key2, value2&#41;&#41;.getValue&#40;&#41;;
     * KeyVaultSecret updatedSecret = secretClient.getSecret&#40;updatedSecretBase.getName&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Updated Secret is returned with name %s, value %s and expires %s%n&quot;,
     *     updatedSecret.getName&#40;&#41;, updatedSecret.getValue&#40;&#41;, updatedSecret.getProperties&#40;&#41;.getExpiresOn&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.updateSecretPropertiesWithResponse#secretProperties-Context -->
     *
     * @param secretProperties The {@link SecretProperties secret properties} object with updated properties.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link SecretProperties updated secret}.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceNotFoundException when a secret with {@link SecretProperties#getName() name} and {@link
     *     SecretProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpResponseException if {@link SecretProperties#getName() name} or
     *     {@link SecretProperties#getVersion() version} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SecretProperties> updateSecretPropertiesWithResponse(SecretProperties secretProperties, Context context) {
        return client.updateSecretPropertiesWithResponse(secretProperties, context).block();
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
     * <p>Gets the latest version of the secret, changes its expiry time, and the updates the secret in the key
     * vault.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.updateSecretProperties#secretProperties -->
     * <pre>
     * SecretProperties secretProperties = secretClient.getSecret&#40;&quot;secretName&quot;&#41;.getProperties&#40;&#41;;
     * secretProperties.setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;60&#41;&#41;;
     * SecretProperties updatedSecretProperties = secretClient.updateSecretProperties&#40;secretProperties&#41;;
     * KeyVaultSecret updatedSecret = secretClient.getSecret&#40;updatedSecretProperties.getName&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Updated Secret is returned with name %s, value %s and expires %s%n&quot;,
     *     updatedSecret.getName&#40;&#41;, updatedSecret.getValue&#40;&#41;, updatedSecret.getProperties&#40;&#41;.getExpiresOn&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.updateSecretProperties#secretProperties -->
     *
     * @param secretProperties The {@link SecretProperties secret properties} object with updated properties.
     * @return The {@link SecretProperties updated secret}.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceNotFoundException when a secret with {@link SecretProperties#getName() name} and {@link
     *     SecretProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpResponseException if {@link SecretProperties#getName() name} or {@link SecretProperties#getVersion() version} is
     *     empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SecretProperties updateSecretProperties(SecretProperties secretProperties) {
        return updateSecretPropertiesWithResponse(secretProperties, Context.NONE).getValue();
    }

    /**
     * Deletes a secret from the key vault. If soft-delete is enabled on the key vault then the secret is placed in the
     * deleted state and for permanent deletion, needs to be purged. Otherwise, the secret is permanently deleted.
     * All versions of a secret are deleted. This cannot be applied to individual versions of a secret.
     * This operation requires the {@code secrets/delete} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Deletes the secret from a soft-delete enabled key vault. Prints out the recovery id of the deleted secret
     * returned in the response.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.deleteSecret#String -->
     * <pre>
     * SyncPoller&lt;DeletedSecret, Void&gt; deleteSecretPoller = secretClient.beginDeleteSecret&#40;&quot;secretName&quot;&#41;;
     *
     * &#47;&#47; Deleted Secret is accessible as soon as polling begins.
     * PollResponse&lt;DeletedSecret&gt; deleteSecretPollResponse = deleteSecretPoller.poll&#40;&#41;;
     *
     * &#47;&#47; Deletion date only works for a SoftDelete-enabled Key Vault.
     * System.out.println&#40;&quot;Deleted Date  %s&quot; + deleteSecretPollResponse.getValue&#40;&#41;
     *     .getDeletedOn&#40;&#41;.toString&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Deleted Secret's Recovery Id %s&quot;, deleteSecretPollResponse.getValue&#40;&#41;
     *     .getRecoveryId&#40;&#41;&#41;;
     *
     * &#47;&#47; Secret is being deleted on server.
     * deleteSecretPoller.waitForCompletion&#40;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.deleteSecret#String -->
     *
     * @param name The name of the secret to be deleted.
     * @return A {@link SyncPoller} to poll on and retrieve the {@link DeletedSecret deleted secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DeletedSecret, Void> beginDeleteSecret(String name) {
        return client.beginDeleteSecret(name).getSyncPoller();
    }

    /**
     * Gets a secret that has been deleted for a soft-delete enabled key vault. This operation requires the
     * {@code secrets/list} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets the deleted secret from the key vault <b>enabled for soft-delete</b>. Prints out the details of the
     * deleted secret returned in the response.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.getDeletedSecret#string -->
     * <pre>
     * DeletedSecret deletedSecret = secretClient.getDeletedSecret&#40;&quot;secretName&quot;&#41;;
     * System.out.printf&#40;&quot;Deleted Secret's Recovery Id %s&quot;, deletedSecret.getRecoveryId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.getDeletedSecret#string -->
     *
     * @param name The name of the deleted secret.
     * @return The {@link DeletedSecret deleted secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeletedSecret getDeletedSecret(String name) {
        return getDeletedSecretWithResponse(name, Context.NONE).getValue();
    }

    /**
     * Gets a secret that has been deleted for a soft-delete enabled key vault. This operation requires the
     * {@code secrets/list} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Gets the deleted secret from the key vault <b>enabled for soft-delete</b>. Prints out the details of the
     * deleted secret returned in the response.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.getDeletedSecretWithResponse#string-Context -->
     * <pre>
     * DeletedSecret deletedSecret = secretClient.getDeletedSecretWithResponse&#40;&quot;secretName&quot;,
     *     new Context&#40;key2, value2&#41;&#41;.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Deleted Secret's Recovery Id %s&quot;, deletedSecret.getRecoveryId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.getDeletedSecretWithResponse#string-Context -->
     *
     * @param name The name of the deleted secret.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DeletedSecret deleted
     * secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeletedSecret> getDeletedSecretWithResponse(String name, Context context) {
        return client.getDeletedSecretWithResponse(name, context).block();
    }

    /**
     * Permanently removes a deleted secret, without the possibility of recovery. This operation can only be performed
     * on a <b>soft-delete enabled</b> vault. This operation requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Purges the deleted secret from the key vault enabled for <b>soft-delete</b>. Prints out the status code from
     * the server response.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.secretclient.purgeDeletedSecret#string -->
     * <pre>
     * secretClient.purgeDeletedSecret&#40;&quot;secretName&quot;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.purgeDeletedSecret#string -->
     *
     * @param name The name of the secret.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void purgeDeletedSecret(String name) {
        purgeDeletedSecretWithResponse(name, Context.NONE);
    }

    /**
     * Permanently removes a deleted secret, without the possibility of recovery. This operation can only be performed
     * on a <b>soft-delete enabled</b> vault. This operation requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Purges the deleted secret from the key vault enabled for <b>soft-delete</b>. Prints out the status code from
     * the server response.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.purgeDeletedSecretWithResponse#string-Context -->
     * <pre>
     * Response&lt;Void&gt; purgeResponse = secretClient.purgeDeletedSecretWithResponse&#40;&quot;secretName&quot;,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Purge Status Code: %d&quot;, purgeResponse.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.purgeDeletedSecretWithResponse#string-Context -->
     *
     * @param name The name of the secret.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> purgeDeletedSecretWithResponse(String name, Context context) {
        return client.purgeDeletedSecretWithResponse(name, context).block();
    }

    /**
     * Recovers the deleted secret in the key vault to its latest version. Can only be performed on a <b>soft-delete
     * enabled</b> vault. This operation requires the {@code secrets/recover} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recovers the deleted secret from the key vault enabled for <b>soft-delete</b>. Prints out the details of the
     * recovered secret returned in the response.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.recoverDeletedSecret#String -->
     * <pre>
     * SyncPoller&lt;KeyVaultSecret, Void&gt; recoverSecretPoller =
     *     secretClient.beginRecoverDeletedSecret&#40;&quot;deletedSecretName&quot;&#41;;
     *
     * &#47;&#47; Deleted Secret can be accessed as soon as polling is in progress.
     * PollResponse&lt;KeyVaultSecret&gt; recoveredSecretPollResponse = recoverSecretPoller.poll&#40;&#41;;
     * System.out.println&#40;&quot;Recovered Key Name %s&quot; + recoveredSecretPollResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Recovered Key's Id %s&quot;, recoveredSecretPollResponse.getValue&#40;&#41;.getId&#40;&#41;&#41;;
     *
     * &#47;&#47; Key is being recovered on server.
     * recoverSecretPoller.waitForCompletion&#40;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.recoverDeletedSecret#String -->
     *
     * @param name The name of the deleted secret to be recovered.
     * @return A {@link SyncPoller} to poll on and retrieve the {@link KeyVaultSecret recovered secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultSecret, Void> beginRecoverDeletedSecret(String name) {
        return client.beginRecoverDeletedSecret(name).getSyncPoller();
    }

    /**
     * Requests a backup of the secret be downloaded to the client. All versions of the secret will be downloaded.
     * This operation requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Backs up the secret from the key vault and prints out the length of the secret's backup byte array returned in
     * the response</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.backupSecret#string -->
     * <pre>
     * byte[] secretBackup = secretClient.backupSecret&#40;&quot;secretName&quot;&#41;;
     * System.out.printf&#40;&quot;Secret's Backup Byte array's length %s&quot;, secretBackup.length&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.backupSecret#string -->
     *
     * @param name The name of the secret.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the backed up secret blob.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] backupSecret(String name) {
        return backupSecretWithResponse(name, Context.NONE).getValue();
    }

    /**
     * Requests a backup of the secret be downloaded to the client. All versions of the secret will be downloaded.
     * This operation requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Backs up the secret from the key vault and prints out the length of the secret's backup byte array returned in
     * the response</p>
     *
     * <!-- src_embed com.azure.security.keyvault.secretclient.backupSecretWithResponse#string-Context -->
     * <pre>
     * byte[] secretBackup = secretClient.backupSecretWithResponse&#40;&quot;secretName&quot;,
     *     new Context&#40;key1, value1&#41;&#41;.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Secret's Backup Byte array's length %s&quot;, secretBackup.length&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.backupSecretWithResponse#string-Context -->
     *
     * @param name The name of the secret.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the backed up secret blob.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<byte[]> backupSecretWithResponse(String name, Context context) {
        return client.backupSecretWithResponse(name, context).block();
    }

    /**
     * Restores a backed up secret, and all its versions, to a vault.
     * This operation requires the {@code secrets/restore} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Restores the secret in the key vault from its backup byte array. Prints out the details of the restored secret
     * returned in the response.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.secretclient.restoreSecret#byte -->
     * <pre>
     * &#47;&#47; Pass the secret backup byte array of the secret to be restored.
     * byte[] secretBackupByteArray = &#123;&#125;;
     * KeyVaultSecret restoredSecret = secretClient.restoreSecretBackup&#40;secretBackupByteArray&#41;;
     * System.out
     *     .printf&#40;&quot;Restored Secret with name %s and value %s&quot;, restoredSecret.getName&#40;&#41;, restoredSecret.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.restoreSecret#byte -->
     *
     * @param backup The backup blob associated with the secret.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultSecret restored secret}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSecret restoreSecretBackup(byte[] backup) {
        return restoreSecretBackupWithResponse(backup, Context.NONE).getValue();
    }

    /**
     * Restores a backed up secret, and all its versions, to a vault.
     * This operation requires the {@code secrets/restore} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Restores the secret in the key vault from its backup byte array. Prints out the details of the restored secret
     * returned in the response.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.secretclient.restoreSecretWithResponse#byte-Context -->
     * <pre>
     * &#47;&#47; Pass the secret backup byte array of the secret to be restored.
     * byte[] secretBackupByteArray = &#123;&#125;;
     * KeyVaultSecret restoredSecret = secretClient.restoreSecretBackupWithResponse&#40;secretBackupByteArray,
     *     new Context&#40;key2, value2&#41;&#41;.getValue&#40;&#41;;
     * System.out
     *     .printf&#40;&quot;Restored Secret with name %s and value %s&quot;, restoredSecret.getName&#40;&#41;, restoredSecret.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.restoreSecretWithResponse#byte-Context -->
     *
     * @param backup The backup blob associated with the secret.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultSecret restored secret}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSecret> restoreSecretBackupWithResponse(byte[] backup, Context context) {
        return client.restoreSecretBackupWithResponse(backup, context).block();
    }

    /**
     * Lists secrets in the key vault. Each {@link SecretProperties secret} returned only has its identifier and
     * attributes populated. The secret values and their versions are not listed in the response.
     * This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Iterate through secrets and fetch their latest value</strong></p>
     * <p>The snippet below loops over each {@link SecretProperties secret} and calls
     * {@link #getSecret(String, String) getSecret(String, String)}. This gets the {@link KeyVaultSecret secret} and the
     * value of its latest version.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.secretclient.listSecrets -->
     * <pre>
     * for &#40;SecretProperties secret : secretClient.listPropertiesOfSecrets&#40;&#41;&#41; &#123;
     *     KeyVaultSecret secretWithValue = secretClient.getSecret&#40;secret.getName&#40;&#41;, secret.getVersion&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Received secret with name %s and value %s&quot;,
     *         secretWithValue.getName&#40;&#41;, secretWithValue.getValue&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.listSecrets -->
     *
     * <p><strong>Iterate over secrets by page</strong></p>
     * <p>The snippet below loops over each {@link SecretProperties secret} by page and calls
     * {@link #getSecret(String, String) getSecret(String, String)}. This gets the {@link KeyVaultSecret secret} and the
     * value of its latest version.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.secretclient.listSecrets.iterableByPage -->
     * <pre>
     * secretClient.listPropertiesOfSecrets&#40;&#41;.iterableByPage&#40;&#41;.forEach&#40;resp -&gt; &#123;
     *     System.out.printf&#40;&quot;Response headers are %s. Url %s  and status code %d %n&quot;, resp.getHeaders&#40;&#41;,
     *         resp.getRequest&#40;&#41;.getUrl&#40;&#41;, resp.getStatusCode&#40;&#41;&#41;;
     *     resp.getItems&#40;&#41;.forEach&#40;value -&gt; &#123;
     *         KeyVaultSecret secretWithValue = secretClient.getSecret&#40;value.getName&#40;&#41;, value.getVersion&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Received secret with name %s and value %s&quot;,
     *             secretWithValue.getName&#40;&#41;, secretWithValue.getValue&#40;&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.listSecrets.iterableByPage -->
     *
     * @return {@link PagedIterable} of {@link SecretProperties} of all the secrets in the vault. The
     *     {@link SecretProperties} contains all the information about the secret, except its value.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SecretProperties> listPropertiesOfSecrets() {
        return listPropertiesOfSecrets(Context.NONE);
    }

    /**
     * Lists secrets in the key vault. Each {@link SecretProperties secret} returned only has its identifier and
     * attributes populated. The secret values and their versions are not listed in the response.
     * This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Iterate over secrets and fetch their latest value</strong></p>
     * <p>The snippet below loops over each {@link SecretProperties secret} and calls
     * {@link #getSecret(String, String) getSecret(String, String)}. This gets the {@link KeyVaultSecret secret} and the
     * value of its latest version.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.listSecrets#Context -->
     * <pre>
     * for &#40;SecretProperties secret : secretClient.listPropertiesOfSecrets&#40;new Context&#40;key1, value2&#41;&#41;&#41; &#123;
     *     KeyVaultSecret secretWithValue = secretClient.getSecret&#40;secret.getName&#40;&#41;, secret.getVersion&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Received secret with name %s and value %s&quot;,
     *         secretWithValue.getName&#40;&#41;, secretWithValue.getValue&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.listSecrets#Context -->
     *
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return {@link PagedIterable} of {@link SecretProperties} of all the secrets in the vault. {@link SecretProperties}
     *     contains all the information about the secret, except its value.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SecretProperties> listPropertiesOfSecrets(Context context) {
        return new PagedIterable<>(client.listPropertiesOfSecrets(context));
    }

    /**
     * Lists {@link DeletedSecret deleted secrets} of the key vault if it has enabled soft-delete. This operation
     * requires the {@code secrets/list} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Lists the deleted secrets in the key vault and for each deleted secret prints out its recovery id.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.listDeletedSecrets#Context -->
     * <pre>
     * for &#40;DeletedSecret deletedSecret : secretClient.listDeletedSecrets&#40;new Context&#40;key1, value2&#41;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Deleted secret's recovery Id %s&quot;, deletedSecret.getRecoveryId&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.listDeletedSecrets#Context -->
     *
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return {@link PagedIterable} of all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedSecret> listDeletedSecrets(Context context) {
        return new PagedIterable<>(client.listDeletedSecrets(context));
    }

    /**
     * Lists {@link DeletedSecret deleted secrets} of the key vault if it has enabled soft-delete. This operation
     * requires the {@code secrets/list} permission.
     *
     * <p><strong>Iterate over secrets</strong></p>
     * <p>Lists the deleted secrets in the key vault and for each deleted secret prints out its recovery id.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.listDeletedSecrets -->
     * <pre>
     * for &#40;DeletedSecret deletedSecret : secretClient.listDeletedSecrets&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Deleted secret's recovery Id %s&quot;, deletedSecret.getRecoveryId&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.listDeletedSecrets -->
     *
     * <p><strong>Iterate over secrets by page</strong></p>
     * <p>Iterate over Lists the deleted secrets by page in the key vault and for each deleted secret prints out its
     * recovery id.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.listDeletedSecrets.iterableByPage -->
     * <pre>
     * secretClient.listDeletedSecrets&#40;&#41;.iterableByPage&#40;&#41;.forEach&#40;resp -&gt; &#123;
     *     System.out.printf&#40;&quot;Got response headers . Url: %s, Status code: %d %n&quot;,
     *         resp.getRequest&#40;&#41;.getUrl&#40;&#41;, resp.getStatusCode&#40;&#41;&#41;;
     *     resp.getItems&#40;&#41;.forEach&#40;value -&gt; &#123;
     *         System.out.printf&#40;&quot;Deleted secret's recovery Id %s&quot;, value.getRecoveryId&#40;&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.listDeletedSecrets.iterableByPage -->
     *
     * @return {@link PagedIterable} of all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedSecret> listDeletedSecrets() {
        return listDeletedSecrets(Context.NONE);
    }

    /**
     * Lists all versions of the specified secret. Each {@link SecretProperties secret} returned only has its identifier
     * and attributes populated. The secret values and secret versions are not listed in the response.
     * This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>The sample below fetches all versions of the given secret. For each secret version retrieved, makes a call
     * to {@link #getSecret(String, String) getSecret(String, String)} to get the version's value, and then prints it out.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.listSecretVersions#string -->
     * <pre>
     * for &#40;SecretProperties secret : secretClient.listPropertiesOfSecretVersions&#40;&quot;secretName&quot;&#41;&#41; &#123;
     *     KeyVaultSecret secretWithValue = secretClient.getSecret&#40;secret.getName&#40;&#41;, secret.getVersion&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Received secret's version with name %s and value %s&quot;,
     *         secretWithValue.getName&#40;&#41;, secretWithValue.getValue&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.listSecretVersions#string -->
     *
     * @param name The name of the secret.
     * @return {@link PagedIterable} of {@link SecretProperties} of all the versions of the specified secret in the vault.
     *     List is empty if secret with {@code name} does not exist in key vault
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SecretProperties> listPropertiesOfSecretVersions(String name) {
        return listPropertiesOfSecretVersions(name, Context.NONE);
    }

    /**
     * Lists all versions of the specified secret. Each {@link SecretProperties secret} returned only has its identifier
     * and attributes populated. The secret values and secret versions are not listed in the response.
     * This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code sample</strong></p>
     * <p>The sample below fetches all versions of the given secret. For each secret version retrieved, makes a call
     * to {@link #getSecret(String, String) getSecret(String, String)} to get the version's value, and then prints it out.</p>
     * <!-- src_embed com.azure.security.keyvault.secretclient.listSecretVersions#string-Context -->
     * <pre>
     * for &#40;SecretProperties secret : secretClient
     *     .listPropertiesOfSecretVersions&#40;&quot;secretName&quot;, new Context&#40;key1, value2&#41;&#41;&#41; &#123;
     *     KeyVaultSecret secretWithValue = secretClient.getSecret&#40;secret.getName&#40;&#41;, secret.getVersion&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Received secret's version with name %s and value %s&quot;,
     *         secretWithValue.getName&#40;&#41;, secretWithValue.getValue&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.listSecretVersions#string-Context -->
     *
     * <p><strong>Iterate over secret versions by page</strong></p>
     * <p>The sample below iterates over each {@link SecretProperties secret} by each page and calls
     * {@link SecretClient#getSecret(String, String)}. This will return the {@link KeyVaultSecret secret} with the
     * corresponding version's value.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.secretclient.listSecretVersions#string-Context-iterableByPage -->
     * <pre>
     * secretClient.listPropertiesOfSecretVersions&#40;&quot;secretName&quot;, new Context&#40;key1, value2&#41;&#41;
     *             .iterableByPage&#40;&#41;.forEach&#40;resp -&gt; &#123;
     *                 System.out.printf&#40;&quot;Got response headers . Url: %s, Status code: %d %n&quot;,
     *                     resp.getRequest&#40;&#41;.getUrl&#40;&#41;, resp.getStatusCode&#40;&#41;&#41;;
     *                 resp.getItems&#40;&#41;.forEach&#40;value -&gt; &#123;
     *                     KeyVaultSecret secretWithValue = secretClient.getSecret&#40;value.getName&#40;&#41;, value.getVersion&#40;&#41;&#41;;
     *                     System.out.printf&#40;&quot;Received secret's version with name %s and value %s&quot;,
     *                         secretWithValue.getName&#40;&#41;, secretWithValue.getValue&#40;&#41;&#41;;
     *                 &#125;&#41;;
     *             &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.secretclient.listSecretVersions#string-Context-iterableByPage -->
     *
     * @param name The name of the secret.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return {@link PagedIterable} of {@link SecretProperties} of all the versions of the specified secret in the vault.
     *     List is empty if secret with {@code name} does not exist in key vault
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException when a secret with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SecretProperties> listPropertiesOfSecretVersions(String name, Context context) {
        return new PagedIterable<>(client.listPropertiesOfSecretVersions(name, context));
    }
}
