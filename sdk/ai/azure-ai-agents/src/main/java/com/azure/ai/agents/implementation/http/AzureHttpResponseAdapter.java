// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.http;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;
import com.openai.core.http.Headers;
import com.openai.core.http.HttpResponse;

import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Adapter that exposes an Azure {@link com.azure.core.http.HttpResponse} as an OpenAI {@link HttpResponse}. This keeps
 * the translation logic encapsulated so response handling elsewhere can remain framework agnostic.
 */
final class AzureHttpResponseAdapter implements HttpResponse {

    private static final ClientLogger LOGGER = new ClientLogger(AzureHttpResponseAdapter.class);

    private final com.azure.core.http.HttpResponse azureResponse;
    private final Consumer<String> bodyLogger;

    /**
     * Creates a new adapter instance for the provided Azure response.
     *
     * @param azureResponse Response returned by the Azure pipeline.
     */
    AzureHttpResponseAdapter(com.azure.core.http.HttpResponse azureResponse) {
        this(azureResponse, false);
    }

    AzureHttpResponseAdapter(com.azure.core.http.HttpResponse azureResponse, boolean logBody) {
        this(azureResponse,
            logBody && isEventStream(azureResponse)
                ? value -> LOGGER.info("OpenAI response body chunk: {}", value)
                : null);
    }

    private static boolean isEventStream(com.azure.core.http.HttpResponse response) {
        String contentType = response.getHeaderValue("Content-Type");
        return contentType != null && "text/event-stream".equalsIgnoreCase(contentType.split(";", 2)[0].trim());
    }

    AzureHttpResponseAdapter(com.azure.core.http.HttpResponse azureResponse, Consumer<String> bodyLogger) {
        this.azureResponse = azureResponse;
        this.bodyLogger = bodyLogger;
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
        InputStream stream = azureResponse.getBodyAsInputStreamSync();
        if (bodyLogger == null) {
            return stream;
        }
        return new FilterInputStream(stream) {
            private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
            private final ByteBuffer pending = ByteBuffer.allocate(1024);
            private final CharBuffer decoded = CharBuffer.allocate(1024);
            private boolean finished;

            @Override
            public int read() throws IOException {
                int value = in.read();
                if (value != -1) {
                    pending.put((byte) value);
                }
                logDecoded(value == -1);
                return value;
            }

            @Override
            public int read(byte[] bytes, int offset, int length) throws IOException {
                int count = in.read(bytes, offset, length);
                int consumed = 0;
                while (consumed < count) {
                    int size = Math.min(count - consumed, pending.remaining());
                    pending.put(bytes, offset + consumed, size);
                    consumed += size;
                    logDecoded(false);
                }
                if (count == -1) {
                    logDecoded(true);
                }
                return count;
            }

            private void logDecoded(boolean endOfInput) {
                if (finished) {
                    return;
                }
                pending.flip();
                decoder.decode(pending, decoded, endOfInput);
                pending.compact();
                if (endOfInput) {
                    decoder.flush(decoded);
                    finished = true;
                }
                decoded.flip();
                if (decoded.hasRemaining()) {
                    bodyLogger.accept(decoded.toString());
                }
                decoded.clear();
            }
        };
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
