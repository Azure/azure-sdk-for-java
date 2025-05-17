// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.agents;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.AzureKeyCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.util.Utility;
import com.azure.search.documents.models.SearchAudience;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help configure and instantiate {@link SearchKnowledgeAgentClient}
 * and {@link SearchKnowledgeAgentAsyncClient} for interacting with Azure AI Search Knowledge Agents.
 *
 * <h2>Overview</h2>
 * <p>
 * This builder enables the creation of both synchronous and asynchronous clients for Azure AI Search Knowledge Agents,
 * allowing you to interact with knowledge retrieval and agent-based search capabilities. The builder supports configuration
 * of authentication, endpoint, agent name, API version, and HTTP pipeline options, following Azure SDK for Java standards.
 * </p>
 *
 * <h2>Getting Started</h2>
 * <p>
 * To create a client, configure the required properties such as the service endpoint, agent name, API version, and authentication
 * credentials. The builder supports both API key and Microsoft Entra ID (role-based) authentication. Additional options such as
 * custom HTTP pipeline policies, retry options, logging, and serialization can also be configured.
 * </p>
 *
 * <h3>Authentication</h3>
 * <p>
 * Azure AI Search Knowledge Agents support authentication using either an {@link AzureKeyCredential} (API key) or a
 * {@link TokenCredential} (Microsoft Entra ID). When using Microsoft Entra ID, you may also specify a {@link SearchAudience}
 * to target a specific Azure cloud environment.
 * </p>
 *
 * <h3>Client Instantiation</h3>
 * <p>
 * Use {@link #buildClient()} to create a synchronous {@link SearchKnowledgeAgentClient}, or {@link #buildAsyncClient()} to create
 * an asynchronous {@link SearchKnowledgeAgentAsyncClient}. Each call to these methods returns a new client instance with the
 * configured options.
 * </p>
 *
 * <h3>Thread Safety</h3>
 * <p>
 * Client instances created by this builder are thread-safe and intended to be shared and reused across threads. The builder itself
 * is not thread-safe and should not be used concurrently from multiple threads.
 * </p>
 *
 * <h3>Additional Information</h3>
 * <ul>
 *   <li>For more information about Azure AI Search Knowledge Agents, see the Azure documentation.</li>
 *   <li>For authentication details, see the Azure AI Search security documentation.</li>
 *   <li>For Azure SDK for Java guidelines, see the <a href="https://azure.github.io/azure-sdk/java_introduction.html">Azure SDK for Java Introduction</a>.</li>
 * </ul>
 *
 * @see SearchKnowledgeAgentClient
 * @see SearchKnowledgeAgentAsyncClient
 * @see com.azure.search.documents.agents
 */
@ServiceClientBuilder(serviceClients = { SearchKnowledgeAgentClient.class, SearchKnowledgeAgentAsyncClient.class })
public final class SearchKnowledgeAgentClientBuilder
    implements AzureKeyCredentialTrait<SearchKnowledgeAgentClientBuilder>,
    ConfigurationTrait<SearchKnowledgeAgentClientBuilder>, EndpointTrait<SearchKnowledgeAgentClientBuilder>,
    HttpTrait<SearchKnowledgeAgentClientBuilder>, TokenCredentialTrait<SearchKnowledgeAgentClientBuilder> {

    private static final ClientLogger LOGGER = new ClientLogger(SearchKnowledgeAgentClientBuilder.class);

    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();

    private AzureKeyCredential azureKeyCredential;
    private TokenCredential tokenCredential;
    private SearchAudience audience;
    private String endpoint;
    private String agentName;
    private SearchServiceVersion serviceVersion;
    private HttpClient httpClient;
    private HttpPipeline httpPipeline;
    private HttpLogOptions httpLogOptions;
    private ClientOptions clientOptions;
    private Configuration configuration;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;
    private JsonSerializer jsonSerializer;

    /**
     * Creates a new builder instance.
     */
    public SearchKnowledgeAgentClientBuilder() {
    }

    /**
     * Sets the service endpoint for the Azure AI Search instance.
     *
     * @param endpoint The URL of the Azure AI Search instance.
     * @return The updated builder object.
     */
    @Override
    public SearchKnowledgeAgentClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the agent name for the Azure AI Search agent.
     *
     * @param agentName The name of the agent.
     * @return The updated builder object.
     */
    public SearchKnowledgeAgentClientBuilder agentName(String agentName) {
        this.agentName = agentName;
        return this;
    }

    /**
     * Sets the API version to use for requests.
     *
     * @param apiVersion The API version.
     * @return The updated builder object.
     */
    public SearchKnowledgeAgentClientBuilder serviceVersion(SearchServiceVersion apiVersion) {
        this.serviceVersion = apiVersion;
        return this;
    }

    @Override
    public SearchKnowledgeAgentClientBuilder credential(AzureKeyCredential credential) {
        this.azureKeyCredential = credential;
        return this;
    }

    @Override
    public SearchKnowledgeAgentClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = credential;
        return this;
    }

    /**
     * Sets the audience for the Azure AI Search instance.
     *
     * @param audience The audience to use.
     * @return The updated builder object.
     */
    public SearchKnowledgeAgentClientBuilder audience(SearchAudience audience) {
        this.audience = audience;
        return this;
    }

    @Override
    public SearchKnowledgeAgentClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;
        return this;
    }

    @Override
    public SearchKnowledgeAgentClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    @Override
    public SearchKnowledgeAgentClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");
        this.perCallPolicies.add(policy); // For simplicity, treat as per-call; refine as needed
        return this;
    }

    @Override
    public SearchKnowledgeAgentClientBuilder httpClient(HttpClient client) {
        this.httpClient = client;
        return this;
    }

    @Override
    public SearchKnowledgeAgentClientBuilder pipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        return this;
    }

    @Override
    public SearchKnowledgeAgentClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    @Override
    public SearchKnowledgeAgentClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Builds a synchronous {@link SearchKnowledgeAgentClient}.
     *
     * @return a new {@link SearchKnowledgeAgentClient} instance.
     */
    public SearchKnowledgeAgentClient buildClient() {
        validateRequiredFields();
        SearchServiceVersion serviceVersion
            = this.serviceVersion != null ? this.serviceVersion : SearchServiceVersion.getLatest();
        HttpPipeline pipeline = this.httpPipeline != null
            ? this.httpPipeline
            : Utility.buildHttpPipeline(clientOptions, httpLogOptions, configuration, retryPolicy, retryOptions,
                azureKeyCredential, tokenCredential, audience, perCallPolicies, perRetryPolicies, httpClient, LOGGER);
        return new SearchKnowledgeAgentClient(endpoint, agentName, serviceVersion, pipeline);
    }

    /**
     * Builds an asynchronous {@link SearchKnowledgeAgentAsyncClient}.
     *
     * @return a new {@link SearchKnowledgeAgentAsyncClient} instance.
     */
    public SearchKnowledgeAgentAsyncClient buildAsyncClient() {
        validateRequiredFields();
        SearchServiceVersion serviceVersion
            = this.serviceVersion != null ? this.serviceVersion : SearchServiceVersion.getLatest();
        HttpPipeline pipeline = this.httpPipeline != null
            ? this.httpPipeline
            : Utility.buildHttpPipeline(clientOptions, httpLogOptions, configuration, retryPolicy, retryOptions,
                azureKeyCredential, tokenCredential, audience, perCallPolicies, perRetryPolicies, httpClient, LOGGER);
        return new SearchKnowledgeAgentAsyncClient(endpoint, agentName, serviceVersion, pipeline);
    }

    private void validateRequiredFields() {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        Objects.requireNonNull(agentName, "'agentName' cannot be null.");
    }
}
