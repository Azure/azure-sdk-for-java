// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.agents;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.agents.implementation.KnowledgeAgentRetrievalClientImpl;
import com.azure.search.documents.agents.implementation.KnowledgeRetrievalsImpl;
import com.azure.search.documents.agents.models.KnowledgeAgentRetrievalRequest;
import com.azure.search.documents.agents.models.KnowledgeAgentRetrievalResponse;
import com.azure.search.documents.implementation.util.MappingUtils;

import reactor.core.publisher.Mono;

/**
 * This class provides an asynchronous client for interacting with Azure AI Search Knowledge Agents, enabling retrieval of knowledge and data from various configured backing stores.
 *
 * <h2>Overview</h2>
 * <p>
 * The {@code SearchKnowledgeAgentAsyncClient} exposes asynchronous APIs for sending retrieval requests to a knowledge agent in Azure AI Search. The agent can aggregate and return relevant data from multiple sources, such as Azure AI Search indexes, vector stores, and other knowledge bases configured in your Azure AI Search instance.
 * </p>
 *
 * <h2>Getting Started</h2>
 * <p>
 * Instances of this client are created via the {@link SearchKnowledgeAgentClientBuilder}, which supports fluent configuration of credentials, endpoints, agent names, API versions, and other client options. Authentication can be performed using either an API key or Azure Active Directory credentials. The builder allows you to specify all required parameters for your scenario.
 * </p>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * This client is thread-safe and intended to be shared and reused across threads. Client instances are immutable and do not maintain any mutable state.
 * </p>
 *
 * <h2>Additional Information</h2>
 * <ul>
 *   <li>For more information about Azure AI Search Knowledge Agents, see the Azure documentation.</li>
 *   <li>For authentication details, see the Azure AI Search security documentation.</li>
 *   <li>For Azure SDK for Java guidelines, see the <a href="https://azure.github.io/azure-sdk/java_introduction.html">Azure SDK for Java Introduction</a>.</li>
 * </ul>
 *
 * @see SearchKnowledgeAgentClientBuilder
 * @see SearchKnowledgeAgentClient
 * @see com.azure.search.documents.agents
 */
@ServiceClient(builder = SearchKnowledgeAgentClientBuilder.class, isAsync = true)
public final class SearchKnowledgeAgentAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(SearchKnowledgeAgentAsyncClient.class);

    private final String endpoint;
    private final String agentName;
    private final SearchServiceVersion serviceVersion;
    private final HttpPipeline httpPipeline;
    private final KnowledgeAgentRetrievalClientImpl impl;
    private final KnowledgeRetrievalsImpl retrievals;

    /**
     * Package-private constructor to be used by {@link SearchKnowledgeAgentClientBuilder}.
     */
    SearchKnowledgeAgentAsyncClient(String endpoint, String agentName, SearchServiceVersion serviceVersion,
        HttpPipeline httpPipeline) {
        this.endpoint = endpoint;
        this.agentName = agentName;
        this.serviceVersion = serviceVersion;
        this.httpPipeline = httpPipeline;
        this.impl
            = new KnowledgeAgentRetrievalClientImpl(httpPipeline, endpoint, agentName, serviceVersion.getVersion());
        this.retrievals = impl.getKnowledgeRetrievals();
    }

    /**
     * Gets the endpoint for the Azure AI Search service.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Gets the agent name.
     *
     * @return the agentName value.
     */
    public String getAgentName() {
        return this.agentName;
    }

    /**
     * Gets the API version.
     *
     * @return the apiVersion value.
     */
    public SearchServiceVersion getServiceVersion() {
        return this.serviceVersion;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return the pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /**
     * Asynchronously retrieves relevant data from backing stores.
     *
     * @param retrievalRequest The retrieval request to process.
     * @param xMsQuerySourceAuthorization Token identifying the user for which the query is being executed.
     * @return a {@link Mono} emitting the output contract for the retrieval response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KnowledgeAgentRetrievalResponse> retrieve(KnowledgeAgentRetrievalRequest retrievalRequest,
        String xMsQuerySourceAuthorization) {
        return retrievals.retrieveAsync(retrievalRequest, xMsQuerySourceAuthorization, null);
    }

    /**
     * Asynchronously retrieves relevant data from backing stores, with a full HTTP response.
     *
     * @param retrievalRequest The retrieval request to process.
     * @param xMsQuerySourceAuthorization Token identifying the user for which the query is being executed.
     * @return a {@link Mono} emitting the output contract for the retrieval response along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KnowledgeAgentRetrievalResponse>>
        retrieveWithResponse(KnowledgeAgentRetrievalRequest retrievalRequest, String xMsQuerySourceAuthorization) {
        return withContext(context -> retrieveWithResponse(retrievalRequest, xMsQuerySourceAuthorization, context));
    }

    Mono<Response<KnowledgeAgentRetrievalResponse>> retrieveWithResponse(
        KnowledgeAgentRetrievalRequest retrievalRequest, String xMsQuerySourceAuthorization, Context context) {
        try {
            return retrievals.retrieveWithResponseAsync(retrievalRequest, xMsQuerySourceAuthorization, null, context)
                .onErrorMap(MappingUtils::exceptionMapper);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }
}
