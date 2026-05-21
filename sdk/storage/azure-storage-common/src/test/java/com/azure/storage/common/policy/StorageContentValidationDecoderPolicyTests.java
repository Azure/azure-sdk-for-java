// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageEncoder;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageFlags;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StorageContentValidationDecoderPolicyTests {

    private static HttpRequest getRequest() {
        try {
            return new HttpRequest(HttpMethod.GET, new URL("http://example.com/blob"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] encodeToBytes(byte[] data, int segmentSize, StructuredMessageFlags flags) throws IOException {
        StructuredMessageEncoder encoder = new StructuredMessageEncoder(data.length, segmentSize, flags);
        Flux<ByteBuffer> flux = encoder.encode(ByteBuffer.wrap(data));
        ByteBuffer encoded
            = ByteBuffer.wrap(Objects.requireNonNull(FluxUtil.collectBytesInByteBufferStream(flux).block()));
        byte[] bytes = new byte[encoded.remaining()];
        encoded.get(bytes);
        return bytes;
    }

    @Test
    public void contentLengthIsOverriddenToDecodedSizeWhenDecodingApplied() throws IOException {
        byte[] payload = new byte[64];
        ThreadLocalRandom.current().nextBytes(payload);

        byte[] encoded = encodeToBytes(payload, 64, StructuredMessageFlags.STORAGE_CRC64);
        long encodedLen = encoded.length;
        long decodedLen = payload.length;

        HttpHeaders responseHeaders = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(encodedLen))
            .set(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME,
                StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE)
            .set(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME, String.valueOf(decodedLen));

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new StorageContentValidationDecoderPolicy())
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200, responseHeaders, encoded)))
            .build();

        Context ctx = new Context(StructuredMessageConstants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY, true);
        HttpResponse response = pipeline.send(getRequest(), ctx).block();

        assertNotNull(response);
        assertEquals(String.valueOf(decodedLen), response.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @Test
    public void contentLengthMatchesActualDecodedBodySize() throws IOException {
        byte[] payload = new byte[128];
        ThreadLocalRandom.current().nextBytes(payload);

        byte[] encoded = encodeToBytes(payload, 64, StructuredMessageFlags.STORAGE_CRC64);
        long encodedLen = encoded.length;
        long decodedLen = payload.length;

        HttpHeaders responseHeaders = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(encodedLen))
            .set(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME,
                StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE)
            .set(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME, String.valueOf(decodedLen));

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new StorageContentValidationDecoderPolicy())
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200, responseHeaders, encoded)))
            .build();

        Context ctx = new Context(StructuredMessageConstants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY, true);
        HttpResponse response = pipeline.send(getRequest(), ctx).block();

        assertNotNull(response);
        assertEquals(String.valueOf(decodedLen), response.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));

        // Consume the body and verify it matches the original payload — the reported Content-Length
        // must equal the actual number of bytes produced by the decoder.
        byte[] body = Objects.requireNonNull(FluxUtil.collectBytesInByteBufferStream(response.getBody()).block());
        assertEquals(decodedLen, body.length);
        assertArrayEquals(payload, body);
    }

    @Test
    public void contentLengthIsUnchangedWhenDecodingNotApplied() throws IOException {
        byte[] payload = new byte[64];
        ThreadLocalRandom.current().nextBytes(payload);

        byte[] encoded = encodeToBytes(payload, 64, StructuredMessageFlags.STORAGE_CRC64);
        long encodedLen = encoded.length;

        HttpHeaders responseHeaders = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(encodedLen));

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new StorageContentValidationDecoderPolicy())
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200, responseHeaders, encoded)))
            .build();

        // No decoding context flag set — policy should pass the response through unchanged.
        HttpResponse response = pipeline.send(getRequest()).block();

        assertNotNull(response);
        assertEquals(String.valueOf(encodedLen), response.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
    }
}
