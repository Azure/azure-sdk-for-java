// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.http;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;
import com.openai.core.http.Headers;
import com.openai.core.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Adapter that exposes an Azure {@link com.azure.core.http.HttpResponse} as an OpenAI {@link HttpResponse}. This keeps
 * the translation logic encapsulated so response handling elsewhere can remain framework agnostic.
 */
final class AzureHttpResponseAdapter implements HttpResponse {

    private static final ClientLogger LOGGER = new ClientLogger(AzureHttpResponseAdapter.class);

    private final com.azure.core.http.HttpResponse azureResponse;
    private final Headers headers;
    private final InputStream bodyStream;

    /**
     * Creates a new adapter instance for the provided Azure response.
     *
     * @param azureResponse Response returned by the Azure pipeline.
     */
    AzureHttpResponseAdapter(com.azure.core.http.HttpResponse azureResponse) {
        this.azureResponse = azureResponse;
        this.headers = toOpenAiHeaders(azureResponse.getHeaders());
        this.bodyStream = azureResponse.getBodyAsBinaryData().toStream();
    }

    @Override
    public int statusCode() {
        return azureResponse.getStatusCode();
    }

    @Override
    public Headers headers() {
        return headers;
    }

    @Override
    public InputStream body() {
        return bodyStream;
    }

    @Override
    public void close() {
        try {
            bodyStream.close();
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsWarning(new UncheckedIOException("Failed to close response body stream", ex));
        }
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
