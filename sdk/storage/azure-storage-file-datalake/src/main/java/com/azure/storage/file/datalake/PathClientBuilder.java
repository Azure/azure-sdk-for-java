// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;


import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.implementation.policy.SasTokenCredentialPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientBuilder;
import com.azure.storage.file.datalake.implementation.util.BuilderHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link FileClient
 * FileClients}, {@link FileAsyncClient FileAsyncClients}, {@link DirectoryClient DirectoryClients} and
 * {@link DirectoryAsyncClient DirectoryAsyncClients}, call {@link #buildFileClient() buildFileClient},
 * {@link #buildFileAsyncClient() buildFileAsyncClient}, {@link #buildDirectoryClient() buildDirectoryClient} and {@link
 * #buildDirectoryAsyncClient() buildDirectoryAsyncClient} respectively to construct an instance of the desired client.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>the endpoint through {@code .endpoint()}, including the file system name and file/directory name, in the format
 * of {@code https://{accountName}.dfs.core.windows.net/{fileSystemName}/{pathName}}.
 * <li>the credential through {@code .credential()} or {@code .connectionString()} if the container is not publicly
 * accessible.
 * </ul>
 */
@ServiceClientBuilder(serviceClients = {FileClient.class, FileAsyncClient.class, DirectoryClient.class,
    DirectoryAsyncClient.class})
public final class PathClientBuilder {

    private final ClientLogger logger = new ClientLogger(PathClientBuilder.class);
    private BlobClientBuilder blobClientBuilder;

    private String endpoint;
    private String accountName;
    private String fileSystemName;
    private String pathName;

    private StorageSharedKeyCredential storageSharedKeyCredential;
    private TokenCredential tokenCredential;
    private SasTokenCredential sasTokenCredential;

    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
    private HttpLogOptions logOptions = new HttpLogOptions();
    private RequestRetryOptions retryOptions = new RequestRetryOptions();
    private HttpPipeline httpPipeline;

    private Configuration configuration;
    private DataLakeServiceVersion version;

    /**
     * Creates a builder instance that is able to configure and construct {@link FileClient FileClients}, {@link
     * FileAsyncClient FileAsyncClients}, {@link DirectoryClient DirectoryClients} and
     * {@link DirectoryAsyncClient DirectoryAsyncClients}.
     */
    public PathClientBuilder() {
        blobClientBuilder = new BlobClientBuilder();
    }

    /**
     * Creates a {@link FileClient} based on options set in the builder.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClientBuilder.buildFileClient}
     *
     * @return a {@link FileClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint} or {@code pathName} is {@code null}.
     */
    public FileClient buildFileClient() {
        return new FileClient(buildFileAsyncClient(), blobClientBuilder.buildClient().getBlockBlobClient());
    }

    /**
     * Creates a {@link FileAsyncClient} based on options set in the builder. 
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClientBuilder.buildFileAsyncClient}
     *
     * @return a {@link FileAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint} or {@code pathName} is {@code null}.
     */
    public FileAsyncClient buildFileAsyncClient() {
        Objects.requireNonNull(pathName, "'pathName' cannot be null.");
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");

        /*
        Implicit and explicit root container access are functionally equivalent, but explicit references are easier
        to read and debug.
         */
        String dataLakeFileSystemName = ImplUtils.isNullOrEmpty(fileSystemName)
            ? FileSystemAsyncClient.ROOT_FILESYSTEM_NAME
            : fileSystemName;

        DataLakeServiceVersion serviceVersion = version != null ? version : DataLakeServiceVersion.getLatest();

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(() -> {
                if (storageSharedKeyCredential != null) {
                    return new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential);
                } else if (tokenCredential != null) {
                    return new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", endpoint));
                } else if (sasTokenCredential != null) {
                    return new SasTokenCredentialPolicy(sasTokenCredential);
                } else {
                    return null;
                }
            }, retryOptions, logOptions, httpClient, additionalPolicies, configuration, serviceVersion);

        return new FileAsyncClient(pipeline, String.format("%s/%s/%s", endpoint, dataLakeFileSystemName, pathName),
            serviceVersion, accountName, dataLakeFileSystemName, pathName,
            blobClientBuilder.buildAsyncClient().getBlockBlobAsyncClient());
    }

    /**
     * Creates a {@link DirectoryClient} based on options set in the builder.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClientBuilder.buildDirectoryClient}
     *
     * @return a {@link DirectoryClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint} or {@code pathName} is {@code null}.
     */
    public DirectoryClient buildDirectoryClient() {
        return new DirectoryClient(buildDirectoryAsyncClient(), blobClientBuilder.buildClient().getBlockBlobClient());
    }

    /**
     * Creates a {@link DirectoryAsyncClient} based on options set in the builder.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClientBuilder.buildDirectoryAsyncClient}
     *
     * @return a {@link DirectoryAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint} or {@code pathName} is {@code null}.
     */
    public DirectoryAsyncClient buildDirectoryAsyncClient() {
        Objects.requireNonNull(pathName, "'pathName' cannot be null.");
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");

        /*
        Implicit and explicit root container access are functionally equivalent, but explicit references are easier
        to read and debug.
         */
        String dataLakeFileSystemName = ImplUtils.isNullOrEmpty(fileSystemName)
            ? FileSystemAsyncClient.ROOT_FILESYSTEM_NAME
            : fileSystemName;

        DataLakeServiceVersion serviceVersion = version != null ? version : DataLakeServiceVersion.getLatest();

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(() -> {
            if (storageSharedKeyCredential != null) {
                return new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential);
            } else if (tokenCredential != null) {
                return new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", endpoint));
            } else if (sasTokenCredential != null) {
                return new SasTokenCredentialPolicy(sasTokenCredential);
            } else {
                return null;
            }
        }, retryOptions, logOptions, httpClient, additionalPolicies, configuration, serviceVersion);

        return new DirectoryAsyncClient(pipeline, String.format("%s/%s/%s", endpoint, dataLakeFileSystemName, pathName),
            serviceVersion, accountName, dataLakeFileSystemName, pathName,
            blobClientBuilder.buildAsyncClient().getBlockBlobAsyncClient());
    }


    PathClient buildPathClient() {
        return new PathClient(buildPathAsyncClient(), blobClientBuilder.buildClient().getBlockBlobClient());
    }

    PathAsyncClient buildPathAsyncClient() {
        Objects.requireNonNull(pathName, "'pathName' cannot be null.");
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");

        /*
        Implicit and explicit root container access are functionally equivalent, but explicit references are easier
        to read and debug.
         */
        String dataLakeFileSystemName = ImplUtils.isNullOrEmpty(fileSystemName)
            ? FileSystemAsyncClient.ROOT_FILESYSTEM_NAME
            : fileSystemName;

        DataLakeServiceVersion serviceVersion = version != null ? version : DataLakeServiceVersion.getLatest();

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(() -> {
            if (storageSharedKeyCredential != null) {
                return new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential);
            } else if (tokenCredential != null) {
                return new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", endpoint));
            } else if (sasTokenCredential != null) {
                return new SasTokenCredentialPolicy(sasTokenCredential);
            } else {
                return null;
            }
        }, retryOptions, logOptions, httpClient, additionalPolicies, configuration, serviceVersion);

        return new PathAsyncClient(pipeline, String.format("%s/%s/%s", endpoint, dataLakeFileSystemName, pathName),
            serviceVersion, accountName, dataLakeFileSystemName, pathName,
            blobClientBuilder.buildAsyncClient().getBlockBlobAsyncClient());
    }

    /**
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated PathClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public PathClientBuilder credential(StorageSharedKeyCredential credential) {
        blobClientBuilder.credential(credential);
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated PathClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public PathClientBuilder credential(TokenCredential credential) {
        blobClientBuilder.credential(credential);
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     * @return the updated PathClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public PathClientBuilder sasToken(String sasToken) {
        blobClientBuilder.sasToken(sasToken);
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
     * @return the updated PathClientBuilder
     */
    public PathClientBuilder setAnonymousAccess() {
        blobClientBuilder.setAnonymousAccess();
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, file system name, path name)
     *
     * <p>If the endpoint is to a file/directory in the root container, this method will fail as it will interpret the
     * path name as the file system name. With only one path element, it is impossible to distinguish between a file
     * system name and a path in the root file system, so it is assumed to be the file system name as this is much more
     * common. When working with paths in the root file system, it is best to set the endpoint to the account url and
     * specify the path name separately using the {@link PathClientBuilder#pathName(String) pathName} method.</p>
     *
     * @param endpoint URL of the service
     * @return the updated PathClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    public PathClientBuilder endpoint(String endpoint) {
        blobClientBuilder.endpoint(Transforms.endpointToDesiredEndpoint(endpoint, "blob", "dfs"));
        try {
            URL url = new URL(endpoint);
            BlobUrlParts parts = BlobUrlParts.parse(url);

            this.accountName = parts.getAccountName();
            this.endpoint = parts.getScheme() + "://" + parts.getHost();
            this.fileSystemName = parts.getBlobContainerName();
            this.pathName = parts.getBlobName();

            String sasToken = parts.getSasQueryParameters().encode();
            if (!ImplUtils.isNullOrEmpty(sasToken)) {
                this.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage DataLake endpoint url is malformed."));
        }
        return this;
    }

    /**
     * Sets the name of the file system that contains the path.
     *
     * @param fileSystemName Name of the file system. If the value {@code null} or empty the root file system,
     * {@code $root}, will be used.
     * @return the updated PathClientBuilder object
     */
    public PathClientBuilder fileSystemName(String fileSystemName) {
        blobClientBuilder.containerName(fileSystemName);
        this.fileSystemName = fileSystemName;
        return this;
    }

    /**
     * Sets the name of the file/directory.
     *
     * @param pathName Name of the path.
     * @return the updated PathClientBuilder object
     * @throws NullPointerException If {@code pathName} is {@code null}
     */
    public PathClientBuilder pathName(String pathName) {
        blobClientBuilder.blobName(pathName);
        this.pathName = Objects.requireNonNull(pathName, "'pathName' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated PathClientBuilder object
     */
    public PathClientBuilder httpClient(HttpClient httpClient) {
        blobClientBuilder.httpClient(httpClient);
        if (this.httpClient != null && httpClient == null) {
            logger.info("'httpClient' is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent.
     *
     * @param pipelinePolicy a pipeline policy
     * @return the updated PathClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public PathClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        blobClientBuilder.addPolicy(pipelinePolicy);
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated PathClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public PathClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        blobClientBuilder.httpLogOptions(logOptions);
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated PathClientBuilder object
     */
    public PathClientBuilder configuration(Configuration configuration) {
        blobClientBuilder.configuration(configuration);
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions The options used to configure retry behavior.
     * @return the updated PathClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public PathClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        blobClientBuilder.retryOptions(retryOptions);
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated PathClientBuilder object
     */
    public PathClientBuilder pipeline(HttpPipeline httpPipeline) {
        blobClientBuilder.pipeline(httpPipeline);
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the {@link DataLakeServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link DataLakeServiceVersion} of the service to be used when making requests.
     * @return the updated PathClientBuilder object
     */
    public PathClientBuilder serviceVersion(DataLakeServiceVersion version) {
        this.version = version;
        return this;
    }

}
