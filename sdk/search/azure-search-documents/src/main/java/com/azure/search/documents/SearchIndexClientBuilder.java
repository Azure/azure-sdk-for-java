// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link SearchIndexClient
 * SearchIndexClients} and {@link SearchIndexAsyncClient SearchIndexAsyncClients}. Call {@link #buildClient()
 * buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an instance of the desired
 * client.
 * <p>
 * The following information must be provided to successfully create a client.
 * <ul>
 *     <li>{@link #endpoint(String)}</li>
 *     <li>{@link #indexName(String)}</li>
 *     <li>{@link #credential(AzureKeyCredential)} or {@link #pipeline(HttpPipeline)}</li>
 * </ul>
 */
@ServiceClientBuilder(serviceClients = {SearchIndexClient.class, SearchIndexAsyncClient.class})
public final class SearchIndexClientBuilder {
    private static final String API_KEY = "api-key";

    /*
     * This header tells the service to return the request ID in the HTTP response. This is useful for correlating the
     * request sent to the response.
     */
    private static final String ECHO_REQUEST_ID_HEADER = "return-client-request-id";

    private static final String SEARCH_PROPERTIES = "azure-search-documents.properties";
    private static final String NAME = "name";
    private static final String VERSION = "version";

    private final ClientLogger logger = new ClientLogger(SearchIndexClientBuilder.class);
    private final List<HttpPipelinePolicy> policies = new ArrayList<>();
    private final HttpHeaders headers = new HttpHeaders().put(ECHO_REQUEST_ID_HEADER, "true");

    private final String clientName;
    private final String clientVersion;

    private AzureKeyCredential keyCredential;
    private SearchServiceVersion serviceVersion;
    private String endpoint;
    private HttpClient httpClient;
    private HttpPipeline httpPipeline;
    private HttpLogOptions httpLogOptions = new HttpLogOptions();
    private Configuration configuration;
    private String indexName;
    private RetryPolicy retryPolicy;


    /**
     * Creates a builder instance that is able to configure and construct {@link SearchIndexClient SearchIndexClients}
     * and {@link SearchIndexAsyncClient SearchIndexAsyncClients}.
     */
    public SearchIndexClientBuilder() {
        Map<String, String> properties = CoreUtils.getProperties(SEARCH_PROPERTIES);
        clientName = properties.getOrDefault(NAME, "UnknownName");
        clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");
    }

    /**
     * Creates a {@link SearchIndexClient} based on options set in the Builder. Every time {@code buildClient()} is
     * called a new instance of {@link SearchIndexClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then only the {@code pipeline}, {@link #endpoint(String)
     * endpoint}, and {@link #indexName(String) indexName} are used to create the {@link SearchIndexClient client}.
     * All other builder settings are ignored.
     *
     * @return A SearchIndexClient with the options set from the builder.
     * @throws NullPointerException If {@code indexName} or {@code endpoint} are {@code null}.
     */
    public SearchIndexClient buildClient() {
        return new SearchIndexClient(buildAsyncClient());
    }

    /**
     * Creates a {@link SearchIndexAsyncClient} based on options set in the Builder. Every time
     * {@code buildAsyncClient()} is called a new instance of {@link SearchIndexAsyncClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then only the {@code pipeline}, {@link #endpoint(String)
     * endpoint}, and {@link #indexName(String) indexName} are used to create the {@link SearchIndexAsyncClient client}.
     * All other builder settings are ignored.
     *
     * @return A SearchIndexClient with the options set from the builder.
     * @throws NullPointerException If {@code indexName} or {@code endpoint} are {@code null}.
     */
    public SearchIndexAsyncClient buildAsyncClient() {
        Objects.requireNonNull(indexName, "'indexName' cannot be null.");
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");

        SearchServiceVersion buildVersion = (serviceVersion == null)
            ? SearchServiceVersion.getLatest()
            : serviceVersion;

        if (httpPipeline != null) {
            return new SearchIndexAsyncClient(endpoint, indexName, buildVersion, httpPipeline);
        }

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();
        httpPipelinePolicies.add(new AddHeadersPolicy(headers));
        httpPipelinePolicies.add(new RequestIdPolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(httpPipelinePolicies);
        httpPipelinePolicies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);

        httpPipelinePolicies.add(new AddDatePolicy());
        if (keyCredential != null) {
            this.policies.add(new AzureKeyCredentialPolicy(API_KEY, keyCredential));
        }
        httpPipelinePolicies.addAll(this.policies);

        HttpPolicyProviders.addAfterRetryPolicies(httpPipelinePolicies);

        httpPipelinePolicies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        httpPipelinePolicies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPipeline buildPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(httpPipelinePolicies.toArray(new HttpPipelinePolicy[0]))
            .build();

        return new SearchIndexAsyncClient(endpoint, indexName, buildVersion, buildPipeline);
    }

    /**
     * Sets the service endpoint for the Azure Search instance.
     *
     * @param endpoint The URL of the Azure Search instance.
     * @return The updated SearchIndexClientBuilder object.
     * @throws IllegalArgumentException If {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    public SearchIndexClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL"));
        }
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used to authenticate HTTP requests.
     *
     * @param keyCredential The {@link AzureKeyCredential} used to authenticate HTTP requests.
     * @return The updated SearchIndexClientBuilder object.
     * @throws NullPointerException If {@code keyCredential} is {@code null}.
     * @throws IllegalArgumentException If {@link AzureKeyCredential#getKey()} is {@code null} or empty.
     */
    public SearchIndexClientBuilder credential(AzureKeyCredential keyCredential) {
        if (keyCredential == null) {
            throw logger.logExceptionAsError(new NullPointerException("'keyCredential' cannot be null."));
        }
        if (CoreUtils.isNullOrEmpty(keyCredential.getKey())) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'keyCredential' cannot have a null or empty API key."));
        }
        this.keyCredential = keyCredential;
        return this;
    }

    /**
     * Sets the name of the index.
     *
     * @param indexName Name of the index.
     * @return The updated SearchIndexClientBuilder object.
     * @throws IllegalArgumentException If {@code indexName} is {@code null} or empty.
     */
    public SearchIndexClientBuilder indexName(String indexName) {
        if (CoreUtils.isNullOrEmpty(indexName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'indexName' cannot be null or empty."));
        }
        this.indexName = indexName;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     * <p>
     * If logging configurations aren't provided HTTP requests and responses won't be logged.
     *
     * @param logOptions The logging configuration for HTTP requests and responses.
     * @return The updated SearchIndexClientBuilder object.
     */
    public SearchIndexClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
        return this;
    }

    /**
     * Adds a pipeline policy to apply to each request sent.
     * <p>
     * This method may be called multiple times, each time it is called the policy will be added to the end of added
     * policy list. All policies will be added after the retry policy.
     *
     * @param policy The pipeline policies to added to the policy list.
     * @return The updated SearchIndexClientBuilder object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public SearchIndexClientBuilder addPolicy(HttpPipelinePolicy policy) {
        policies.add(Objects.requireNonNull(policy));
        return this;
    }

    /**
     * Sets the HTTP client to use for sending requests and receiving responses.
     *
     * @param client The HTTP client that will handle sending requests and receiving responses.
     * @return The updated SearchIndexClientBuilder object.
     */
    public SearchIndexClientBuilder httpClient(HttpClient client) {
        if (this.httpClient != null && client == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     * <p>
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint} and
     * {@link #indexName(String) index} when building a {@link SearchIndexClient} or {@link SearchIndexAsyncClient}.
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated SearchIndexClientBuilder object.
     */
    public SearchIndexClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     * <p>
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store that will be used.
     * @return The updated SearchIndexClientBuilder object.
     */
    public SearchIndexClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that will attempt to retry requests when needed.
     * <p>
     * A default retry policy will be supplied if one isn't provided.
     *
     * @param retryPolicy The {@link RetryPolicy} that will attempt to retry requests when needed.
     * @return The updated SearchIndexClientBuilder object.
     */
    public SearchIndexClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link SearchServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, {@link SearchServiceVersion#getLatest()} will be used as a default. When
     * this default is used updating to a newer client library may result in a newer version of the service being used.
     *
     * @param serviceVersion The version of the service to be used when making requests.
     * @return The updated SearchIndexClientBuilder object.
     */
    public SearchIndexClientBuilder serviceVersion(SearchServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }
}
