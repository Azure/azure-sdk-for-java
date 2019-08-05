// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.SASTokenCredentialPolicy;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation Storage Blob clients.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 *     <li>the endpoint through {@code .endpoint()}, including the container name and blob name, in the format of {@code https://{accountName}.blob.core.windows.net/{containerName}/{blobName}}.
 *     <li>the credential through {@code .credential()} or {@code .connectionString()} if the container is not publicly accessible.
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder use the following mapping to construct the given client:
 * <ul>
 *     <li>{@link BlobClientBuilder#buildBlobClient()} - {@link BlobClient}</li>
 *     <li>{@link BlobClientBuilder#buildBlobAsyncClient()} - {@link BlobAsyncClient}</li>
 *     <li>{@link BlobClientBuilder#buildAppendBlobClient()} - {@link AppendBlobClient}</li>
 *     <li>{@link BlobClientBuilder#buildAppendBlobAsyncClient()} - {@link AppendBlobAsyncClient}</li>
 *     <li>{@link BlobClientBuilder#buildBlockBlobClient()} - {@link BlockBlobClient}</li>
 *     <li>{@link BlobClientBuilder#buildBlockBlobAsyncClient()} - {@link BlockBlobAsyncClient}</li>
 *     <li>{@link BlobClientBuilder#buildPageBlobClient()} - {@link PageBlobClient}</li>
 *     <li>{@link BlobClientBuilder#buildPageBlobAsyncClient()} - {@link PageBlobAsyncClient}</li>
 * </ul>
 */
public final class BlobClientBuilder {
    private static final String ACCOUNT_NAME = "accountname";
    private static final String ACCOUNT_KEY = "accountkey";
    private static final String ENDPOINT_PROTOCOL = "defaultendpointsprotocol";
    private static final String ENDPOINT_SUFFIX = "endpointsuffix";

    private final List<HttpPipelinePolicy> policies;

    private String endpoint;
    private String containerName;
    private String blobName;
    private String snapshot;
    private SharedKeyCredential sharedKeyCredential;
    private TokenCredential tokenCredential;
    private SASTokenCredential sasTokenCredential;
    private HttpClient httpClient;
    private HttpLogDetailLevel logLevel;
    private RequestRetryOptions retryOptions;
    private Configuration configuration;

    /**
     * Creates a builder instance that is able to configure and construct Storage Blob clients.
     */
    public BlobClientBuilder() {
        retryOptions = new RequestRetryOptions();
        logLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
    }

    private AzureBlobStorageImpl buildImpl() {
        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(blobName);

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        if (configuration == null) {
            configuration = ConfigurationManager.getConfiguration();
        }
        policies.add(new UserAgentPolicy(BlobConfiguration.NAME, BlobConfiguration.VERSION, configuration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        if (sharedKeyCredential != null) {
            policies.add(new SharedKeyCredentialPolicy(sharedKeyCredential));
        } else if (tokenCredential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", endpoint)));
        } else if (sasTokenCredential != null) {
            policies.add(new SASTokenCredentialPolicy(sasTokenCredential));
        }

        policies.add(new RequestRetryPolicy(retryOptions));

        policies.addAll(this.policies);
        policies.add(new HttpLoggingPolicy(logLevel));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new AzureBlobStorageBuilder()
            .url(String.format("%s/%s/%s", endpoint, containerName, blobName))
            .pipeline(pipeline)
            .build();
    }

    /**
     * Creates a {@link BlobClient} based on options set in the Builder. BlobClients are used to perform generic blob
     * methods such as {@link BlobClient#download(OutputStream) download} and
     * {@link BlobClient#getProperties() get properties}, use this when the blob type is unknown.
     *
     * @return a {@link BlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public BlobClient buildBlobClient() {
        return new BlobClient(buildBlobAsyncClient());
    }

    /**
     * Creates a {@link BlobAsyncClient} based on options set in the Builder. BlobAsyncClients are used to perform
     * generic blob methods such as {@link BlobAsyncClient#download() download} and
     * {@link BlobAsyncClient#getProperties()}, use this when the blob type is unknown.
     *
     * @return a {@link BlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public BlobAsyncClient buildBlobAsyncClient() {
        return new BlobAsyncClient(buildImpl(), snapshot);
    }

    /**
     * Creates a {@link AppendBlobClient} based on options set in the Builder. AppendBlobClients are used to perform
     * append blob specific operations such as {@link AppendBlobClient#appendBlock(InputStream, long) append block},
     * only use this when the blob is known to be an append blob.
     *
     * @return a {@link AppendBlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public AppendBlobClient buildAppendBlobClient() {
        return new AppendBlobClient(buildAppendBlobAsyncClient());
    }

    /**
     * Creates a {@link AppendBlobAsyncClient} based on options set in the Builder. AppendBlobAsyncClients are used to
     * perform append blob specific operations such as {@link AppendBlobAsyncClient#appendBlock(Flux, long) append blob},
     * only use this when the blob is known to be an append blob.
     *
     * @return a {@link AppendBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public AppendBlobAsyncClient buildAppendBlobAsyncClient() {
        return new AppendBlobAsyncClient(buildImpl(), snapshot);
    }

    /**
     * Creates a {@link BlockBlobClient} based on options set in the Builder. BlockBlobClients are used to perform
     * generic upload operations such as {@link BlockBlobClient#uploadFromFile(String) upload from file} and block
     * blob specific operations such as {@link BlockBlobClient#stageBlock(String, InputStream, long) stage block} and
     * {@link BlockBlobClient#commitBlockList(List)}, only use this when the blob is known to be a block blob.
     *
     * @return a {@link BlockBlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public BlockBlobClient buildBlockBlobClient() {
        return new BlockBlobClient(buildBlockBlobAsyncClient());
    }

    /**
     * Creates a {@link BlockBlobAsyncClient} based on options set in the Builder. BlockBlobAsyncClients are used to
     * perform generic upload operations such as {@link BlockBlobAsyncClient#uploadFromFile(String) upload from file}
     * and block blob specific operations such as {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stage block}
     * and {@link BlockBlobAsyncClient#commitBlockList(List) commit block list}, only use this when the blob is known to
     * be a block blob.
     *
     * @return a {@link BlockBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public BlockBlobAsyncClient buildBlockBlobAsyncClient() {
        return new BlockBlobAsyncClient(buildImpl(), snapshot);
    }

    /**
     * Creates a {@link PageBlobClient} based on options set in the Builder. PageBlobClients are used to perform page
     * blob specific operations such as {@link PageBlobClient#uploadPages(PageRange, InputStream) upload pages} and
     * {@link PageBlobClient#clearPages(PageRange) clear pages}, only use this when the blob is known to be a page blob.
     *
     * @return a {@link PageBlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public PageBlobClient buildPageBlobClient() {
        return new PageBlobClient(buildPageBlobAsyncClient());
    }

    /**
     * Creates a {@link PageBlobAsyncClient} based on options set in the Builder. PageBlobAsyncClients are used to
     * perform page blob specific operations such as {@link PageBlobAsyncClient#uploadPages(PageRange, Flux) upload pages}
     * and {@link PageBlobAsyncClient#clearPages(PageRange) clear pages}, only use this when the blob is known to be a
     * page blob.
     *
     * @return a {@link PageBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public PageBlobAsyncClient buildPageBlobAsyncClient() {
        return new PageBlobAsyncClient(buildImpl(), snapshot);
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name, blob name)
     * @param endpoint URL of the service
     * @return the updated BlobClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    public BlobClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            BlobURLParts parts = URLParser.parse(url);

            this.endpoint = parts.scheme() + "://" + parts.host();
            this.containerName = parts.containerName();
            this.blobName = parts.blobName();
            this.snapshot = parts.snapshot();

            this.sasTokenCredential = SASTokenCredential.fromQueryParameters(parts.sasQueryParameters());
            if (this.sasTokenCredential != null) {
                this.tokenCredential = null;
                this.sharedKeyCredential = null;
            }
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("The Azure Storage Blob endpoint url is malformed.");
        }

        return this;
    }

    /**
     * Sets the name of the container this client is connecting to.
     * @param containerName the name of the container
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code containerName} is {@code null}
     */
    public BlobClientBuilder containerName(String containerName) {
        this.containerName = Objects.requireNonNull(containerName);
        return this;
    }

    /**
     * Sets the name of the blob this client is connecting to.
     * @param blobName the name of the blob
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code blobName} is {@code null}
     */
    public BlobClientBuilder blobName(String blobName) {
        this.blobName = Objects.requireNonNull(blobName);
        return this;
    }

    /**
     * Sets the snapshot of the blob this client is connecting to.
     * @param snapshot the snapshot identifier for the blob
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder snapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}
     */
    public BlobClientBuilder credential(SharedKeyCredential credential) {
        this.sharedKeyCredential = Objects.requireNonNull(credential);
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}
     */
    public BlobClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential);
        this.sharedKeyCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code credential} is {@code null}
     */
    public BlobClientBuilder credential(SASTokenCredential credential) {
        this.sasTokenCredential = Objects.requireNonNull(credential);
        this.sharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Clears the credential used to authorize requests sent to the service
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder anonymousCredential() {
        this.sharedKeyCredential = null;
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the connection string for the service, parses it for authentication information (account name, account key)
     * @param connectionString connection string from access keys section
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code connectionString} is {@code null}
     * @throws IllegalArgumentException If {@code connectionString} doesn't contain AccountName or AccountKey.
     */
    public BlobClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString);

        Map<String, String> connectionKVPs = new HashMap<>();
        for (String s : connectionString.split(";")) {
            String[] kvp = s.split("=", 2);
            connectionKVPs.put(kvp[0].toLowerCase(Locale.ROOT), kvp[1]);
        }

        String accountName = connectionKVPs.get(ACCOUNT_NAME);
        String accountKey = connectionKVPs.get(ACCOUNT_KEY);
        String endpointProtocol = connectionKVPs.get(ENDPOINT_PROTOCOL);
        String endpointSuffix = connectionKVPs.get(ENDPOINT_SUFFIX);

        if (ImplUtils.isNullOrEmpty(accountName) || ImplUtils.isNullOrEmpty(accountKey)) {
            throw new IllegalArgumentException("Connection string must contain 'AccountName' and 'AccountKey'.");
        }

        if (!ImplUtils.isNullOrEmpty(endpointProtocol) && !ImplUtils.isNullOrEmpty(endpointSuffix)) {
            String endpoint = String.format("%s://%s.blob.%s", endpointProtocol, accountName, endpointSuffix.replaceFirst("^\\.", ""));
            endpoint(endpoint);
        }

        // Use accountName and accountKey to get the SAS token using the credential class.
        return credential(new SharedKeyCredential(accountName, accountKey));
    }

    /**
     * Sets the http client used to send service requests
     * @param httpClient http client to send requests
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code httpClient} is {@code null}
     */
    public BlobClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent
     * @param pipelinePolicy a pipeline policy
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}
     */
    public BlobClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.policies.add(Objects.requireNonNull(pipelinePolicy));
        return this;
    }

    /**
     * Sets the logging level for service requests
     * @param logLevel logging level
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values used to buildClient the client with
     * when they are not set in the appendBlobClientBuilder, defaults to Configuration.NONE
     * @param configuration configuration store
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     * @param retryOptions the options to configure retry behaviors
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}
     */
    public BlobClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions);
        return this;
    }
}
