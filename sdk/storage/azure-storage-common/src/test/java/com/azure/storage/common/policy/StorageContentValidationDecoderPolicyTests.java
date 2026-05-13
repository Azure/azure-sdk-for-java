// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpClient;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link StorageContentValidationDecoderPolicy} together with {@link StructuredMessageEncoder} /
 * wire-format payloads so the reactive decode path matches what the blob download pipeline uses.
 */
public class StorageContentValidationDecoderPolicyTests {

    /**
     * End-to-end through the policy: encoded body uses multi-megabyte segment payload lengths (not the default
     * 4 MiB framing only); decoded flux must match the original bytes.
     */
    @ParameterizedTest
    @MethodSource("segmentPayloadSizeAndTotalPayloadSizeSupplier")
    public void decodesDynamicallySizedSegmentStructuredMessageThroughPipeline(int segmentPayloadSize,
        int totalPayloadSize) throws IOException {
        byte[] originalData = new byte[totalPayloadSize];
        ThreadLocalRandom.current().nextBytes(originalData);

        byte[] encodedBytes
            = encodeStructuredMessageWireBytes(originalData, segmentPayloadSize, StructuredMessageFlags.STORAGE_CRC64);

        AtomicReference<HttpRequest> requestAfterPolicies = new AtomicReference<>();
        HttpClient httpClient = request -> {
            requestAfterPolicies.set(request);
            HttpHeaders headers = structuredDownloadResponseHeaders(encodedBytes.length, totalPayloadSize);
            return Mono.just(new MockHttpResponse(request, 200, headers, encodedBytes));
        };

        HttpPipeline pipeline = new HttpPipelineBuilder().policies((context, next) -> {
            context.setData(StructuredMessageConstants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY, true);
            return next.process();
        }, new StorageContentValidationDecoderPolicy()).httpClient(httpClient).build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://example.blob.core.windows.net/c/b");
        try (HttpResponse response = pipeline.send(request, Context.NONE).block()) {
            assertNotNull(response);
            assertTrue(response instanceof DecodedResponse);
            byte[] decoded = Objects.requireNonNull(response.getBodyAsByteArray().block());
            assertArrayEquals(originalData, decoded);
        }

        HttpRequest sent = requestAfterPolicies.get();
        assertNotNull(sent);
        assertEquals(StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE,
            sent.getHeaders().getValue(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME));
    }

    private static Stream<Arguments> segmentPayloadSizeAndTotalPayloadSizeSupplier() {
        return Stream.of(Arguments.of(10 * 1024 * 1024, 10 * 1024 * 1024 + 1), //larger than 4 Mib
            Arguments.of(3 * 1024 * 1024, 3 * 1024 * 1024 + 1), //smaller than 4 Mib, but not kb
            Arguments.of(5 * 1024 * 1024 + 1, 15 * 1024 * 1024));
    }

    private static HttpHeaders structuredDownloadResponseHeaders(int contentLength, long structuredContentLength) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength));
        headers.set(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME,
            StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE);
        headers.set(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME,
            String.valueOf(structuredContentLength));
        return headers;
    }

    private static byte[] encodeStructuredMessageWireBytes(byte[] originalData, int segmentLength,
        StructuredMessageFlags flags) throws IOException {
        StructuredMessageEncoder encoder = new StructuredMessageEncoder(originalData.length, segmentLength, flags);
        Flux<ByteBuffer> flux = encoder.encode(ByteBuffer.wrap(originalData));
        return Objects.requireNonNull(FluxUtil.collectBytesInByteBufferStream(flux).block());
    }
}
