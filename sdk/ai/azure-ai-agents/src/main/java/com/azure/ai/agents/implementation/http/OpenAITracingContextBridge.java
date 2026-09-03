// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.http;

import com.azure.core.util.Context;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Carries an Azure tracing context across openai-java's asynchronous request preparation boundary.
 *
 * <p>The opaque header is consumed by {@link HttpClientHelper} and is never sent to the service. Entries are scoped
 * to a single client and removed either when the HTTP adapter consumes them or when the traced operation ends.</p>
 */
public final class OpenAITracingContextBridge {
    /**
     * Internal header used only between openai-java request preparation and the Azure HTTP adapter.
     */
    public static final String TRACE_CONTEXT_HEADER = "x-ms-azure-ai-agents-trace-context";

    private final ConcurrentMap<String, Context> contexts = new ConcurrentHashMap<>();

    /**
     * Registers a context and returns its opaque request token.
     *
     * @param context the Azure tracing context.
     * @return the opaque request token.
     */
    public String register(Context context) {
        String token = UUID.randomUUID().toString();
        contexts.put(token, context);
        return token;
    }

    /**
     * Removes and returns the context associated with a request token.
     *
     * @param token the opaque request token.
     * @return the context, or {@link Context#NONE} when the token is absent or already consumed.
     */
    public Context take(String token) {
        if (token == null) {
            return Context.NONE;
        }
        Context context = contexts.remove(token);
        return context == null ? Context.NONE : context;
    }

    /**
     * Removes an unconsumed request token.
     *
     * @param token the opaque request token.
     */
    public void discard(String token) {
        if (token != null) {
            contexts.remove(token);
        }
    }
}
