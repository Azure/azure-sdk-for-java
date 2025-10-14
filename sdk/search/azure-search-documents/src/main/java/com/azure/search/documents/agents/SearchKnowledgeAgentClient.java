// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.agents;

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

/**
 * This class provides a client that contains the operations for retrieving knowledge from an Azure AI Search agent.
 *
 * <h2>Overview</h2>
 * <p>
 *     The {@code SearchKnowledgeAgentClient} provides a synchronous API for interacting with Azure AI Search knowledge agents. This client enables you to send retrieval requests to a knowledge agent, which can aggregate and return relevant data from various backing stores configured in your Azure AI Search instance.
 * </p>
 *
 * <p>
 *     The client is designed to be instantiated via the {@link SearchKnowledgeAgentClientBuilder}, which allows for fluent configuration of credentials, endpoints, agent names, and other client options. Once built, the client exposes methods to perform retrieval operations, returning structured responses that include the agent's results and any associated metadata.
 * </p>
 *
 * <h2>Getting Started</h2>
 * <p>
 *     To get started, configure and build an instance of this client using the {@link SearchKnowledgeAgentClientBuilder}. Authentication can be performed using either an API key or Azure Active Directory credentials, and the builder allows you to specify the agent name, endpoint, and API version as required by your scenario.
 * </p>
 *
 * <h2>Thread Safety</h2>
 * <p>
 *     This client is thread-safe and intended to be shared across threads and reused for multiple requests.
 * </p>
 *
 * <h2>Additional Information</h2>
 * <p>
 *     For more information about Azure AI Search knowledge agents, see the Azure documentation. For advanced scenarios, such as customizing the HTTP pipeline or integrating with other Azure SDK components, refer to the Azure SDK for Java design guidelines and the documentation for {@link SearchKnowledgeAgentClientBuilder}.
 * </p>
 *
 * @see SearchKnowledgeAgentClientBuilder
 * @see SearchKnowledgeAgentAsyncClient
 */
@ServiceClient(builder = SearchKnowledgeAgentClientBuilder.class)
public final class SearchKnowledgeAgentClient {
    private static final ClientLogger LOGGER = new ClientLogger(SearchKnowledgeAgentClient.class);

    private final String endpoint;
    private final String agentName;
    private final SearchServiceVersion serviceVersion;
    private final HttpPipeline httpPipeline;
    private final KnowledgeAgentRetrievalClientImpl impl;
    private final KnowledgeRetrievalsImpl retrievals;

    /**
     * Package-private constructor to be used by {@link SearchKnowledgeAgentClientBuilder}.
     */
    SearchKnowledgeAgentClient(String endpoint, String agentName, SearchServiceVersion serviceVersion,
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
     * Retrieves relevant data from backing stores synchronously.
     *
     * @param retrievalRequest The retrieval request to process.
     * @param xMsQuerySourceAuthorization Token identifying the user for which the query is being executed.
     * @return the output contract for the retrieval response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KnowledgeAgentRetrievalResponse retrieve(KnowledgeAgentRetrievalRequest retrievalRequest,
        String xMsQuerySourceAuthorization) {
        return retrievals.retrieve(retrievalRequest, xMsQuerySourceAuthorization, null);
    }

    /**
     * Retrieves relevant data from backing stores synchronously, with a full HTTP response.
     *
     * @param retrievalRequest The retrieval request to process.
     * @param xMsQuerySourceAuthorization Token identifying the user for which the query is being executed.
     * @param context The context to associate with this operation.
     * @return the output contract for the retrieval response along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KnowledgeAgentRetrievalResponse> retrieveWithResponse(
        KnowledgeAgentRetrievalRequest retrievalRequest, String xMsQuerySourceAuthorization, Context context) {
        return retrievals.retrieveWithResponse(retrievalRequest, xMsQuerySourceAuthorization, null, context);
    }
}
