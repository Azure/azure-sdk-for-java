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
import com.azure.keyvault.models.DeletedSecret;
import com.azure.keyvault.models.Secret;
import com.azure.keyvault.models.SecretAttributes;
import org.apache.commons.lang3.Validate;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public final class SecretClient extends ServiceClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";


    private String vaultEndpoint;
    private final SecretService service;

    /**
     * Creates a SecretClient that uses {@code pipeline} to
     * service requests
     *
     * @param vaultEndpoint URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    SecretClient(URL vaultEndpoint, HttpPipeline pipeline) {
        super(pipeline);
        this.vaultEndpoint = vaultEndpoint.toString();
        this.service = RestProxy.create(SecretService.class, this);
    }

    /**
     * Creates a builder that can configure options for the SecretClient before creating an instance of it.
     * @return A new Builder to create a SecretClient from.
     */
    public static SecretClientBuilder builder() {
        return new SecretClientBuilder();
    }

    /**
     * The set operation adds a secret to the Azure Key Vault. If the named secret already exists, Azure Key Vault creates a new version of that secret.
     * This operation requires the secrets/set permission.
     *
     * <p> The {@code secret} is required along with its non-null fields secret.name and secret.value. The secret.expires,
     * secret.contentType and secret.notBefore values in {@code secret} are optional. If not specified, no values are set
     * for the fields. The secret.enabled field is set to true by Azure Key Vault, if not specified.</p>
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties secret.name and secret.value must be non null.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @return A {@link Response} whose {@link Response#value()} contains the created {@link Secret}.
     */
    public Response<Secret> setSecret(Secret secret) {
        Objects.requireNonNull(secret, "The Secret input parameter cannot be null.");
        Objects.requireNonNull(secret.name(), "The Secret name cannot be null.");
        Objects.requireNonNull(secret.value(), "The Secret value cannot be null.");

        SecretRequestParameters parameters = new SecretRequestParameters()
                                            .value(secret.value())
                                            .tags(secret.tags())
                                            .contentType(secret.contentType())
                                            .secretAttributes(new SecretRequestAttributes(secret));

        return service.setSecret(vaultEndpoint, secret.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * The set operation adds a secret to the Azure Key Vault. If the named secret already exists, Azure Key Vault creates a new version of that secret.
     * This operation requires the secrets/set permission.
     *
     * @param name The name of the secret. It is required and cannot be null.
     * @param value The value of the secret. It is required and cannot be null.
     * @throws NullPointerException if {@code name} or {@code value} parameter is {@code null}.
     * @return A {@link Response} whose {@link Response#value()} contains the created {@link Secret}.
     */
    public Response<Secret> setSecret(String name, String value) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        Objects.requireNonNull(value, "The Secret value cannot be null.");

        SecretRequestParameters parameters = new SecretRequestParameters()
                                            .value(value);
        return service.setSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Get the latest version of the specified secret from the key vault. The get operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the secrets/get permission.
     *
     * @param name The name of the secret, cannot be null.
     * @param version The version of the secret to retrieve. If this is an empty String or null, this call is equivalent to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @throws NullPointerException if {@code name} or {@code version} parameter is {@code null}.
     * @return A {@link Response} whose {@link Response#value()} contains the requested {@link Secret}.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with {@code name} and {@code version} doesn't exist in the key vault.
     */
    public Response<Secret> getSecret(String name, String version) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        if (version == null) {
            return service.getSecret(vaultEndpoint, name, "", API_VERSION, ACCEPT_LANGUAGE, getHost(), CONTENT_TYPE_HEADER_VALUE).block();
        } else {
            return service.getSecret(vaultEndpoint, name, version, API_VERSION, ACCEPT_LANGUAGE, getHost(), CONTENT_TYPE_HEADER_VALUE).block();
        }
    }

    private String getHost(){
        try{
            return (new URL(vaultEndpoint).getHost());
        } catch (MalformedURLException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the latest version of the specified secret from the key vault. The get operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the secrets/get permission.
     *
     * @param name The name of the secret.
     * @throws NullPointerException if {@code name} parameter is {@code null}.
     * @return A {@link Response} whose {@link Response#value()} contains the requested {@link Secret}.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with {@code name} doesn't exist in the key vault.
     */
    public Response<Secret> getSecret(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return getSecret(name, "");
    }

    /**
     * Updates the attributes associated with a specified secret in the key vault. The update operation changes specified
     * attributes of an existing stored secret and attributes that are not specified in the request are left unchanged.
     * The value of a secret itself cannot be changed. This operation requires the secrets/set permission.
     *
     * <p> The {@code secretAttributes} is required along with its non-null fields secretAttributes.name and secretAttributes.version. </p>
     *
     * @param secretAttributes the {@link SecretAttributes} object with updated properties.
     * @throws NullPointerException if {@code secretAttributes} is {@code null}.
     * @return A {@link Response} whose {@link Response#value()} contains the updated {@link SecretAttributes}.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with secretAttributes.name and secretAttributes.version doesn't exist in the key vault.
     */
    public Response<SecretAttributes> updateSecretAttributes(SecretAttributes secretAttributes) {
        Objects.requireNonNull(secretAttributes, "The secretAttributes input parameter cannot be null.");
        Objects.requireNonNull(secretAttributes.name(), "The Secret name cannot be null.");
        Objects.requireNonNull(secretAttributes.version(), "The Secret version cannot be null.");

        SecretRequestParameters parameters = new SecretRequestParameters()
                .tags(secretAttributes.tags())
                .contentType(secretAttributes.contentType())
                .secretAttributes(new SecretRequestAttributes(secretAttributes));

        return service.updateSecret(vaultEndpoint, secretAttributes.name(), secretAttributes.version(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Deletes a secret from the key vault. The delete operation applies to any secret stored in Azure Key Vault but
     * it cannot be applied to an individual version of a secret. This operation requires the secrets/delete permission.
     *
     * @param name The name of the secret to be deleted.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return A {@link Response} whose {@link Response#value()} contains the deleted {@link DeletedSecret}.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with {@code name} doesn't exist in the key vault.
     */
    public Response<DeletedSecret> deleteSecret(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.deleteSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * The get deleted secret operation returns the secrets that have been deleted for a vault enabled for soft-delete.
     * This operation requires the secrets/list permission.
     *
     * @param name The name of the deleted secret.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return A {@link Response} whose {@link Response#value()} contains the deleted {@link DeletedSecret}.
     * @throws com.azure.common.exception.ServiceRequestException when a deleted secret with {@code name} doesn't exist in the key vault.
     */
    public Response<DeletedSecret> getDeletedSecret(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.getDeletedSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * The purge deleted secret operation removes the secret permanently, without the possibility of recovery.
     * This operation can only be enabled on a soft-delete enabled vault. This operation requires the secrets/purge permission.
     *
     * @param name The name of the secret.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return A {@link VoidResponse}.
     * @throws com.azure.common.exception.ServiceRequestException when a deleted secret with {@code name} doesn't exist in the key vault.
     */
    public VoidResponse purgeDeletedSecret(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.purgeDeletedSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Recovers the deleted secret in the key vault to its latest version and can only be performed on a soft-delete enabled vault.
     * This operation requires the secrets/recover permission.
     *
     * @param name The name of the deleted secret to be recovered.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return A {@link Response} whose {@link Response#value()} contains the recovered {@link Secret}.
     * @throws com.azure.common.exception.ServiceRequestException when a deleted secret with {@code name} doesn't exist in the key vault.
     */
    public Response<Secret> recoverDeletedSecret(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.recoverDeletedSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * Requests a backup of the specified secret be downloaded to the client. All versions of the secret will be downloaded.
     * This operation requires the secrets/backup permission.
     *
     * @param name The name of the secret.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return A {@link Response} whose {@link Response#value()} contains the backed up secret blob.
     * @throws com.azure.common.exception.ServiceRequestException when a secret with {@code name} doesn't exist in the key vault.
     */
    public Response<byte[]> backupSecret(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.backupSecret(vaultEndpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .flatMap( base64URLResponse ->  Mono.just(new SimpleResponse<byte[]>(base64URLResponse.request(),
                            base64URLResponse.statusCode(), base64URLResponse.headers(), base64URLResponse.value().value()))).block();
    }

    /**
     * Restores a backed up secret, and all its versions, to a vault.
     * This operation requires the secrets/restore permission.
     *
     * @param backup The backup blob associated with the secret.
     * @throws NullPointerException if {@code SecretBackup} is {@code null}.
     * @return A {@link Response} whose {@link Response#value()} contains the restored {@link Secret}.
     * @throws com.azure.common.exception.ServiceRequestException when the {@code backup} is corrupted.
     */
    public Response<Secret> restoreSecret(byte[] backup) {
        Objects.requireNonNull(backup, "The Secret backup parameter cannot be null.");
        SecretRestoreRequestParameters parameters = new SecretRestoreRequestParameters().secretBackup(backup);
        return service.restoreSecret(vaultEndpoint, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE).block();
    }

    /**
     * List the secrets in the key vault. The list Secrets operation is applicable to the entire vault. The individual secret response
     * in the list is represented by {@link SecretAttributes} as only the base secret identifier and its attributes are
     * provided in the response. The secret values and individual secret versions are not listed in the response. This operation requires the secrets/list permission.
     *
     * <p> It is possible to get full Secrets with values from this information. Loop over the {@link SecretAttributes secretAttributes} and
     * call {@link SecretClient#getSecret(String secretName)} . This will return the {@link Secret} secrets with values included of its latest version. </p>
     *
     * <pre>
     * List<SecretAttributes> secretAttributes = secretClient.listSecrets();
     * List<Secret> secrets = new ArrayList();
     * for (SecretAttributes secretAttr : secretAttributes) {
     *     secretVersions.add(secretClient.getSecret(secretAttr.name());
     * }
     * </pre>
     *
     * @return A {@link List} containing {@link SecretAttributes} of all the secrets in the vault. The {@link SecretAttributes} contains all the information about the secret, except its value.
     */
    public List<SecretAttributes> listSecrets() {
        Mono<PagedResponse<SecretAttributes>> result = service.getSecrets(vaultEndpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
        return result.flatMapMany(this::extractAndFetchSecrets).collectList().block();
    }

    /**
     * Lists {@link DeletedSecret secrets} of the key vault. The get deleted secrets operation returns the secrets that
     * have been deleted for a vault enabled for soft-delete. This operation requires the secrets/list permission.
     *
     * @return A {@link List} containing all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    public List<DeletedSecret> listDeletedSecrets() {
        Mono<PagedResponse<DeletedSecret>> result = service.getDeletedSecrets(vaultEndpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
        return result.flatMapMany(this::extractAndFetchSecrets).collectList().block();
    }

    /**
     * List all versions of the specified secret. The individual secret response in the list is represented by {@link SecretAttributes}
     * as only the base secret identifier and its attributes are provided in the response. The secret values are
     * not provided in the response. This operation requires the secrets/list permission.
     *
     * <p> It is possible to get full Secrets with values for each version from this information. Loop over the {@link SecretAttributes secretAttributes} and
     * call {@link SecretClient#getSecret(String secretName, String secretVersion)} . This will return the {@link Secret} secrets with values included of the specified versions. </p>
     *
     * <pre>
     * List<SecretAttributes> secretAttributes = secretClient.listSecretVersions("secretName");
     * List<Secret> secretVersions = new ArrayList();
     * for (SecretAttributes secretAttr : secretAttributes) {
     *     secretVersions.add(secretClient.getSecret(secretAttr.name(), secretAttr.version());
     * }
     * </pre>
     *
     * @param name The name of the secret.
     * @throws NullPointerException thrown if name parameter is null.
     * @throws IllegalArgumentException thrown if name parameter is empty.
     * @return A {@link List} containing {@link SecretAttributes} of all the versions of the specified secret in the vault. List is empty if secret with {@code name} does not exist in key vault
     */
    public List<SecretAttributes> listSecretVersions(String name) {
        //TODO: replace this with ImplUtils string is empty or null check, once Issue: Azure/azure-sdk-for-java#3373 is completed and merged.
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        Validate.isTrue(!name.isEmpty(), "The Secret name cannot be empty");
        Mono<PagedResponse<SecretAttributes>> result = service.getSecretVersions(vaultEndpoint, name, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
        return result.flatMapMany(this::extractAndFetchSecrets).collectList().block();
    }

    /**
     * List secrets in a specified key vault. The list secrets operation is applicable to the entire vault.
     * The individual secret response in the list is represented by {@link SecretAttributes} as only the base secret identifier
     * and its attributes are provided in the response. The secret values and individual secret versions are not
     * provided in the response. This operation requires the secrets/list permission.
     *
     * <p> It is possible to get full Secrets with values from this information. Loop over the {@link SecretAttributes secretAttributes} and
     * call {@link SecretClient#getSecret(String secretName)} . This will return the {@link Secret} secrets with values included of its latest version. </p>
     *
     * <pre>
     * List<SecretAttributes> secretAttributes = secretClient.listSecrets(25);
     * List<Secret> secrets = new ArrayList();
     * for (SecretAttributes secretAttr : secretAttributes) {
     *     secretVersions.add(secretClient.getSecret(secretAttr.name());
     * }
     * </pre>
     *
     * @param maxPageResults Maximum number of results to return in a page.
     * @throws IllegalArgumentException thrown if maxPageResults parameter is 0 or less than 0.
     * @return A {@link List} containing {@link SecretAttributes} of all the secrets in the key vault.
     */
    public List<SecretAttributes> listSecrets(int maxPageResults) {
        Validate.isTrue(maxPageResults > 0, "The maximum page results parameter needs to be greater than 0.");
        Mono<PagedResponse<SecretAttributes>> result = service.getSecrets(vaultEndpoint, maxPageResults, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
        return result.flatMapMany(this::extractAndFetchSecrets).collectList().block();
    }

    /**
     * Lists {@link DeletedSecret} secrets of the key vault. The list deleted secrets operation returns the secrets that
     * have been deleted for a vault enabled for soft-delete. This operation requires the secrets/list permission.
     *
     * @param maxPageResults Maximum number of results to return in a page.
     * @throws IllegalArgumentException thrown if maxPageResults parameter is 0 or less than 0.
     * @return A {@link List} containing all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    public List<DeletedSecret> listDeletedSecrets(int maxPageResults) {
        Validate.isTrue(maxPageResults > 0, "The maximum page results parameter needs to be greater than 0.");
        Mono<PagedResponse<DeletedSecret>> result = service.getDeletedSecrets(vaultEndpoint, maxPageResults, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
        return result.flatMapMany(this::extractAndFetchSecrets).collectList().block();
    }

    /**
     * List all versions of the specified secret. The individual secret response in the list is represented by {@link SecretAttributes}
     * as only the base secret identifier and its attributes are provided in the response. The secret values are
     * not provided in the response. This operations requires the secrets/list permission.
     *
     * <p> It is possible to get full Secrets with values for each version from this information. Loop over the {@link SecretAttributes secretAttributes} and
     * call {@link SecretClient#getSecret(String secretName, String secretVersion)} . This will return the {@link Secret} secrets with values included of the specified versions. </p>
     *
     * <pre>
     * List<SecretAttributes> secretAttributes = secretClient.listSecretVersions("secretName", 25);
     * List<Secret> secretVersions = new ArrayList();
     * for (SecretAttributes secretAttr : secretAttributes) {
     *     secretVersions.add(secretClient.getSecret(secretAttr.name(), secretAttr.version());
     * }
     * </pre>
     *
     * @param name The name of the secret.
     * @param maxPageResults Maximum number of results to return in a page.
     * @throws IllegalArgumentException thrown if {@code name} or {@code maxPageResults} is 0 or less than 0.
     * @return A {@link List} containing {@link SecretAttributes} of all the versions of the secret in the key vault. List is empty if secret with {@code name} does not exist in key vault
     */
    public List<SecretAttributes> listSecretVersions(String name, int maxPageResults) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        //TODO: replace this with ImplUtils string is empty or null check, once Issue: Azure/azure-sdk-for-java#3373 is completed and merged.
        Validate.isTrue(!name.isEmpty(), "The Secret name cannot be empty");
        Objects.requireNonNull(maxPageResults, "The maximum page results parameter cannot be null.");
        Validate.isTrue(maxPageResults > 0, "The maximum page results parameter needs to be greater than 0.");
        Mono<PagedResponse<SecretAttributes>> result = service.getSecretVersions(vaultEndpoint, name, maxPageResults, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
        return result.flatMapMany(this::extractAndFetchSecrets).collectList().block();
    }

    /**
     * Gets {@link T type secrets} given by the {@code nextPageLink} that was retrieved from a call to
     * {@link SecretClient#listSecrets()} or {@link SecretClient#listSecrets(int)} or {@link SecretClient#listDeletedSecrets()}
     * or {@link SecretClient#listDeletedSecrets(int)}.
     *
     * @param nextPageLink The {@link com.azure.keyvault.implementation.SecretsPage#nextLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link T type secrets} from the next page of results.
     */
    private <T> Flux<T> listSecretsNext(String nextPageLink) {
        Mono<PagedResponse<T>> result = service.getSecrets(vaultEndpoint, nextPageLink, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
        return result.flatMapMany(this::extractAndFetchSecrets);
    }

    private <T> Publisher<T> extractAndFetchSecrets(PagedResponse<T> page) {
        String nextPageLink = page.nextLink();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(listSecretsNext(nextPageLink));
    }
}
