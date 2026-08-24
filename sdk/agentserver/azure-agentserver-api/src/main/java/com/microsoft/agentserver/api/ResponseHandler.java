// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

/**
 * Handler interface for processing incoming response requests.
 * <p>
 * Implementors must override at least one of the two methods:
 * <ul>
 *   <li>{@link #createAsync} — for streaming responses (SSE)</li>
 *   <li>{@link #createResponse} — for non-streaming (synchronous) responses</li>
 * </ul>
 */
public interface ResponseHandler {

    /**
     * Creates a streaming response from a {@link AgentServerCreateResponse}.
     * Used by the server adapter when dispatching incoming streaming HTTP requests.
     *
     * @param responseContext the context for this response (ID, provider, history)
     * @param request         the deserialized create-response request
     * @return an event stream that produces SSE events as the response is generated
     * @throws UnsupportedOperationException if the handler does not support streaming
     */
    default ResponseEventStream createAsync(
        ResponseContext responseContext,
        AgentServerCreateResponse request
    ) {
        throw new UnsupportedOperationException(
            "Streaming responses (createAsync) are not implemented by this handler. "
                + "Override createAsync() to support streaming.");
    }

    /**
     * Creates a synchronous (non-streaming) response from a {@link AgentServerCreateResponse}.
     * Used by the server adapter when dispatching incoming non-streaming HTTP requests.
     *
     * @param responseContext the context for this response (ID, provider, history)
     * @param request         the deserialized create-response request
     * @return the completed response
     * @throws UnsupportedOperationException if the handler does not support synchronous responses
     */
    default CreateResponse createResponse(
        ResponseContext responseContext,
        AgentServerCreateResponse request
    ) {
        throw new UnsupportedOperationException(
            "Synchronous responses (createResponse) are not implemented by this handler. "
                + "Override createResponse() to support non-streaming.");
    }
}
