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
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.file.datalake.implementation.util.BuilderHelper;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.implementation.util.TransformUtils;
import com.azure.storage.file.datalake.models.CustomerProvidedKey;
import com.azure.storage.file.datalake.options.FileSystemEncryptionScope;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of
 * {@link DataLakeServiceClient DataLakeServiceClients} and {@link DataLakeServiceAsyncClient
 * DataLakeServiceAsyncClients}, call {@link #buildClient() buildClient} and {@link #buildAsyncClient()
 * buildAsyncClient} respectively to construct an instance of the desired client.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>the endpoint through {@code .endpoint()}, in the format of {@code https://{accountName}.dfs.core.windows.net}.
 * <li>the credential through {@code .credential()} or {@code .connectionString()}.
 * </ul>
 */
@ServiceClientBuilder(serviceClients = {DataLakeServiceClient.class, DataLakeServiceAsyncClient.class})
public class DataLakeServiceClientBuilder implements
    TokenCredentialTrait<DataLakeServiceClientBuilder>,
    AzureNamedKeyCredentialTrait<DataLakeServiceClientBuilder>,
    AzureSasCredentialTrait<DataLakeServiceClientBuilder>,
    HttpTrait<DataLakeServiceClientBuilder>,
    ConfigurationTrait<DataLakeServiceClientBuilder>,
    EndpointTrait<DataLakeServiceClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(DataLakeServiceClientBuilder.class);

    private final BlobServiceClientBuilder blobServiceClientBuilder;

    private String endpoint;
    private String accountName;

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
    private FileSystemEncryptionScope fileSystemEncryptionScope;

    /**
     * Creates a builder instance that is able to configure and construct {@link DataLakeServiceClient
     * DataLakeServiceClients} and {@link DataLakeServiceAsyncClient DataLakeServiceAsyncClients}.
     */
    public DataLakeServiceClientBuilder() {
        logOptions = getDefaultHttpLogOptions();
        blobServiceClientBuilder = new BlobServiceClientBuilder();
        blobServiceClientBuilder.addPolicy(BuilderHelper.getBlobUserAgentModificationPolicy());
    }

    /**
     * @return a {@link DataLakeServiceClient} created from the configurations in this builder.
     * @throws IllegalStateException If multiple credentials have been specified.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryOptions(RequestRetryOptions)} have been set.
     */
    public DataLakeServiceClient buildClient() {
        return new DataLakeServiceClient(buildAsyncClient(), blobServiceClientBuilder.buildClient());
    }

    /**
     * @return a {@link DataLakeServiceAsyncClient} created from the configurations in this builder.
     * @throws IllegalStateException If multiple credentials have been specified.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryOptions(RequestRetryOptions)} have been set.
     */
    public DataLakeServiceAsyncClient buildAsyncClient() {
        if (Objects.isNull(storageSharedKeyCredential) && Objects.isNull(tokenCredential)
            && Objects.isNull(azureSasCredential)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Data Lake Service Client cannot be accessed "
                + "anonymously. Please provide a form of authentication"));
        }
        DataLakeServiceVersion serviceVersion = version != null ? version : DataLakeServiceVersion.getLatest();

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(
            storageSharedKeyCredential, tokenCredential, azureSasCredential,
            endpoint, retryOptions, coreRetryOptions, logOptions,
            clientOptions, httpClient, perCallPolicies, perRetryPolicies, configuration, LOGGER);

        return new DataLakeServiceAsyncClient(pipeline, endpoint, serviceVersion, accountName,
            blobServiceClientBuilder.buildAsyncClient(), azureSasCredential);
    }

    /**
     * Sets the data lake service endpoint, additionally parses it for information (SAS token)
     *
     * @param endpoint URL of the service
     * @return the updated DataLakeServiceClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    @Override
    public DataLakeServiceClientBuilder endpoint(String endpoint) {
        // Ensure endpoint provided is dfs endpoint
        endpoint = DataLakeImplUtils.endpointToDesiredEndpoint(endpoint, "dfs", "blob");
        blobServiceClientBuilder.endpoint(DataLakeImplUtils.endpointToDesiredEndpoint(endpoint, "blob", "dfs"));
        try {
            BlobUrlParts parts = BlobUrlParts.parse(new URL(endpoint));

            this.accountName = parts.getAccountName();
            this.endpoint = BuilderHelper.getEndpoint(parts);

            String sasToken = parts.getCommonSasQueryParameters().encode();
            if (!CoreUtils.isNullOrEmpty(sasToken)) {
                this.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage endpoint url is malformed.", ex));
        }

        return this;
    }

    /**
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link StorageSharedKeyCredential}.
     * @return the updated DataLakeServiceClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public DataLakeServiceClientBuilder credential(StorageSharedKeyCredential credential) {
        blobServiceClientBuilder.credential(credential);
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.azureSasCredential = null;
        return this;
    }

    /**
     * Sets the {@link AzureNamedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureNamedKeyCredential}.
     * @return the updated DataLakeServiceClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public DataLakeServiceClientBuilder credential(AzureNamedKeyCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        return credential(StorageSharedKeyCredential.fromAzureNamedKeyCredential(credential));
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return the updated DataLakeServiceClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public DataLakeServiceClientBuilder credential(TokenCredential credential) {
        blobServiceClientBuilder.credential(credential);
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
     * @return the updated DataLakeServiceClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public DataLakeServiceClientBuilder sasToken(String sasToken) {
        blobServiceClientBuilder.sasToken(sasToken);
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
     * @return the updated DataLakeServiceClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public DataLakeServiceClientBuilder credential(AzureSasCredential credential) {
        blobServiceClientBuilder.credential(credential);
        this.azureSasCredential = Objects.requireNonNull(credential,
            "'credential' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
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
     * @return the updated DataLakeServiceClientBuilder object
     */
    @Override
    public DataLakeServiceClientBuilder httpClient(HttpClient httpClient) {
        blobServiceClientBuilder.httpClient(httpClient);
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
     * @return the updated DataLakeServiceClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public DataLakeServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        blobServiceClientBuilder.addPolicy(pipelinePolicy);
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
     * @return the updated DataLakeServiceClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    @Override
    public DataLakeServiceClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        blobServiceClientBuilder.httpLogOptions(logOptions);
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
     * @return the updated DataLakeServiceClientBuilder object
     */
    @Override
    public DataLakeServiceClientBuilder configuration(Configuration configuration) {
        blobServiceClientBuilder.configuration(configuration);
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryOptions {@link RequestRetryOptions}.
     * @return the updated DataLakeServiceClientBuilder object
     */
    public DataLakeServiceClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        blobServiceClientBuilder.retryOptions(retryOptions);
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
     * @return the updated DataLakeServiceClientBuilder object
     */
    @Override
    public DataLakeServiceClientBuilder retryOptions(RetryOptions retryOptions) {
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
     * @return the updated DataLakeServiceClientBuilder object
     */
    @Override
    public DataLakeServiceClientBuilder pipeline(HttpPipeline httpPipeline) {
        blobServiceClientBuilder.pipeline(httpPipeline);
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
     * @return the updated DataLakeServiceClientBuilder object
     * @throws NullPointerException If {@code clientOptions} is {@code null}.
     */
    @Override
    public DataLakeServiceClientBuilder clientOptions(ClientOptions clientOptions) {
        blobServiceClientBuilder.clientOptions(clientOptions);
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
     * @return the updated DataLakeServiceClientBuilder object
     */
    public DataLakeServiceClientBuilder serviceVersion(DataLakeServiceVersion version) {
        blobServiceClientBuilder.serviceVersion(TransformUtils.toBlobServiceVersion(version));
        this.version = version;
        return this;
    }


    /**
     * Sets the {@link CustomerProvidedKey customer provided key} that is used to encrypt file contents on the server.
     *
     * @param customerProvidedKey Customer provided key containing the encryption key information.
     * @return the updated DataLakeServiceClientBuilder object
     */
    public DataLakeServiceClientBuilder customerProvidedKey(CustomerProvidedKey customerProvidedKey) {
        if (customerProvidedKey == null) {
            blobServiceClientBuilder.customerProvidedKey(null);
        } else {
            blobServiceClientBuilder.customerProvidedKey(Transforms.toBlobCustomerProvidedKey(customerProvidedKey));
        }

        return this;
    }

    /**
     * Sets the {@link FileSystemEncryptionScope encryption scope} that is used to determine how path contents are
     * encrypted on the server.
     *
     * @param fileSystemEncryptionScope Encryption scope containing the encryption key information.
     * @return the updated DataLakeServiceClientBuilder object
     */
    public DataLakeServiceClientBuilder fileSystemEncryptionScope(FileSystemEncryptionScope fileSystemEncryptionScope) {
        this.fileSystemEncryptionScope = fileSystemEncryptionScope;
        blobServiceClientBuilder.blobContainerEncryptionScope(Transforms.toBlobContainerEncryptionScope(fileSystemEncryptionScope));
        return this;
    }

    /**
     * Sets the encryption scope that is used to encrypt path contents on the server.
     *
     * @param encryptionScope Encryption scope containing the encryption key information.
     * @return the updated DataLakeServiceClientBuilder object
     */
    public DataLakeServiceClientBuilder encryptionScope(String encryptionScope) {
        blobServiceClientBuilder.encryptionScope(encryptionScope);
        return this;
    }
}
