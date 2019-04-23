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
import java.util.List;
import java.util.Objects;

/**
 * The secretClient provides synchronous methods to manage {@link Secret secrets} in the Azure Key Vault. The client
 * supports creating, retrieving, updating, deleting, purging, backing up, restoring and listing the {@link Secret secrets}. The client
 * also supports listing {@link DeletedSecret deleted secrets} for a soft-delete enabled Azure Key Vault.
 *
 * <p><strong>Samples to construct the client</strong></p>
 * <pre>
 *    SecretClient secretClient = SecretClient.builder()
 *                                .vaultEndpoint("https://myvault.vault.azure.net/")
 *                                .credentials(keyVaultCredentials)
 *                                .build()
 * </pre>
 */
public final class SecretClient extends ServiceClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private String vaultEndpoint;
    private final SecretService service;

    /**
     * Creates a SecretClient that uses {@code pipeline} to service requests
     *
     * @param vaultEndpoint URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    SecretClient(URL vaultEndpoint, HttpPipeline pipeline) {
        super(pipeline);
        Objects.requireNonNull(vaultEndpoint, KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.vaultEndpoint = vaultEndpoint.toString();
        this.service = RestProxy.create(SecretService.class, this);
    }

    /**
     * Creates a builder that can configure options for the SecretClient before creating an instance of it.
     * @return A new builder to create a SecretClient from.
     */
    public static SecretClientBuilder builder() {
        return new SecretClientBuilder();
    }

    /**
     * The set operation adds a secret to the Azure Key Vault. If the named secret already exists, Azure Key Vault creates a new version of that secret.
     * This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@code secret} is required and its fields secret.name and secret.value fields cannot be null. The secret.expires,
     * secret.contentType and secret.notBefore values in {@code secret} are optional. If not specified, no values are set
     * for the fields. The secret.enabled field is set to true by Azure Key Vault, if not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * Secret secret = new Secret("secretName", "secretValue")
     *   .notBefore(LocalDateTime.of(2000,12,24,12,30))
     *   .expires(LocalDateTime.of(2050,1,1,0,0));
     *
     * Secret retSecret = secretClient.(keySecret).value();
     * System.out.println(String.format("Secret is created with name %s and value %s",retSecret.name(), retSecret.value()));
     * </pre>
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties secret.name and secret.value must be non null.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @return A {@link Response} whose {@link Response#value()} contains the created {@link Secret}.
     */
    public Response<Secret> setSecret(Secret secret) {
        Objects.requireNonNull(secret, "The Secret input parameter cannot be null.");
        SecretRequestParameters parameters = new SecretRequestParameters()
            .value(secret.value())
            .tags(secret.tags())
            .contentType(secret.contentType())
            .secretAttributes(new SecretRequestAttributes(secret));

        return service.setSecret(vaultEndpoint, secret.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * The set operation adds a secret to the Azure Key Vault. If the named secret already exists, Azure Key Vault creates a new version of that secret.
     * This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * Secret secret = secretClient.setSecret("secretName", "secretValue").value();
     * System.out.println(String.format("Secret is created with name %s and value %s",secret.name(), secret.value()));
     * </pre>
     *
     * @param name The name of the secret. It is required and cannot be null.
     * @param value The value of the secret. It is required and cannot be null.
     * @return A {@link Response} whose {@link Response#value()} contains the created {@link Secret}.
     */
    public Response<Secret> setSecret(String name, String value) {
        SecretRequestParameters parameters = new SecretRequestParameters()
                                            .value(value);
        return service.setSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Get the latest version of the specified secret from the key vault. The get operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the {@code secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
     * Secret secretWithVersion = secretClient.getSecret("secretName",secretVersion).value();
     * System.out.println(String.format("Secret is returned with name %s and value %s",secretWithVersion.name(), secretWithVersion.value()));
     * </pre>
     *
     * @param name The name of the secret, cannot be null.
     * @param version The version of the secret to retrieve. If this is an empty String or null, this call is equivalent to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @return A {@link Response} whose {@link Response#value()} contains the requested {@link Secret}.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with {@code name} and {@code version} doesn't exist in the key vault.
     */
    public Response<Secret> getSecret(String name, String version) {
        return version == null ? service.getSecret(vaultEndpoint, name, "", API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block()
            : service.getSecret(vaultEndpoint, name, version, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();

    }

    /**
     * Get the secret which represents {@link SecretAttributes secretAttributes} from the key vault. Returns the latest version of the secret,
     * if {@code secretAttributes.version} is not set. The get operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the {@code secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * //Pass the secretAttributes of the secret to fetch.
     * Secret secret = secretClient.getSecret(secretAttributes).value();
     * System.out.println(String.format("Secret is returned with name %s and value %s",secret.name(), secret.value()));
     * </pre>
     *
     * @param secretAttributes the {@link SecretAttributes} attributes of the secret being requested.
     * @return A {@link Response} whose {@link Response#value()} contains the requested {@link Secret}.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with {@code secretAttributes.name} and {@code secretAttributes.version} doesn't exist in the key vault.
     */
    public Response<Secret> getSecret(SecretAttributes secretAttributes) {
        Objects.requireNonNull(secretAttributes, "The Secret attributes parameter cannot be null.");
        return secretAttributes.version() == null ? service.getSecret(vaultEndpoint, secretAttributes.name(), "", API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block()
            : service.getSecret(vaultEndpoint, secretAttributes.name(), secretAttributes.version(), API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Get the latest version of the specified secret from the key vault. The get operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the {@code secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * Secret secret = secretClient.getSecret("secretName").value();
     * System.out.println(String.format("Secret is returned with name %s and value %s",secret.name(), secret.value()));
     * </pre>
     *
     * @param name The name of the secret.
     * @return A {@link Response} whose {@link Response#value()} contains the requested {@link Secret}.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with {@code name} doesn't exist in the key vault.
     */
    public Response<Secret> getSecret(String name) {
        return getSecret(name, "");
    }

    /**
     * Updates the attributes associated with a specified secret in the key vault. The update operation changes specified
     * attributes of an existing stored secret and attributes that are not specified in the request are left unchanged.
     * The value of a secret itself cannot be changed. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@code secretAttributes} is required and its fields secretAttributes.name and secretAttributes.version cannot be null.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * Secret secret = secretClient.getSecret("secretName").value();
     * secret.notBefore(LocalDateTime.of(2030,01,01,12,00));
     * SecretAttributes updatedSecretAttributes = secretClient.updateSecretAttributes(secret).value();
     * Secret updatedSecret = secretClient.getSecret(updatedSecretAttributes.name()).value();
     * </pre>
     *
     * @param secretAttributes the {@link SecretAttributes} object with updated properties.
     * @throws NullPointerException if {@code secretAttributes} is {@code null}.
     * @return A {@link Response} whose {@link Response#value()} contains the updated {@link SecretAttributes}.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with secretAttributes.name and secretAttributes.version doesn't exist in the key vault.
     */
    public Response<SecretAttributes> updateSecretAttributes(SecretAttributes secretAttributes) {
        Objects.requireNonNull(secretAttributes, "The secretAttributes input parameter cannot be null.");
        SecretRequestParameters parameters = new SecretRequestParameters()
                .tags(secretAttributes.tags())
                .contentType(secretAttributes.contentType())
                .secretAttributes(new SecretRequestAttributes(secretAttributes));

        return service.updateSecret(vaultEndpoint, secretAttributes.name(), secretAttributes.version(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Deletes a secret from the key vault. The delete operation applies to any secret stored in Azure Key Vault but
     * it cannot be applied to an individual version of a secret. This operation requires the {@code secrets/delete} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * DeletedSecret deletedSecret = secretClient.deleteSecret("secretName").value();
     * </pre>
     *
     * @param name The name of the secret to be deleted.
     * @return A {@link Response} whose {@link Response#value()} contains the deleted {@link DeletedSecret}.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with {@code name} doesn't exist in the key vault.
     */
    public Response<DeletedSecret> deleteSecret(String name) {
        return service.deleteSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * The get deleted secret operation returns the secrets that have been deleted for a vault enabled for soft-delete.
     * This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled key vault.
     * DeletedSecret deletedSecret = secretClient.getDeletedSecret("secretName").value();
     * </pre>
     *
     * @param name The name of the deleted secret.
     * @return A {@link Response} whose {@link Response#value()} contains the deleted {@link DeletedSecret}.
     * @throws com.azure.common.exception.ServiceRequestException when a deleted secret with {@code name} doesn't exist in the key vault.
     */
    public Response<DeletedSecret> getDeletedSecret(String name) {
        return service.getDeletedSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * The purge deleted secret operation removes the secret permanently, without the possibility of recovery.
     * This operation can only be enabled on a soft-delete enabled vault. This operation requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled key vault.
     * secretClient.purgeDeletedSecret("deletedSecretName");
     * </pre>
     *
     * @param name The name of the secret.
     * @return A {@link VoidResponse}.
     * @throws com.azure.common.exception.ServiceRequestException when a deleted secret with {@code name} doesn't exist in the key vault.
     */
    public VoidResponse purgeDeletedSecret(String name) {
        return service.purgeDeletedSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Recovers the deleted secret in the key vault to its latest version and can only be performed on a soft-delete enabled vault.
     * This operation requires the {@code secrets/recover} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled key vault.
     * Secret recoveredSecret =  secretClient.recoverDeletedSecret("deletedSecretName").value();
     * </pre>
     *
     * @param name The name of the deleted secret to be recovered.
     * @return A {@link Response} whose {@link Response#value()} contains the recovered {@link Secret}.
     * @throws com.azure.common.exception.ServiceRequestException when a deleted secret with {@code name} doesn't exist in the key vault.
     */
    public Response<Secret> recoverDeletedSecret(String name) {
        return service.recoverDeletedSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Requests a backup of the specified secret be downloaded to the client. All versions of the secret will be downloaded.
     * This operation requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * byte[] secretBackup = secretClient.backupSecret("secretName").value();
     * </pre>
     *
     * @param name The name of the secret.
     * @return A {@link Response} whose {@link Response#value()} contains the backed up secret blob.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with {@code name} doesn't exist in the key vault.
     */
    public Response<byte[]> backupSecret(String name) {
        return service.backupSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .flatMap(base64URLResponse ->  Mono.just(new SimpleResponse<byte[]>(base64URLResponse.request(),
                            base64URLResponse.statusCode(), base64URLResponse.headers(), base64URLResponse.value().value()))).block();
    }

    /**
     * Restores a backed up secret, and all its versions, to a vault.
     * This operation requires the {@code secrets/restore} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <pre>
     * //Pass the secret backup byte array of the secret to be restored.
     * Secret restoredSecret = secretClient.restoreSecret(secretBackupByteArray).value();
     * </pre>
     *
     * @param backup The backup blob associated with the secret.
     * @return A {@link Response} whose {@link Response#value()} contains the restored {@link Secret}.
     * @throws com.azure.common.exception.ServiceRequestException when the {@code backup} is corrupted.
     */
    public Response<Secret> restoreSecret(byte[] backup) {
        SecretRestoreRequestParameters parameters = new SecretRestoreRequestParameters().secretBackup(backup);
        return service.restoreSecret(vaultEndpoint, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * List the secrets in the key vault. The list Secrets operation is applicable to the entire vault. The individual secret response
     * in the list is represented by {@link SecretAttributes} as only the base secret identifier and its attributes are
     * provided in the response. The secret values and individual secret versions are not listed in the response. This operation requires the {@code secrets/list} permission.
     *
     * <p>It is possible to get full Secrets with values from this information. Loop over the {@link SecretAttributes secretAttributes} and
     * call {@link SecretClient#getSecret(SecretAttributes)} . This will return the {@link Secret} secrets with values included of its latest version.</p>
     * <pre>
     * List-&lt;Secret-&gt; secrets = new ArrayList();
     * secretClient.listSecrets().stream().map(secretClient::getSecret).forEach(secretResponse -&gt;
     *   secrets.add(secretResponse.value()));
     * </pre>
     *
     * @return A {@link List} containing {@link SecretAttributes} of all the secrets in the vault. The {@link SecretAttributes} contains all the information about the secret, except its value.
     */
    public List<SecretAttributes> listSecrets() {
        return service.getSecrets(vaultEndpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
            .flatMapMany(this::extractAndFetchSecrets).collectList().block();
    }

    /**
     * Lists {@link DeletedSecret secrets} of the key vault. The get deleted secrets operation returns the secrets that
     * have been deleted for a vault enabled for soft-delete. This operation requires the {@code secrets/list} permission.
     *
     * @return A {@link List} containing all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    public List<DeletedSecret> listDeletedSecrets() {
        return service.getDeletedSecrets(vaultEndpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
            .flatMapMany(this::extractAndFetchDeletedSecrets).collectList().block();
    }

    /**
     * List all versions of the specified secret. The individual secret response in the list is represented by {@link SecretAttributes}
     * as only the base secret identifier and its attributes are provided in the response. The secret values are
     * not provided in the response. This operation requires the {@code secrets/list} permission.
     *
     * <p>It is possible to get full Secrets with values for each version from this information. Loop over the {@link SecretAttributes secretAttributes} and
     * call {@link SecretClient#getSecret(SecretAttributes)} . This will return the {@link Secret} secrets with values included of the specified versions.</p>
     * <pre>
     * List-&lt;Secret-&gt; secretVersions = new ArrayList();
     * secretClient.listSecretVersions("secretName").stream().map(secretClient::getSecret).forEach(secretResponse -&gt;
     *   secretVersions.add(secretResponse.value()));
     * </pre>
     *
     * @param name The name of the secret.
     * @return A {@link List} containing {@link SecretAttributes} of all the versions of the specified secret in the vault. List is empty if secret with {@code name} does not exist in key vault
     */
    public List<SecretAttributes> listSecretVersions(String name) {
        return  service.getSecretVersions(vaultEndpoint, name, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
            .flatMapMany(this::extractAndFetchSecrets).collectList().block();
    }

    /**
     * Gets attributes of all the secrets given by the {@code nextPageLink} that was retrieved from a call to
     * {@link SecretClient#listSecrets()}.
     *
     * @param nextPageLink The {@link SecretAttributesPage#nextLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link SecretAttributes} from the next page of results.
     */
    private Flux<SecretAttributes> listSecretsNext(String nextPageLink) {
        return service.getSecrets(vaultEndpoint, nextPageLink, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchSecrets);
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
     * {@link SecretClient#listDeletedSecrets()}.
     *
     * @param nextPageLink The {@link com.azure.keyvault.implementation.DeletedSecretPage#nextLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link SecretAttributes} from the next page of results.
     */
    private Flux<DeletedSecret> listDeletedSecretsNext(String nextPageLink) {
        return service.getDeletedSecrets(vaultEndpoint, nextPageLink, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchDeletedSecrets);
    }

    private Publisher<DeletedSecret> extractAndFetchDeletedSecrets(PagedResponse<DeletedSecret> page) {
        String nextPageLink = page.nextLink();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(listDeletedSecretsNext(nextPageLink));
    }
}
