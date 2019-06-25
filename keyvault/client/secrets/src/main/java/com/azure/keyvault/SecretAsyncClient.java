// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault;

import com.azure.core.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.RestProxy;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import com.azure.keyvault.implementation.SecretBasePage;
import com.azure.keyvault.models.DeletedSecret;
import com.azure.keyvault.models.Secret;
import com.azure.keyvault.models.SecretBase;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Objects;
import java.util.function.Function;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.HttpRequestException;

/**
 * The SecretAsyncClient provides asynchronous methods to manage {@link Secret secrets} in the Azure Key Vault. The client
 * supports creating, retrieving, updating, deleting, purging, backing up, restoring and listing the {@link Secret secrets}. The client
 * also supports listing {@link DeletedSecret deleted secrets} for a soft-delete enabled Azure Key Vault.
 *
 * <p><strong>Samples to construct the client</strong></p>
 * <pre>
 * SecretAsyncClient.builder()
 *   .endpoint("https://myvault.vault.azure.net/")
 *   .credential(keyVaultCredential)
 *   .build()
 * </pre>
 *
 * @see SecretAsyncClientBuilder
 */
public final class SecretAsyncClient extends ServiceClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    static final String KEY_VAULT_SCOPE = "https://vault.azure.net/.default";

    private String endpoint;
    private final SecretService service;

    /**
     * Creates a SecretAsyncClient that uses {@code pipeline} to service requests
     *
     * @param endpoint URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    SecretAsyncClient(URL endpoint, HttpPipeline pipeline) {
        super(pipeline);
        Objects.requireNonNull(endpoint, KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.endpoint = endpoint.toString();
        this.service = RestProxy.create(SecretService.class, this);
    }

    /**
     * Creates a builder that can configure options for the SecretAsyncClient before creating an instance of it.
     * @return A new builder to create a SecretAsyncClient from.
     */
    public static SecretAsyncClientBuilder builder() {
        return new SecretAsyncClientBuilder();
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
     * <pre>
     * Secret secret = new Secret("secretName", "secretValue")
     *   .notBefore(OffsetDateTime.now().plusDays(1))
     *   .expires(OffsetDateTime.now().plusDays(365));
     *
     * secretAsyncClient.setSecret(secret).subscribe(secretResponse -&gt;
     *   System.out.printf("Secret is created with name %s and value %s \n", secretResponse.value().name(), secretResponse.value().value()));
     * </pre>
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties secret.name and secret.value must be non null.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceModifiedException if {@code secret} is malformed.
     * @throws HttpRequestException if {@link Secret#name()  name} or {@link Secret#value() value} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link Secret created secret}.
     */
    public Mono<Response<Secret>> setSecret(Secret secret) {
        Objects.requireNonNull(secret, "The Secret input parameter cannot be null.");
        SecretRequestParameters parameters = new SecretRequestParameters()
            .value(secret.value())
            .tags(secret.tags())
            .contentType(secret.contentType())
            .secretAttributes(new SecretRequestAttributes(secret));

        return service.setSecret(endpoint, secret.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * The set operation adds a secret to the key vault. If the named secret already exists, Azure Key Vault creates a new version of that secret.
     * This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new secret in the key vault. Subscribes to the call asynchronously and prints out the newly
     * created secret details when a response is received.</p>
     * <pre>
     * secretAsyncClient.setSecret("secretName", "secretValue").subscribe(secretResponse -&gt;
     *   System.out.printf("Secret is created with name %s and value %s \n", secretResponse.value().name(), secretResponse.value().value()));
     * </pre>
     *
     * @param name The name of the secret. It is required and cannot be null.
     * @param value The value of the secret. It is required and cannot be null.
     * @throws ResourceModifiedException if invalid {@code name} or {@code value} are specified.
     * @throws HttpRequestException if {@code name} or {@code value} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link Secret created secret}.
     */
    public Mono<Response<Secret>> setSecret(String name, String value) {
        SecretRequestParameters parameters = new SecretRequestParameters().value(value);
        return service.setSecret(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * Get the specified secret with specified version from the key vault. The get operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the {@code secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the secret in the key vault. Subscribes to the call asynchronously and prints out the
     * returned secret details when a response is received.</p>
     * <pre>
     * String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
     * secretAsyncClient.getSecret("secretName", secretVersion).subscribe(secretResponse -&gt;
     *   System.out.printf("Secret with name %s, value %s and version %s", secretResponse.value().name(),
     *   secretResponse.value().value(), secretResponse.value().version()));
     * </pre>
     *
     * @param name The name of the secret, cannot be null
     * @param version The version of the secret to retrieve. If this is an empty String or null, this call is equivalent to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @throws ResourceNotFoundException when a secret with {@code name} and {@code version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name}  name} or {@code version} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the requested {@link Secret secret}.
     */
    public Mono<Response<Secret>> getSecret(String name, String version) {
        String secretVersion = "";
        if (version != null) {
            secretVersion = version;
        }
        return service.getSecret(endpoint, name, secretVersion, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * Get the secret which represents {@link SecretBase secretBase} from the key vault. The get operation is applicable to any
     * secret stored in Azure Key Vault. This operation requires the {@code secrets/get} permission.
     *
     * <p>The list operations {@link SecretAsyncClient#listSecrets()} and {@link SecretAsyncClient#listSecretVersions(String)} return
     * the {@link Flux} containing {@link SecretBase base secret} as output excluding the include the value of the secret.
     * This operation can then be used to get the full secret with its value from {@code secretBase}. </p>
     * <pre>
     * secretAsyncClient.listSecrets().subscribe(secretBase -&gt;
     *     client.getSecret(secretBase).subscribe(secretResponse -&gt;
     *       System.out.printf("Secret with name %s and value %s \n", secretResponse.value().name(), secretResponse.value().value())));
     * </pre>
     *
     * @param secretBase The {@link SecretBase base secret} secret base holding attributes of the secret being requested.
     * @throws ResourceNotFoundException when a secret with {@link SecretBase#name() name} and {@link SecretBase#version() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link SecretBase#name()}  name} or {@link SecretBase#version() version} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the requested {@link Secret secret}.
     */
    public Mono<Response<Secret>> getSecret(SecretBase secretBase) {
        Objects.requireNonNull(secretBase, "The Secret Base parameter cannot be null.");
        return secretBase.version() == null ? service.getSecret(endpoint, secretBase.name(), "", API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
            : service.getSecret(endpoint, secretBase.name(), secretBase.version(), API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }
    /**
     * Get the latest version of the specified secret from the key vault. The get operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the {@code secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the secret in the key vault. Subscribes to the call asynchronously and prints out the
     * returned secret details when a response is received.</p>
     * <pre>
     * secretAsyncClient.getSecret("secretName").subscribe(secretResponse -&gt;
     *   System.out.printf("Secret with name %s , value %s \n", secretResponse.value().name(),
     *   secretResponse.value().value()));
     * </pre>
     *
     * @param name The name of the secret.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the requested {@link Secret secret}.
     */
    public Mono<Response<Secret>> getSecret(String name) {
        return getSecret(name, "");
    }

    /**
     * Updates the attributes associated with the specified secret, but not the value of the specified secret in the key vault. The update
     * operation changes specified attributes of an existing stored secret and attributes that are not specified in the request are left unchanged.
     * The value of a secret itself cannot be changed. This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the secret, changes its notBefore time and then updates it in the Azure Key Vault. Subscribes to the call asynchronously and prints out the
     * returned secret details when a response is received.</p>
     * <pre>
     * secretAsyncClient.getSecret("secretName").subscribe(secretResponse -&gt; {
     *     Secret secret = secretResponse.value();
     *     //Update the not before time of the secret.
     *     secret.notBefore(OffsetDateTime.now().plusDays(50));
     *     secretAsyncClient.updateSecret(secret).subscribe(secretResponse -&gt;
     *         System.out.printf("Secret's updated not before time %s \n", secretResponse.value().notBefore().toString()));
     *   });
     * </pre>
     * <p>The {@code secret} is required and its fields {@link SecretBase#name() name} and {@link SecretBase#version() version} cannot be null.</p>
     *
     * @param secret The {@link SecretBase base secret} object with updated properties.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceNotFoundException when a secret with {@link SecretBase#name() name} and {@link SecretBase#version() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link SecretBase#name()}  name} or {@link SecretBase#version() version} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link SecretBase updated secret}.
     */
    public Mono<Response<SecretBase>> updateSecret(SecretBase secret) {
        Objects.requireNonNull(secret, "The secret input parameter cannot be null.");
        SecretRequestParameters parameters = new SecretRequestParameters()
                .tags(secret.tags())
                .contentType(secret.contentType())
                .secretAttributes(new SecretRequestAttributes(secret));

        return service.updateSecret(endpoint, secret.name(), secret.version(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * Deletes a secret from the key vault. If soft-delete is enabled on the key vault then the secret is placed in the deleted state
     * and requires to be purged for permanent deletion else the secret is permanently deleted. The delete operation applies to any secret stored in Azure Key Vault but
     * it cannot be applied to an individual version of a secret. This operation requires the {@code secrets/delete} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the secret in the Azure Key Vault. Subscribes to the call asynchronously and prints out the
     * deleted secret details when a response is received.</p>
     * <pre>
     * secretAsyncClient.deleteSecret("secretName").subscribe(deletedSecretResponse -&gt;
     *   System.out.printf("Deleted Secret's Recovery Id %s \n", deletedSecretResponse.value().recoveryId()));
     * </pre>
     *
     * @param name The name of the secret to be deleted.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link DeletedSecret deleted secret}.
     */
    public Mono<Response<DeletedSecret>> deleteSecret(String name) {
        return service.deleteSecret(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * The get deleted secret operation returns the secrets that have been deleted for a vault enabled for soft-delete.
     * This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Gets the deleted secret from the key vault enabled for soft-delete. Subscribes to the call asynchronously and prints out the
     * deleted secret details when a response is received.</p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled vault.
     * secretAsyncClient.getDeletedSecret("secretName").subscribe(deletedSecretResponse -&gt;
     *   System.out.printf("Deleted Secret with recovery Id %s \n", deletedSecretResponse.value().recoveryId()));
     * </pre>
     *
     * @param name The name of the deleted secret.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link DeletedSecret deleted secret}.
     */
    public Mono<Response<DeletedSecret>> getDeletedSecret(String name) {
        return service.getDeletedSecret(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * The purge deleted secret operation removes the secret permanently, without the possibility of recovery.
     * This operation can only be enabled on a soft-delete enabled vault. This operation requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted secret from the key vault enabled for soft-delete. Subscribes to the call asynchronously and prints out the
     * status code from the server response when a response is received.</p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled vault.
     * secretAsyncClient.purgeDeletedSecret("deletedSecretName").subscribe(purgeResponse -&gt;
     *   System.out.printf("Purge Status response %d \n", purgeResponse.statusCode()));
     * </pre>
     *
     * @param name The name of the secret.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link VoidResponse}.
     */
    public Mono<VoidResponse> purgeDeletedSecret(String name) {
        return service.purgeDeletedSecret(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
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
     * secretAsyncClient.recoverDeletedSecret("deletedSecretName").subscribe(recoveredSecretResponse -&gt;
     *   System.out.printf("Recovered Secret with name %s \n", recoveredSecretResponse.value().name()));
     * </pre>
     *
     * @param name The name of the deleted secret to be recovered.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link Secret recovered secret}.
     */
    public Mono<Response<Secret>> recoverDeletedSecret(String name) {
        return service.recoverDeletedSecret(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * Requests a backup of the specified secret be downloaded to the client. All versions of the secret will be downloaded.
     * This operation requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the secret from the key vault. Subscribes to the call asynchronously and prints out the
     * length of the secret's backup byte array returned in the response.</p>
     * <pre>
     * secretAsyncClient.backupSecret("secretName").subscribe(secretBackupResponse -&gt;
     *   System.out.printf("Secret's Backup Byte array's length %s \n", secretBackupResponse.value().length));
     * </pre>
     *
     * @param name The name of the secret.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the backed up secret blob.
     */
    public Mono<Response<byte[]>> backupSecret(String name) {
        return service.backupSecret(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .flatMap(base64URLResponse ->  Mono.just(new SimpleResponse<byte[]>(base64URLResponse.request(),
                base64URLResponse.statusCode(), base64URLResponse.headers(), base64URLResponse.value().value())));
    }

    /**
     * Restores a backed up secret, and all its versions, to a vault. This operation requires the {@code secrets/restore} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the secret in the key vault from its backup. Subscribes to the call asynchronously and prints out the
     * restored secret details when a response is received.</p>
     * <pre>
     * //Pass the Secret Backup Byte array to the restore operation.
     * secretAsyncClient.restoreSecret(secretBackupByteArray).subscribe(secretResponse -&gt;
     *   System.out.printf("Restored Secret with name %s and value %s \n", secretResponse.value().name(), secretResponse.value().value()));
     * </pre>
     *
     * @param backup The backup blob associated with the secret.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the {@link Secret restored secret}.
     */
    public Mono<Response<Secret>> restoreSecret(byte[] backup) {
        SecretRestoreRequestParameters parameters = new SecretRestoreRequestParameters().secretBackup(backup);
        return service.restoreSecret(endpoint, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * List secrets in the key vault. The list Secrets operation is applicable to the entire vault. The individual secret response
     * in the flux is represented by {@link SecretBase} as only the base secret identifier and its attributes are
     * provided in the response. The secret values and individual secret versions are not listed in the response. This operation requires the {@code secrets/list} permission.
     *
     * <p>It is possible to get full Secrets with values from this information. Convert the {@link Flux} containing {@link SecretBase base secret} to
     * {@link Flux} containing {@link Secret secret} using {@link SecretAsyncClient#getSecret(SecretBase baseSecret)} within {@link Flux#flatMap(Function)}.</p>
     * <pre>
     * Flux&lt;Secret&gt; secrets = secretAsyncClient.listSecrets()
     *   .flatMap(secretBase -&gt;
     *     client.getSecret(secretBase).map(secretResponse -&gt; secretResponse.value()));
     * </pre>
     *
     * @return A {@link Flux} containing {@link SecretBase secret} of all the secrets in the vault.
     */
    public Flux<SecretBase> listSecrets() {
        return service.getSecrets(endpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(r -> extractAndFetchSecrets(r, Context.NONE));
    }

    /**
     * Lists {@link DeletedSecret deleted secrets} of the key vault. The get deleted secrets operation returns the secrets that
     * have been deleted for a vault enabled for soft-delete. This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted secrets in the key vault. Subscribes to the call asynchronously and prints out the
     * recovery id of each deleted secret when a response is received.</p>
     * <pre>
     * secretAsyncClient.listDeletedSecrets().subscribe(deletedSecret -&gt;
     *   System.out.printf("Deleted secret's recovery Id %s \n", deletedSecret.recoveryId()));
     * </pre>
     *
     * @return A {@link Flux} containing all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    public Flux<DeletedSecret> listDeletedSecrets() {
        return service.getDeletedSecrets(endpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(r -> extractAndFetchDeletedSecrets(r, Context.NONE));
    }

    /**
     * List all versions of the specified secret. The individual secret response in the flux is represented by {@link SecretBase}
     * as only the base secret identifier and its attributes are provided in the response. The secret values are
     * not provided in the response. This operation requires the {@code secrets/list} permission.
     *
     * <p>It is possible to get the Secret with value of all the versions from this information. Convert the {@link Flux}
     * containing {@link SecretBase base secret} to {@link Flux} containing {@link Secret secret} using
     * {@link SecretAsyncClient#getSecret(SecretBase baseSecret)} within {@link Flux#flatMap(Function)}.</p>
     * <pre>
     * Flux&lt;Secret&gt; secrets = secretAsyncClient.listSecretVersions("secretName")
     *   .flatMap(secretBase -&gt;
     *     client.getSecret(secretBase).map(secretResponse -&gt; secretResponse.value()));
     * </pre>
     *
     * @param name The name of the secret.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link Flux} containing {@link SecretBase secret} of all the versions of the specified secret in the vault. Flux is empty if secret with {@code name} does not exist in key vault
     */
    public Flux<SecretBase> listSecretVersions(String name) {
        return service.getSecretVersions(endpoint, name, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(r -> extractAndFetchSecrets(r, Context.NONE));
    }

    /**
     * Gets attributes of all the secrets given by the {@code nextPageLink} that was retrieved from a call to
     * {@link SecretAsyncClient#listSecrets()}.
     *
     * @param nextPageLink The {@link SecretBasePage#nextLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link SecretBase secret} from the next page of results.
     */
    private Flux<SecretBase> listSecretsNext(String nextPageLink, Context context) {
        return service.getSecrets(endpoint, nextPageLink, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(r -> extractAndFetchSecrets(r, context));
    }

    private Publisher<SecretBase> extractAndFetchSecrets(PagedResponse<SecretBase> page, Context context) {
        return ImplUtils.extractAndFetch(page, context, this::listSecretsNext);
    }

    /**
     * Gets attributes of all the secrets given by the {@code nextPageLink} that was retrieved from a call to
     * {@link SecretAsyncClient#listDeletedSecrets()}.
     *
     * @param nextPageLink The {@link com.azure.keyvault.implementation.DeletedSecretPage#nextLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link SecretBase secret} from the next page of results.
     */
    private Flux<DeletedSecret> listDeletedSecretsNext(String nextPageLink, Context context) {
        return service.getDeletedSecrets(endpoint, nextPageLink, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(r -> extractAndFetchDeletedSecrets(r, context));
    }

    private Publisher<DeletedSecret> extractAndFetchDeletedSecrets(PagedResponse<DeletedSecret> page, Context context) {
        return ImplUtils.extractAndFetch(page, context, this::listDeletedSecretsNext);
    }
}
