// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.implementation.util.BuilderHelper;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.connectionstring.StorageAuthenticationSettings;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import com.azure.storage.common.implementation.connectionstring.StorageEndpoint;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.implementation.policy.SasTokenCredentialPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;
import com.azure.storage.common.policy.ScrubEtagPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.USER_AGENT_PROPERTIES;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of Storage Blob clients.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>Endpoint set through {@link #endpoint(String)}, including the container name and blob name, in the format of
 * {@code https://{accountName}.blob.core.windows.net/{containerName}/{blobName}}.
 * <li>Container and blob name if not specified in the {@link #endpoint(String)}, set through
 * {@link #containerName(String)} and {@link #blobName(String)} respectively.
 * <li>Credential set through {@link #credential(StorageSharedKeyCredential)} , {@link #sasToken(String)}, or
 * {@link #connectionString(String)} if the container is not publicly accessible.
 * <li>Key and key wrapping algorithm (for encryption) and/or key resolver (for decryption) must be specified
 * through {@link #key(AsyncKeyEncryptionKey, String)} and {@link #keyResolver(AsyncKeyEncryptionKeyResolver)}
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder use the following mapping to construct the given client:
 * <ul>
 * <li>{@link EncryptedBlobClientBuilder#buildEncryptedBlobClient()} - {@link EncryptedBlobClient}</li>
 * <li>{@link EncryptedBlobClientBuilder#buildEncryptedBlobAsyncClient()} -
 * {@link EncryptedBlobAsyncClient}</li>
 * </ul>
 */
@ServiceClientBuilder(serviceClients = {EncryptedBlobAsyncClient.class, EncryptedBlobClient.class})
public final class EncryptedBlobClientBuilder {
    private final ClientLogger logger = new ClientLogger(EncryptedBlobClientBuilder.class);
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private String endpoint;
    private String accountName;
    private String containerName;
    private String blobName;
    private String snapshot;

    private StorageSharedKeyCredential storageSharedKeyCredential;
    private TokenCredential tokenCredential;
    private SasTokenCredential sasTokenCredential;

    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
    private HttpLogOptions logOptions;
    private RequestRetryOptions retryOptions = new RequestRetryOptions();
    private HttpPipeline httpPipeline;

    private Configuration configuration;

    private AsyncKeyEncryptionKey keyWrapper;
    private AsyncKeyEncryptionKeyResolver keyResolver;
    private String keyWrapAlgorithm;
    private BlobServiceVersion version;

    /**
     * Creates a new instance of the EncryptedBlobClientBuilder
     */
    public EncryptedBlobClientBuilder() {
        logOptions = getDefaultHttpLogOptions();
    }

    /**
     * Creates a {@link EncryptedBlobClient} based on options set in the Builder.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlobAsyncClient}
     *
     * @return a {@link EncryptedBlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public EncryptedBlobClient buildEncryptedBlobClient() {
        return new EncryptedBlobClient(buildEncryptedBlobAsyncClient());
    }

    /**
     * Creates a {@link EncryptedBlobAsyncClient} based on options set in the Builder.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlobClient}
     *
     * @return a {@link EncryptedBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public EncryptedBlobAsyncClient buildEncryptedBlobAsyncClient() {
        Objects.requireNonNull(blobName, "'blobName' cannot be null.");
        checkValidEncryptionParameters();

        /*
        Implicit and explicit root container access are functionally equivalent, but explicit references are easier
        to read and debug.
         */
        if (CoreUtils.isNullOrEmpty(containerName)) {
            containerName = BlobContainerAsyncClient.ROOT_CONTAINER_NAME;
        }
        BlobServiceVersion serviceVersion = version != null ? version : BlobServiceVersion.getLatest();

        return new EncryptedBlobAsyncClient(getHttpPipeline(),
            String.format("%s/%s/%s", endpoint, containerName, blobName), serviceVersion, accountName, containerName,
            blobName, snapshot, keyWrapper, keyWrapAlgorithm);
    }

    private HttpPipeline getHttpPipeline() {
        if (httpPipeline != null) {
            return  httpPipeline;
        }

        Configuration userAgentConfiguration = (configuration == null) ? Configuration.NONE : configuration;

        // Closest to API goes first, closest to wire goes last.
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new BlobDecryptionPolicy(keyWrapper, keyResolver));
        String clientName = USER_AGENT_PROPERTIES.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = USER_AGENT_PROPERTIES.getOrDefault(SDK_VERSION, "UnknownVersion");
        policies.add(new UserAgentPolicy(logOptions.getApplicationId(), clientName, clientVersion,
            userAgentConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        if (storageSharedKeyCredential != null) {
            policies.add(new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential));
        } else if (tokenCredential != null) {
            BuilderHelper.httpsValidation(tokenCredential, "bearer token", endpoint, logger);
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", endpoint)));
        } else if (sasTokenCredential != null) {
            policies.add(new SasTokenCredentialPolicy(sasTokenCredential));
        }

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new RequestRetryPolicy(retryOptions));

        policies.addAll(additionalPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(new ResponseValidationPolicyBuilder()
            .addOptionalEcho(Constants.HeaderConstants.CLIENT_REQUEST_ID)
            .addOptionalEcho(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256)
            .build());

        policies.add(new HttpLoggingPolicy(logOptions));

        policies.add(new ScrubEtagPolicy());

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    /**
     * Sets the encryption key parameters for the client
     *
     * @param key An object of type {@link AsyncKeyEncryptionKey} that is used to wrap/unwrap the content encryption
     * key
     * @param keyWrapAlgorithm The {@link String} used to wrap the key.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder key(AsyncKeyEncryptionKey key, String keyWrapAlgorithm) {
        this.keyWrapper = key;
        this.keyWrapAlgorithm = keyWrapAlgorithm;
        return this;
    }

    /**
     * Sets the encryption parameters for this client
     *
     * @param keyResolver The key resolver used to select the correct key for decrypting existing blobs.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder keyResolver(AsyncKeyEncryptionKeyResolver keyResolver) {
        this.keyResolver = keyResolver;
        return this;
    }

    private void checkValidEncryptionParameters() {
        // Check that key and key wrapper are not both null.
        if (this.keyWrapper == null && this.keyResolver == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Key and KeyResolver cannot both be null"));
        }

        // If the key is provided, ensure the key wrap algorithm is also provided.
        if (this.keyWrapper != null && this.keyWrapAlgorithm == null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Key Wrap Algorithm must be specified with a Key."));
        }
    }

    /**
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated EncryptedBlobClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public EncryptedBlobClientBuilder credential(StorageSharedKeyCredential credential) {
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated EncryptedBlobClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public EncryptedBlobClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     * @return the updated EncryptedBlobClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public EncryptedBlobClientBuilder sasToken(String sasToken) {
        this.sasTokenCredential = new SasTokenCredential(Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null."));
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Clears the credential used to authorize the request.
     *
     * <p>This is for blobs that are publicly accessible.</p>
     *
     * @return the updated EncryptedBlobClientBuilder
     */
    public EncryptedBlobClientBuilder setAnonymousAccess() {
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the connection string to connect to the service.
     *
     * @param connectionString Connection string of the storage account.
     * @return the updated EncryptedBlobClientBuilder
     * @throws IllegalArgumentException If {@code connectionString} is invalid.
     */
    public EncryptedBlobClientBuilder connectionString(String connectionString) {
        StorageConnectionString storageConnectionString
                = StorageConnectionString.create(connectionString, logger);
        StorageEndpoint endpoint = storageConnectionString.getBlobEndpoint();
        if (endpoint == null || endpoint.getPrimaryUri() == null) {
            throw logger
                    .logExceptionAsError(new IllegalArgumentException(
                            "connectionString missing required settings to derive blob service endpoint."));
        }
        this.endpoint(endpoint.getPrimaryUri());
        if (storageConnectionString.getAccountName() != null) {
            this.accountName = storageConnectionString.getAccountName();
        }
        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        if (authSettings.getType() == StorageAuthenticationSettings.Type.ACCOUNT_NAME_KEY) {
            this.credential(new StorageSharedKeyCredential(authSettings.getAccount().getName(),
                    authSettings.getAccount().getAccessKey()));
        } else if (authSettings.getType() == StorageAuthenticationSettings.Type.SAS_TOKEN) {
            this.sasToken(authSettings.getSasToken());
        }
        return this;
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name, blob name)
     *
     * <p>If the endpoint is to a blob in the root container, this method will fail as it will interpret the blob name
     * as the container name. With only one path element, it is impossible to distinguish between a container name and a
     * blob in the root container, so it is assumed to be the container name as this is much more common. When working
     * with blobs in the root container, it is best to set the endpoint to the account url and specify the blob name
     * separately using the {@link EncryptedBlobClientBuilder#blobName(String) blobName} method.</p>
     *
     * @param endpoint URL of the service
     * @return the updated EncryptedBlobClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    public EncryptedBlobClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            BlobUrlParts parts = BlobUrlParts.parse(url);

            this.accountName = parts.getAccountName();
            this.endpoint = BuilderHelper.getEndpoint(parts);
            this.containerName = parts.getBlobContainerName();
            this.blobName = Utility.urlEncode(parts.getBlobName());
            this.snapshot = parts.getSnapshot();

            String sasToken = parts.getCommonSasQueryParameters().encode();
            if (!CoreUtils.isNullOrEmpty(sasToken)) {
                this.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage Blob endpoint url is malformed."));
        }
        return this;
    }

    /**
     * Sets the name of the container that contains the blob.
     *
     * @param containerName Name of the container. If the value {@code null} or empty the root container, {@code $root},
     * will be used.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder containerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * Sets the name of the blob.
     *
     * @param blobName Name of the blob.
     * @return the updated EncryptedBlobClientBuilder object
     * @throws NullPointerException If {@code blobName} is {@code null}
     */
    public EncryptedBlobClientBuilder blobName(String blobName) {
        this.blobName = Utility.urlEncode(Utility.urlDecode(Objects.requireNonNull(blobName,
            "'blobName' cannot be null.")));
        return this;
    }

    /**
     * Sets the snapshot identifier of the blob.
     *
     * @param snapshot Snapshot identifier for the blob.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder snapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            logger.info("'httpClient' is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a {@link HttpPipelinePolicy} to apply on each request sent. The policy will be added after the retry policy.
     * If the method is called multiple times, all policies will be added and their order preserved.
     *
     * @param pipelinePolicy a pipeline policy
     * @return the updated EncryptedBlobClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public EncryptedBlobClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated EncryptedBlobClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public EncryptedBlobClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Gets the default Storage whitelist log headers and query parameters.
     *
     * @return the default http log options.
     */
    public static HttpLogOptions getDefaultHttpLogOptions() {
        return BuilderHelper.getDefaultHttpLogOptions();
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions The options used to configure retry behavior.
     * @return the updated EncryptedBlobClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public EncryptedBlobClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the {@link BlobServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link BlobServiceVersion} of the service to be used when making requests.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder serviceVersion(BlobServiceVersion version) {
        this.version = version;
        return this;
    }
}
