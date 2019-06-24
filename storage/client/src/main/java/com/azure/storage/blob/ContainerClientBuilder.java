// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.policy.SASTokenCredentialPolicy;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Fluent ContainerClientBuilder for instantiating a {@link ContainerClient} or {@link ContainerAsyncClient}.
 *
 * <p>
 * An instance of this builder may only be created from static method {@link ContainerClient#containerClientBuilder()}.
 * The following information must be provided on this builder:
 *
 * <p><ul>
 *     <li>the endpoint through {@code .endpoint()}, including the container name, in the format of {@code https://{accountName}.blob.core.windows.net/{containerName}}.
 *     <li>the credential through {@code .credential()} or {@code .connectionString()} if the container is not publicly accessible.
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder, call {@code .buildClient()} to create a
 * {@link ContainerClient} or {@code .buildAsyncClient()} to create a {@link ContainerAsyncClient}.
 */
public final class ContainerClientBuilder {
    private static final String ACCOUNT_NAME = "AccountName".toLowerCase();
    private static final String ACCOUNT_KEY = "AccountKey".toLowerCase();
    private static final String ENDPOINT_PROTOCOL = "DefaultEndpointsProtocol".toLowerCase();
    private static final String ENDPOINT_SUFFIX = "EndpointSuffix".toLowerCase();

    private final List<HttpPipelinePolicy> policies;

    private URL endpoint;
    private String containerName;
    private SharedKeyCredential sharedKeyCredential;
    private TokenCredential tokenCredential;
    private SASTokenCredential sasTokenCredential;
    private HttpClient httpClient;
    private HttpLogDetailLevel logLevel;
    private RetryPolicy retryPolicy;
    private Configuration configuration;

    public ContainerClientBuilder() {
        retryPolicy = new RetryPolicy();
        logLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
    }

    /**
     * Constructs an instance of ContainerAsyncClient based on the configurations stored in the appendBlobClientBuilder.
     * @return a new client instance
     */
    private AzureBlobStorageBuilder buildImpl() {
        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(containerName);

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        if (configuration == null) {
            configuration = Configuration.NONE;
        }
        policies.add(new UserAgentPolicy(BlobConfiguration.NAME, BlobConfiguration.VERSION, configuration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        if (sharedKeyCredential != null) {
            policies.add(new SharedKeyCredentialPolicy(sharedKeyCredential));
        } else if (tokenCredential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s://%s/.default", endpoint.getProtocol(), endpoint.getHost())));
        } else if (sasTokenCredential != null) {
            policies.add(new SASTokenCredentialPolicy(sasTokenCredential));
        } else {
            policies.add(new AnonymousCredentialPolicy());
        }

        policies.add(retryPolicy);

        policies.addAll(this.policies);
        policies.add(new HttpLoggingPolicy(logLevel));

        HttpPipeline pipeline = HttpPipeline.builder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new AzureBlobStorageBuilder()
            .url(String.format("%s/%s", endpoint.toString(), containerName))
            .pipeline(pipeline);
    }

    /**
     * @return a {@link ContainerClient} created from the configurations in this builder.
     */
    public ContainerClient buildClient() {
        return new ContainerClient(buildAsyncClient());
    }

    /**
     * @return a {@link ContainerAsyncClient} created from the configurations in this builder.
     */
    public ContainerAsyncClient buildAsyncClient() {
        return new ContainerAsyncClient(buildImpl());
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name)
     * @param endpoint URL of the service
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        URL url;
        try {
            url = new URL(endpoint);
            this.endpoint = new URL(url.getProtocol() + "://" + url.getAuthority());
            String path = url.getPath();
            if (path != null && !path.isEmpty() && !path.equals("/")) {
                path = path.replaceAll("^/", "").replaceAll("/$", "");
                if (path.contains("/")) {
                    throw new IllegalArgumentException("Endpoint should contain exactly 0 or 1 path segments");
                } else {
                    this.containerName = path;
                }
            }
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("The Azure Storage Blob endpoint url is malformed.");
        }

        SASTokenCredential credential = SASTokenCredential.fromQuery(url.getQuery());
        if (credential != null) {
            this.sasTokenCredential = credential;
        }

        return this;
    }

    /**
     * Sets the name of the container this client is connecting to.
     * @param containerName the name of the container
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder containerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    String endpoint() {
        return this.endpoint.toString();
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder credential(SharedKeyCredential credential) {
        this.sharedKeyCredential = credential;
        return this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = credential;
        return this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder credential(SASTokenCredential credential) {
        this.sasTokenCredential = credential;
        return this;
    }

    /**
     * Clears the credential used to authorize requests sent to the service
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder anonymousCredential() {
        this.sharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Sets the connection string for the service, parses it for authentication information (account name, account key)
     * @param connectionString connection string from access keys section
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString);

        Map<String, String> connectionKVPs = new HashMap<>();
        for (String s : connectionString.split(";")) {
            String[] kvp = s.split("=", 2);
            connectionKVPs.put(kvp[0].toLowerCase(), kvp[1]);
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
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent
     * @param pipelinePolicy a pipeline policy
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.policies.add(pipelinePolicy);
        return this;
    }

    /**
     * Sets the logging level for service requests
     * @param logLevel logging level
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values used to buildClient the client with
     * when they are not set in the appendBlobClientBuilder, defaults to Configuration.NONE
     * @param configuration configuration store
     * @return the updated ContainerClientBuilder object
     */
    public ContainerClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
}
