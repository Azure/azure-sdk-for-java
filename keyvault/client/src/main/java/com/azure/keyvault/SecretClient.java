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
import com.azure.common.implementation.RestProxy;
import com.azure.keyvault.implementation.Page;
import com.azure.keyvault.implementation.RestPagedResponseImpl;
import com.azure.keyvault.models.DeletedSecret;
import com.azure.keyvault.models.Secret;
import com.azure.keyvault.models.SecretRequestAttributes;
import com.azure.keyvault.models.SecretRequestOptions;
import com.azure.keyvault.models.SecretBackup;
import com.azure.keyvault.models.SecretInfo;
import com.azure.keyvault.models.SecretRestoreRequestOptions;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

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
     * Creates a SecretClient that uses {@code credentials} to authorize with Azure and {@code pipeline} to
     * service requests
     *
     * @param vaultEndPoint URL for the Application configuration service.
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
         * Creates a {@link SecretClient} based on options set in the Builder.
         *
         * Every time {@code build()} is called, a new instance of {@link SecretClient} is created.
         *
         * @return A SecretClient with the options set from the builder.
         * @throws IllegalStateException If {@link Builder#credentials(ServiceClientCredentials)}
         * @throws IllegalStateException If {@link Builder#credentials(ServiceClientCredentials)}
         * has not been set.
         */
        public SecretClient build() {
            if (credentials == null) {
                throw new IllegalStateException("'credentials' is required.");
            }

            if (vaultEndPoint == null){
                throw new IllegalStateException("'Vault's Endpoint Url' is required.");
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
         * Sets the credentials to use when authenticating HTTP requests.
         *
         * @param vaultEndPoint The vault endpoint url is used as destination on Azure to send requests to.
         * @return The updated Builder object.
         * @throws NullPointerException if {@code credentials} is {@code null}.
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
         * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
         * @return The updated Builder object.
         */
        public Builder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
            httpLogDetailLevel = logLevel;
            return this;
        }

        /**
         * Adds a policy to the set of existing policies that are executed after
         * {@link com.azure.keyvault.SecretClient} required policies.
         *
         * @param policy The retry policy for service requests.
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
         * @throws NullPointerException if {@code client} is {@code null}.
         */
        public Builder httpClient(HttpClient client) {
            this.httpClient = client;
            return this;
        }
    }

    public static List<HttpPipelinePolicy> getDefaultPolicies(ServiceClientCredentials credentials, String vaultUrl) {
        //final ApplicationConfigCredentials credentials = new ApplicationConfigCredentials(connectionString);
        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(String.format("Azure-SDK-For-Java/%s (%s)", SDK_NAME, SDK_VERSION)));
        policies.add(new RequestIdPolicy());
        policies.add(new RetryPolicy());
        policies.add(new KeyvaultCredentialsPolicy(credentials, vaultUrl));

        return policies;
    }

    /**
     * Sets a secret in a the key vault.
     * The SET operation adds a secret to the Azure Key Vault.
     * If the named secret already exists, Azure Key Vault creates a new version of that secret.
     * This operation requires the secrets/set permission.
     *
     * <p>
     * The name and value fields in {@code secret} are required.
     * The expires, contentType and and notBefore values value for the Secret are optional.
     * If not specified, no values are set for the fields.
     * </p>
     *
     * @param secret The Secret object containing information about the secret and its properties.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @return the Mono to the Secret object
     */
    public Mono<RestResponse<Secret>> setSecretAsync(Secret secret) {
        Objects.requireNonNull(secret, "The Secret input parameter cannot be null.");
        Objects.requireNonNull(secret.name(), "The Secret name cannot be null.");
        Objects.requireNonNull(secret.value(), "The Secret value cannot be null.");

        SecretRequestAttributes secretRequestAttributes = new SecretRequestAttributes()
                                                .withEnabled(secret.enabled())
                                                .withExpires(secret.expires())
                                                .withNotBefore(secret.notBefore());

        SecretRequestOptions parameters = new SecretRequestOptions()
                                            .withValue(secret.value())
                                            .withTags(secret.tags())
                                            .withContentType(secret.contentType())
                                            .withSecretAttributes(secretRequestAttributes);

        return service.setSecret(vaultEndPoint, secret.name(), API_VERSION,ACCEPT_LANGUAGE , parameters);
    }


    /**
     * Sets a secret in the key vault.
     * The SET operation adds a secret to the Azure Key Vault.
     * If the named secret already exists, Azure Key Vault creates a new version of that secret.
     * This operation requires the secrets/set permission.
     *
     * @param name The name of the secret. This field
     * @param value The value of the secret.
     * @throws NullPointerException if {@code name} or {@code value} parameter is {@code null}.
     * * @return the Secret that was created.
     */
    public Mono<RestResponse<Secret>> setSecretAsync(String name, String value) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        Objects.requireNonNull(value, "The Secret value cannot be null.");

        SecretRequestOptions parameters = new SecretRequestOptions()
                                            .withValue(value);
        return service.setSecret(vaultEndPoint, name, API_VERSION, ACCEPT_LANGUAGE, parameters);
    }


    /**
     * Get a specified secret from the key vault.
     * The GET operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the secrets/get permission.
     *
     * @param name The name of the secret, cannot be null
     * @param version The version of the secret, cannot be null
     * @throws NullPointerException if {@code name} or {@code version} parameter is {@code null}.
     * @return the Secret that was requested.
     */
    public Mono<RestResponse<Secret>> getSecretAsync(String name, String version) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        Objects.requireNonNull(version, "The Secret version cannot be null.");
        return service.getSecret(vaultEndPoint, name, version, API_VERSION, ACCEPT_LANGUAGE);
    }

    /**
     * Get a specified secret from the key vault.
     * The GET operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the secrets/get permission.
     *
     * @param name The name of the secret.
     * @throws NullPointerException if {@code name} parameter is {@code null}.
     * @return the Secret that was requested.
     */
    public Mono<RestResponse<Secret>> getSecretAsync(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return getSecretAsync(name, "");
    }


    /**
     * Updates the attributes associated with a specified secret in the key vault.
     * The UPDATE operation changes specified attributes of an existing stored secret.
     * Attributes that are not specified in the request are left unchanged.
     * The value of a secret itself cannot be changed. This operation requires the secrets/set permission.
     *
     * @param secret the Secret object with updated properties.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @return the Secret that was updated.
     */
    public Mono<RestResponse<Secret>> updateSecretAsync(Secret secret) {
        Objects.requireNonNull(secret, "The Secret input parameter cannot be null.");
        Objects.requireNonNull(secret.name(), "The Secret name cannot be null.");

        SecretRequestAttributes secretRequestAttributes = new SecretRequestAttributes()
                .withEnabled(secret.enabled())
                .withExpires(secret.expires())
                .withNotBefore(secret.notBefore());

        SecretRequestOptions parameters = new SecretRequestOptions()
                .withValue(secret.value())
                .withTags(secret.tags())
                .withContentType(secret.contentType())
                .withSecretAttributes(secretRequestAttributes);

        return service.updateSecret(vaultEndPoint, secret.name(), "", API_VERSION, ACCEPT_LANGUAGE, parameters);
    }


    /**
     * Deletes a secret from the key vault.
     * The DELETE operation applies to any secret stored in Azure Key Vault.
     * DELETE cannot be applied to an individual version of a secret. This operation requires the secrets/delete permission.
     *
     * @param name The name of the secret to be deleted.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return the Deleted Secret instance of the secret that was deleted.
     */
    public Mono<RestResponse<DeletedSecret>> deleteSecretAsync(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.deleteSecret(vaultEndPoint, name, API_VERSION, ACCEPT_LANGUAGE);
    }


    /**
     * Lists deleted secrets for the key vault.
     * The Get Deleted Secrets operation returns the secrets that have been deleted for a vault enabled for soft-delete.
     * This operation requires the secrets/list permission.
     *
     * @param name The name of the deleted secret.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return the Deleted {@link Secret}.
     */
    public Mono<RestResponse<DeletedSecret>> getDeletedSecretAsync(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.getDeletedSecret(vaultEndPoint, name, API_VERSION, ACCEPT_LANGUAGE);
    }


    /**
     * Permanently deletes the specified secret.
     * The purge deleted secret operation removes the secret permanently, without the possibility of recovery.
     * This operation can only be enabled on a soft-delete enabled vault. This operation requires the secrets/purge permission.
     *
     * @param name The name of the secret.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return the RestResponse.
     */
    public Mono<RestResponse> purgeDeletedSecretAsync(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.purgeDeletedSecret(vaultEndPoint, name, API_VERSION, ACCEPT_LANGUAGE);
    }


    /**
     * Recovers the deleted secret to the latest version.
     * Recovers the deleted secret in the key vault. This operation can only be performed on a soft-delete enabled vault.
     * This operation requires the secrets/recover permission.
     *
     * @param name The name of the deleted secret to be recovered.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return the {@link Secret} that was recovered
     */
    public Mono<RestResponse<Secret>> recoverDeletedSecretAsync(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.recoverDeletedSecret(vaultEndPoint, name, API_VERSION, ACCEPT_LANGUAGE);
    }


    /**
     * Backs up the specified secret.
     * Requests that a backup of the specified secret be downloaded to the client. All versions of the secret will be downloaded.
     * This operation requires the secrets/backup permission.
     *
     * @param name The name of the secret.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @return the {@link SecretBackup} backup of the secret.
     */
    public Mono<RestResponse<SecretBackup>> backupSecretAsync(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        return service.backupSecret(vaultEndPoint, name, API_VERSION, ACCEPT_LANGUAGE);
    }


    /**
     * Restores a backed up secret to a vault.
     * Restores a backed up secret, and all its versions, to a vault.
     * This operation requires the secrets/restore permission.
     *
     * @param backup The backup blob associated with the secret.
     * @throws NullPointerException if {@code SecretBackup} is {@code null}.
     * @return the {@link Secret} that was restored.
     */
    public Mono<RestResponse<Secret>> restoreSecretAsync(SecretBackup backup) {
        Objects.requireNonNull(backup, "The Secret backup parameter cannot be null.");
        Objects.requireNonNull(backup.value(), "The backup value cannot be null.");
        SecretRestoreRequestOptions parameters = new SecretRestoreRequestOptions()
                                                .withSecretBackup(backup.value());
        return service.restoreSecret(vaultEndPoint, API_VERSION, ACCEPT_LANGUAGE, parameters);
    }


    /**
     * List {@link Secret} secrets in a specified key vault.
     * The Get Secrets operation is applicable to the entire vault. However, only the base secret identifier and its attributes are provided in the response.
     * Individual secret versions are not listed in the response. This operation requires the secrets/list permission.
     *
     * @return A Flux of Secrets. The Flux contains all of the secrets in the vault.
     */
    public Flux<SecretInfo> listSecretsAsync() {
        Mono<RestResponse<Page<SecretInfo>>> result = service.getSecrets(vaultEndPoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE);

        return getPagedSecrets(result);
    }

    /**
     * Lists {@link DeletedSecret} secrets of the key vault.
     * The Get Deleted Secrets operation returns the secrets that have been deleted for a vault enabled for soft-delete.
     * This operation requires the secrets/list permission.
     *
     * @return A Flux of Deleted Secrets. The Flux contains all of the deleted secrets in the vault.
     */
    public Flux<DeletedSecret> listDeletedSecretsAsync() {
        Mono<RestResponse<Page<DeletedSecret>>> result = service.getDeletedSecrets(vaultEndPoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE);
        return getPagedDeletedSecrets(result);
    }

    /**
     * List all versions of the specified secret. The individual secret version response in the list is represented by {@link SecretInfo}
     * The full secret identifier and attributes are provided in the response. No values are returned for the secrets.
     * This operations requires the secrets/list permission.
     *
     * @param name The name of the secret.
     * @throws NullPointerException thrown if name parameter is null
     * @return A Flux of Secrets. The Flux contains all of the versions of the secret in the vault.
     */
    public Flux<SecretInfo> listSecretVersionsAsync(String name) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        Mono<RestResponse<Page<SecretInfo>>> result = service.getSecretVersions(vaultEndPoint, name, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE);

        return getPagedSecrets(result);
    }

    /**
     * List secrets in a specified key vault.
     * The Get Secrets operation is applicable to the entire vault.
     * However, only the base secret identifier and its attributes are provided in the response. Individual secret versions are not listed in the response.
     * This operation requires the secrets/list permission.
     *
     * @param maxPageResults Maximum number of results to return in a page. If not specified, the service will return up to 25 results.
     * @throws NullPointerException thrown if maxPageResults parameter is null
     * @return A Flux of Secrets. The Flux contains all of the secrets in the vault.
     */
    public Flux<SecretInfo> listSecretsAsync(int maxPageResults) {
        Objects.requireNonNull(maxPageResults, "The maximum page results parameter cannot be null.");
        Mono<RestResponse<Page<SecretInfo>>> result = service.getSecrets(vaultEndPoint, maxPageResults, API_VERSION, ACCEPT_LANGUAGE);

        return getPagedSecrets(result);
    }


    /**
     * Lists {@link DeletedSecret} secrets of the key vault.
     * The Get Deleted Secrets operation returns the secrets that have been deleted for a vault enabled for soft-delete.
     * This operation requires the secrets/list permission.
     *
     * @param maxPageResults Maximum number of results to return in a page. If not specified, the service will return up to 25 results.
     * @throws NullPointerException thrown if maxPageResults parameter is null
     * @return A Flux of Deleted Secrets. The Flux contains all of the deleted secrets in the vault.
     */
    public Flux<DeletedSecret> listDeletedSecretsAsync(int maxPageResults) {
        Objects.requireNonNull(maxPageResults, "The maximum page results parameter cannot be null.");
        Mono<RestResponse<Page<DeletedSecret>>> result = service.getDeletedSecrets(vaultEndPoint, maxPageResults, API_VERSION, ACCEPT_LANGUAGE);
        return getPagedDeletedSecrets(result);
    }

    /**
     * List all versions of the specified secret.
     * The full secret identifier and attributes are provided in the response. No values are returned for the secrets.
     * This operations requires the secrets/list permission.
     *
     * @param name The name of the secret.
     * @param maxPageResults Maximum number of results to return in a page. If not specified, the service will return up to 25 results.
     * @throws NullPointerException thrown if {@code name} or {@code maxPageResults} is null
     * @return A Flux of Secrets. The Flux contains all of the versions of the secret in the vault.
     */
    public Flux<SecretInfo> listSecretVersionsAsync(String name, int maxPageResults) {
        Objects.requireNonNull(name, "The Secret name cannot be null.");
        Objects.requireNonNull(maxPageResults, "The maximum page results parameter cannot be null.");
        Mono<RestResponse<Page<SecretInfo>>> result = service.getSecretVersions(vaultEndPoint, name, maxPageResults, API_VERSION, ACCEPT_LANGUAGE);

        return getPagedSecrets(result);
    }

    private Flux<SecretInfo> listSecretsNext(@NonNull String nextPageLink) {
        Mono<RestResponse<Page<SecretInfo>>> result = service.getSecrets(vaultEndPoint, nextPageLink, ACCEPT_LANGUAGE);
        return getPagedSecrets(result);
    }

    private Flux<DeletedSecret> listDeletedSecretsNext(@NonNull String nextPageLink) {
        Mono<RestResponse<Page<DeletedSecret>>> result = service.getDeletedSecrets(vaultEndPoint, nextPageLink, ACCEPT_LANGUAGE);
        return getPagedDeletedSecrets(result);
    }

    private Flux<SecretInfo> getPagedSecrets(Mono<RestResponse<Page<SecretInfo>>> response) {
        return response.flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())))
                .concatMap(this::extractAndFetchSecrets);
    }

    private Flux<DeletedSecret> getPagedDeletedSecrets(Mono<RestResponse<Page<DeletedSecret>>> response) {
        return response.flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())))
                .concatMap(this::extractAndFetchDeletedSecrets);
    }

    private Publisher<SecretInfo> extractAndFetchSecrets(RestPagedResponseImpl<SecretInfo> page) {
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
