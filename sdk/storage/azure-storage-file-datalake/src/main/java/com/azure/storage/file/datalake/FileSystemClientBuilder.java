package com.azure.storage.file.datalake;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.implementation.policy.SasTokenCredentialPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientBuilder;
import com.azure.storage.file.datalake.implementation.util.BuilderHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link
 * FileSystemClient FileSystemClients} and {@link FileSystemAsyncClient FileSystemAsyncClients}, call
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
@ServiceClientBuilder(serviceClients = {FileSystemClient.class, FileSystemAsyncClient.class})
public class FileSystemClientBuilder {
    private final ClientLogger logger = new ClientLogger(FileSystemClientBuilder.class);
    private BlobContainerClientBuilder blobContainerClientBuilder;

    private String endpoint;
    private String accountName;
    private String fileSystemName;
    
    private SharedKeyCredential sharedKeyCredential;
    private TokenCredential tokenCredential;
    private SasTokenCredential sasTokenCredential;

    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
    private HttpLogOptions logOptions = new HttpLogOptions();
    private RequestRetryOptions retryOptions = new RequestRetryOptions();
    private HttpPipeline httpPipeline;

    private Configuration configuration;

    /**
     * Creates a builder instance that is able to configure and construct {@link FileSystemClient FileSystemClients}
     * and {@link FileSystemAsyncClient FileSystemAsyncClients}.
     */
    public FileSystemClientBuilder() {
        blobContainerClientBuilder = new BlobContainerClientBuilder();
    }

    /**
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClientBuilder.buildClient}
     *
     * @return a {@link FileSystemClient} created from the configurations in this builder.
     */
    public FileSystemClient buildClient() {
        return new FileSystemClient(blobContainerClientBuilder.buildClient(), buildAsyncClient());
    }

    /**
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClientBuilder.buildAsyncClient}
     *
     * @return a {@link FileSystemAsyncClient} created from the configurations in this builder.
     */
    public FileSystemAsyncClient buildAsyncClient() {
        /*
        Implicit and explicit root file system access are functionally equivalent, but explicit references are easier
        to read and debug.
         */
        if (Objects.isNull(fileSystemName) || fileSystemName.isEmpty()) {
            fileSystemName = FileSystemAsyncClient.ROOT_FILESYSTEM_NAME;
        }

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(() -> {
            if (sharedKeyCredential != null) {
                return new SharedKeyCredentialPolicy(sharedKeyCredential);
            } else if (tokenCredential != null) {
                return new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", endpoint));
            } else if (sasTokenCredential != null) {
                return new SasTokenCredentialPolicy(sasTokenCredential);
            } else {
                return null;
            }
        }, retryOptions, logOptions, httpClient, additionalPolicies, configuration);

        return new FileSystemAsyncClient(blobContainerClientBuilder.buildAsyncClient(),
            new DataLakeStorageClientBuilder()
                .url(String.format("%s/%s", endpoint, fileSystemName))
                .fileSystem(fileSystemName)
                .pipeline(pipeline)
                .build(), accountName, fileSystemName);
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, file system name)
     *
     * @param endpoint URL of the service
     * @return the updated FileSystemClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    public FileSystemClientBuilder endpoint(String endpoint) {
        blobContainerClientBuilder.endpoint(Transforms.endpointToDesiredEndpoint(endpoint, "blob", "dfs"));
        try {
            URL url = new URL(endpoint);
            BlobUrlParts parts = BlobUrlParts.parse(url);

            this.endpoint = parts.getScheme() + "://" + parts.getHost();
            this.fileSystemName = parts.getBlobContainerName();

            String sasToken = parts.getSasQueryParameters().encode();
            if (!ImplUtils.isNullOrEmpty(sasToken)) {
                this.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage Blob endpoint url is malformed."));
        }

        return this;
    }

    /**
     * Sets the {@link SharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated FileSystemClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public FileSystemClientBuilder credential(SharedKeyCredential credential) {
        blobContainerClientBuilder.credential(credential);
        this.sharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated FileSystemClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public FileSystemClientBuilder credential(TokenCredential credential) {
        blobContainerClientBuilder.credential(credential);
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.sharedKeyCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     * @return the updated FileSystemClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public FileSystemClientBuilder sasToken(String sasToken) {
        blobContainerClientBuilder.sasToken(sasToken);
        this.sasTokenCredential = new SasTokenCredential(Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null."));
        this.sharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Clears the credential used to authorize the request.
     *
     * <p>This is for file systems that are publicly accessible.</p>
     *
     * @return the updated FileSystemClientBuilder
     */
    public FileSystemClientBuilder setAnonymousAccess() {
        blobContainerClientBuilder.setAnonymousAccess();
        this.sharedKeyCredential = null;
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Constructs a {@link SharedKeyCredential} used to authorize requests sent to the service. Additionally, if the
     * connection string contains `DefaultEndpointsProtocol` and `EndpointSuffix` it will set the {@link
     * #endpoint(String) endpoint}.
     *
     * @param connectionString Connection string of the storage account.
     * @return the updated FileSystemClientBuilder
     * @throws IllegalArgumentException If {@code connectionString} doesn't contain `AccountName` or `AccountKey`.
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     */
    public FileSystemClientBuilder connectionString(String connectionString) {
        blobContainerClientBuilder.connectionString(connectionString);
        BuilderHelper.configureConnectionString(connectionString, (accountName) -> this.accountName = accountName,
            this::credential, this::endpoint, logger);

        return this;
    }

    /**
     * Sets the name of the file system.
     *
     * @param fileSystemName Name of the file system. If the value {@code null} or empty the root file system,
     * {@code $root}, will be used.
     * @return the updated FileSystemClientBuilder object
     */
    public FileSystemClientBuilder fileSystemName(String fileSystemName) {
        blobContainerClientBuilder.containerName(fileSystemName);
        this.fileSystemName = fileSystemName;
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated FileSystemClientBuilder object
     */
    public FileSystemClientBuilder httpClient(HttpClient httpClient) {
        blobContainerClientBuilder.httpClient(httpClient);
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
     * @return the updated FileSystemClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public FileSystemClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        blobContainerClientBuilder.addPolicy(pipelinePolicy);
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated FileSystemClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public FileSystemClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        blobContainerClientBuilder.httpLogOptions(logOptions);
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated FileSystemClientBuilder object
     */
    public FileSystemClientBuilder configuration(Configuration configuration) {
        blobContainerClientBuilder.configuration(configuration);
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions The options used to configure retry behavior.
     * @return the updated FileSystemClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public FileSystemClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        blobContainerClientBuilder.retryOptions(retryOptions);
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated FileSystemClientBuilder object
     */
    public FileSystemClientBuilder pipeline(HttpPipeline httpPipeline) {
        blobContainerClientBuilder.pipeline(httpPipeline);
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }
}
