// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault;

import com.azure.common.ServiceClient;
import com.azure.common.credentials.ServiceClientCredentials;
import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.policy.HttpLogDetailLevel;
import com.azure.common.http.policy.HttpPipelinePolicy;
import com.azure.common.http.policy.RetryPolicy;
import com.azure.common.http.policy.UserAgentPolicy;
import com.azure.common.http.policy.HttpLoggingPolicy;
import com.azure.common.http.rest.RestResponse;
import com.azure.common.http.rest.RestVoidResponse;
import com.azure.common.http.rest.SimpleRestResponse;
import com.azure.common.implementation.RestProxy;
import com.azure.keyvault.implementation.Page;
import com.azure.keyvault.implementation.RestPagedResponseImpl;
import com.azure.keyvault.models.*;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class SecretClient extends ServiceClient {
    static final String SDK_NAME = "Azure-Keyvault";
    static final String SDK_VERSION = "1.0.0-SNAPSHOT";
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;

    private String vaultEndPoint;
    private final SecretService service;

    /**
     * Creates a SecretClient that uses {@code pipeline} to
     * service requests
     *
     * @param vaultEndPoint URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    private SecretClient(String vaultEndPoint, HttpPipeline pipeline) {
        super(pipeline);
        this.vaultEndPoint = vaultEndPoint;
        this.service = RestProxy.create(SecretService.class, this);
    }

    /**
     * Creates a builder that can configure options for the SecretClient before creating an instance of it.
     * @return A new Builder to create a SecretClient from.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Provides configuration options for instances of {@link SecretClient}.
     */
    public static final class Builder {
        private final List<HttpPipelinePolicy> policies;
        private ServiceClientCredentials credentials;
        private String vaultEndPoint;
        private HttpClient httpClient;
        private HttpLogDetailLevel httpLogDetailLevel;
        private RetryPolicy retryPolicy;
        private String userAgent;

        private Builder() {
            userAgent = String.format("Azure-SDK-For-Java/%s (%s)", SDK_NAME, SDK_VERSION);
            retryPolicy = new RetryPolicy();
            httpLogDetailLevel = HttpLogDetailLevel.NONE;
            policies = new ArrayList<>();
        }

        /**
         * Creates a {@link SecretClient} based on options set in the builder.
         *
         * Every time {@code build()} is called, a new instance of {@link SecretClient} is created.
         *
         * @return A SecretClient with the options set from the builder.
         * @throws IllegalStateException If {@link Builder#credentials(ServiceClientCredentials)}
         * has not been set.
         */
        public SecretClient build() {
            if (credentials == null) {
                throw new IllegalStateException(KeyVaultErrorCodeStrings.CredentialsRequired);
            }

            if (vaultEndPoint == null) {
                throw new IllegalStateException(KeyVaultErrorCodeStrings.VaultEndPointRequired);
            }

            // Closest to API goes first, closest to wire goes last.
            final List<HttpPipelinePolicy> policies = new ArrayList<>();

            policies.add(new UserAgentPolicy(userAgent));
            policies.add(new RequestIdPolicy());
            policies.add(retryPolicy);
            policies.add(new KeyvaultCredentialsPolicy(credentials, vaultEndPoint));

            policies.addAll(this.policies);

            policies.add(new HttpLoggingPolicy(httpLogDetailLevel));

            HttpPipeline pipeline = httpClient == null
                    ? new HttpPipeline(policies)
                    : new HttpPipeline(httpClient, policies);

            return new SecretClient(vaultEndPoint, pipeline);
        }

        /**
         * Sets the vault endpoint url to send HTTP requests to.
         *
         * @param vaultEndPoint The vault endpoint url is used as destination on Azure to send requests to.
         * @return The updated Builder object.
         * @throws NullPointerException if {@code vaultEndpoint} is {@code null}.
         */
        public Builder vaultEndPoint(String vaultEndPoint) {
            Objects.requireNonNull(vaultEndPoint);
            this.vaultEndPoint = vaultEndPoint;
            return this;
        }

        /**
         * Sets the credentials to use when authenticating HTTP requests.
         *
         * @param credentials The credentials to use for authenticating HTTP requests.
         * @return The updated Builder object.
         * @throws NullPointerException if {@code credentials} is {@code null}.
         */
        public Builder credentials(ServiceClientCredentials credentials) {
            Objects.requireNonNull(credentials);
            this.credentials = credentials;
            return this;
        }

        /**
         * Sets the logging level for HTTP requests and responses.
         *
         * <p>
         *  logLevel is optional. If not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
         * </p>
         *
         * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
         * @return The updated Builder object.
         * @throws NullPointerException if {@code logLevel} is {@code null}.
         */
        public Builder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
            Objects.requireNonNull(logLevel);
            httpLogDetailLevel = logLevel;
            return this;
        }

        /**
         * Adds a policy to the set of existing policies that are executed after
         * {@link com.azure.keyvault.SecretClient} required policies.
         *
         * @param policy The {@link HttpPipelinePolicy policy} to be added.
         * @return The updated Builder object.
         * @throws NullPointerException if {@code policy} is {@code null}.
         */
        public Builder addPolicy(HttpPipelinePolicy policy) {
            Objects.requireNonNull(policy);
            policies.add(policy);
            return this;
        }

        /**
         * Sets the HTTP client to use for sending and receiving requests to and from the service.
         *
         * @param client The HTTP client to use for requests.
         * @return The updated Builder object.
         */
        public Builder httpClient(HttpClient client) {
            this.httpClient = client;
            return this;
        }
    }

    /**
     * The set operation adds a secret to the Azure Key Vault.
     * If the named secret already exists, Azure Key Vault creates a new version of that secret.
     * This operation requires the secrets/set permission.
     *
     * <p>
     * The {@code secret} is required along with its non-null fields secret.name and secret.value.
     * The secret.expires, secret.contentType and and secret.notBefore values in {@code secret} are optional.
     * If not specified, no values are set for the fields.
     * </p>
     *
     * @param secret The Secret object containing information about the secret and its properties.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @return A {@link Mono} containing a {@link RestResponse} whose {@link RestResponse#body()} contains the created {@link Secret}.
     */
    public Mono<RestResponse<Secret>> setSecretAsync(Secret secret) {
        Objects.requireNonNull(secret, "The Secret input parameter cannot be null.");
        Objects.requireNonNull(secret.name(), "The Secret name cannot be null.");
        Objects.requireNonNull(secret.value(), "The Secret value cannot be null.");

        SecretRequestParameters parameters = new SecretRequestParameters()
                                            .value(secret.value())
                                            .tags(secret.tags())
                                            .contentType(secret.contentType())
                                            .secretAttributes(new SecretRequestAttributes(secret));

        return service.setSecret(vaultEndPoint, secret.name(), API_VERSION, ACCEPT_LANGUAGE, parameters);
    }

    /**
     * The set operation adds a secret to the Azure Key Vault.
     * If the named secret already exists, Azure Key Vault creates a new version of that secret.
     * This operation requires the secrets/set permission.
     *
     * @param name The name of the secret. It is required and cannot be null.
     * @param value The value of the secret. It is required and cannot be null.
     * @throws NullPointerException if {@code name} or {@code value} parameter is {@code null}.
     * @return A {@link Mono} containing a {@link RestResponse} whose {@link RestResponse#body()} contains the created {@link Secret}.
     */
    public Mono<RestResponse<Secret>> setSecretAsync(String name, String value) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        Objects.requireNonNull(value, "The Secret value cannot be null.");

        SecretRequestParameters parameters = new SecretRequestParameters()
                                            .value(value);
        return service.setSecret(vaultEndPoint, name, API_VERSION, ACCEPT_LANGUAGE, parameters);
    }

    /**
     * Get the specified secret with specified version from the key vault.
     * The get operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the secrets/get permission.
     *
     * @param name The name of the secret, cannot be null
     * @param version The version of the secret to retrieve. If not specified the latest version will be retrieved.
     * @throws NullPointerException if {@code name} or {@code version} parameter is {@code null}.
     * @return A {@link Mono} containing a {@link RestResponse} whose {@link RestResponse#body()} contains the requested {@link Secret}.
     * @throws com.azure.common.http.rest.RestException when a secret with {@code name} and {@code version} doesn't exist in the key vault.
     */
    public Mono<RestResponse<Secret>> getSecretAsync(String name, String version) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        if(version == null){
            return service.getSecret(vaultEndPoint, name, "", API_VERSION, ACCEPT_LANGUAGE);
        } else {
            return service.getSecret(vaultEndPoint, name, version, API_VERSION, ACCEPT_LANGUAGE);
        }
    }

    /**
     * Get the latest version of the specified secret from the key vault.
     * The get operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the secrets/get permission.
     *
     * @param name The name of the secret.
     * @throws NullPointerException if {@code name} parameter is {@code null}.
     * @return A {@link Mono} containing a {@link RestResponse} whose {@link RestResponse#body()} contains the requested {@link Secret}.
     * @throws com.azure.common.http.rest.RestException when a secret with {@code name} doesn't exist in the key vault.
     */
    public Mono<RestResponse<Secret>> getSecretAsync(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return getSecretAsync(name, "");
    }

    /**
     * Updates the attributes associated with a specified secret in the key vault.
     * The update operation changes specified attributes of an existing stored secret.
     * Attributes that are not specified in the request are left unchanged.
     * The value of a secret itself cannot be changed. This operation requires the secrets/set permission.
     *
     * <p>
     * The {@code secretAttributes} is required along with its non-null fields secretAttributes.name and secretAttributes.version.
     * </p>
     *
     * @param secretAttributes the {@link SecretAttributes} object with updated properties.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @return A {@link Mono} containing a {@link RestResponse} whose {@link RestResponse#body()} contains the updated {@link SecretAttributes}.
     * @throws com.azure.common.http.rest.RestException when a secret with secretAttributes.name and secretAttributes.version doesn't exist in the key vault.
     */
    public Mono<RestResponse<SecretAttributes>> updateSecretAttributesAsync(SecretAttributes secretAttributes) {
        Objects.requireNonNull(secretAttributes, "The secretAttributes input parameter cannot be null.");
        Objects.requireNonNull(secretAttributes.name(), "The Secret name cannot be null.");
        Objects.requireNonNull(secretAttributes.version(), "The Secret version cannot be null.");

        SecretRequestParameters parameters = new SecretRequestParameters()
                .tags(secretAttributes.tags())
                .contentType(secretAttributes.contentType())
                .secretAttributes(new SecretRequestAttributes(secretAttributes));

        return service.updateSecret(vaultEndPoint, secretAttributes.name(), secretAttributes.version(), API_VERSION, ACCEPT_LANGUAGE, parameters);
    }

    /**
     * Deletes a secret from the key vault.
     * The delete operation applies to any secret stored in Azure Key Vault.
     * delete cannot be applied to an individual version of a secret. This operation requires the secrets/delete permission.
     *
     * @param name The name of the secret to be deleted.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return A {@link Mono} containing a {@link RestResponse} whose {@link RestResponse#body()} contains the deleted {@link DeletedSecret}.
     * @throws com.azure.common.http.rest.RestException when a secret with {@code name} doesn't exist in the key vault.
     */
    public Mono<RestResponse<DeletedSecret>> deleteSecretAsync(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.deleteSecret(vaultEndPoint, name, API_VERSION, ACCEPT_LANGUAGE);
    }

    /**
     * The get deleted secret operation returns the secrets that have been deleted for a vault enabled for soft-delete.
     * This operation requires the secrets/list permission.
     *
     * @param name The name of the deleted secret.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return A {@link Mono} containing a {@link RestResponse} whose {@link RestResponse#body()} contains the deleted {@link DeletedSecret}.
     * @throws com.azure.common.http.rest.RestException when a deleted secret with {@code name} doesn't exist in the key vault.
     */
    public Mono<RestResponse<DeletedSecret>> getDeletedSecretAsync(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.getDeletedSecret(vaultEndPoint, name, API_VERSION, ACCEPT_LANGUAGE);
    }

    /**
     * The purge deleted secret operation removes the secret permanently, without the possibility of recovery.
     * This operation can only be enabled on a soft-delete enabled vault. This operation requires the secrets/purge permission.
     *
     * @param name The name of the secret.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return A {@link Mono} containing a {@link RestVoidResponse}.
     * @throws com.azure.common.http.rest.RestException when a deleted secret with {@code name} doesn't exist in the key vault.
     */
    public Mono<RestVoidResponse> purgeDeletedSecretAsync(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.purgeDeletedSecret(vaultEndPoint, name, API_VERSION, ACCEPT_LANGUAGE);
    }

    /**
     * Recovers the deleted secret in the key vault to its latest version.
     * This operation can only be performed on a soft-delete enabled vault.
     * This operation requires the secrets/recover permission.
     *
     * @param name The name of the deleted secret to be recovered.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return A {@link Mono} containing a {@link RestResponse} whose {@link RestResponse#body()} contains the recovered {@link Secret}.
     * @throws com.azure.common.http.rest.RestException when a deleted secret with {@code name} doesn't exist in the key vault.
     */
    public Mono<RestResponse<Secret>> recoverDeletedSecretAsync(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.recoverDeletedSecret(vaultEndPoint, name, API_VERSION, ACCEPT_LANGUAGE);
    }

    /**
     * Requests a backup of the specified secret be downloaded to the client. All versions of the secret will be downloaded.
     * This operation requires the secrets/backup permission.
     *
     * @param name The name of the secret.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return A {@link Mono} containing a {@link RestResponse} whose {@link RestResponse#body()} contains the backed up secret blob.
     * @throws com.azure.common.http.rest.RestException when a secret with {@code name} doesn't exist in the key vault.
     */
    public Mono<RestResponse<byte[]>> backupSecretAsync(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.backupSecret(vaultEndPoint, name, API_VERSION, ACCEPT_LANGUAGE)
                .flatMap(new Function<RestResponse<SecretBackup>, Mono<? extends RestResponse<byte[]>>>() {
                    @Override
                    public Mono<? extends RestResponse<byte[]>> apply(RestResponse<SecretBackup> base64URLRestResponse) {
                         return Mono.just(new SimpleRestResponse<byte[]>(base64URLRestResponse.request(),
                                base64URLRestResponse.statusCode(),base64URLRestResponse.headers(),base64URLRestResponse.body().value()));
                    }
                });
    }

    /**
     * Restores a backed up secret, and all its versions, to a vault.
     * This operation requires the secrets/restore permission.
     *
     * @param backup The backup blob associated with the secret.
     * @throws NullPointerException if {@code SecretBackup} is {@code null}.
     * @return A {@link Mono} containing a {@link RestResponse} whose {@link RestResponse#body()} contains the restored {@link Secret}.
     * @throws com.azure.common.http.rest.RestException when the {@code backup} is corrupted.
     */
    public Mono<RestResponse<Secret>> restoreSecretAsync(byte[] backup) {
        Objects.requireNonNull(backup, "The Secret backup parameter cannot be null.");
        SecretRestoreRequestParameters parameters = new SecretRestoreRequestParameters()
                                                .secretBackup(backup);
        return service.restoreSecret(vaultEndPoint, API_VERSION, ACCEPT_LANGUAGE, parameters);
    }

    /**
     * List {@link Secret secrets} in the key vault.
     * The list Secrets operation is applicable to the entire vault. However, only the base secret identifier and its attributes are provided in the response.
     * Individual secret versions are not listed in the response. This operation requires the secrets/list permission.
     *
     * @return A {@link Flux} containing {@link SecretAttributes} of all the secrets in the vault.
     */
    public Flux<SecretAttributes> listSecretsAsync() {
        return getPagedSecrets(service.getSecrets(vaultEndPoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE));
    }

    /**
     * Lists {@link DeletedSecret secrets} of the key vault.
     * The get deleted secrets operation returns the secrets that have been deleted for a vault enabled for soft-delete.
     * This operation requires the secrets/list permission.
     *
     * @return A {@link Flux} containing all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    public Flux<DeletedSecret> listDeletedSecretsAsync() {
        return getPagedDeletedSecrets(service.getDeletedSecrets(vaultEndPoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE));
    }

    /**
     * List all versions of the specified secret. The individual secret version response in the list is represented by {@link SecretAttributes}.
     * The full secret identifier and attributes are provided in the response. No values are returned for the secrets.
     * This operation requires the secrets/list permission.
     *
     * @param name The name of the secret.
     * @throws NullPointerException thrown if name parameter is null
     * @return A {@link Flux} containing {@link SecretAttributes} of all the versions of the specified secret in the vault. Flux is empty if secret with {@code name} does not exist in key vault
     */
    public Flux<SecretAttributes> listSecretVersionsAsync(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return getPagedSecrets(service.getSecretVersions(vaultEndPoint, name, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE));
    }

    /**
     * List secrets in a specified key vault.
     * The list secrets operation is applicable to the entire vault.
     * However, only the base secret identifier and its attributes are provided in the response. Individual secret versions are not listed in the response.
     * This operation requires the secrets/list permission.
     *
     * @param maxPageResults Maximum number of results to return in a page. If not specified, the service will return up to 25 results.
     * @throws NullPointerException thrown if maxPageResults parameter is null
     * @return A {@link Flux} containing {@link SecretAttributes} of all the secrets in the key vault.
     */
    public Flux<SecretAttributes> listSecretsAsync(int maxPageResults) {
        Objects.requireNonNull(maxPageResults, "The maximum page results parameter cannot be null.");
        return getPagedSecrets(service.getSecrets(vaultEndPoint, maxPageResults, API_VERSION, ACCEPT_LANGUAGE));
    }

    /**
     * Lists {@link DeletedSecret} secrets of the key vault.
     * The list deleted secrets operation returns the secrets that have been deleted for a vault enabled for soft-delete.
     * This operation requires the secrets/list permission.
     *
     * @param maxPageResults Maximum number of results to return in a page. If not specified, the service will return up to 25 results.
     * @throws NullPointerException thrown if maxPageResults parameter is null
     * @return A {@link Flux} containing all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    public Flux<DeletedSecret> listDeletedSecretsAsync(int maxPageResults) {
        Objects.requireNonNull(maxPageResults, "The maximum page results parameter cannot be null.");
        return getPagedDeletedSecrets(service.getDeletedSecrets(vaultEndPoint, maxPageResults, API_VERSION, ACCEPT_LANGUAGE));
    }

    /**
     * List all versions of the specified secret.
     * The full secret identifier and attributes are provided in the response. No values are returned for the secrets.
     * This operations requires the secrets/list permission.
     *
     * @param name The name of the secret.
     * @param maxPageResults Maximum number of results to return in a page. If not specified, the service will return up to 25 results.
     * @throws NullPointerException thrown if {@code name} or {@code maxPageResults} is null
     * @return A {@link Flux} containing {@link SecretAttributes} of all the versions of the secret in the key vault. Flux is empty if secret with {@code name} does not exist in key vault
     */
    public Flux<SecretAttributes> listSecretVersionsAsync(String name, int maxPageResults) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        Objects.requireNonNull(maxPageResults, "The maximum page results parameter cannot be null.");
        return getPagedSecrets(service.getSecretVersions(vaultEndPoint, name, maxPageResults, API_VERSION, ACCEPT_LANGUAGE));
    }

    /**
     * Gets attributes of all the secrets given by the {@code nextPageLink} that was retrieved from a call to
     * {@link SecretClient#listSecretsAsync()} or {@link SecretClient#listSecretsAsync(int)}.
     *
     * @param nextPageLink The {@link Page#nextPageLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link SecretAttributes} from the next page of results.
     */
    private Flux<SecretAttributes> listSecretsNext(String nextPageLink) {
        return getPagedSecrets(service.getSecrets(vaultEndPoint, nextPageLink, ACCEPT_LANGUAGE));
    }

    /**
     * Gets all deleted secrets given by the {@code nextPageLink} that was retrieved from a call to
     * {@link SecretClient#listDeletedSecretsAsync()} or {@link SecretClient#listDeletedSecretsAsync(int)}.
     *
     * @param nextPageLink The {@link Page#nextPageLink()} from a previous, successful call to one of the list operations.
     * @return A {@link Flux} containing {@link SecretAttributes} of all the versions of the secret in the key vault.
     * @return A stream of {@link DeletedSecret} from the next page of results.
     */
    private Flux<DeletedSecret> listDeletedSecretsNext(String nextPageLink) {
        return getPagedDeletedSecrets(service.getDeletedSecrets(vaultEndPoint, nextPageLink, ACCEPT_LANGUAGE));
    }

    private Flux<SecretAttributes> getPagedSecrets(Mono<RestResponse<Page<SecretAttributes>>> response) {
        return response.flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())))
                .concatMap(this::extractAndFetchSecrets);
    }

    private Flux<DeletedSecret> getPagedDeletedSecrets(Mono<RestResponse<Page<DeletedSecret>>> response) {
        return response.flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())))
                .concatMap(this::extractAndFetchDeletedSecrets);
    }

    private Publisher<SecretAttributes> extractAndFetchSecrets(RestPagedResponseImpl<SecretAttributes> page) {
        String nextPageLink = page.nextLink();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(listSecretsNext(nextPageLink));
    }

    private Publisher<DeletedSecret> extractAndFetchDeletedSecrets(RestPagedResponseImpl<DeletedSecret> page) {
        String nextPageLink = page.nextLink();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(listDeletedSecretsNext(nextPageLink));
    }

}
