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
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.file.datalake.implementation.util.BuilderHelper;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.implementation.util.TransformUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link DataLakeFileClient
 * FileClients}, {@link DataLakeFileAsyncClient FileAsyncClients}, {@link DataLakeDirectoryClient DirectoryClients} and
 * {@link DataLakeDirectoryAsyncClient DirectoryAsyncClients}, call {@link #buildFileClient() buildFileClient},
 * {@link #buildFileAsyncClient() buildFileAsyncClient}, {@link #buildDirectoryClient() buildDirectoryClient} and {@link
 * #buildDirectoryAsyncClient() buildDirectoryAsyncClient} respectively to construct an instance of the desired client.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>the endpoint through {@code .endpoint()}, including the file system name and file/directory name, in the format
 * of {@code https://{accountName}.dfs.core.windows.net/{fileSystemName}/{pathName}}.
 * <li>the credential through {@code .credential()} or {@code .connectionString()} if the file system is not publicly
 * accessible.
 * </ul>
 */
@ServiceClientBuilder(serviceClients = {DataLakeFileClient.class, DataLakeFileAsyncClient.class,
    DataLakeDirectoryClient.class, DataLakeDirectoryAsyncClient.class})
public final class DataLakePathClientBuilder implements
    TokenCredentialTrait<DataLakePathClientBuilder>,
    AzureNamedKeyCredentialTrait<DataLakePathClientBuilder>,
    AzureSasCredentialTrait<DataLakePathClientBuilder>,
    HttpTrait<DataLakePathClientBuilder>,
    ConfigurationTrait<DataLakePathClientBuilder>,
    EndpointTrait<DataLakePathClientBuilder> {

    private static final ClientLogger LOGGER = new ClientLogger(DataLakePathClientBuilder.class);
    private final BlobClientBuilder blobClientBuilder;

    private String endpoint;
    private String accountName;
    private String fileSystemName;
    private String pathName;

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
     * Creates a builder instance that is able to configure and construct {@link DataLakeFileClient FileClients}, {@link
     * DataLakeFileAsyncClient FileAsyncClients}, {@link DataLakeDirectoryClient DirectoryClients} and
     * {@link DataLakeDirectoryAsyncClient DirectoryAsyncClients}.
     */
    public DataLakePathClientBuilder() {
        logOptions = getDefaultHttpLogOptions();
        blobClientBuilder = new BlobClientBuilder();
        blobClientBuilder.addPolicy(BuilderHelper.getBlobUserAgentModificationPolicy());
    }

    /**
     * Creates a {@link DataLakeFileClient} based on options set in the builder.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClientBuilder.buildFileClient -->
     * <pre>
     * DataLakeFileClient client = new DataLakePathClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .credential&#40;storageSharedKeyCredential&#41;
     *     .buildFileClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClientBuilder.buildFileClient -->
     *
     * @return a {@link DataLakeFileClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint} or {@code pathName} is {@code null}.
     * @throws IllegalStateException If multiple credentials have been specified.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryOptions(RequestRetryOptions)} have been set.
     */
    public DataLakeFileClient buildFileClient() {
        return new DataLakeFileClient(buildFileAsyncClient(),
            blobClientBuilder.buildClient().getBlockBlobClient());
    }

    /**
     * Creates a {@link DataLakeFileAsyncClient} based on options set in the builder.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClientBuilder.buildFileAsyncClient -->
     * <pre>
     * DataLakeFileAsyncClient client = new DataLakePathClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .credential&#40;storageSharedKeyCredential&#41;
     *     .buildFileAsyncClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClientBuilder.buildFileAsyncClient -->
     *
     * @return a {@link DataLakeFileAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint} or {@code pathName} is {@code null}.
     * @throws IllegalStateException If multiple credentials have been specified.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryOptions(RequestRetryOptions)} have been set.
     */
    public DataLakeFileAsyncClient buildFileAsyncClient() {
        Objects.requireNonNull(pathName, "'pathName' cannot be null.");
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");

        /*
        Implicit and explicit root container access are functionally equivalent, but explicit references are easier
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

        return new DataLakeFileAsyncClient(pipeline, endpoint, serviceVersion, accountName, dataLakeFileSystemName,
            pathName, blobClientBuilder.buildAsyncClient().getBlockBlobAsyncClient(), azureSasCredential);
    }

    /**
     * Creates a {@link DataLakeDirectoryClient} based on options set in the builder.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClientBuilder.buildDirectoryClient -->
     * <pre>
     * DataLakeDirectoryClient client = new DataLakePathClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .credential&#40;storageSharedKeyCredential&#41;
     *     .buildDirectoryClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClientBuilder.buildDirectoryClient -->
     *
     * @return a {@link DataLakeDirectoryClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint} or {@code pathName} is {@code null}.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryOptions(RequestRetryOptions)} have been set.
     */
    public DataLakeDirectoryClient buildDirectoryClient() {
        return new DataLakeDirectoryClient(buildDirectoryAsyncClient(),
            blobClientBuilder.buildClient().getBlockBlobClient());
    }

    /**
     * Creates a {@link DataLakeDirectoryAsyncClient} based on options set in the builder.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClientBuilder.buildDirectoryAsyncClient -->
     * <pre>
     * DataLakeDirectoryAsyncClient client = new DataLakePathClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .credential&#40;storageSharedKeyCredential&#41;
     *     .buildDirectoryAsyncClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClientBuilder.buildDirectoryAsyncClient -->
     *
     * @return a {@link DataLakeDirectoryAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint} or {@code pathName} is {@code null}.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryOptions(RequestRetryOptions)} have been set.
     */
    public DataLakeDirectoryAsyncClient buildDirectoryAsyncClient() {
        Objects.requireNonNull(pathName, "'pathName' cannot be null.");
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");

        /*
        Implicit and explicit root container access are functionally equivalent, but explicit references are easier
        to read and debug.
         */
        String dataLakeFileSystemName = CoreUtils.isNullOrEmpty(fileSystemName)
            ? DataLakeFileSystemAsyncClient.ROOT_FILESYSTEM_NAME
            : fileSystemName;

        DataLakeServiceVersion serviceVersion = version != null ? version : DataLakeServiceVersion.getLatest();

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(
            storageSharedKeyCredential, tokenCredential, azureSasCredential, endpoint,
            retryOptions, coreRetryOptions, logOptions,
            clientOptions, httpClient, perCallPolicies, perRetryPolicies, configuration, LOGGER);

        return new DataLakeDirectoryAsyncClient(pipeline, endpoint, serviceVersion, accountName, dataLakeFileSystemName,
            pathName, blobClientBuilder.buildAsyncClient().getBlockBlobAsyncClient(), azureSasCredential);
    }

    /**
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link StorageSharedKeyCredential}.
     * @return the updated DataLakePathClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public DataLakePathClientBuilder credential(StorageSharedKeyCredential credential) {
        blobClientBuilder.credential(credential);
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.azureSasCredential = null;
        return this;
    }

    /**
     * Sets the {@link AzureNamedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureNamedKeyCredential}.
     * @return the updated DataLakePathClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public DataLakePathClientBuilder credential(AzureNamedKeyCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        return credential(StorageSharedKeyCredential.fromAzureNamedKeyCredential(credential));
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return the updated DataLakePathClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public DataLakePathClientBuilder credential(TokenCredential credential) {
        blobClientBuilder.credential(credential);
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
     * @return the updated DataLakePathClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public DataLakePathClientBuilder sasToken(String sasToken) {
        blobClientBuilder.sasToken(sasToken);
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
     * @return the updated DataLakePathClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public DataLakePathClientBuilder credential(AzureSasCredential credential) {
        blobClientBuilder.credential(credential);
        this.azureSasCredential = Objects.requireNonNull(credential,
            "'credential' cannot be null.");
        return this;
    }

    /**
     * Clears the credential used to authorize the request.
     *
     * <p>This is for paths that are publicly accessible.</p>
     *
     * @return the updated DataLakePathClientBuilder
     */
    public DataLakePathClientBuilder setAnonymousAccess() {
        blobClientBuilder.setAnonymousAccess();
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        this.azureSasCredential = null;
        return this;
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, file system name, path name)
     *
     * <p>If the path name contains special characters, pass in the url encoded version of the path name. </p>
     *
     * <p>If the endpoint is to a file/directory in the root container, this method will fail as it will interpret the
     * path name as the file system name. With only one path element, it is impossible to distinguish between a file
     * system name and a path in the root file system, so it is assumed to be the file system name as this is much more
     * common. When working with paths in the root file system, it is best to set the endpoint to the account url and
     * specify the path name separately using the {@link DataLakePathClientBuilder#pathName(String) pathName} method.
     * </p>
     *
     * @param endpoint URL of the service
     * @return the updated DataLakePathClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    @Override
    public DataLakePathClientBuilder endpoint(String endpoint) {
        // Ensure endpoint provided is dfs endpoint
        endpoint = DataLakeImplUtils.endpointToDesiredEndpoint(endpoint, "dfs", "blob");
        blobClientBuilder.endpoint(DataLakeImplUtils.endpointToDesiredEndpoint(endpoint, "blob", "dfs"));
        try {
            URL url = new URL(endpoint);
            BlobUrlParts parts = BlobUrlParts.parse(url);

            this.accountName = parts.getAccountName();
            this.endpoint = BuilderHelper.getEndpoint(parts);
            this.fileSystemName = parts.getBlobContainerName() == null ? this.fileSystemName
                : parts.getBlobContainerName();
            this.pathName = parts.getBlobName() == null ? this.pathName : Utility.urlEncode(parts.getBlobName());

            String sasToken = parts.getCommonSasQueryParameters().encode();
            if (!CoreUtils.isNullOrEmpty(sasToken)) {
                this.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage DataLake endpoint url is malformed.", ex));
        }
        return this;
    }

    /**
     * Sets the name of the file system that contains the path.
     *
     * @param fileSystemName Name of the file system. If the value {@code null} or empty the root file system,
     * {@code $root}, will be used.
     * @return the updated DataLakePathClientBuilder object
     */
    public DataLakePathClientBuilder fileSystemName(String fileSystemName) {
        blobClientBuilder.containerName(fileSystemName);
        this.fileSystemName = fileSystemName;
        return this;
    }

    /**
     * Sets the name of the file/directory.
     *
     * @param pathName Name of the path. If the path name contains special characters, pass in the url encoded version
     * of the path name.
     * @return the updated DataLakePathClientBuilder object
     * @throws NullPointerException If {@code pathName} is {@code null}
     */
    public DataLakePathClientBuilder pathName(String pathName) {
        blobClientBuilder.blobName(pathName);
        this.pathName = Utility.urlEncode(Utility.urlDecode(Objects.requireNonNull(pathName,
            "'pathName' cannot be null.")));
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
     * @return the updated DataLakePathClientBuilder object
     */
    @Override
    public DataLakePathClientBuilder httpClient(HttpClient httpClient) {
        blobClientBuilder.httpClient(httpClient);
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
     * @return the updated DataLakePathClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public DataLakePathClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        blobClientBuilder.addPolicy(pipelinePolicy);
        Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null");
        if (pipelinePolicy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(pipelinePolicy);
        } else {
            perRetryPolicies.add(pipelinePolicy);
        }
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
     * @return the updated DataLakePathClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    @Override
    public DataLakePathClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        blobClientBuilder.httpLogOptions(logOptions);
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated DataLakePathClientBuilder object
     */
    @Override
    public DataLakePathClientBuilder configuration(Configuration configuration) {
        blobClientBuilder.configuration(configuration);
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryOptions {@link RequestRetryOptions}.
     * @return the updated DataLakePathClientBuilder object.
     */
    public DataLakePathClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        blobClientBuilder.retryOptions(retryOptions);
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
     * @return the updated DataLakePathClientBuilder object
     */
    @Override
    public DataLakePathClientBuilder retryOptions(RetryOptions retryOptions) {
        blobClientBuilder.retryOptions(retryOptions);
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
     * @return the updated DataLakePathClientBuilder object
     * @throws NullPointerException If {@code clientOptions} is {@code null}.
     */
    @Override
    public DataLakePathClientBuilder clientOptions(ClientOptions clientOptions) {
        blobClientBuilder.clientOptions(clientOptions);
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
     * @return the updated DataLakePathClientBuilder object
     */
    @Override
    public DataLakePathClientBuilder pipeline(HttpPipeline httpPipeline) {
        blobClientBuilder.pipeline(httpPipeline);
        if (this.httpPipeline != null && httpPipeline == null) {
            LOGGER.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
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
     * @return the updated DataLakePathClientBuilder object
     */
    public DataLakePathClientBuilder serviceVersion(DataLakeServiceVersion version) {
        blobClientBuilder.serviceVersion(TransformUtils.toBlobServiceVersion(version));
        this.version = version;
        return this;
    }

}
