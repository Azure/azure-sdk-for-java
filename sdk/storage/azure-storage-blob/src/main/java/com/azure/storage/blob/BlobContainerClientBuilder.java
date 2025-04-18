// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.AzureNamedKeyCredentialTrait;
import com.azure.core.client.traits.AzureSasCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.implementation.util.BuilderHelper;
import com.azure.storage.blob.models.BlobAudience;
import com.azure.storage.blob.models.BlobContainerEncryptionScope;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.connectionstring.StorageAuthenticationSettings;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import com.azure.storage.common.implementation.connectionstring.StorageEndpoint;
import com.azure.storage.common.policy.RequestRetryOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link
 * BlobContainerClient BlobContainerClients} and {@link BlobContainerAsyncClient BlobContainerAsyncClients}, call
 * {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an
 * instance of the desired client.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>the endpoint through {@code .endpoint()}, including the container name, in the format of {@code
 * https://{accountName}.blob.core.windows.net/{containerName}}.
 * <li>the credential through {@code .credential()} or {@code .connectionString()} if the container is not publicly
 * accessible.
 * </ul>
 */
@ServiceClientBuilder(serviceClients = { BlobContainerClient.class, BlobContainerAsyncClient.class })
public final class BlobContainerClientBuilder implements TokenCredentialTrait<BlobContainerClientBuilder>,
    ConnectionStringTrait<BlobContainerClientBuilder>, AzureSasCredentialTrait<BlobContainerClientBuilder>,
    AzureNamedKeyCredentialTrait<BlobContainerClientBuilder>, HttpTrait<BlobContainerClientBuilder>,
    ConfigurationTrait<BlobContainerClientBuilder>, EndpointTrait<BlobContainerClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(BlobContainerClientBuilder.class);

    private String endpoint;
    private String accountName;
    private String containerName;

    private CpkInfo customerProvidedKey;
    private EncryptionScope encryptionScope;
    private BlobContainerEncryptionScope blobContainerEncryptionScope;
    private StorageSharedKeyCredential storageSharedKeyCredential;
    private TokenCredential tokenCredential;
    private AzureSasCredential azureSasCredential;
    private String sasToken;

    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();
    private HttpLogOptions logOptions;
    private RequestRetryOptions retryOptions;
    private RetryOptions coreRetryOptions;
    private HttpPipeline httpPipeline;

    private ClientOptions clientOptions = new ClientOptions();
    private Configuration configuration;
    private BlobServiceVersion version;
    private BlobAudience audience;

    /**
     * Creates a builder instance that is able to configure and construct {@link BlobContainerClient ContainerClients}
     * and {@link BlobContainerAsyncClient ContainerAsyncClients}.
     */
    public BlobContainerClientBuilder() {
        logOptions = getDefaultHttpLogOptions();
    }

    /**
     * Creates a {@link BlobContainerClient} from the configured options.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClientBuilder.buildClient -->
     * <pre>
     * BlobContainerClient client = new BlobContainerClientBuilder&#40;&#41;
     *     .connectionString&#40;connectionString&#41;
     *     .buildClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClientBuilder.buildClient -->
     *
     * @return a {@link BlobContainerClient} created from the configurations in this builder.
     * @throws IllegalStateException If multiple credentials have been specified.
     */
    public BlobContainerClient buildClient() {
        BuilderHelper.httpsValidation(customerProvidedKey, "customer provided key", endpoint, LOGGER);

        if (Objects.nonNull(customerProvidedKey) && Objects.nonNull(encryptionScope)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Customer provided key and encryption " + "scope cannot both be set"));
        }

        /*
        Implicit and explicit root container access are functionally equivalent, but explicit references are easier
        to read and debug.
         */
        String blobContainerName
            = CoreUtils.isNullOrEmpty(containerName) ? BlobContainerClient.ROOT_CONTAINER_NAME : containerName;

        BlobServiceVersion serviceVersion = version != null ? version : BlobServiceVersion.getLatest();

        HttpPipeline pipeline = constructPipeline();

        return new BlobContainerClient(pipeline, endpoint, serviceVersion, accountName, blobContainerName,
            customerProvidedKey, encryptionScope, blobContainerEncryptionScope);
    }

    /**
     * Creates a {@link BlobContainerAsyncClient} from the configured options.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClientBuilder.buildAsyncClient -->
     * <pre>
     * BlobContainerAsyncClient client = new BlobContainerClientBuilder&#40;&#41;
     *     .connectionString&#40;connectionString&#41;
     *     .buildAsyncClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClientBuilder.buildAsyncClient -->
     *
     * @return a {@link BlobContainerAsyncClient} created from the configurations in this builder.
     * @throws IllegalStateException If multiple credentials have been specified.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryOptions(RequestRetryOptions)} have been set.
     */
    public BlobContainerAsyncClient buildAsyncClient() {
        BuilderHelper.httpsValidation(customerProvidedKey, "customer provided key", endpoint, LOGGER);

        if (Objects.nonNull(customerProvidedKey) && Objects.nonNull(encryptionScope)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Customer provided key and encryption " + "scope cannot both be set"));
        }

        /*
        Implicit and explicit root container access are functionally equivalent, but explicit references are easier
        to read and debug.
         */
        String blobContainerName
            = CoreUtils.isNullOrEmpty(containerName) ? BlobContainerAsyncClient.ROOT_CONTAINER_NAME : containerName;

        BlobServiceVersion serviceVersion = version != null ? version : BlobServiceVersion.getLatest();

        HttpPipeline pipeline = constructPipeline();

        return new BlobContainerAsyncClient(pipeline, endpoint, serviceVersion, accountName, blobContainerName,
            customerProvidedKey, encryptionScope, blobContainerEncryptionScope);
    }

    private HttpPipeline constructPipeline() {
        return (httpPipeline != null)
            ? httpPipeline
            : BuilderHelper.buildPipeline(storageSharedKeyCredential, tokenCredential, azureSasCredential, sasToken,
                endpoint, retryOptions, coreRetryOptions, logOptions, clientOptions, httpClient, perCallPolicies,
                perRetryPolicies, configuration, audience, LOGGER);
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name)
     *
     * @param endpoint URL of the service
     * @return the updated BlobContainerClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    @Override
    public BlobContainerClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            BlobUrlParts parts = BlobUrlParts.parse(url);

            this.accountName = parts.getAccountName();
            this.containerName
                = parts.getBlobContainerName() == null ? this.containerName : parts.getBlobContainerName();
            this.endpoint = BuilderHelper.getEndpoint(parts);

            String sasToken = parts.getCommonSasQueryParameters().encode();
            if (!CoreUtils.isNullOrEmpty(sasToken)) {
                this.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage Blob endpoint url is malformed.", ex));
        }

        return this;
    }

    /**
     * Sets the {@link CustomerProvidedKey customer provided key} that is used to encrypt blob contents on the server.
     *
     * @param customerProvidedKey Customer provided key containing the encryption key information.
     * @return the updated BlobContainerClientBuilder object
     */
    public BlobContainerClientBuilder customerProvidedKey(CustomerProvidedKey customerProvidedKey) {
        if (customerProvidedKey == null) {
            this.customerProvidedKey = null;
        } else {
            this.customerProvidedKey = new CpkInfo().setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }

        return this;
    }

    /**
     * Sets the {@code encryption scope} that is used to encrypt blob contents on the server.
     *
     * @param encryptionScope Encryption scope containing the encryption key information.
     * @return the updated BlobContainerClientBuilder object
     */
    public BlobContainerClientBuilder encryptionScope(String encryptionScope) {
        if (encryptionScope == null) {
            this.encryptionScope = null;
        } else {
            this.encryptionScope = new EncryptionScope().setEncryptionScope(encryptionScope);
        }

        return this;
    }

    /**
     * Sets the {@link BlobContainerEncryptionScope encryption scope} that is used to determine how blob contents are
     * encrypted on the server.
     *
     * @param blobContainerEncryptionScope Encryption scope containing the encryption key information.
     * @return the updated BlobContainerClientBuilder object
     */
    public BlobContainerClientBuilder
        blobContainerEncryptionScope(BlobContainerEncryptionScope blobContainerEncryptionScope) {
        this.blobContainerEncryptionScope = blobContainerEncryptionScope;
        return this;
    }

    /**
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link StorageSharedKeyCredential}.
     * @return the updated BlobContainerClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public BlobContainerClientBuilder credential(StorageSharedKeyCredential credential) {
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.sasToken = null;
        return this;
    }

    /**
     * Sets the {@link AzureNamedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureNamedKeyCredential}.
     * @return the updated BlobContainerClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public BlobContainerClientBuilder credential(AzureNamedKeyCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        return credential(StorageSharedKeyCredential.fromAzureNamedKeyCredential(credential));
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return the updated BlobContainerClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public BlobContainerClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.sasToken = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests. This string should only be the query parameters
     * (with or without a leading '?') and not a full url.
     * @return the updated BlobContainerClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public BlobContainerClientBuilder sasToken(String sasToken) {
        this.sasToken = Objects.requireNonNull(sasToken, "'sasToken' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link AzureSasCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureSasCredential} used to authorize requests sent to the service.
     * @return the updated BlobContainerClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public BlobContainerClientBuilder credential(AzureSasCredential credential) {
        this.azureSasCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        return this;
    }

    /**
     * Clears the credential used to authorize the request.
     *
     * <p>This is for containers that are publicly accessible.</p>
     *
     * @return the updated BlobContainerClientBuilder
     */
    public BlobContainerClientBuilder setAnonymousAccess() {
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        this.azureSasCredential = null;
        this.sasToken = null;
        return this;
    }

    /**
     * Sets the connection string to connect to the service.
     *
     * @param connectionString Connection string of the storage account.
     * @return the updated BlobContainerClientBuilder
     * @throws IllegalArgumentException If {@code connectionString} in invalid.
     */
    @Override
    public BlobContainerClientBuilder connectionString(String connectionString) {
        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, LOGGER);
        StorageEndpoint endpoint = storageConnectionString.getBlobEndpoint();
        if (endpoint == null || endpoint.getPrimaryUri() == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
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
     * Sets the name of the container.
     *
     * @param containerName Name of the container. If the value {@code null} or empty the root container, {@code $root}
     * , will be used.
     * @return the updated BlobContainerClientBuilder object
     */
    public BlobContainerClientBuilder containerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param httpClient The {@link HttpClient} to use for requests.
     * @return the updated BlobContainerClientBuilder object
     */
    @Override
    public BlobContainerClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            LOGGER.info("'httpClient' is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a {@link HttpPipelinePolicy pipeline policy} to apply on each request sent.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param pipelinePolicy A {@link HttpPipelinePolicy pipeline policy}.
     * @return the updated BlobContainerClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public BlobContainerClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null");
        if (pipelinePolicy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(pipelinePolicy);
        } else {
            perRetryPolicies.add(pipelinePolicy);
        }
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions logging configuration} to use when sending and receiving requests to and from
     * the service. If a {@code logLevel} is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param logOptions The {@link HttpLogOptions logging configuration} to use when sending and receiving requests to
     * and from the service.
     * @return the updated BlobContainerClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    @Override
    public BlobContainerClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Gets the default Storage allowlist log headers and query parameters.
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
     * @return the updated BlobContainerClientBuilder object
     */
    @Override
    public BlobContainerClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryOptions {@link RequestRetryOptions}.
     * @return the updated BlobContainerClientBuilder object
     */
    public BlobContainerClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the {@link RetryOptions} for all the requests made through the client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     * <p>
     * Setting this is mutually exclusive with using {@link #retryOptions(RequestRetryOptions)}.
     * Consider using {@link #retryOptions(RequestRetryOptions)} to also set storage specific options.
     *
     * @param retryOptions The {@link RetryOptions} to use for all the requests made through the client.
     * @return the updated BlobContainerClientBuilder object
     */
    @Override
    public BlobContainerClientBuilder retryOptions(RetryOptions retryOptions) {
        this.coreRetryOptions = retryOptions;
        return this;
    }

    /**
     * Allows for setting common properties such as application ID, headers, proxy configuration, etc. Note that it is
     * recommended that this method be called with an instance of the {@link HttpClientOptions}
     * class (a subclass of the {@link ClientOptions} base class). The HttpClientOptions subclass provides more
     * configuration options suitable for HTTP clients, which is applicable for any class that implements this HttpTrait
     * interface.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param clientOptions A configured instance of {@link HttpClientOptions}.
     * @see HttpClientOptions
     * @return the updated BlobContainerClientBuilder object
     * @throws NullPointerException If {@code clientOptions} is {@code null}.
     */
    @Override
    public BlobContainerClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = Objects.requireNonNull(clientOptions, "'clientOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     * <p>
     * The {@link #endpoint(String) endpoint} is not ignored when {@code pipeline} is set.
     *
     * @param httpPipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return the updated BlobContainerClientBuilder object
     */
    @Override
    public BlobContainerClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            LOGGER.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the {@link BlobServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param version {@link BlobServiceVersion} of the service to be used when making requests.
     * @return the updated BlobContainerClientBuilder object
     */
    public BlobContainerClientBuilder serviceVersion(BlobServiceVersion version) {
        this.version = version;
        return this;
    }

    /**
     * Sets the Audience to use for authentication with Azure Active Directory (AAD). The audience is not considered
     * when using a shared key.
     * @param audience {@link BlobAudience} to be used when requesting a token from Azure Active Directory (AAD).
     * @return the updated BlobContainerClientBuilder object
     */
    public BlobContainerClientBuilder audience(BlobAudience audience) {
        this.audience = audience;
        return this;
    }
}
