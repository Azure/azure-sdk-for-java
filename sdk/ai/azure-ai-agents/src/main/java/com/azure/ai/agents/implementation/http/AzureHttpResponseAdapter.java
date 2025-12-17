// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.http;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;
import com.openai.core.http.Headers;
import com.openai.core.http.HttpResponse;

import java.io.InputStream;

/**
 * Adapter that exposes an Azure {@link com.azure.core.http.HttpResponse} as an OpenAI {@link HttpResponse}. This keeps
 * the translation logic encapsulated so response handling elsewhere can remain framework agnostic.
 */
final class AzureHttpResponseAdapter implements HttpResponse {

    private static final ClientLogger LOGGER = new ClientLogger(AzureHttpResponseAdapter.class);

    private final com.azure.core.http.HttpResponse azureResponse;

    /**
     * Creates a new adapter instance for the provided Azure response.
     *
     * @param azureResponse Response returned by the Azure pipeline.
     */
    AzureHttpResponseAdapter(com.azure.core.http.HttpResponse azureResponse) {
        this.azureResponse = azureResponse;
    }

    @Override
    public int statusCode() {
        return azureResponse.getStatusCode();
    }

    @Override
    public Headers headers() {
        return toOpenAiHeaders(azureResponse.getHeaders());
    }

    @Override
    public InputStream body() {
        return azureResponse.getBodyAsBinaryData().toStream();
    }

    @Override
    public void close() {
        azureResponse.close();
    }

    /**
     * Copies headers from the Azure response into the immutable OpenAI {@link Headers} collection.
     */
    private static Headers toOpenAiHeaders(HttpHeaders httpHeaders) {
        Headers.Builder builder = Headers.builder();
        for (HttpHeader header : httpHeaders) {
            builder.put(header.getName(), header.getValuesList());
        }
        return builder.build();
    }
}
