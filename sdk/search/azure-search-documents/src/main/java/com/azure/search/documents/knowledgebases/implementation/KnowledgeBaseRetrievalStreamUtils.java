// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent;
import com.azure.search.documents.models.ServerSentEvent;
import com.azure.search.documents.models.ServerSentEventListener;
import com.azure.search.documents.models.implementation.sse.ServerSentEventStreams;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Supports the knowledge base retrieval stream convenience APIs.
 */
public final class KnowledgeBaseRetrievalStreamUtils {
    private static final HttpHeaderName QUERY_SOURCE_AUTHORIZATION
        = HttpHeaderName.fromString("x-ms-query-source-authorization");
    private static final HttpHeaderName QUERY_WORK_IQ_SOURCE_AUTHORIZATION
        = HttpHeaderName.fromString("x-ms-query-work-iq-source-authorization");

    private KnowledgeBaseRetrievalStreamUtils() {
    }

    /**
     * Creates request options for the knowledge base retrieval stream.
     *
     * @param querySourceAuthorization The query source authorization token.
     * @param queryWorkIQSourceAuthorization The Work IQ query source authorization token.
     * @return The request options.
     */
    public static RequestOptions createRequestOptions(String querySourceAuthorization,
        String queryWorkIQSourceAuthorization) {
        RequestOptions requestOptions = new RequestOptions();
        if (querySourceAuthorization != null) {
            requestOptions.setHeader(QUERY_SOURCE_AUTHORIZATION, querySourceAuthorization);
        }
        if (queryWorkIQSourceAuthorization != null) {
            requestOptions.setHeader(QUERY_WORK_IQ_SOURCE_AUTHORIZATION, queryWorkIQSourceAuthorization);
        }
        return requestOptions;
    }

    /**
     * Converts a streaming response into typed knowledge base retrieval events.
     *
     * @param response The streaming response.
     * @return A stream of typed knowledge base retrieval events.
     */
    public static Flux<ServerSentEvent<KnowledgeBaseRetrievalStreamEvent>> toFlux(Mono<Response<BinaryData>> response) {
        return response.flatMapMany(value -> ServerSentEventStreams.toFlux(value,
            KnowledgeBaseRetrievalStreamEventConverter::convert, event -> event.getData().isTerminal()));
    }

    /**
     * Delivers typed knowledge base retrieval events from a streaming response.
     *
     * @param response The streaming response.
     * @param listener The listener that receives events and lifecycle notifications.
     */
    public static void listen(Response<BinaryData> response,
        ServerSentEventListener<KnowledgeBaseRetrievalStreamEvent> listener) {
        ServerSentEventStreams.listen(response, KnowledgeBaseRetrievalStreamEventConverter::convert,
            event -> event.getData().isTerminal(), listener);
    }
}
