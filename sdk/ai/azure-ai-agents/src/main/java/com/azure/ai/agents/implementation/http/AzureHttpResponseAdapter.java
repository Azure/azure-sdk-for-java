// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.http;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.openai.core.http.Headers;
import com.openai.core.http.HttpResponse;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

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
        // getBodyAsBinaryData().toStream() blocks until the entire Flux<ByteBuffer>
        // drains (FluxByteBufferContent.toStream() collects into a byte[] via .block()),
        // which breaks SSE progressive delivery. Iterate the Flux lazily so chunks
        // reach the SSE parser as they arrive on the wire.
        Flux<ByteBuffer> body = azureResponse.getBody();
        if (LOGGER.canLogAtLevel(LogLevel.VERBOSE)) {
            body = tapLines(body);
        }
        return new FluxByteBufferInputStream(body.toIterable().iterator());
    }

    /**
     * Taps the body Flux to log each complete {@code \n}-terminated line at VERBOSE. A
     * {@link StringBuilder} buffers partial lines across chunk boundaries so a line
     * split across two {@link ByteBuffer}s is logged once, on arrival of the chunk
     * that completes it.
     */
    private static Flux<ByteBuffer> tapLines(Flux<ByteBuffer> body) {
        StringBuilder lineBuffer = new StringBuilder();
        return body.doOnNext(buffer -> {
            ByteBuffer view = buffer.duplicate();
            byte[] bytes = new byte[view.remaining()];
            view.get(bytes);
            lineBuffer.append(new String(bytes, StandardCharsets.UTF_8));
            int newline;
            while ((newline = lineBuffer.indexOf("\n")) >= 0) {
                String line = lineBuffer.substring(0, newline);
                lineBuffer.delete(0, newline + 1);
                if (!line.isEmpty()) {
                    LOGGER.verbose("SSE line: {}", line);
                }
            }
        }).doOnComplete(() -> {
            if (lineBuffer.length() > 0) {
                LOGGER.verbose("SSE line (trailing, no newline): {}", lineBuffer);
            }
        });
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

    /**
     * {@link InputStream} that lazily pulls {@link ByteBuffer} chunks from a reactor
     * {@code Flux<ByteBuffer>} iterator. Each {@code hasNext()} parks until the next
     * {@code onNext} signal, so reads complete as soon as a chunk arrives on the wire
     * rather than waiting for the full response to be buffered.
     */
    private static final class FluxByteBufferInputStream extends InputStream {

        private final Iterator<ByteBuffer> buffers;
        private ByteBuffer current;

        FluxByteBufferInputStream(Iterator<ByteBuffer> buffers) {
            this.buffers = buffers;
        }

        @Override
        public int read() {
            ByteBuffer buffer = nextBuffer();
            if (buffer == null) {
                return -1;
            }
            return buffer.get() & 0xff;
        }

        @Override
        public int read(byte[] out, int offset, int length) {
            // Return after draining the current buffer. Do NOT loop into nextBuffer()
            // to top up `length` — that would block on the Flux waiting for the next
            // chunk and stall SSE reads until BufferedReader's 8KB request is filled,
            // which defeats progressive delivery.
            ByteBuffer buffer = nextBuffer();
            if (buffer == null) {
                return -1;
            }
            int toTransfer = Math.min(buffer.remaining(), length);
            buffer.get(out, offset, toTransfer);
            return toTransfer;
        }

        @Override
        public int available() {
            ByteBuffer buffer = current;
            return buffer == null ? 0 : buffer.remaining();
        }

        private ByteBuffer nextBuffer() {
            if (current != null && current.hasRemaining()) {
                return current;
            }
            while (buffers.hasNext()) {
                ByteBuffer candidate = buffers.next();
                if (candidate.hasRemaining()) {
                    current = candidate.duplicate();
                    return current;
                }
            }
            current = null;
            return null;
        }
    }
}
