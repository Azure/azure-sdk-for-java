// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

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
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.connectionstring.StorageAuthenticationSettings;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import com.azure.storage.common.implementation.connectionstring.StorageEndpoint;
import com.azure.storage.common.implementation.credentials.CredentialValidator;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.sas.CommonSasQueryParameters;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.implementation.util.BuilderHelper;
import com.azure.storage.file.share.models.ShareAudience;
import com.azure.storage.file.share.models.ShareTokenIntent;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
 * <!-- src_embed com.azure.storage.file.share.ShareFileClient.instantiation.sastoken -->
 * <pre>
 * ShareFileClient fileClient = new ShareFileClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;https:&#47;&#47;$&#123;accountName&#125;.file.core.windows.net?$&#123;SASToken&#125;&quot;&#41;
 *     .shareName&#40;&quot;myshare&quot;&#41;
 *     .resourcePath&#40;&quot;myfilepath&quot;&#41;
 *     .buildFileClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.ShareFileClient.instantiation.sastoken -->
 *
 * <p><strong>Instantiating an Asynchronous File Client with SAS token</strong></p>
 * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.instantiation.sastoken -->
 * <pre>
 * ShareDirectoryClient shareDirectoryClient = new ShareFileClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;https:&#47;&#47;$&#123;accountName&#125;.file.core.windows.net?$&#123;SASToken&#125;&quot;&#41;
 *     .shareName&#40;&quot;myshare&quot;&#41;
 *     .resourcePath&#40;&quot;mydirectory&quot;&#41;
 *     .buildDirectoryClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.ShareDirectoryClient.instantiation.sastoken -->
 *
 * <p>If the {@code endpoint} doesn't contain the query parameters to construct a SAS token it may be set using
 * {@link #sasToken(String) sasToken}.</p>
 *
 * <!-- src_embed com.azure.storage.file.share.ShareFileClient.instantiation.credential -->
 * <pre>
 * ShareFileClient fileClient = new ShareFileClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;https:&#47;&#47;$&#123;accountName&#125;.file.core.windows.net&quot;&#41;
 *     .sasToken&#40;&quot;$&#123;SASTokenQueryParams&#125;&quot;&#41;
 *     .shareName&#40;&quot;myshare&quot;&#41;
 *     .resourcePath&#40;&quot;myfilepath&quot;&#41;
 *     .buildFileClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.ShareFileClient.instantiation.credential -->
 *
 * <!-- src_embed com.azure.storage.file.share.ShareFileAsyncClient.instantiation.credential -->
 * <pre>
 * ShareFileAsyncClient shareFileAsyncClient = new ShareFileClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;https:&#47;&#47;&#123;accountName&#125;.file.core.windows.net&quot;&#41;
 *     .sasToken&#40;&quot;$&#123;SASTokenQueryParams&#125;&quot;&#41;
 *     .shareName&#40;&quot;myshare&quot;&#41;
 *     .resourcePath&#40;&quot;myfilepath&quot;&#41;
 *     .buildFileAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.ShareFileAsyncClient.instantiation.credential -->
 *
 * <p>Another way to authenticate the client is using a {@link StorageSharedKeyCredential}. To create a
 * StorageSharedKeyCredential a connection string from the Storage File service must be used.
 * Set the StorageSharedKeyCredential with {@link ShareFileClientBuilder#connectionString(String) connectionString}.
 * If the builder has both a SAS token and StorageSharedKeyCredential the StorageSharedKeyCredential will be preferred
 * when authorizing requests sent to the service.</p>
 *
 * <p><strong>Instantiating a synchronous File Client with connection string.</strong></p>
 * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.instantiation.connectionstring -->
 * <pre>
 * String connectionString = &quot;DefaultEndpointsProtocol=https;AccountName=&#123;name&#125;;AccountKey=&#123;key&#125;&quot;
 *     + &quot;;EndpointSuffix=&#123;core.windows.net&#125;&quot;;
 * ShareDirectoryClient shareDirectoryClient = new ShareFileClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .shareName&#40;&quot;myshare&quot;&#41;
 *     .resourcePath&#40;&quot;mydirectory&quot;&#41;
 *     .buildDirectoryClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.ShareDirectoryClient.instantiation.connectionstring -->
 *
 * <p><strong>Instantiating an Asynchronous File Client with connection string.</strong></p>
 * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.instantiation.connectionstring -->
 * <pre>
 * String connectionString = &quot;DefaultEndpointsProtocol=https;AccountName=&#123;name&#125;;AccountKey=&#123;key&#125;;&quot;
 *     + &quot;EndpointSuffix=&#123;core.windows.net&#125;&quot;;
 * ShareDirectoryAsyncClient shareDirectoryAsyncClient = new ShareFileClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;.shareName&#40;&quot;myshare&quot;&#41;.resourcePath&#40;&quot;mydirectory&quot;&#41;
 *     .buildDirectoryAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.instantiation.connectionstring -->
 *
 * @see ShareFileClient
 * @see ShareFileAsyncClient
 * @see StorageSharedKeyCredential
 */
@ServiceClientBuilder(serviceClients = {
    ShareFileClient.class, ShareFileAsyncClient.class,
    ShareDirectoryClient.class, ShareDirectoryAsyncClient.class
})
public class ShareFileClientBuilder implements
    TokenCredentialTrait<ShareFileClientBuilder>,
    HttpTrait<ShareFileClientBuilder>,
    ConnectionStringTrait<ShareFileClientBuilder>,
    AzureNamedKeyCredentialTrait<ShareFileClientBuilder>,
    AzureSasCredentialTrait<ShareFileClientBuilder>,
    ConfigurationTrait<ShareFileClientBuilder>,
    EndpointTrait<ShareFileClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(ShareFileClientBuilder.class);

    private String endpoint;
    private String accountName;
    private String shareName;
    private String shareSnapshot;
    private String resourcePath;

    private StorageSharedKeyCredential storageSharedKeyCredential;
    private AzureSasCredential azureSasCredential;
    private TokenCredential tokenCredential;
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
    private ShareServiceVersion version;
    private ShareTokenIntent shareTokenIntent;
    private boolean allowSourceTrailingDot;
    private boolean allowTrailingDot;
    private ShareAudience audience;

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

    private AzureFileStorageImpl constructImpl() {
        Objects.requireNonNull(shareName, "'shareName' cannot be null.");
        Objects.requireNonNull(resourcePath, "'resourcePath' cannot be null.");
        CredentialValidator.validateSingleCredentialIsPresent(
            storageSharedKeyCredential, null, azureSasCredential, sasToken, LOGGER);

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(
            storageSharedKeyCredential, tokenCredential, azureSasCredential, sasToken,
            endpoint, retryOptions, coreRetryOptions, logOptions,
            clientOptions, httpClient, perCallPolicies, perRetryPolicies, configuration, audience, LOGGER);

        return new AzureFileStorageImpl(pipeline, getServiceVersion().getVersion(), shareTokenIntent, endpoint,
            allowTrailingDot, allowSourceTrailingDot);
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
     * @throws IllegalStateException If multiple credentials have been specified.
     */
    public ShareDirectoryAsyncClient buildDirectoryAsyncClient() {
        ShareServiceVersion serviceVersion = getServiceVersion();
        return new ShareDirectoryAsyncClient(constructImpl(), shareName, resourcePath,
            shareSnapshot, accountName, serviceVersion, sasToken != null ? new AzureSasCredential(sasToken) : azureSasCredential);
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
     * @throws IllegalStateException If multiple credentials have been specified.
     */
    public ShareDirectoryClient buildDirectoryClient() {
        ShareServiceVersion serviceVersion = getServiceVersion();
        return new ShareDirectoryClient(constructImpl(), shareName, resourcePath,
            shareSnapshot, accountName, serviceVersion, sasToken != null ? new AzureSasCredential(sasToken) : azureSasCredential);
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
     * @throws IllegalStateException If multiple credentials have been specified.
     */
    public ShareFileAsyncClient buildFileAsyncClient() {
        ShareServiceVersion serviceVersion = getServiceVersion();
        return new ShareFileAsyncClient(constructImpl(), shareName, resourcePath, shareSnapshot,
            accountName, serviceVersion, sasToken != null ? new AzureSasCredential(sasToken) : azureSasCredential);
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
     * @throws IllegalStateException If multiple credentials have been specified.
     */
    public ShareFileClient buildFileClient() {
        ShareServiceVersion serviceVersion = getServiceVersion();
        return new ShareFileClient(new ShareFileAsyncClient(constructImpl(), shareName, resourcePath, shareSnapshot,
            accountName, serviceVersion, sasToken != null ? new AzureSasCredential(sasToken) : azureSasCredential), constructImpl(), shareName, resourcePath, shareSnapshot,
            accountName, serviceVersion, sasToken != null ? new AzureSasCredential(sasToken) : azureSasCredential);
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
    @Override
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

            // Attempt to get the snapshot from the URL passed
            Map<String, String[]> queryParamsMap = SasImplUtils.parseQueryString(fullUrl.getQuery());

            String[] snapshotArray = queryParamsMap.remove("sharesnapshot");
            if (snapshotArray != null) {
                this.shareSnapshot = snapshotArray[0];
            }

            // TODO (gapra): What happens if a user has custom queries?
            // Attempt to get the SAS token from the URL passed
            String sasToken = new CommonSasQueryParameters(
                SasImplUtils.parseQueryString(fullUrl.getQuery()), false).encode();
            if (!CoreUtils.isNullOrEmpty(sasToken)) {
                sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage File endpoint url is malformed.", ex));
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
     * @param credential {@link StorageSharedKeyCredential}.
     * @return the updated ShareFileClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public ShareFileClientBuilder credential(StorageSharedKeyCredential credential) {
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.sasToken = null;
        return this;
    }

    /**
     * Sets the {@link AzureNamedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureNamedKeyCredential}.
     * @return the updated ShareFileClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public ShareFileClientBuilder credential(AzureNamedKeyCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        return credential(StorageSharedKeyCredential.fromAzureNamedKeyCredential(credential));
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param tokenCredential {@link TokenCredential} used to authorize requests sent to the service.
     * @return the updated ShareFileClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public ShareFileClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'credential' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.sasToken = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests. This string should only be the query parameters
     * (with or without a leading '?') and not a full url.
     * @return the updated ShareFileClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public ShareFileClientBuilder sasToken(String sasToken) {
        this.sasToken = Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link AzureSasCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureSasCredential} used to authorize requests sent to the service.
     * @return the updated ShareFileClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public ShareFileClientBuilder credential(AzureSasCredential credential) {
        this.azureSasCredential = Objects.requireNonNull(credential,
            "'credential' cannot be null.");
        return this;
    }

    /**
     * Sets the connection string to connect to the service.
     *
     * @param connectionString Connection string of the storage account.
     * @return the updated ShareFileClientBuilder
     * @throws IllegalArgumentException If {@code connectionString} in invalid.
     */
    @Override
    public ShareFileClientBuilder connectionString(String connectionString) {
        StorageConnectionString storageConnectionString
                = StorageConnectionString.create(connectionString, LOGGER);
        StorageEndpoint endpoint = storageConnectionString.getFileEndpoint();
        if (endpoint == null || endpoint.getPrimaryUri() == null) {
            throw LOGGER
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
     * @return the updated ShareFileClientBuilder object
     */
    @Override
    public ShareFileClientBuilder httpClient(HttpClient httpClient) {
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
     * @return the updated ShareFileClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public ShareFileClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
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
     * @return the updated ShareFileClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    @Override
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
    @Override
    public ShareFileClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryOptions {@link RequestRetryOptions}.
     * @return the updated ShareFileClientBuilder object.
     */
    public ShareFileClientBuilder retryOptions(RequestRetryOptions retryOptions) {
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
     * @return the updated ShareFileClientBuilder object
     */
    @Override
    public ShareFileClientBuilder retryOptions(RetryOptions retryOptions) {
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
     * @return the updated ShareFileClientBuilder object
     */
    @Override
    public ShareFileClientBuilder pipeline(HttpPipeline httpPipeline) {
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
     * @return the updated ShareFileClientBuilder object
     * @throws NullPointerException If {@code clientOptions} is {@code null}.
     */
    @Override
    public ShareFileClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = Objects.requireNonNull(clientOptions, "'clientOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link ShareServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param version {@link ShareServiceVersion} of the service to be used when making requests.
     * @return the updated ShareFileClientBuilder object
     */
    public ShareFileClientBuilder serviceVersion(ShareServiceVersion version) {
        this.version = version;
        return this;
    }

    /**
     * Set the trailing dot property to specify whether trailing dot will be trimmed or not from the source URI.
     *
     * If set to true, trailing dot (.) will be allowed to suffix directory and file names.
     * If false, the trailing dot will be trimmed. Supported by x-ms-version 2022-11-02 and above.
     *
     * @param allowSourceTrailingDot the allowSourceTrailingDot value.
     * @return the updated ShareFileClientBuilder object
     */
    public ShareFileClientBuilder allowSourceTrailingDot(boolean allowSourceTrailingDot) {
        this.allowSourceTrailingDot = allowSourceTrailingDot;
        return this;
    }

    /**
     * Set the trailing dot property to specify whether trailing dot will be trimmed or not from the target URI.
     *
     * If set to true, trailing dot (.) will be allowed to suffix directory and file names.
     * If false, the trailing dot will be trimmed. Supported by x-ms-version 2022-11-02 and above.
     *
     * @param allowTrailingDot the allowTrailingDot value.
     * @return the updated ShareFileClientBuilder object
     */
    public ShareFileClientBuilder allowTrailingDot(boolean allowTrailingDot) {
        this.allowTrailingDot = allowTrailingDot;
        return this;
    }

    /**
     * Sets the {@link ShareTokenIntent} that specifies whether there is intent for a file to be backed up.
     * This is currently required when using {@link TokenCredential}, and ignored for other forms of authentication.
     *
     * @param shareTokenIntent the {@link ShareTokenIntent} value.
     * @return the updated ShareFileClientBuilder object
     */
    public ShareFileClientBuilder shareTokenIntent(ShareTokenIntent shareTokenIntent) {
        this.shareTokenIntent = shareTokenIntent;
        return this;
    }

    /**
     * Sets the Audience to use for authentication with Azure Active Directory (AAD). The audience is not considered
     * when using a shared key.
     * @param audience {@link ShareAudience} to be used when requesting a token from Azure Active Directory (AAD).
     * @return the updated ShareFileClientBuilder object
     */
    public ShareFileClientBuilder audience(ShareAudience audience) {
        this.audience = audience;
        return this;
    }
}
