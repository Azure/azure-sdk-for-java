// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.AzureNamedKeyCredentialTrait;
import com.azure.core.client.traits.AzureSasCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
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
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.file.datalake.implementation.util.BuilderHelper;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.implementation.util.TransformUtils;
import com.azure.storage.file.datalake.models.CustomerProvidedKey;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link
 * DataLakeFileSystemClient FileSystemClients} and {@link DataLakeFileSystemAsyncClient FileSystemAsyncClients}, call
 * {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an
 * instance of the desired client.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>the endpoint through {@code .endpoint()}, including the file system name, in the format of {@code
 * https://{accountName}.dfs.core.windows.net/{fileSystemName}}.
 * <li>the credential through {@code .credential()} or {@code .connectionString()} if the file system is not publicly
 * accessible.
 * </ul>
 */
@ServiceClientBuilder(serviceClients = {DataLakeFileSystemClient.class, DataLakeFileSystemAsyncClient.class})
public class DataLakeFileSystemClientBuilder implements
    TokenCredentialTrait<DataLakeFileSystemClientBuilder>,
    AzureNamedKeyCredentialTrait<DataLakeFileSystemClientBuilder>,
    AzureSasCredentialTrait<DataLakeFileSystemClientBuilder>,
    HttpTrait<DataLakeFileSystemClientBuilder>,
    ConfigurationTrait<DataLakeFileSystemClientBuilder>,
    EndpointTrait<DataLakeFileSystemClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(DataLakeFileSystemClientBuilder.class);

    private final BlobContainerClientBuilder blobContainerClientBuilder;

    private String endpoint;
    private String accountName;
    private String fileSystemName;

    private StorageSharedKeyCredential storageSharedKeyCredential;
    private TokenCredential tokenCredential;
    private AzureSasCredential azureSasCredential;

    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();
    private HttpLogOptions logOptions;
    private RequestRetryOptions retryOptions;
    private RetryOptions coreRetryOptions;
    private HttpPipeline httpPipeline;

    private ClientOptions clientOptions = new ClientOptions();
    private Configuration configuration;
    private DataLakeServiceVersion version;

    /**
     * Creates a builder instance that is able to configure and construct {@link DataLakeFileSystemClient
     * FileSystemClients}
     * and {@link DataLakeFileSystemAsyncClient FileSystemAsyncClients}.
     */
    public DataLakeFileSystemClientBuilder() {
        logOptions = getDefaultHttpLogOptions();
        blobContainerClientBuilder = new BlobContainerClientBuilder();
        blobContainerClientBuilder.addPolicy(BuilderHelper.getBlobUserAgentModificationPolicy());
    }

    /**
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClientBuilder.buildClient -->
     * <pre>
     * DataLakeFileSystemClient client = new DataLakeFileSystemClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .credential&#40;storageSharedKeyCredential&#41;
     *     .buildClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClientBuilder.buildClient -->
     *
     * @return a {@link DataLakeFileSystemClient} created from the configurations in this builder.
     * @throws IllegalStateException If multiple credentials have been specified.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryOptions(RequestRetryOptions)} have been set.
     */
    public DataLakeFileSystemClient buildClient() {
        return new DataLakeFileSystemClient(buildAsyncClient(),
            blobContainerClientBuilder.buildClient());
    }

    /**
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClientBuilder.buildAsyncClient -->
     * <pre>
     * DataLakeFileSystemAsyncClient client = new DataLakeFileSystemClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .credential&#40;storageSharedKeyCredential&#41;
     *     .buildAsyncClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClientBuilder.buildAsyncClient -->
     *
     * @return a {@link DataLakeFileSystemAsyncClient} created from the configurations in this builder.
     * @throws IllegalStateException If multiple credentials have been specified.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryOptions(RequestRetryOptions)} have been set.
     */
    public DataLakeFileSystemAsyncClient buildAsyncClient() {
        /*
        Implicit and explicit root file system access are functionally equivalent, but explicit references are easier
        to read and debug.
         */
        String dataLakeFileSystemName = CoreUtils.isNullOrEmpty(fileSystemName)
            ? DataLakeFileSystemAsyncClient.ROOT_FILESYSTEM_NAME
            : fileSystemName;

        DataLakeServiceVersion serviceVersion = version != null ? version : DataLakeServiceVersion.getLatest();

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(
            storageSharedKeyCredential, tokenCredential, azureSasCredential,
            endpoint, retryOptions, coreRetryOptions, logOptions,
            clientOptions, httpClient, perCallPolicies, perRetryPolicies, configuration, LOGGER);

        return new DataLakeFileSystemAsyncClient(pipeline, endpoint, serviceVersion, accountName,
            dataLakeFileSystemName, blobContainerClientBuilder.buildAsyncClient(), azureSasCredential);
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, file system name)
     *
     * @param endpoint URL of the service
     * @return the updated DataLakeFileSystemClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    @Override
    public DataLakeFileSystemClientBuilder endpoint(String endpoint) {
        // Ensure endpoint provided is dfs endpoint
        endpoint = DataLakeImplUtils.endpointToDesiredEndpoint(endpoint, "dfs", "blob");
        blobContainerClientBuilder.endpoint(DataLakeImplUtils.endpointToDesiredEndpoint(endpoint, "blob", "dfs"));
        try {
            URL url = new URL(endpoint);
            BlobUrlParts parts = BlobUrlParts.parse(url);

            this.accountName = parts.getAccountName();
            this.fileSystemName = parts.getBlobContainerName() == null ? this.fileSystemName
                : parts.getBlobContainerName();
            this.endpoint = BuilderHelper.getEndpoint(parts);

            String sasToken = parts.getCommonSasQueryParameters().encode();
            if (!CoreUtils.isNullOrEmpty(sasToken)) {
                this.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage Datalake endpoint url is malformed.", ex));
        }

        return this;
    }

    /**
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link StorageSharedKeyCredential}.
     * @return the updated DataLakeFileSystemClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public DataLakeFileSystemClientBuilder credential(StorageSharedKeyCredential credential) {
        blobContainerClientBuilder.credential(credential);
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.azureSasCredential = null;
        return this;
    }

    /**
     * Sets the {@link AzureNamedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureNamedKeyCredential}.
     * @return the updated DataLakeFileSystemClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public DataLakeFileSystemClientBuilder credential(AzureNamedKeyCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        return credential(StorageSharedKeyCredential.fromAzureNamedKeyCredential(credential));
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return the updated DataLakeFileSystemClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public DataLakeFileSystemClientBuilder credential(TokenCredential credential) {
        blobContainerClientBuilder.credential(credential);
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.azureSasCredential = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests. This string should only be the query parameters
     * (with or without a leading '?') and not a full url.
     * @return the updated DataLakeFileSystemClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public DataLakeFileSystemClientBuilder sasToken(String sasToken) {
        blobContainerClientBuilder.sasToken(sasToken);
        this.azureSasCredential = new AzureSasCredential(Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null."));
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link AzureSasCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureSasCredential} used to authorize requests sent to the service.
     * @return the updated DataLakeFileSystemClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public DataLakeFileSystemClientBuilder credential(AzureSasCredential credential) {
        blobContainerClientBuilder.credential(credential);
        this.azureSasCredential = Objects.requireNonNull(credential,
            "'credential' cannot be null.");
        return this;
    }

    /**
     * Clears the credential used to authorize the request.
     *
     * <p>This is for file systems that are publicly accessible.</p>
     *
     * @return the updated DataLakeFileSystemClientBuilder
     */
    public DataLakeFileSystemClientBuilder setAnonymousAccess() {
        blobContainerClientBuilder.setAnonymousAccess();
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        this.azureSasCredential = null;
        return this;
    }

    /**
     * Sets the name of the file system.
     *
     * @param fileSystemName Name of the file system. If the value {@code null} or empty the root file system,
     * {@code $root}, will be used.
     * @return the updated DataLakeFileSystemClientBuilder object
     */
    public DataLakeFileSystemClientBuilder fileSystemName(String fileSystemName) {
        blobContainerClientBuilder.containerName(fileSystemName);
        this.fileSystemName = fileSystemName;
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
     * @return the updated DataLakeFileSystemClientBuilder object
     */
    @Override
    public DataLakeFileSystemClientBuilder httpClient(HttpClient httpClient) {
        blobContainerClientBuilder.httpClient(httpClient);
        if (this.httpClient != null && httpClient == null) {
            LOGGER.info("'httpClient' is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
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
     * @return the updated DataLakeFileSystemClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public DataLakeFileSystemClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        blobContainerClientBuilder.addPolicy(pipelinePolicy);
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
     * @return the updated DataLakeFileSystemClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    @Override
    public DataLakeFileSystemClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        blobContainerClientBuilder.httpLogOptions(logOptions);
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated DataLakeFileSystemClientBuilder object
     */
    @Override
    public DataLakeFileSystemClientBuilder configuration(Configuration configuration) {
        blobContainerClientBuilder.configuration(configuration);
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryOptions {@link RequestRetryOptions}.
     * @return the updated DataLakeFileSystemClientBuilder object.
     */
    public DataLakeFileSystemClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        blobContainerClientBuilder.retryOptions(retryOptions);
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
     * @return the updated DataLakeFileSystemClientBuilder object
     */
    @Override
    public DataLakeFileSystemClientBuilder retryOptions(RetryOptions retryOptions) {
        blobContainerClientBuilder.retryOptions(retryOptions);
        this.coreRetryOptions = retryOptions;
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
     * @return the updated DataLakeFileSystemClientBuilder object
     */
    @Override
    public DataLakeFileSystemClientBuilder pipeline(HttpPipeline httpPipeline) {
        blobContainerClientBuilder.pipeline(httpPipeline);
        if (this.httpPipeline != null && httpPipeline == null) {
            LOGGER.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
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
     * @return the updated DataLakeFileSystemClientBuilder object
     * @throws NullPointerException If {@code clientOptions} is {@code null}.
     */
    @Override
    public DataLakeFileSystemClientBuilder clientOptions(ClientOptions clientOptions) {
        blobContainerClientBuilder.clientOptions(clientOptions);
        this.clientOptions = Objects.requireNonNull(clientOptions, "'clientOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link DataLakeServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param version {@link DataLakeServiceVersion} of the service to be used when making requests.
     * @return the updated DataLakeFileSystemClientBuilder object
     */
    public DataLakeFileSystemClientBuilder serviceVersion(DataLakeServiceVersion version) {
        blobContainerClientBuilder.serviceVersion(TransformUtils.toBlobServiceVersion(version));
        this.version = version;
        return this;
    }

    /**
     * Sets the {@link CustomerProvidedKey customer provided key} that is used to encrypt file contents on the server.
     *
     * @param customerProvidedKey Customer provided key containing the encryption key information.
     * @return the updated DataLakeFileSystemClientBuilder object
     */
    public DataLakeFileSystemClientBuilder customerProvidedKey(CustomerProvidedKey customerProvidedKey) {
        if (customerProvidedKey == null) {
            blobContainerClientBuilder.customerProvidedKey(null);
        } else {
            blobContainerClientBuilder.customerProvidedKey(Transforms.toBlobCustomerProvidedKey(customerProvidedKey));
        }

        return this;
    }
}
