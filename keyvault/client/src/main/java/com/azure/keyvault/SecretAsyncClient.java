// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault;

import com.azure.common.ServiceClient;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.rest.PagedResponse;
import com.azure.common.http.rest.Response;
import com.azure.common.http.rest.SimpleResponse;
import com.azure.common.http.rest.VoidResponse;
import com.azure.common.implementation.RestProxy;
import com.azure.keyvault.implementation.SecretAttributesPage;
import com.azure.keyvault.models.DeletedSecret;
import com.azure.keyvault.models.Secret;
import com.azure.keyvault.models.SecretAttributes;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Objects;
import java.util.function.Function;


/**
 * The SecretAsyncClient provides asynchronous methods to manage {@link Secret secrets} in the Azure Key Vault. The client
 * supports creating, retrieving, updating, deleting, purging, backing up, restoring and listing the {@link Secret secrets}. The client
 * also supports listing {@link DeletedSecret deleted secrets} for a soft-delete enabled Azure Key Vault.
 *
 * <p><strong>Samples to construct the client</strong></p>
 * <pre>
 * SecretAsyncClient.builder()
 *   .vaultEndpoint("https://myvault.vault.azure.net/")
 *   .credentials(keyVaultCredentials)
 *  .build()
 * </pre>
 */
public final class SecretAsyncClient extends ServiceClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private String vaultEndpoint;
    private final SecretService service;

    /**
     * Creates a SecretAsyncClient that uses {@code pipeline} to service requests
     *
     * @param vaultEndpoint URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    SecretAsyncClient(URL vaultEndpoint, HttpPipeline pipeline) {
        super(pipeline);
        Objects.requireNonNull(vaultEndpoint, KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.vaultEndpoint = vaultEndpoint.toString();
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
     * The set operation adds a secret to the Azure Key Vault. If the named secret already exists, Azure Key Vault creates
     * a new version of that secret. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@code secret} is required and its fields secret.name and secret.value cannot be null. The secret.expires,
     * secret.contentType and secret.notBefore values in {@code secret} are optional. If not specified, no values are set
     * for the fields. The secret.enabled field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * Secret secret = new Secret("secretName", "secretValue")
     *   .notBefore(LocalDateTime.of(2000,12,24,12,30))
     *   .expires(LocalDateTime.of(2050,1,1,0,0));
     *
     * secretAsyncClient.setSecret(secret).subscribe(secretResponse -&gt;
     *   System.out.println(String.format("Secret is created with name %s and value %s",secretResponse.value().name(), secretResponse.value().value())));
     * </pre>
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties secret.name and secret.value must be non null.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value()} contains the created {@link Secret}.
     */
    public Mono<Response<Secret>> setSecret(Secret secret) {
        Objects.requireNonNull(secret, "The Secret input parameter cannot be null.");
        SecretRequestParameters parameters = new SecretRequestParameters()
            .value(secret.value())
            .tags(secret.tags())
            .contentType(secret.contentType())
            .secretAttributes(new SecretRequestAttributes(secret));

        return service.setSecret(vaultEndpoint, secret.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * The set operation adds a secret to the Azure Key Vault. If the named secret already exists, Azure Key Vault creates a new version of that secret.
     * This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * secretAsyncClient.setSecret("secretName", "secretValue").subscribe(secretResponse -&gt;
     *   System.out.println(String.format("Secret is created with name %s and value %s",secretResponse.value().name(), secretResponse.value().value())));
     * </pre>
     *
     * @param name The name of the secret. It is required and cannot be null.
     * @param value The value of the secret. It is required and cannot be null.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value()} contains the created {@link Secret}.
     */
    public Mono<Response<Secret>> setSecret(String name, String value) {
        SecretRequestParameters parameters = new SecretRequestParameters().value(value);
        return service.setSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * Get the specified secret with specified version from the key vault. The get operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the {@code secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
     * secretAsyncClient.getSecret("secretName",secretVersion).subscribe(secretResponse -&gt;
     *   System.out.println(String.format("Secret with name %s , value %s and version %s",secretResponse.value().name(),
     *   secretResponse.value().value(), secretResponse.value().version())));
     * </pre>
     *
     * @param name The name of the secret, cannot be null
     * @param version The version of the secret to retrieve. If this is an empty String or null, this call is equivalent to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value()} contains the requested {@link Secret}.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with {@code name} and {@code version} doesn't exist in the key vault.
     */
    public Mono<Response<Secret>> getSecret(String name, String version) {
        return version == null ?  service.getSecret(vaultEndpoint, name, "", API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
            : service.getSecret(vaultEndpoint, name, version, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * Get the latest version of the specified secret from the key vault. The get operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the {@code secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * secretAsyncClient.getSecret("secretName").subscribe(secretResponse -&gt;
     *   System.out.println(String.format("Secret with name %s , value %s",secretResponse.value().name(),
     *   secretResponse.value().value())));
     * </pre>
     *
     * @param name The name of the secret.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value()} contains the requested {@link Secret}.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with {@code name} doesn't exist in the key vault.
     */
    public Mono<Response<Secret>> getSecret(String name) {
        return getSecret(name, "");
    }

    /**
     * Updates the attributes associated with a specified secret in the key vault. The update operation changes specified
     * attributes of an existing stored secret and attributes that are not specified in the request are left unchanged.
     * The value of a secret itself cannot be changed. This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * secretAsyncClient.getSecret("secretName")
     *   .subscribe(secretResponse -&gt; {
     *     Secret secret = secretResponse.value();
     *     //Update the not before time of the secret.
     *     secret.notBefore(LocalDateTime.of(2030,01,01,12,00));
     *     secretAsyncClient.updateSecretAttributes(secret)
     *       .subscribe(secretAttributesResponse -&gt; {
     *         System.out.println(String.format("Secret's updated not before time %s", secretAttributesResponse.value().notBefore().toString()));
     *       });
     *   });
     * </pre>
     * <p>The {@code secretAttributes} is required and its fields secretAttributes.name and secretAttributes.version cannot be null.</p>
     *
     * @param secretAttributes the {@link SecretAttributes} object with updated properties.
     * @throws NullPointerException if {@code secretAttributes} is {@code null}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value()} contains the updated {@link SecretAttributes}.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with secretAttributes.name and secretAttributes.version doesn't exist in the key vault.
     */
    public Mono<Response<SecretAttributes>> updateSecretAttributes(SecretAttributes secretAttributes) {
        Objects.requireNonNull(secretAttributes, "The secretAttributes input parameter cannot be null.");
        SecretRequestParameters parameters = new SecretRequestParameters()
                .tags(secretAttributes.tags())
                .contentType(secretAttributes.contentType())
                .secretAttributes(new SecretRequestAttributes(secretAttributes));

        return service.updateSecret(vaultEndpoint, secretAttributes.name(), secretAttributes.version(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * Deletes a secret from the key vault. The delete operation applies to any secret stored in Azure Key Vault but
     * it cannot be applied to an individual version of a secret. This operation requires the {@code secrets/delete} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * secretAsyncClient.deleteSecret("secretName").subscribe(deletedSecretResponse -&gt;
     *   System.out.println(String.format("Deleted Secret's Recovery Id %s", deletedSecretResponse.value().recoveryId())));
     * </pre>
     *
     * @param name The name of the secret to be deleted.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value()} contains the deleted {@link DeletedSecret}.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with {@code name} doesn't exist in the key vault.
     */
    public Mono<Response<DeletedSecret>> deleteSecret(String name) {
        return service.deleteSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * The get deleted secret operation returns the secrets that have been deleted for a vault enabled for soft-delete.
     * This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled vault.
     * secretAsyncClient.getDeletedSecret("secretName").subscribe(deletedSecretResponse -&gt;
     *   System.out.println(String.format("Deleted Secret with recovery Id %s", deletedSecretResponse.value().recoveryId())));
     * </pre>
     *
     * @param name The name of the deleted secret.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value()} contains the deleted {@link DeletedSecret}.
     * @throws com.azure.common.exception.ServiceRequestException when a deleted secret with {@code name} doesn't exist in the key vault.
     */
    public Mono<Response<DeletedSecret>> getDeletedSecret(String name) {
        return service.getDeletedSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * The purge deleted secret operation removes the secret permanently, without the possibility of recovery.
     * This operation can only be enabled on a soft-delete enabled vault. This operation requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled vault.
     * secretAsyncClient.purgeDeletedSecret("deletedSecretName").subscribe(purgeResponse -&gt;
     *   System.out.println(String.format("Purge Status response %s", purgeResponse.statusCode())));
     * </pre>
     *
     * @param name The name of the secret.
     * @return A {@link Mono} containing a {@link com.azure.common.http.rest.VoidResponse}.
     * @throws com.azure.common.exception.ServiceRequestException when a deleted secret with {@code name} doesn't exist in the key vault.
     */
    public Mono<VoidResponse> purgeDeletedSecret(String name) {
        return service.purgeDeletedSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * Recovers the deleted secret in the key vault to its latest version and can only be performed on a soft-delete enabled vault.
     * This operation requires the {@code secrets/recover} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled vault.
     * secretAsyncClient.recoverDeletedSecret("deletedSecretName").subscribe(recoveredSecretResponse ->
     *   System.out.println(String.format("Recovered Secret with name %s", recoveredSecretResponse.value().name())));
     * </pre>
     *
     * @param name The name of the deleted secret to be recovered.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value()} contains the recovered {@link Secret}.
     * @throws com.azure.common.exception.ServiceRequestException when a deleted secret with {@code name} doesn't exist in the key vault.
     */
    public Mono<Response<Secret>> recoverDeletedSecret(String name) {
        return service.recoverDeletedSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * Requests a backup of the specified secret be downloaded to the client. All versions of the secret will be downloaded.
     * This operation requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * secretAsyncClient.backupSecret("secretName").subscribe(secretBackupResponse -&gt;
     *   System.out.println(String.format("Secret's Backup Byte array's length %s",secretBackupResponse.value().length)));
     * </pre>
     *
     * @param name The name of the secret.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value()} contains the backed up secret blob.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with {@code name} doesn't exist in the key vault.
     */
    public Mono<Response<byte[]>> backupSecret(String name) {
        return service.backupSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .flatMap(base64URLResponse ->  Mono.just(new SimpleResponse<byte[]>(base64URLResponse.request(),
                base64URLResponse.statusCode(), base64URLResponse.headers(), base64URLResponse.value().value())));
    }

    /**
     * Restores a backed up secret, and all its versions, to a vault. This operation requires the {@code secrets/restore} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * //Pass the Secret Backup Byte array to the restore operation.
     * secretAsyncClient.restoreSecret(secretBackupByteArray).subscribe(secretResponse -&gt;
     *   System.out.println(String.format("Restored Secret with name %s and value %s", secretResponse.value().name(), secretResponse.value().value())));
     * </pre>
     *
     * @param backup The backup blob associated with the secret.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value()} contains the restored {@link Secret}.
     * @throws com.azure.common.exception.ServiceRequestException when the {@code backup} is corrupted.
     */
    public Mono<Response<Secret>> restoreSecret(byte[] backup) {
        SecretRestoreRequestParameters parameters = new SecretRestoreRequestParameters()
                                                .secretBackup(backup);
        return service.restoreSecret(vaultEndpoint, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE);
    }

    /**
     * List secrets in the key vault. The list Secrets operation is applicable to the entire vault. The individual secret response
     * in the flux is represented by {@link SecretAttributes} as only the base secret identifier and its attributes are
     * provided in the response. The secret values and individual secret versions are not listed in the response. This operation requires the {@code secrets/list} permission.
     *
     * <p>It is possible to get full Secrets with values from this information. Convert the {@link Flux} containing {@link SecretAttributes secretAttributes} to
     * {@link Flux} containing {@link Secret secrets} using {@link SecretAsyncClient#getSecret(String secretName)} within {@link Flux#flatMap(Function)}.</p>
     * <pre>
     * Flux<SecretAttributes> secretAttributes = secretAsyncClient.listSecrets();
     * Flux<Secret> secrets = secretAttributes.flatMap(secretAttr -&gt;
     *   Flux.just(secretAsyncClient.getSecret(secretAttr.name())))
     *     .flatMap(secretMonoResponse -&gt;
     *       secretMonoResponse.flatMap(secretResponse -&gt;
     *         Mono.just(secretResponse.value())));
     *
     * secrets.subscribe(secret -> System.out.println((secret.value())));
     * </pre>
     *
     * @return A {@link Flux} containing {@link SecretAttributes} of all the secrets in the vault.
     */
    public Flux<SecretAttributes> listSecrets() {
        return service.getSecrets(vaultEndpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchSecrets);
    }

    /**
     * Lists {@link DeletedSecret secrets} of the key vault. The get deleted secrets operation returns the secrets that
     * have been deleted for a vault enabled for soft-delete. This operation requires the {@code secrets/list} permission.
     *
     * @return A {@link Flux} containing all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    public Flux<DeletedSecret> listDeletedSecrets() {
        return service.getDeletedSecrets(vaultEndpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchDeletedSecrets);
    }

    /**
     * List all versions of the specified secret. The individual secret response in the flux is represented by {@link SecretAttributes}
     * as only the base secret identifier and its attributes are provided in the response. The secret values are
     * not provided in the response. This operation requires the {@code secrets/list} permission.
     *
     * <p>It is possible to get the Secret with value of all the versions from this information. Convert the {@link Flux}
     * containing {@link SecretAttributes secretAttributes} to {@link Flux} containing {@link Secret secrets} using
     * {@link SecretAsyncClient#getSecret(String secretName, String secretVersion)} within {@link Flux#flatMap(Function)}.</p>
     * <pre>
     * Flux<SecretAttributes> secretAttributes = secretAsyncClient.listSecretVersions("secretName");
     * Flux<Secret> secrets = secretAttributes.flatMap(secretAttr -&gt;
     *   Flux.just(secretAsyncClient.getSecret(secretAttr.name(), secretAttr.version())))
     *     .flatMap(secretMonoResponse -&gt;
     *       secretMonoResponse.flatMap(secretResponse -&gt;
     *         Mono.just(secretResponse.value())));
     *
     * secrets.subscribe(secret -> System.out.println((secret.value())));
     * </pre>
     *
     * @param name The name of the secret.
     * @throws IllegalArgumentException thrown if name parameter is empty.
     * @return A {@link Flux} containing {@link SecretAttributes} of all the versions of the specified secret in the vault. Flux is empty if secret with {@code name} does not exist in key vault
     */
    public Flux<SecretAttributes> listSecretVersions(String name) {
        return service.getSecretVersions(vaultEndpoint, name, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchSecrets);
    }

    /**
     * Gets attributes of all the secrets given by the {@code nextPageLink} that was retrieved from a call to
     * {@link SecretAsyncClient#listSecrets()}.
     *
     * @param nextPageLink The {@link SecretAttributesPage#nextLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link SecretAttributes} from the next page of results.
     */
    private Flux<SecretAttributes> listSecretsNext(String nextPageLink) {
        Mono<PagedResponse<SecretAttributes>> result = service.getSecrets(vaultEndpoint, nextPageLink, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
        return result.flatMapMany(this::extractAndFetchSecrets);
    }

    private Publisher<SecretAttributes> extractAndFetchSecrets(PagedResponse<SecretAttributes> page) {
        String nextPageLink = page.nextLink();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(listSecretsNext(nextPageLink));
    }

    /**
     * Gets attributes of all the secrets given by the {@code nextPageLink} that was retrieved from a call to
     * {@link SecretAsyncClient#listDeletedSecrets()}.
     *
     * @param nextPageLink The {@link com.azure.keyvault.implementation.DeletedSecretPage#nextLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link SecretAttributes} from the next page of results.
     */
    private Flux<DeletedSecret> listDeletedSecretsNext(String nextPageLink) {
        Mono<PagedResponse<DeletedSecret>> result = service.getDeletedSecrets(vaultEndpoint, nextPageLink, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
        return result.flatMapMany(this::extractAndFetchDeletedSecrets);
    }

    private Publisher<DeletedSecret> extractAndFetchDeletedSecrets(PagedResponse<DeletedSecret> page) {
        String nextPageLink = page.nextLink();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(listDeletedSecretsNext(nextPageLink));
    }
}
