// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.CommonSasQueryParameters;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.implementation.connectionstring.StorageAuthenticationSettings;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import com.azure.storage.common.implementation.connectionstring.StorageEndpoint;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.implementation.policy.SasTokenCredentialPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;
import com.azure.storage.file.share.implementation.AzureFileStorageBuilder;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.implementation.util.BuilderHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the
 * {@link ShareFileClient FileClients}, {@link ShareFileAsyncClient FileAsyncClients},
 * {@link ShareDirectoryClient DirectoryClients}, and
 * {@link ShareDirectoryAsyncClient DirectoryAsyncClients}. Calling
 * {@link ShareFileClientBuilder#buildFileClient() buildFileClient},
 * {@link ShareFileClientBuilder#buildFileAsyncClient() buildFileAsyncClient},
 * {@link ShareFileClientBuilder#buildDirectoryClient() buildDirectoryClient}, or
 * {@link ShareFileClientBuilder#buildDirectoryAsyncClient() buildDirectoryAsyncClient} constructs
 * an instance of {@link ShareFileClient}, {@link ShareFileAsyncClient}, {@link ShareDirectoryClient}, or
 * {@link ShareDirectoryAsyncClient} respectively.
 *
 * <p>The client needs the endpoint of the Azure Storage File service, name of the share, and authorization credential.
 * {@link ShareFileClientBuilder#endpoint(String) endpoint} gives the builder the endpoint and may give the builder the
 * {@link ShareFileClientBuilder#shareName(String)}, {@link ShareFileClientBuilder#resourcePath(String)} and a
 * {@link #sasToken(String) SAS token} that authorizes the client.</p>
 *
 * <p><strong>Instantiating a synchronous File Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.file.share.ShareFileClient.instantiation.sastoken}
 *
 * <p><strong>Instantiating an Asynchronous File Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.instantiation.sastoken}
 *
 * <p>If the {@code endpoint} doesn't contain the query parameters to construct a SAS token it may be set using
 * {@link #sasToken(String) sasToken}.</p>
 *
 * {@codesnippet com.azure.storage.file.share.ShareFileClient.instantiation.credential}
 *
 * {@codesnippet com.azure.storage.file.share.ShareFileAsyncClient.instantiation.credential}
 *
 * <p>Another way to authenticate the client is using a {@link StorageSharedKeyCredential}. To create a
 * StorageSharedKeyCredential a connection string from the Storage File service must be used.
 * Set the StorageSharedKeyCredential with {@link ShareFileClientBuilder#connectionString(String) connectionString}.
 * If the builder has both a SAS token and StorageSharedKeyCredential the StorageSharedKeyCredential will be preferred
 * when authorizing requests sent to the service.</p>
 *
 * <p><strong>Instantiating a synchronous File Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.file.share.ShareDirectoryClient.instantiation.connectionstring}
 *
 * <p><strong>Instantiating an Asynchronous File Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.file.share.ShareDirectoryAsyncClient.instantiation.connectionstring}
 *
 * @see ShareFileClient
 * @see ShareFileAsyncClient
 * @see StorageSharedKeyCredential
 */
@ServiceClientBuilder(serviceClients = {
    ShareFileClient.class, ShareFileAsyncClient.class,
    ShareDirectoryClient.class, ShareDirectoryAsyncClient.class
})
public class ShareFileClientBuilder {
    private final ClientLogger logger = new ClientLogger(ShareFileClientBuilder.class);

    private String endpoint;
    private String accountName;
    private String shareName;
    private String shareSnapshot;
    private String resourcePath;

    private StorageSharedKeyCredential storageSharedKeyCredential;
    private SasTokenCredential sasTokenCredential;

    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
    private HttpLogOptions logOptions;
    private RequestRetryOptions retryOptions = new RequestRetryOptions();
    private HttpPipeline httpPipeline;

    private Configuration configuration;
    private ShareServiceVersion version;

    /**
     * Creates a builder instance that is able to configure and construct {@link ShareFileClient FileClients} and {@link
     * ShareFileAsyncClient FileAsyncClients}.
     */
    public ShareFileClientBuilder() {
        logOptions = getDefaultHttpLogOptions();
    }

    private ShareServiceVersion getServiceVersion() {
        return version != null ? version : ShareServiceVersion.getLatest();
    }

    private AzureFileStorageImpl constructImpl(ShareServiceVersion serviceVersion) {
        Objects.requireNonNull(shareName, "'shareName' cannot be null.");
        Objects.requireNonNull(resourcePath, "'resourcePath' cannot be null.");

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(() -> {
            if (storageSharedKeyCredential != null) {
                return new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential);
            } else if (sasTokenCredential != null) {
                return new SasTokenCredentialPolicy(sasTokenCredential);
            } else {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Credentials are required for authorization"));
            }
        }, retryOptions, logOptions, httpClient, additionalPolicies, configuration);

        return new AzureFileStorageBuilder()
            .url(endpoint)
            .pipeline(pipeline)
            .version(serviceVersion.getVersion())
            .build();
    }

    /**
     * Creates a {@link ShareDirectoryAsyncClient} based on options set in the builder. Every time
     * {@code buildFileAsyncClient()} is called a new instance of {@link ShareDirectoryAsyncClient} is created.
     *
     * <p>
     * If {@link ShareFileClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * ShareFileClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link ShareDirectoryAsyncClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A ShareAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code shareName} is {@code null} or {@code shareName} is {@code null}.
     * @throws IllegalArgumentException If neither a {@link StorageSharedKeyCredential}
     * or {@link #sasToken(String) SAS token} has been set.
     */
    public ShareDirectoryAsyncClient buildDirectoryAsyncClient() {
        ShareServiceVersion serviceVersion = getServiceVersion();
        return new ShareDirectoryAsyncClient(constructImpl(serviceVersion), shareName, resourcePath, shareSnapshot,
            accountName, serviceVersion);
    }

    /**
     * Creates a {@link ShareDirectoryClient} based on options set in the builder. Every time
     * {@code buildDirectoryClient()} is called a new instance of {@link ShareDirectoryClient} is created.
     *
     * <p>
     * If {@link ShareFileClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * ShareFileClientBuilder#endpoint(String) endpoint} are used to create the {@link ShareDirectoryClient client}.
     * All other builder settings are ignored.
     * </p>
     *
     * @return A ShareDirectoryClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint}, {@code shareName} or {@code directoryPath} is {@code null}.
     * @throws IllegalArgumentException If neither a {@link StorageSharedKeyCredential}
     * or {@link #sasToken(String) SAS token} has been set.
     */
    public ShareDirectoryClient buildDirectoryClient() {
        return new ShareDirectoryClient(this.buildDirectoryAsyncClient());
    }

    /**
     * Creates a {@link ShareFileAsyncClient} based on options set in the builder. Every time
     * {@code buildFileAsyncClient()} is called a new instance of {@link ShareFileAsyncClient} is created.
     *
     * <p>
     * If {@link ShareFileClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * ShareFileClientBuilder#endpoint(String) endpoint} are used to create the {@link ShareFileAsyncClient client}.
     * All other builder settings are ignored.
     * </p>
     *
     * @return A ShareAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code shareName} is {@code null} or the (@code resourcePath) is {@code null}.
     * @throws IllegalArgumentException If neither a {@link StorageSharedKeyCredential}
     * or {@link #sasToken(String) SAS token} has been set.
     */
    public ShareFileAsyncClient buildFileAsyncClient() {
        ShareServiceVersion serviceVersion = getServiceVersion();
        return new ShareFileAsyncClient(constructImpl(serviceVersion), shareName, resourcePath, shareSnapshot,
            accountName, serviceVersion);
    }

    /**
     * Creates a {@link ShareFileClient} based on options set in the builder. Every time {@code buildFileClient()} is
     * called a new instance of {@link ShareFileClient} is created.
     *
     * <p>
     * If {@link ShareFileClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * ShareFileClientBuilder#endpoint(String) endpoint} are used to create the {@link ShareFileClient client}.
     * All other builder settings are ignored.
     * </p>
     *
     * @return A ShareFileClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint}, {@code shareName} or {@code resourcePath} is {@code null}.
     * @throws IllegalStateException If neither a {@link StorageSharedKeyCredential}
     * or {@link #sasToken(String) SAS token} has been set.
     */
    public ShareFileClient buildFileClient() {
        return new ShareFileClient(this.buildFileAsyncClient());
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
     * @return the updated ShareFileClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is an invalid URL
     */
    public ShareFileClientBuilder endpoint(String endpoint) {
        try {
            URL fullUrl = new URL(endpoint);
            this.endpoint = fullUrl.getProtocol() + "://" + fullUrl.getHost();
            this.accountName = BuilderHelper.getAccountName(fullUrl);

            // Attempt to get the share name and file path from the URL passed
            String[] pathSegments = fullUrl.getPath().split("/");
            int length = pathSegments.length;
            this.shareName = length >= 2 ? pathSegments[1] : this.shareName;
            String[] filePathParams = length >= 3 ? Arrays.copyOfRange(pathSegments, 2, length) : null;
            this.resourcePath = filePathParams != null ? String.join("/", filePathParams) : this.resourcePath;

            // TODO (gapra): What happens if a user has custom queries?
            // Attempt to get the SAS token from the URL passed
            String sasToken = new CommonSasQueryParameters(
                StorageImplUtils.parseQueryStringSplitValues(fullUrl.getQuery()), false).encode();
            if (!CoreUtils.isNullOrEmpty(sasToken)) {
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
     * @return the updated ShareFileClientBuilder object
     * @throws NullPointerException If {@code shareName} is {@code null}.
     */
    public ShareFileClientBuilder shareName(String shareName) {
        this.shareName = shareName;
        return this;
    }

    /**
     * Sets the shareSnapshot that the constructed clients will interact with. This shareSnapshot must be linked to the
     * share that has been specified in the builder.
     *
     * @param snapshot Identifier of the shareSnapshot
     * @return the updated ShareFileClientBuilder object
     * @throws NullPointerException If {@code shareSnapshot} is {@code null}.
     */
    public ShareFileClientBuilder snapshot(String snapshot) {
        this.shareSnapshot = snapshot;
        return this;
    }

    /**
     * Sets the file that the constructed clients will interact with
     *
     * @param resourcePath Path of the file (or directory).
     * @return the updated ShareFileClientBuilder object
     * @throws NullPointerException If {@code resourcePath} is {@code null}.
     */
    public ShareFileClientBuilder resourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
        return this;
    }

    /**
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated ShareFileClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public ShareFileClientBuilder credential(StorageSharedKeyCredential credential) {
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     * @return the updated ShareFileClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public ShareFileClientBuilder sasToken(String sasToken) {
        this.sasTokenCredential = new SasTokenCredential(Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null."));
        this.storageSharedKeyCredential = null;
        return this;
    }

    /**
     * Sets the connection string to connect to the service.
     *
     * @param connectionString Connection string of the storage account.
     * @return the updated ShareFileClientBuilder
     * @throws IllegalArgumentException If {@code connectionString} in invalid.
     */
    public ShareFileClientBuilder connectionString(String connectionString) {
        StorageConnectionString storageConnectionString
                = StorageConnectionString.create(connectionString, logger);
        StorageEndpoint endpoint = storageConnectionString.getFileEndpoint();
        if (endpoint == null || endpoint.getPrimaryUri() == null) {
            throw logger
                    .logExceptionAsError(new IllegalArgumentException(
                            "connectionString missing required settings to derive file service endpoint."));
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
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated ShareFileClientBuilder object
     */
    public ShareFileClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            logger.info("'httpClient' is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent. The policy will be added after the retry policy. If
     * the method is called multiple times, all policies will be added and their order preserved.
     *
     * @param pipelinePolicy a pipeline policy
     * @return the updated ShareFileClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public ShareFileClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated ShareFileClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public ShareFileClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Gets the default log options with Storage headers and query parameters.
     *
     * @return the default log options.
     */
    public static HttpLogOptions getDefaultHttpLogOptions() {
        return BuilderHelper.getDefaultHttpLogOptions();
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated ShareFileClientBuilder object
     */
    public ShareFileClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions The options used to configure retry behavior.
     * @return the updated ShareFileClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public ShareFileClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated ShareFileClientBuilder object
     */
    public ShareFileClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the {@link ShareServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link ShareServiceVersion} of the service to be used when making requests.
     * @return the updated ShareFileClientBuilder object
     */
    public ShareFileClientBuilder serviceVersion(ShareServiceVersion version) {
        this.version = version;
        return this;
    }
}
