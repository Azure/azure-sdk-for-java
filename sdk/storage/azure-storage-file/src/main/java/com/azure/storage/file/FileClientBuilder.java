// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.implementation.policy.SasTokenCredentialPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;
import com.azure.storage.file.implementation.AzureFileStorageBuilder;
import com.azure.storage.file.implementation.AzureFileStorageImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link FileClient
 * FileClients}, {@link FileAsyncClient FileAsyncClients}, {@link DirectoryClient DirectoryClients}, and {@link
 * DirectoryAsyncClient DirectoryAsyncClients}. Calling {@link FileClientBuilder#buildFileClient() buildFileClient},
 * {@link FileClientBuilder#buildFileAsyncClient() buildFileAsyncClient},
 * {@link FileClientBuilder#buildDirectoryClient() buildDirectoryClient}, or
 * {@link FileClientBuilder#buildDirectoryAsyncClient() buildDirectoryAsyncClient} constructs
 * an instance of {@link FileClient}, {@link FileAsyncClient}, {@link DirectoryClient}, or {@link DirectoryAsyncClient}
 * respectively.
 *
 * <p>The client needs the endpoint of the Azure Storage File service, name of the share, and authorization credential.
 * {@link FileClientBuilder#endpoint(String) endpoint} gives the builder the endpoint and may give the builder the
 * {@link FileClientBuilder#shareName(String)}, {@link FileClientBuilder#resourcePath(String)} and a
 * {@link #sasToken(String) SAS token} that authorizes the client.</p>
 *
 * <p><strong>Instantiating a synchronous File Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.file.fileClient.instantiation.sastoken}
 *
 * <p><strong>Instantiating an Asynchronous File Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.file.directoryClient.instantiation.sastoken}
 *
 * <p>If the {@code endpoint} doesn't contain the query parameters to construct a SAS token it may be set using
 * {@link #sasToken(String) sasToken}.</p>
 *
 * {@codesnippet com.azure.storage.file.fileClient.instantiation.credential}
 *
 * {@codesnippet com.azure.storage.file.fileAsyncClient.instantiation.credential}
 *
 * <p>Another way to authenticate the client is using a {@link SharedKeyCredential}. To create a SharedKeyCredential
 * a connection string from the Storage File service must be used. Set the SharedKeyCredential with {@link
 * FileClientBuilder#connectionString(String) connectionString}. If the builder has both a SAS token and
 * SharedKeyCredential the SharedKeyCredential will be preferred when authorizing requests sent to the service.</p>
 *
 * <p><strong>Instantiating a synchronous File Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.file.directoryClient.instantiation.connectionstring}
 *
 * <p><strong>Instantiating an Asynchronous File Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.file.directoryAsyncClient.instantiation.connectionstring}
 *
 * @see FileClient
 * @see FileAsyncClient
 * @see SharedKeyCredential
 */
@ServiceClientBuilder(serviceClients = {
    FileClient.class, FileAsyncClient.class,
    DirectoryClient.class, DirectoryAsyncClient.class
})
public class FileClientBuilder {
    private final ClientLogger logger = new ClientLogger(FileClientBuilder.class);

    private String endpoint;
    private String accountName;
    private String shareName;
    private String shareSnapshot;
    private String resourcePath;

    private SharedKeyCredential sharedKeyCredential;
    private SasTokenCredential sasTokenCredential;

    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
    private HttpLogOptions logOptions = new HttpLogOptions();
    private RequestRetryOptions retryOptions = new RequestRetryOptions();
    private HttpPipeline httpPipeline;

    private Configuration configuration;
    private FileServiceVersion version;

    /**
     * Creates a builder instance that is able to configure and construct {@link FileClient FileClients} and {@link
     * FileAsyncClient FileAsyncClients}.
     */
    public FileClientBuilder() {
    }

    private AzureFileStorageImpl constructImpl() {
        Objects.requireNonNull(shareName, "'shareName' cannot be null.");
        Objects.requireNonNull(resourcePath, "'resourcePath' cannot be null.");
        FileServiceVersion serviceVersion = version != null ? version : FileServiceVersion.getLatest();

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(() -> {
            if (sharedKeyCredential != null) {
                return new SharedKeyCredentialPolicy(sharedKeyCredential);
            } else if (sasTokenCredential != null) {
                return new SasTokenCredentialPolicy(sasTokenCredential);
            } else {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Credentials are required for authorization"));
            }
        }, retryOptions, logOptions, httpClient, additionalPolicies, configuration, serviceVersion);

        return new AzureFileStorageBuilder()
            .url(endpoint)
            .pipeline(pipeline)
            .version(serviceVersion.getVersion())
            .build();
    }

    /**
     * Creates a {@link DirectoryAsyncClient} based on options set in the builder. Every time
     * {@code buildFileAsyncClient()} is called a new instance of {@link DirectoryAsyncClient} is created.
     *
     * <p>
     * If {@link FileClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * FileClientBuilder#endpoint(String) endpoint} are used to create the {@link DirectoryAsyncClient client}. All
     * other builder settings are ignored.
     * </p>
     *
     * @return A ShareAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code shareName} is {@code null} or {@code shareName} is {@code null}.
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link #sasToken(String) SAS token}
     * has been set.
     */
    public DirectoryAsyncClient buildDirectoryAsyncClient() {
        return new DirectoryAsyncClient(constructImpl(), shareName, resourcePath, shareSnapshot, accountName);
    }

    /**
     * Creates a {@link DirectoryClient} based on options set in the builder. Every time {@code buildDirectoryClient()}
     * is called a new instance of {@link DirectoryClient} is created.
     *
     * <p>
     * If {@link FileClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * FileClientBuilder#endpoint(String) endpoint} are used to create the {@link DirectoryClient client}. All other
     * builder settings are ignored.
     * </p>
     *
     * @return A DirectoryClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint}, {@code shareName} or {@code directoryPath} is {@code null}.
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link #sasToken(String) SAS token}
     * has been set.
     */
    public DirectoryClient buildDirectoryClient() {
        return new DirectoryClient(this.buildDirectoryAsyncClient());
    }

    /**
     * Creates a {@link FileAsyncClient} based on options set in the builder. Every time {@code buildFileAsyncClient()}
     * is called a new instance of {@link FileAsyncClient} is created.
     *
     * <p>
     * If {@link FileClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * FileClientBuilder#endpoint(String) endpoint} are used to create the {@link FileAsyncClient client}. All other
     * builder settings are ignored.
     * </p>
     *
     * @return A ShareAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code shareName} is {@code null} or the (@code resourcePath) is {@code null}.
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link #sasToken(String) SAS token}
     * has been set.
     */
    public FileAsyncClient buildFileAsyncClient() {

        return new FileAsyncClient(constructImpl(), shareName, resourcePath, shareSnapshot, accountName);
    }

    /**
     * Creates a {@link FileClient} based on options set in the builder. Every time {@code buildFileClient()} is called
     * a new instance of {@link FileClient} is created.
     *
     * <p>
     * If {@link FileClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * FileClientBuilder#endpoint(String) endpoint} are used to create the {@link FileClient client}. All other builder
     * settings are ignored.
     * </p>
     *
     * @return A FileClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint}, {@code shareName} or {@code resourcePath} is {@code null}.
     * @throws IllegalStateException If neither a {@link SharedKeyCredential} or {@link #sasToken(String) SAS token}
     * has been set.
     */
    public FileClient buildFileClient() {
        return new FileClient(this.buildFileAsyncClient());
    }

    /**
     * Sets the endpoint for the Azure Storage File instance that the client will interact with.
     *
     * <p>The first path segment, if the endpoint contains path segments, will be assumed to be the name of the share
     * that the client will interact with. Rest of the path segments should be the path of the file. It mush end up with
     * the file name if more segments exist.</p>
     *
     * <p>Query parameters of the endpoint will be parsed in an attempt to generate a SAS token to authenticate
     * requests sent to the service.</p>
     *
     * @param endpoint The URL of the Azure Storage File instance to send service requests to and receive responses
     * from.
     * @return the updated FileClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is an invalid URL
     */
    public FileClientBuilder endpoint(String endpoint) {
        try {
            URL fullUrl = new URL(endpoint);
            this.endpoint = fullUrl.getProtocol() + "://" + fullUrl.getHost();

            this.accountName = Utility.getAccountName(fullUrl);

            // Attempt to get the share name and file path from the URL passed
            String[] pathSegments = fullUrl.getPath().split("/");
            int length = pathSegments.length;
            this.shareName = length >= 2 ? pathSegments[1] : this.shareName;
            String[] filePathParams = length >= 3 ? Arrays.copyOfRange(pathSegments, 2, length) : null;
            this.resourcePath = filePathParams != null ? String.join("/", filePathParams) : this.resourcePath;

            // Attempt to get the SAS token from the URL passed
            String sasToken = new FileServiceSasQueryParameters(
                Utility.parseQueryStringSplitValues(fullUrl.getQuery()), false).encode();
            if (!ImplUtils.isNullOrEmpty(sasToken)) {
                sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage File endpoint url is malformed."));
        }

        return this;
    }

    /**
     * Sets the share that the constructed clients will interact with
     *
     * @param shareName Name of the share
     * @return the updated FileClientBuilder object
     * @throws NullPointerException If {@code shareName} is {@code null}.
     */
    public FileClientBuilder shareName(String shareName) {
        this.shareName = shareName;
        return this;
    }

    /**
     * Sets the shareSnapshot that the constructed clients will interact with. This shareSnapshot must be linked to the
     * share that has been specified in the builder.
     *
     * @param snapshot Identifier of the shareSnapshot
     * @return the updated FileClientBuilder object
     * @throws NullPointerException If {@code shareSnapshot} is {@code null}.
     */
    public FileClientBuilder snapshot(String snapshot) {
        this.shareSnapshot = snapshot;
        return this;
    }

    /**
     * Sets the file that the constructed clients will interact with
     *
     * @param resourcePath Path of the file (or directory).
     * @return the updated FileClientBuilder object
     * @throws NullPointerException If {@code resourcePath} is {@code null}.
     */
    public FileClientBuilder resourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
        return this;
    }

    /**
     * Sets the {@link SharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated FileClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public FileClientBuilder credential(SharedKeyCredential credential) {
        this.sharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     * @return the updated FileClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public FileClientBuilder sasToken(String sasToken) {
        this.sasTokenCredential = new SasTokenCredential(Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null."));
        this.sharedKeyCredential = null;
        return this;
    }

    /**
     * Constructs a {@link SharedKeyCredential} used to authorize requests sent to the service. Additionally, if the
     * connection string contains `DefaultEndpointsProtocol` and `EndpointSuffix` it will set the {@link
     * #endpoint(String) endpoint}.
     *
     * @param connectionString Connection string of the storage account.
     * @return the updated FileClientBuilder
     * @throws IllegalArgumentException If {@code connectionString} doesn't contain `AccountName` or `AccountKey`.
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     */
    public FileClientBuilder connectionString(String connectionString) {
        BuilderHelper.configureConnectionString(connectionString, (accountName) -> this.accountName = accountName,
            this::credential, this::endpoint, logger);

        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated FileClientBuilder object
     */
    public FileClientBuilder httpClient(HttpClient httpClient) {
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
     * @return the updated FileClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public FileClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated FileClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public FileClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated FileClientBuilder object
     */
    public FileClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions The options used to configure retry behavior.
     * @return the updated FileClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public FileClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated FileClientBuilder object
     */
    public FileClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the {@link FileServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link FileServiceVersion} of the service to be used when making requests.
     * @return the updated FileClientBuilder object
     */
    public FileClientBuilder serviceVersion(FileServiceVersion version) {
        this.version = version;
        return this;
    }
}
